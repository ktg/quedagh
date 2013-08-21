package uk.ac.nott.mrl.quedagh.model;

import java.util.Date;

import com.googlecode.objectify.annotation.Embed;

@Embed
public class PositionLogItem extends Position
{
	private Date time;
	private float err;

	public PositionLogItem(final float latitude, final float longitude, final float error)
	{
		super(latitude, longitude);
		time = new Date();
		this.err = error;
	}

	public PositionLogItem(final float latitude, final float longitude, final Date time, final float error)
	{
		super(latitude, longitude);
		this.time = time;
		this.err = error;
	}

	PositionLogItem()
	{

	}

	public Date getTime()
	{
		return time;
	}

	public void setTime(final Date time)
	{
		this.time = time;
	}

	public float getError()
	{
		return err;
	}

	public void setError(float err)
	{
		this.err = err;
	}
}
