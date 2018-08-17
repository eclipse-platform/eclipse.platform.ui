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

import org.eclipse.ui.tests.views.properties.tabbed.dynamic.model.DynamicTestsShape;

/**
 * A section filter for the dynamic tests view.
 *
 * @author Anthony Hunter
 */
public class DynamicTestsSquareSectionFilter extends
		DynamicTestsElementSectionFilter {

	@Override
	protected boolean appliesToShape(DynamicTestsShape shape) {
		return DynamicTestsShape.SQUARE.equals(shape);
	}
}
