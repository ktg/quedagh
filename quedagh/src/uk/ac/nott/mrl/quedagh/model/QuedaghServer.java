package uk.ac.nott.mrl.quedagh.model;

import java.util.Collection;

import org.wornchaos.client.server.AsyncCallback;
import org.wornchaos.client.server.Cache;
import org.wornchaos.client.server.JSONServer;
import org.wornchaos.client.server.Prefix;
import org.wornchaos.client.server.Request;
import org.wornchaos.client.server.Request.Method;
import org.wornchaos.client.server.RequestParam;

@Prefix("command")
public interface QuedaghServer extends JSONServer
{
	@Cache
	@Request("createGame")
	public void createGame(@RequestParam("level") final String levelID, final AsyncCallback<Game> response);

	@Cache
	@Request("game")
	public void getGame(@RequestParam("id") final String id, final AsyncCallback<Game> response);

	@Request("games")
	public void getGames(final AsyncCallback<Iterable<Game>> response);

	@Request("levels")
	public void getLevels(final AsyncCallback<Iterable<Level>> response);

	@Cache
	@Request("reset")
	public void reset(@RequestParam(value="game", required = false) final String gameID, final AsyncCallback<Iterable<Game>> response);

	@Request("respond")
	public void respond(@RequestParam(value="device", required=false) final String device,
			@RequestParam("response") final String responseValue, final AsyncCallback<Game> response);

	@Request(value = "update", method = Method.POST)
	public void update(@RequestParam("game") String gameID, @RequestParam("device") final String device,
			@RequestParam("logs") final Collection<PositionLogItem> logs, final AsyncCallback<Game> response);
}
