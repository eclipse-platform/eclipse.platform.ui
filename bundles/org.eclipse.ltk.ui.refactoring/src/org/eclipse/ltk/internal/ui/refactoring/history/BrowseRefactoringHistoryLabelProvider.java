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
package org.eclipse.ltk.internal.ui.refactoring.history;

import org.eclipse.swt.graphics.Image;

import org.eclipse.core.runtime.Assert;

import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.jface.viewers.ILabelProvider;

import org.eclipse.ui.model.WorkbenchLabelProvider;

import org.eclipse.ltk.ui.refactoring.history.RefactoringHistoryControlConfiguration;
import org.eclipse.ltk.ui.refactoring.history.RefactoringHistoryLabelProvider;

/**
 * Label provider for the browse refactoring history control
 *
 * @since 3.2
 */
public final class BrowseRefactoringHistoryLabelProvider extends RefactoringHistoryLabelProvider {

	/** The delegate label provider */
	private final ILabelProvider fDelegateLabelProvider;

	/** The workbench label provider */
	private final ILabelProvider fWorkbenchLabelProvider= new WorkbenchLabelProvider();

	/**
	 * Creates a new browse refactoring history label provider.
	 *
	 * @param configuration
	 *            the refactoring history control configuration
	 */
	public BrowseRefactoringHistoryLabelProvider(final RefactoringHistoryControlConfiguration configuration) {
		super(configuration);
		Assert.isNotNull(configuration);
		fDelegateLabelProvider= configuration.getLabelProvider();
	}

	@Override
	public Image getImage(final Object element) {
		if (element instanceof RefactoringHistoryProject) {
			final RefactoringHistoryProject project= (RefactoringHistoryProject) element;
			return fWorkbenchLabelProvider.getImage(ResourcesPlugin.getWorkspace().getRoot().getProject(project.getProject()));
		}
		return fDelegateLabelProvider.getImage(element);
	}

	@Override
	public String getText(final Object element) {
		if (element instanceof RefactoringHistoryProject) {
			final RefactoringHistoryProject project= (RefactoringHistoryProject) element;
			return fWorkbenchLabelProvider.getText(ResourcesPlugin.getWorkspace().getRoot().getProject(project.getProject()));
		}
		return fDelegateLabelProvider.getText(element);
	}

	@Override
	public void dispose() {
		if (fDelegateLabelProvider != null) {
			fDelegateLabelProvider.dispose();
		}
		if (fWorkbenchLabelProvider != null) {
			fWorkbenchLabelProvider.dispose();
		}
		super.dispose();
	}
}
