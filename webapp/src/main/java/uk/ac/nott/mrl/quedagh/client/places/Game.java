package uk.ac.nott.mrl.quedagh.client.places;

import org.wornchaos.client.places.SimplePlace;

import uk.ac.nott.mrl.quedagh.client.pages.GameView;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class Game extends SimplePlace
{
	@Prefix("game")
	public static class Tokenizer implements PlaceTokenizer<Game>
	{
		@Override
		public Game getPlace(final String token)
		{
			return new Game(token);
		}

		@Override
		public String getToken(final Game place)
		{
			return place.id;
		}
	}
	
	private String id;
	
	public Game(final String id)
	{
		this.id = id;
	}
	
	@Override
	public Activity createActivity()
	{
		return new GameView(id);
	}
}
