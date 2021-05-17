/* Generated by camel build tools - do NOT edit this file! */
package org.apache.camel.component.vertx;

import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.spi.ExtendedPropertyConfigurerGetter;
import org.apache.camel.spi.PropertyConfigurerGetter;
import org.apache.camel.spi.ConfigurerStrategy;
import org.apache.camel.spi.GeneratedPropertyConfigurer;
import org.apache.camel.util.CaseInsensitiveMap;
import org.apache.camel.support.component.PropertyConfigurerSupport;

/**
 * Generated by camel build tools - do NOT edit this file!
 */
@SuppressWarnings("unchecked")
public class VertxComponentConfigurer extends PropertyConfigurerSupport implements GeneratedPropertyConfigurer, PropertyConfigurerGetter {

    @Override
    public boolean configure(CamelContext camelContext, Object obj, String name, Object value, boolean ignoreCase) {
        VertxComponent target = (VertxComponent) obj;
        switch (ignoreCase ? name.toLowerCase() : name) {
        case "autowiredenabled":
        case "autowiredEnabled": target.setAutowiredEnabled(property(camelContext, boolean.class, value)); return true;
        case "bridgeerrorhandler":
        case "bridgeErrorHandler": target.setBridgeErrorHandler(property(camelContext, boolean.class, value)); return true;
        case "host": target.setHost(property(camelContext, java.lang.String.class, value)); return true;
        case "lazystartproducer":
        case "lazyStartProducer": target.setLazyStartProducer(property(camelContext, boolean.class, value)); return true;
        case "port": target.setPort(property(camelContext, int.class, value)); return true;
        case "timeout": target.setTimeout(property(camelContext, int.class, value)); return true;
        case "vertx": target.setVertx(property(camelContext, io.vertx.core.Vertx.class, value)); return true;
        case "vertxfactory":
        case "vertxFactory": target.setVertxFactory(property(camelContext, io.vertx.core.spi.VertxFactory.class, value)); return true;
        case "vertxoptions":
        case "vertxOptions": target.setVertxOptions(property(camelContext, io.vertx.core.VertxOptions.class, value)); return true;
        default: return false;
        }
    }

    @Override
    public String[] getAutowiredNames() {
        return new String[]{"vertx"};
    }

    @Override
    public Class<?> getOptionType(String name, boolean ignoreCase) {
        switch (ignoreCase ? name.toLowerCase() : name) {
        case "autowiredenabled":
        case "autowiredEnabled": return boolean.class;
        case "bridgeerrorhandler":
        case "bridgeErrorHandler": return boolean.class;
        case "host": return java.lang.String.class;
        case "lazystartproducer":
        case "lazyStartProducer": return boolean.class;
        case "port": return int.class;
        case "timeout": return int.class;
        case "vertx": return io.vertx.core.Vertx.class;
        case "vertxfactory":
        case "vertxFactory": return io.vertx.core.spi.VertxFactory.class;
        case "vertxoptions":
        case "vertxOptions": return io.vertx.core.VertxOptions.class;
        default: return null;
        }
    }

    @Override
    public Object getOptionValue(Object obj, String name, boolean ignoreCase) {
        VertxComponent target = (VertxComponent) obj;
        switch (ignoreCase ? name.toLowerCase() : name) {
        case "autowiredenabled":
        case "autowiredEnabled": return target.isAutowiredEnabled();
        case "bridgeerrorhandler":
        case "bridgeErrorHandler": return target.isBridgeErrorHandler();
        case "host": return target.getHost();
        case "lazystartproducer":
        case "lazyStartProducer": return target.isLazyStartProducer();
        case "port": return target.getPort();
        case "timeout": return target.getTimeout();
        case "vertx": return target.getVertx();
        case "vertxfactory":
        case "vertxFactory": return target.getVertxFactory();
        case "vertxoptions":
        case "vertxOptions": return target.getVertxOptions();
        default: return null;
        }
    }
}

