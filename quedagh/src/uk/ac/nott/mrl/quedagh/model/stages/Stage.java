package uk.ac.nott.mrl.quedagh.model.stages;

import java.util.ArrayList;
import java.util.Collection;

import org.wornchaos.client.server.ObjectStore;

import uk.ac.nott.mrl.quedagh.model.Game;
import uk.ac.nott.mrl.quedagh.model.Message;
import uk.ac.nott.mrl.quedagh.model.PositionLogItem;
import uk.ac.nott.mrl.quedagh.model.Team;

import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Load;

@Entity
public abstract class Stage
{
	@Id
	private String id;

	@Load
	private Ref<Stage> next;

	private Collection<Message> messages = new ArrayList<Message>();

	Stage()
	{

	}

	Stage(final Stage next)
	{
		this.next = Ref.create(next);
	}

	Stage(final String id)
	{
		this.id = id;
	}

	Stage(final String id, final Stage next)
	{
		this.id = id;
		this.next = Ref.create(next);
	}

	Stage(final String id, final Stage next, final Collection<Message> messages)
	{
		this.id = id;
		this.next = Ref.create(next);
		this.messages = messages;
	}

	public String getId()
	{
		return id;
	}

	public Collection<Message> getMessages()
	{
		return messages;
	}

	public Stage getNext()
	{
		return next.get();
	}

	public void response(Game game, final Team team, final String response, final ObjectStore store)
	{
	}

	public void setId(final String id)
	{
		this.id = id;
	}

	public void setNext(final Stage next)
	{
		this.next = Ref.create(next);
	}

	public void setupTeam(Game game, Team team)
	{
		team.getMessages().clear();
	}

	protected Stage nextStage(final Game game, final ObjectStore store)
	{
		final Stage next = getNext(); 
		game.setStage(next);
		store.store(game);
		return next;
	}
	
	public void start(final Game game)
	{
		for (final Team team : game.getTeams())
		{
			setupTeam(game, team);
		}

		game.getMessages().clear();
		game.getMessages().addAll(messages);
	}

	public abstract void update(Game game, Team team, Collection<PositionLogItem> log, final ObjectStore store);
}