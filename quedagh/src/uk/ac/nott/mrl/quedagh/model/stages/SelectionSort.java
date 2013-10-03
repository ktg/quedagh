package uk.ac.nott.mrl.quedagh.model.stages;

import uk.ac.nott.mrl.quedagh.model.Game;
import uk.ac.nott.mrl.quedagh.model.Marker;
import uk.ac.nott.mrl.quedagh.model.Swap;

import com.googlecode.objectify.annotation.EntitySubclass;

@EntitySubclass
public class SelectionSort extends Sort
{
	private int sortLength = 0;

	public SelectionSort(final String id, final Stage next)
	{
		super(id, next);
	}

	SelectionSort()
	{
		super();
	}

	@Override
	protected Swap findNextSwap(final Game game)
	{
		int index = 1;
		int highIndex = 0;
		Marker highest = game.getMarkers().get(0);
		if (sortLength == 0)
		{
			sortLength = game.getMarkers().size();
		}
		if (sortLength == 1) { return null; }

		for (; index < sortLength; index++)
		{
			incComparisons();
			final Marker marker = game.getMarkers().get(index);
			if (comparator.compare(marker, highest) > 0 )
			{
				highest = marker;
				highIndex = index;
			}
		}

		sortLength--;
		final int swapIndex = sortLength;
		if (swapIndex == highIndex) { return findNextSwap(game); }

		final Marker swapMarker = game.getMarkers().get(swapIndex);
		return new Swap(swapMarker.getValue(), highest.getValue());
	}

	@Override
	protected String getSortName()
	{
		return "Selection";
	}
}