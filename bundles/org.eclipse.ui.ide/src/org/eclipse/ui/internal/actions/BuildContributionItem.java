/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.actions;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * This contribution item controls the visibility of the incremental
 * build action based on the current preference value for auto-building.
 * 
 * @since 3.0
 */
public class BuildContributionItem extends ActionContributionItem {
	private IWorkbenchWindow window = null;

	private IPropertyChangeListener prefListener = new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
			if (event.getProperty().equals(ResourcesPlugin.PREF_AUTO_BUILDING)) {
				if (getParent() != null) {
					boolean autoBuild = ResourcesPlugin.getPlugin().getPluginPreferences().getDefaultBoolean(ResourcesPlugin.PREF_AUTO_BUILDING);
					setVisible(!autoBuild);
					getParent().markDirty();
					if (window.getShell() != null && !window.getShell().isDisposed()) {
						// this property change notification could be from a non-ui thread
						window.getShell().getDisplay().syncExec(new Runnable() {
							public void run() {
								getParent().update(false);
							}
						});
					}
				}
			}
		}
	};

	/**
	 * Create the incremental build contribution item
	 */
	public BuildContributionItem(IAction action, IWorkbenchWindow window) {
		super(action);
		
		if (window == null) {
			throw new IllegalArgumentException();
		}
		this.window = window;
		
		setVisible(ResourcesPlugin.getWorkspace().isAutoBuilding());
		ResourcesPlugin.getPlugin().getPluginPreferences().addPropertyChangeListener(prefListener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IContributionItem#isVisible()
	 */
	public boolean isVisible() {
		// @issue the ActionContributionItem implementation of this method ignores the "visible" value set
		return super.isVisible() && !ResourcesPlugin.getWorkspace().isAutoBuilding();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IContributionItem#dispose()
	 */
	public void dispose() {
		super.dispose();
		ResourcesPlugin.getPlugin().getPluginPreferences().removePropertyChangeListener(prefListener);
	}
}
