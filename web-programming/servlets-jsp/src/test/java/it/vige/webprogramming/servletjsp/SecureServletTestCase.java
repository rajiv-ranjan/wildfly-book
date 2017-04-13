package it.vige.webprogramming.servletjsp;

import static com.gargoylesoftware.htmlunit.HttpMethod.POST;
import static com.gargoylesoftware.htmlunit.HttpMethod.PUT;
import static java.util.logging.Logger.getLogger;
import static org.jboss.shrinkwrap.api.ShrinkWrap.create;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.xml.sax.SAXException;

import com.gargoylesoftware.htmlunit.DefaultCredentialsProvider;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;

import it.vige.webprogramming.servletjsp.secure.LoginServlet;
import it.vige.webprogramming.servletjsp.secure.SecureServlet;

@RunWith(Arquillian.class)
public class SecureServletTestCase {

	private static final String WEBAPP_SRC = "src/main/webapp";

	private static final Logger logger = getLogger(SecureServletTestCase.class.getName());

	@ArquillianResource
	private URL base;

	HtmlForm loginForm;

	WebClient webClient;
	DefaultCredentialsProvider correctCreds = new DefaultCredentialsProvider();
	DefaultCredentialsProvider incorrectCreds = new DefaultCredentialsProvider();

	@Deployment(testable = false)
	public static WebArchive createDeployment() {
		WebArchive war = create(WebArchive.class).addClass(SecureServlet.class).addClass(LoginServlet.class)
				.addAsWebResource(new File(WEBAPP_SRC + "/view", "secure-form.jsp"))
				.addAsWebResource(new File(WEBAPP_SRC + "/view", "loginerror.jsp"))
				.addAsWebResource(new File(WEBAPP_SRC + "/view", "loginform.jsp"))
				.addAsWebInfResource((new File("src/test/resources", "web.xml")));
		return war;
	}

	@Before
	public void setup() throws IOException {
		webClient = new WebClient();
		correctCreds.addCredentials("u1", "p1");
		incorrectCreds.addCredentials("random", "random");
		HtmlPage page = webClient.getPage(base + "/secure-form.jsp");
		loginForm = page.getForms().get(0);
	}

	@Test
	public void testGetWithCorrectCredentials() throws Exception {
		webClient.setCredentialsProvider(correctCreds);
		TextPage page = webClient.getPage(base + "/SecureServlet");
		assertEquals("my GET", page.getContent());
		page = webClient.getPage(base + "/SecureOmissionServlet");
		assertEquals("my GET", page.getContent());
		page = webClient.getPage(base + "/SecureDenyUncoveredServlet");
		assertEquals("my GET", page.getContent());
		loginForm.getInputByName("j_username").setValueAttribute("u1");
		loginForm.getInputByName("j_password").setValueAttribute("p1");
		HtmlSubmitInput submitButton = loginForm.getInputByName("submitButton");
		HtmlPage page2 = submitButton.click();
		assertEquals("Form-based Security - Success", page2.getTitleText());
	}

	@Test
	public void testGetWithIncorrectCredentials() throws Exception {
		webClient.setCredentialsProvider(incorrectCreds);
		try {
			webClient.getPage(base + "/SecureServlet");
		} catch (FailingHttpStatusCodeException e) {
			assertNotNull(e);
			assertEquals(401, e.getStatusCode());
			return;
		}
		fail("/SecureServlet could be accessed without proper security credentials");
		try {
			webClient.getPage(base + "/SecureOmissionServlet");
		} catch (FailingHttpStatusCodeException e) {
			assertNotNull(e);
			assertEquals(401, e.getStatusCode());
			return;
		}
		fail("/SecureOmissionServlet could be accessed without proper security credentials");
		loginForm.getInputByName("j_username").setValueAttribute("random");
		loginForm.getInputByName("j_password").setValueAttribute("random");
		HtmlSubmitInput submitButton = loginForm.getInputByName("submitButton");
		HtmlPage page2 = submitButton.click();

		assertEquals("Form-Based Login Error Page", page2.getTitleText());
	}

	@Test
	public void testPostWithCorrectCredentials() throws Exception {
		webClient.setCredentialsProvider(correctCreds);
		WebRequest request = new WebRequest(new URL(base + "/SecureServlet"), POST);
		TextPage page = webClient.getPage(request);
		assertEquals("my POST", page.getContent());
		request = new WebRequest(new URL(base + "/SecureOmissionServlet"), POST);
		page = webClient.getPage(request);
		assertEquals("my POST", page.getContent());
		request = new WebRequest(new URL(base + "SecureDenyUncoveredServlet"), POST);
		try {
			TextPage p = webClient.getPage(request);
			logger.info(p.getContent());
		} catch (FailingHttpStatusCodeException e) {
			assertNotNull(e);
			assertEquals(403, e.getStatusCode());
			return;
		}
		fail("POST method could be called even with deny-unocvered-http-methods");
	}

	@Test
	public void testPostWithIncorrectCredentials() throws Exception {
		webClient.setCredentialsProvider(incorrectCreds);
		WebRequest request = new WebRequest(new URL(base + "/SecureServlet"), POST);
		try {
			webClient.getPage(request);
		} catch (FailingHttpStatusCodeException e) {
			assertNotNull(e);
			assertEquals(401, e.getStatusCode());
			return;
		}
		fail("/SecureServlet could be accessed without proper security credentials");
		request = new WebRequest(new URL(base + "SecureOmissionServlet"), POST);
		TextPage page = webClient.getPage(request);
		assertEquals("my POST", page.getContent());
	}

	@Test
	public void testPutWithCorrectCredentials() throws Exception {
		webClient.setCredentialsProvider(correctCreds);
		WebRequest request = new WebRequest(new URL(base + "SecureDenyUncoveredServlet"), PUT);
		try {
			TextPage p = webClient.getPage(request);
			logger.info(p.getContent());
		} catch (FailingHttpStatusCodeException e) {
			assertNotNull(e);
			assertEquals(403, e.getStatusCode());
			return;
		}
		fail("PUT method could be called even with deny-unocvered-http-methods");
	}

	@Test
	public void testUnauthenticatedRequest() throws IOException, SAXException {
		HtmlPage page = webClient.getPage(base + "/LoginServlet");
		String responseText = page.asText();
		assertTrue("Is User in Role", responseText.contains("isUserInRole?false"));
		assertTrue("Get Remote User", responseText.contains("getRemoteUser?null"));
		assertTrue("Get User Principal", responseText.contains("getUserPrincipal?null"));
		assertTrue("Get Auth Type", responseText.contains("getAuthType?null"));
	}

	@Test
	public void testAuthenticatedRequest() throws IOException, SAXException {
		WebRequest request = new WebRequest(new URL(base + "/LoginServlet?user=u1&password=p1"), HttpMethod.GET);
		WebResponse response = webClient.getWebConnection().getResponse(request);
		String responseText = response.getContentAsString();
		logger.info(responseText);

		assertTrue("Is user in role", responseText.contains("isUserInRole?true"));
		assertTrue("Get Remote User", responseText.contains("getRemoteUser?u1"));
		assertTrue("Get User Principal", responseText.contains("getUserPrincipal?u1"));
		assertTrue("Get Auth Type", responseText.contains("getAuthType?BASIC"));
	}
}