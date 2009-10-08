/**********************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.ui.externaltools.internal.program.launchConfigurations;

import org.eclipse.osgi.util.NLS;

public class ExternalToolsProgramMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ui.externaltools.internal.program.launchConfigurations.ExternalToolsProgramMessages";//$NON-NLS-1$

	public static String BackgroundResourceRefresher_0;

	public static String ProgramLaunchDelegate_Workbench_Closing_1;
	public static String ProgramLaunchDelegate_The_workbench_is_exiting;

	public static String ProgramMainTab_Select;

	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, ExternalToolsProgramMessages.class);
	}
}