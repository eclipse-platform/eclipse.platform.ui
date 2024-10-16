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
 ******************************************************************************/

package org.eclipse.ui.internal;

import java.util.List;
import org.eclipse.core.expressions.PropertyTester;

/**
 * <p>
 * Tests whether the object under test represents an MPart instance which is
 * tagged as being one which represents an Editor (rather than a View).
 * </p>
 *
 * <p>
 * This test is performed via a query of the tags associated with the MPart, and
 * checking whether this collection contains the
 * {@link org.eclipse.ui.internal.Workbench#EDITOR_TAG} identifier.
 * </p>
 *
 */
public class PartTaggedAsEditorPropertyTester extends PropertyTester {

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (receiver instanceof E4PartWrapper) {
			E4PartWrapper partWrapper = (E4PartWrapper) receiver;
			if (partWrapper.wrappedPart != null) {
				List<String> partTags = partWrapper.wrappedPart.getTags();
				return partTags == null || partTags.isEmpty() ? false
						: partTags.stream().anyMatch(tag -> Workbench.EDITOR_TAG.equals(tag));
			}
		}
		return false;
	}
}