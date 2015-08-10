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

import org.eclipse.jface.viewers.IFilter;
import org.eclipse.ui.tests.views.properties.tabbed.dynamic.filters.DynamicTestsBlackSectionFilter;
import org.eclipse.ui.tests.views.properties.tabbed.dynamic.sections.DynamicTestsBlackSection;
import org.eclipse.ui.views.properties.tabbed.AbstractSectionDescriptor;
import org.eclipse.ui.views.properties.tabbed.ISection;
import org.eclipse.ui.views.properties.tabbed.ITypeMapper;

/**
 * A section descriptor for the dynamic tests view.
 *
 * @author Anthony Hunter
 */
public class DynamicTestsBlackSectionDescriptor extends
		AbstractSectionDescriptor {

	/**
	 * Constructor for DynamicTestsBlackSectionDescriptor.
	 *
	 * @param typeMapper
	 *            the optional type mapper for the section.
	 */
	public DynamicTestsBlackSectionDescriptor(ITypeMapper typeMapper) {
		super(typeMapper);
	}

	@Override
	public IFilter getFilter() {
		return new DynamicTestsBlackSectionFilter();
	}

	@Override
	public String getId() {
		return "DynamicTestsBlackSection"; //$NON-NLS-1$
	}

	@Override
	public ISection getSectionClass() {
		return new DynamicTestsBlackSection();
	}

	@Override
	public String getTargetTab() {
		return "BlackTab"; //$NON-NLS-1$
	}

}
