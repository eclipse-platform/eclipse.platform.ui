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
public class NavigatorDescriptor {
	private static final String CHILD_CONTENT = "content"; //$NON-NLS-1$
	private static final String CHILD_ROOT_CONTENT = "rootContent"; //$NON-NLS-1$
	private static final String ATT_TARGET_ID = "targetId"; //$NON-NLS-1$

	private String targetId;
	private IConfigurationElement configElement;
	private NavigatorDelegateDescriptor contentDescriptor;
	private NavigatorRootDescriptor rootContentDescriptor;

/**
 * Creates a descriptor from a configuration element.
 * 
 * @param configElement configuration element to create a descriptor from
 */
public NavigatorDescriptor(IConfigurationElement configElement) throws WorkbenchException {
	super();
	this.configElement = configElement;
	readConfigElement();
}
/**
 */
public NavigatorDelegateDescriptor getContentDescriptor() {
	return contentDescriptor;
}
public NavigatorRootDescriptor getRootDescriptor() {
	return rootContentDescriptor;
}
public String getTargetId() {
	return targetId;
}
private void readConfigElement() throws WorkbenchException {
	targetId = configElement.getAttribute(ATT_TARGET_ID);
			
	IConfigurationElement[] children = configElement.getChildren(CHILD_CONTENT);
	if (children.length > 0)
		contentDescriptor = new NavigatorDelegateDescriptor(children[0]);

	children = configElement.getChildren(CHILD_ROOT_CONTENT);
	if (children.length > 0)
		rootContentDescriptor = new NavigatorRootDescriptor(children[0]);
}
}
