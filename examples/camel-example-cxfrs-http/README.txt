CXF-RS HTTP Client Example
==========================

This example shows how to use the Camel CXF-RS component as a RS client in HTTP centric mode.

This example provides a server and a client.
The server is a REST service which is exposed using pure Apache CXF (no Camel).
The server is a JAX-RS service in the org.fusesource.example.OrderService class.
The server is mapped in the src/webapp/WEB-INF/web.xml file to use a servlet to expose the REST service.

The client is a pure Camel route located in the src/main/resources/META-INF/spring/camel-client.xml file.

You will need to compile this example first:
  mvn compile

To run the server:
  mvn jetty:run

To run the client (server must be started first):
  mvn exec:java -Pclient

This example is not prepared to run in FuseESB/ServiceMix.

To stop the example hit ctrl + c

If you hit any problems please let us know on the Camel Forums
  http://camel.apache.org/discussion-forums.html

Please help us make Apache Camel better - we appreciate any feedback you may
have.  Enjoy!

------------------------
The Camel riders!



