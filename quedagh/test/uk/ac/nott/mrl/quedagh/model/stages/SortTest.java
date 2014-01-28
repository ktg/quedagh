package uk.ac.nott.mrl.quedagh.model.stages;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;

import org.junit.Test;
import org.wornchaos.logger.Log;
import org.wornchaos.logger.LogFormatter;

import uk.ac.nott.mrl.quedagh.model.Game;
import uk.ac.nott.mrl.quedagh.model.Marker;
import uk.ac.nott.mrl.quedagh.model.Team.Colour;

public class SortTest
{
	@Test
	public void testBubbleSort() throws Exception
	{
		ConsoleHandler chandler = new ConsoleHandler();
		chandler.setFormatter(new LogFormatter());
		Logger.getLogger("wornchaos").addHandler(chandler);
		
	
		for(Handler handler: Logger.getLogger("wornchaos").getHandlers())
		{
			System.out.println(handler.getClass().getName());			
			System.out.println(handler.getFormatter().getClass().getName());
		}
		
		testSort(new BubbleSort());
	}
	
	private void testSort(Sort sort) throws Exception
	{
		final Marker marker1 = new Marker(Colour.blue, 0, 0);
		final Marker marker2 = new Marker(Colour.blue, 0, 0);
		final Marker marker3 = new Marker(Colour.blue, 0, 0);
		final Marker marker4 = new Marker(Colour.blue, 0, 0);
		final Marker marker5 = new Marker(Colour.blue, 0, 0);	

		final Game game = new Game();
		game.setState("CEDAB");
		game.getMarkers().add(marker1);
		game.getMarkers().add(marker2);
		game.getMarkers().add(marker3);
		game.getMarkers().add(marker4);
		game.getMarkers().add(marker5);

		while (!game.isInOrder())
		{
			sort.nextStep(game);			
			Log.info(sort.getClass().getSimpleName() + " " + game.getState());
			Log.info(sort.getStep());			
			Log.info(sort.getComparisons() + " comparisons, " + sort.getSwaps() + " swaps");
		}

		//Log.info(game.getState());
		if(!game.getState().equals("ABCDE"))
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
