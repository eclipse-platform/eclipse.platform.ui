/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

	public Class mapType(Object object) {
		if (object instanceof DynamicTestsTreeNode) {
			return ((DynamicTestsTreeNode) object).getDynamicTestsElement()
					.getClass();
		}
		return super.mapType(object);
	}

}
