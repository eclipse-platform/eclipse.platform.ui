/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
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

package org.eclipse.ui.tests.views.properties.tabbed.dynamic.views;

import org.eclipse.jface.viewers.TreeNode;
import org.eclipse.ui.tests.views.properties.tabbed.dynamic.model.DynamicTestsElement;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor;

/**
 * A nodes in the tree of the dynamic tests view.
 *
 * @author Anthony Hunter
 * @since 3.4
 */
public class DynamicTestsTreeNode extends TreeNode implements
		ITabbedPropertySheetPageContributor {

	public DynamicTestsTreeNode(Object object) {
		super(object);
	}

	@Override
	public String getContributorId() {
		return getDynamicTestsElement().getContributorId();
	}

	public DynamicTestsElement getDynamicTestsElement() {
		return (DynamicTestsElement) getValue();
	}
}
