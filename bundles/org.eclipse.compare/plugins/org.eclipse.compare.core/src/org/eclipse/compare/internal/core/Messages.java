/*******************************************************************************
 * Licensed Materials - Property of IBM
 * (c) Copyright IBM Corporation 2008. All Rights Reserved.
 * 
 * Note to U.S. Government Users Restricted Rights:  Use,
 * duplication or disclosure restricted by GSA ADP Schedule 
 * Contract with IBM Corp.
 *******************************************************************************/
package org.eclipse.compare.internal.core;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.compare.internal.core.messages"; //$NON-NLS-1$
	public static String Activator_1;
	public static String FileDiffResult_0;
	public static String FileDiffResult_1;
	public static String FileDiffResult_2;
	public static String FileDiffResult_3;
	public static String Patcher_0;
	public static String Patcher_1;
	public static String Patcher_2;
	public static String WorkspacePatcher_0;
	public static String WorkspacePatcher_1;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
