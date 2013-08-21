package uk.ac.nott.mrl.quedagh.activities;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;

import org.wornchaos.client.server.AsyncCallback;
import org.wornchaos.server.JSONServerHandler;

import uk.ac.nott.mrl.quedagh.R;
import uk.ac.nott.mrl.quedagh.model.Game;
import uk.ac.nott.mrl.quedagh.model.Message;
import uk.ac.nott.mrl.quedagh.model.PositionLogItem;
import uk.ac.nott.mrl.quedagh.model.QuedaghServer;
import uk.ac.nott.mrl.quedagh.model.Team;
import uk.ac.nott.mrl.quedagh.view.TrailView;
import android.app.Activity;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;

public class TresureHunt extends Activity implements LocationListener, ConnectionCallbacks, OnConnectionFailedListener
{
	private GoogleMap map;
	private boolean tracking = true;

	private LinearLayout messagePanel;

	private TrailView trailView;

	private Collection<PositionLogItem> log = new ArrayList<PositionLogItem>();

	private String deviceID;
	
	private final Timer timer = new Timer();

	private QuedaghServer server;

	// Milliseconds per second
	private static final int MILLISECONDS_PER_SECOND = 1000;
	// Update frequency in seconds
	public static final int UPDATE_INTERVAL_IN_SECONDS = 5;
	// Update frequency in milliseconds
	private static final long UPDATE_INTERVAL = MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
	// The fastest update frequency, in seconds
	private static final int FASTEST_INTERVAL_IN_SECONDS = 1;
	// A fast frequency ceiling in milliseconds
	private static final long FASTEST_INTERVAL = MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;

	// private final String android_id = Secure.getString(getContentResolver(), Secure.ANDROID_ID);

	// Define an object that holds accuracy and frequency parameters
	LocationRequest locationRequest;
	LocationClient locationClient;

	/*
	 * Called by Location Services when the request to connect the client finishes successfully. At
	 * this point, you can request the current location or start periodic updates
	 */
	@Override
	public void onConnected(final Bundle dataBundle)
	{
		locationClient.requestLocationUpdates(locationRequest, this);
	}

	/*
	 * Called by Location Services if the attempt to Location Services fails.
	 */
	@Override
	public void onConnectionFailed(final ConnectionResult connectionResult)
	{
		/*
		 * Google Play services can resolve some errors it detects. If the error has a resolution,
		 * try sending an Intent to start a Google Play services activity that can resolve error.
		 */
		if (connectionResult.hasResolution())
		{
			try
			{
				// Start an Activity that tries to resolve the error
				connectionResult.startResolutionForResult(this, connectionResult.getErrorCode());
				/*
				 * Thrown if Google Play services canceled the original PendingIntent
				 */
			}
			catch (final IntentSender.SendIntentException e)
			{
				// Log the error
				e.printStackTrace();
			}
		}
		else
		{
			/*
			 * If no resolution is available, display a dialog to the user with the error.
			 */
			// showErrorDialog(connectionResult.getErrorCode());
		}
	}

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		messagePanel = (LinearLayout) findViewById(R.id.messagePanel);

		deviceID = Secure.getString(getContentResolver(), Secure.ANDROID_ID);
		
		server = (QuedaghServer) Proxy.newProxyInstance(QuedaghServer.class.getClassLoader(),
														new Class[] { QuedaghServer.class }, new JSONServerHandler());
		server.setHostURL("http://quedaghs.appspot.com/");

		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
		map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		map.getUiSettings().setCompassEnabled(true);
		map.getUiSettings().setRotateGesturesEnabled(false);
		map.getUiSettings().setScrollGesturesEnabled(false);
		map.getUiSettings().setTiltGesturesEnabled(false);
		map.getUiSettings().setZoomControlsEnabled(false);
		map.getUiSettings().setZoomGesturesEnabled(false);
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(52.90890204, -1.23046875), 18));

		trailView = (TrailView) findViewById(R.id.trailView);
		trailView.setDeviceID(deviceID);
		trailView.setMap(map);
		trailView.setPackageName(getPackageName());

		locationRequest = LocationRequest.create();
		locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		locationRequest.setInterval(UPDATE_INTERVAL);
		locationRequest.setFastestInterval(FASTEST_INTERVAL);

		locationClient = new LocationClient(this, this, this);

		timer.scheduleAtFixedRate(new TimerTask()
		{
			@Override
			public void run()
			{
				try
				{
					if (log.isEmpty()) { return; }
					
					final Collection<PositionLogItem> updates = log;
					log = new ArrayList<PositionLogItem>();

					server.update(deviceID, updates, new AsyncCallback<Game>()
					{
						@Override
						public void onSuccess(final Game game)
						{
							trailView.setGame(game);
							runOnUiThread(new Runnable()
							{
								@Override
								public void run()
								{
									handleUpdate(game);
								}
							});
						}
					});
				}
				catch (final Exception e)
				{
					Log.e("SendLog", e.getMessage(), e);
				}
			}
		}, 0, 5000);
	}

	/*
	 * Called by Location Services if the connection to the location client drops because of an
	 * error.
	 */
	@Override
	public void onDisconnected()
	{
		// Display the connection status
		Toast.makeText(this, "Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onLocationChanged(final Location location)
	{
		if (tracking)
		{
			map.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
		}

		final PositionLogItem position = new PositionLogItem((float) location.getLatitude(),
				(float) location.getLongitude(), location.getAccuracy());
		log.add(position);
		trailView.addExplored(position);
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		locationClient.connect();
	}

	@Override
	protected void onStop()
	{
		timer.cancel();

		// If the client is connected
		if (locationClient.isConnected())
		{
			/*
			 * Remove location updates for a listener. The current Activity is the listener, so the
			 * argument is "this".
			 */
			locationClient.removeLocationUpdates(this);
		}
		/*
		 * After disconnect() is called, the client is considered "dead".
		 */
		locationClient.disconnect();
		super.onStop();
	}

	private void handleUpdate(final Game update)
	{
		Team team = update.getTeam(deviceID);
		if(team == null)
		{
			messagePanel.setVisibility(View.VISIBLE);
			messagePanel.removeAllViewsInLayout();
			
			// TODO
		}
		else if (team.getMessages().isEmpty())
		{
			messagePanel.setVisibility(View.INVISIBLE);
		}
		else
		{
			messagePanel.setVisibility(View.VISIBLE);
			messagePanel.removeAllViewsInLayout();

			for (final Message message : team.getMessages())
			{
				if (message.getResponse() != null)
				{
					final Button button = new Button(getApplicationContext());
					button.setText(message.getText());
					button.setTextSize(20);
					button.setTextColor(0xFF000000);
					button.setOnClickListener(new OnClickListener()
					{
						@Override
						public void onClick(final View v)
						{
							responseClick(message);
						}
					});

					messagePanel.addView(button);
				}
				else
				{

					final TextView text = new TextView(getApplicationContext());
					text.setTextColor(0xFF000000);
					text.setTextSize(20);
					text.setGravity(Gravity.CENTER);
					text.setText(message.getText());

					messagePanel.addView(text);
				}
			}
		}
		trailView.invalidate();
	}

	private void responseClick(final Message message)
	{
		final LinearLayout messagePanel = (LinearLayout) findViewById(R.id.messagePanel);
		messagePanel.setVisibility(View.INVISIBLE);
		server.respond(	Secure.getString(getContentResolver(), Secure.ANDROID_ID), message.getResponse(),
						new AsyncCallback<Game>()
						{

							@Override
							public void onSuccess(final Game arg0)
							{
								// TODO Auto-generated method stub

							}
						});
	}
}