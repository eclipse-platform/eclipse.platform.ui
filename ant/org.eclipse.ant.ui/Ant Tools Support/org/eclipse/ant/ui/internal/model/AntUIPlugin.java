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
package org.eclipse.ant.ui.internal.model;


import java.util.Locale;

import org.eclipse.ant.ui.internal.editor.text.IAntEditorColorConstants;
import org.eclipse.ant.ui.internal.preferences.AntEditorPreferenceConstants;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * The plug-in runtime class for the Ant Core plug-in.
 */
public class AntUIPlugin extends AbstractUIPlugin {

	/**
	 * Status code indicating an unexpected internal error.
	 * @since 2.1
	 */
	public static final int INTERNAL_ERROR = 120;		
	
	/**
	 * The single instance of this plug-in runtime class.
	 */
	private static AntUIPlugin plugin;

	/**
	 * Unique identifier constant (value <code>"org.eclipse.ant.ui"</code>)
	 * for the Ant UI plug-in.
	 */
	public static final String PI_ANTUI = "org.eclipse.ant.ui"; //$NON-NLS-1$
	
	private static final String EMPTY_STRING= ""; //$NON-NLS-1$

	/** 
	 * Constructs an instance of this plug-in runtime class.
	 * <p>
	 * An instance of this plug-in runtime class is automatically created 
	 * when the facilities provided by the Ant Core plug-in are required.
	 * <b>Clients must never explicitly instantiate a plug-in runtime class.</b>
	 * </p>
	 * 
	 * @param pluginDescriptor the plug-in descriptor for the
	 *   Ant UI plug-in
	 */
	public AntUIPlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		plugin = this;
	}

	/**
	 * @see org.eclipse.core.runtime.Plugin#shutdown()
	 */
	public void shutdown() throws CoreException {
		super.shutdown();
		ColorManager.getDefault().dispose();
		AntUIImages.disposeImageDescriptorRegistry();
	}

	/**
	 * Returns this plug-in instance.
	 *
	 * @return the single instance of this plug-in runtime class
	 */
	public static AntUIPlugin getDefault() {
		return plugin;
	}
	
	/**
	 * Logs the specified throwable with this plug-in's log.
	 * 
	 * @param t throwable to log 
	 */
	public static void log(Throwable t) {
		IStatus status= new Status(IStatus.ERROR, PI_ANTUI, INTERNAL_ERROR, "Error logged from Ant Core: ", t); //$NON-NLS-1$
		getDefault().getLog().log(status);
	}
	
	/**
	 * Writes the message to the plug-in's log
	 * 
	 * @param message the text to write to the log
	 */
	public static void log(String message, Throwable exception) {
		IStatus status = newErrorStatus(message, exception);
		getDefault().getLog().log(status);
	}
	
	/**
	 * Returns a new <code>IStatus</code> for this plug-in
	 */
	public static IStatus newErrorStatus(String message, Throwable exception) {
		if (message == null) {
			message= EMPTY_STRING; 
		}		
		return new Status(IStatus.ERROR, IAntUIConstants.PLUGIN_ID, 0, message, exception);
	}
	
	/* (non-Javadoc)
	 * Method declared in AbstractUIPlugin.
	 */
	protected void initializeDefaultPreferences(IPreferenceStore prefs) {
		prefs.setDefault(IAntUIPreferenceConstants.ANT_FIND_BUILD_FILE_NAMES, "build.xml"); //$NON-NLS-1$
		if (isMacOS()) {
			//the mac does not have a tools.jar Bug 40778
			prefs.setDefault(IAntUIPreferenceConstants.ANT_TOOLS_JAR_WARNING, false);
		} else {
			prefs.setDefault(IAntUIPreferenceConstants.ANT_TOOLS_JAR_WARNING, true);
		}
		prefs.setDefault(IAntUIPreferenceConstants.ANT_XERCES_JARS_WARNING, true);
		// Ant Editor color preferences
		PreferenceConverter.setDefault(prefs, IAntEditorColorConstants.P_DEFAULT, IAntEditorColorConstants.DEFAULT);
		PreferenceConverter.setDefault(prefs, IAntEditorColorConstants.P_PROC_INSTR, IAntEditorColorConstants.PROC_INSTR);
		PreferenceConverter.setDefault(prefs, IAntEditorColorConstants.P_STRING, IAntEditorColorConstants.STRING);
		PreferenceConverter.setDefault(prefs, IAntEditorColorConstants.P_TAG, IAntEditorColorConstants.TAG);
		PreferenceConverter.setDefault(prefs, IAntEditorColorConstants.P_XML_COMMENT, IAntEditorColorConstants.XML_COMMENT);
		
		PreferenceConverter.setDefault(prefs, IAntUIPreferenceConstants.CONSOLE_ERROR_RGB, new RGB(255, 0, 0)); // red - exactly the same as debug Consol
		PreferenceConverter.setDefault(prefs, IAntUIPreferenceConstants.CONSOLE_WARNING_RGB, new RGB(255, 100, 0)); // orange
		PreferenceConverter.setDefault(prefs, IAntUIPreferenceConstants.CONSOLE_INFO_RGB, new RGB(0, 0, 255)); // blue
		PreferenceConverter.setDefault(prefs, IAntUIPreferenceConstants.CONSOLE_VERBOSE_RGB, new RGB(0, 200, 125)); // green
		PreferenceConverter.setDefault(prefs, IAntUIPreferenceConstants.CONSOLE_DEBUG_RGB, new RGB(0, 0, 0)); // black
				
		
		AntEditorPreferenceConstants.initializeDefaultValues(prefs);
	}
	
	/**
	 * Returns the standard display to be used. The method first checks, if
	 * the thread calling this method has an associated display. If so, this
	 * display is returned. Otherwise the method returns the default display.
	 */
	public static Display getStandardDisplay() {
		Display display = Display.getCurrent();
		if (display == null) {
			display = Display.getDefault();
		}
		return display;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#createImageRegistry()
	 */
	protected ImageRegistry createImageRegistry() {
		return AntUIImages.initializeImageRegistry();
	}
	
	/**
	 * Returns the preference color, identified by the given preference.
	 */
	public static Color getPreferenceColor(String pref) {
		return ColorManager.getDefault().getColor(PreferenceConverter.getColor(getDefault().getPreferenceStore(), pref));
	}	
	
	/**
	* Returns the active workbench page or <code>null</code> if none.
	*/
   public static IWorkbenchPage getActivePage() {
	   IWorkbenchWindow window= getActiveWorkbenchWindow();
	   if (window != null) {
		   return window.getActivePage();
	   }
	   return null;
   }

   /**
	* Returns the active workbench window or <code>null</code> if none
	*/
   public static IWorkbenchWindow getActiveWorkbenchWindow() {
	   return getDefault().getWorkbench().getActiveWorkbenchWindow();
   }
   
   /**
	* Returns whether the current OS claims to be Mac
	*/
   public static boolean isMacOS() {
		String osname= System.getProperty("os.name").toLowerCase(Locale.US); //$NON-NLS-1$
		return osname.indexOf("mac") != -1; //$NON-NLS-1$
   }
}
