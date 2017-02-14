/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.catalog.nexus;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class LocalFileNexusRepository extends ComponentNexusRepository {

    @Override
    protected URL createNexusUrl() throws MalformedURLException {
        File file = new File("src/test/resources/nexus-sample-result.xml");
        return new URL("file:" + file.getAbsolutePath());
    }

    @Override
    protected String createArtifactURL(NexusArtifactDto dto) {
        // load from local file instead
        return "file:target/localrepo/" + dto.getArtifactId() + "-" + dto.getVersion() + ".jar";
    }
}
