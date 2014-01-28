package uk.ac.nott.mrl.quedagh.view;

import java.util.EnumMap;
import java.util.Map;

import uk.ac.nott.mrl.quedagh.R;
import uk.ac.nott.mrl.quedagh.activities.PositionManager;
import uk.ac.nott.mrl.quedagh.model.Game;
import uk.ac.nott.mrl.quedagh.model.Game.Draw;
import uk.ac.nott.mrl.quedagh.model.Marker;
import uk.ac.nott.mrl.quedagh.model.Position;
import uk.ac.nott.mrl.quedagh.model.PositionLogItem;
import uk.ac.nott.mrl.quedagh.model.Team;
import uk.ac.nott.mrl.quedagh.model.Team.Colour;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.location.Location;
import android.util.AttributeSet;
import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.VisibleRegion;

public class TrailView extends View
{
	private final Paint orderLine;
	private final Paint fog;
	private final Paint path;
	private final Paint debug;
	private final Paint text;

	private PositionManager positionManager;

	private final Map<Colour, Paint> lines = new EnumMap<Colour, Paint>(Colour.class);

	private GoogleMap map;

	private String packageName;

	public TrailView(final Context context, final AttributeSet attrs)
	{
		super(context, attrs);

		if (android.os.Build.VERSION.SDK_INT >= 11)
		{
			setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		}

		fog = new Paint(Paint.ANTI_ALIAS_FLAG);
		fog.setColor(Color.BLACK);
		fog.setAlpha(128);
		fog.setStyle(Style.FILL_AND_STROKE);

		path = new Paint(Paint.ANTI_ALIAS_FLAG);
		path.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
		path.setColor(Color.BLACK);
		path.setStrokeWidth(5);
		path.setAlpha(128);
		path.setStyle(Style.FILL_AND_STROKE);

		debug = new Paint();
		debug.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
		debug.setColor(Color.BLACK);
		debug.setAlpha(64);
		debug.setStyle(Style.FILL_AND_STROKE);

		orderLine = new Paint(Paint.ANTI_ALIAS_FLAG);
		orderLine.setColor(Color.BLACK);
		orderLine.setAlpha(128);
		orderLine.setStyle(Style.STROKE);
		orderLine.setStrokeWidth(3);
		
		text = new Paint(Paint.ANTI_ALIAS_FLAG); 
		text.setColor(Color.BLACK); 
		text.setStyle(Style.FILL); 
		text.setTextSize(38);

		for (final Colour colour : Colour.values())
		{
			final Paint colourLine = new Paint(Paint.ANTI_ALIAS_FLAG);
			switch (colour)
			{
				case blue:
					colourLine.setColor(Color.BLUE);
					break;

				case green:
					colourLine.setColor(Color.GREEN);
					break;

				case red:
					colourLine.setColor(Color.RED);
					break;

				case yellow:
					colourLine.setColor(Color.YELLOW);
					break;

				case white:
					colourLine.setColor(Color.WHITE);
					break;

				case purple:
					colourLine.setColor(Color.MAGENTA);
					break;

				case pink:
					colourLine.setColor(0xffff8888);
					break;

				case orange:
					colourLine.setColor(0xffff8800);
					break;

				default:
					colourLine.setColor(Color.BLACK);
					break;
			}
			colourLine.setAlpha(128);
			colourLine.setStyle(Style.STROKE);
			colourLine.setStrokeWidth(5);
			colourLine.setStrokeCap(Cap.ROUND);
			colourLine.setStrokeJoin(Join.ROUND);

			lines.put(colour, colourLine);
		}

	}

	public void setMap(final GoogleMap map)
	{
		this.map = map;
	}

	public void setPackageName(final String packageName)
	{
		this.packageName = packageName;
	}

	public void setPositionManager(final PositionManager manager)
	{
		positionManager = manager;
	}

	@Override
	@SuppressLint("DrawAllocation")
	protected void onDraw(final Canvas canvas)
	{
		super.onDraw(canvas);

		if (positionManager == null) { return; }

		final Game game = positionManager.getGame();
		if (map == null || game == null)
		{
			final Location location = positionManager.getLocation();
			if (location != null)
			{
				final Point point = map.getProjection().toScreenLocation(	new LatLng(location.getLatitude(), location
																					.getLongitude()));
				final Bitmap bitmap = getBitmap("bullet", null);
				canvas.drawBitmap(bitmap, point.x - (bitmap.getWidth() / 2), point.y - (bitmap.getHeight() / 2), null);
			}
			return;
		}

		if (game.getDraw() == Draw.explored)
		{
			canvas.drawPaint(fog);

			final VisibleRegion bounds = map.getProjection().getVisibleRegion();

			final float[] results = new float[1];
			Location.distanceBetween(	bounds.farRight.latitude, bounds.farRight.longitude, bounds.farLeft.latitude,
										bounds.farLeft.longitude, results);

			final float radius = getWidth() / results[0] * game.getRadius();

			for (final Position position : positionManager.getCurrent())
			{
				final Point drawPoint = map.getProjection().toScreenLocation(	new LatLng(position.getLatitude(),
																						position.getLongitude()));
				canvas.drawCircle(drawPoint.x, drawPoint.y, radius, debug);
			}

			if (positionManager.getPending() != null)
			{
				for (final Position position : positionManager.getPending())
				{
					final Point drawPoint = map.getProjection().toScreenLocation(	new LatLng(position.getLatitude(),
																							position.getLongitude()));
					canvas.drawCircle(drawPoint.x, drawPoint.y, radius, debug);
				}
			}

			if (game.getExplored() != null)
			{
				for (final Position position : game.getExplored())
				{
					final Point drawPoint = map.getProjection().toScreenLocation(	new LatLng(position.getLatitude(),
																							position.getLongitude()));
					canvas.drawCircle(drawPoint.x, drawPoint.y, radius, path);
				}
			}
		}

		if (game.getDraw() == Draw.order)
		{
			final String order = game.getState();
			Point firstMarker = null;
			Point lastMarker = null;
			for (int index = 0; index < order.length(); index++)
			{
				final int markerIndex = order.charAt(index) - 'A';
				final Marker marker = game.getMarkers().get(markerIndex);
				final Point markerPoint = map.getProjection().toScreenLocation(	new LatLng(marker.getLatitude(), marker
																						.getLongitude()));
				if (firstMarker == null)
				{
					firstMarker = markerPoint;
				}

				if (lastMarker != null)
				{
					canvas.drawLine(lastMarker.x, lastMarker.y, markerPoint.x, markerPoint.y, orderLine);
				}

				lastMarker = markerPoint;
			}

			if (lastMarker != null && !lastMarker.equals(firstMarker))
			{
				canvas.drawLine(lastMarker.x, lastMarker.y, firstMarker.x, firstMarker.y, orderLine);
			}
		}

		for (final Team team : game.getTeams())
		{
			if (team.getLog() != null)
			{
				Point lastPoint = null;
				for (final PositionLogItem item : team.getLog())
				{
					final Point point = map.getProjection().toScreenLocation(	new LatLng(item.getLatitude(), item
																						.getLongitude()));
					if (lastPoint != null)
					{
						canvas.drawLine(lastPoint.x, lastPoint.y, point.x, point.y, lines.get(team.getColour()));
					}
					lastPoint = point;
				}
			}
		}

		if (game.getDraw() == Draw.order)
		{
			if (game.getComparison() != null)
			{
				final Point point = map.getProjection().toScreenLocation(	new LatLng(game.getComparison()
																					.getLatitude(), game
																					.getComparison().getLongitude()));
				final Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.center_flag);
				canvas.drawBitmap(bitmap, point.x - 2, point.y - bitmap.getHeight(), null);
			}
		}

		int index = 1;
		for (final Marker marker : game.getMarkers())
		{
			if (marker.getColour() != null)
			{
				final Point point = map.getProjection().toScreenLocation(	new LatLng(marker.getLatitude(), marker
																					.getLongitude()));
				final Bitmap bitmap = getBitmap("marker", marker.getColour());
				canvas.drawBitmap(bitmap, point.x - (bitmap.getWidth() / 2), point.y - bitmap.getHeight(), null);
				
				if(game.getDraw() == Draw.order)
				{
					String integer = "" + index;
					float width = text.measureText(integer);
					canvas.drawText(integer, point.x - (width / 2) , point.y - bitmap.getHeight() + text.getTextSize(), text);
				}
			}
			index++;
		}

		for (final Team team : game.getTeams())
		{
			if (team.getLastKnown() != null)
			{
				final Point point = map.getProjection().toScreenLocation(	new LatLng(team.getLastKnown()
																					.getLatitude(), team.getLastKnown()
																					.getLongitude()));
				final Bitmap bitmap = getBitmap("bullet", team.getColour());
				canvas.drawBitmap(bitmap, point.x - (bitmap.getWidth() / 2), point.y - (bitmap.getHeight() / 2), null);
			}
		}
	}

	private Bitmap getBitmap(final String prefix, final Colour colour)
	{
		String name = prefix + "_" + colour;
		if (colour == null)
		{
			name = prefix + "_grey";
		}
		final int identifier = getResources().getIdentifier(name, "drawable", packageName);
		return BitmapFactory.decodeResource(getResources(), identifier);
	}
}