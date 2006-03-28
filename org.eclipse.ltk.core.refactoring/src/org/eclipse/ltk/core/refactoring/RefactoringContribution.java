/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring;

import java.util.Collections;
import java.util.Map;

import org.eclipse.ltk.internal.core.refactoring.history.DefaultRefactoringDescriptor;

/**
 * Partial implementation of refactoring contribution objects which are capable
 * of creating a specific refactoring instance and associated refactoring
 * descriptors or arguments. Refactoring contributions are stateless objects.
 * They are instantiated on demand by the refactoring framework. It is not
 * guaranteed that the same refactoring contribution object will be used to
 * create the arguments for a refactoring and to create the refactoring itself.
 * 
 * @since 3.2
 */
public abstract class RefactoringContribution {

	/**
	 * Creates a new refactoring descriptor.
	 * <p>
	 * This method is used by the refactoring framework to create a
	 * language-specific refactoring descriptor representing the refactoring
	 * instance corresponding to the argument map. Implementations of this
	 * method must never return <code>null</code>.
	 * </p>
	 * 
	 * @param id
	 *            the unique id of the refactoring
	 * @param project
	 *            the non-empty name of the project associated with this
	 *            refactoring, or <code>null</code> for a workspace
	 *            refactoring
	 * @param description
	 *            a non-empty human-readable description of the particular
	 *            refactoring instance
	 * @param comment
	 *            the comment associated with the refactoring, or
	 *            <code>null</code> for no comment
	 * @param arguments
	 *            the argument map (element type: &lt;String, String&gt;). The
	 *            keys of the arguments are required to be non-empty strings
	 *            which must not contain spaces. The values must be non-empty
	 *            strings
	 * @param flags
	 *            the flags of the refactoring descriptor
	 * @return the refactoring descriptor
	 * 
	 * @see #retrieveArgumentMap(RefactoringDescriptor)
	 */
	public abstract RefactoringDescriptor createDescriptor(String id, String project, String description, String comment, Map arguments, int flags);

	/**
	 * Retrieves the argument map of the specified refactoring descriptor.
	 * <p>
	 * This method is used by the refactoring framework to obtain
	 * refactoring-specific arguments provided by the refactoring descriptor.
	 * These are the arguments which are specific to certain refactoring
	 * instances, and correspond to the argument map which has been passed to
	 * {@link #createDescriptor(String, String, String, String, Map, int)} upon
	 * creation of the refactoring descriptor.
	 * </p>
	 * <p>
	 * The returned argument map (element type: &lt;String, String&gt;) must
	 * satisfy the following conditions:
	 * <ul>
	 * <li>The keys of the arguments are required to be non-empty strings which
	 * must not contain spaces. </li>
	 * <li>The values must be non-empty</li>
	 * strings
	 * </ul>
	 * </p>
	 * <p>
	 * Subclasses must extend this method to provide more specific
	 * implementation in order to let the refactoring framework retrieve the
	 * argument map from language-specific refactoring descriptors.
	 * Implementations of this method must never return <code>null</code>.
	 * </p>
	 * 
	 * @param descriptor
	 *            the refactoring descriptor to retrieve its argument map
	 * @return the argument map of the specified refactoring descriptor
	 * 
	 * @see #createDescriptor(String, String, String, String, Map, int)
	 */
	public Map retrieveArgumentMap(final RefactoringDescriptor descriptor) {
		if (descriptor instanceof DefaultRefactoringDescriptor) {
			final DefaultRefactoringDescriptor extended= (DefaultRefactoringDescriptor) descriptor;
			return extended.getArguments();
		}
		return Collections.EMPTY_MAP;
	}
}
