package uk.ac.nott.mrl.quedagh.model.stages;

import uk.ac.nott.mrl.quedagh.model.Game;
import uk.ac.nott.mrl.quedagh.model.Marker;
import uk.ac.nott.mrl.quedagh.model.Swap;

import com.googlecode.objectify.annotation.EntitySubclass;

@EntitySubclass
public class BubbleSort extends Sort
{
	private int index = 0;
	private int sortLength = 0;

	public BubbleSort(final String id, final Stage next)
	{
		super(id, next);
	}

	BubbleSort()
	{
		super();
	}

	@Override
	protected Swap findNextSwap(final Game game)
	{
		if (sortLength == 0)
		{
			sortLength = game.getMarkers().size() - 1;
		}
			
		while (sortLength > 0)
		{
			while(index < sortLength)
			{
				incComparisons();
				final Marker marker1 = game.getMarkers().get(index);
				final Marker marker2 = game.getMarkers().get(index + 1);
				if (comparator.compare(marker1, marker2) > 0)
				{
					return new Swap(marker1.getValue(), marker2.getValue()); 
				}
				index++;				
			}
			index = 0;
			sortLength--;
		}
		return null;
	}

	@Override
	protected String getSortName()
	{
		return "Bubble";
	}
}