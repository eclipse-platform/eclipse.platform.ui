package org.eclipse.ui.internal.performance;
/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/

import org.eclipse.osgi.util.NLS;

/**
 * The PerformanceMessages are messages used in the performance view.
 *
 */

public class PerformanceMessages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.ui.internal.performance.messages";//$NON-NLS-1$
	
	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, PerformanceMessages.class);
	}
	
	public static String PerformanceView_InvalidInput;
	public static String PerformanceView_InvalidColumn;
	public static String PerformanceView_eventHeader;
	public static String PerformanceView_blameHeader;
	public static String PerformanceView_contextHeader;
	public static String PerformanceView_countHeader;
	public static String PerformanceView_timeHeader;
	public static String PerformanceView_resetAction;
	public static String PerformanceView_resetTooltip;
}
