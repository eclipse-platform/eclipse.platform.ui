/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.ui.refactoring.model;

import org.eclipse.team.core.diff.IThreeWayDiff;

import org.eclipse.swt.widgets.Composite;

import org.eclipse.core.runtime.Assert;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareUI;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIMessages;

/**
 * Compare viewer which displays a summary of a pending refactoring.
 *
 * @since 3.2
 */
public final class RefactoringDescriptorCompareViewer extends RefactoringDescriptorViewer {

	/** The compare configuration */
	private final CompareConfiguration fConfiguration;

	/**
	 * Creates a new refactoring descriptor compare viewer.
	 *
	 * @param parent
	 *            the parent control
	 * @param configuration
	 *            the compare configuration
	 * @param style
	 *            the viewer style
	 */
	public RefactoringDescriptorCompareViewer(final Composite parent, final CompareConfiguration configuration, final int style) {
		super(parent, style);
		Assert.isNotNull(configuration);
		fConfiguration= configuration;
		fBrowser.setData(CompareUI.COMPARE_VIEWER_TITLE, RefactoringUIMessages.RefactoringWizard_refactoring);
	}

	/**
	 * Returns the compare configuration.
	 *
	 * @return the compare configuration
	 */
	public CompareConfiguration getCompareConfiguration() {
		return fConfiguration;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setInput(final Object element) {
		if (element instanceof RefactoringDescriptorCompareInput) {
			final RefactoringDescriptorCompareInput input= (RefactoringDescriptorCompareInput) element;
			final RefactoringDescriptorProxy descriptor= input.getDescriptor();
			if (descriptor instanceof RefactoringDescriptorSynchronizationProxy) {
				final RefactoringDescriptorSynchronizationProxy proxy= (RefactoringDescriptorSynchronizationProxy) descriptor;
				if (proxy.getDirection() == IThreeWayDiff.INCOMING)
					fBrowser.setData(CompareUI.COMPARE_VIEWER_TITLE, ModelMessages.RefactoringDescriptorCompareInput_pending_refactoring);
				else
					fBrowser.setData(CompareUI.COMPARE_VIEWER_TITLE, ModelMessages.RefactoringDescriptorCompareInput_performed_refactoring);
			}
			super.setInput(descriptor);
		}
		super.setInput(element);
	}
}
