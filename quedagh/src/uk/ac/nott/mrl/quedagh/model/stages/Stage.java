package uk.ac.nott.mrl.quedagh.model.stages;

import java.util.Collection;

import uk.ac.nott.mrl.quedagh.model.Game;
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

	Stage()
	{

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

	public String getId()
	{
		return id;
	}

	public Stage getNext()
	{
		return next.get();
	}

	public void response(final Team team, final String response)
	{
	}

	public void setId(final String id)
	{
		this.id = id;
	}

	public void setNext(final Ref<Stage> next)
	{
		this.next = next;
	}

	public void setNext(final Stage next)
	{
		this.next = Ref.create(next);
	}

	public void start(final Game game)
	{

	}

	public abstract void update(Team team, Collection<PositionLogItem> log);
}