/**********************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 * dakshinamurthy.karra@gmail.com - bug 165371
 **********************************************************************/
package org.eclipse.ant.internal.launching.launchConfigurations;

import org.eclipse.osgi.util.NLS;

public class AntLaunchConfigurationMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ant.internal.launching.launchConfigurations.AntLaunchConfigurationMessages";//$NON-NLS-1$

	public static String AntLaunchDelegate_Launching__0__1;
	public static String AntLaunchDelegate_Running__0__2;
	public static String AntLaunchDelegate_Build_In_Progress;
	public static String AntLaunchDelegate_Failure;
	public static String AntLaunchDelegate_22;
	public static String AntLaunchDelegate_23;
	public static String AntLaunchDelegate_28;

	public static String AntHomeClasspathEntry_8;
	public static String AntHomeClasspathEntry_9;
	public static String AntHomeClasspathEntry_10;
	public static String AntHomeClasspathEntry_11;

	public static String ContributedClasspathEntriesEntry_1;

	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, AntLaunchConfigurationMessages.class);
	}
}