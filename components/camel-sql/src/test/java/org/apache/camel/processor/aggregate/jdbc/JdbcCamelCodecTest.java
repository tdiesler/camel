package org.apache.camel.processor.aggregate.jdbc;

import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Assert;
import org.junit.Test;
import org.malicious.example.Employee;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.ObjectOutputStream;

public class JdbcCamelCodecTest extends CamelTestSupport {

	JdbcCamelCodec codec;

	@Override
	protected void startCamelContext() throws Exception {
		super.startCamelContext();
		codec = new JdbcCamelCodec();
	}

	@Test
	public void shouldFailWithRejected() throws IOException, ClassNotFoundException {
		Employee emp = new Employee("Mickey", "Mouse");

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);

		oos.writeObject(emp);

		oos.flush();
		oos.close();

		InputStream is = new ByteArrayInputStream(baos.toByteArray());

		InvalidClassException thrown = Assert.assertThrows(InvalidClassException.class, () -> {
			codec.unmarshallExchange(context, toByteArray(is), "java.**;org.apache.camel.**;!*");
		});

		Assert.assertEquals("filter status: REJECTED", thrown.getMessage());
	}

	private static byte[] toByteArray(InputStream from) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		byte[] buf = new byte[8192];
		long total = 0L;

		while(true) {
			int r = from.read(buf);
			if (r == -1) {
				return out.toByteArray();
			}

			out.write(buf, 0, r);
			total += (long)r;
		}
	}
}
