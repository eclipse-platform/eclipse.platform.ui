/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
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
import org.eclipse.team.core.diff.ITwoWayDiff;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;

import org.eclipse.osgi.util.NLS;

/**
 * Diff node for an incoming refactoring.
 * 
 * @since 3.2
 */
public final class RefactoringDescriptorDiffNode implements IThreeWayDiff {

	/** The refactoring descriptor */
	private final RefactoringDescriptorProxy fDescriptor;

	/** The diff direction */
	private final int fDirection;

	/** The diff kind */
	private final int fKind;

	/**
	 * Creates a new refactoring descriptor diff node.
	 * 
	 * @param descriptor
	 *            the refactoring descriptor
	 * @param kind
	 * @param direction
	 */
	public RefactoringDescriptorDiffNode(final RefactoringDescriptorProxy descriptor, final int kind, final int direction) {
		Assert.isNotNull(descriptor);
		fDescriptor= descriptor;
		fKind= kind;
		fDirection= direction;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getDirection() {
		return fDirection;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getKind() {
		return fKind;
	}

	/**
	 * {@inheritDoc}
	 */
	public ITwoWayDiff getLocalChange() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public IPath getPath() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public ITwoWayDiff getRemoteChange() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public String toDiffString() {
		return NLS.bind(ModelMessages.RefactoringDescriptorDiffNode_diff_string, fDescriptor.getDescription());
	}
}