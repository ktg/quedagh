package uk.ac.nott.mrl.quedagh.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import uk.ac.nott.mrl.quedagh.model.stages.Stage;

import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Load;

@Entity
public class Level
{
	@Id
	private String id;

	private String name;
	private Collection<Position> markers = new HashSet<Position>();
	private Collection<Position> bounds = new ArrayList<Position>();

	@Load
	private Ref<Stage> start;

	public Collection<Position> getBounds()
	{
		return bounds;
	}

	public String getId()
	{
		return id;
	}

	public Collection<Position> getMarkers()
	{
		return markers;
	}

	public String getName()
	{
		return name;
	}

	public Stage getStart()
	{
		return start.get();
	}

	public void setId(final String id)
	{
		this.id = id;
	}

	public void setName(final String name)
	{
		this.name = name;
	}

	public void setStart(final Stage start)
	{
		this.start = Ref.create(start);
	}
}