package uk.ac.nott.mrl.quedagh.model;

import com.googlecode.objectify.annotation.Embed;

@Embed
public class Swap
{
	private int value1;
	private int value2;

	public Swap(final int value1, final int value2)
	{
		this.value1 = value1;
		this.value2 = value2;
	}

	Swap()
	{

	}

	public int getValue1()
	{
		return value1;
	}

	public int getValue2()
	{
		return value2;
	}

	@Override
	public String toString()
	{
		return Game.str(value1) + "<->" + Game.str(value2);
	}
}
