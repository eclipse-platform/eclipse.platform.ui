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
package org.eclipse.ltk.core.refactoring;

import org.eclipse.core.runtime.Assert;

/**
 * Descriptor object of a refactoring session. Refactoring session descriptors
 * encapsulate a series of refactoring descriptors and provide additional
 * information such as version information and comments.
 * <p>
 * Refactoring session descriptors are potentially heavyweight objects which
 * should not be held on to. Use refactoring descriptor handles
 * {@link RefactoringDescriptorProxy} to store refactoring information.
 * </p>
 * <p>
 * Note: this class is not indented to be subclassed outside the refactoring
 * framework.
 * </p>
 * <p>
 * Note: This API is considered experimental and may change in the near future.
 * </p>
 * 
 * @see RefactoringDescriptor
 * 
 * @since 3.2
 */
public class RefactoringSessionDescriptor {

	/** The comment associated with this refactoring, or <code>null</code> */
	private final String fComment;

	/** The refactoring descriptors in recorded order */
	private final RefactoringDescriptor[] fDescriptors;

	/** The non-empty version string */
	private final String fVersion;

	/**
	 * Creates a new refactoring session descriptor.
	 * 
	 * @param descriptors
	 *            the refactoring descriptors in executed order, or the empty
	 *            array
	 * @param version
	 *            the non-empty version tag
	 * @param comment
	 *            the comment associated with the refactoring session, or
	 *            <code>null</code> for no commment
	 */
	public RefactoringSessionDescriptor(final RefactoringDescriptor[] descriptors, final String version, final String comment) {
		Assert.isNotNull(descriptors);
		Assert.isTrue(version != null && !"".equals(version)); //$NON-NLS-1$
		fDescriptors= descriptors;
		fVersion= version;
		fComment= comment;
	}

	/**
	 * Returns the comment associated with this refactoring session.
	 * 
	 * @return the associated comment, or the empty string
	 */
	public final String getComment() {
		return (fComment != null) ? fComment : ""; //$NON-NLS-1$
	}

	/**
	 * Returns the refactoring descriptors of the refactorings in this session.
	 * 
	 * @return the array of refactoring descriptors in executed order, or the
	 *         empty array
	 */
	public final RefactoringDescriptor[] getRefactorings() {
		final RefactoringDescriptor[] result= new RefactoringDescriptor[fDescriptors.length];
		System.arraycopy(fDescriptors, 0, result, 0, result.length);
		return result;
	}

	/**
	 * Returns the version tag.
	 * 
	 * @return the version tag
	 */
	public final String getVersion() {
		return fVersion;
	}
}