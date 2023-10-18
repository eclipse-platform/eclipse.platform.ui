/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
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
package org.eclipse.ltk.internal.ui.refactoring.model;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.viewers.Viewer;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.IViewerCreator;

/**
 * Compare viewer creator for refactoring descriptor compare viewers.
 *
 * @since 3.2
 */
public final class RefactoringDescriptorCompareViewerCreator implements IViewerCreator {

	@Override
	public Viewer createViewer(final Composite parent, final CompareConfiguration configuration) {
		return new RefactoringDescriptorCompareViewer(parent, configuration, SWT.NONE);
	}
}