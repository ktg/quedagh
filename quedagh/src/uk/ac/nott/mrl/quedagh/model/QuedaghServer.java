package uk.ac.nott.mrl.quedagh.model;

import java.util.Collection;

import org.wornchaos.client.server.AsyncCallback;
import org.wornchaos.client.server.JSONServer;
import org.wornchaos.client.server.Named;
import org.wornchaos.client.server.Prefix;
import org.wornchaos.client.server.Request;
import org.wornchaos.client.server.Request.Cache;
import org.wornchaos.client.server.Request.Method;

@Prefix("command")
public interface QuedaghServer extends JSONServer
{
	@Request(value = "games", cache = Cache.RESULT)
	public void getGames(final AsyncCallback<Iterable<Game>> response);

	@Request(value = "levels", cache = Cache.RESULT)
	public void getLevels(final AsyncCallback<Iterable<Level>> response);
	
	@Request(value = "game", cache = Cache.RESULT, parseGroup = "logs")
	public void getGame(@Named("id") final String id, final AsyncCallback<Game> response);
	
	@Request(value = "createGame", cache = Cache.RESULT)
	public void createGame(@Named("level") final String levelID, final AsyncCallback<Game> response);
	
	@Request(value = "reset", cache = Cache.RESULT)
	public void reset(@Named("game") final String gameID, final AsyncCallback<Iterable<Game>> response);
	
	@Request(value = "update", method = Method.POST)
	public void update(@Named("game") String gameID, @Named("device") final String device, @Named("logs") final Collection<PositionLogItem> logs, final AsyncCallback<Game> response);
	
	@Request("respond")
	public void respond(@Named("device") final String device, @Named("response") final String responseValue, final AsyncCallback<Game> response);
}
