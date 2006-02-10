/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

import org.eclipse.ltk.internal.core.refactoring.Messages;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCoreMessages;

/**
 * Default implementation of a refactoring descriptor.
 * 
 * @since 3.2
 */
public final class DefaultRefactoringDescriptor extends RefactoringDescriptor {

	/** The map of arguments (element type: &lt;String, String&gt;) */
	private final Map fArguments;

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
	public DefaultRefactoringDescriptor(final String id, final String project, final String description, final String comment, final Map arguments, final int flags) {
		super(id, project, description, comment, flags);
		Assert.isNotNull(arguments);
		fArguments= Collections.unmodifiableMap(new HashMap(arguments));
	}

	/**
	 * {@inheritDoc}
	 */
	public Refactoring createRefactoring() throws CoreException {
		return null;
	}

	/**
	 * Returns the argument map
	 * 
	 * @return the argument map.
	 */
	public final Map getArguments() {
		return fArguments;
	}

	/**
	 * {@inheritDoc}
	 */
	public RefactoringStatus initialize(final Refactoring refactoring) {
		return RefactoringStatus.createFatalErrorStatus(Messages.format(RefactoringCoreMessages.DefaultRefactoringDescriptor_initialization_error, getID()));
	}
}