package uk.ac.nott.mrl.quedagh.model;

import java.util.ArrayList;
import java.util.Collection;

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
	private float radius;
	private Collection<Position> markers = new ArrayList<Position>();
	private Collection<Position> bounds = new ArrayList<Position>();
	private String initOrder;

	private Position comparison;

	@Load
	private Ref<Stage> start;

	public Collection<Position> getBounds()
	{
		return bounds;
	}

	public Position getComparison()
	{
		return comparison;
	}

	public String getId()
	{
		return id;
	}

	public String getInitOrder()
	{
		return initOrder;
	}

	public Collection<Position> getMarkers()
	{
		return markers;
	}

	public String getName()
	{
		return name;
	}

	public float getRadius()
	{
		return radius;
	}

	public Stage getStart()
	{
		return start.get();
	}

	public void setComparison(final Position comparison)
	{
		this.comparison = comparison;
	}

	public void setId(final String id)
	{
		this.id = id;
	}

	public void setInitOrder(final String initOrder)
	{
		this.initOrder = initOrder;
	}

	public void setName(final String name)
	{
		this.name = name;
	}

	public void setRadius(final float radius)
	{
		this.radius = radius;
	}

	public void setStart(final Stage start)
	{
		this.start = Ref.create(start);
	}
}