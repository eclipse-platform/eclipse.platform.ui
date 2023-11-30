/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 *
 * @since 3.2
 */
public class ToolBarContributionItem2 extends ToolBarContributionItem {

	public ToolBarContributionItem2() {
		super();
	}

	public ToolBarContributionItem2(IToolBarManager toolBarManager) {
		super(toolBarManager);
	}

	public ToolBarContributionItem2(IToolBarManager toolBarManager, String id) {
		super(toolBarManager, id);
	}

}
