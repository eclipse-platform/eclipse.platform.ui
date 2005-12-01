/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.navigator.resources.internal.plugin;

import org.eclipse.osgi.util.NLS;

/**
 * Utility class which helps managing messages
 * 
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * @since 3.2
 */
public class WorkbenchNavigatorMessages extends NLS { 
	public static final String BUNDLE_NAME = "messages"; //$NON-NLS-1$

	public static String ResourceExtensionFilterProvider_Hides;
	public static String ImportResourcesAction_text;
	public static String ExportResourcesAction_text;
	

	public static String NewProjectWizard_errorTitle;
	public static String NewProjectAction_text;

	public static String OpenWithMenu_label;
	
	
	static {
		initializeMessages(BUNDLE_NAME, WorkbenchNavigatorMessages.class);
	}
}
