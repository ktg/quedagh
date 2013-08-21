package uk.ac.nott.mrl.quedagh.tiles;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import uk.ac.nott.mrl.quedagh.model.Game;
import uk.ac.nott.mrl.quedagh.model.Position;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileProvider;

public class FoggyTileProvider implements TileProvider
{
	private static final int TILE_SIZE = 512;

	private static final Paint fog;
	private static final Paint path;

	private static final Tile blankTile;

	private static double fromLatitude(final double latitude)
	{
		final double radians = Math.toRadians(latitude + 90) / 2;
		return Math.toDegrees(Math.log(Math.tan(radians)));
	}

	private static PointF fromLatLng(final Position position, final int zoom)
	{
		final int noTiles = (1 << zoom);
		final double mercator = fromLatitude(position.getLatitude());
		final double y = ((180 - mercator) / 360) * noTiles;

		final double longitudeSpan = 360.0 / noTiles;

		final double x = (position.getLongitude() + 180) / longitudeSpan;

		// final float y = ((float) (mercator / 360 * noTiles));
		return new PointF((float) x, (float) y);
	}

	private static LatLngBounds getTileBounds(final int x, final int y, final int zoom, final double border)
	{
		final double yMin = y - border;
		final double yMax = y + 1 + border;

		final double xMin = x - border;
		final double xMax = x + 1 + border;

		final int noTiles = (1 << zoom);
		final double longitudeSpan = 360.0 / noTiles;
		final double longitudeMin = -180.0 + xMin * longitudeSpan;
		final double longitudeMax = -180.0 + xMax * longitudeSpan;

		final double mercatorMax = 180 - (yMin / noTiles) * 360;
		final double mercatorMin = 180 - (yMax / noTiles) * 360;
		final double latitudeMax = toLatitude(mercatorMax);
		final double latitudeMin = toLatitude(mercatorMin);

		final LatLngBounds bounds = new LatLngBounds(new LatLng(latitudeMin, longitudeMin), new LatLng(latitudeMax,
				longitudeMax));

		return bounds;
	}

	private static double toLatitude(final double mercator)
	{
		final double radians = Math.atan(Math.exp(Math.toRadians(mercator)));
		return Math.toDegrees(2 * radians) - 90;
	}

	private int zoomLevel = 0;

	private final Game game;

	private Map<String, Tile> tileCache = new HashMap<String, Tile>();

	static
	{
		fog = new Paint(Paint.ANTI_ALIAS_FLAG);
		fog.setColor(Color.BLACK);
		fog.setAlpha(128);
		fog.setStyle(Style.FILL_AND_STROKE);

		path = new Paint(Paint.ANTI_ALIAS_FLAG);
		path.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
		path.setColor(Color.BLACK);
		path.setAlpha(255);
		path.setStyle(Style.FILL_AND_STROKE);

		final Bitmap bitmap = Bitmap.createBitmap(TILE_SIZE, TILE_SIZE, Bitmap.Config.ARGB_8888);

		final Canvas canvas = new Canvas(bitmap);
		canvas.drawPaint(fog);

		final ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
		blankTile = new Tile(TILE_SIZE, TILE_SIZE, stream.toByteArray());
	}

	public FoggyTileProvider(final Game game)
	{
		this.game = game;
	}

	@Override
	public Tile getTile(final int x, final int y, final int zoom)
	{
		final String key = x + "," + y;
		if (zoom != zoomLevel)
		{
			tileCache.clear();
			zoomLevel = zoom;
		}
		else
		{
			final Tile tile = tileCache.get(key);
			if (tile != null) { return tile; }
		}

		final Tile tile = createTile(x, y, zoom);
		tileCache.put(key, tile);
		return tile;
	}

	public void newLocation(final Position position)
	{
		final PointF tilePoint = fromLatLng(position, zoomLevel);
		final int x = (int) tilePoint.x;
		final int y = (int) tilePoint.y;
		final String key = x + "," + y;
		if (tileCache.containsKey(key))
		{
			tileCache.remove(key);
		}

		// TODO Update edge tiles
	}

	private Tile createTile(final int x, final int y, final int zoom)
	{
		Bitmap bitmap = null;
		Canvas canvas = null;

		final LatLngBounds bounds = getTileBounds(x, y, zoom, 0);

		final float[] results = new float[1];
		Location.distanceBetween(	bounds.northeast.latitude, bounds.northeast.longitude, bounds.northeast.latitude,
									bounds.southwest.longitude, results);
		Log.i("Quedagh", "Tile Width = " + results[0] + "m");

		final float radius = TILE_SIZE / results[0] * game.getRadius();

		for (final Position position : game.getExplored())
		{
			final PointF tilePoint = fromLatLng(position, zoom);
			final PointF drawPoint = new PointF((tilePoint.x - x) * TILE_SIZE, (tilePoint.y - y) * TILE_SIZE);

			if (drawPoint.x + radius >= 0 && drawPoint.x - radius <= TILE_SIZE && drawPoint.y + radius >= 0
					&& drawPoint.y - radius <= TILE_SIZE)
			{
				if (bitmap == null)
				{
					bitmap = Bitmap.createBitmap(TILE_SIZE, TILE_SIZE, Bitmap.Config.ARGB_8888);
					canvas = new Canvas(bitmap);
					canvas.drawPaint(fog);
				}

				canvas.drawCircle(drawPoint.x, drawPoint.y, radius, path);
			}
		}

		if (bitmap == null) { return blankTile; }

		final ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
		return new Tile(TILE_SIZE, TILE_SIZE, stream.toByteArray());
	}
}