package uk.ac.nott.mrl.quedagh.server;

import java.util.Collection;
import java.util.List;

import org.wornchaos.client.server.AsyncCallback;
import org.wornchaos.server.MethodParseGroup;
import org.wornchaos.server.ObjectifyJSONServerImpl;

import uk.ac.nott.mrl.quedagh.model.Event;
import uk.ac.nott.mrl.quedagh.model.Game;
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
import com.googlecode.objectify.Work;

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
	public void createGame(final AsyncCallback<Game> response)
	{
		final Game game = new Game();
		game.setId(allocateID(Game.class));

		store(game);

		response.onSuccess(game);
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
	public void reset(final Boolean clean, final AsyncCallback<Iterable<Game>> response)
	{
		if (clean != null && clean)
		{
			delete(get(Game.class));
			delete(get(Team.class));
			delete(get(Level.class));
			delete(get(Stage.class));
		}

		final Game existingGame = getQuery(Game.class).filter("stage !=", null).first().now();
		if (existingGame == null)
		{
			final Level level = new Level();
			level.setName("Test");
			level.setId(allocateID(Level.class));
			level.getMarkers().add(new Position(52.949197f, -1.186162f));
			level.getMarkers().add(new Position(52.953804f, -1.187403f));
			level.getMarkers().add(new Position(52.952268f, -1.187023f));

			final Stage stage5 = new Staging(allocateID(Staging.class));
			stage5.getMessages().add(new Message("Finished!"));
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

			final Game game = new Game();
			game.setId(allocateID(Game.class));
			game.setLevel(level);
			store(game);
		}
		else
		{
			existingGame.reset();
			store(existingGame);
		}

		getGames(response);
	}

	@Override
	public void respond(final String device, final String responseValue, final AsyncCallback<Game> response)
	{
		final Team team = getTeam(device);
		if (team != null)
		{
			final Game game = team.getGame();
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
	public void update(final String device, final Collection<PositionLogItem> log, final AsyncCallback<Game> response)
	{
		final Team team = getTeam(device);
		if (team != null)
		{
			final Game game = transaction(new Work<Game>()
			{
				@Override
				public Game run()
				{
					final Game game = team.getGame();
					if (log != null && !log.isEmpty())
					{
						team.getLog().addAll(log);
						if(!game.getEvents().isEmpty())
						{
							Event event = game.getEvents().get(game.getEvents().size() - 1);
							team.updateDistance(event.getDate());							
						}

						final Stage stage = game.getStage();
						if (stage != null)
						{
							stage.update(game, team, log, QuedaghServerImpl.this);
						}
							
						store(team);
						store(game);
					}
										
					return game;
				}	
			});
			
			response.onSuccess(game);	
		}
	}

	private Team getTeam(final String device)
	{
		assert device != null;
		final List<Team> teams = getQuery(Team.class).filter("device", device).list();
		for (final Team team : teams)
		{
			if (team.getGame() != null && team.getGame().getStage() != null) { return team; }
		}

		final Game game = getQuery(Game.class).filter("stage !=", null).first().now();
		if (game != null)
		{
			final Team team = game.createTeam(allocateID(Team.class), device);
			store(team);
			store(game);
			return team;
		}
		return null;
	}
}