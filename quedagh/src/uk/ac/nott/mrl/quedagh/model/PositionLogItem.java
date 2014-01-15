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
		err = error;
	}

	public PositionLogItem(final float latitude, final float longitude, final long time, final float error)
	{
		super(latitude, longitude);
		this.time = time;
		err = error;
	}

	PositionLogItem()
	{

	}

	public float getError()
	{
		return err;
	}

	public long getTime()
	{
		return time;
	}

	public void setError(final float err)
	{
		this.err = err;
	}

	public void setTime(final long time)
	{
		this.time = time;
	}
}
