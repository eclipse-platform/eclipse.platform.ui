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
import org.eclipse.ui.internal.ActionExpression;

/**
 * 
 * @since 3.0
 */
public class NavigatorContentDescriptor extends NavigatorAbstractContentDescriptor {
	private static final String CHILD_ENABLEMENT = "enablement"; //$NON-NLS-1$
	private static final String ATT_PRIORITY = "priority"; //$NON-NLS-1$
	private static final String ATT_CONTENT_TARGET_ID = "contentTargetId"; //$NON-NLS-1$

	private int priority;
	private String contentTargetId;
	private ActionExpression enablement;
	/**
	 * Creates a descriptor from a configuration element.
	 * 
	 * @param configElement configuration element to create a descriptor from
	 */
	public NavigatorContentDescriptor(IConfigurationElement configElement) throws WorkbenchException {
		super(configElement);
	}
	public String getContentTargetId() {
		return contentTargetId;
	}
	/**
	 */
	public int getPriority() {
		return priority;
	}
	/**
	 */
	public ActionExpression getEnableExpression() {
		return enablement;
	}
	protected void readConfigElement() throws WorkbenchException {
		IConfigurationElement configElement = getConfigurationElement();
		
		super.readConfigElement();		
		String priorityString = configElement.getAttribute(ATT_PRIORITY);
		if (priorityString != null) {
			try {
				priority = Integer.valueOf(priorityString).intValue();
			}
			catch (NumberFormatException exception) {
				// TODO: handle exception
			}
		} 
		contentTargetId = configElement.getAttribute(ATT_CONTENT_TARGET_ID);
			
		IConfigurationElement[] children = configElement.getChildren(CHILD_ENABLEMENT);
		if (children.length == 1) {
			enablement = new ActionExpression(children[0]);
		}
		else
		if (children.length > 1) {
			throw new WorkbenchException("More than one element: " +//$NON-NLS-1$
				CHILD_ENABLEMENT +
				" in navigator extension: " +//$NON-NLS-1$
				configElement.getDeclaringExtension().getUniqueIdentifier());				
		} 
	}
}
