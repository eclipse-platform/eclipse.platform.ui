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

import org.eclipse.ui.tests.views.properties.tabbed.dynamic.section.descriptors.DynamicTestsElementSectionDescriptor;
import org.eclipse.ui.tests.views.properties.tabbed.dynamic.views.DynamicTestsTypeMapper;
import org.eclipse.ui.views.properties.tabbed.AbstractTabDescriptor;

/**
 * A tab descriptor for the dynamic tests view.
 *
 * @author Anthony Hunter
 */
public class DynamicTestsElementTabDescriptor extends AbstractTabDescriptor {

	public DynamicTestsElementTabDescriptor() {
		super();
		getSectionDescriptors().add(
				new DynamicTestsElementSectionDescriptor(
						new DynamicTestsTypeMapper()));
	}

	@Override
	public String getCategory() {
		return "default"; //$NON-NLS-1$
	}

	@Override
	public String getId() {
		return "ElementTab"; //$NON-NLS-1$
	}

	@Override
	public String getLabel() {
		return "Element"; //$NON-NLS-1$
	}

}
