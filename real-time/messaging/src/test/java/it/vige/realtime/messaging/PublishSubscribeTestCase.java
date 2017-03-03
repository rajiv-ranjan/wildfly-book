package it.vige.realtime.messaging;

import static org.junit.Assert.assertEquals;
import static it.vige.realtime.messaging.clients.Constants.QUEUE_LOOKUP;
import static it.vige.realtime.messaging.clients.Constants.QUEUE_NAME;
import static it.vige.realtime.messaging.clients.Constants.TOPIC_LOOKUP;
import static it.vige.realtime.messaging.clients.Constants.TOPIC_NAME;
import static java.util.logging.Logger.getLogger;
import static org.jboss.as.test.integration.common.jms.JMSOperationsProvider.getInstance;
import static org.jboss.shrinkwrap.api.ShrinkWrap.create;

import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.jms.JMSException;
import javax.jms.Message;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.as.arquillian.api.ServerSetup;
import org.jboss.as.arquillian.api.ServerSetupTask;
import org.jboss.as.arquillian.container.ManagementClient;
import org.jboss.as.test.integration.common.jms.JMSOperations;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import it.vige.realtime.messaging.MessageTestCase.MessagingResourcesSetupTask;
import it.vige.realtime.messaging.publishsubscribe.Publisher;
import it.vige.realtime.messaging.publishsubscribe.Subscriber;

@RunWith(Arquillian.class)
@ServerSetup(MessagingResourcesSetupTask.class)
public class PublishSubscribeTestCase {

	private static final Logger logger = getLogger(PublishSubscribeTestCase.class.getName());

	@EJB
	private Publisher publisher;

	@EJB
	private Subscriber subscriber;

	static class MessagingResourcesSetupTask implements ServerSetupTask {

		@Override
		public void setup(ManagementClient managementClient, String containerId) throws Exception {
			JMSOperations jmsOperations = getInstance(managementClient.getControllerClient());
			jmsOperations.createJmsQueue(QUEUE_NAME, QUEUE_LOOKUP);
			jmsOperations.createJmsTopic(TOPIC_NAME, TOPIC_LOOKUP);
		}

		@Override
		public void tearDown(ManagementClient managementClient, String containerId) throws Exception {
			JMSOperations jmsOperations = getInstance(managementClient.getControllerClient());
			jmsOperations.removeJmsQueue(QUEUE_NAME);
			jmsOperations.removeJmsTopic(TOPIC_NAME);
		}
	}

	@Deployment
	public static JavaArchive createEJBDeployment() {
		final JavaArchive jar = create(JavaArchive.class, "publishsubscribe.jar");
		jar.addPackage(Publisher.class.getPackage());
		return jar;
	}

	@Test
	public void testSendQueueMessage() throws JMSException {
		logger.info("Start publish subscribe topic test");
		subscriber.createConnectionAndReceiveMessage("bus_stop_client_1", "localhost");
		publisher.createConnectionAndSendMessage("localhost");
		assertEquals("the message is: ", "", subscriber.getLastReceived());
		logger.info("End publish subscribe topic test");
	}

}