package org.apache.camel;

import org.apache.camel.builder.ExpressionBuilder;
import org.apache.camel.impl.DefaultExchange;

/**
 * @version $Revision$
 */
public class CamelExceptionsTest extends ContextTestSupport {

    public void testExpectedBodyTypeException() {
        Exchange exchange = new DefaultExchange(context);

        ExpectedBodyTypeException e = new ExpectedBodyTypeException(exchange, Integer.class);
        assertSame(exchange, e.getExchange());
        assertEquals(Integer.class, e.getExpectedBodyType());
    }

    public void testExpressionEvaluationException() {
        Expression exp = ExpressionBuilder.constantExpression("foo");
        Exchange exchange = new DefaultExchange(context);

        ExpressionEvaluationException e = new ExpressionEvaluationException(exp, exchange, new IllegalArgumentException("Damn"));
        assertSame(exchange ,e.getExchange());
        assertSame(exp ,e.getExpression());
        assertNotNull(e.getCause());
    }

    public void testFailedToCreateConsumerException() {
        Endpoint endpoint = context.getEndpoint("seda:foo");
        FailedToCreateConsumerException e = new FailedToCreateConsumerException(endpoint, new IllegalArgumentException("Damn"));

        assertEquals(endpoint.getEndpointUri(), e.getUri());
        assertNotNull(e.getCause());
    }

    public void testFailedToCreateProducerException() {
        Endpoint endpoint = context.getEndpoint("seda:foo");
        FailedToCreateProducerException e = new FailedToCreateProducerException(endpoint, new IllegalArgumentException("Damn"));

        assertEquals(endpoint.getEndpointUri(), e.getUri());
        assertNotNull(e.getCause());
    }

    public void testInvalidPayloadRuntimeException() {
        Exchange exchange = new DefaultExchange(context);

        InvalidPayloadRuntimeException e = new InvalidPayloadRuntimeException(exchange, Integer.class);
        assertSame(exchange, e.getExchange());
        assertEquals(Integer.class, e.getType());
    }

    public void testRuntimeTransformException() {
        RuntimeTransformException e = new RuntimeTransformException("Forced");
        assertEquals("Forced", e.getMessage());
        assertNull(e.getCause());

        RuntimeTransformException e2 = new RuntimeTransformException("Forced", new IllegalAccessException("Damn"));
        assertEquals("Forced", e2.getMessage());
        assertNotNull(e2.getCause());

        RuntimeTransformException e3 = new RuntimeTransformException(new IllegalAccessException("Damn"));
        assertEquals("java.lang.IllegalAccessException: Damn", e3.getMessage());
        assertNotNull(e3.getCause());
    }
    
    public void testRuntimeExpressionException() {
        RuntimeExpressionException e = new RuntimeExpressionException("Forced");
        assertEquals("Forced", e.getMessage());
        assertNull(e.getCause());

        RuntimeExpressionException e2 = new RuntimeExpressionException("Forced", new IllegalAccessException("Damn"));
        assertEquals("Forced", e2.getMessage());
        assertNotNull(e2.getCause());

        RuntimeExpressionException e3 = new RuntimeExpressionException(new IllegalAccessException("Damn"));
        assertEquals("java.lang.IllegalAccessException: Damn", e3.getMessage());
        assertNotNull(e3.getCause());
    }

    public void testRollbackExchangeException() {
        Exchange exchange = new DefaultExchange(context);

        RollbackExchangeException e = new RollbackExchangeException(exchange, new IllegalAccessException("Damn"));
        assertNotNull(e.getMessage());
        assertSame(exchange, e.getExchange());

        RollbackExchangeException e2 = new RollbackExchangeException("Forced", exchange, new IllegalAccessException("Damn"));
        assertNotNull(e2.getMessage());
        assertSame(exchange, e2.getExchange());
    }

    public void testValidationException() {
        Exchange exchange = new DefaultExchange(context);

        ValidationException e = new ValidationException(exchange, "Forced");
        assertNotNull(e.getMessage());
        assertSame(exchange, e.getExchange());

        ValidationException e2 = new ValidationException("Forced", exchange, new IllegalAccessException("Damn"));
        assertNotNull(e2.getMessage());
        assertSame(exchange, e2.getExchange());
    }

    public void testNoSuchBeanException() {
        NoSuchBeanException e = new NoSuchBeanException("foo");
        assertEquals("foo", e.getName());
        assertNull(e.getCause());

        NoSuchBeanException e2 = new NoSuchBeanException("foo", new IllegalArgumentException("Damn"));
        assertEquals("foo", e2.getName());
        assertNotNull(e2.getCause());
    }

    public void testCamelExecutionException() {
        Exchange exchange = new DefaultExchange(context);

        CamelExecutionException e = new CamelExecutionException("Forced", exchange);
        assertNotNull(e.getMessage());
        assertSame(exchange, e.getExchange());
        assertNull(e.getCause());

        CamelExecutionException e2 = new CamelExecutionException("Forced", exchange, new IllegalArgumentException("Damn"));
        assertNotNull(e2.getMessage());
        assertSame(exchange, e2.getExchange());
        assertNotNull(e2.getCause());
    }

    public void testCamelException() {
        CamelException e = new CamelException();
        assertNull(e.getCause());

        CamelException e2 = new CamelException("Forced");
        assertNull(e2.getCause());
        assertEquals("Forced", e2.getMessage());
        
        CamelException e3 = new CamelException("Forced", new IllegalArgumentException("Damn"));
        assertNotNull(e3.getCause());
        assertEquals("Forced", e3.getMessage());

        CamelException e4 = new CamelException(new IllegalArgumentException("Damn"));
        assertNotNull(e4.getCause());
        assertNotNull(e4.getMessage());
    }

    public void testServiceStatus() {
        assertTrue(ServiceStatus.Started.isStarted());
        assertFalse(ServiceStatus.Starting.isStarted());
        assertFalse(ServiceStatus.Stopped.isStarted());
        assertFalse(ServiceStatus.Stopping.isStarted());

        assertTrue(ServiceStatus.Stopped.isStopped());
        assertFalse(ServiceStatus.Starting.isStopped());
        assertFalse(ServiceStatus.Started.isStopped());
        assertFalse(ServiceStatus.Stopping.isStopped());

        assertTrue(ServiceStatus.Stopped.isStartable());
        assertFalse(ServiceStatus.Started.isStartable());
        assertFalse(ServiceStatus.Starting.isStartable());
        assertFalse(ServiceStatus.Stopping.isStartable());

        assertTrue(ServiceStatus.Started.isStoppable());
        assertTrue(ServiceStatus.Starting.isStoppable());
        assertFalse(ServiceStatus.Stopped.isStoppable());
        assertFalse(ServiceStatus.Stopping.isStoppable());
    }

    public void testRuntimeExchangeException() {
        Exchange exchange = new DefaultExchange(context);

        RuntimeExchangeException e = new RuntimeExchangeException("Forced", exchange);
        assertNotNull(e.getMessage());
        assertSame(exchange, e.getExchange());

        RuntimeExchangeException e2 = new RuntimeExchangeException("Forced", null);
        assertNotNull(e2.getMessage());
        assertNull(e2.getExchange());
    }

    public void testExchangePattern() {
        assertTrue(ExchangePattern.InOnly.isInCapable());
        assertTrue(ExchangePattern.InOptionalOut.isInCapable());
        assertTrue(ExchangePattern.InOut.isInCapable());

        assertFalse(ExchangePattern.InOnly.isFaultCapable());
        assertTrue(ExchangePattern.InOptionalOut.isFaultCapable());
        assertTrue(ExchangePattern.InOut.isFaultCapable());

        assertFalse(ExchangePattern.InOnly.isOutCapable());
        assertTrue(ExchangePattern.InOptionalOut.isOutCapable());
        assertTrue(ExchangePattern.InOut.isOutCapable());

        assertEquals(ExchangePattern.InOnly, ExchangePattern.asEnum("InOnly"));
        assertEquals(ExchangePattern.InOut, ExchangePattern.asEnum("InOut"));

        try {
            ExchangePattern.asEnum("foo");
            fail("Should have thrown an exception");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    public void testInvalidPayloadException() {
        Exchange exchange = new DefaultExchange(context);

        InvalidPayloadException e = new InvalidPayloadException(exchange, Integer.class);
        assertSame(exchange, e.getExchange());
        assertEquals(Integer.class, e.getType());
    }

    public void testExchangeTimedOutException() {
        Exchange exchange = new DefaultExchange(context);

        ExchangeTimedOutException e = new ExchangeTimedOutException(exchange, 5000);
        assertSame(exchange, e.getExchange());
        assertEquals(5000, e.getTimeout());
    }

    public void testExpressionIllegalSyntaxException() {
        ExpressionIllegalSyntaxException e = new ExpressionIllegalSyntaxException("foo");
        assertEquals("foo", e.getExpression());
    }

    public void testNoFactoryAvailableException() {
        NoFactoryAvailableException e = new NoFactoryAvailableException("killer", new IllegalArgumentException("Damn"));
        assertNotNull(e.getCause());
        assertEquals("killer", e.getUri());
    }

    public void testCamelExchangeException() {
        Exchange exchange = new DefaultExchange(context);

        CamelExchangeException e = new CamelExchangeException("Forced", exchange);
        assertNotNull(e.getMessage());
        assertSame(exchange, e.getExchange());
    }

    public void testNoSuchHeaderException() {
        Exchange exchange = new DefaultExchange(context);

        NoSuchHeaderException e = new NoSuchHeaderException(exchange, "foo", Integer.class);
        assertEquals(Integer.class, e.getType());
        assertEquals("foo", e.getHeaderName());
        assertSame(exchange, e.getExchange());
    }
    
    public void testNoSuchPropertyException() {
        Exchange exchange = new DefaultExchange(context);

        NoSuchPropertyException e = new NoSuchPropertyException(exchange, "foo", Integer.class);
        assertEquals(Integer.class, e.getType());
        assertEquals("foo", e.getPropertyName());
        assertSame(exchange, e.getExchange());
    }

}
