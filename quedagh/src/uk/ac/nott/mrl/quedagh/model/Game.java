package uk.ac.nott.mrl.quedagh.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.wornchaos.client.objectify.RefCollection;
import org.wornchaos.client.server.Modified;
import org.wornchaos.client.server.ParseExclude;
import org.wornchaos.logger.Log;

import uk.ac.nott.mrl.quedagh.model.Team.Colour;
import uk.ac.nott.mrl.quedagh.model.stages.Stage;

import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Load;
import com.googlecode.objectify.annotation.OnSave;

@Entity
public class Game
{
	public enum Draw
	{
		none, explored, order
	}

	public static final String str(final int value)
	{
		return new String(Character.toChars(value + 64));
	}

	@Id
	private String id;

	private float radius = 10;

	@Load
	@ParseExclude
	private Ref<Level> level;

	private List<Marker> markers = new ArrayList<Marker>();

	private Collection<Ref<Team>> teams = new ArrayList<Ref<Team>>();

	private Collection<Message> messages = new ArrayList<Message>();

	private Position comparison;

	@Logs
	private List<GameEvent> events = new ArrayList<GameEvent>();

	private long modified;

	private Draw draw = Draw.none;

	private String state;

	@Load
	@Index
	@ParseExclude
	private Ref<Stage> stage;

	private Collection<Position> explored = new HashSet<Position>();

	public void addMessage(final Message message)
	{
		messages.add(message);
	}

	public boolean areNear(final Position position, final Position position2)
	{
		return position.getDistance(position2) < radius;
	}

	public Team createTeam(final String id, final String device)
	{
		for (final Ref<Team> team : teams)
		{
			if (team.get().getDevice().equals(device)) { return team.get(); }
		}

		final Team team = new Team(this);
//		if (teams.size() == 0)
//		{
//			team.setAdmin(true);
//		}
		team.setColour(Colour.values()[teams.size()]);
		team.setId(id);
		team.setDevice(device);

		teams.add(Ref.create(team));

		if (stage != null)
		{
			getStage().setupTeam(this, team);
		}

		return team;
	}

	public Position getComparison()
	{
		return comparison;
	}

	public Draw getDraw()
	{
		return draw;
	}

	public List<GameEvent> getEvents()
	{
		return events;
	}

	public Collection<Position> getExplored()
	{
		return explored;
	}

	public String getId()
	{
		return id;
	}

	public Level getLevel()
	{
		if (level == null) { return null; }
		return level.get();
	}

	public Marker getMarker(final Position position)
	{
		for (final Marker marker : markers)
		{
			if (marker.equals(position)) { return marker; }
		}
		return null;
	}

	public List<Marker> getMarkers()
	{
		return markers;
	}

	public Iterable<Message> getMessages()
	{
		return messages;
	}
	
	@Modified
	public long getModified()
	{
		return modified;
	}

	public float getRadius()
	{
		return radius;
	}

	public Stage getStage()
	{
		if (stage == null) { return null; }
		return stage.get();
	}

	public String getState()
	{
		return state;
	}

	public Team getTeam(final String device)
	{
		for (final Ref<Team> team : teams)
		{
			try
			{
				if (team.get().getDevice().equals(device)) { return team.get(); }
			}
			catch(Exception e)
			{
				Log.error(e);
			}
		}
		return null;
	}

	public Collection<Team> getTeams()
	{
		return new RefCollection<Team>(teams);
	}

	public boolean hasNextStage()
	{
		return stage != null;
	}

	public boolean isInOrder()
	{
		char last = 0;
		for (final char value : state.toCharArray())
		{
			if (last > value) { return false; }
			last = value;
		}
		return true;
	}

	@OnSave
	public void markChanged()
	{
		modified = new Date().getTime();
	}

	public Marker nearMarker(final Position position)
	{
		for (final Marker marker : markers)
		{
			if (marker.getDistance(position) < radius) { return marker; }
		}
		return null;
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
				events.add(new GameEvent(time, state, getStageKey(), message));
				this.messages.add(message);
			}
		}
		else
		{
			events.add(new GameEvent(time, state, getStageKey(), null));
		}
	}

	String getStageKey()
	{
		if(stage == null)
		{
			return null;
		}
		return stage.getKey().getName();
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
				events.add(new GameEvent(time, state, getStageKey(), message));
				this.messages.add(message);
			}
		}
		else
		{
			events.add(new GameEvent(time, state, getStageKey(), null));
		}
	}

	public void reset()
	{
		explored.clear();
		messages.clear();
		events.clear();
		for (final Team team : getTeams())
		{
			team.reset();
		}
		setLevel(getLevel());
	}

	public void setComparison(final Position comparison)
	{
		this.comparison = comparison;
	}

	public void setDraw(final Draw draw)
	{
		this.draw = draw;
	}

	public void setId(final String id)
	{
		this.id = id;
	}

	public void setLevel(final Level level)
	{
		markers.clear();
		if (level == null)
		{
			this.level = null;
			return;
		}
		this.level = Ref.create(level);
		setRadius(level.getRadius());
		setComparison(level.getComparison());
		for (final Position position : level.getMarkers())
		{
			markers.add(new Marker(position.getLatitude(), position.getLongitude()));
		}

		setStage(level.getStart());
	}

	public void setModified(final long modified)
	{
		this.modified = modified;
	}

	public void setRadius(final float radius)
	{
		this.radius = radius;
	}

	public void setStage(final Stage stage)
	{
		if(stage == null)
		{
			this.stage = null;
		}
		else
		{
			this.stage = Ref.create(stage);
			draw = stage.getDraw();
			stage.start(this);
		}
	}

	public void setState(final String state)
	{
		this.state = state;
	}
}