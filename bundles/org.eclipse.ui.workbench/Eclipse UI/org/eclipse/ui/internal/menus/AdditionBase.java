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

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;

/**
 * Base class for all new-style menu additions
 * 
 * @since 3.3
 *
 */
public abstract class AdditionBase {

	protected IConfigurationElement element;

	public AdditionBase(IConfigurationElement element) {
		this.element = element;
	}
	
	public String getId() {
		return element.getAttribute(IWorkbenchRegistryConstants.ATT_ID);
	}
	
	public abstract IContributionItem getContributionItem();
}
