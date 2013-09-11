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
	@Id
	private String id;
	private float radius = 10;

	@Load
	@ParseGroup({ "complete" })
	private Ref<Level> level;

	private List<Marker> markers = new ArrayList<Marker>();

	private Collection<Ref<Team>> teams = new ArrayList<Ref<Team>>();

	private Collection<Message> messages = new ArrayList<Message>();

	@ParseGroup({"logs"})
	private List<Event> events = new ArrayList<Event>();
	
	private long modified;
	
	@Modified
	public long getModified()
	{
		return modified;
	}
	
	public Collection<Message> getMessages()
	{
		return messages;
	}
	

	public void setModified(long modified)
	{
		this.modified = modified;
	}

	@Load
	@Index
	@ParseGroup({ "complete" })
	private Ref<Stage> stage;

	private Collection<Position> explored = new HashSet<Position>();

	public Team getTeam(final String device)
	{
		for (final Ref<Team> team : teams)
		{
			if (team.get().getDevice().equals(device)) { return team.get(); }
		}
		return null;
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
		
		if(stage != null)
		{
			getStage().setupTeam(this, team);
		}

		return team;
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
		if(level == null)
		{
			return null;
		}
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
	
	public List<Event> getEvents()
	{
		return events;
	}

	public float getRadius()
	{
		return radius;
	}

	public Stage getStage()
	{
		if(stage == null)
		{
			return null;
		}
		return stage.get();
	}

	public Collection<Team> getTeams()
	{
		return new RefCollection<Team>(teams);
	}

	public Marker nearMarker(final Position position)
	{
		for (final Marker marker : markers)
		{
			if (marker.getDistance(position) < radius) { return marker; }
		}
		return null;
	}
	
	@OnSave
	public void markChanged()
	{
		modified = new Date().getTime();
	}

	public void reset()
	{
		explored.clear();
		messages.clear();
		events.clear();
		for(Team team: getTeams())
		{
			team.getMessages().clear();
			team.getLog().clear();
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

	public void setRadius(final float radius)
	{
		this.radius = radius;
	}

	public void setStage(final Stage stage)
	{
		this.stage = Ref.create(stage);
		if(stage != null)
		{
			events.add(new Event(stage.getClass().getName() + " started", new Date().getTime()));
		}
		stage.start(this);
	}
}