package uk.ac.nott.mrl.quedagh.model;

import com.googlecode.objectify.annotation.Embed;

@Embed
public class Event
{
	private String event;
	private long date;

	Event()
	{
		
	}
	
	public Event(final String event, final long date)
	{
		super();
		this.event = event;
		this.date = date;
	}

	public long getDate()
	{
		return date;
	}

	public String getEvent()
	{
		return event;
	}

	public void setDate(final long date)
	{
		this.date = date;
	}

	public void setEvent(final String event)
	{
		this.event = event;
	}
}
