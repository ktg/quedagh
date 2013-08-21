package uk.ac.nott.mrl.quedagh.model.stages;

import java.util.Collection;

import uk.ac.nott.mrl.quedagh.model.Game;
import uk.ac.nott.mrl.quedagh.model.Marker;
import uk.ac.nott.mrl.quedagh.model.Message;
import uk.ac.nott.mrl.quedagh.model.PositionLogItem;
import uk.ac.nott.mrl.quedagh.model.Team;

import com.googlecode.objectify.annotation.EntitySubclass;

@EntitySubclass
public class Sort extends Stage
{
	private String sort;
	private String swap = null;

	@Override
	public void start(final Game game)
	{
		Marker marker1 = null;
		for (final Marker marker2 : game.getMarkers())
		{
			if (marker1 != null && marker1.getValue() > marker2.getValue())
			{
				swap = marker1.getValue() + "<->" + marker2.getValue();
				final short value1 = marker1.getValue();
				marker1.setValue(marker2.getValue());
				marker2.setValue(value1);
			}
			marker1 = marker2;
		}

		if (swap == null)
		{
			game.setStage(getNext());
		}
	}

	@Override
	public void update(final Team team, final Collection<PositionLogItem> log)
	{
		// Check team is at a marker
		team.getMessages().clear();
		team.getMessages().add(new Message(sort + " Sort: Swapping " + swap));
		team.getMessages().add(new Message("You are: " + team.getValue()));

		if (isFinished(team.getGame()))
		{

		}
	}

	private boolean isFinished(final Game game)
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