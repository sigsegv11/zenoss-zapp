zenoss-zapp
===========

Base project for creating zenoss REST applications.

Intro
----
A zapp is a standalone java application that provides REST webservices. A zapp is built on the
[Dropwizard][1] framework and adds integration with [Spring][2] to enable
auto configuration and registration of objects in [Dropwizard][1]. A zapp is deployed and run as a self contained jar
with all dependencies and given an optional configuration file.

There is also an zapp-example that demonstrates how to create a zapp project.

Getting Started
---
To get started you need provide an implementation of `org.zenoss.app.AutowiredApp` and optionally an implementation of
`org.zenoss.app.AppConfiguration` if you webservice needs to define configuration properties.

	public class ExampleApp extends AutowiredApp<ExampleAppConfiguration> {

    	public static void main(String[] args) throws Exception {
        	new ExampleApp().run(args);
    	}

	    @Override
    	public String getAppName() {
        	return "Example App";
    	}

	    @Override
    	protected Class<ExampleAppConfiguration> getConfigType() {
        	return ExampleAppConfiguration.class;
	    }
    }

By default the `AutowiredApp` will scan `org.zenoss.app` and it's sub packages for any classes that need to be loaded
via [Spring][2] or registered in [Dropwizard][1].

Registering REST resources
---
The first thing you will probably want to do is provide a REST resource. Here we use [Jersey][3] to implement the rest
resource and the `org.zenoss.dropwizardspring.annotations.Resource` annotation to automatically register the resource in
 [Dropwizard][1].

	@Resource //Annotation ensures it is loaded and registered via Spring
	@Path("/example")
	@Produces(MediaType.APPLICATION_JSON)
	public class ExampleResource {
	
	@Path("/hello")
    @Timed
    @GET
    public String hello(){ return "hello";}
	…

Read the [Jersey][3] [documentation](https://jersey.java.net/nonav/documentation/2.0/index.html) to how to handle
resource requests.

Registering a websocket listener
---
Websocket listeners can be registered automatically using [Spring][2].  Any classe annotated with the
`org.zenoss.dropwizardspring.websocket.annotations.WebSocketListener` will be registerd to listen on the path defined by
the `@Path` annotation. Additionally the `org.zenoss.dropwizardsrping.annotations.OnMessage` annotations is needed to
define the method that will handle websocket messages.

    import com.fasterxml.jackson.databind.ObjectMapper;
    import org.eclipse.jetty.websocket.WebSocket.Connection;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.zenoss.dropwizardspring.websockets.annotations.OnMessage;
    import org.zenoss.dropwizardspring.websockets.annotations.WebSocketListener;

    import javax.ws.rs.Path;
    import java.io.IOException;

    @Path("/ws/example")
    @WebSocketListener
    public class ExampleWebSocket {

        private ObjectMapper mapper = new ObjectMapper();

        @OnMessage
        public void echo(String data, Connection connection) throws IOException {
            ArrayList<String> input = mapper.readValue(data, new TypeReference<ArrayList<String>>() {});
            connection.sendMessage(mapper.writeValueAsString(input));
        }
    }

Registring Dropwizard objects
---
The `org.zenoss.dropwizardspring.annotations` pacage contains `HealthChecks`, `Tasks` and "`Managed`" annotations.
These annotations can be used to automatically register their respective [Dropwizard][1] components.  Read the
[Dropwizard][1] documentation to find out more about the components.

Spring Profiles
---
You can annotate your components that have different implementations based on running environment with `Profile`. For
example a component that runs in production can be annotated `@Profile("prod")` and a version of the component that runs
in development can be annotated with `@Profile("dev")`. If a component is annotated with a profile than it will only be
loaded if the profile matches any of the active profiles.

The bundle sets the default active profile to be `prod`. The active profile can be changed by setting a
command line environment.

    java -Dspring.profiles.active=dev

Read more about Spring [Profiles](http://blog.springsource.com/2011/02/14/spring-3-1-m1-introducing-profile/).

Writing unit tests
---
Writing tests for zapp resource requires a combination of spring and dropwizard test classes. Add the dropwizard and
spring dependencies to your pom.

         <dependency>
             <groupId>com.yammer.dropwizard</groupId>
             <artifactId>dropwizard-testing</artifactId>
             <version>${dropwizard.version}</version>
             <scope>test</scope>
         </dependency>
         <dependency>
             <groupId>org.springframework</groupId>
             <artifactId>spring-test</artifactId>
             <version>${spring.version}</version>
             <scope>test</scope>


Extend the Dropwizard test class `ResourceTest` to test resources. If you want Spring to autowire your resources you'll
need to annotate your test class to provide a Spring environment. The annotated static class in the test will allow you
to register mock beans or any other bean needed to run the test.


    @RunWith(SpringJUnit4ClassRunner.class)
    @ContextConfiguration(loader = AnnotationConfigContextLoader.class)
    @ActiveProfiles({"dev","test"}) //Profiles used by the test
    public class QueryTest extends ResourceTest {

        //static configuration class provides the spring configuration i.e. what beans are loaded
        @Configuration
        @ComponentScan(basePackages = {"org.zenoss.app"})//scan for annotated classes
        static class ContextConfiguration {

            @Bean //provide you app config if needed
            public QueryAppConfiguration getQueryAppConfiguration() {
                return new QueryAppConfiguration();
            }
        }

        @Autowired
        PerformanceMetricQueryResources resource;

        @Test
        public void myTest() throws Exception {
            //see http://dropwizard.codahale.com/manual/testing/#testing-resources
        }

        /*
         * (non-Javadoc)
         *
         * @see com.yammer.dropwizard.testing.ResourceTest#setUpResources()
         */
        // @Override
        protected void setUpResources() throws Exception {
            addResource(resource);
        }
    }


Building and running
---
The example zapp contains examples of the mvn build plugins needed to create a zapp jar.  To build example app run the
following in the zapp-example directory:
	
	mvn package

To run the zapp-example run the following:

	java -jar target/zapp-example-0.0.1-SNAPSHOT.jar server configuration.yaml
	

You can also run the example zapp without packaging directly via maven.

	mvn compile exec:java

To build your own zapp you can copy and modify the build plugins in the `pom.xml` in the zapp-example project or you can
use the zapp maven archetype (TBD) to generate a zapp project skeleton.



[1]: http://dropwizard.codahale.com/
[2]: http://www.springsource.org/
[3]: https://jersey.java.net/

