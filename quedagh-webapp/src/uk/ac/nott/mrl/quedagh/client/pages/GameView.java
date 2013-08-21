package uk.ac.nott.mrl.quedagh.client.pages;

import java.util.ArrayList;
import java.util.Collection;

import org.wornchaos.client.ui.Page;
import org.wornchaos.client.ui.ViewCallback;
import org.wornchaos.views.View;

import uk.ac.nott.mrl.quedagh.client.Quedagh;
import uk.ac.nott.mrl.quedagh.model.Game;
import uk.ac.nott.mrl.quedagh.model.PositionLogItem;
import uk.ac.nott.mrl.quedagh.model.Team;

import com.google.gwt.ajaxloader.client.ArrayHelper;
import com.google.gwt.maps.client.MapOptions;
import com.google.gwt.maps.client.MapTypeId;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.events.click.ClickMapEvent;
import com.google.gwt.maps.client.events.click.ClickMapHandler;
import com.google.gwt.maps.client.overlays.Marker;
import com.google.gwt.maps.client.overlays.MarkerImage;
import com.google.gwt.maps.client.overlays.MarkerOptions;
import com.google.gwt.maps.client.overlays.PolylineOptions;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Widget;

public class GameView extends Page implements View<Game>
{
	private MapWidget mapWidget;
	private Collection<Marker> markers = new ArrayList<Marker>();

	private String id;

	public GameView(final String id)
	{
		this.id = id;
	}

	@Override
	public void itemChanged(final Game item)
	{
		// TODO More efficient
		for (final Marker marker : markers)
		{
			marker.clear();
		}

		markers.clear();


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

		for(final Team team: item.getTeams())
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
				options.setIcon(MarkerImage.newInstance("/images/markers/marker_grey.png"));				
			}
			final Marker amarker = Marker.newInstance(options);

			markers.add(amarker);
			
			final Collection<LatLng> array = new ArrayList<LatLng>();
			for(PositionLogItem logItem: team.getLog())
			{
				array.add(LatLng.newInstance(logItem.getLatitude(), logItem.getLongitude()));
			}
			
			final PolylineOptions opts3 = PolylineOptions.newInstance();
			opts3.setMap(mapWidget);
			opts3.setPath(ArrayHelper.toJsArray(array.toArray(new LatLng[] {})));
			opts3.setStrokeColor("#00FF00");
			opts3.setStrokeOpacity(1.0);
			opts3.setStrokeWeight(2);			
		}
	}

	@Override
	protected Widget createView()
	{
		// TODO Auto-generated method stub

		final LatLng center = LatLng.newInstance(52.952268, -1.187023);
		final MapOptions opts = MapOptions.newInstance();
		opts.setZoom(12);
		opts.setCenter(center);
		opts.setPanControl(false);
		opts.setStreetViewControl(false);
		opts.setMapTypeId(MapTypeId.ROADMAP);

		mapWidget = new MapWidget(opts);
		mapWidget.setSize("750px", "500px");

		mapWidget.addClickHandler(new ClickMapHandler()
		{
			@Override
			public void onEvent(final ClickMapEvent event)
			{
			}
		});

		new Timer()
		{
			@Override
			public void run()
			{
				Quedagh.getServer().getGame(id, new ViewCallback<Game>(GameView.this));
			}
		}.scheduleRepeating(5000);

		return mapWidget;
	}
}