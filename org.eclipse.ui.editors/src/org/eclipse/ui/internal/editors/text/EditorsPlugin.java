/**********************************************************************
Copyright (c) 2000, 2003 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/
package org.eclipse.ui.internal.editors.text;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.jface.text.source.ISharedTextColors;

import org.eclipse.jface.preference.IPreferenceStore;

import org.eclipse.ui.editors.text.TextEditorPreferenceConstants;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * EditorsPlugin.java
 */
public class EditorsPlugin extends AbstractUIPlugin {
	
	private static EditorsPlugin fgInst;

	private ISharedTextColors fSharedTextColors;

	public EditorsPlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		fgInst= this;
	}

	public static EditorsPlugin getDefault() {
		return fgInst;
	}
	
	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}
	
	public static String getPluginId() {
		return getDefault().getDescriptor().getUniqueIdentifier();
	}
	
	public static void logErrorMessage(String message) {
		log(new Status(IStatus.ERROR, getPluginId(), IEditorsStatusConstants.INTERNAL_ERROR, message, null));
	}

	public static void logErrorStatus(String message, IStatus status) {
		if (status == null) {
			logErrorMessage(message);
			return;
		}
		MultiStatus multi= new MultiStatus(getPluginId(), IEditorsStatusConstants.INTERNAL_ERROR, message, null);
		multi.add(status);
		log(multi);
	}
	
	public static void log(Throwable e) {
		log(new Status(IStatus.ERROR, getPluginId(), IEditorsStatusConstants.INTERNAL_ERROR, TextEditorMessages.getString("EditorsPlugin.internal_error"), e)); //$NON-NLS-1$
	}

	public ISharedTextColors getSharedTextColors() {
		if (fSharedTextColors == null)
			fSharedTextColors= new SharedTextColors();
		return fSharedTextColors;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#initializeDefaultPreferences(org.eclipse.jface.preference.IPreferenceStore)
	 */
	protected void initializeDefaultPreferences(IPreferenceStore store) {
		TextEditorPreferenceConstants.initializeDefaultValues(store);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.editors.text.EditorsPlugin#shutdown()
	 */
	public void shutdown() throws CoreException {
		if (fSharedTextColors != null) {
			fSharedTextColors.dispose();
			fSharedTextColors= null;
		}
		super.shutdown();
	}
}
