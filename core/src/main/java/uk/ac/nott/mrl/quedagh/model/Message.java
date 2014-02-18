package uk.ac.nott.mrl.quedagh.model;

import com.googlecode.objectify.annotation.Embed;

@Embed
public class Message
{
	private String text;
	private String response;
	private boolean admin = false;

	public Message()
	{

	}

	public Message(final String text)
	{
		this.text = text;
	}

	public Message(final String text, final String response)
	{
		this.text = text;
		this.response = response;
	}
	
	public Message(final String text, final String response, final boolean admin)
	{
		this.text = text;
		this.response = response;
		this.setAdmin(admin);
	}	

	public String getResponse()
	{
		return response;
	}

	public String getText()
	{
		return text;
	}

	public void setResponse(final String response)
	{
		this.response = response;
	}

	public void setText(final String text)
	{
		this.text = text;
	}

	public boolean isAdmin()
	{
		return admin;
	}

	public void setAdmin(boolean admin)
	{
		this.admin = admin;
	}
}