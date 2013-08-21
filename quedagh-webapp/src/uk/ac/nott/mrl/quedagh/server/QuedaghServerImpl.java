package uk.ac.nott.mrl.quedagh.server;

import java.util.Collection;
import java.util.List;

import org.wornchaos.client.server.AsyncCallback;
import org.wornchaos.server.Method;
import org.wornchaos.server.ObjectifyJSONServerImpl;

import uk.ac.nott.mrl.quedagh.model.Game;
import uk.ac.nott.mrl.quedagh.model.Level;
import uk.ac.nott.mrl.quedagh.model.Position;
import uk.ac.nott.mrl.quedagh.model.PositionLogItem;
import uk.ac.nott.mrl.quedagh.model.QuedaghServer;
import uk.ac.nott.mrl.quedagh.model.Team;
import uk.ac.nott.mrl.quedagh.model.stages.Search;
import uk.ac.nott.mrl.quedagh.model.stages.Sort;
import uk.ac.nott.mrl.quedagh.model.stages.SortSetup;
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
		ObjectifyService.register(Sort.class);
		ObjectifyService.register(Method.class);
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
		if(clean != null && clean)
		{
			delete(get(Game.class));
			delete(get(Team.class));
			delete(get(Level.class));
			delete(get(Stage.class));
		}
		
		final Iterable<Game> games = get(Game.class);
		if (!games.iterator().hasNext())
		{
			final Level level = new Level();
			level.setName("Test");
			level.setId(allocateID(Level.class));
			level.getMarkers().add(new Position(52.949197f, -1.186162f));
			level.getMarkers().add(new Position(52.953804f, -1.187403f));
			level.getMarkers().add(new Position(52.952268f, -1.187023f));

			final Stage stage5 = new Staging(allocateID(Staging.class), "Finished!");
			final Stage stage4 = new SortSetup(allocateID(SortSetup.class), stage5);
			final Stage stage3 = new Staging(allocateID(Staging.class), "Ended", stage4);
			final Stage stage2 = new Search(allocateID(Search.class), stage3);
			final Stage stage1 = new Staging(allocateID(Staging.class), "Waiting for Start...", stage2);
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
			final Game game = games.iterator().next();
			game.reset();

			store(game);
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
			game.getStage().response(team, responseValue);

			store(game);

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
			team.getLog().addAll(log);
			team.updateDistance();
			final Game game = team.getGame();
			final Stage stage = game.getStage();
			if (stage != null)
			{
				stage.update(team, log);

				store(team);
				store(game);

				response.onSuccess(game);
			}

			store(game);
		}
	}

	private Team getTeam(final String device)
	{
		assert device != null;
		List<Team> teams = getQuery(Team.class).filter("device", device).list();
		for(Team team: teams)
		{
			if(team.getGame() != null && team.getGame().getStage() != null)
			{
				return team;
			}
		}

		final Game game = getQuery(Game.class).filter("stage !=", null).first().now();
		if (game != null)
		{
			final Team team = game.createTeam(allocateID(Team.class), device);
			store(game);
			return team;
		}
		return null;
	}
}