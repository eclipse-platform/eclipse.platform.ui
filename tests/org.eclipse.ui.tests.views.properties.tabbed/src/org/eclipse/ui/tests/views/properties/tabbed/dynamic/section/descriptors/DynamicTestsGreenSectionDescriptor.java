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
import org.eclipse.ui.tests.views.properties.tabbed.dynamic.filters.DynamicTestsGreenSectionFilter;
import org.eclipse.ui.tests.views.properties.tabbed.dynamic.sections.DynamicTestsGreenSection;
import org.eclipse.ui.views.properties.tabbed.AbstractSectionDescriptor;
import org.eclipse.ui.views.properties.tabbed.ISection;
import org.eclipse.ui.views.properties.tabbed.ITypeMapper;

/**
 * A section descriptor for the dynamic tests view.
 *
 * @author Anthony Hunter
 */
public class DynamicTestsGreenSectionDescriptor extends
		AbstractSectionDescriptor {

	/**
	 * Constructor for DynamicTestsGreenSectionDescriptor.
	 *
	 * @param typeMapper
	 *            the optional type mapper for the section.
	 */
	public DynamicTestsGreenSectionDescriptor(ITypeMapper typeMapper) {
		super(typeMapper);
	}

	@Override
	public IFilter getFilter() {
		return new DynamicTestsGreenSectionFilter();
	}

	@Override
	public String getId() {
		return "DynamicTestsGreenSection"; //$NON-NLS-1$
	}

	@Override
	public ISection getSectionClass() {
		return new DynamicTestsGreenSection();
	}

	@Override
	public String getTargetTab() {
		return "ColorTab"; //$NON-NLS-1$
	}

}
