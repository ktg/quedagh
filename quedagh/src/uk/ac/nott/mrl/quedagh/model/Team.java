package uk.ac.nott.mrl.quedagh.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.wornchaos.client.parser.ParseGroup;

import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

@Entity
@Cache
public class Team
{
	public enum Colour
	{
		blue, green, orange, pink, purple, red, white, yellow
	}

	private short value;

	@ParseGroup("none")
	private Ref<Game> game;

	@Id
	private String id;
	
	@Index
	private String device;

	private boolean admin = false;

	private Colour colour;

	private float distance;
	
	private PositionLogItem lastKnown;

	@ParseGroup("logs")
	private List<PositionLogItem> log = new ArrayList<PositionLogItem>();

	private Collection<Message> messages = new ArrayList<Message>();

	public Team(final Game game)
	{
		this.game = Ref.create(game);
	}

	Team()
	{

	}

	public Colour getColour()
	{
		return colour;
	}

	public String getDevice()
	{
		return device;
	}

	public float getDistance()
	{
		return distance;
	}

	public PositionLogItem getLastKnown()
	{
		return lastKnown;
	}

	public Collection<PositionLogItem> getLog()
	{
		return log;
	}

	public void setLastKnown(PositionLogItem lastKnown)
	{
		this.lastKnown = lastKnown;
	}

	public Collection<Message> getMessages()
	{
		return messages;
	}

	public int getValue()
	{
		return value;
	}

	public boolean isAdmin()
	{
		return admin;
	}

	public void removeLogs(final Iterable<PositionLogItem> oldLogs)
	{
		for (final PositionLogItem item : oldLogs)
		{
			log.remove(item);
		}
	}

	public Game getGame()
	{
		return game.get();
	}

	public void setAdmin(final boolean admin)
	{
		this.admin = admin;
	}

	public void setColour(final Colour colour)
	{
		this.colour = colour;
	}

	public void setDevice(final String device)
	{
		this.device = device;
	}

	public void setDistance(final float distance)
	{
		this.distance = distance;
	}

	public void setValue(final short value)
	{
		this.value = value;
	}

	public void updateDistance()
	{
		if(log == null || log.isEmpty())
		{
			return;
		}		

		float dist = 0;
		PositionLogItem last = null;
		for (final PositionLogItem positionLog : log)
		{
			if (last != null)
			{
				dist += last.getDistance(positionLog);
			}

			last = positionLog;
		}

		distance = dist;
		
		lastKnown = log.get(log.size() - 1);		
	}

	public String getId()
	{
		return id;
	}
	
	public void setId(String id)
	{
		this.id = id;
		
	}
}