/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.SubProgressMonitor;

/**
 * Abstract super class for all refactorings.
 * <p>
 * The life cycle of a refactoring is as follows:
 * <ul>
 *   <li>the refactoring gets created</li>
 *   <li>the refactoring is initialized with the elements to be refactored. It is
 *       up to a concrete refactoring implementation to provide corresponding API.
 *   <li>{@link #checkInitialConditions(IProgressMonitor)} is called. The method 
 *       can be called more than once.</li>
 *   <li>additional arguments are provided to perform the refactoring (for example
 *       the new name of a element in the case of a rename refactoring). It is up
 *       to a concrete implementation to provide corresponding API.
 *   <li>{@link #checkFinalConditions(IProgressMonitor)} is called. The method 
 *       can be called more than once. The method is not called if  
 *       {@link #checkInitialConditions(IProgressMonitor)} returns a refactoring
 *       status of severity {@link RefactoringStatus#FATAL}.</li>
 *   <li>{@link #createChange(IProgressMonitor)} is called. The method is only 
 *       called once and is not called if one of the condition checking methods
 *       return a refactoring status of severity {@link RefactoringStatus#FATAL}.
 *       </li>
 * </ul>
 * 
 * @since 3.0
 */
public abstract class Refactoring extends PlatformObject {

	/**
	 * Returns the refactoring's name.
	 * 
	 * @return the refactoring's human readable name. Must not be
	 *  <code>null</code>
	 */ 
	public abstract String getName();
	
	//---- Conditions ------------------------------------------------------------
	
	/**
	 * Checks all conditions. This implementation calls <code>checkInitialConditions</code>
	 * and <code>checkFinalConditions</code>. 
	 * <p>
	 * Subclasses may extend this method to provide additional condition checks.
	 * </p>
	 * 
	 * @see #checkInitialConditions(IProgressMonitor)
	 * @see #checkFinalConditions(IProgressMonitor)
	 */
	public RefactoringStatus checkAllConditions(IProgressMonitor pm) throws CoreException {
		pm.beginTask("", 11); //$NON-NLS-1$
		RefactoringStatus result= new RefactoringStatus();
		result.merge(checkInitialConditions(new SubProgressMonitor(pm, 1)));
		if (!result.hasFatalError())
			result.merge(checkFinalConditions(new SubProgressMonitor(pm, 10)));	
		pm.done();
		return result;
	}
	
	/**
	 * Checks some initial conditions based on the element to be refactored. The 
	 * method is typically called by the UI to perform a initial checks after an 
	 * action has been executed.
	 * <p>
	 * The refactoring is considered as not being executable if the returned status
	 * has the severity <code>RefactoringStatus#FATAL</code>.
	 * </p>
	 * This method can be called more than once.
	 * <p>
	 * 
	 * @param pm a progress monitor to report progress. Although availability checks 
	 *  are supposed to execute fast, there can be certain situations where progress
	 *  reporting is necessary. For example rebuilding a corrupted index may report
	 *  progress.
	 * 
	 * @return a refactoring status. If the status is <code>RefactoringStatus#FATAL</code>
	 *  the refactoring is considered as not being executable.
	 * 
	 * @throws CoreException if an exception occurred during initial condition checking
	 * 
	 * @see #checkFinalConditions(IProgressMonitor)
	 * @see RefactoringStatus#FATAL
	 */ 
	public abstract RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException;
	
	/**
	 * After <code>checkInitialConditions</code> has been performed and the user has 
	 * provided all input necessary to perform the refactoring this method is called 
	 * to check the remaining preconditions.
	 * <p>
	 * The refactoring is considered as not being executable if the returned status
	 * has the severity <code>RefactoringStatus#FATAL</code>.
	 * </p>
	 * This method can be called more than once.
	 * <p>
	 * 
	 * @param pm a progress monitor to report progress. Although availability checks 
	 *  are supposed to execute fast, there can be certain situations where progress
	 *  reporting is necessary. For example rebuilding a corrupted index may report
	 *  progress.
	 * 
	 * @return a refactoring status. If the status is <code>RefactoringStatus#FATAL</code>
	 *  the refactoring is considered as not being executable.
	 * 
	 * @throws CoreException if an exception occurred during final condition checking
	 * 
	 * @see #checkInitialConditions(IProgressMonitor)
	 * @see RefactoringStatus#FATAL
	 */ 		
	public abstract RefactoringStatus checkFinalConditions(IProgressMonitor pm) throws CoreException;
	
	//---- change creation ------------------------------------------------------
		
	/**
	 * Creates a {@link Change} object that performs the actual refactoring.
	 * 
	 * @param pm a progress monitor to report progress
	 * 
	 * @return the change representing the work space modifications of the
	 *  refactoring
	 * 
	 * @throws CoreException if an error occurred while creating the change 
	 */
	public abstract Change createChange(IProgressMonitor pm) throws CoreException;

	/**
	 * Returns the scheduling rule associated with this refactoring element.
	 * This scheduling rule should be used whenever one of the refactoring's
	 * method is executed inside a {@linkplain org.eclipse.core.resources.IWorkspaceRunnable
	 * work space runnable} or when the change created by this refactoring is
	 * performed.
	 * 
	 * @return the scheduling rule associated with this refactoring
	 */
	/* public abstract ISchedulingRule getSchedulingRule(); */
	
	/**
	 * {@inheritDoc}
	 */
	public Object getAdapter(Class adapter) {
		if (adapter.isInstance(this))
			return this;
		return super.getAdapter(adapter);
	}
	
	/* (non-Javadoc)
	 * for debugging only
	 */
	public String toString() {
		return getName();
	}
}
