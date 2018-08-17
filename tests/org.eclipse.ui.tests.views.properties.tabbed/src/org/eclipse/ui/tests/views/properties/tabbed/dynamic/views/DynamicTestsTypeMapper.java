/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
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
package org.eclipse.ui.tests.views.properties.tabbed.dynamic.views;

import org.eclipse.ui.views.properties.tabbed.AbstractTypeMapper;

/**
 * A type mapper for nodes in the dynamic tests view.
 *
 * @author Anthony Hunter
 */
public class DynamicTestsTypeMapper extends AbstractTypeMapper {

	@Override
	public Class mapType(Object object) {
		if (object instanceof DynamicTestsTreeNode) {
			return ((DynamicTestsTreeNode) object).getDynamicTestsElement()
					.getClass();
		}
		return super.mapType(object);
	}

}
