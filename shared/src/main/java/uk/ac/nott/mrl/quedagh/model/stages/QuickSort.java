package uk.ac.nott.mrl.quedagh.model.stages;

import uk.ac.nott.mrl.quedagh.model.Game;

import com.googlecode.objectify.annotation.EntitySubclass;

@EntitySubclass
public class QuickSort extends Sort
{
	public QuickSort(final String id, final Stage next)
	{
		super(id, next);
	}

	QuickSort()
	{
		super();
	}

	// private int partition(List<Marker> array, int left, int right, int pivotIndex)
	// {
	// int pivotValue = array.get(pivotIndex);
	// swap array[pivotIndex] and array[right] // Move pivot to end
	// storeIndex := left
	// for i from left to right - 1 // left ≤ i < right
	// if array[i] < pivotValue
	// swap array[i] and array[storeIndex]
	// storeIndex := storeIndex + 1
	// swap array[storeIndex] and array[right] // Move pivot to its final place
	// return storeIndex;
	// return 0;
	// }

	// @Override
	// protected Swap findNextSwap(final Game game)
	// {
	// if (sortLength == 0)
	// {
	// sortLength = game.getMarkers().size() - 1;
	// }
	//
	// Log.info("" + sortLength + "," + index);
	//
	// while (sortLength > 0)
	// {
	// while (index < sortLength)
	// {
	// incComparisons();
	// final Marker marker1 = game.getMarkers().get(index);
	// final Marker marker2 = game.getMarkers().get(index + 1);
	// if (comparator.compare(marker1, marker2) > 0)
	// {
	// final int value1 = marker1.getValue();
	// marker1.setValue(marker2.getValue());
	// marker2.setValue(value1);
	// return new Swap(marker2.getValue(), marker1.getValue());
	// }
	// index++;
	// }
	// index = 0;
	// sortLength--;
	// }
	// return null;
	// }

	@Override
	protected String getSortName()
	{
		return "Bubble";
	}

	@Override
	protected String getStep()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void nextStep(final Game game)
	{
		// TODO Auto-generated method stub
	}
}