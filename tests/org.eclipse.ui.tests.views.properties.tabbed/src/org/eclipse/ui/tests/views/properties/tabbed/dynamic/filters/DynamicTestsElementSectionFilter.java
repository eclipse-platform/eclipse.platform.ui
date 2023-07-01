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
 *******************************************************************************/
package org.eclipse.ui.tests.views.properties.tabbed.dynamic.filters;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.IFilter;
import org.eclipse.ui.tests.views.properties.tabbed.dynamic.model.DynamicTestsColor;
import org.eclipse.ui.tests.views.properties.tabbed.dynamic.model.DynamicTestsElement;
import org.eclipse.ui.tests.views.properties.tabbed.dynamic.model.DynamicTestsShape;
import org.eclipse.ui.tests.views.properties.tabbed.dynamic.views.DynamicTestsTreeNode;

/**
 * A section filter for the dynamic tests view.
 *
 * @author Anthony Hunter
 */
public class DynamicTestsElementSectionFilter implements IFilter {

	/**
	 * Determine if the filter applies to the provided color.
	 *
	 * @return true if the filter applies to the provided color.
	 */
	protected boolean appliesToColor(DynamicTestsColor color) {
		Assert.isNotNull(color);
		return true;
	}

	/**
	 * Determine if the filter applies to the provided shape.
	 *
	 * @return true if the filter applies to the provided shape.
	 */
	protected boolean appliesToShape(DynamicTestsShape shape) {
		Assert.isNotNull(shape);
		return true;
	}

	@Override
	public boolean select(Object object) {
		if (object instanceof DynamicTestsTreeNode dynamicNode) {
			DynamicTestsElement element = dynamicNode.getDynamicTestsElement();
			DynamicTestsColor color = (DynamicTestsColor) element
					.getPropertyValue(DynamicTestsElement.ID_COLOR);
			DynamicTestsShape shape = (DynamicTestsShape) element
					.getPropertyValue(DynamicTestsElement.ID_SHAPE);
			return (appliesToColor(color) && appliesToShape(shape));
		}
		return false;
	}
}
