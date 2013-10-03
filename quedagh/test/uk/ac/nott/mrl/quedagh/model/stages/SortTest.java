package uk.ac.nott.mrl.quedagh.model.stages;

import org.junit.Test;
import org.wornchaos.logger.Log;

import uk.ac.nott.mrl.quedagh.model.Game;
import uk.ac.nott.mrl.quedagh.model.Marker;
import uk.ac.nott.mrl.quedagh.model.Team.Colour;

public class SortTest
{

	@Test
	public void testBubbleSort() throws Exception
	{
		testSort(new BubbleSort());
	}
	
	private void testSort(Sort sort) throws Exception
	{
		final Marker marker1 = new Marker(Colour.blue, 0, 0);
		marker1.setValue(4);
		final Marker marker2 = new Marker(Colour.blue, 0, 0);
		marker2.setValue(3);
		final Marker marker3 = new Marker(Colour.blue, 0, 0);
		marker3.setValue(5);
		final Marker marker4 = new Marker(Colour.blue, 0, 0);
		marker4.setValue(2);
		final Marker marker5 = new Marker(Colour.blue, 0, 0);
		marker5.setValue(1);		

		final Game game = new Game();
		game.getMarkers().add(marker1);
		game.getMarkers().add(marker2);
		game.getMarkers().add(marker3);
		game.getMarkers().add(marker4);
		game.getMarkers().add(marker5);

		while (sort.hasNextSwap(game))
		{
			sort.swap(game);
			Log.info(sort.getClass().getSimpleName() + " " + game.getOrder());
			Log.info(sort.getComparisons() + " comparisons, " + sort.getSwaps() + " swaps");
		}

		Log.info(game.getOrder());
		if(!game.getOrder().equals("ABCDE"))
		{
			throw new Exception();
		}
	}

	@Test
	public void testSelectionSort() throws Exception
	{
		testSort(new SelectionSort());
	}
}
