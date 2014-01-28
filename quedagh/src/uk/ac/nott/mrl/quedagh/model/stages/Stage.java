package uk.ac.nott.mrl.quedagh.model.stages;

import java.util.ArrayList;
import java.util.Collection;

import org.wornchaos.client.server.ObjectStore;

import uk.ac.nott.mrl.quedagh.model.Game;
import uk.ac.nott.mrl.quedagh.model.Game.Draw;
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

	private Draw draw;

	private Collection<Message> messages = new ArrayList<Message>();

	Stage()
	{

	}

	Stage(final Stage next)
	{
		this.next = Ref.create(next);
	}

	Stage(final String id, final Draw draw)
	{
		this.id = id;
		this.draw = draw;
	}

	Stage(final String id, final Draw draw, final Stage next)
	{
		this.id = id;
		this.draw = draw;
		if (next != null)
		{
			this.next = Ref.create(next);
		}
	}

	Stage(final String id, final Draw draw, final Stage next, final Collection<Message> messages)
	{
		this.id = id;
		this.draw = draw;
		if (next != null)
		{
			this.next = Ref.create(next);
		}
		this.messages = messages;
	}

	public Draw getDraw()
	{
		return draw;
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
		if (next == null) { return null; }
		return next.get();
	}

	public void response(final Game game, final Team team, final String response, final ObjectStore store)
	{
	}

	public void setDraw(final Draw draw)
	{
		this.draw = draw;
	}

	public void setId(final String id)
	{
		this.id = id;
	}

	public void setNext(final Stage next)
	{
		if(next == null)
		{
			this.next = null;
		}
		else
		{
			this.next = Ref.create(next);
		}
	}

	public void setupTeam(final Game game, final Team team)
	{
		team.postMessages();
	}

	public void start(final Game game)
	{
		for (final Team team : game.getTeams())
		{
			setupTeam(game, team);
		}

		game.postMessages(messages);
	}

	public abstract void update(Game game, Team team, Collection<PositionLogItem> log, final ObjectStore store);

	protected Stage startNextStage(final Game game, final ObjectStore store)
	{
		final Stage next = getNext();
		game.setStage(next);
		store.store(game);
		return next;
	}
}