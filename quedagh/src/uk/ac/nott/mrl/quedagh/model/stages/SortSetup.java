package uk.ac.nott.mrl.quedagh.model.stages;

import java.util.Collection;
import java.util.HashSet;
import java.util.Random;

import org.wornchaos.client.server.ObjectStore;
import org.wornchaos.logger.Log;

import uk.ac.nott.mrl.quedagh.model.Game;
import uk.ac.nott.mrl.quedagh.model.Marker;
import uk.ac.nott.mrl.quedagh.model.Message;
import uk.ac.nott.mrl.quedagh.model.PositionLogItem;
import uk.ac.nott.mrl.quedagh.model.Team;

import com.googlecode.objectify.annotation.EntitySubclass;

@EntitySubclass
public class SortSetup extends Stage
{
	public enum SortType
	{
		Bubble, Selection
	}

	private static final Message message = new Message("Go to a marker");
	private SortType type;

	public SortSetup(final String id, final Stage next, final SortType type)
	{
		super(id, next);
		this.type = type;
	}

	SortSetup()
	{

	}

	@Override
	public void setupTeam(final Game game, final Team team)
	{
		team.getMessages().clear();
		team.getMessages().add(message);
	}

	@Override
	public void update(final Game game, final Team team, final Collection<PositionLogItem> log, final ObjectStore store)
	{
		if (isFinished(game))
		{
			// Set up markers & teams
			for (final Team ateam : game.getTeams())
			{
				final Marker near = game.nearMarker(ateam.getLastKnown());
				near.setValue(ateam.getValue());
			}

			int value = game.getTeams().size() + 1;
			for (final Marker marker : game.getMarkers())
			{
				if (marker.getValue() == null)
				{
					marker.setValue(value);
					value++;
				}
			}

			while (inOrder(game))
			{
				randomizeMarkers(game);
			}

			for (final Team ateam : game.getTeams())
			{
				final Marker near = game.nearMarker(ateam.getLastKnown());
				ateam.setValue(near.getValue());
			}
			
			Log.info(type.name() + " Sort");
			if (type == SortType.Bubble)
			{
				final Stage stage = new BubbleSort(store.allocateID(Stage.class), getNext());
				store.store(stage);
				game.setStage(stage);
				stage.update(game, team, log, store);
			}
			else if (type == SortType.Selection)
			{
				final Stage stage = new SelectionSort(store.allocateID(Stage.class), getNext());
				store.store(stage);
				game.setStage(stage);
				stage.update(game, team, log, store);
			}
		}
		else
		{
			team.getMessages().clear();
			final Marker near = game.nearMarker(team.getLastKnown());
			if (near == null)
			{
				team.getMessages().add(message);
			}
		}
	}

	private boolean inOrder(final Game game)
	{
		Marker last = null;
		for (final Marker marker : game.getMarkers())
		{
			if (last != null)
			{
				if (last.getValue() > marker.getValue()) { return false; }
			}
			last = marker;
		}
		return true;
	}

	private boolean isFinished(final Game game)
	{
		final Collection<Marker> markers = new HashSet<Marker>();
		for (final Team ateam : game.getTeams())
		{
			final Marker near = game.nearMarker(ateam.getLastKnown());
			if (near == null)
			{
				return false;
			}
			else if (markers.contains(near)) { return false; }
			markers.add(near);
		}

		return true;
	}

	private void randomizeMarkers(final Game game)
	{
		for (final Marker marker : game.getMarkers())
		{
			marker.setValue(null);
		}
		int value = 1;
		final Random random = new Random();
		while (value <= game.getMarkers().size())
		{
			final int rval = random.nextInt(game.getMarkers().size());
			final Marker marker = game.getMarkers().get(rval);
			if (marker.getValue() == null)
			{
				marker.setValue(value);
				value++;
			}
		}
	}
}
