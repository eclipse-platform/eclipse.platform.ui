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
 * Interface for a refactoring contribution manager. This class provides a
 * facade to the
 * <code>org.eclipse.ltk.core.refactoring.refactoringContributions</code>
 * extension point.
 * <p>
 * Note: this interface is not intended to be implemented by clients.
 * </p>
 * <p>
 * Note: This API is considered experimental and may change in the near future.
 * </p>
 * 
 * @since 3.2
 */
public interface IRefactoringContributionManager {

	/**
	 * Creates the a refactoring arguments for the specified refactoring
	 * descriptor.
	 * <p>
	 * This method is used by the refactoring framework to create refactoring
	 * arguments for the refactoring instance represented by the specified
	 * descriptor. The result of this method is used as argument to initialize a
	 * refactoring using
	 * {@link IInitializableRefactoringComponent#initialize(RefactoringArguments)}.
	 * </p>
	 * 
	 * @param descriptor
	 *            the refactoring descriptor
	 * @return the refactoring arguments
	 */
	public RefactoringArguments createArguments(RefactoringDescriptor descriptor);

	/**
	 * Creates a new refactoring descriptor for the specified input data.
	 * <p>
	 * This method is used by the refactoring framework to create a
	 * language-specific refactoring descriptor representing the refactoring
	 * instance corresponding to the input arguments.
	 * </p>
	 * <p>
	 * If no refactoring contribution is available which is able to create a
	 * refactoring descriptor, a default refactoring descriptor is returned by
	 * the refactoring framework. The returned refactoring descriptor can be
	 * persisted and displayed in the user interface, but the creation of
	 * refactoring instances is not possible.
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
	public RefactoringDescriptor createDescriptor(String id, String project, String description, String comment, Map arguments, int flags);

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
	 *            the refactoring descriptor
	 * @return the refactoring, or <code>null</code> if no refactoring
	 *         contribution is available to create a refactoring from this
	 *         descriptor
	 * @throws CoreException
	 *             if the refactoring could not be created from the descriptor
	 */
	public Refactoring createRefactoring(RefactoringDescriptor descriptor) throws CoreException;

	/**
	 * Returns the refactoring contribution for the refactoring with the
	 * specified id.
	 * 
	 * @param id
	 *            the unique id of the refactoring
	 * @return the refactoring contribution, or <code>null</code> if no
	 *         refactoring contribution has been registered with the specified
	 *         id
	 */
	public RefactoringContribution getRefactoringContribution(String id);
}