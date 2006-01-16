/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.launchConfigurations;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationListener;
import org.eclipse.debug.core.ILaunchListener;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.ILaunchGroup;
import org.eclipse.ui.activities.WorkbenchActivityHelper;

/**
 * A history of launches and favorites for a launch group
 */
public class LaunchHistory implements ILaunchListener, ILaunchConfigurationListener {

	private ILaunchGroup fGroup;
	
	private List fHistory = new ArrayList();
	private List fFavorites = new ArrayList();
	private boolean fDirty = false;
	private ILaunchConfiguration fRecentLaunch;
	
	private static List launchHistoryInstances= new ArrayList();
	
	/**
	 * Creates a new launch history for the given launch group
	 */
	public LaunchHistory(ILaunchGroup group) {
		fGroup = group;
		ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager(); 
		manager.addLaunchListener(this);
		manager.addLaunchConfigurationListener(this);
		launchHistoryInstances.add(this);
	}
	
	/**
	 * Disposes this history
	 */
	public void dispose() {
		ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
		manager.removeLaunchListener(this);
		manager.removeLaunchConfigurationListener(this);
		launchHistoryInstances.remove(this);
	}

	/**
	 * @see org.eclipse.debug.core.ILaunchListener#launchAdded(org.eclipse.debug.core.ILaunch)
	 */
	public void launchAdded(ILaunch launch) {
		ILaunchConfiguration configuration = launch.getLaunchConfiguration();
		if (configuration != null && !configuration.isWorkingCopy() && DebugUIPlugin.doLaunchConfigurationFiltering(configuration) && accepts(configuration)) {
			addHistory(configuration, true);
			setRecentLaunch(configuration);
		}
	}
	
	/**
	 * Adds the given configuration to this hisotry
	 * 
	 * @param configuration
	 * @param prepend whether the configuration should be added to the beginning of
	 * the history list
	 */
	protected void addHistory(ILaunchConfiguration configuration, boolean prepend) {
		clearDirty();
		if (fFavorites.contains(configuration)) {
			return;
		}
		// might be reconstructing history
		if (checkIfFavorite(configuration)) {
			return;
		}
		int index = fHistory.indexOf(configuration);
		if (index < 0) {
			if (prepend) {
				fHistory.add(0, configuration);
			} else {
				fHistory.add(configuration);
			}
			resizeHistory();
			setDirty();
		} else if (index > 0) {
			// move to first
			for (int i = index; i > 0; i--) {
				fHistory.set(i, fHistory.get(i -1));
			}
			fHistory.set(0, configuration);
			setDirty();
		}	
		save();
	}
	
	/**
	 * Saves if dirty
	 */
	private void save() {
		if (isDirty()) {
			try {
				DebugUIPlugin.getDefault().getLaunchConfigurationManager().persistLaunchHistory();
			} catch (CoreException e) {
				DebugUIPlugin.log(e);
			} catch (IOException e) {
				DebugUIPlugin.log(e);
			} catch (ParserConfigurationException e) {
				DebugUIPlugin.log(e);
			} catch (TransformerException e) {
				DebugUIPlugin.log(e);
			}
		}
	}
	
	/**
	 * Clears the dirty flag
	 */
	private void clearDirty() {
		fDirty = false;
	}
	
	/**
	 * Sets the dirty flag
	 */
	private void setDirty() {
		fDirty = true;
	}
	
	/**
	 * Returns the dirty state
	 */
	private boolean isDirty() {
		return fDirty;
	}	

	/**
	 * @see org.eclipse.debug.core.ILaunchListener#launchChanged(org.eclipse.debug.core.ILaunch)
	 */
	public void launchChanged(ILaunch launch) {
	}

	/**
	 * @see org.eclipse.debug.core.ILaunchListener#launchRemoved(org.eclipse.debug.core.ILaunch)
	 */
	public void launchRemoved(ILaunch launch) {
	}

	/**
	 * Returns the most recently launched configuration in this history, or
	 * <code>null</code> if none.
	 * 
	 * @return the most recently launched configuration in this history, or
	 * <code>null</code> if none 
	 */
	public ILaunchConfiguration getRecentLaunch() {
		try {
			if(fRecentLaunch != null && fRecentLaunch.exists()) {
				if(DebugUIPlugin.doLaunchConfigurationFiltering(fRecentLaunch) && !WorkbenchActivityHelper.filterItem(new LaunchConfigurationTypeContribution(fRecentLaunch.getType()))) {
					return fRecentLaunch;
				}
			}
		}
		catch(CoreException e) {e.printStackTrace();}
		return null;
	}
	
	/**
	 * Sets the most recently launched configuration in this history, or
	 * <code>null</code> if none.
	 */
	protected void setRecentLaunch(ILaunchConfiguration configuration) {
		if (accepts(configuration)) {
			if (!configuration.equals(fRecentLaunch)) {
				fRecentLaunch = configuration;
				setDirty();
				save();
			}
		}
	}	
	
	/**
	 * Returns the launch configuration in this history, in most recently
	 * launched order.
	 * 
	 * @return launch history
	 */
	public ILaunchConfiguration[] getHistory() {
		return (ILaunchConfiguration[])fHistory.toArray(new ILaunchConfiguration[fHistory.size()]);
	}
	
	/**
	 * Returns the favorite launch configurations in this history, in the order
	 * they were created.
	 * 
	 * @return launch favorites
	 */
	public ILaunchConfiguration[] getFavorites() {
		return (ILaunchConfiguration[])fFavorites.toArray(new ILaunchConfiguration[fFavorites.size()]);
	}
	
	/**
	 * Sets this container's favorites.
	 * 
	 * @param favorites
	 */
	public void setFavorites(ILaunchConfiguration[] favorites) {
		fFavorites = new ArrayList(favorites.length);
		for (int i = 0; i < favorites.length; i++) {
			fFavorites.add(favorites[i]);
		}
		setDirty();
		save();
	}	
	
	/**
	 * Adds the given configuration to the favorites list.
	 * 
	 * @param configuration
	 */
	public void addFavorite(ILaunchConfiguration configuration) {
		clearDirty();
		if (!fFavorites.contains(configuration)) {
			fFavorites.add(configuration);
			fHistory.remove(configuration);
			setDirty();
		}
		save();
	}
	
	/**
	 * Returns the launch group associated with this history
	 * 
	 * @return group
	 */
	public ILaunchGroup getLaunchGroup() {
		return fGroup;
	}
	
	/**
	 * Returns whether the given configruation is included in the group
	 * associated with this launch history.
	 * 
	 * @param launch
	 * @return boolean
	 */
	public boolean accepts(ILaunchConfiguration configuration) {
		try {
			if (!LaunchConfigurationManager.isVisible(configuration)) {
				return false;
			}
			if (configuration.getType().supportsMode(getLaunchGroup().getMode())) {
				String launchCategory = null;
				launchCategory = configuration.getCategory();
				String category = getLaunchGroup().getCategory();
				if (launchCategory == null || category == null) {
					return launchCategory == category;
				}
				return category.equals(launchCategory);
			}
		} catch (CoreException e) {
			DebugUIPlugin.log(e);
		}
		return false;
	}	
	
	/**
	 * Notifies all launch histories that the launch history size has changed.
	 */
	public static void launchHistoryChanged() {
		Iterator iter= launchHistoryInstances.iterator();
		while (iter.hasNext()) {
			LaunchHistory history= (LaunchHistory) iter.next();
			history.resizeHistory();
			history.save();			
		}

	}
	
	/**
	 * The max history size has changed - remove any histories if current
	 * collection is too long.
	 */
	protected void resizeHistory() {
		int max = getMaxHistorySize();
		while (fHistory.size() > max) {
			fHistory.remove(fHistory.size() - 1);
			setDirty();
		}
	}

	/**
	 * Returns the maximum number of entries allowed in this history
	 * 
	 * @return the maximum number of entries allowed in this history
	 */
	protected int getMaxHistorySize() {
		return DebugUIPlugin.getDefault().getPreferenceStore().getInt(IDebugUIConstants.PREF_MAX_HISTORY_SIZE);
	}
	
	/**
	 * @see org.eclipse.debug.core.ILaunchConfigurationListener#launchConfigurationAdded(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void launchConfigurationAdded(ILaunchConfiguration configuration) {
		ILaunchConfiguration movedFrom = DebugPlugin.getDefault().getLaunchManager().getMovedFrom(configuration);
		if (movedFrom == null) {
			checkIfFavorite(configuration);
		} else {
			String movedFromName= movedFrom.getName();
			ILaunchConfiguration[] history = getHistory();
			for (int i = 0; i < history.length; i++) {
				if (history[i].getName().equals(movedFromName)) {
					if (i == 0) {
						fRecentLaunch= configuration;
					}
					setDirty();
				}
			}
		}
	}
	
	/**
	 * Adds the given config to the favorites list if it is a favorite, and
	 * returns whether the config was added to the favorites list.
	 * 
	 * @param configuration
	 * @return whether added to the favorites list
	 */
	protected boolean checkIfFavorite(ILaunchConfiguration configuration) {
		// update favorites
		if (configuration.isWorkingCopy()) {
			return false;
		}
		try {
			List favoriteGroups = configuration.getAttribute(IDebugUIConstants.ATTR_FAVORITE_GROUPS, (List)null);
			if (favoriteGroups == null) {
				// check deprecated attributes for backwards compatibility
				String groupId = getLaunchGroup().getIdentifier();
				boolean fav = false;
				if (groupId.equals(IDebugUIConstants.ID_DEBUG_LAUNCH_GROUP)) {
					fav = configuration.getAttribute(IDebugUIConstants.ATTR_DEBUG_FAVORITE, false);
				} else if (groupId.equals(IDebugUIConstants.ID_RUN_LAUNCH_GROUP)) {
					fav = configuration.getAttribute(IDebugUIConstants.ATTR_RUN_FAVORITE, false);
				}
				if (fav) {
					addFavorite(configuration);
					return true;
				} 
				removeFavorite(configuration);
				return false;
			} else if (favoriteGroups.contains(getLaunchGroup().getIdentifier())) {
				addFavorite(configuration);
				return true;
			} else {
				removeFavorite(configuration);
				return false;
			}
		} catch (CoreException e) {
		}		
		return false;
	}
	
	/**
	 * Revmoves the given config from the favorites list, if needed.
	 * 
	 * @param configuration
	 */
	protected void removeFavorite(ILaunchConfiguration configuration) {
		if (fFavorites.contains(configuration)) {
			fFavorites.remove(configuration);
			setDirty();
			save();
		}
	}

	/**
	 * @see org.eclipse.debug.core.ILaunchConfigurationListener#launchConfigurationChanged(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void launchConfigurationChanged(ILaunchConfiguration configuration) {
		checkIfFavorite(configuration);
	}

	/**
	 * @see org.eclipse.debug.core.ILaunchConfigurationListener#launchConfigurationRemoved(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void launchConfigurationRemoved(ILaunchConfiguration configuration) {
		boolean changed = false;
		ILaunchConfiguration newConfig = DebugPlugin.getDefault().getLaunchManager().getMovedTo(configuration);
		if (newConfig == null) {
			// deleted
			changed = fHistory.remove(configuration) || fFavorites.remove(configuration);
		} else {
			// moved/renamed
			int index = fHistory.indexOf(configuration);
			if (index >= 0) {
				fHistory.remove(index);
				fHistory.add(index, newConfig);
				changed = true;
			} else {
				index = fFavorites.indexOf(configuration);
				if (index >= 0) {
					fFavorites.remove(index);
					fFavorites.add(index, newConfig);
				}
			}
			checkIfFavorite(newConfig);
		}
		if (changed) {
			setDirty();
			if (configuration.equals(fRecentLaunch)) {
				if (!fHistory.isEmpty()) {
					fRecentLaunch = (ILaunchConfiguration)fHistory.get(0);
				} else if (!fFavorites.isEmpty()) {
					fRecentLaunch = (ILaunchConfiguration)fFavorites.get(0);
				} else {
					fRecentLaunch = null;
				}
			}
			save();
		}
	}

}
