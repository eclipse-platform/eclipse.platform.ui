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
	
	/**
	 * Create a new rename processor with the style bit set to
	 * <code>RefactoringStyles.NEEDS_PREVIEW</code> and <code>
	 * </code>
	 */
	protected RenameProcessor() {
		fStyle= RefactoringStyles.NEEDS_PREVIEW;	
	}
	
	protected RenameProcessor(int style) {
		fStyle= style;	
	}

	/**
	 * {@inheritDoc}
	 */
	public int getStyle() {
		return fStyle;
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
	public final RenameParticipant[] loadElementParticipants() throws CoreException {
		return null;
	}	
}
