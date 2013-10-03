package uk.ac.nott.mrl.quedagh.model.stages;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
	public void start(final Game game)
	{
		game.getExplored().clear();
		super.start(game);
	}

	@Override
	public void setupTeam(Game game, Team team)
	{
		team.postMessages(new Message("Travelled " + Math.round(team.getDistance()) + "m: Found 0/"
										+ game.getMarkers().size()));
	}

	@Override
	public void update(Game game, final Team team, final Collection<PositionLogItem> log, final ObjectStore store)
	{
		final List<Marker> remaining = new ArrayList<Marker>();
		for (final Marker marker : game.getMarkers())
		{
			if (marker.getColour() == null)
			{
				remaining.add(marker);
			}
		}

		for (final PositionLogItem item : log)
		{
			game.getExplored().add(item);

			for (final Marker marker : remaining)
			{
				if (marker.getDistance(item) < game.getRadius())
				{
					marker.setColour(team.getColour());
					remaining.remove(marker);
					break;
				}
			}
		}

		if (remaining.isEmpty())
		{
			Stage next = getNext();
			game.setStage(next);
			store.store(game);
			next.update(null, team, log, store);			
		}
		else
		{
			if(team.getDistance() > 1000)
			{
				team.postMessages(new Message("Travelled " + Math.round(team.getDistance() / 100) / 10.0 + "km: Found "
						+ (game.getMarkers().size() - remaining.size()) + "/"
						+ game.getMarkers().size()));				
			}
			else
			{
				team.postMessages(new Message("Travelled " + Math.round(team.getDistance()) + "m: Found "
												+ (game.getMarkers().size() - remaining.size()) + "/"
												+ game.getMarkers().size()));
			}
		}
	}
}
