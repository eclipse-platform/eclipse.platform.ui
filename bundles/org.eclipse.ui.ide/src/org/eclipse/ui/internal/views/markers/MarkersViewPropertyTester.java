/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
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

package org.eclipse.ui.internal.views.markers;

import org.eclipse.core.expressions.PropertyTester;

/**
 * ContentGeneratorPropertyTester is the property tester for what content
 * generator is being shown.
 *
 * @since 3.4
 */
public class MarkersViewPropertyTester extends PropertyTester {

	private static final String ATTRIBUTE_CONTENT_GENERATOR = "contentGenerator"; //$NON-NLS-1$


	private static final String ATTRIBUTE_HAS_FILTERS = "hasFilters"; //$NON-NLS-1$

	private static final String ATTRIBUTE_HAS_GROUPS = "hasGroups"; //$NON-NLS-1$

	private static final String ANY_CONTENT_GENERATOR = "any"; //$NON-NLS-1$

	/**
	 * Create a new instance of the receiver.
	 */
	public MarkersViewPropertyTester() {
	}

	@Override
	public boolean test(Object receiver, String property, Object[] args,
			Object expectedValue) {

		if (!(receiver instanceof ExtendedMarkersView))
			return false;

		ExtendedMarkersView view = (ExtendedMarkersView) receiver;

		if (property.equals(ATTRIBUTE_CONTENT_GENERATOR))
			return testContentGenerator(view, args);
		if (property.equals(ATTRIBUTE_HAS_FILTERS))
			return view.getAllFilters().size() > 0;
		if (property.equals(ATTRIBUTE_HAS_GROUPS))
			return view.getBuilder().getGenerator().getMarkerGroups().size() > 0;

		return false;
	}

	/**
	 * Test if the content generator in the args match the receiver.
	 *
	 * @return boolean
	 */
	private boolean testContentGenerator(ExtendedMarkersView view, Object[] args) {

		String currentGenerator = view.getBuilder().getGenerator().getId();
		for (Object arg : args) {
			if (arg.equals(currentGenerator))
				return true;

			// The value 'any' works for any content generator
			if (arg.equals(ANY_CONTENT_GENERATOR))
				return true;
		}
		return false;
	}
}
