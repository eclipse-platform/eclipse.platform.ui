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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.PlatformObject;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

public abstract class RefactoringProcessor extends PlatformObject {
	
	private static final RefactoringParticipant[] EMPTY_PARTICIPANT_ARRAY= new RefactoringParticipant[0];
	
	public abstract Object[] getElements();
	
	public abstract String getIdentifier();
	
	public abstract String getProcessorName();
	
	public abstract int getStyle();
	
	public abstract boolean isApplicable() throws CoreException;
	
	public abstract RefactoringStatus checkInitialConditions(IProgressMonitor pm, CheckConditionsContext context) throws CoreException;
	
	public abstract RefactoringStatus checkFinalConditions(IProgressMonitor pm, CheckConditionsContext context) throws CoreException;
	
	public abstract Change createChange(IProgressMonitor pm) throws CoreException;
	
	/**
	 * Returns an array of derived participants. There are two different flavours of 
	 * derived participants that should be added via this hook method:
	 * <ul>
	 *   <li>participants listening to changes of derived elements. For example if
	 *       a Java field gets renamed corresponding setter and getters methods are 
	 *       renamed as well. The setter and getter methods are considered as
	 *       derived elements and the corresponding participants should be added
	 *       via this hook.</li>
	 *   <li>participants listening to changes of a domain model different than the
	 *       one that gets manipulated, but changed as a "side effect" of the
	 *       refactoring. For example, renaming a package moves all its files to a
	 *       different folder. If the package contains a HTML file then the rename
	 *       package processor is supposed to load all move HTML file participants 
	 *       via this hook.</li>
	 * </ul>
	 * <p>
	 * Implementors are responsible to initialize the created participants with the
	 * right arguments. The method is called after {@link #checkFinalConditions} has
	 * been called on the processor itself.
	 * </p>
	 * <p>
	 * This default implementation returns an empty array.
	 * </p>
	 * 
	 * @return an array of derived participants
	 * 
	 * @throws CoreException if creating or loading of the participants failed
	 */
	public RefactoringParticipant[] loadDerivedParticipants() throws CoreException {
		return EMPTY_PARTICIPANT_ARRAY;
	}	
}
