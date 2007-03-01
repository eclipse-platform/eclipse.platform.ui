/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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
import org.eclipse.team.ui.history.IHistoryPageSource;
import org.eclipse.team.ui.history.IHistoryView;
import org.eclipse.team.ui.mapping.ITeamContentProviderManager;
import org.eclipse.team.ui.synchronize.ISynchronizeManager;
import org.eclipse.ui.*;

/**
 * TeamUI contains public API for generic UI-based Team functionality.
 * <p>
 * This class is not intended to be subclassed or instantiated by clients
 */
public class TeamUI {
	
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
	 * Id of the Team Local History action that is contributed by this plugin. Repository providers may override
	 * this id in their action definition to provide a unified history.
	 * @since 3.3
	 */
	public static final String SHOW_LOCAL_HISTORY_ACTION_ID = "org.eclipse.team.ui.showLocalHistory"; //$NON-NLS-1$
	
	/**
	 * Id of the Compare With Local History action that is contributed by this plugin. Repository providers may override
	 * this id in their action definition to provide a unified history.
	 * @since 3.3
	 */
	public static final String COMPARE_WITH_LOCAL_HISTORY_ACTION_ID = "org.eclipse.team.ui.compareLocalHistory"; //$NON-NLS-1$
	
	/**
	 * Id of the Replace With Local History action that is contributed by this plugin. Repository providers may override
	 * this id in their action definition to provide a unified history.
	 * @since 3.3
	 */
	public static final String REPLACE_WITH_LOCAL_HISTORY_ACTION_ID = "org.eclipse.team.ui.replaceLocalHistory"; //$NON-NLS-1$
	
	
	/**
	 * Return the synchronize manager.
	 * 
	 * @return the synchronize manager
	 * @since 3.0
	 */
	public static ISynchronizeManager getSynchronizeManager() {
		return TeamUIPlugin.getPlugin().getSynchronizeManager();
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
			TeamUIPlugin.getActivePage().showView(IHistoryView.VIEW_ID);
			return (IHistoryView) TeamUIPlugin.getActivePage().findView(IHistoryView.VIEW_ID);
		} catch (PartInitException e) {
		}

		return null;
	}
	
	/**
	 * Shows a history view containing the given input and returns a handle to the view
	 * or <code>null</code> if no history was available for the given input. If an appropriate
	 * instance of a history view is already opened, its input will be changed and the view will
	 * be activated. Otherwise a new view will be opened.
	 * @param page the workbench page containing the history view
	 * @param input the input whose history is to be displayed
	 * @param pageSource the history page source from which to obtain the page or <code>null</code> 
	 * 	if the page source should be determined using the Adapter manager.
	 * 
	 * @return an IHistoryView which is the main history view if it is found or null if it can't be found
	 * @since 3.3
	 */
	public static IHistoryView showHistoryFor(IWorkbenchPage page, Object input, IHistoryPageSource pageSource) {
		try {
			IHistoryView view = (IHistoryView) page.findView(IHistoryView.VIEW_ID);
			if (view == null) {
				page.showView(IHistoryView.VIEW_ID);
				view = (IHistoryView) TeamUIPlugin.getActivePage().findView(IHistoryView.VIEW_ID);
				return showInputInView(page, input, view, pageSource);
			} else {
				view = ((GenericHistoryView)view).findAppropriateHistoryViewFor(input, pageSource);
				if (view == null) {
					view = (IHistoryView) page.showView(IHistoryView.VIEW_ID, IHistoryView.VIEW_ID + System.currentTimeMillis(), IWorkbenchPage.VIEW_CREATE);
					return showInputInView(page, input, view, pageSource);
				} else {
					return showInputInView(page, input, view, pageSource);
				}
			}
		} catch (PartInitException e) {
		}

		return null;
	}

	private static IHistoryView showInputInView(IWorkbenchPage page,
			Object input, IHistoryView view, IHistoryPageSource pageSource) {
		page.activate((IWorkbenchPart)view);
		((GenericHistoryView)view).showHistoryPageFor(input, true, false, pageSource);
		return view;
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
