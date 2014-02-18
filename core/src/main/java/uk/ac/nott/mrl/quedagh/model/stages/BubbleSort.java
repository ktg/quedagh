package uk.ac.nott.mrl.quedagh.model.stages;

import org.wornchaos.logger.Log;

import uk.ac.nott.mrl.quedagh.model.Game;
import uk.ac.nott.mrl.quedagh.model.Message;

import com.googlecode.objectify.annotation.EntitySubclass;

@EntitySubclass
public class BubbleSort extends Sort
{
	public BubbleSort(final String id, final Stage next)
	{
		super(id, next);
	}

	BubbleSort()
	{
		super();
	}

	@Override
	public String getStep()
	{
		if (compare == null) { return "Returning"; }
		if (compare.length() == 2) { return "Comparing " + compare.charAt(0) + " & " + compare.charAt(1); }
		return "Returning";
	}

	@Override
	protected String getSortName()
	{
		return "Bubble";
	}

	@Override
	protected void nextStep(final Game game)
	{
		if (order == null)
		{
			order = game.getState();
			sortLength = order.length() - 1;
			comparisons = -1;
			index = 1;
		}
	
		if (compare == null || compare.length() == 0 || compare.length() == 1)
		{
			if(!order.equals(game.getState()))
			{
				game.setState(order);
				swaps++;
			}

			compare = "" + order.charAt(index - 1) + order.charAt(index);

			Log.info("Comparing " + order.charAt(index - 1) + " and " + order.charAt(index) + ". Index = " + index + ". Sort Length = " + sortLength);
			
			updateTeamMessages(game, new Message("Go to the center flag"));

			comparisons++;
		}
		// Comparing
		else if (compare.length() == 2)
		{
			final char value1 = order.charAt(index - 1);
			final char value2 = order.charAt(index);

			if (index >= sortLength)
			{
				updateTeamMessages(game, new Message("Lowest value go to point " + index
						+ ". Highest value go to point " + (index + 1)));
			}
			else
			{
				updateTeamMessages(game, new Message("Lowest value go to point " + index
						+ ". Highest value remain in the center"));
			}

			if (value1 > value2)
			{
				compare = "" + value1;
				final StringBuilder newOrder = new StringBuilder(order);
				newOrder.setCharAt(index - 1, value2);
				newOrder.setCharAt(index, value1);

				Log.info("Swapping " + value1 + " and " + value2);
				
				order = newOrder.toString();
			}
			else
			{
				compare = "" + value2;
			}

			index++;
			if (index > sortLength)
			{
				index = 1;
				sortLength--;
				compare = null;
				
				Log.info("Sort Length = " + sortLength);
			}
		}
		else
		{
			Log.error("Er... how did we get here?");
		}
	}
}