package uk.ac.nott.mrl.quedagh.server;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.wornchaos.parser.Parser;
import org.wornchaos.parser.gson.GsonParser;
import org.wornchaos.server.JSONServerDispatcher;
import org.wornchaos.server.ServerMethod;
import org.wornchaos.server.ServerResponse;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

public class ServerTests
{
	private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

	@Test
	public void stressTest() throws Exception
	{
		final JSONServerDispatcher dispatcher = new JSONServerDispatcher();
		final QuedaghServerImpl server = new QuedaghServerImpl();

		dispatcher.setServer(server);

		final Parser parser = new GsonParser();
		final ServerMethod method = dispatcher.getMethod("/command/reset");
		final ServerResponse<?> response = new ServerResponse(parser, null, null);
		method.invoke(server, response, new Object[] {null, response});
		
		
	}

	@Before
	public void setUp()
	{
		helper.setUp();
	}

	@After
	public void tearDown()
	{
		helper.tearDown();
	}
}
