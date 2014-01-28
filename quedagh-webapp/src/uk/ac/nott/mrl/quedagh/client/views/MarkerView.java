package uk.ac.nott.mrl.quedagh.client.views;

import org.wornchaos.views.View;

import uk.ac.nott.mrl.quedagh.model.Marker;

import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.overlays.MarkerImage;
import com.google.gwt.maps.client.overlays.MarkerOptions;

public class MarkerView implements View<Marker>
{
	private Marker marker;
	private MapWidget mapWidget;
	private com.google.gwt.maps.client.overlays.Marker mapMarker;

	public MarkerView(final MapWidget mapWidget, final Marker marker)
	{
		this.mapWidget = mapWidget;
		itemChanged(marker);
	}

	@Override
	public void itemChanged(final Marker item)
	{
		final Marker oldMarker = marker;
		marker = item;
		if (mapMarker == null)
		{
			mapMarker = createMarker();
		}
		else if (marker.getColour() != oldMarker.getColour())
		{
			mapMarker.clear();
			mapMarker = createMarker();
		}
	}
	
	public LatLng getLatLng()
	{
		return LatLng.newInstance(marker.getLatitude(), marker.getLongitude());
	}

	private com.google.gwt.maps.client.overlays.Marker createMarker()
	{
		final LatLng latlng = LatLng.newInstance(marker.getLatitude(), marker.getLongitude());
		final MarkerOptions options = MarkerOptions.newInstance();
		options.setPosition(latlng);
		options.setMap(mapWidget);
		if (marker.getColour() != null)
		{
			options.setIcon(MarkerImage.newInstance("/images/markers/marker_" + marker.getColour().name() + ".png"));
		}
		else
		{
			options.setIcon(MarkerImage.newInstance("/images/markers/marker_grey.png"));
		}
		return com.google.gwt.maps.client.overlays.Marker.newInstance(options);
	}
}
