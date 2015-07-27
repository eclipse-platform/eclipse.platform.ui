/*******************************************************************************
 * Copyright (c) 2010, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.testing;

import org.eclipse.core.runtime.IConfigurationElement;

/**
 * Instances of this class describe a contribution of an element of a certain
 * type to the UI.
 *
 * @since 3.6
 */
public class ContributionInfo {

	private String bundleId;
	private String elementType;
	private IConfigurationElement configurationElement;

	/**
	 * Creates a new instance.
	 *
	 * @param bundleId
	 * @param elementType
	 *            a localized string describing the contribution (e.g., 'view',
	 *            'editor', 'preference page')
	 * @param configurationElement
	 *            an optional configuration element, or <code>null</code>.
	 */
	public ContributionInfo(String bundleId, String elementType,
			IConfigurationElement configurationElement) {
		super();
		this.bundleId = bundleId;
		this.elementType = elementType;
		this.configurationElement = configurationElement;
	}

	/**
	 * @return Returns the bundleId.
	 */
	public String getBundleId() {
		return bundleId;
	}

	/**
	 * @return Returns the elementType, a localized string describing the
	 *         contribution (e.g., 'view', 'editor', 'preference page').
	 */
	public String getElementType() {
		return elementType;
	}

	/**
	 * @return Returns the configurationElement or <code>null</code> if no
	 *         configuration element is available.
	 */
	public IConfigurationElement getConfigurationElement() {
		return configurationElement;
	}

}
