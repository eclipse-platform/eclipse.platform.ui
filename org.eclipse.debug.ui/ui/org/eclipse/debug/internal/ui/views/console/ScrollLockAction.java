/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.console;


import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.actions.ActionMessages;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Toggles console auto-scroll
 */
public class ScrollLockAction extends Action implements IPropertyChangeListener {

	private IPreferenceStore fStore = DebugUIPlugin.getDefault().getPreferenceStore();
	
	public ScrollLockAction() {
		super(ActionMessages.getString("ScrollLockAction.Scroll_Lock_1")); //$NON-NLS-1$
		setToolTipText(ActionMessages.getString("ScrollLockAction.Scroll_Lock_1")); //$NON-NLS-1$
		setHoverImageDescriptor(DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_LCL_LOCK));		
		setDisabledImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_LOCK));
		setImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_LOCK));
		WorkbenchHelp.setHelp(
			this,
			IDebugHelpContextIds.CONSOLE_SCROLL_LOCK_ACTION);
		setChecked(DebugUIPlugin.getDefault().getPreferenceStore().getBoolean(IInternalDebugUIConstants.PREF_CONSOLE_SCROLL_LOCK));
		fStore.addPropertyChangeListener(this);
	}

	/**
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		fStore.setValue(IInternalDebugUIConstants.PREF_CONSOLE_SCROLL_LOCK, isChecked());
	}
	
	public void dispose() {
		fStore.removePropertyChangeListener(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().equals(IInternalDebugUIConstants.PREF_CONSOLE_SCROLL_LOCK)) {
			setChecked(fStore.getBoolean(IInternalDebugUIConstants.PREF_CONSOLE_SCROLL_LOCK));
		}
		
	}
}

