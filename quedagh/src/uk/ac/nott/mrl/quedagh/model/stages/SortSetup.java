package uk.ac.nott.mrl.quedagh.model.stages;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import org.wornchaos.client.server.ObjectStore;
import org.wornchaos.logger.Log;

import uk.ac.nott.mrl.quedagh.model.Game;
import uk.ac.nott.mrl.quedagh.model.Marker;
import uk.ac.nott.mrl.quedagh.model.Message;
import uk.ac.nott.mrl.quedagh.model.PositionLogItem;
import uk.ac.nott.mrl.quedagh.model.Team;
import uk.ac.nott.mrl.quedagh.model.Game.Draw;

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
		super(id, Draw.none, next);
		this.type = type;
	}

	SortSetup()
	{

	}

	@Override
	public void setupTeam(final Game game, final Team team)
	{
		team.postMessages(message);
	}

	@Override
	public void update(final Game game, final Team team, final Collection<PositionLogItem> log, final ObjectStore store)
	{
		if (isFinished(game))
		{
			setupMarkers(game);
			
			Log.info("Order = " + game.getOrder());
			
			while (inOrder(game.getMarkers()))
			{
				randomizeMarkers(game.getMarkers());
				for (final Team ateam : game.getTeams())
				{
					final Marker near = game.nearMarker(ateam.getLastKnown());
					ateam.setValue(near.getValue());
				}
				Log.info("Order = " + game.getOrder());				
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

	private boolean inOrder(final Iterable<Marker> markers)
	{
		Marker last = null;
		for (final Marker marker : markers)
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

	public void setupMarkers(final Game game)
	{
		// Set up markers & teams
		final List<Marker> markers = new ArrayList<Marker>(game.getMarkers());
		for (final Team ateam : game.getTeams())
		{
			final Marker near = game.nearMarker(ateam.getLastKnown());
			near.setValue(ateam.getValue());
			markers.remove(near);
		}

		int value = game.getTeams().size() + 1;
		final Random random = new Random();			
		while(!markers.isEmpty())
		{
			int mark = random.nextInt(markers.size());
			Marker marker = markers.get(mark);
			markers.remove(marker);
			marker.setValue(value);
			value++;
		}
	}
	
	private void randomizeMarkers(final List<Marker> originalList)
	{
		int value = 1;
		final List<Marker> markers = new ArrayList<Marker>(originalList);		
		final Random random = new Random();			
		while(!markers.isEmpty())
		{
			final int rval = random.nextInt(markers.size());
			final Marker marker = markers.get(rval);
			markers.remove(marker);
			marker.setValue(value);
			value++;
		}		
	}
}
