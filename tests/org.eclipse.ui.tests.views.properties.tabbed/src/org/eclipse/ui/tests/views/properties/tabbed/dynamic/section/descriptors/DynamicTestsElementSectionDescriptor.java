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
package org.eclipse.ui.tests.views.properties.tabbed.dynamic.section.descriptors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.tests.views.properties.tabbed.dynamic.model.DynamicTestsElement;
import org.eclipse.ui.tests.views.properties.tabbed.dynamic.sections.DynamicTestsElementSection;
import org.eclipse.ui.views.properties.tabbed.AbstractSectionDescriptor;
import org.eclipse.ui.views.properties.tabbed.ISection;
import org.eclipse.ui.views.properties.tabbed.ITypeMapper;

/**
 * A section descriptor for the dynamic tests view.
 *
 * @author Anthony Hunter
 */
public class DynamicTestsElementSectionDescriptor extends
		AbstractSectionDescriptor {

	/**
	 * Constructor for DynamicTestsElementSectionDescriptor.
	 *
	 * @param typeMapper
	 *            the optional type mapper for the section.
	 */
	public DynamicTestsElementSectionDescriptor(ITypeMapper typeMapper) {
		super(typeMapper);
	}

	@Override
	public String getId() {
		return "DynamicTestsElementSection"; //$NON-NLS-1$
	}

	@Override
	public List getInputTypes() {
		List list = new ArrayList();
		list.add(DynamicTestsElement.class.getName());
		return list;
	}

	@Override
	public ISection getSectionClass() {
		return new DynamicTestsElementSection();
	}

	@Override
	public String getTargetTab() {
		return "ElementTab"; //$NON-NLS-1$
	}

}
