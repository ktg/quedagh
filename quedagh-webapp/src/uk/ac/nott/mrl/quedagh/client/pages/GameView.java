package uk.ac.nott.mrl.quedagh.client.pages;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.wornchaos.client.ui.Page;
import org.wornchaos.client.ui.Slider;
import org.wornchaos.client.ui.ViewCallback;
import org.wornchaos.logger.Log;
import org.wornchaos.views.View;

import uk.ac.nott.mrl.quedagh.client.Quedagh;
import uk.ac.nott.mrl.quedagh.client.views.MarkerView;
import uk.ac.nott.mrl.quedagh.client.views.TeamInfoView;
import uk.ac.nott.mrl.quedagh.model.Game;
import uk.ac.nott.mrl.quedagh.model.GameEvent;
import uk.ac.nott.mrl.quedagh.model.Message;
import uk.ac.nott.mrl.quedagh.model.PositionLogItem;
import uk.ac.nott.mrl.quedagh.model.Team;

import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.TextAlign;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.VerticalAlign;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.maps.client.MapOptions;
import com.google.gwt.maps.client.MapTypeId;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.base.LatLngBounds;
import com.google.gwt.maps.client.base.Point;
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
	private class Sort
	{
		private String name;
		private String details;
		private String stage;
		private String order;
		private long start;
		private long end;
	}
	
	private static final DateTimeFormat formatter = DateTimeFormat.getFormat("h:mm:ss a");

	private MapWidget mapWidget;
	private Map<uk.ac.nott.mrl.quedagh.model.Marker, MarkerView> markers = new HashMap<uk.ac.nott.mrl.quedagh.model.Marker, MarkerView>();

	private String id;
	private long lastModified;

	private final Panel teamPanel = new FlowPanel();
	private final Panel allMessages = new FlowPanel();
	private final Panel visualize = new FlowPanel();
	private final Panel priorSorts = new FlowPanel();

	private final Slider slider = new Slider();
	private final Label timeLabel = new Label();

	private Marker center = null;

	private Map<String, TeamInfoView> teamViews = new HashMap<String, TeamInfoView>();

	private String lastOrder = null;
	private Polyline orderLine = null;

	private List<Long> times = new ArrayList<Long>();
	private long time = Long.MAX_VALUE;

	private Game game;
	private Timer timer;

	public GameView(final String id)
	{
		this.id = id;
	}

	@Override
	public void onStop()
	{
		super.onStop();
		timer.cancel();
	}

	@Override
	public void itemChanged(final Game item)
	{
		if (item == null) { return; }
		if (lastModified != 0 && item.getModified() != 0 && lastModified >= item.getModified()) { return; }
		game = item;

		lastModified = item.getModified();

		final List<LatLng> boundryPoints = new ArrayList<LatLng>();

		for (final uk.ac.nott.mrl.quedagh.model.Marker marker : item.getMarkers())
		{
			final MarkerView view = markers.get(marker);
			if (view != null)
			{
				view.itemChanged(marker);
				boundryPoints.add(view.getLatLng());
			}
			else
			{
				final MarkerView newView = new MarkerView(mapWidget, marker);
				boundryPoints.add(newView.getLatLng());
				markers.put(marker, newView);
			}
		}
		
		if (center == null && item.getComparison() != null)
		{
			final LatLng latlng = LatLng.newInstance(item.getComparison().getLatitude(), item.getComparison()
					.getLongitude());
			final MarkerOptions options = MarkerOptions.newInstance();
			options.setPosition(latlng);
			options.setMap(mapWidget);
			boundryPoints.add(latlng);
			final MarkerImage image = MarkerImage.newInstance("/images/markers/center_flag.png");
			image.setAnchor(Point.newInstance(3, 29));
			options.setIcon(image);
			center = com.google.gwt.maps.client.overlays.Marker.newInstance(options);
		}

		long oldest = Long.MAX_VALUE;
		long newest = Long.MIN_VALUE;

		final Set<Long> allTimes = new HashSet<Long>();

		for (final Team team : item.getTeams())
		{
			for (final GameEvent event : team.getEvents())
			{
				allTimes.add(event.getTime());
			}

			for (final PositionLogItem logItem : team.getLog())
			{
				allTimes.add(logItem.getTime());
			}

			TeamInfoView view = teamViews.get(team.getDevice());
			if (view == null)
			{
				view = new TeamInfoView();
				teamPanel.add(view);
				teamViews.put(team.getDevice(), view);
			}
			view.setTime(time);
			view.itemChanged(team);

			final MVCArray<LatLng> points = view.getTrail(time);
			if (points != null)
			{
				if (points.getLength() > 1)
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

				if (points.getLength() != 0)
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
					boundryPoints.add(latlng);
					final Marker amarker = Marker.newInstance(options);
					view.setMarker(amarker);
				}
				else
				{
					view.setMarker(null);
				}
			}
			else
			{
				// TODO boundryPoints.add(view.);				
			}

			oldest = Math.min(oldest, view.getOldest());
			newest = Math.max(newest, view.getNewest());
		}

		if (boundryPoints.size() >= 2)
		{
			final LatLngBounds bounds = LatLngBounds.newInstance(boundryPoints.get(0), boundryPoints.get(0));
			for (final LatLng boundryPoint : boundryPoints)
			{
				bounds.extend(boundryPoint);
			}

			mapWidget.fitBounds(bounds);
		}

		if (item.getEvents() != null)
		{
			final Collection<GameEvent> events = new ArrayList<GameEvent>();
			long lastTime = 0;
			String stage = null;
			final Map<String, Sort> sorts = new HashMap<String, Sort>();
			times.clear();
			for (final GameEvent event : item.getEvents())
			{
				if(event.getTime() > time)
				{
					break;
				}
				
				Log.info(event.getStage());
				
				stage = event.getStage();
				Sort sort = sorts.get(event.getStage());
				if(sort == null)
				{
					sort = new Sort();
					sort.stage = event.getStage();
					sort.start = event.getTime();
					sort.order = event.getState();
					sort.end = event.getTime();
					sorts.put(sort.stage, sort);
				}
				else
				{
					sort.start = Math.min(event.getTime(), sort.start);
					sort.end = Math.max(event.getTime(), sort.end);
					sort.order = event.getState();					
				}
				
				if(event.getText() != null)
				{
					if(event.getText().contains(" sorting "))
					{
						sort.name = event.getText().substring(0, event.getText().indexOf(" sorting "));						
					}
					else if (event.getText().contains(" comparisons,"))
					{
						sort.details = event.getText();						
					}
				}										

				if (event.getTime() == lastTime)
				{
					events.add(event);
				}
				else
				{
					events.clear();
					events.add(event);
					lastTime = event.getTime();
					allTimes.add(lastTime);					
				}
			}

			allMessages.clear();
			for (final GameEvent event : events)
			{
				allMessages.add(TeamInfoView.getMessageWidget(null, event));
			}
			
			final Sort lastSort = sorts.get(stage);
			if (lastSort != null && lastSort.order != null && !lastSort.order.equals(lastOrder))
			{
				lastOrder = lastSort.order;
				visualize.clear();				
				if (orderLine != null)
				{
					orderLine.setMap(null);
				}
				
				if (lastOrder != null && !lastOrder.contains("-") && lastSort.name != null)
				{
					final MVCArray<LatLng> points = MVCArray.newInstance();
					for (int index = 0; index < lastOrder.length(); index++)
					{
						final char orderItem = lastOrder.charAt(index);
						final int itemValue = orderItem - 'A' + 1;
						final Label label = new Label("" + orderItem);
						label.getElement().getStyle().setFontSize(30, Unit.PX);
						label.getElement().getStyle().setProperty("margin", "0 1px");
						label.getElement().getStyle().setProperty("border", "0px solid #000");
						label.getElement().getStyle().setProperty("borderTopWidth", itemValue * 10 + "px");
						visualize.add(label);

						final uk.ac.nott.mrl.quedagh.model.Marker marker = item.getMarkers().get(itemValue - 1);
						points.push(LatLng.newInstance(marker.getLatitude(), marker.getLongitude()));
					}

					if (points.getLength() > 0)
					{
						points.push(points.get(0));

						final PolylineOptions opts3 = PolylineOptions.newInstance();
						opts3.setPath(points);
						opts3.setStrokeColor("#000");
						opts3.setStrokeOpacity(0.6);
						opts3.setStrokeWeight(3);
						opts3.setVisible(true);
						opts3.setMap(mapWidget);

						orderLine = Polyline.newInstance(opts3);
					}
				}

				priorSorts.clear();
				if (!sorts.isEmpty())
				{
					for (final Sort sort : sorts.values())
					{
						Log.info(sort.stage + ": " + sort.name + " = " + stage);
						if(sort.name == null || stage == null || stage.equals(sort.stage))
						{
							continue;
						}

						long totalDistance = 0;
						for(Team team: game.getTeams())
						{
							PositionLogItem lastLog = null;
							for(PositionLogItem log: team.getLog())
							{
								if(sort.start < log.getTime() && sort.end > log.getTime())
								{
									if(lastLog != null)
									{
										totalDistance += lastLog.getDistance(log);
									}
									lastLog = log;
								}
							}
						}
						String distance = "";
						if(totalDistance == 0)
						{
							
						}
						else if (totalDistance > 1000)
						{
							distance = ", " + Math.round(totalDistance / 100) / 10.0 + "km";
						}
						else
						{
							distance = ", " + Math.round(totalDistance) + "m";
						}
						
						priorSorts.add(new Label(sort.name + " Sort took " + sort.details + distance));
					}
				}
			}
		}
		else
		{
			allMessages.clear();
			boolean messages = false;
			for (final Message message : item.getMessages())
			{
				messages = true;
				allMessages.add(TeamInfoView.getMessageWidget(null, message));
			}

			allMessages.setVisible(messages);
		}

		times = new ArrayList<Long>(allTimes);
		Collections.sort(times);
		
		slider.setMin(0);
		slider.setMax(times.size() - 1);
		if (time == Long.MAX_VALUE)
		{
			slider.setValue(times.size() - 1);
		}

		if(!times.isEmpty())
		{
			final long current = times.get((int) slider.getValue());
			timeLabel.setText(formatter.format(new Date(current)));
		}
	}

	@Override
	protected Widget createView()
	{
		final FlowPanel panel = new FlowPanel();

		final FlowPanel overviewPanel = new FlowPanel();
		
		overviewPanel.getElement().getStyle().setMargin(5, Unit.PX);
		overviewPanel.getElement().getStyle().setProperty("padding", "3px 5px");
		overviewPanel.getElement().getStyle().setBackgroundColor("#ddd");
		overviewPanel.getElement().getStyle().setProperty("display", "flex");
		overviewPanel.getElement().getStyle().setProperty("flexDirection", "column");	
		
		visualize.getElement().getStyle().setProperty("display", "flex");
		visualize.getElement().getStyle().setProperty("alignItems", "flex-end");
		visualize.getElement().getStyle().setProperty("justifyContent", "center");		
		
		allMessages.getElement().getStyle().setProperty("display", "flex");
		allMessages.getElement().getStyle().setProperty("flexDirection", "column");
		allMessages.getElement().getStyle().setTextAlign(TextAlign.CENTER);

		overviewPanel.add(visualize);
		overviewPanel.add(allMessages);
		overviewPanel.add(priorSorts);
		
		teamPanel.add(overviewPanel);
		teamPanel.getElement().getStyle().setDisplay(Display.INLINE_BLOCK);
		teamPanel.getElement().getStyle().setVerticalAlign(VerticalAlign.TOP);
		teamPanel.getElement().getStyle().setFontSize(16, Unit.PX);
		teamPanel.getElement().getStyle().setWidth(350, Unit.PX);

		final LatLng center = LatLng.newInstance(52.952268, -1.187023);
		final MapOptions opts = MapOptions.newInstance();
		opts.setZoom(12);
		opts.setCenter(center);
		opts.setPanControl(false);
		opts.setStreetViewControl(false);
		opts.setMapTypeId(MapTypeId.ROADMAP);

		mapWidget = new MapWidget(opts);
		mapWidget.setSize("1000px", "800px");
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
		timer = new Timer()
		{
			@Override
			public void run()
			{
				Quedagh.getServer().getGame(id, new ViewCallback<Game>(GameView.this));
			}
		};
		timer.scheduleRepeating(1000);

		slider.addChangeHandler(new ChangeHandler()
		{
			@Override
			public void onChange(final ChangeEvent event)
			{
				if (slider.getValue() == slider.getMax())
				{
					time = Long.MAX_VALUE;
				}
				else
				{
					time = times.get((int) slider.getValue());
				}
				lastModified = 0;
				itemChanged(game);
			}
		});
		slider.getElement().getStyle().setDisplay(Display.BLOCK);
		slider.getElement().getStyle().setWidth(1000, Unit.PX);

		panel.add(mapWidget);
		panel.add(teamPanel);
		panel.add(slider);
		panel.add(timeLabel);

		return panel;
	}
}