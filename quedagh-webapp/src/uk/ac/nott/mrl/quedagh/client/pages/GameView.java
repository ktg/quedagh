package uk.ac.nott.mrl.quedagh.client.pages;

import java.util.ArrayList;
import java.util.Collection;

import org.wornchaos.client.ui.Page;
import org.wornchaos.client.ui.ViewCallback;
import org.wornchaos.logger.Log;
import org.wornchaos.views.View;

import uk.ac.nott.mrl.quedagh.client.Quedagh;
import uk.ac.nott.mrl.quedagh.client.views.TeamInfoView;
import uk.ac.nott.mrl.quedagh.model.Game;
import uk.ac.nott.mrl.quedagh.model.Message;
import uk.ac.nott.mrl.quedagh.model.PositionLogItem;
import uk.ac.nott.mrl.quedagh.model.Team;
import uk.ac.nott.mrl.quedagh.model.Team.Colour;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.VerticalAlign;
import com.google.gwt.maps.client.MapOptions;
import com.google.gwt.maps.client.MapTypeId;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.events.click.ClickMapEvent;
import com.google.gwt.maps.client.events.click.ClickMapHandler;
import com.google.gwt.maps.client.overlays.Marker;
import com.google.gwt.maps.client.overlays.MarkerImage;
import com.google.gwt.maps.client.overlays.MarkerOptions;
import com.google.gwt.maps.client.overlays.Polyline;
import com.google.gwt.maps.client.overlays.PolylineOptions;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

public class GameView extends Page implements View<Game>
{
	private MapWidget mapWidget;
	private Collection<Marker> markers = new ArrayList<Marker>();
	private Collection<Polyline> lines = new ArrayList<Polyline>();	

	private String id;
	private long lastModified;

	private Panel teamPanel;
	
	public GameView(final String id)
	{
		this.id = id;
	}

	@Override
	public void itemChanged(final Game item)
	{
		if(item == null)
		{
			return;
		}
		if(lastModified != 0 && item.getModified() != 0 && lastModified >= item.getModified())
		{
			return;
		}
		
		lastModified = item.getModified();
		
		for (final Marker marker : markers)
		{
			marker.clear();
		}

		for(final Polyline line: lines)
		{
			line.setVisible(false);
			line.setMap(null);			
		}
		
		markers.clear();
		lines.clear();

		for (final uk.ac.nott.mrl.quedagh.model.Marker marker : item.getMarkers())
		{
			final LatLng latlng = LatLng.newInstance(marker.getLatitude(), marker.getLongitude());
			final MarkerOptions options = MarkerOptions.newInstance();
			options.setPosition(latlng);
			options.setMap(mapWidget);
			if(marker.getColour() != null)
			{
				options.setIcon(MarkerImage.newInstance("/images/markers/marker_" + marker.getColour().name() + ".png"));
			}
			else
			{
				options.setIcon(MarkerImage.newInstance("/images/markers/marker_grey.png"));				
			}
			final Marker amarker = Marker.newInstance(options);

			markers.add(amarker);
		}

		teamPanel.clear();
		
		for(final Message message: item.getMessages())
		{
			teamPanel.add(TeamInfoView.getMessageWidget(null, message));
		}
		
		for(final Team team: item.getTeams())
		{
			if(team.getLastKnown() != null)
			{
				final LatLng latlng = LatLng.newInstance(team.getLastKnown().getLatitude(), team.getLastKnown().getLongitude());
				final MarkerOptions options = MarkerOptions.newInstance();
				options.setPosition(latlng);
				options.setMap(mapWidget);
				if(team.getColour() != null)
				{
					options.setIcon(MarkerImage.newInstance("/images/markers/bullet_" + team.getColour().name() + ".png"));
				}
				else
				{
					options.setIcon(MarkerImage.newInstance("/images/markers/bullet_grey.png"));				
				}
				final Marker amarker = Marker.newInstance(options);
	
				markers.add(amarker);
			}

			final JsArray<LatLng> points = (JsArray<LatLng>) JsArray.createArray();
			points.setLength(team.getLog().size());
			int index = 0;
			for(PositionLogItem logItem: team.getLog())
			{
				points.set(index, LatLng.newInstance(logItem.getLatitude(), logItem.getLongitude()));
				index++;
			}
			
			final PolylineOptions opts3 = PolylineOptions.newInstance();
			opts3.setPath(points);
			if(team.getColour() == Colour.blue)
			{
				opts3.setStrokeColor("#0000FF");				
			}
			else if(team.getColour() == Colour.red)
			{
				opts3.setStrokeColor("#FF0000");				
			}
			else if(team.getColour() == Colour.green)
			{
				opts3.setStrokeColor("#00FF00");				
			}			
			else if(team.getColour() == Colour.orange)
			{
				opts3.setStrokeColor("#FF6600");
			}
			else if(team.getColour() == Colour.pink)
			{
				opts3.setStrokeColor("#FF6666");
			}
			else
			{
				opts3.setStrokeColor("#000000");
			}
			opts3.setStrokeOpacity(0.6);
			opts3.setStrokeWeight(3);	
			opts3.setVisible(true);
			opts3.setMap(mapWidget);			
		
			Polyline line = Polyline.newInstance(opts3);
			lines.add(line);
			
			TeamInfoView teamView = new TeamInfoView();
			teamView.itemChanged(team);
			teamPanel.add(teamView);
		}
	}

	@Override
	protected Widget createView()
	{
		// TODO Auto-generated method stub
		final FlowPanel panel = new FlowPanel();
		teamPanel = new FlowPanel();
		teamPanel.getElement().getStyle().setDisplay(Display.INLINE_BLOCK);
		teamPanel.getElement().getStyle().setVerticalAlign(VerticalAlign.TOP);

		final LatLng center = LatLng.newInstance(52.952268, -1.187023);
		final MapOptions opts = MapOptions.newInstance();
		opts.setZoom(12);
		opts.setCenter(center);
		opts.setPanControl(false);
		opts.setStreetViewControl(false);
		opts.setMapTypeId(MapTypeId.ROADMAP);

		mapWidget = new MapWidget(opts);
		mapWidget.setSize("750px", "500px");
		mapWidget.getElement().getStyle().setDisplay(Display.INLINE_BLOCK);
		mapWidget.getElement().getStyle().setVerticalAlign(VerticalAlign.TOP);		

		mapWidget.addClickHandler(new ClickMapHandler()
		{
			@Override
			public void onEvent(final ClickMapEvent event)
			{
			}
		});

		Quedagh.getServer().getGame(id, new ViewCallback<Game>(GameView.this));		
		new Timer()
		{
			@Override
			public void run()
			{
				Quedagh.getServer().getGame(id, new ViewCallback<Game>(GameView.this));
			}
		}.scheduleRepeating(5000);

		panel.add(mapWidget);
		
		panel.add(teamPanel);
		
		return panel;
	}
}