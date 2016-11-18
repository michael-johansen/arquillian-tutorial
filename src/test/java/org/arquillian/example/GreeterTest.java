package org.arquillian.example;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.builder.RequestSpecBuilder;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.servlet.ServletContext;
import java.io.File;

import static com.jayway.restassured.RestAssured.config;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.config.LogConfig.logConfig;
import static org.hamcrest.core.Is.is;


@RunWith(Arquillian.class)
public class GreeterTest {

  @Deployment(name = "test")
  public static WebArchive createDeployment() {
    // Import Maven runtime dependencies
    File[] files = Maven.resolver()
      .loadPomFromFile("pom.xml")
      .importRuntimeDependencies()
      .resolve()
      .withTransitivity()
      .asFile();

    // Create deploy file
    WebArchive war = ShrinkWrap.create(WebArchive.class, "test.war")
      .addClasses(
        Greeting.class,
        GreetingController.class,
        Application.class
      )
      .addAsLibraries(files)
      .setWebXML("basic-web.xml");

    // Show the deploy structure
    System.out.println(war.toString(true));

    return war;
  }

  @ArquillianResource
  ServletContext servletContext;

  @Before
  public void setUp() throws Exception {
    RestAssured.config = config()
      .logConfig(logConfig().enableLoggingOfRequestAndResponseIfValidationFails());

    RestAssured.requestSpecification = new RequestSpecBuilder()
      .setBaseUri("http://localhost:9090" + servletContext.getContextPath())
      .build();
  }

  @Test
  public void should_create_greeting() {
    given()
      .when().get("/greeting")
      .then().statusCode(200)
      .and().body("content", is("Hello, World!"));
  }
}
