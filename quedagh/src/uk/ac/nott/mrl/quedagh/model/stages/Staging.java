package uk.ac.nott.mrl.quedagh.model.stages;

import java.util.ArrayList;
import java.util.Collection;

import org.wornchaos.client.server.ObjectStore;

import uk.ac.nott.mrl.quedagh.model.Game;
import uk.ac.nott.mrl.quedagh.model.Message;
import uk.ac.nott.mrl.quedagh.model.PositionLogItem;
import uk.ac.nott.mrl.quedagh.model.Team;
import uk.ac.nott.mrl.quedagh.model.Game.Draw;

import com.googlecode.objectify.annotation.EntitySubclass;

@EntitySubclass
public class Staging extends Stage
{
	private Collection<Message> adminMessages = new ArrayList<Message>();

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

	public Collection<Message> getAdminMessages()
	{
		return adminMessages;
	}

	@Override
	public void response(Game game, final Team team, final String response, final ObjectStore store)
	{
		nextStage(game, store);
	}

	@Override
	public void setupTeam(Game game, final Team team)
	{
		if (team.isAdmin())
		{
			team.postMessages(adminMessages);
		}
	}

	@Override
	public void update(Game game, final Team team, final Collection<PositionLogItem> log, final ObjectStore store)
	{
	}
}