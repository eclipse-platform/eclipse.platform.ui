/**********************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.ant.internal.ui.antsupport;

import org.eclipse.osgi.util.NLS;

public class AntSupportMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ant.internal.ui.antsupport.AntSupportMessages";//$NON-NLS-1$

	public static String AntInputHandler_Ant_Input_Request_1;
	public static String AntInputHandler_Invalid_input_2;
	public static String AntInputHandler_Unable_to_respond_to__input__request_4;
	public static String AntInputHandler_5;

	public static String NullBuildLogger_1;
	public static String AntProcessBuildLogger_Total_time;
	public static String AntProcessBuildLogger__minutes_2;
	public static String AntProcessBuildLogger__minute_3;
	public static String AntProcessBuildLogger__seconds_4;
	public static String AntProcessBuildLogger__second_5;
	public static String AntProcessBuildLogger__milliseconds_6;
	
	public static String AntProcessDebugBuildLogger_1;

	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, AntSupportMessages.class);
	}
}