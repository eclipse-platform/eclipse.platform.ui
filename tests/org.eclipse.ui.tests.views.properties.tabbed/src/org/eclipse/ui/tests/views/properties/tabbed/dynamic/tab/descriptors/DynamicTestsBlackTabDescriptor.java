/*******************************************************************************
 * Copyright (c) 2007, 2019 IBM Corporation and others.
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
 *     Alexander Fedorov <alexander.fedorov@arsysop.ru> - Bug 548799
 *******************************************************************************/
package org.eclipse.ui.tests.views.properties.tabbed.dynamic.tab.descriptors;

import org.eclipse.jface.resource.ResourceLocator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.tests.views.properties.tabbed.dynamic.section.descriptors.DynamicTestsBlackSectionDescriptor;
import org.eclipse.ui.tests.views.properties.tabbed.dynamic.views.DynamicTestsTypeMapper;
import org.eclipse.ui.views.properties.tabbed.AbstractTabDescriptor;

/**
 * A tab descriptor for the dynamic tests view.
 *
 * @author Anthony Hunter
 */
public class DynamicTestsBlackTabDescriptor extends AbstractTabDescriptor {
	private Image image;

	public DynamicTestsBlackTabDescriptor() {
		super();
		getSectionDescriptors().add(
				new DynamicTestsBlackSectionDescriptor(
						new DynamicTestsTypeMapper()));
	}

	@Override
	public String getAfterTab() {
		return "ColorTab"; //$NON-NLS-1$
	}

	@Override
	public String getCategory() {
		return "default"; //$NON-NLS-1$
	}

	@Override
	public String getId() {
		return "BlackTab"; //$NON-NLS-1$
	}

	@Override
	public Image getImage() {
		if (image == null) {
			String path = "icons/black_triangle.gif"; //$NON-NLS-1$
			ResourceLocator.imageDescriptorFromBundle(getClass(), path).ifPresent(d -> image = d.createImage());
		}
		return image;
	}

	@Override
	public String getLabel() {
		return "Black"; //$NON-NLS-1$
	}

	@Override
	public boolean isIndented() {
		return true;
	}

}
