package uk.ac.nott.mrl.quedagh.model.stages;

import java.util.Collection;
import java.util.HashSet;
import java.util.Random;

import org.wornchaos.client.server.ObjectStore;
import org.wornchaos.logger.Log;

import uk.ac.nott.mrl.quedagh.model.Game;
import uk.ac.nott.mrl.quedagh.model.Game.Draw;
import uk.ac.nott.mrl.quedagh.model.Marker;
import uk.ac.nott.mrl.quedagh.model.Message;
import uk.ac.nott.mrl.quedagh.model.Position;
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

	private static final Message message = new Message("Go to a Marker");

	private static char[] createCharArray(final int length)
	{
		final char[] array = new char[length];
		for (int index = 0; index < length; index++)
		{
			array[index] = (char) (65 + index);
		}

		return array;
	}

	// Implementing Fisher–Yates shuffle
	private static void shuffleArray(final char[] array)
	{
		final Random rnd = new Random();
		for (int index = array.length - 1; index > 0; index--)
		{
			final int swapIndex = rnd.nextInt(index + 1);
			// Simple swap
			final char swap = array[swapIndex];
			array[swapIndex] = array[index];
			array[index] = swap;
		}
	}

	private SortType type;

	public SortSetup(final String id, final Stage next, final SortType type)
	{
		super(id, Draw.none, next);
		this.type = type;
	}

	SortSetup()
	{

	}

	public void setupMarkers(final Game game)
	{
		// Set up markers & teams
		if (game.getLevel() != null && game.getLevel().getInitOrder() != null)
		{
			game.setState(game.getLevel().getInitOrder());
			return;
		}

		randomizeMarkers(game);
		while (game.isInOrder())
		{
			randomizeMarkers(game);
		}

		for (final Team ateam : game.getTeams())
		{
			final int near = nearestMarkerIndex(game, ateam.getLastKnown());
			if (near != -1)
			{
				ateam.setValue(game.getState().charAt(near));
			}
		}
	}

	@Override
	public void setupTeam(final Game game, final Team team)
	{
		team.postMessages(message);
	}

	@Override
	public void update(final Game game, final Team team, final Collection<PositionLogItem> log, final ObjectStore store)
	{
		if (isInPosition(game))
		{
			setupMarkers(game);

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
			final Marker near = game.nearMarker(team.getLastKnown());
			if (near == null)
			{
				team.postMessages(message);
			}
			else
			{
				team.postMessages();
			}
		}
	}

	private boolean isInPosition(final Game game)
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

	private int nearestMarkerIndex(final Game game, final Position position)
	{
		for (int index = 0; index < game.getMarkers().size(); index++)
		{
			final Marker marker = game.getMarkers().get(index);
			if (game.areNear(position, marker)) { return index; }
		}
		return -1;
	}

	private void randomizeMarkers(final Game game)
	{
		final char[] array = createCharArray(game.getMarkers().size());
		shuffleArray(array);
		game.setState(new String(array));
		Log.info("Order = " + game.getState());
	}
}