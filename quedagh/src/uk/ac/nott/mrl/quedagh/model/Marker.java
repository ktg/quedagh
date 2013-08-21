package uk.ac.nott.mrl.quedagh.model;

import uk.ac.nott.mrl.quedagh.model.Team.Colour;

import com.googlecode.objectify.annotation.Embed;

@Embed
public class Marker extends Position
{
	private Short value = null;
	private Colour colour = null;

	public Marker(final Colour colour, final float latitude, final float longitude)
	{
		super(latitude, longitude);
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

	public Short getValue()
	{
		return value;
	}

	public void setColour(final Colour device)
	{
		colour = device;
	}

	public void setValue(final Short value)
	{
		this.value = value;
	}
}
