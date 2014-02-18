package uk.ac.nott.mrl.quedagh.model;

import uk.ac.nott.mrl.quedagh.model.Team.Colour;

import com.googlecode.objectify.annotation.Embed;

@Embed
public class Marker extends Position
{
	private Colour colour = null;

	public Marker(final Colour colour, final float lat, final float lng)
	{
		super(lat, lng);
		this.colour = colour;
	}

	public Marker(final float latitude, final float longitude)
	{
		super(latitude, longitude);
	}

	Marker()
	{

	}

	public Colour getColour()
	{
		return colour;
	}

	public void setColour(final Colour device)
	{
		colour = device;
	}
}
