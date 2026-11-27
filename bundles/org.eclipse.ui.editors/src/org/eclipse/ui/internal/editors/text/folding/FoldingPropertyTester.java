/*******************************************************************************
 * Copyright (c) 2026 vogella GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.ui.internal.editors.text.folding;

import org.eclipse.core.expressions.PropertyTester;

import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.source.projection.ProjectionViewer;

import org.eclipse.ui.IWorkbenchPart;

/**
 * Tests whether a workbench part shows its text in a viewer that supports folding.
 */
public class FoldingPropertyTester extends PropertyTester {

	private static final String FOLDING_SUPPORTED= "foldingSupported"; //$NON-NLS-1$

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (FOLDING_SUPPORTED.equals(property) && receiver instanceof IWorkbenchPart part) {
			return getProjectionViewer(part) != null;
		}
		return false;
	}

	static ProjectionViewer getProjectionViewer(IWorkbenchPart part) {
		if (part != null && part.getAdapter(ITextOperationTarget.class) instanceof ProjectionViewer viewer
				&& viewer.canDoOperation(ProjectionViewer.TOGGLE)) {
			return viewer;
		}
		return null;
	}
}
