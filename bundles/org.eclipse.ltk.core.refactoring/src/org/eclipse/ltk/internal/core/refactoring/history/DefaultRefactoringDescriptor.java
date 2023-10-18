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
package org.eclipse.ltk.internal.core.refactoring.history;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCoreMessages;

/**
 * Default implementation of a refactoring descriptor.
 * This refactoring descriptor can only be used as temporary storage to transfer
 * refactoring descriptor data. {@link #createRefactoring(RefactoringStatus)} always returns null.
 *
 * @since 3.2
 */
public final class DefaultRefactoringDescriptor extends RefactoringDescriptor {

	/** The map of arguments (element type: &lt;String, String&gt;) */
	private final Map<String, String> fArguments;

	/**
	 * Creates a new default refactoring descriptor.
	 *
	 * @param id
	 *            the unique id of the refactoring
	 * @param project
	 *            the project name, or <code>null</code>
	 * @param description
	 *            the description
	 * @param comment
	 *            the comment, or <code>null</code>
	 * @param arguments
	 *            the argument map
	 * @param flags
	 *            the flags
	 */
	public DefaultRefactoringDescriptor(final String id, final String project, final String description, final String comment, final Map<String, String> arguments, final int flags) {
		super(id, project, description, comment, flags);
		Assert.isNotNull(arguments);
		fArguments= Collections.unmodifiableMap(new HashMap<>(arguments));
	}

	/**
	 * {@inheritDoc}
	 *
	 * @return always null
	 */
	@Override
	public Refactoring createRefactoring(final RefactoringStatus status) throws CoreException {
		status.merge(RefactoringStatus.createFatalErrorStatus(RefactoringCoreMessages.DefaultRefactoringDescriptor_cannot_create_refactoring));
		return null;
	}

	/**
	 * Returns the argument map
	 *
	 * @return the argument map.
	 */
	public Map<String, String> getArguments() {
		return fArguments;
	}
}
