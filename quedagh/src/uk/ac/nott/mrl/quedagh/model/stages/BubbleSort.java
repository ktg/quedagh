package uk.ac.nott.mrl.quedagh.model.stages;

import uk.ac.nott.mrl.quedagh.model.Game;
import uk.ac.nott.mrl.quedagh.model.Marker;

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
	protected String findNextSwap(final Game game)
	{
		if (sortLength == 0)
		{
			sortLength = game.getMarkers().size() - 1;
		}

		while (sortLength > 1)
		{
			for (; index < sortLength; index++)
			{
				incComparisons();
				final Marker marker1 = game.getMarkers().get(index);
				final Marker marker2 = game.getMarkers().get(index + 1);
				if (marker1 != null && marker1.getValue() > marker2.getValue())
				{
					final int value1 = marker1.getValue();
					marker1.setValue(marker2.getValue());
					marker2.setValue(value1);
					return str(marker2.getValue()) + "<->" + str(marker1.getValue());
				}
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