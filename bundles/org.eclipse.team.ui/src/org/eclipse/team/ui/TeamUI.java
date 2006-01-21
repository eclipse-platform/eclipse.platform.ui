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
import org.eclipse.team.internal.ui.synchronize.SynchronizeManager;
import org.eclipse.team.ui.history.IHistoryView;
import org.eclipse.team.ui.synchronize.ISynchronizeManager;

/**
 * TeamUI contains public API for generic UI-based Team functionality.
 * <p>
 * This class is not intended to be subclassed or instantiated by clients
 */
public class TeamUI {

	// manages synchronize participants
	private static ISynchronizeManager synchronizeManager;
	
	//returns the history view
	private static IHistoryView historyView;

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
	 * TODO: This won't work!!! We need to find the view in
	 * the current perspective and, if we can't, open it
	 * @return
	 */
	public static IHistoryView getHistoryView(){
		if (historyView == null)
			historyView = new GenericHistoryView();
			
		return historyView;
	}
}
