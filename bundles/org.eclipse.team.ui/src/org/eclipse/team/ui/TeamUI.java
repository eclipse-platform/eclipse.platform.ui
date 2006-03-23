/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.ui;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.internal.ui.history.GenericHistoryView;
import org.eclipse.team.internal.ui.registry.TeamContentProviderManager;
import org.eclipse.team.internal.ui.synchronize.SynchronizeManager;
import org.eclipse.team.ui.history.IHistoryView;
import org.eclipse.team.ui.mapping.ITeamContentProviderManager;
import org.eclipse.team.ui.synchronize.ISynchronizeManager;
import org.eclipse.ui.PartInitException;

/**
 * TeamUI contains public API for generic UI-based Team functionality.
 * <p>
 * This class is not intended to be subclassed or instantiated by clients
 */
public class TeamUI {

	// manages synchronize participants
	private static ISynchronizeManager synchronizeManager;
	
	/**
	 * Property constant indicating the global ignores list has changed. 
	 */
	public static final String GLOBAL_IGNORES_CHANGED = TeamUIPlugin.ID + "global_ignores_changed"; //$NON-NLS-1$
	
    /**
     * Property constant indicating the global file types list has changed.
     * @since 3.1
     */
	public static final String GLOBAL_FILE_TYPES_CHANGED = TeamUIPlugin.ID + "global_file_types_changed"; //$NON-NLS-1$

	/**
	 * Return the synchronize manager.
	 * 
	 * @return the synchronize manager
	 * @since 3.0
	 */
	public static ISynchronizeManager getSynchronizeManager() {
		if (synchronizeManager == null) {
			synchronizeManager = new SynchronizeManager();
		}
		return synchronizeManager;
	}

	/**
	 * Register for changes made to Team properties.
	 * 
	 * @param listener the listener to add
	 */
	public static void addPropertyChangeListener(IPropertyChangeListener listener) {
		TeamUIPlugin.addPropertyChangeListener(listener);
	}

	/**
	 * Remove the listener from Team property change listener list.
	 * 
	 * @param listener the listener to remove
	 */
	public static void removePropertyChangeListener(IPropertyChangeListener listener) {
		TeamUIPlugin.removePropertyChangeListener(listener);
	}
	
	/**
	 * Shows the history view and returns a handle to it. Note that in the case of many
	 * history views, the main history view is the one returned here.
	 * 
	 * @return an IHistoryView which is the main history view if it is found or null if it can't be found
	 * @since 3.2
	 */
	public static IHistoryView getHistoryView() {
		try {
			TeamUIPlugin.getActivePage().showView(GenericHistoryView.VIEW_ID);
			return (IHistoryView) TeamUIPlugin.getActivePage().findView(GenericHistoryView.VIEW_ID);
		} catch (PartInitException e) {
		}

		return null;
	}
	
	/**
	 * Return the team content provider manager which gives access to the team
	 * content proivders registered with the
	 * <code>org.eclipse.team.ui.teamContentProviders</code> extension point.
	 * 
	 * @return the team content provider manager
	 * @since 3.2
	 */
	public static ITeamContentProviderManager getTeamContentProviderManager() {
		return TeamContentProviderManager.getInstance();
	}
}
