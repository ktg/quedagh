package uk.ac.nott.mrl.quedagh.model;

import java.util.Date;

import com.googlecode.objectify.annotation.Embed;

@Embed
public class GameEvent extends Message
{
	private long time;
	private String order;

	public GameEvent()
	{
		time = new Date().getTime();
	}

	public GameEvent(final long time, final String order, final Message message)
	{
		this.time = time;
		if(message != null)
		{
			setText(message.getText());
			setResponse(message.getResponse());
		}
		this.order = order;
	}

	public String getOrder()
	{
		return order;
	}

	public long getTime()
	{
		return time;
	}

	public void setOrder(final String order)
	{
		this.order = order;
	}

	public void setTime(final long time)
	{
		this.time = time;
	}
}
