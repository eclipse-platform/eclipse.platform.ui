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
package org.eclipse.update.internal.core;
import java.io.*;

import org.eclipse.update.configuration.*;
import org.eclipse.update.internal.model.*;
public class ConfigurationActivity
	extends ConfigurationActivityModel
	implements IActivity, IWritable {
		
	/**
	 * Default constructor
	 */		
	public ConfigurationActivity() {
	}
	
	/**
	 * Constructor with action
	 */
	public ConfigurationActivity(int action) {
		super();
		setAction(action);
		setStatus(STATUS_NOK);
	}
	
	/*
	 * @see IWritable#write(int, PrintWriter)
	 */
	public void write(int indent, PrintWriter w) {
//		String gap= ""; //$NON-NLS-1$
//		for (int i= 0; i < indent; i++)
//			gap += " "; //$NON-NLS-1$
//		String increment= ""; //$NON-NLS-1$
//		for (int i= 0; i < IWritable.INDENT; i++)
//			increment += " "; //$NON-NLS-1$
//			
//		// ACTIVITY	
//		w.print(gap + "<" + InstallConfigurationParser.ACTIVITY + " ");
//		//$NON-NLS-1$ //$NON-NLS-2$
//		w.println("action=\"" + getAction() + "\" "); //$NON-NLS-1$ //$NON-NLS-2$
//		if (getLabel() != null) {
//			w.println(gap + increment+ "label=\"" + UpdateManagerUtils.Writer.xmlSafe(getLabel()) + "\" ");
//			//$NON-NLS-1$ //$NON-NLS-2$
//		}
//		w.println(gap + increment+"date=\"" + getDate().getTime() + "\" ");
//		//$NON-NLS-1$ //$NON-NLS-2$
//		w.println(gap + increment+"status=\"" + getStatus() + "\">"); //$NON-NLS-1$ //$NON-NLS-2$
//
//		// end
//		w.println(gap + "</" + InstallConfigurationParser.ACTIVITY + ">");
//		//$NON-NLS-1$ //$NON-NLS-2$
//		w.println(""); //$NON-NLS-1$		
	}
	
	/*
	 * @see IActivity#getInstallConfiguration()
	 */
	public IInstallConfiguration getInstallConfiguration() {
		return (IInstallConfiguration) getInstallConfigurationModel();
	}
	
	public boolean equals(Object other) {
		if (!(other instanceof ConfigurationActivity))
			return false;
		if (this == other)
			return true;
		
		ConfigurationActivity activity = (ConfigurationActivity) other;
		return getAction() == activity.getAction()
				&& getLabel().equals(activity.getLabel())
				&& getStatus() == activity.getStatus();
	}
}
