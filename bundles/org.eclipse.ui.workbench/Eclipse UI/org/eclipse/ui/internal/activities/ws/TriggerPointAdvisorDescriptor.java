/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.activities.ws;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.activities.ITriggerPointAdvisor;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.internal.registry.RegistryReader;

/**
 * Descriptor for trigger point advisor extensions.
 * 
 * @since 3.1
 */
public final class TriggerPointAdvisorDescriptor {

	private String id;

	private IConfigurationElement element;

	/**
	 * Create a new instance of this class.
	 * 
	 * @param element
	 *            the configuration element
	 * @throws IllegalArgumentException
	 *             thrown if the element is missing an id attribute
	 */
	public TriggerPointAdvisorDescriptor(IConfigurationElement element)
			throws IllegalArgumentException {
		id = element.getAttribute(IWorkbenchRegistryConstants.ATT_ID);
		if (id == null
				|| RegistryReader.getClassValue(element,
						IWorkbenchRegistryConstants.ATT_CLASS) == null) {
			throw new IllegalArgumentException();
		}
		this.element = element;
	}

	/**
	 * Return the id.
	 * 
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Create the advisor for this descriptor.
	 * 
	 * @return the advisor
	 * @throws CoreException
	 *             thrown if there is an issue creating the advisor
	 */
	public ITriggerPointAdvisor createAdvisor() throws CoreException {
		return (ITriggerPointAdvisor) element
				.createExecutableExtension(IWorkbenchRegistryConstants.ATT_CLASS);
	}
}
