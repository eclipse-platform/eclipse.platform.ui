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
public abstract class MoveProcessor extends RefactoringProcessor {

	private int fStyle;
	
	protected MoveProcessor() {
		fStyle= RefactoringStyles.NEEDS_PREVIEW;	
	}
	
	protected MoveProcessor(int style) {
		fStyle= style;	
	}

	public int getStyle() {
		return fStyle;
	}	
}
