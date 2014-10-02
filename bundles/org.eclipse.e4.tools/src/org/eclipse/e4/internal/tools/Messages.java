/*******************************************************************************
 * Copyright (c) 2011-2014 EclipseSource Muenchen GmbH and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Jonas - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.internal.tools;

import org.eclipse.osgi.util.NLS;

/**
 * @author Jonas
 *
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.e4.internal.tools.messages"; //$NON-NLS-1$
	public static String AbstractNewClassPage_Browse;
	public static String AbstractNewClassPage_ChooseAPackage;
	public static String AbstractNewClassPage_ChoosePackage;
	public static String AbstractNewClassPage_ClassExists;
	public static String AbstractNewClassPage_Name;
	public static String AbstractNewClassPage_NameMustBeQualified;
	public static String AbstractNewClassPage_NameNotEmpty;
	public static String AbstractNewClassPage_NeedToSelectAPackage;
	public static String AbstractNewClassPage_Package;
	public static String AbstractNewClassPage_SourceFolder;
	public static String AbstractNewClassPage_SourceFolderNotEmpty;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
