/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.menus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;

/**
 * Handles the top level caching for 3.2 style trim
 * contributions.
 * 
 * @since 3.3
 * 
 */
public class TrimAdditionCacheEntry {
	private IConfigurationElement additionElement;
	private MenuLocationURI uri = null;
	private IMenuService menuSvc = null;

	// Caches

	/**
	 * Maps an IContributionItem to its corresponding IConfigurationElement
	 */
	Map iciToConfigElementMap = new HashMap();

	public TrimAdditionCacheEntry(IConfigurationElement element,
			MenuLocationURI uri, IMenuService service) {
		this.additionElement = element;
		this.uri = uri;

		menuSvc = service;
		
		if (additionElement == null || this.uri == null || menuSvc == null)
			menuSvc = null;
	}

	/**
	 * Populate the list
	 * 
	 * @param additions
	 */
	public void getContributionItems(List additions) {
		additions.clear();

	}
}
