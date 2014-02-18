package uk.ac.nott.mrl.quedagh.model.stages;

import org.wornchaos.logger.Log;

import uk.ac.nott.mrl.quedagh.model.Game;
import uk.ac.nott.mrl.quedagh.model.Message;

import com.googlecode.objectify.annotation.EntitySubclass;

@EntitySubclass
public class SelectionSort extends Sort
{
	private int highIndex = 0;

	public SelectionSort(final String id, final Stage next)
	{
		super(id, next);
	}

	SelectionSort()
	{
		super();
	}

	@Override
	public String getStep()
	{
		if (compare == null || compare.length() == 0)
		{
			return "Swapping";
		}
		else if (compare.length() == 2) { return "Comparing " + compare.charAt(0) + " & " + compare.charAt(1); }

		return "Returning";
	}

	@Override
	protected String getSortName()
	{
		return "Selection";
	}

	@Override
	protected void nextStep(final Game game)
	{
		// Finish Swapping
		if (order == null)
		{
			order = game.getState();
			sortLength = order.length();
			comparisons = -1;
		}

		if (compare == null || compare.length() == 0)
		{
			if (!game.getState().equals(order))
			{
				game.setState(order);
				swaps++;
			}
			comparisons++;

			highIndex = 0;
			index = 1;
			compare = "" + order.charAt(highIndex) + order.charAt(index);
			
			Log.info("Comparing " + order.charAt(highIndex) + " and " + order.charAt(index) + ". Index = " + index + ". Sort Length = " + sortLength);
			
			updateTeamMessages(game, new Message("Go to the center"));
		}
		// Finished Compare
		else if (compare.length() == 1)
		{
			compare = "" + order.charAt(highIndex) + order.charAt(index);

			Log.info("Comparing " + order.charAt(highIndex) + " and " + order.charAt(index) + ". Index = " + index + ". Sort Length = " + sortLength);
			
			updateTeamMessages(game, new Message("Go to the center"));

			comparisons++;
		}
		// Comparing
		else if (compare.length() == 2)
		{
			char high = order.charAt(highIndex);
			final char value = order.charAt(index);

			if (value > high)
			{
				highIndex = index;
			}

			index++;
			if (index >= sortLength)
			{
				sortLength--;

				high = order.charAt(highIndex);
				final char swap = order.charAt(sortLength);

				if (swap == high)
				{
					updateTeamMessages(game, new Message("Return to where you came from"));
				}
				else
				{
					final StringBuilder newOrder = new StringBuilder(order);
					newOrder.setCharAt(sortLength, high);
					newOrder.setCharAt(highIndex, swap);

					order = newOrder.toString();

					updateTeamMessages(game, new Message("Highest value go to point " + (sortLength + 1)
							+ ". Lowest value go to " + (highIndex + 1) + "."));
				}
				compare = null;
			}
			else
			{
				updateTeamMessages(game, new Message("Lowest value, return to where you came from."));
				compare = "" + order.charAt(highIndex);
			}
		}
		else
		{
			Log.error("Er... how did we get here?");
		}
	}

	protected void setup(final Game game)
	{
		highIndex = 0;
		index = 1;
		sortLength = game.getMarkers().size();
		order = game.getState();
		compare = "" + order.charAt(highIndex) + order.charAt(index);
	}
}