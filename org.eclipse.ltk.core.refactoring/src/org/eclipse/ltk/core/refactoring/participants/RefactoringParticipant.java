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
package org.eclipse.ltk.core.refactoring.participants;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.internal.core.refactoring.Assert;
import org.eclipse.ltk.internal.core.refactoring.ParticipantDescriptor;

/**
 * A refactoring participant can participate in the condidtion checking and
 * change creation of a refactoring processor.
 * <p>
 * If the severity of the condition checking result is {@link RefactoringStatus#FATAL}
 * then the whole refactoring will not be carried out. 
 * </p>
 * <p>
 * The change created from a participant must not conflict with any changes
 * provided by other participants or the refactoring itself. If the change 
 * conflicts it will be ignored during change execution.
 * </p>
 * <p>
 * A refactoring participant can not assume that all resources are saved before any 
 * methods are called on it. Therefore a participant must be able to deal with unsaved
 * resources.
 * </p>
 * 
 * @since 3.0
 * @see org.eclipse.ltk.core.refactoring.participants.RefactoringProcessor
 */
public abstract class RefactoringParticipant {
	
	private RefactoringProcessor fProcessor;
	
	private ParticipantDescriptor fDescriptor;
	
	/**
	 * Returns the processor that is associated with this participant. 
	 * 
	 * @return the processor that is associated with this participant
	 */
	public RefactoringProcessor getProcessor() {
		return fProcessor;
	}
	
	/**
	 * Initializes the participant. This method is called by the framework when a
	 * participant gets instantiated. It is not intended to be called by normal
	 * clients.
	 * 
	 * @param processor the processor this participant is associated with
	 * @param element the element to be refactored
	 * @param arguments the refactoring arguments
	 * 
	 * @return <code>true</code> if the particpant could be initialized;
	 *  otherwise <code>false</code> is returned.
	 * 
	 * @see #initialize(Object)
	 */
	public boolean initialize(RefactoringProcessor processor, Object element, RefactoringArguments arguments) {
		Assert.isNotNull(processor);
		Assert.isNotNull(arguments);
		fProcessor= processor;
		initialize(arguments);
		return initialize(element);
	}
	
	/**
	 * Initialize the participant with the element to be refactored.
	 * If this method returns <code>false</code> then the framework
	 * will consider the participant as not initialized and the 
	 * participant will be dropped by the framework.
	 * 
	 * @param element the element to be refactored
	 * 
	 * @return <code>true</code> if the particpant could be initialized;
	 *  otherwise <code>false</code> is returned.
	 */
	protected abstract boolean initialize(Object element);
	
	/**
	 * Initializes the participant with the refactoring arguments
	 * 
	 * @param arguments the refactoring arguments
	 */
	protected abstract void initialize(RefactoringArguments arguments);
	
	/**
	 * Returns a human readable name of this participant.
	 * 
	 * @return a human readable name
	 */
	public abstract String getName();
	
	/**
	 * Checks the conditions of the refactoring participant. 
	 * <p>
	 * The refactoring is considered as not being executable if the returned status
	 * has the severity <code>RefactoringStatus#FATAL</code>.
	 * </p>
	 * <p>
	 * This method can be called more than once.
	 * </p>
	 * 
	 * @param pm a progress monitor to report progress
	 * 
	 * @return a refactoring status. If the status is <code>RefactoringStatus#FATAL</code>
	 *  the refactoring is considered as not being executable.
	 * 
	 * @throws CoreException if an exception occurred during final condition checking
	 * 
	 * @see org.eclipse.ltk.core.refactoring.Refactoring#checkInitialConditions(IProgressMonitor)
	 * @see RefactoringStatus#FATAL
	 */ 		
	public abstract RefactoringStatus checkConditions(IProgressMonitor pm, CheckConditionsContext context);
	
	/**
	 * Creates a {@link Change} object that contains the workspace modifications
	 * of this participant. The changes provided by a participant must not conflict
	 * with any changes provided by other participants ot by the refactoring itself.
	 * If the change conflicts it will be ignored during change execution.
	 * 
	 * @param pm a progress monitor to report progress
	 * 
	 * @return the change representing the workspace modifications
	 * 
	 * @throws CoreException if an error occurred while creating the change 
	 */
	public abstract Change createChange(IProgressMonitor pm) throws CoreException;

	/**
	 * TO be deleted before 3.0.
	 * FIXME
	 */
	private final void initialize(RefactoringProcessor processor, Object element) throws CoreException {
	}
	/**
	 * TO be deleted before 3.0.
	 * FIXME
	 */
	private final boolean isApplicable() throws CoreException {
		return false;
	}
	/**
	 * TO be deleted before 3.0.
	 * FIXME
	 */
	private final RefactoringStatus checkInitialConditions(IProgressMonitor pm, CheckConditionsContext context) throws CoreException {
		return null;
	}
	
	/**
	 * TO be deleted before 3.0.
	 * FIXME
	 */
	private final RefactoringStatus checkFinalConditions(IProgressMonitor pm, CheckConditionsContext context) throws CoreException {
		return null;
	}

	//---- helper method ----------------------------------------------------
	
	/* package */ void setDescriptor(ParticipantDescriptor descriptor) {
		Assert.isNotNull(descriptor);
		fDescriptor= descriptor;
	}
	
	/* package */ ParticipantDescriptor getDescriptor() {
		return fDescriptor;
	}
}
