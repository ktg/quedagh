package uk.ac.nott.mrl.quedagh.client.pages;

import org.wornchaos.client.logger.Log;
import org.wornchaos.client.ui.Page;
import org.wornchaos.client.ui.ViewCallback;
import org.wornchaos.views.View;

import uk.ac.nott.mrl.quedagh.client.Quedagh;
import uk.ac.nott.mrl.quedagh.model.Game;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

public class GamesView extends Page implements View<Iterable<Game>> 
{
	interface GamesUiBinder extends UiBinder<Widget, GamesView>
	{
	}
	
	private static final GamesUiBinder uiBinder = GWT.create(GamesUiBinder.class);
	
	@UiField
	Panel games;
	
	@Override
	protected Widget createView()
	{
		final Widget view = uiBinder.createAndBindUi(this);

		Quedagh.getServer().getGames(new ViewCallback<Iterable<Game>>(this));
		
		return view;
	}

	@Override
	public void itemChanged(Iterable<Game> item)
	{
		Log.info("Games View");
		games.clear();
		for(Game game: item)
		{
			games.add(new Hyperlink(game.getId(), Quedagh.getToken(new uk.ac.nott.mrl.quedagh.client.places.Game(game.getId()))));
		}
			
	}

	@UiHandler("create")
	void createGame(ClickEvent event)
	{
		
	}
}

