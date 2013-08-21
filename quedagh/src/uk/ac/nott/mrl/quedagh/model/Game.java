package uk.ac.nott.mrl.quedagh.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.wornchaos.client.objectify.RefIterable;
import org.wornchaos.client.parser.ParseGroup;

import uk.ac.nott.mrl.quedagh.model.Team.Colour;
import uk.ac.nott.mrl.quedagh.model.stages.Stage;

import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Load;

@Entity
@Cache
public class Game
{
	@Id
	private String id;
	private float radius = 10;

	@Load
	@ParseGroup({ "complete" })
	private Ref<Level> level;

	private Collection<Marker> markers = new HashSet<Marker>();

	private Collection<Ref<Team>> teams = new ArrayList<Ref<Team>>();

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

	public Collection<Marker> getMarkers()
	{
		return markers;
	}

	public float getRadius()
	{
		return radius;
	}

	public Stage getStage()
	{
		return stage.get();
	}

	public Iterable<Team> getTeams()
	{
		return new RefIterable<Team>(teams);
	}

	public Marker nearMarker(final Position position)
	{
		for (final Marker marker : markers)
		{
			if (marker.getDistance(position) < radius) { return marker; }
		}
		return null;
	}

	public void reset()
	{
		explored.clear();
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
		stage.start(this);
	}
}