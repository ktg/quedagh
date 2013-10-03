package uk.ac.nott.mrl.quedagh.activities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import uk.ac.nott.mrl.quedagh.model.Game;
import uk.ac.nott.mrl.quedagh.model.PositionLogItem;
import uk.ac.nott.mrl.quedagh.model.Team;
import android.location.Location;

public class PositionManager
{
	private static final int TWO_MINUTES = 1000 * 60 * 2;

	private String deviceID;
	
	private Game game;
	private Location currentBestLocation;
	private PositionLogItem currentBestPosition;
	
	private List<PositionLogItem> current = new ArrayList<PositionLogItem>();
	private List<PositionLogItem> pending;

	public boolean addLocation(final Location location)
	{
		if (isBetterLocation(location))
		{
			final PositionLogItem position = new PositionLogItem((float) location.getLatitude(),
					(float) location.getLongitude(), location.getTime(), location.getAccuracy());
			current.add(position);
			currentBestLocation = location;
			currentBestPosition = position;
			updateLastKnown();
			return true;
		}
		return false;
	}
	
	public long getLastModified()
	{
		if(game == null)
		{
			return 0;
		}
		return game.getModified();
	}

	public void setDeviceID(final String deviceID)
	{
		this.deviceID = deviceID;
		currentBestLocation = null;
		currentBestPosition = null;
	}
	
	public String getDeviceID()
	
	{
		return deviceID;
	}
	
	/**
	 * Determines whether one Location reading is better than the current Location fix
	 * 
	 * @param location
	 *            The new Location that you want to evaluate
	 * @param currentBestLocation
	 *            The current Location fix, to which you want to compare the new one
	 */
	private boolean isBetterLocation(final Location location)
	{
		if (currentBestLocation == null)
		{
			// A new location is always better than no location
			return true;
		}

		// Check whether the new location fix is newer or older
		final long timeDelta = location.getTime() - currentBestLocation.getTime();
		final boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
		final boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
		final boolean isNewer = timeDelta > 0;

		// If it's been more than two minutes since the current location, use the new location
		// because the user has likely moved
		if (isSignificantlyNewer)
		{
			return true;
			// If the new location is more than two minutes older, it must be worse
		}
		else if (isSignificantlyOlder) { return false; }

		// Check whether the new location fix is more or less accurate
		final int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
		final boolean isLessAccurate = accuracyDelta > 0;
		final boolean isMoreAccurate = accuracyDelta < 0;
		final boolean isSignificantlyLessAccurate = accuracyDelta > 200;

		// Check if the old and new location are from the same provider
		final boolean isFromSameProvider = isSameProvider(location.getProvider(), currentBestLocation.getProvider());

		// Determine location quality using a combination of timeliness and accuracy
		if (isMoreAccurate)
		{
			return true;
		}
		else if (isNewer && !isLessAccurate)
		{
			return true;
		}
		else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) { return true; }
		return false;
	}
	
	public Location getLocation()
	{
		return currentBestLocation;
	}
	
	public Collection<PositionLogItem> getCurrent()
	{
		return current;
	}
	
	public Collection<PositionLogItem> getPending()
	{
		return pending;
	}
	
	public Collection<PositionLogItem> getUpdates()
	{
		if(pending != null)
		{
			return null;
		}
		
		pending = current;
		current = new ArrayList<PositionLogItem>();
		
		return pending;
	}
	
	public Game getGame()
	{
		return game;
	}
	
	/** Checks whether two providers are the same */
	private boolean isSameProvider(final String provider1, final String provider2)
	{
		if (provider1 == null) { return provider2 == null; }
		return provider1.equals(provider2);
	}

	public void setGame(Game game)
	{
		pending = null;
		this.game = game;
		updateLastKnown();
	}
	
	private void updateLastKnown()
	{
		if(game != null && currentBestPosition != null)
		{
			Team team = game.getTeam(deviceID);
			if(team != null)
			{
				team.setLastKnown(currentBestPosition);
			}
		}
	}

	public void updateFailed()
	{
		pending.addAll(current);
		current = pending;
		pending = null;		
	}

	public void clearPending()
	{
		pending = null;		
	}

	public String getGameID()
	{
		if(game == null)
		{
			return null;
		}
		return game.getId();
	}
}
