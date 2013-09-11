package uk.ac.nott.mrl.quedagh.model;

import java.util.Date;

import com.googlecode.objectify.annotation.Embed;

@Embed
public class PositionLogItem extends Position
{
	private long time;
	private float err;

	public PositionLogItem(final float latitude, final float longitude, final float error)
	{
		super(latitude, longitude);
		time = new Date().getTime();
		this.err = error;
	}

	public PositionLogItem(final float latitude, final float longitude, final long time, final float error)
	{
		super(latitude, longitude);
		this.time = time;
		this.err = error;
	}

	PositionLogItem()
	{

	}

	public long getTime()
	{
		return time;
	}

	public void setTime(final long time)
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
