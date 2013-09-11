package uk.ac.nott.mrl.quedagh.client.views;

import java.util.Date;

import org.wornchaos.client.server.AsyncCallback;
import org.wornchaos.views.View;

import uk.ac.nott.mrl.quedagh.client.Quedagh;
import uk.ac.nott.mrl.quedagh.model.Game;
import uk.ac.nott.mrl.quedagh.model.Message;
import uk.ac.nott.mrl.quedagh.model.Team;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

public class TeamInfoView extends Composite implements View<Team>
{
	interface TeamInfoUiBinder extends UiBinder<Widget, TeamInfoView>
	{
	}

	private static DateTimeFormat yearFormat = DateTimeFormat.getFormat("d MMM yyyy");

	private static DateTimeFormat monthFormat = DateTimeFormat.getFormat("d MMM");

	private static final TeamInfoUiBinder uiBinder = GWT.create(TeamInfoUiBinder.class);

	public static Widget getMessageWidget(final String device, final Message message)
	{
		if (message == null) { return null; }
		if (message.getResponse() == null) { return new Label(message.getText()); }
		final Button button = new Button(message.getText());
		button.addClickHandler(new ClickHandler()
		{
			@Override
			public void onClick(final ClickEvent event)
			{
				Quedagh.getServer().respond(device, message.getResponse(), new AsyncCallback<Game>()
				{

					@Override
					public void onSuccess(final Game arg0)
					{

					}
				});
			}
		});
		return button;
	}

	@SuppressWarnings("deprecation")
	private static String getRelativeTime(final long time)
	{
		if (time == 0) { return "Never"; }

		final Date now = new Date();
		final Date date = new Date(time);
		if (date.getYear() != now.getYear())
		{
			return yearFormat.format(date);
		}
		else if (date.getMonth() != now.getMonth() || date.getDate() != now.getDate())
		{
			return monthFormat.format(date);
		}
		else
		{
			final int hours = now.getHours() - date.getHours();
			if (hours != 0)
			{
				return hours + " hours ago";
			}
			else
			{
				final int minutes = now.getMinutes() - date.getMinutes();
				if (minutes != 0)
				{
					return minutes + " minutes ago";
				}
				else
				{
					return "just now";
				}
			}
		}
	}

	@UiField
	Label name;

	@UiField
	Image marker;

	@UiField
	Panel messages;

	@UiField
	Label update;

	public TeamInfoView()
	{
		super();
		initWidget(uiBinder.createAndBindUi(this));
	}

	@Override
	public void itemChanged(final Team team)
	{
		name.setText("Team " + team.getValue());
		if (team.getColour() != null)
		{
			marker.setUrl("/images/markers/bullet_" + team.getColour().name() + ".png");
		}
		else
		{
			marker.setUrl("/images/markers/bullet_grey.png");
		}

		messages.clear();
		for (final Message message : team.getMessages())
		{
			messages.add(getMessageWidget(team.getDevice(), message));
		}

		if (team.getLastKnown() != null)
		{
			update.setText("Last Update: " + getRelativeTime(team.getLastKnown().getTime()));
		}
	}
}