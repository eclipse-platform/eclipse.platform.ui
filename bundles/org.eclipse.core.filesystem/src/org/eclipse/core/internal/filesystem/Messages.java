/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.filesystem;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.core.internal.filesystem.messages"; //$NON-NLS-1$

	public static String copying;
	public static String couldNotCreateFolder;
	public static String couldnotDelete;
	public static String couldnotDeleteReadOnly;
	public static String couldNotLoadLibrary;
	public static String couldNotMove;
	public static String couldNotRead;
	public static String couldNotWrite;
	public static String deleteProblem;
	public static String failedCreateWrongType;
	public static String failedMove;
	public static String failedReadDuringWrite;
	public static String fileNotFound;
	public static String moving;
	public static String noImplDelete;
	public static String noImplWrite;
	public static String notAFile;
	public static String readOnlyParent;
	public static String fileExists;

	static {
		// initialize resource bundles
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

}
