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

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.PinEditorAction;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * This contribution item controls the visibility of the pin editor
 * action based on the current preference value for reusing editors.
 * 
 * @since 3.0
 */
public class PinEditorContributionItem extends ActionContributionItem {
	private boolean reuseEditors = false;
	
	private IPropertyChangeListener prefListener = new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
			if (event.getProperty().equals(IPreferenceConstants.REUSE_EDITORS_BOOLEAN)) {
				reuseEditors = WorkbenchPlugin.getDefault().getPreferenceStore().getBoolean(IPreferenceConstants.REUSE_EDITORS_BOOLEAN);
				setVisible(reuseEditors);
				getParent().markDirty();
				getParent().update(false);
			}
		}
	};
	
	/**
	 * @param action
	 */
	public PinEditorContributionItem(PinEditorAction action) {
		super(action);
		
		reuseEditors = WorkbenchPlugin.getDefault().getPreferenceStore().getBoolean(IPreferenceConstants.REUSE_EDITORS_BOOLEAN);
		setVisible(reuseEditors);
		WorkbenchPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(prefListener);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IContributionItem#isVisible()
	 */
	public boolean isVisible() {
		// @issue the ActionContributionItem implementation of this method ignores the "visible" value set
		return super.isVisible() && reuseEditors;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IContributionItem#dispose()
	 */
	public void dispose() {
		super.dispose();
		WorkbenchPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(prefListener);
	}
}
