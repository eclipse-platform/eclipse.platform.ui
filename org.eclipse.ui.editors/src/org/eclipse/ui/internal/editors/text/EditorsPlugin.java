/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.editors.text;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

import org.eclipse.jface.preference.IPreferenceStore;

import org.eclipse.ui.editors.text.TextEditorPreferenceConstants;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * Represents the editors plug-in. It provides a series of convenience methods such as
 * access to the shared text colors and the log.
 * 
 * @since 2.1
 */
public class EditorsPlugin extends AbstractUIPlugin {

	private static EditorsPlugin fgInstance;
	
	public static EditorsPlugin getDefault() {
		return fgInstance;
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
	
	
	
	private FileEditorInputAdapterFactory fFileEditorInputAdapterFactory;
	
	
	public EditorsPlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		fgInstance= this;
	}


	/*
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#initializeDefaultPreferences(org.eclipse.jface.preference.IPreferenceStore)
	 */
	protected void initializeDefaultPreferences(IPreferenceStore store) {
		TextEditorPreferenceConstants.initializeDefaultValues(store);
	}
	
	/*
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#startup()
	 */
	public void startup() throws CoreException {
		super.startup();
		fFileEditorInputAdapterFactory= new FileEditorInputAdapterFactory();
		IAdapterManager manager= Platform.getAdapterManager();		
		manager.registerAdapters(fFileEditorInputAdapterFactory, IFile.class);
	}
	
	/*
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#shutdown()
	 */
	public void shutdown() throws CoreException {
		IAdapterManager manager= Platform.getAdapterManager();		
		manager.unregisterAdapters(fFileEditorInputAdapterFactory);
		super.shutdown();
	}
}
