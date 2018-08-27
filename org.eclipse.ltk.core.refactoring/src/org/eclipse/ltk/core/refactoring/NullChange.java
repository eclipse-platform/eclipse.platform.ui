/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
package org.eclipse.ltk.core.refactoring;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.ltk.internal.core.refactoring.RefactoringCoreMessages;

/**
 * A refactoring change that does nothing. The reverse change of a
 * <code>NullChange</code> is a <code>NullChange</code>.
 * <p>
 * Note: this class is not intended to be extended by clients.
 * </p>
 *
 * @since 3.0
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class NullChange extends Change {

	private String fName;

	/**
	 * Creates a new <code>NullChange</code> with a default name.
	 */
	public NullChange() {
		this(RefactoringCoreMessages.NullChange_name);
	}

	/**
	 * Creates a new <code>NullChange</code> with the given name.
	 *
	 * @param name the human readable name of this change
	 */
	public NullChange(String name) {
		Assert.isNotNull(name);
		fName= name;
	}

	@Override
	public String getName() {
		return fName;
	}

	@Override
	public void initializeValidationData(IProgressMonitor pm) {
		// do nothing
	}

	@Override
	public RefactoringStatus isValid(IProgressMonitor pm) throws CoreException {
		return new RefactoringStatus();
	}

	@Override
	public Change perform(IProgressMonitor pm) throws CoreException {
		return new NullChange();
	}

	@Override
	public Object getModifiedElement() {
		return null;
	}
}
