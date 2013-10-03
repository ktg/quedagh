package uk.ac.nott.mrl.quedagh.client.pages;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.wornchaos.client.ui.Page;
import org.wornchaos.client.ui.Slider;
import org.wornchaos.client.ui.ViewCallback;
import org.wornchaos.views.View;

import uk.ac.nott.mrl.quedagh.client.Quedagh;
import uk.ac.nott.mrl.quedagh.client.views.TeamInfoView;
import uk.ac.nott.mrl.quedagh.model.Game;
import uk.ac.nott.mrl.quedagh.model.GameEvent;
import uk.ac.nott.mrl.quedagh.model.Message;
import uk.ac.nott.mrl.quedagh.model.Team;

import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.VerticalAlign;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.maps.client.MapOptions;
import com.google.gwt.maps.client.MapTypeId;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.events.click.ClickMapEvent;
import com.google.gwt.maps.client.events.click.ClickMapHandler;
import com.google.gwt.maps.client.mvc.MVCArray;
import com.google.gwt.maps.client.overlays.Marker;
import com.google.gwt.maps.client.overlays.MarkerImage;
import com.google.gwt.maps.client.overlays.MarkerOptions;
import com.google.gwt.maps.client.overlays.Polyline;
import com.google.gwt.maps.client.overlays.PolylineOptions;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

public class GameView extends Page implements View<Game>
{
	private static final DateTimeFormat formatter = DateTimeFormat.getFormat("h:mm:ss a");

	private MapWidget mapWidget;
	private Collection<Marker> markers = new ArrayList<Marker>();

	private String id;
	private long lastModified;

	private final Panel teamPanel = new FlowPanel();
	private final Panel allMessages = new FlowPanel();
	private final Panel visualize = new FlowPanel();

	private final Slider slider = new Slider();
	private final Label timeLabel = new Label();

	private Map<String, TeamInfoView> teamViews = new HashMap<String, TeamInfoView>();
	
	private String lastOrder = null;
	private Polyline orderLine = null;

	private long time = Long.MAX_VALUE;

	private Game game;

	public GameView(final String id)
	{
		this.id = id;
	}

	@Override
	public void itemChanged(final Game item)
	{
		if (item == null) { return; }
		if (lastModified != 0 && item.getModified() != 0 && lastModified > item.getModified()) { return; }
		game = item;

		lastModified = item.getModified();

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
			if (marker.getColour() != null)
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

		long oldest = Long.MAX_VALUE;
		long newest = Long.MIN_VALUE;

		for (final Team team : item.getTeams())
		{
			TeamInfoView view = teamViews.get(team.getDevice());
			if(view == null)
			{
				view = new TeamInfoView();
				teamPanel.add(view);	
				teamViews.put(team.getDevice(), view);
			}
			view.setTime(time);
			view.itemChanged(team);

			final MVCArray<LatLng> points = view.getTrail(time);
			if(points != null)
			{			
				if(points.getLength() > 1)
				{
					final PolylineOptions opts3 = PolylineOptions.newInstance();
					opts3.setPath(points);
					opts3.setStrokeColor(view.getHTMLColour());				
					opts3.setStrokeOpacity(0.6);
					opts3.setStrokeWeight(3);
					opts3.setVisible(false);
					opts3.setMap(mapWidget);
	
					final Polyline line = Polyline.newInstance(opts3);
					view.setTrail(line);
				}
				else
				{
					view.setTrail(null);
				}

				if(points.getLength() > 0)
				{
					final LatLng latlng = points.get(points.getLength() - 1);
					final MarkerOptions options = MarkerOptions.newInstance();
					options.setPosition(latlng);
					options.setMap(mapWidget);
					if (team.getColour() != null)
					{
						options.setIcon(MarkerImage.newInstance("/images/markers/bullet_" + team.getColour().name()
								+ ".png"));
					}
					else
					{
						options.setIcon(MarkerImage.newInstance("/images/markers/bullet_grey.png"));
					}
					final Marker amarker = Marker.newInstance(options);
					view.setMarker(amarker);
				}	
				else if(team.getLastKnown() != null)
				{
					final LatLng latlng = LatLng.newInstance(team.getLastKnown().getLatitude(), team.getLastKnown().getLongitude());
					final MarkerOptions options = MarkerOptions.newInstance();
					options.setPosition(latlng);
					options.setMap(mapWidget);
					if (team.getColour() != null)
					{
						options.setIcon(MarkerImage.newInstance("/images/markers/bullet_" + team.getColour().name()
								+ ".png"));
					}
					else
					{
						options.setIcon(MarkerImage.newInstance("/images/markers/bullet_grey.png"));
					}
					final Marker amarker = Marker.newInstance(options);
					view.setMarker(amarker);
				}
				else
				{
					view.setMarker(null);
				}
			}		
			
			oldest = Math.min(oldest, view.getOldest());
			newest = Math.max(newest, view.getNewest());			
		}

		if (item.getEvents() != null)
		{
			final Collection<GameEvent> events = new ArrayList<GameEvent>();
			long lastTime = 0;
			final Map<String, String> sorts = new HashMap<String, String>();
			String lastSort = null;
			for (final GameEvent event : item.getEvents())
			{
				if (event.getTime() > time)
				{
					break;
				}

				if(event.getText() != null && event.getText().contains(" Sort:"))
				{
					lastSort = event.getText().substring(0, event.getText().indexOf(" Sort:"));
				}
				else if(lastSort != null && event.getText().contains(" comparisons,"))
				{
					sorts.put(lastSort, event.getText());
					lastSort = null;
				}
				
				if (event.getTime() == lastTime)
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

			String order = null;
			allMessages.clear();			
			for (final GameEvent event : events)
			{
				allMessages.add(TeamInfoView.getMessageWidget(null, event));
				if (event.getOrder() != null)
				{
					order = event.getOrder();
				}
			}

			if (lastOrder == null || !lastOrder.equals(order))
			{
				lastOrder = order;
				visualize.clear();
				if (order != null)
				{
					final MVCArray<LatLng> points = MVCArray.newInstance();
					for (int index = 0; index < order.length(); index++)
					{
						final char orderItem = order.charAt(index);
						final int itemValue = orderItem - 'A' + 1;
						final Label label = new Label("" + orderItem);					
						label.getElement().getStyle().setDisplay(Display.INLINE_BLOCK);
						label.getElement().getStyle().setFontSize(30, Unit.PX);
						label.getElement().getStyle().setProperty("margin", "0 1px");
						label.getElement().getStyle().setProperty("border", "0px solid #000");
						label.getElement().getStyle().setProperty("border-top-width", itemValue * 10 + "px");						
						visualize.add(label);

						uk.ac.nott.mrl.quedagh.model.Marker marker = item.getMarkers().get(itemValue - 1);
						points.push(LatLng.newInstance(marker.getLatitude(), marker.getLongitude()));
					}
					
					if(points.getLength() > 0)
					{
						points.push(points.get(0));
						
						final PolylineOptions opts3 = PolylineOptions.newInstance();
						opts3.setPath(points);
						opts3.setStrokeColor("#000");				
						opts3.setStrokeOpacity(0.6);
						opts3.setStrokeWeight(3);
						opts3.setVisible(true);
						opts3.setMap(mapWidget);
		
						if(orderLine != null)
						{
							orderLine.setMap(null);
						}			
						
						orderLine = Polyline.newInstance(opts3);
					}
				}
				
				if(!sorts.isEmpty())
				{
					for(String sort: sorts.keySet())
					{
						visualize.add(new Label(sort + " Sort: " + sorts.get(sort)));
					}
				}				
			}
		}
		else
		{
			allMessages.clear();
			for (final Message message : item.getMessages())
			{
				allMessages.add(TeamInfoView.getMessageWidget(null, message));
			}
		}

		slider.setMin(oldest);
		slider.setMax(newest);
		if(time == Long.MAX_VALUE)
		{
			slider.setValue(newest);
		}
		timeLabel.setText(formatter.format(new Date((long)slider.getValue())));		
	}

	@Override
	protected Widget createView()
	{
		final FlowPanel panel = new FlowPanel();
		teamPanel.add(allMessages);		
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
		}.scheduleRepeating(1000);

		visualize.getElement().getStyle().setDisplay(Display.INLINE_BLOCK);
		visualize.getElement().getStyle().setVerticalAlign(VerticalAlign.TOP);

		slider.addChangeHandler(new ChangeHandler()
		{
			@Override
			public void onChange(final ChangeEvent event)
			{
				if(slider.getValue() == slider.getMax())
				{
					time = Long.MAX_VALUE;
				}
				else
				{
					time = (long) slider.getValue();					
				}
				itemChanged(game);
			}
		});
		slider.getElement().getStyle().setDisplay(Display.BLOCK);
		slider.getElement().getStyle().setWidth(750, Unit.PX);

	
		panel.add(mapWidget);
		panel.add(teamPanel);
		panel.add(visualize);
		panel.add(slider);
		panel.add(timeLabel);

		return panel;
	}
}