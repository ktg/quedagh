package uk.ac.nott.mrl.quedagh.model.stages;

import java.util.Collection;

import org.wornchaos.client.server.ObjectStore;
import org.wornchaos.logger.Log;

import uk.ac.nott.mrl.quedagh.model.Game;
import uk.ac.nott.mrl.quedagh.model.Marker;
import uk.ac.nott.mrl.quedagh.model.Message;
import uk.ac.nott.mrl.quedagh.model.PositionLogItem;
import uk.ac.nott.mrl.quedagh.model.Team;

import com.googlecode.objectify.annotation.EntitySubclass;

@EntitySubclass
public abstract class Sort extends Stage
{
	private int comparisons = 0;
	private int swaps = 0;

	public Sort(final String id, final Stage next)
	{
		super(id, next);
	}

	Sort()
	{
		super();
	}

	@Override
	public void setupTeam(Game game, final Team team)
	{
		team.getMessages().clear();
		team.getMessages().add(new Message("You are " + str(team.getValue())));
	}

	@Override
	public void start(final Game game)
	{	
		super.start(game);		
		if (!hasNextSwap(game))
		{
			Log.warn("No Swaps at start of sort!");
		}
	}

	@Override
	public void update(Game game, final Team team, final Collection<PositionLogItem> log, final ObjectStore store)
	{
		if (inPosition(game))
		{
			if (!hasNextSwap(game))
			{
				final Stage stage = nextStage(game, store);
				if (stage != null)
				{
					stage.update(game, team, log, store);
				}
			}
		}
	}

	protected String str(final int value)
	{
		return new String(Character.toChars(value + 64));
	}
	
	protected abstract String findNextSwap(final Game game);

	protected abstract String getSortName();

	protected void incComparisons()
	{
		comparisons++;
	}

	private boolean hasNextSwap(final Game game)
	{
		String order = "";
		for (final Marker marker : game.getMarkers())
		{
			order = order + str(marker.getValue());
		}
		Log.info("Order = " + order);

		final String swap = findNextSwap(game);
		if (swap == null)
		{
			Log.info("No swaps left");
			return false;
		}
		else
		{
			Log.info("Swapping " + swap);
			swaps++;
			game.getMessages().clear();
			game.getMessages().add(new Message(getSortName() + " Sort: " + order));
			game.getMessages().add(new Message("Swapping " + swap));
			game.getMessages().add(new Message(comparisons + " comparisons, " + swaps + " swaps"));
			return true;
		}
	}

	private boolean inPosition(final Game game)
	{
		for (final Team team : game.getTeams())
		{
			final Marker near = game.nearMarker(team.getLastKnown());
			if (near == null)
			{
				return false;
			}
			else if (near.getValue() != team.getValue()) { return false; }
		}

		return true;
	}
}
