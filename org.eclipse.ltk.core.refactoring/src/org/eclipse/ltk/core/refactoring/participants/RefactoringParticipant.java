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
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.PlatformObject;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.internal.core.refactoring.Assert;
import org.eclipse.ltk.internal.core.refactoring.ParticipantDescriptor;

/**
 * A refactoring participant can participate in the condition checking and
 * change creation of a refactoring processor.
 * <p>
 * If the severity of the condition checking result is {@link RefactoringStatus#FATAL}
 * then the whole refactoring will not be carried out. 
 * </p>
 * <p>
 * The change created from a participant <em>MUST</em> not conflict with any changes
 * provided by other participants or the refactoring itself. To ensure this a participant
 * is only allowed to manipulate resources belonging to its domain. For example a rename type 
 * participant updating launch configuration is only allowed to update launch configurations.
 * It is not allowed to manipulate any Java resources or any other resources not belonging to
 * its domain. If a change conflicts with another change during execution then the participant
 * who created the change will be disabled for the rest of the eclipse session.
 * </p>
 * <p>
 * A refactoring participant can not assume that all resources are saved before any 
 * methods are called on it. Therefore a participant must be able to deal with unsaved
 * resources.
 * </p>
 * <p>
 * This class should be subclassed by clients wishing to provide special refactoring 
 * participants extension points.
 * </p>
 * 
 * @see org.eclipse.ltk.core.refactoring.participants.RefactoringProcessor
 * 
 * @since 3.0
 */
public abstract class RefactoringParticipant extends PlatformObject {
	
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
	 * @return <code>true</code> if the participant could be initialized;
	 *  otherwise <code>false</code> is returned. If <code>false</code> is
	 *  returned then the participant will not be added to the refactoring. 
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
	 * will consider the participant as not being initialized and the 
	 * participant will be dropped by the framework.
	 * 
	 * @param element the element to be refactored
	 * 
	 * @return <code>true</code> if the participant could be initialized;
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
	 * @param context a condition checking context to collect shared condition checks
	 * 
	 * @return a refactoring status. If the status is <code>RefactoringStatus#FATAL</code>
	 *  the refactoring is considered as not being executable.
	 * 
	 * @throws OperationCanceledException if the condition checking got cancelled
	 * 
	 * @see org.eclipse.ltk.core.refactoring.Refactoring#checkInitialConditions(IProgressMonitor)
	 * @see RefactoringStatus#FATAL
	 */ 		
	public abstract RefactoringStatus checkConditions(IProgressMonitor pm, CheckConditionsContext context) throws OperationCanceledException;
	
	/**
	 * Creates a {@link Change}object that contains the workspace modifications
	 * of this participant. The changes provided by a participant <em>must</em>
	 * not conflict with any change provided by other participants or by the
	 * refactoring itself.
	 * <p>
	 * If the change conflicts with any change provided by other participants or
	 * by the refactoring itself then change execution will fail and the
	 * participant will be disabled for the rest of the eclipse session.
	 * </p>
	 * <p>
	 * If an exception occurs while creating the change the refactoring can not
	 * be carried out and the participant will be disabled for the rest of the
	 * eclipse session.
	 * </p>
	 * 
	 * @param pm a progress monitor to report progress
	 * 
	 * @return the change representing the workspace modifications
	 * 
	 * @throws CoreException if an error occurred while creating the change
	 * 
	 * @throws OperationCanceledException if the condition checking got cancelled
	 */
	public abstract Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException;

	//---- helper method ----------------------------------------------------
	
	/* package */ void setDescriptor(ParticipantDescriptor descriptor) {
		Assert.isNotNull(descriptor);
		fDescriptor= descriptor;
	}
	
	/* package */ ParticipantDescriptor getDescriptor() {
		return fDescriptor;
	}
}
