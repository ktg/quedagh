package uk.ac.nott.mrl.quedagh.view;

import java.util.EnumMap;
import java.util.Map;

import uk.ac.nott.mrl.quedagh.activities.PositionManager;
import uk.ac.nott.mrl.quedagh.model.Game;
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
	private final Paint fog;
	private final Paint path;
	private final Paint debug;

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
		path.setAlpha(255);
		path.setStyle(Style.FILL_AND_STROKE);

		debug = new Paint();
		debug.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
		debug.setColor(Color.BLACK);
		debug.setAlpha(64);
		debug.setStyle(Style.FILL_AND_STROKE);

		final Paint blue = new Paint(Paint.ANTI_ALIAS_FLAG);
		blue.setColor(Color.BLUE);
		blue.setAlpha(128);
		blue.setStyle(Style.STROKE);
		blue.setStrokeWidth(5);
		blue.setStrokeCap(Cap.ROUND);
		blue.setStrokeJoin(Join.ROUND);

		lines.put(Colour.blue, blue);

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

		if (game.getExplored() != null && !game.getExplored().isEmpty())
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

			for (final Position position : game.getExplored())
			{
				final Point drawPoint = map.getProjection().toScreenLocation(	new LatLng(position.getLatitude(),
																						position.getLongitude()));
				canvas.drawCircle(drawPoint.x, drawPoint.y, radius, path);
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

		for (final Marker marker : game.getMarkers())
		{
			if (marker.getColour() != null)
			{
				final Point point = map.getProjection().toScreenLocation(	new LatLng(marker.getLatitude(), marker
																					.getLongitude()));
				final Bitmap bitmap = getBitmap("marker", marker.getColour());
				canvas.drawBitmap(bitmap, point.x - (bitmap.getWidth() / 2), point.y - bitmap.getHeight(), null);
			}
		}

		for (final Team team : game.getTeams())
		{
			if (team.getDevice().equals(positionManager.getDeviceID()) && positionManager.getLocation() != null)
			{
				final Point point = map.getProjection().toScreenLocation(	new LatLng(positionManager.getLocation()
																					.getLatitude(), positionManager
																					.getLocation().getLongitude()));
				final Bitmap bitmap = getBitmap("bullet", team.getColour());
				canvas.drawBitmap(bitmap, point.x - (bitmap.getWidth() / 2), point.y - (bitmap.getHeight() / 2), null);
			}
			else if (team.getLastKnown() != null)
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