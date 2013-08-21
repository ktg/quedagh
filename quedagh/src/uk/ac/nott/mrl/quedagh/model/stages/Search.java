package uk.ac.nott.mrl.quedagh.model.stages;

import java.util.Collection;

import uk.ac.nott.mrl.quedagh.model.Game;
import uk.ac.nott.mrl.quedagh.model.Marker;
import uk.ac.nott.mrl.quedagh.model.PositionLogItem;
import uk.ac.nott.mrl.quedagh.model.Team;

import com.googlecode.objectify.annotation.EntitySubclass;

@EntitySubclass
public class Search extends Stage
{
	public Search(final String id)
	{
		super(id);
	}

	public Search(final String id, final Stage next)
	{
		super(id, next);
	}

	Search()
	{

	}

	@Override
	public void start(final Game game)
	{
		game.getExplored().clear();
	}

	@Override
	public void update(final Team team, final Collection<PositionLogItem> log)
	{
		team.getMessages().clear();
		final Game game = team.getGame();
		for (final PositionLogItem item : log)
		{
			game.getExplored().add(item);

			int remaining = 0;
			for (final Marker marker : game.getMarkers())
			{
				if (marker.getColour() == null)
				{
					if (marker.getDistance(item) < game.getRadius())
					{
						marker.setColour(team.getColour());
					}
					else
					{
						remaining++;
					}
				}
			}

			if (remaining == 0)
			{
				game.setStage(getNext());
			}
		}
	}
}
