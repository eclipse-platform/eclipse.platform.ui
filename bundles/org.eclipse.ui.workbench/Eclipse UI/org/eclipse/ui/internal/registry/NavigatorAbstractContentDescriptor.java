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

import java.util.ArrayList;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.WorkbenchException;

/**
 * 
 * @since 3.0
 */
public class NavigatorAbstractContentDescriptor {
	public static final String ATT_ID = "id"; //$NON-NLS-1$
	public static final String ATT_NAME = "name"; //$NON-NLS-1$	
	public static final String ATT_CLASS = "class"; //$NON-NLS-1$	

	private String id;	
	private String name;
	private String className;
	private IConfigurationElement configElement;
	
	/**
	 * Creates a descriptor from a configuration element.
	 * 
	 * @param configElement configuration element to create a descriptor from
	 */
	public NavigatorAbstractContentDescriptor(IConfigurationElement configElement) throws WorkbenchException {
		super();
		this.configElement = configElement;
	}
	/**
	 */
	public String getId() {
		return id;
	}
	public String getClassName() {
		return className;
	}
	public IConfigurationElement getConfigurationElement() {
		return configElement;
	}
	/**
	 */
	public String getName() {
		return name;
	}
	protected void readConfigElement() throws WorkbenchException {
		id = configElement.getAttribute(ATT_ID);
		name = configElement.getAttribute(ATT_NAME);
		className = configElement.getAttribute(ATT_CLASS);

		if (id == null) {
			throw new WorkbenchException("Missing attribute: " +//$NON-NLS-1$
				ATT_ID +
				" in navigator extension: " +//$NON-NLS-1$
				configElement.getDeclaringExtension().getUniqueIdentifier());				
		}
		if (className == null) {
			throw new WorkbenchException("Missing attribute: " +//$NON-NLS-1$
				ATT_CLASS +
				" in navigator extension: " +//$NON-NLS-1$
				configElement.getDeclaringExtension().getUniqueIdentifier());				
		}
	}
	protected ArrayList delegateDescriptors = new ArrayList();
	protected void addDelegateDescriptor(NavigatorContentDescriptor descriptor) {
		delegateDescriptors.add(descriptor);
	}
	/**
	 */
	protected NavigatorAbstractContentDescriptor findDescriptor(String contentProviderId) {
		for (int i=0; i<delegateDescriptors.size(); i++) {
			NavigatorContentDescriptor descriptor = (NavigatorContentDescriptor)delegateDescriptors.get(i);
			if (descriptor.getId().equals(contentProviderId)) return descriptor;
		}
		return null;
	}
	protected ArrayList getDelegateDescriptors() {
		return delegateDescriptors;
	}
}
