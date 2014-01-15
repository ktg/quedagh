package uk.ac.nott.mrl.quedagh.model;

import org.wornchaos.client.server.RequestParam;

import com.googlecode.objectify.annotation.Embed;

@Embed
public class Position
{
	private static final float EARTH_DIAMETER = 12756200;

	private float lat;
	private float lng;

	public Position(final float lat, final float lng)
	{
		super();
		this.lat = lat;
		this.lng = lng;
	}

	Position()
	{

	}

	@Override
	public boolean equals(final Object o)
	{
		if (o instanceof Position)
		{
			final Position p2 = (Position) o;
			return lat == p2.lat && lng == p2.lng;
		}
		return false;
	}

	public float getDistance(final Position position2)
	{
		if (position2 == null) { return 0; }
		final double dLat = deg2rad(position2.lat - lat);
		final double dLon = deg2rad(position2.lng - lng);
		final double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(deg2rad(lat))
				* Math.cos(deg2rad(position2.lat)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
		final double c = Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		final float d = (float) (EARTH_DIAMETER * c); // Distance in m
		return d;
	}

	@RequestParam("lat")
	public float getLatitude()
	{
		return lat;
	}

	@RequestParam("lng")
	public float getLongitude()
	{
		return lng;
	}

	@Override
	public int hashCode()
	{
		long bits = java.lang.Double.doubleToLongBits(lat);
		bits ^= java.lang.Double.doubleToLongBits(lng) * 31;
		return (((int) bits) ^ ((int) (bits >> 32)));
	}

	@RequestParam("lat")
	public void setLatitude(final float lat)
	{
		this.lat = lat;
	}

	@RequestParam("lng")
	public void setLongitude(final float lng)
	{
		this.lng = lng;
	}

	@Override
	public String toString()
	{
		return "{lat: " + lat + ", lng: " + lng + "}";
	}

	private double deg2rad(final double deg)
	{
		return deg * (Math.PI / 180);
	}
}
