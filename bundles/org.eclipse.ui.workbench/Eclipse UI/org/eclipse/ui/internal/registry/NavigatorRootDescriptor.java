/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.registry;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.WorkbenchException;

/**
 * 
 * @since 3.0
 */
public class NavigatorRootDescriptor extends NavigatorContentDescriptor {
	public static final String ATT_ELEMENT_CLASS = "elementClass"; //$NON-NLS-1$	
	
	Class elementClass;
	
	/**
	 * Creates a descriptor from a configuration element.
	 * 
	 * @param configElement configuration element to create a descriptor from
	 */
	public NavigatorRootDescriptor(IConfigurationElement configElement) throws WorkbenchException {
		super(configElement);
		readConfigElement();
	}
	public Class getElementClass() {
		return elementClass;
	}
	protected void readConfigElement() throws WorkbenchException {
		super.readConfigElement();
		IConfigurationElement configElement = getConfigurationElement();
		try {
			elementClass = Class.forName(configElement.getAttribute(ATT_ELEMENT_CLASS));
		} catch (ClassNotFoundException e) {
		}
	}
}
