package it.vige.webprogramming.servletjsp;

import static it.vige.webprogramming.servletjsp.async.AsynchronousServlet.states;
import static java.util.logging.Logger.getLogger;
import static org.jboss.shrinkwrap.api.ShrinkWrap.create;
import static org.jboss.shrinkwrap.api.asset.EmptyAsset.INSTANCE;
import static org.junit.Assert.assertTrue;
import static org.openqa.selenium.By.xpath;

import java.io.File;
import java.net.URL;
import java.util.logging.Logger;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

import it.vige.webprogramming.servletjsp.async.AsynchronousServlet;

@RunWith(Arquillian.class)
@RunAsClient
public class AsynchronousTestCase {

	private static final Logger logger = getLogger(AsynchronousTestCase.class.getName());

	@ArquillianResource
	private URL url;

	@Drone
	private WebDriver driver;

	@Deployment
	public static WebArchive createWebDeployment() {
		final WebArchive war = create(WebArchive.class, "asynchronous-test.war");
		war.addPackage(AsynchronousServlet.class.getPackage());
		war.addAsWebInfResource(INSTANCE, "beans.xml");
		war.addAsWebResource(new FileAsset(new File("src/main/webapp/view/asynchronous.jsp")), "view/asynchronous.jsp");
		war.addAsWebInfResource(new FileAsset(new File("src/test/resources/web.xml")), "web.xml");
		return war;
	}

	@Test
	public void testAsynchronous() throws Exception {
		logger.info("start asynchronous test");
		assertTrue("asynchrnous call not executed", states.isEmpty());
		driver.get(url + "/view/asynchronous.jsp");
		driver.findElement(xpath("html/body/a")).click();
	}

}
