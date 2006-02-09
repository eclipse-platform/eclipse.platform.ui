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

package org.eclipse.jface.internal.provisional.action;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.ToolBarContributionItem;

/**
 * Extends <code>ToolBarContributionItem</code> to implement <code>IToolBarContributionItem</code>.
 * 
 * @since 3.2
 */
public class ToolBarContributionItem2 extends ToolBarContributionItem implements
		IToolBarContributionItem {

	/**
	 * 
	 */
	public ToolBarContributionItem2() {
		super();
	}

	/**
	 * @param toolBarManager
	 */
	public ToolBarContributionItem2(IToolBarManager toolBarManager) {
		super(toolBarManager);
	}

	/**
	 * @param toolBarManager
	 * @param id
	 */
	public ToolBarContributionItem2(IToolBarManager toolBarManager, String id) {
		super(toolBarManager, id);
	}

}
