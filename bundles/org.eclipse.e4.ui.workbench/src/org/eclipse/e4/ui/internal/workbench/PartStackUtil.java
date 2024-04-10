/*******************************************************************************
 * Copyright (c) 2024 Vector Informatik GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.e4.ui.internal.workbench;

import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;

public final class PartStackUtil {
	private static final String PRIMARY_DATA_STACK_ID = "org.eclipse.e4.primaryDataStack"; //$NON-NLS-1$
	public static final String EDITOR_STACK_TAG = "EditorStack"; //$NON-NLS-1$

	private PartStackUtil() {
	}

	/**
	 * Marks the given part stack as the primary data stack, setting a proper
	 * element ID and adding an {@link #EDITOR_STACK_TAG} tag.
	 *
	 * @param partStack the part stack to make the primary data stack
	 */
	public static void initializeAsPrimaryDataStack(MPartStack partStack) {
		makeEditorStack(partStack);
		partStack.getTags().add(PRIMARY_DATA_STACK_ID);
		partStack.setElementId(PRIMARY_DATA_STACK_ID);
	}

	/**
	 * @param element the element to check for being a primary data stack
	 * @return whether the given element is marked as the primary data stack
	 */
	public static boolean isPrimaryDataStack(MApplicationElement element) {
		return element instanceof MPartStack && PRIMARY_DATA_STACK_ID.equals(element.getElementId());
	}

	/**
	 * @param element the element to check for being an editor stack
	 * @return whether the given element is marked as an editor stack
	 */
	public static boolean isEditorStack(MApplicationElement element) {
		return element instanceof MPartStack && element.getTags().contains(EDITOR_STACK_TAG);
	}

	/**
	 * Marks the given part stack as an editor stack. In consequence calling
	 * {{@link #isEditorStack(MApplicationElement)} for the element will return
	 * {@code true}.
	 *
	 * @param partStack the part stack to mark as an editor stack
	 */
	public static void makeEditorStack(MPartStack partStack) {
		if (!partStack.getTags().contains(EDITOR_STACK_TAG)) {
			partStack.getTags().add(EDITOR_STACK_TAG);
		}
	}

}
