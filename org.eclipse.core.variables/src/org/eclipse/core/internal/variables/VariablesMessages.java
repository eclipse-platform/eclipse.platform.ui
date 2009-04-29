/**********************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.internal.variables;

import org.eclipse.osgi.util.NLS;

public class VariablesMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.core.internal.variables.VariablesMessages";//$NON-NLS-1$

	public static String StringSubstitutionEngine_3;
	public static String StringSubstitutionEngine_4;

	public static String StringVariableManager_26;
	public static String StringVariableManager_27;

	public static String DynamicVariable_0;

	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, VariablesMessages.class);
	}
}