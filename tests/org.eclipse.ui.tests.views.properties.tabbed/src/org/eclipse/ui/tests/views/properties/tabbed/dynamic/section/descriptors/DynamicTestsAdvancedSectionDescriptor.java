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
import org.eclipse.ui.views.properties.tabbed.AbstractSectionDescriptor;
import org.eclipse.ui.views.properties.tabbed.AdvancedPropertySection;
import org.eclipse.ui.views.properties.tabbed.ISection;
import org.eclipse.ui.views.properties.tabbed.ITypeMapper;

/**
 * A section descriptor for the dynamic tests view.
 * 
 * @author Anthony Hunter
 */
public class DynamicTestsAdvancedSectionDescriptor extends
		AbstractSectionDescriptor {

	/**
	 * Constructor for DynamicTestsAdvancedSectionDescriptor.
	 * 
	 * @param typeMapper
	 *            the optional type mapper for the section.
	 */
	public DynamicTestsAdvancedSectionDescriptor(ITypeMapper typeMapper) {
		super(typeMapper);
	}

	public int getEnablesFor() {
		return 1;
	}

	public String getId() {
		return "AdvancedSection"; //$NON-NLS-1$
	}

	public List getInputTypes() {
		List list = new ArrayList();
		list.add(DynamicTestsElement.class.getName());
		return list;
	}

	public ISection getSectionClass() {
		return new AdvancedPropertySection();
	}

	public String getTargetTab() {
		return "AdvancedTab"; //$NON-NLS-1$
	}

}
