package org.apache.camel.processor.aggregate.cassandra;

import com.google.common.io.ByteStreams;
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
import java.nio.ByteBuffer;

public class CassandraCamelCodecTest extends CamelTestSupport {

	CassandraCamelCodec codec;

	@Override
	protected void startCamelContext() throws Exception {
		super.startCamelContext();
		codec = new CassandraCamelCodec();
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
			codec.unmarshallExchange(context, ByteBuffer.wrap(ByteStreams.toByteArray(is)),
					"java.**;org.apache.camel.**;!*");
		});

		Assert.assertEquals("filter status: REJECTED", thrown.getMessage());
	}
}
