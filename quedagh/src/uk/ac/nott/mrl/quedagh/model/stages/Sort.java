package uk.ac.nott.mrl.quedagh.model.stages;

import java.util.ArrayList;
import java.util.Collection;

import org.wornchaos.client.server.ObjectStore;
import org.wornchaos.logger.Log;

import uk.ac.nott.mrl.quedagh.model.Game;
import uk.ac.nott.mrl.quedagh.model.Game.Draw;
import uk.ac.nott.mrl.quedagh.model.Marker;
import uk.ac.nott.mrl.quedagh.model.Message;
import uk.ac.nott.mrl.quedagh.model.PositionLogItem;
import uk.ac.nott.mrl.quedagh.model.Team;

import com.googlecode.objectify.annotation.EntitySubclass;

@EntitySubclass
public abstract class Sort extends Stage
{
	protected int index = 0;
	protected String order;
	protected String compare;
	protected int comparisons = 0;
	protected int swaps = 0;
	protected int sortLength;

	public Sort(final String id, final Stage next)
	{
		super(id, Draw.order, next);
	}

	Sort()
	{
		super();
	}

	public int getComparisons()
	{
		return comparisons;
	}

	public int getSwaps()
	{
		return swaps;
	}

	@Override
	public void response(final Game game, final Team team, final String response, final ObjectStore store)
	{
		if (game.isInOrder() && team.isAdmin())
		{
			final Stage stage = startNextStage(game, store);
			if (stage != null)
			{
				stage.update(game, team, new ArrayList<PositionLogItem>(), store);
			}
		}
	}

	@Override
	public void setupTeam(final Game game, final Team team)
	{
		team.postMessages(new Message("You are " + team.getName()));
	}

	@Override
	public void update(final Game game, final Team team, final Collection<PositionLogItem> log, final ObjectStore store)
	{
		while(inPosition(game))
		{
			nextStep(game);

			if(game.isInOrder())
			{		
				game.postMessages(new Message(getSortName() + " sorting " + game.getState()), new Message(comparisons + " comparisons, " + swaps + " swaps"));
				
				final Staging staging = new Staging(store.allocateID(Staging.class));
				staging.setDraw(Draw.order);
				staging.getMessages().add(new Message(getSortName() + " Sort Finished"));
				staging.getMessages().add(new Message(comparisons + " comparisons, " + swaps + " swaps"));
				staging.getMessages().add(new Message("Return Back to Base"));
				staging.getMessages().add( new Message("Start Next Stage", "next", true));
				staging.setNext(getNext());
				
				store.store(staging);
				game.setStage(staging);
				store.store(game);
				
				staging.update(game, team, log, store);
			}
			else
			{
				game.postMessages(new Message(getSortName() + " sorting " + game.getState()), new Message(comparisons
	                 + " comparisons, " + swaps + " swaps"));
			}

			store.store(this);
		}
	}

	protected abstract String getSortName();

	protected abstract String getStep();

	protected abstract void nextStep(final Game game);

	protected void updateTeamMessages(final Game game, final Message compareMessage)
	{
		for (final Team team : game.getTeams())
		{
			if (compare.indexOf(team.getValue()) != -1)
			{
				team.postMessages(new Message("You are " + team.getName()), compareMessage);
			}
			else if (team.getMessageCount() > 1)
			{
				if(inPosition(game, team))
				{
					team.postMessages(new Message("You are " + team.getName()));
				}
				else
				{
					team.postMessages(new Message("You are " + team.getName()), new Message("Move Back to your Marker"));
				}
			}
		}
	}

	private boolean inPosition(final Game game)
	{
		for (final Team team : game.getTeams())
		{
			if (!inPosition(game, team))
			{
				Log.info("Team " + team.getName() + " not in position");
				return false;
			}
		}

		return true;
	}

	private boolean inPosition(final Game game, final Team team)
	{
		if(game.isInOrder())
		{
			return false;
		}
		if (compare != null)
		{
			for (final char comparitor : compare.toCharArray())
			{
				if (team.getValue() == comparitor) { return game.areNear(team.getLastKnown(), game.getLevel()
						.getComparison()); }
			}
		}

		final String ordered = (order != null) ? order : game.getState();
		for (int index = 0; index < ordered.length(); index++)
		{
			final char markerValue = ordered.charAt(index);
			if (team.getValue() == markerValue)
			{
				final Marker marker = game.getMarkers().get(index);
				return team.getLastKnown().getDistance(marker) < (game.getRadius() * 2);
			}
		}

		return false;
	}
}