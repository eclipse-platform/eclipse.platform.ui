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

import java.util.Map;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.ltk.core.refactoring.participants.RefactoringArguments;

/**
 * Partial implementation of refactoring contribution objects which are capable
 * of creating a specific refactoring instance and associated refactoring
 * descriptors or arguments. Refactoring contributions are stateless objects.
 * They are instantiated on demand by the refactoring framework. It is not
 * guaranteed that the same refactoring contribution object will be used to
 * create the arguments for a refactoring and to create the refactoring itself.
 * <p>
 * Note: This API is considered experimental and may change in the near future.
 * </p>
 * 
 * @since 3.2
 */
public abstract class RefactoringContribution {

	/**
	 * Creates the a refactoring arguments for the specified refactoring
	 * descriptor.
	 * <p>
	 * This method is used by the refactoring framework to create refactoring
	 * arguments for the refactoring instance represented by the specified
	 * descriptor. The result of this method is used as argument to initialize a
	 * refactoring using
	 * {@link IInitializableRefactoringComponent#initialize(RefactoringArguments)}.
	 * Implementations of this method must never return <code>null</code>.
	 * </p>
	 * 
	 * @param descriptor
	 *            the refactoring descriptor to create refactoring arguments for
	 * @return the refactoring arguments
	 */
	public abstract RefactoringArguments createArguments(RefactoringDescriptor descriptor);

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
	 *            <code>null</code> for no commment
	 * @param arguments
	 *            the argument map (element type: &lt;String, String&gt;). The
	 *            keys of the arguments are required to be non-empty strings
	 *            which must not contain spaces. The values must be non-empty
	 *            strings
	 * @param flags
	 *            the flags of the refactoring descriptor
	 * @return the refactoring descriptor
	 */
	public abstract RefactoringDescriptor createDescriptor(String id, String project, String description, String comment, Map arguments, int flags);

	/**
	 * Creates the a refactoring instance for the specified refactoring
	 * descriptor.
	 * <p>
	 * This method is used by the refactoring framework to instantiate a
	 * refactoring from a refactoring descriptor, in order to apply it later on
	 * a local or remote workspace.
	 * </p>
	 * 
	 * @param descriptor
	 *            the refactoring descriptor to create a refactoring from
	 * @return the refactoring, or <code>null</code> if no refactoring
	 *         contribution is available to create a refactoring from this
	 *         descriptor
	 * @throws CoreException
	 *             if the refactoring could not be created from the descriptor.
	 */
	public abstract Refactoring createRefactoring(RefactoringDescriptor descriptor) throws CoreException;

	/**
	 * Retrieves the argument map of the specified refactoring descriptor.
	 * <p>
	 * This method is used by the refactoring framework to obtain
	 * refactoring-specific arguments provided by the refactoring descriptor.
	 * Implementations of this method must never return <code>null</code>.
	 * </p>
	 * 
	 * @param descriptor
	 *            the refactoring descriptor to retrieve its argument map
	 * @return the argument map of the specified refactoring descriptor
	 * 
	 * @see #createDescriptor(String, String, String, String, Map, int)
	 */
	public abstract Map retrieveArguments(RefactoringDescriptor descriptor);
}