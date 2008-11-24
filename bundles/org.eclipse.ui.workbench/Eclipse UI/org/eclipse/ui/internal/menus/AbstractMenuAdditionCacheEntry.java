/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.menus;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.menus.AbstractContributionFactory;

/**
 * Base class for factories derived from extensions.
 *
 * @since 3.5
 */
public abstract class AbstractMenuAdditionCacheEntry extends
		AbstractContributionFactory {

	private IConfigurationElement additionElement;
	
	/**
     * Create a new instance of this class.
     *
	 * @param location the location URI
	 * @param namespace the namespace 
	 * @param element the declaring configuration element
	 */
	public AbstractMenuAdditionCacheEntry(String location, String namespace, IConfigurationElement element) {
		super(location, namespace);
		this.additionElement = element;
	}

	/**  
     * Return the configuration element.
     *
	 * @return the configuration element
	 */
	public final IConfigurationElement getConfigElement() {
		return additionElement;
	}	
}