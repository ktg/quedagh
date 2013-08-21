package uk.ac.nott.mrl.quedagh.model.stages;

import java.util.Collection;
import java.util.HashSet;

import uk.ac.nott.mrl.quedagh.model.Game;
import uk.ac.nott.mrl.quedagh.model.Marker;
import uk.ac.nott.mrl.quedagh.model.Message;
import uk.ac.nott.mrl.quedagh.model.PositionLogItem;
import uk.ac.nott.mrl.quedagh.model.Team;

import com.googlecode.objectify.annotation.EntitySubclass;

@EntitySubclass
public class SortSetup extends Stage
{
	public SortSetup(final String id, final Stage next)
	{
		super(id, next);
	}

	SortSetup()
	{

	}

	@Override
	public void update(final Team team, final Collection<PositionLogItem> log)
	{
		Game game = team.getGame();
		
		// Check team is at a marker
		if (isFinished(game))
		{

			// TODO game.setStage(new Sort());
		}

		team.getMessages().clear();
		final Marker near = game.nearMarker(team.getLastKnown());
		if (near == null)
		{
			team.getMessages().add(new Message("Go to a marker"));
		}
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
}
