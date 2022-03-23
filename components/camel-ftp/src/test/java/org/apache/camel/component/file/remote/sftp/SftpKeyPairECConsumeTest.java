/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.file.remote.sftp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.impl.PropertyPlaceholderDelegateRegistry;
import org.apache.camel.util.IOHelper;
import org.apache.sshd.common.util.security.SecurityUtils;
import org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.BeforeClass;
import org.junit.Test;

public class SftpKeyPairECConsumeTest extends SftpServerTestSupport {

    private static KeyPair keyPair;
    private static PublicKey ecPublicKey;
    private static ByteArrayOutputStream privateKey = new ByteArrayOutputStream();

    @BeforeClass
    public static void createKeys() throws Exception {
        // default EC KeyPairGenerator returns this ASN.1 structure (PrivateKey.getEncoded()):
        // $ xclip -o | base64 -d | openssl asn1parse -inform der -i
        //    0:d=0  hl=2 l=  96 cons: SEQUENCE
        //    2:d=1  hl=2 l=   1 prim:  INTEGER           :00
        //    5:d=1  hl=2 l=  16 cons:  SEQUENCE
        //    7:d=2  hl=2 l=   7 prim:   OBJECT            :id-ecPublicKey
        //   16:d=2  hl=2 l=   5 prim:   OBJECT            :secp521r1
        //   23:d=1  hl=2 l=  73 prim:  OCTET STRING      [HEX DUMP]:30470201010442006659F1D83A914AFDF5B92A031F8...
        // $ xclip -o | base64 -d | openssl asn1parse -inform der -i -strparse 23
        //    0:d=0  hl=2 l=  71 cons: SEQUENCE
        //    2:d=1  hl=2 l=   1 prim:  INTEGER           :01
        //    5:d=1  hl=2 l=  66 prim:  OCTET STRING      [HEX DUMP]:006659F1D83A914AFDF5B92A031F8B478738B376B63...
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
        keyGen.initialize(521);
        keyGen.generateKeyPair();

        // BouncyCastle EC KeyPairGenerator returns this ASN.1 structure (PrivateKey.getEncoded()):
        // $ xclip -o | base64 -d | openssl asn1parse -inform der -i
        //    0:d=0  hl=3 l= 247 cons: SEQUENCE
        //    3:d=1  hl=2 l=   1 prim:  INTEGER           :00
        //    6:d=1  hl=2 l=  16 cons:  SEQUENCE
        //    8:d=2  hl=2 l=   7 prim:   OBJECT            :id-ecPublicKey
        //   17:d=2  hl=2 l=   5 prim:   OBJECT            :secp521r1
        //   24:d=1  hl=3 l= 223 prim:  OCTET STRING      [HEX DUMP]:3081DC0201010442003A93246A8E4E7AC6B8E62276F...
        // $ xclip -o | base64 -d | openssl asn1parse -inform der -i -strparse 24
        //    0:d=0  hl=3 l= 220 cons: SEQUENCE
        //    3:d=1  hl=2 l=   1 prim:  INTEGER           :01
        //    6:d=1  hl=2 l=  66 prim:  OCTET STRING      [HEX DUMP]:003A93246A8E4E7AC6B8E62276F4E730463DE08BAB1...
        //   74:d=1  hl=2 l=   7 cons:  cont [ 0 ]
        //   76:d=2  hl=2 l=   5 prim:   OBJECT            :secp521r1
        //   83:d=1  hl=3 l= 137 cons:  cont [ 1 ]
        //   86:d=2  hl=3 l= 134 prim:   BIT STRING

        KeyPairGenerator keyGenBc = KeyPairGenerator.getInstance("EC", new BouncyCastleProvider());
        keyGenBc.initialize(521);
        keyGenBc.generateKeyPair();

        // this works better, it generates ASN.1 structure (so the same as embedded OCTET STRING in
        // BC-generated EC key structure):
        // $ xclip -o | openssl asn1parse -i
        //    0:d=0  hl=3 l= 220 cons: SEQUENCE
        //    3:d=1  hl=2 l=   1 prim:  INTEGER           :01
        //    6:d=1  hl=2 l=  66 prim:  OCTET STRING      [HEX DUMP]:01F923B0E659D67612C3F695B0DE377AD295D4EEA1E...
        //   74:d=1  hl=2 l=   7 cons:  cont [ 0 ]
        //   76:d=2  hl=2 l=   5 prim:   OBJECT            :secp521r1
        //   83:d=1  hl=3 l= 137 cons:  cont [ 1 ]
        //   86:d=2  hl=3 l= 134 prim:   BIT STRING
        // and a key with "-----BEGIN EC PRIVATE KEY-----"
        com.jcraft.jsch.KeyPair kp = com.jcraft.jsch.KeyPair.genKeyPair(null, com.jcraft.jsch.KeyPair.ECDSA, 521);
        kp.writePrivateKey(privateKey);
        byte[] bytes = kp.getPublicKeyBlob();

        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bytes));
        int pos = 0;
        List<byte[]> keyData = new ArrayList<>();
        while (pos < bytes.length) {
            int length = dis.readInt();
            byte[] data = new byte[length];
            keyData.add(data);
            dis.read(data, 0, length);
            pos += 4 + length;
        }

        int len = keyData.get(2).length - 1; // first octed is compression (0x04 - uncompressed)
        byte[] xp = new byte[len / 2];
        byte[] yp = new byte[len / 2];
        System.arraycopy(keyData.get(2), 1, xp, 0, len / 2);
        System.arraycopy(keyData.get(2), 1 + len / 2, yp, 0, len / 2);

        BigInteger x = new BigInteger(1, xp);
        BigInteger y = new BigInteger(1, yp);
        ECPoint point = new ECPoint(x, y);

        AlgorithmParameters ap = AlgorithmParameters.getInstance("EC");
        ap.init(new ECGenParameterSpec("1.3.132.0.35")); // "nistp521"
        ECParameterSpec spec = ap.getParameterSpec(ECParameterSpec.class);

        KeyFactory kf = KeyFactory.getInstance("EC", new BouncyCastleProvider());
        ecPublicKey = kf.generatePublic(new ECPublicKeySpec(point, spec));
    }

    @Test
    public void testSftpSimpleConsume() throws Exception {
        String expected = "Hello World";

        // create file using regular file
        template.sendBodyAndHeader("file://" + FTP_ROOT_DIR, expected, Exchange.FILE_NAME, "hello.txt");

        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(1);
        mock.expectedHeaderReceived(Exchange.FILE_NAME, "hello.txt");
        mock.expectedBodiesReceived(expected);

        context.startRoute("foo");

        assertMockEndpointsSatisfied();
    }

    private byte[] getBytesFromFile(String filename) throws IOException {
        InputStream input;
        input = new FileInputStream(new File(filename));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        IOHelper.copyAndCloseInput(input, output);
        return output.toByteArray();
    }

    @Override
    protected PublickeyAuthenticator getPublickeyAuthenticator() {
        return (username, key, session) -> key.equals(ecPublicKey);
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        //        StringBuilder sb = new StringBuilder(256);
        //        sb.append("-----BEGIN EC PRIVATE KEY-----").append("\n");
        //        sb.append(Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded())).append("\n");
        //        sb.append("-----END EC PRIVATE KEY-----").append("\n");

        ((JndiRegistry) ((PropertyPlaceholderDelegateRegistry) context.getRegistry()).getRegistry())
                .bind("privateKey", privateKey.toByteArray());
        ((JndiRegistry) ((PropertyPlaceholderDelegateRegistry) context.getRegistry()).getRegistry())
                .bind("knownHosts", getBytesFromFile("./src/test/resources/known_hosts"));

        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("sftp://localhost:" + getPort() + "/" + FTP_ROOT_DIR
                        + "?username=admin&knownHosts=#knownHosts&privateKey=#privateKey&delay=10000&strictHostKeyChecking=yes&useUserKnownHostsFile=false&disconnect=true")
                    .routeId("foo").noAutoStartup()
                    .to("mock:result");
            }
        };
    }
}
