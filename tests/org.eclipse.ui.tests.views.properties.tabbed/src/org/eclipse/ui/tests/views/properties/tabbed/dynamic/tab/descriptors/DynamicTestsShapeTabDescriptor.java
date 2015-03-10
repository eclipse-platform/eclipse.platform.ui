/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.views.properties.tabbed.dynamic.tab.descriptors;

import org.eclipse.ui.tests.views.properties.tabbed.dynamic.section.descriptors.DynamicTestsCircleSectionDescriptor;
import org.eclipse.ui.tests.views.properties.tabbed.dynamic.section.descriptors.DynamicTestsSquareSectionDescriptor;
import org.eclipse.ui.tests.views.properties.tabbed.dynamic.section.descriptors.DynamicTestsTriangleSectionDescriptor;
import org.eclipse.ui.tests.views.properties.tabbed.dynamic.views.DynamicTestsTypeMapper;
import org.eclipse.ui.views.properties.tabbed.AbstractTabDescriptor;

/**
 * A tab descriptor for the dynamic tests view.
 *
 * @author Anthony Hunter
 */
public class DynamicTestsShapeTabDescriptor extends AbstractTabDescriptor {

	public DynamicTestsShapeTabDescriptor() {
		super();
		getSectionDescriptors().add(
				new DynamicTestsCircleSectionDescriptor(
						new DynamicTestsTypeMapper()));
		getSectionDescriptors().add(
				new DynamicTestsSquareSectionDescriptor(
						new DynamicTestsTypeMapper()));
		getSectionDescriptors().add(
				new DynamicTestsTriangleSectionDescriptor(
						new DynamicTestsTypeMapper()));
	}

	public String getAfterTab() {
		return "ColorTab"; //$NON-NLS-1$
	}

	public String getCategory() {
		return "default"; //$NON-NLS-1$
	}

	public String getId() {
		return "ShapeTab"; //$NON-NLS-1$
	}

	public String getLabel() {
		return "Shape"; //$NON-NLS-1$
	}

}
