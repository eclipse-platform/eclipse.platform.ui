package org.eclipse.ui.externaltools.internal.menu;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.internal.registry.ExternalToolRegistry;
import org.eclipse.ui.externaltools.internal.registry.ExternalToolType;
import org.eclipse.ui.externaltools.model.ExternalTool;
import org.eclipse.ui.externaltools.model.ExternalToolStorage;
import org.eclipse.ui.externaltools.model.IStorageListener;

/**
 * This class manages the list of favorite external tools. These
 * favorites are shown on the Run > External Tools menu.
 */
public final class FavoritesManager {
	private static final FavoritesManager INSTANCE = new FavoritesManager();
	private final StorageListener storageListener = new StorageListener();
	private Set favorites = new TreeSet(new ExternalToolComparator());	
	private ExternalTool lastTool;

	// Private constructor to ensure this class is a singleton.
	private FavoritesManager() {
		// Add the favorites manager as a storage listener so
		// it can react to changes in the registry.
		ExternalToolStorage.addStorageListener(storageListener);
		updateFavorites();
	}
	
	/**
	 * Returns the singleton instance of the favorites manager.
	 * 
	 * @return the singleton instance of the favorites manager.
	 */
	public static FavoritesManager getInstance() {
		return INSTANCE;
	}
	
	/**
	 * Add an external tool to the favorites list,
	 * if it is not already in the list.
	 */
	private void add(ExternalTool tool) {
		favorites.add(tool);	
	}
	
	/**
	 * Removes an external tool from the favorites list,
	 * if it is in the list.
	 */
	private void remove(ExternalTool tool) {
		favorites.remove(tool);	
	}
	
	/**
	 * Returns an array of the favorite external tools.
	 */
	public ExternalTool[] getFavorites() {
		return (ExternalTool[])favorites.toArray(new ExternalTool[favorites.size()]);
	}
	
	/**
	 * Returns the last run tool if there is one,
	 * <code>null</code> otherwise.
	 */
	public ExternalTool getLastTool() {
		return lastTool;	
	}
	
	/**
	 * Sets the last run tool to be the given tool.
	 */
	public void setLastTool(ExternalTool tool) {
		lastTool = tool;
	}

	/**
	 * Updates the favorites list based on whether
	 * each tool in the registry is a favorite.
	 */	
	private void updateFavorites() {
		boolean lastToolDeleted = true;
		favorites.clear();
		
		ExternalToolType[] types = 
			ExternalToolsPlugin.getDefault().getTypeRegistry().getToolTypes();
		ExternalToolRegistry registry = 
			ExternalToolsPlugin.getDefault().getToolRegistry(null);
		for (int i=0; i < types.length; i++) {
			ExternalTool[] tools = registry.getToolsOfType(types[i].getId());
			for (int j=0; j < tools.length; j++) {
				if (tools[j].getShowInMenu())
					add(tools[j]);
				if (tools[j] == lastTool)
					lastToolDeleted = false;
			}
		}
		
		if (lastToolDeleted)
			lastTool = null;	
	}
	
	/**
	 * Updates the favorites list based on whether
	 * the given tool is a favorite.
	 */
	private void updateFavorites(ExternalTool tool) {
		if (tool.getShowInMenu())
			add(tool);
		else
			remove(tool);		
	}
	
	/**
	 * A storage listener so the favorites manager can react 
	 * to changes in the registry.
	 */
	private class StorageListener implements IStorageListener {
		/* (non-Javadoc)
		 * Method declared on IStorageListener.
		 */
		public void toolDeleted(ExternalTool tool) {
			remove(tool);
			if (lastTool == tool)
				setLastTool(null);
		}
	
		/* (non-Javadoc)
		 * Method declared on IStorageListener.
		 */	
		public void toolCreated(ExternalTool tool) {
			if (tool.getShowInMenu())
				add(tool);
		}
	
		/* (non-Javadoc)
		 * Method declared on IStorageListener.
		 */
		public void toolModified(ExternalTool tool) {
			updateFavorites(tool);
		}
	
		/* (non-Javadoc)
		 * Method declared on IStorageListener.
		 */	
		public void toolsRefreshed() {
			updateFavorites();
		}		
	}
	
	/**
	 * Compares external tools so they may be stored 
	 * in alphabetical order.
	 */
	private static class ExternalToolComparator implements Comparator {
		public int compare(Object o1, Object o2) {
			ExternalTool tool1 = (ExternalTool) o1;
			ExternalTool tool2 = (ExternalTool) o2;
			return (tool1.getName().compareTo(tool2.getName()));
		}
	}
}
