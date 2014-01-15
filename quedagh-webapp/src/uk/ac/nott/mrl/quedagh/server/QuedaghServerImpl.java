package uk.ac.nott.mrl.quedagh.server;

import java.util.Collection;

import org.wornchaos.client.server.AsyncCallback;
import org.wornchaos.collections.ListReverser;
import org.wornchaos.server.Exclude;
import org.wornchaos.server.Transact;
import org.wornchaos.server.objectify.ObjectifyJSONServer;

import uk.ac.nott.mrl.quedagh.model.Game;
import uk.ac.nott.mrl.quedagh.model.GameEvent;
import uk.ac.nott.mrl.quedagh.model.Level;
import uk.ac.nott.mrl.quedagh.model.Logs;
import uk.ac.nott.mrl.quedagh.model.PositionLogItem;
import uk.ac.nott.mrl.quedagh.model.QuedaghServer;
import uk.ac.nott.mrl.quedagh.model.Team;
import uk.ac.nott.mrl.quedagh.model.stages.BubbleSort;
import uk.ac.nott.mrl.quedagh.model.stages.Search;
import uk.ac.nott.mrl.quedagh.model.stages.SelectionSort;
import uk.ac.nott.mrl.quedagh.model.stages.SortSetup;
import uk.ac.nott.mrl.quedagh.model.stages.Stage;
import uk.ac.nott.mrl.quedagh.model.stages.Staging;

import com.googlecode.objectify.ObjectifyService;

public class QuedaghServerImpl extends ObjectifyJSONServer implements QuedaghServer
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
	}

	@Override
	public void createGame(final String levelID, final AsyncCallback<Game> response)
	{
		final Level level = get(Level.class, levelID);
		if (level != null)
		{
			final Game game = new Game();
			game.setId(allocateID(Game.class));
			game.setLevel(level);

			store(game);

			response.onSuccess(game);
		}
	}

	@Override
	public void getGame(final String id, final AsyncCallback<Game> response)
	{
		response.onSuccess(get(Game.class, id));
	}

	@Override
	@Exclude(Logs.class)
	public void getGames(final AsyncCallback<Iterable<Game>> response)
	{
		response.onSuccess(get(Game.class));
	}

	@Override
	public void getLevels(final AsyncCallback<Iterable<Level>> response)
	{
		response.onSuccess(get(Level.class));
	}

	@Override
	public Class<?> getServerInterface()
	{
		return QuedaghServer.class;
	}

	@Override
	@Transact
	public void reset(final String gameID, final AsyncCallback<Iterable<Game>> response)
	{
//		if (gameID == null)
//		{
//			delete(get(Stage.class));
//			delete(get(Level.class));
//			delete(get(Team.class));
//			delete(get(Game.class));
//
//			final Level level = new Level();
//			level.setName("NCSL");
//			level.setRadius(5.0f);
//			level.setId(allocateID(Level.class));
//
//			level.getMarkers().add(new Position(52.94933f, -1.1861542f));
//			level.getMarkers().add(new Position(52.949166f, -1.1853822f));
//
//			level.getMarkers().add(new Position(52.948801f, -1.185656f));
//			level.getMarkers().add(new Position(52.948765f, -1.185999f));
//
//			level.getMarkers().add(new Position(52.948468f, -1.186203f));
//			level.getMarkers().add(new Position(52.948753f, -1.186525f));
//
//			level.getMarkers().add(new Position(52.94912f, -1.1873257f));
//			level.getMarkers().add(new Position(52.94912f, -1.1869257f));
//
//			level.setComparison(new Position(52.948995f, -1.186242f));
//
//			final Stage stage5 = new SortSetup(allocateID(SortSetup.class), null, SortType.Selection);
//			final Stage stage4 = new SortSetup(allocateID(SortSetup.class), stage5, SortType.Bubble);
//			final Staging stage3 = new Staging(allocateID(Staging.class), stage4);
//			stage3.getMessages().add(new Message("Everything Found"));
//			stage3.getMessages().add(new Message("Return Back to Base"));
//			stage3.getMessages().add(new Message("Start Sort", "sort", true));
//			final Stage stage2 = new Search(allocateID(Search.class), stage3);
//			final Staging stage1 = new Staging(allocateID(Staging.class), stage2);
//			stage1.getMessages().add(new Message("Waiting for Start..."));
//			stage1.getMessages().add(new Message("Start", "start", true));
//			store(stage5);
//			store(stage4);
//			store(stage3);
//			store(stage2);
//			store(stage1);
//
//			level.setStart(stage1);
//
//			store(level);
//
//			final Level level2 = new Level();
//			level2.setName("Jubilee");
//			level2.setRadius(5.0f);
//			level2.setId(allocateID(Level.class));
//
//			level2.getMarkers().add(new Position(52.953009f, -1.187856f));
//			level2.getMarkers().add(new Position(52.952797f, -1.187484f));
//
//			level2.getMarkers().add(new Position(52.952615f, -1.187417f));
//			level2.getMarkers().add(new Position(52.952308f, -1.187293f));
//
//			level2.getMarkers().add(new Position(52.952246f, -1.187502f));
//			level2.getMarkers().add(new Position(52.952431f, -1.187567f));
//
//			level2.getMarkers().add(new Position(52.95265f, -1.187701f));
//			level2.getMarkers().add(new Position(52.952854f, -1.187814f));
//
//			level2.setComparison(new Position(52.952641f, -1.187578f));
//
//			level2.setStart(stage1);
//
//			store(level2);
//
//			final Level level3 = new Level();
//			level3.setName("Highfields Park");
//			level3.setRadius(5.0f);
//			level3.setId(allocateID(Level.class));
//
//			level3.getMarkers().add(new Position(52.939359f, -1.189986f));
//			level3.getMarkers().add(new Position(52.939029f, -1.189348f));
//
//			level3.getMarkers().add(new Position(52.948801f, -1.185656f));
//			level3.getMarkers().add(new Position(52.948765f, -1.185999f));
//
//			level3.getMarkers().add(new Position(52.948468f, -1.186203f));
//			level3.getMarkers().add(new Position(52.948753f, -1.186525f));
//
//			level3.getMarkers().add(new Position(52.94912f, -1.1873257f));
//			level3.getMarkers().add(new Position(52.94912f, -1.1869257f));
//
//			// level3.getMarkers().add(new Position(52.948724f, -1.186992f));
//			// level3.getMarkers().add(new Position(52.949053f, -1.186723f));
//
//			level3.setStart(stage1);
//
//			store(level3);
//
//			final Level level4 = new Level();
//			level4.setName("Jubilee Short");
//			level4.setRadius(5.0f);
//			level4.setId(allocateID(Level.class));
//
//			level4.getMarkers().add(new Position(52.953009f, -1.187856f));
//			level4.getMarkers().add(new Position(52.952797f, -1.187484f));
//
//			// level4.getMarkers().add(new Position(52.952615f,-1.187417f));
//			level4.getMarkers().add(new Position(52.952308f, -1.187293f));
//
//			level4.getMarkers().add(new Position(52.952246f, -1.187502f));
//			level4.getMarkers().add(new Position(52.952431f, -1.187567f));
//
//			// level4.getMarkers().add(new Position(52.95265f,-1.187701f));
//			level4.getMarkers().add(new Position(52.952854f, -1.187814f));
//
//			level4.setComparison(new Position(52.952641f, -1.187578f));
//
//			level4.setStart(stage1);
//
//			store(level4);
//		}
//		else
//		{
//			final Game game = get(Game.class, gameID);
//			if (game != null)
//			{
//				game.reset();
//			}
//			store(game);
//		}

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
	@Transact
	@Exclude(Logs.class)
	public void update(final String gameid, final String device, final Collection<PositionLogItem> log,
			final AsyncCallback<Game> response)
	{
		final Game game = getGame(gameid);
		if (game != null)
		{
			Team team = game.getTeam(device);
			if (team == null)
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
					String stage = null;
					long timestamp = 0;
					for (final GameEvent event : new ListReverser<GameEvent>(game.getEvents()))
					{
						if (stage == null)
						{
							stage = event.getStage();
						}
						else if (!stage.equals(event.getStage()))
						{
							timestamp = event.getTime();
							break;
						}
					}

					team.updateDistance(timestamp);
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
		if (id != null)
		{
			final Game game = get(Game.class, id);
			if (game != null) { return game; }
		}

		return getQuery(Game.class).filter("stage !=", null).first().now();
	}
}