package uk.ac.nott.mrl.quedagh.model;

import java.util.Date;

import com.googlecode.objectify.annotation.Embed;

@Embed
public class GameEvent extends Message
{
	private long time;
	private String state;
	private String stage;

	public GameEvent()
	{
		time = new Date().getTime();
	}

	public GameEvent(final long time, final String state, final String stage, final Message message)
	{
		this.time = time;
		if (message != null)
		{
			setText(message.getText());
			setResponse(message.getResponse());
			setAdmin(message.isAdmin());
		}
		this.state = state;
		this.stage = stage;
	}

	public String getState()
	{
		return state;
	}

	public long getTime()
	{
		return time;
	}

	public String getStage()
	{
		return stage;
	}
	
	public void setStage(final String stage)
	{
		this.stage = stage;
	}
	
	public void setState(final String state)
	{
		this.state = state;
	}

	public void setTime(final long time)
	{
		this.time = time;
	}
}
