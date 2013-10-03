package uk.ac.nott.mrl.quedagh.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.wornchaos.client.objectify.RefCollection;
import org.wornchaos.client.parser.ParseGroup;
import org.wornchaos.client.server.Modified;

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
	
	@Id
	private String id;
	private float radius = 10;

	@Load
	@ParseGroup({ "complete" })
	private Ref<Level> level;

	private List<Marker> markers = new ArrayList<Marker>();

	private Collection<Ref<Team>> teams = new ArrayList<Ref<Team>>();

	private Collection<Message> messages = new ArrayList<Message>();

	@ParseGroup({ "logs" })
	private List<GameEvent> events = new ArrayList<GameEvent>();

	private long modified;
	
	private Draw draw = Draw.none;
	
	@Load
	@Index
	@ParseGroup({ "complete" })
	private Ref<Stage> stage;

	private Collection<Position> explored = new HashSet<Position>();

	public void addMessage(final Message message)
	{
		messages.add(message);
	}

	public Team createTeam(final String id, final String device)
	{
		for (final Ref<Team> team : teams)
		{
			if (team.get().getDevice().equals(device)) { return team.get(); }
		}

		final Team team = new Team(this);
		if (teams.size() == 0)
		{
			team.setAdmin(true);
		}
		team.setValue((short) (teams.size() + 1));
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

	public String getOrder()
	{
		String order = "";
		int index = 0;
		for (final Marker marker : getMarkers())
		{
			index++;
			if(marker.getColour() == null)
			{
				continue;
			}
			if(marker.getValue() == null)
			{
				order = order + str(index);				
			}
			else
			{
				order = order + str(marker.getValue());
			}
		}
		return order;
	}
	
	public static final String str(final int value)
	{
		return new String(Character.toChars(value + 64));
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

	public Team getTeam(final String device)
	{
		for (final Ref<Team> team : teams)
		{
			if (team.get().getDevice().equals(device)) { return team.get(); }
		}
		return null;
	}

	public Collection<Team> getTeams()
	{
		return new RefCollection<Team>(teams);
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
		final String order = getOrder();
		if(messages.iterator().hasNext())
		{
			for (final Message message : messages)
			{
				events.add(new GameEvent(time, order, message));
				this.messages.add(message);
			}
		}
		else
		{
			events.add(new GameEvent(time, order, null));
		}
	}

	public void postMessages(final Message... messages)
	{
		if ((messages == null || messages.length == 0) && this.messages.isEmpty()) { return; }
		this.messages.clear();
		final long time = new Date().getTime();
		final String order = getOrder();		
		if(messages.length != 0)
		{
			for (final Message message : messages)
			{
				events.add(new GameEvent(time, order, message));
				this.messages.add(message);
			}
		}
		else
		{
			events.add(new GameEvent(time, order, null));
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

	public void setId(final String id)
	{
		this.id = id;
	}

	public void setLevel(final Level level)
	{
		this.level = Ref.create(level);
		markers.clear();
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
		this.stage = Ref.create(stage);
		draw = stage.getDraw();
		stage.start(this);
	}

	public boolean hasNextStage()
	{
		return stage != null;
	}

	public Draw getDraw()
	{
		return draw;
	}

	public void setDraw(Draw draw)
	{
		this.draw = draw;
	}
}