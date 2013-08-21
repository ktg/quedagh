package uk.ac.nott.mrl.quedagh.client;

import java.util.ArrayList;

import org.wornchaos.client.places.PlaceEntryPoint;

import uk.ac.nott.mrl.quedagh.client.places.Game;
import uk.ac.nott.mrl.quedagh.client.places.Games;
import uk.ac.nott.mrl.quedagh.model.QuedaghServer;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.maps.client.LoadApi;
import com.google.gwt.maps.client.LoadApi.LoadLibrary;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.place.shared.WithTokenizers;

public class Quedagh extends PlaceEntryPoint
{
	@WithTokenizers({ Games.Tokenizer.class, Game.Tokenizer.class})
	public static interface QuedaghHistoryMapper extends PlaceHistoryMapper
	{
	}

	private static final QuedaghServer server = GWT.create(QuedaghServer.class);

	public static QuedaghServer getServer()
	{
		return server;
	}

	@Override
	public void onModuleLoad()
	{
		// Move to activity?
		final ArrayList<LoadLibrary> loadLibraries = new ArrayList<LoadApi.LoadLibrary>();
		loadLibraries.add(LoadLibrary.DRAWING);
		loadLibraries.add(LoadLibrary.GEOMETRY);

		final Runnable onLoad = new Runnable()
		{
			@Override
			public void run()
			{
				Quedagh.super.onModuleLoad();
			}
		};

		final boolean sensor = true;
		LoadApi.go(onLoad, loadLibraries, sensor);	
	}

	@Override
	protected Place getDefaultPlace()
	{
		return new Games();
	}

	@Override
	protected PlaceHistoryMapper getHistoryMapper()
	{
		return GWT.create(QuedaghHistoryMapper.class);
	}
}