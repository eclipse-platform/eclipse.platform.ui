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
package org.eclipse.ltk.internal.core.refactoring.history;

import org.eclipse.core.runtime.CoreException;

/**
 * Interface for objects which transform refactoring sessions and associated
 * refactorings.
 * 
 * @since 3.2
 */
public interface IRefactoringSessionTransformer {

	/**
	 * Begins the tranformation of a refactoring specified by the given
	 * arguments.
	 * <p>
	 * Calls to
	 * {@link IRefactoringSessionTransformer#beginRefactoring(String, long, String, String, String)}
	 * must be balanced with calls to
	 * {@link IRefactoringSessionTransformer#endRefactoring()}. If the
	 * transformer is already processing a refactoring, nothing happens.
	 * </p>
	 * 
	 * @param id
	 *            the unique identifier of the refactoring
	 * @param stamp
	 *            the time stamp of the refactoring, or <code>-1</code>
	 * @param project
	 *            the non-empty name of the project this refactoring is
	 *            associated with, or <code>null</code>
	 * @param description
	 *            a human-readable description of the refactoring
	 * @param comment
	 *            the comment associated with the refactoring, or
	 *            <code>null</code>
	 * @throws CoreException
	 *             if an error occurs while creating a new refactoring
	 */
	public void beginRefactoring(String id, long stamp, String project, String description, String comment) throws CoreException;

	/**
	 * Begins the transformation of a refactoring session.
	 * <p>
	 * Calls to {@link IRefactoringSessionTransformer#beginSession(String)} must
	 * be balanced with calls to
	 * {@link IRefactoringSessionTransformer#endSession()}. If the transformer
	 * is already processing a session, nothing happens.
	 * </p>
	 * 
	 * @param comment
	 *            the comment associated with the refactoring session, or
	 *            <code>null</code>
	 * @throws CoreException
	 *             if an error occurs while creating a new session
	 */
	public void beginSession(String comment) throws CoreException;

	/**
	 * Creates a refactoring argument with the specified name and value.
	 * <p>
	 * If no refactoring is currently processed, this call has no effect.
	 * </p>
	 * 
	 * @param name
	 *            the non-empty name of the argument
	 * @param value
	 *            the non-empty value of the argument
	 * 
	 * @throws CoreException
	 *             if an error occurs while creating a new argument
	 */
	public void createArgument(String name, String value) throws CoreException;

	/**
	 * Ends the tranformation of the current refactoring.
	 * <p>
	 * If no refactoring is currently processed, this call has no effect.
	 * </p>
	 */
	public void endRefactoring();

	/**
	 * Ends the transformation of the current refactoring session.
	 * <p>
	 * If no refactoring session is currently processed, this call has no
	 * effect.
	 * </p>
	 */
	public void endSession();

	/**
	 * Returns the result of the transformation process.
	 * <p>
	 * This method must only be called once during the life time of a
	 * transformer.
	 * </p>
	 * 
	 * @return the object representing the refactoring session, or
	 *         <code>null</code> if no session has been transformed
	 */
	public Object getResult();
}
