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

/**
 * A rename processor is a special refactoring processor to support 
 * participating in rename refactorings. A rename processor is responsible
 * for actual renaming the element to be refactored. Additionally the
 * processor can update reference which are part of the same domain as the 
 * element to be renamed. For example a processor to rename a Java field 
 * can also update all references to that field found in Java files.
 * <p>
 * A rename processor is also responsible to load participants that want
 * to participate in a rename refactoring.
 * </p>
 * 
 * @since 3.0
 */
public abstract class RenameProcessor extends RefactoringProcessor {

	private int fStyle;
	private SharableParticipants fSharedParticipants= new SharableParticipants();
	
	protected RenameProcessor() {
		fStyle= RefactoringStyles.NEEDS_PREVIEW;	
	}
	
	protected RenameProcessor(int style) {
		fStyle= style;	
	}

	public int getStyle() {
		return fStyle;
	}
	
	/**
	 * Forwards the current rename arguments to the passed participant.
	 *  
	 * @param participant the participant to set the arguments to
	 * 
	 * @throws CoreException if the arguments can't be set
	 */
	public void setArgumentsTo(RenameParticipant participant) throws CoreException {
		participant.setArguments(getArguments());
	}
	
	/**
	 * Returns the participants that participate in the rename of the element. The
	 * method is called after {@link #checkInitialConditions} has been called on the 
	 * processor itself. 
	 * 
	 * The arguments are set to the participants by the processor via the call 
	 * {@link RenameParticipant#setArguments(RenameArguments)}. They are set 
	 * before {@link #checkFinalConditions}is called on the participants. 
	 * 
	 * @return an array of rename participants
	 * 
	 * @throws CoreException if creating or loading of the participants failed
	 */
	public abstract RenameParticipant[] loadElementParticipants() throws CoreException;
	
	/**
	 * Returns the shared participants. ????
	 * 
	 * @return
	 */
	protected SharableParticipants getSharedParticipants() {
		return fSharedParticipants;
	}
	
	/**
	 * Returns the arguments of the rename.
	 * 
	 * @return the rename arguments
	 */
	protected RenameArguments getArguments() {
		return new RenameArguments(getNewElementName(), getUpdateReferences());
	}
	
	/**
	 * Returns the new name of the element to be renamed. The 
	 * method must not return <code>null</code>.
	 * 
	 * @return the new element name.
	 */
	protected abstract String getNewElementName();
	
	/**
	 * Returns whether reference updating is requested or not.
	 * 
	 * @return whether reference updating is requested or not
	 */
	protected abstract boolean getUpdateReferences();
}
