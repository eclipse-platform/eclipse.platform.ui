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

import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;

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
	 *            the non-empty name of the project associated with this
	 *            refactoring, or <code>null</code>
	 * @param description
	 *            a non-empty human-readable description of the particular
	 *            refactoring instance
	 * @param comment
	 *            the comment associated with the refactoring, or
	 *            <code>null</code> for no commment
	 * @param arguments
	 *            the argument map (element type: &lt;String, String&gt;). The
	 *            keys of the arguments are required to be non-empty strings
	 *            which must not contain spaces. The values must be non-empty
	 *            strings
	 * @param flags
	 *            the flags of the refactoring descriptor
	 */
	public DefaultRefactoringDescriptor(final String id, final String project, final String description, final String comment, final Map arguments, final int flags) {
		super(id, project, description, comment, flags);
		Assert.isNotNull(arguments);
		fArguments= Collections.unmodifiableMap(new HashMap(arguments));
	}

	/**
	 * Returns the arguments describing the refactoring, in no particular order.
	 * 
	 * @return the argument map (element type: &lt;String, String&gt;). The
	 *         resulting map cannot be modified.
	 */
	public final Map getArguments() {
		return fArguments;
	}
}