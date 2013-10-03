package uk.ac.nott.mrl.quedagh.server;

import java.util.Collection;

import org.wornchaos.client.server.AsyncCallback;
import org.wornchaos.server.MethodParseGroup;
import org.wornchaos.server.ObjectifyJSONServerImpl;

import uk.ac.nott.mrl.quedagh.model.Game;
import uk.ac.nott.mrl.quedagh.model.GameEvent;
import uk.ac.nott.mrl.quedagh.model.Level;
import uk.ac.nott.mrl.quedagh.model.Message;
import uk.ac.nott.mrl.quedagh.model.Position;
import uk.ac.nott.mrl.quedagh.model.PositionLogItem;
import uk.ac.nott.mrl.quedagh.model.QuedaghServer;
import uk.ac.nott.mrl.quedagh.model.Team;
import uk.ac.nott.mrl.quedagh.model.stages.BubbleSort;
import uk.ac.nott.mrl.quedagh.model.stages.Search;
import uk.ac.nott.mrl.quedagh.model.stages.SelectionSort;
import uk.ac.nott.mrl.quedagh.model.stages.SortSetup;
import uk.ac.nott.mrl.quedagh.model.stages.SortSetup.SortType;
import uk.ac.nott.mrl.quedagh.model.stages.Stage;
import uk.ac.nott.mrl.quedagh.model.stages.Staging;

import com.googlecode.objectify.ObjectifyService;

public class QuedaghServerImpl extends ObjectifyJSONServerImpl implements QuedaghServer
{
	static
	{
		ObjectifyService.register(Level.class);
		ObjectifyService.register(Team.class);
		ObjectifyService.register(Game.class);
		ObjectifyService.register(Stage.class);
		ObjectifyService.register(Staging.class);
		ObjectifyService.register(Search.class);
		ObjectifyService.register(SortSetup.class);
		ObjectifyService.register(BubbleSort.class);
		ObjectifyService.register(SelectionSort.class);
		ObjectifyService.register(MethodParseGroup.class);
	}

	@Override
	public void getGame(final String id, final AsyncCallback<Game> response)
	{
		response.onSuccess(get(Game.class, id));
	}

	@Override
	public void getGames(final AsyncCallback<Iterable<Game>> response)
	{
		response.onSuccess(get(Game.class));
	}

	@Override
	public Class<?> getServerInterface()
	{
		return QuedaghServer.class;
	}

	@Override
	public void reset(final String gameID, final AsyncCallback<Iterable<Game>> response)
	{
		if(gameID == null)
		{
			delete(get(Game.class));
			delete(get(Team.class));
			delete(get(Level.class));
			delete(get(Stage.class));

			final Level level = new Level();
			level.setName("NCSL");
			level.setId(allocateID(Level.class));

			level.getMarkers().add(new Position(52.94933f, -1.1861542f));
			level.getMarkers().add(new Position(52.949166f, -1.1853822f));

			level.getMarkers().add(new Position(52.948801f, -1.185656f));
			level.getMarkers().add(new Position(52.948765f, -1.185999f));

			level.getMarkers().add(new Position(52.948468f, -1.186203f));
			level.getMarkers().add(new Position(52.948753f, -1.186525f));

			level.getMarkers().add(new Position(52.94912f, -1.1873257f));
			level.getMarkers().add(new Position(52.94912f, -1.1869257f));

			// level.getMarkers().add(new Position(52.948724f, -1.186992f));
			// level.getMarkers().add(new Position(52.949053f, -1.186723f));

			final Stage stage5 = new SortSetup(allocateID(SortSetup.class), null, SortType.Selection);
			final Stage stage4 = new SortSetup(allocateID(SortSetup.class), stage5, SortType.Bubble);
			final Staging stage3 = new Staging(allocateID(Staging.class), stage4);
			stage3.getMessages().add(new Message("All Found"));
			stage3.getAdminMessages().add(new Message("Start Sort", "sort"));
			final Stage stage2 = new Search(allocateID(Search.class), stage3);
			final Staging stage1 = new Staging(allocateID(Staging.class), stage2);
			stage1.getMessages().add(new Message("Waiting for Start..."));
			stage1.getAdminMessages().add(new Message("Start", "start"));
			store(stage5);
			store(stage4);
			store(stage3);
			store(stage2);
			store(stage1);

			level.setStart(stage1);

			store(level);
			
			final Level level2 = new Level();
			level2.setName("Jubilee Island");
			level2.setId(allocateID(Level.class));

			level2.getMarkers().add(new Position(52.94933f, -1.1861542f));
			level2.getMarkers().add(new Position(52.949166f, -1.1853822f));

			level2.getMarkers().add(new Position(52.948801f, -1.185656f));
			level2.getMarkers().add(new Position(52.948765f, -1.185999f));

			level2.getMarkers().add(new Position(52.948468f, -1.186203f));
			level2.getMarkers().add(new Position(52.948753f, -1.186525f));

			level2.getMarkers().add(new Position(52.94912f, -1.1873257f));
			level2.getMarkers().add(new Position(52.94912f, -1.1869257f));

			// level2.getMarkers().add(new Position(52.948724f, -1.186992f));
			// level2.getMarkers().add(new Position(52.949053f, -1.186723f));

			level2.setStart(stage1);

			store(level2);
			
			final Level level3 = new Level();
			level3.setName("Highfields Park");
			level3.setId(allocateID(Level.class));

			level3.getMarkers().add(new Position(52.94933f, -1.1861542f));
			level3.getMarkers().add(new Position(52.949166f, -1.1853822f));

			level3.getMarkers().add(new Position(52.948801f, -1.185656f));
			level3.getMarkers().add(new Position(52.948765f, -1.185999f));

			level3.getMarkers().add(new Position(52.948468f, -1.186203f));
			level3.getMarkers().add(new Position(52.948753f, -1.186525f));

			level3.getMarkers().add(new Position(52.94912f, -1.1873257f));
			level3.getMarkers().add(new Position(52.94912f, -1.1869257f));

			// level3.getMarkers().add(new Position(52.948724f, -1.186992f));
			// level3.getMarkers().add(new Position(52.949053f, -1.186723f));

			level3.setStart(stage1);

			store(level3);
		}
		else
		{
			Game game = get(Game.class, gameID);
			if(game != null)
			{
				game.reset();
			}
			store(game);
		}

		getGames(response);
	}

	@Override
	public void respond(final String device, final String responseValue, final AsyncCallback<Game> response)
	{
		final Game game = getGame(null);
		if (game != null)
		{
			final Team team = game.getTeam(device);
			final Stage stage = game.getStage();
			if (stage != null)
			{
				stage.response(game, team, responseValue, this);
			}

			response.onSuccess(game);
		}
		else
		{
			response.onFailure(new NullPointerException("Failed to get game"));
		}
	}

	@Override
	public void update(final String gameid, final String device, final Collection<PositionLogItem> log, final AsyncCallback<Game> response)
	{
		final Game game = getGame(gameid);
		if (game != null)
		{
			Team team = game.getTeam(device);
			if(team == null)
			{
				team = game.createTeam(allocateID(Team.class), device);
				store(team);
				store(game);
			}
			
			if (log != null && !log.isEmpty())
			{
				team.getLog().addAll(log);
				if (!game.getEvents().isEmpty())
				{
					final GameEvent event = game.getEvents().get(game.getEvents().size() - 1);
					team.updateDistance(event.getTime());
				}

				final Stage stage = game.getStage();
				if (stage != null)
				{
					stage.update(game, team, log, QuedaghServerImpl.this);
				}

				store(team);
				store(game);
			}
		}
		response.onSuccess(game);
	}

	private Game getGame(final String id)
	{
		if(id != null)
		{
			final Game game = get(Game.class, id);
			if(game != null)
			{
				return game;
			}
		}

		return getQuery(Game.class).filter("stage !=", null).first().now();
	}

	@Override
	public void createGame(String levelID, AsyncCallback<Game> response)
	{
		final Level level = get(Level.class, levelID);
		if(level != null)
		{
			final Game game = new Game();
			game.setId(allocateID(Game.class));
			game.setLevel(level);

			store(game);

			response.onSuccess(game);			
		}
	}

	@Override
	public void getLevels(AsyncCallback<Iterable<Level>> response)
	{
		response.onSuccess(get(Level.class));
	}
}