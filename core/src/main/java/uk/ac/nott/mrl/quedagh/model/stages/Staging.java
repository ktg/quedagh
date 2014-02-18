package uk.ac.nott.mrl.quedagh.model.stages;

import java.util.Collection;

import org.wornchaos.client.server.ObjectStore;

import uk.ac.nott.mrl.quedagh.model.Game;
import uk.ac.nott.mrl.quedagh.model.Game.Draw;
import uk.ac.nott.mrl.quedagh.model.PositionLogItem;
import uk.ac.nott.mrl.quedagh.model.Team;

import com.googlecode.objectify.annotation.EntitySubclass;

@EntitySubclass
public class Staging extends Stage
{
	public Staging(final String id)
	{
		super(id, Draw.none);
	}

	public Staging(final String id, final Stage next)
	{
		super(id, Draw.none, next);
	}

	Staging()
	{

	}

	@Override
	public void response(final Game game, final Team team, final String response, final ObjectStore store)
	{
		startNextStage(game, store);
	}

	@Override
	public void update(final Game game, final Team team, final Collection<PositionLogItem> log, final ObjectStore store)
	{
	}
}