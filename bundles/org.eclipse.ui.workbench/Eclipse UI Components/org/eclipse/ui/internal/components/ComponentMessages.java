/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.ui.internal.components;

import org.eclipse.osgi.util.NLS;

public class ComponentMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ui.internal.components.messages";//$NON-NLS-1$

    public static String Components_instantiationException;
	public static String ComponentException_unable_to_instantiate;
	public static String ComponentException_recursive_requires_string;
	public static String Components_missing_required;
	public static String ComponentUtil_class_not_found;
	public static String ComponentUtil_missing_attribute;
	public static String Components_wrong_type;
	public static String ReflectionFactory_missing_data;
	public static String ReflectionFactory_no_constructors;
	public static String Container_missing_dependency;
	public static String Container_cycle_detected;
    public static String ComponentException_unable_to_instantiate_because;

	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, ComponentMessages.class);
	}
}