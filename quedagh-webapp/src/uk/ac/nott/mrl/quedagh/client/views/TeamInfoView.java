package uk.ac.nott.mrl.quedagh.client.views;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.wornchaos.client.server.AsyncCallback;
import org.wornchaos.views.View;

import uk.ac.nott.mrl.quedagh.client.Quedagh;
import uk.ac.nott.mrl.quedagh.model.Game;
import uk.ac.nott.mrl.quedagh.model.GameEvent;
import uk.ac.nott.mrl.quedagh.model.Message;
import uk.ac.nott.mrl.quedagh.model.PositionLogItem;
import uk.ac.nott.mrl.quedagh.model.Team;
import uk.ac.nott.mrl.quedagh.model.Team.Colour;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.mvc.MVCArray;
import com.google.gwt.maps.client.overlays.Marker;
import com.google.gwt.maps.client.overlays.Polyline;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

public class TeamInfoView extends Composite implements View<Team>
{
	interface TeamInfoUiBinder extends UiBinder<Widget, TeamInfoView>
	{
	}

	private static DateTimeFormat yearFormat = DateTimeFormat.getFormat("d MMM yyyy");

	private static DateTimeFormat monthFormat = DateTimeFormat.getFormat("d MMM");

	private static final TeamInfoUiBinder uiBinder = GWT.create(TeamInfoUiBinder.class);

	public static Widget getMessageWidget(final String device, final Message message)
	{
		if (message == null) { return null; }
		if (message.getResponse() == null) { return new Label(message.getText()); }
		final Button button = new Button(message.getText());
		button.addClickHandler(new ClickHandler()
		{
			@Override
			public void onClick(final ClickEvent event)
			{
				Quedagh.getServer().respond(device, message.getResponse(), new AsyncCallback<Game>()
				{

					@Override
					public void onSuccess(final Game arg0)
					{

					}
				});
			}
		});
		return button;
	}

	@SuppressWarnings("deprecation")
	private static String getRelativeTime(final long time)
	{
		if (time == 0) { return "Never"; }

		final Date now = new Date();
		final Date date = new Date(time);
		if (date.getYear() != now.getYear())
		{
			return yearFormat.format(date);
		}
		else if (date.getMonth() != now.getMonth() || date.getDate() != now.getDate())
		{
			return monthFormat.format(date);
		}
		else
		{
			final int hours = now.getHours() - date.getHours();
			if (hours != 0)
			{
				return hours + " hours ago";
			}
			else
			{
				final int minutes = now.getMinutes() - date.getMinutes();
				if (minutes != 0)
				{
					return minutes + " minutes ago";
				}
				else
				{
					return "just now";
				}
			}
		}
	}

	@UiField
	Image marker;

	@UiField
	Panel messages;

	private long oldest;
	private long newest;
	private long time;
	private Marker mapMarker;
	private Polyline trail;
	private long lastTime;
	private Team team;
		
	public TeamInfoView()
	{
		super();
		initWidget(uiBinder.createAndBindUi(this));
	}

	@Override
	public void itemChanged(final Team team)
	{
		this.team = team;
		if (team.getColour() != null)
		{
			marker.setUrl("/images/markers/bullet_" + team.getColour().name() + ".png");
		}
		else
		{
			marker.setUrl("/images/markers/bullet_grey.png");
		}

		messages.clear();
		if(team.getEvents() != null && time != 0)
		{
			final Collection<GameEvent> events = new ArrayList<GameEvent>();
			long lastTime = 0;
			for(GameEvent event: team.getEvents())
			{
				if(event.getTime() > time)
				{
					break;
				}
				
				if(event.getTime() == lastTime)
				{
					events.add(event);
				}
				else
				{
					lastTime = event.getTime();
					events.clear();
					events.add(event);
				}
			}
			
			for(final Message message: events)
			{
				messages.add(getMessageWidget(team.getDevice(), message));				
			}
		}
		else
		{
			for (final Message message : team.getMessages())
			{
				messages.add(getMessageWidget(team.getDevice(), message));
			}
		}
		
		if(team.getLastKnown() != null)
		{
			Label update = new Label("Updated " + getRelativeTime(team.getLastKnown().getTime()));
			update.getElement().getStyle().setProperty("fontSize", "x-small");
			update.getElement().getStyle().setColor("#555");
			messages.add(update);
		}
		else if(!team.getEvents().isEmpty())
		{
			long lastTime = 0;
			for(GameEvent event: team.getEvents())
			{
				lastTime = Math.max(lastTime, event.getTime());
			}
			
			if(lastTime != 0)
			{
				Label update = new Label("Updated " + getRelativeTime(lastTime));
				update.getElement().getStyle().setProperty("font-size", "x-small");
				update.getElement().getStyle().setColor("#555");
				messages.add(update);				
			}
		}
	}

	@UiHandler("marker")
	void toggleTrail(ClickEvent event)
	{
		if(trail != null)
		{
			trail.setVisible(!trail.getVisible());
		}
	}
	
	public MVCArray<LatLng> getTrail(final long time)
	{
		oldest = Long.MAX_VALUE;
		newest = Long.MIN_VALUE;
		
		final MVCArray<LatLng> points = MVCArray.newInstance();
		PositionLogItem last = null;
		for (final PositionLogItem logItem : team.getLog())
		{
			oldest = Math.min(oldest, logItem.getTime());
			newest = Math.max(newest, logItem.getTime());
			
			if (logItem.getTime() <= time)
			{
				LatLng latlng = LatLng.newInstance(logItem.getLatitude(), logItem.getLongitude());
				points.push(latlng);
				last = logItem;
			}
		}
		
		if(last == null || last.getTime() != lastTime)
		{
			if(last != null)
			{
				lastTime = last.getTime();
			}
			else
			{
				lastTime = 0;
			}
			return points;
		}
		return null;
	}
	
	public void setTime(long time)
	{
		this.time = time;
	}

	public void setTrail(Polyline line)
	{
		boolean visible = false;
		if(trail != null)
		{
			visible = trail.getVisible();
			trail.setMap(null);			
		}
		if(line != null)
		{
			line.setVisible(visible);
		}
		trail = line;		
	}

	public String getHTMLColour()
	{
		if (team.getColour() == Colour.blue)
		{
			return "#0000FF";
		}
		else if (team.getColour() == Colour.red)
		{
			return "#FF0000";
		}
		else if (team.getColour() == Colour.green)
		{
			return "#00FF00";
		}
		else if (team.getColour() == Colour.orange)
		{
			return "#FF6600";
		}
		else if (team.getColour() == Colour.pink)
		{
			return "#FF6666";
		}
		else if (team.getColour() == Colour.purple)
		{
			return "#FF00FF";
		}
		else if (team.getColour() == Colour.white)
		{
			return "#FFFFFF";
		}
		else if (team.getColour() == Colour.yellow)
		{
			return "#FFFF00";
		}
		return "#000000";
	}

	public void setMarker(Marker amarker)
	{
		if(mapMarker != null)
		{
			mapMarker.clear();
		}
		mapMarker = amarker;
		
	}

	public long getOldest()
	{
		return oldest;
	}
	
	public long getNewest()
	{
		return newest;
	}
}