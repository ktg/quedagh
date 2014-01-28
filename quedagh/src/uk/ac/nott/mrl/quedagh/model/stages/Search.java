package uk.ac.nott.mrl.quedagh.model.stages;

import java.util.Collection;

import org.wornchaos.client.server.ObjectStore;

import uk.ac.nott.mrl.quedagh.model.Game;
import uk.ac.nott.mrl.quedagh.model.Game.Draw;
import uk.ac.nott.mrl.quedagh.model.Marker;
import uk.ac.nott.mrl.quedagh.model.Message;
import uk.ac.nott.mrl.quedagh.model.PositionLogItem;
import uk.ac.nott.mrl.quedagh.model.Team;

import com.googlecode.objectify.annotation.EntitySubclass;

@EntitySubclass
public class Search extends Stage
{
	public Search(final String id)
	{
		super(id, Draw.explored);
	}

	public Search(final String id, final Stage next)
	{
		super(id, Draw.explored, next);
	}

	Search()
	{

	}

	@Override
	public void setupTeam(final Game game, final Team team)
	{
		team.postMessages(new Message("Travelled " + Math.round(team.getDistance()) + "m"));
	}

	@Override
	public void start(final Game game)
	{
		game.getExplored().clear();
		super.start(game);
	}

	private static int getCurrentFound(Game game)
	{
		if(game == null || game.getState() == null)
		{
			return 0;
		}
		int found = 0;		
		for(char character : game.getState().toCharArray())
		{
			if(character == '-')
			{
				found++;
			}
		}
		
		return found;
	}
	
	@Override
	public void update(final Game game, final Team team, final Collection<PositionLogItem> log, final ObjectStore store)
	{
		for (final PositionLogItem item : log)
		{
			game.getExplored().add(item);

			for (final Marker marker : game.getMarkers())
			{
				if (marker.getColour() == null && marker.getDistance(item) < game.getRadius())
				{
					marker.setColour(team.getColour());
				}
			}
		}

		final int oldFound = getCurrentFound(game);
		int found = 0;
		String state = "";
		for (final Marker marker : game.getMarkers())
		{
			if (marker.getColour() == null)
			{
				state = state + "-";
			}
			else
			{
				state = state + new String(Character.toChars(marker.getColour().ordinal() + 65));
				found++;
			}
		}
		
		if(oldFound != found)
		{
			game.setState(state);
			game.postMessages(new Message("Found " + found + "/" + game.getMarkers().size()));
		}

		if (found == game.getMarkers().size())
		{
			final Stage next = getNext();
			game.setStage(next);
			store.store(game);
			next.update(null, team, log, store);
		}
		else
		{
			if (team.getDistance() > 1000)
			{
				team.postMessages(new Message("Travelled " + Math.round(team.getDistance() / 100) / 10.0 + "km"));
			}
			else
			{
				team.postMessages(new Message("Travelled " + Math.round(team.getDistance()) + "m"));
			}
		}
	}
}
