/*******************************************************************************
 * Copyright (c) 2003 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ltk.core.refactoring.participants;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

public abstract class ProcessorBasedRefactoring extends Refactoring {
	
	private RefactoringParticipant[] fDerivedParticipants;

	/**
	 * Creates a new processor based refactoring.
	 */
	protected ProcessorBasedRefactoring() {
	}
	
	/**
	 * Return the processor associated with this refactoring. The
	 * method must not return <code>null</code>.
	 * 
	 * @return the processor associated with this refactoring
	 */
	public abstract RefactoringProcessor getProcessor();
	
	protected abstract RefactoringParticipant[] getElementParticipants(boolean setArguments) throws CoreException;
	
	public boolean isAvailable() throws CoreException {
		return getProcessor().isApplicable();
	}
		
	public int getStyle() {
		return getProcessor().getStyle();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getName() {
		return getProcessor().getProcessorName();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException {
		RefactoringStatus result= new RefactoringStatus();
		CheckConditionsContext context= createCheckConditionsContext();
		pm.beginTask("", 5); //$NON-NLS-1$
		result.merge(getProcessor().checkInitialConditions(new SubProgressMonitor(pm, 3), context));
		if (result.hasFatalError()) {
			pm.done();
			return result;
		}
		RefactoringParticipant[] elementParticipants= getElementParticipants(false);
		IProgressMonitor sm= new SubProgressMonitor(pm, 1);
		sm.beginTask("", elementParticipants.length); //$NON-NLS-1$
		for (int i= 0; i < elementParticipants.length; i++) {
			result.merge(elementParticipants[i].checkInitialConditions(new SubProgressMonitor(sm, 1), context));
		}
		sm.done();
		if (result.hasFatalError()) {
			pm.done();
			return result;
		}
		result.merge(context.check(new SubProgressMonitor(pm, 1)));
		pm.done();
		return result;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm) throws CoreException {
		RefactoringStatus result= new RefactoringStatus();
		CheckConditionsContext context= createCheckConditionsContext();
		
		pm.beginTask("", 9); //$NON-NLS-1$
		
		result.merge(getProcessor().checkFinalConditions(new SubProgressMonitor(pm, 5), context));
		if (result.hasFatalError()) {
			pm.done();
			return result;
		}
		
		RefactoringParticipant[] elementParticipants= getElementParticipants(true);
		IProgressMonitor sm= new SubProgressMonitor(pm, 1);
		sm.beginTask("", elementParticipants.length); //$NON-NLS-1$
		for (int i= 0; i < elementParticipants.length; i++) {
			result.merge(elementParticipants[i].checkFinalConditions(new SubProgressMonitor(sm, 1), context));
		}
		sm.done();
		if (result.hasFatalError()) {
			pm.done();
			return result;
		}
		
		fDerivedParticipants= getProcessor().loadDerivedParticipants();
		sm= new SubProgressMonitor(pm, 1);
		sm.beginTask("", fDerivedParticipants.length); //$NON-NLS-1$
		for (int i= 0; i < fDerivedParticipants.length; i++) {
			result.merge(fDerivedParticipants[i].checkInitialConditions(new SubProgressMonitor(sm, 1), context));
		}
		sm.done();
		if (result.hasFatalError()) {
			pm.done();
			return result;
		}
		
		sm= new SubProgressMonitor(pm, 1);
		sm.beginTask("", fDerivedParticipants.length); //$NON-NLS-1$
		for (int i= 0; i < fDerivedParticipants.length; i++) {
			result.merge(fDerivedParticipants[i].checkFinalConditions(new SubProgressMonitor(sm, 1), context));
		}
		sm.done();
		if (result.hasFatalError()) {
			pm.done();
			return result;
		}
		
		result.merge(context.check(new SubProgressMonitor(pm, 1)));
		pm.done();
		return result;		
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Change createChange(IProgressMonitor pm) throws CoreException {
		RefactoringParticipant[] elementParticipants= getElementParticipants(false);
		
		pm.beginTask("", elementParticipants.length + fDerivedParticipants.length + 1); //$NON-NLS-1$
		List changes= new ArrayList();
		changes.add(getProcessor().createChange(new SubProgressMonitor(pm, 1)));
		
		for (int i= 0; i < elementParticipants.length; i++) {
			changes.add(elementParticipants[i].createChange(new SubProgressMonitor(pm, 1)));
		}
		for (int i= 0; i < fDerivedParticipants.length; i++) {
			changes.add(fDerivedParticipants[i].createChange(new SubProgressMonitor(pm, 1)));
		}
		CompositeChange result= new CompositeChange();
		result.addAll((Change[]) changes.toArray(new Change[changes.size()]));
		return result;
	}
	
	/**
	 * Adapts the refactoring to the given type. The adapter is resolved
	 * as follows:
	 * <ol>
	 *   <li>the refactoring itself is checked whether it is an instance
	 *       of the requested type.</li>
	 *   <li>its processor is checked whether it is an instance of the
	 *       requested type.</li>
	 *   <li>the request is delegated to the super class.</li>
	 * </ol>
	 * 
	 * @return the requested adapter or <code>null</code>if no adapter
	 *  exists. 
	 */
	public Object getAdapter(Class clazz) {
		if (clazz.isInstance(this))
			return this;
		if (clazz.isInstance(getProcessor()))
			return getProcessor();
		return super.getAdapter(clazz);
	}
	
	/* non java-doc
	 * for debugging only
	 */
	public String toString() {
		return getName();
	}
	
	//---- Helper methods ---------------------------------------------------------------------
	
	private CheckConditionsContext createCheckConditionsContext() throws CoreException {
		CheckConditionsContext result= new CheckConditionsContext();
		IConditionChecker checker= new ValidateEditChecker(null);
		result.add(checker);
		return result;
		
	}
}
