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

import java.util.List;

import org.eclipse.core.expressions.Expression;
import org.eclipse.jface.action.IContributionItem;

/**
 * @since 3.3
 *
 */
public abstract class MenuCacheEntry {
	private MenuLocationURI uri = null;


	/**
	 * @return Returns the uri.
	 */
	public MenuLocationURI getUri() {
		return uri;
	}
	
	void setUri(MenuLocationURI u) {
		uri = u;
	}
	
	public abstract Expression getVisibleWhenForItem(IContributionItem item);
	
	public abstract void getContributionItems(List additions);
	
	public abstract void generateSubCaches();
}
