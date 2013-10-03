package uk.ac.nott.mrl.quedagh.model.stages;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

import org.wornchaos.client.server.ObjectStore;
import org.wornchaos.logger.Log;

import uk.ac.nott.mrl.quedagh.model.Game;
import uk.ac.nott.mrl.quedagh.model.Game.Draw;
import uk.ac.nott.mrl.quedagh.model.Marker;
import uk.ac.nott.mrl.quedagh.model.Message;
import uk.ac.nott.mrl.quedagh.model.PositionLogItem;
import uk.ac.nott.mrl.quedagh.model.Swap;
import uk.ac.nott.mrl.quedagh.model.Team;

import com.googlecode.objectify.annotation.EntitySubclass;

@EntitySubclass
public abstract class Sort extends Stage
{
	private Swap swap;
	private int comparisons = 0;
	private int swaps = 0;

	static final Comparator<Marker> comparator = new Comparator<Marker>()
	{
		@Override
		public int compare(final Marker o1, final Marker o2)
		{
			return o1.getValue() - o2.getValue();
		}
	};

	public Sort(final String id, final Stage next)
	{
		super(id, Draw.order, next);
	}

	Sort()
	{
		super();
	}

	@Override
	public void response(final Game game, final Team team, final String response, final ObjectStore store)
	{
		if (swap == null && team.isAdmin())
		{
			final Stage stage = nextStage(game, store);
			if (stage != null)
			{
				stage.update(game, team, new ArrayList<PositionLogItem>(), store);
			}
		}
	}

	@Override
	public void setupTeam(final Game game, final Team team)
	{
		team.postMessages(new Message("You are " + Game.str(team.getValue())));
	}

	@Override
	public void start(final Game game)
	{
		super.start(game);
	}

	@Override
	public void update(final Game game, final Team team, final Collection<PositionLogItem> log, final ObjectStore store)
	{
		if(swap == null)
		{
			hasNextSwap(game);
			store.store(this);
		}
		if (swap == null)
		{
			Log.info("No swap?");
			if (team.getMessageCount() == 1)
			{
				if (team.isAdmin() && game.hasNextStage())
				{
					team.postMessages(new Message("You are " + Game.str(team.getValue())), new Message(
							"Start Next Stage", "next"));
				}
				else
				{
					team.postMessages(new Message("You are " + Game.str(team.getValue())), new Message(
							"Sorting Finished"));
				}
			}
		}
		else if (inPosition(game))
		{
			swap(game);
			hasNextSwap(game);
			store.store(this);
		}
	}

	protected abstract Swap findNextSwap(final Game game);

	protected int getComparisons()
	{
		return comparisons;
	}

	protected abstract String getSortName();

	protected int getSwaps()
	{
		return swaps;
	}

	protected void incComparisons()
	{
		comparisons++;
	}

	public boolean hasNextSwap(final Game game)
	{
		final String order = game.getOrder();
		Log.info("Order = " + order);

		swap = findNextSwap(game);
		if (swap == null)
		{
			game.postMessages(new Message(getSortName() + " Sort: " + order), new Message(comparisons
					+ " comparisons, " + swaps + " swaps"));
			Log.info("No swaps left");
			return false;
		}
		else
		{
			Log.info("Swapping " + swap);
			swaps++;
			game.postMessages(	new Message(getSortName() + " Sort: " + order), new Message("Swapping " + swap),
								new Message(comparisons + " comparisons, " + swaps + " swaps"));
			return true;
		}
	}

	private boolean inPosition(final Game game)
	{
		for (final Team team : game.getTeams())
		{
			if (!inPosition(game, team)) { return false; }
		}

		return true;
	}

	private boolean inPosition(final Game game, final Team team)
	{
		final Marker near = game.nearMarker(team.getLastKnown());
		if (near == null) { return false; }

		if (team.getValue() == swap.getValue1())
		{
			return near.getValue() == swap.getValue2();
		}
		else if (team.getValue() == swap.getValue2())
		{
			return near.getValue() == swap.getValue1();
		}
		else
		{
			return near.getValue() == team.getValue();
		}
	}

	private Marker getMarker(final Game game, final int value)
	{
		for (Marker marker : game.getMarkers())
		{
			if (marker.getValue() == value) { return marker; }
		}

		return null;
	}

	void swap(final Game game)
	{
		final Marker marker1 = getMarker(game, swap.getValue1());
		final Marker marker2 = getMarker(game, swap.getValue2());
		final int placeholder = marker1.getValue();
		marker1.setValue(marker2.getValue());
		marker2.setValue(placeholder);
	}
}