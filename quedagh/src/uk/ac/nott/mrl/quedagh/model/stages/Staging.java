package uk.ac.nott.mrl.quedagh.model.stages;

import java.util.ArrayList;
import java.util.Collection;

import org.wornchaos.client.server.ObjectStore;

import uk.ac.nott.mrl.quedagh.model.Game;
import uk.ac.nott.mrl.quedagh.model.Message;
import uk.ac.nott.mrl.quedagh.model.PositionLogItem;
import uk.ac.nott.mrl.quedagh.model.Team;

import com.googlecode.objectify.annotation.EntitySubclass;

@EntitySubclass
public class Staging extends Stage
{
	private Collection<Message> adminMessages = new ArrayList<Message>();

	public Staging(final String id)
	{
		super(id);
	}

	public Staging(final String id, final Stage next)
	{
		super(id, next);
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
		team.getMessages().clear();
		if (team.isAdmin())
		{
			team.getMessages().addAll(adminMessages);
		}
	}

	@Override
	public void update(Game game, final Team team, final Collection<PositionLogItem> log, final ObjectStore store)
	{
	}
}