/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
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

import org.eclipse.team.core.diff.IThreeWayDiff;

import org.eclipse.swt.graphics.Image;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.PlatformObject;

import org.eclipse.compare.CompareUI;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.compare.structuremergeviewer.ICompareInputChangeListener;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringPluginImages;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIMessages;

/**
 * Compare input which represents a pending refactoring.
 *
 * @since 3.2
 */
public final class RefactoringDescriptorCompareInput extends PlatformObject implements ICompareInput {

	/** Refactoring descriptor element */
	private final class RefactoringDescriptorElement implements ITypedElement {

		@Override
		public Image getImage() {
			return RefactoringDescriptorCompareInput.this.getImage();
		}

		@Override
		public String getName() {
			if (fDescriptor instanceof RefactoringDescriptorSynchronizationProxy) {
				final RefactoringDescriptorSynchronizationProxy proxy= (RefactoringDescriptorSynchronizationProxy) fDescriptor;
				if (proxy.getDirection() == IThreeWayDiff.INCOMING)
					return ModelMessages.RefactoringDescriptorCompareInput_pending_refactoring;
				else
					return ModelMessages.RefactoringDescriptorCompareInput_performed_refactoring;
			}
			return RefactoringUIMessages.RefactoringWizard_refactoring;
		}

		@Override
		public String getType() {
			return TYPE_REFACTORING_DESCRIPTOR;
		}
	}

	/** Has the image already been registered? */
	private static boolean fImageRegistered= false;

	/** The refactoring descriptor type */
	private static final String TYPE_REFACTORING_DESCRIPTOR= "refactoring_descriptor"; //$NON-NLS-1$

	/** The refactoring descriptor */
	private final RefactoringDescriptorProxy fDescriptor;

	/** The differencer kind */
	private final int fKind;

	/**
	 * Creates a new refactoring descriptor compare input.
	 *
	 * @param descriptor
	 *            the refactoring descriptor
	 * @param kind
	 *            the differencer kind
	 */
	public RefactoringDescriptorCompareInput(final RefactoringDescriptorProxy descriptor, final int kind) {
		Assert.isNotNull(descriptor);
		fDescriptor= descriptor;
		fKind= kind;
		if (!fImageRegistered) {
			CompareUI.registerImageDescriptor(TYPE_REFACTORING_DESCRIPTOR, RefactoringPluginImages.DESC_OBJS_REFACTORING);
			fImageRegistered= true;
		}
	}

	@Override
	public void addCompareInputChangeListener(final ICompareInputChangeListener listener) {
		// Do nothing
	}

	@Override
	public void copy(final boolean leftToRight) {
		// Do nothing
	}

	@Override
	public ITypedElement getAncestor() {
		return new RefactoringDescriptorElement();
	}

	/**
	 * Returns the refactoring descriptor.
	 *
	 * @return the refactoring descriptor
	 */
	public RefactoringDescriptorProxy getDescriptor() {
		return fDescriptor;
	}

	@Override
	public Image getImage() {
		return CompareUI.getImage(TYPE_REFACTORING_DESCRIPTOR);
	}

	@Override
	public int getKind() {
		return fKind;
	}

	@Override
	public ITypedElement getLeft() {
		return new RefactoringDescriptorElement();
	}

	@Override
	public String getName() {
		return fDescriptor.getDescription();
	}

	@Override
	public ITypedElement getRight() {
		return new RefactoringDescriptorElement();
	}

	@Override
	public void removeCompareInputChangeListener(final ICompareInputChangeListener listener) {
		// Do nothing
	}
}
