package uk.ac.nott.mrl.quedagh.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.wornchaos.client.server.ParseExclude;

import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

@Entity
public class Team
{
	public enum Colour
	{
		blue, green, orange, pink, purple, red, white, yellow
	}

	private int value;

	@ParseExclude
	private Ref<Game> game;

	@Id
	private String id;

	@Index
	private String device;

	private boolean admin = false;

	private Colour colour;

	private float distance;

	private PositionLogItem lastKnown;

	@Logs
	private List<PositionLogItem> log = new ArrayList<PositionLogItem>();

	@Logs
	private List<GameEvent> events = new ArrayList<GameEvent>();

	private Collection<Message> messages = new ArrayList<Message>();

	public Team()
	{

	}

	public Team(final Game game)
	{
		this.game = Ref.create(game);
	}

	public void addMessage(final Message message)
	{
		messages.add(message);
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

	public Collection<GameEvent> getEvents()
	{
		return events;
	}

	public Game getGame()
	{
		if (game == null) { return null; }
		return game.get();
	}

	public String getId()
	{
		return id;
	}

	public PositionLogItem getLastKnown()
	{
		if (lastKnown == null && !log.isEmpty()) { return log.get(log.size() - 1); }
		return lastKnown;
	}

	public Collection<PositionLogItem> getLog()
	{
		return log;
	}

	public int getMessageCount()
	{
		return messages.size();
	}

	public Iterable<Message> getMessages()
	{
		return messages;
	}

	public String getName()
	{
		if (value == 0) { return colour.name(); }
		return "" + (char) value;
	}

	public int getValue()
	{
		return value;
	}

	public boolean isAdmin()
	{
		return admin;
	}

	public void postMessages(final Iterable<Message> messages)
	{
		if ((messages == null || !messages.iterator().hasNext()) && this.messages.isEmpty()) { return; }
		this.messages.clear();
		final long time = new Date().getTime();
		if (messages.iterator().hasNext())
		{
			for (final Message message : messages)
			{
				events.add(new GameEvent(time, null, getGame().getStageKey(), message));
				this.messages.add(message);
			}
		}
		else
		{
			events.add(new GameEvent(time, null, getGame().getStageKey(), null));
		}
	}

	public void postMessages(final Message... messages)
	{
		if ((messages == null || messages.length == 0) && this.messages.isEmpty()) { return; }
		this.messages.clear();
		final long time = new Date().getTime();
		if (messages.length != 0)
		{
			for (final Message message : messages)
			{
				events.add(new GameEvent(time, null, getGame().getStageKey(), message));
				this.messages.add(message);
			}
		}
		else
		{
			events.add(new GameEvent(time, null, getGame().getStageKey(), null));
		}
	}

	public void removeLogs(final Iterable<PositionLogItem> oldLogs)
	{
		for (final PositionLogItem item : oldLogs)
		{
			log.remove(item);
		}
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

	public void setId(final String id)
	{
		this.id = id;

	}

	public void setLastKnown(final PositionLogItem lastKnown)
	{
		this.lastKnown = lastKnown;
	}

	public void setValue(final int value)
	{
		this.value = value;
	}

	public void updateDistance(final long timestamp)
	{
		if (log == null || log.isEmpty()) { return; }

		float dist = 0;
		PositionLogItem last = null;
		for (final PositionLogItem positionLog : log)
		{
			if (positionLog.getTime() > timestamp)
			{
				if (last != null)
				{
					dist += last.getDistance(positionLog);
				}

				last = positionLog;
			}
		}

		distance = dist;
		lastKnown = last;
	}

	void reset()
	{
		messages.clear();
		events.clear();
		log.clear();
	}
}