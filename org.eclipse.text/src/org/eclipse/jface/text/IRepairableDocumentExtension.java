/*******************************************************************************
 * Copyright (c) 2007, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text;


/**
 * Extension interface for {@link org.eclipse.jface.text.IRepairableDocument}.
 * <p>
 * Adds the ability to query whether the repairable document would have to be repaired after
 * replacing some text.
 * 
 * @see org.eclipse.jface.text.IRepairableDocument
 * @since 3.4
 */
public interface IRepairableDocumentExtension {

	/**
	 * Tells whether the line information of the document implementing this interface needs to be
	 * repaired after replacing the given text.
	 * 
	 * @param offset the document offset
	 * @param length the length of the specified range
	 * @param text the substitution text to check
	 * @return <code>true</code> if the line information must be repaired after replacing
	 * @throws BadLocationException if the offset is invalid in this document
	 * @see IRepairableDocument#repairLineInformation()
	 */
	boolean isLineInformationRepairNeeded(int offset, int length, String text) throws BadLocationException;
}
