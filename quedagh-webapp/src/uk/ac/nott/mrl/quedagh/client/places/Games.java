package uk.ac.nott.mrl.quedagh.client.places;

import org.wornchaos.client.places.SimplePlace;

import uk.ac.nott.mrl.quedagh.client.pages.GamesView;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class Games extends SimplePlace
{
	@Prefix("games")
	public static class Tokenizer implements PlaceTokenizer<Games>
	{
		@Override
		public Games getPlace(final String token)
		{
			return new Games();
		}

		@Override
		public String getToken(final Games place)
		{
			return "";
		}
	}

	@Override
	public Activity createActivity()
	{
		return new GamesView();
	}
}
