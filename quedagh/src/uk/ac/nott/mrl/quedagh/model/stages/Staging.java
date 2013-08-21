package uk.ac.nott.mrl.quedagh.model.stages;

import java.util.Collection;

import uk.ac.nott.mrl.quedagh.model.Game;
import uk.ac.nott.mrl.quedagh.model.Message;
import uk.ac.nott.mrl.quedagh.model.PositionLogItem;
import uk.ac.nott.mrl.quedagh.model.Team;

import com.googlecode.objectify.annotation.EntitySubclass;

@EntitySubclass
public class Staging extends Stage
{
	private String message;

	public Staging(final String id, final String message)
	{
		super(id);
		this.message = message;
	}

	public Staging(final String id, final String message, final Stage next)
	{
		super(id, next);
		this.message = message;
	}

	Staging()
	{

	}

	public String getMessage()
	{
		return message;
	}

	@Override
	public void response(final Team team, final String response)
	{
		final Game game = team.getGame();
		// TODO Asserts, etc
		for (final Team ateam : game.getTeams())
		{
			ateam.getMessages().clear();
		}
		game.setStage(getNext());
	}

	public void setMessage(final String message)
	{
		this.message = message;
	}

	@Override
	public void update(final Team team, final Collection<PositionLogItem> log)
	{
		team.getMessages().clear();
		team.getMessages().add(new Message(message));
		if (team.isAdmin())
		{
			team.getMessages().add(new Message("Start", "start"));
		}
	}
}