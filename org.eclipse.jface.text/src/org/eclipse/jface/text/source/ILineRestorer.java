/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.source;

import org.eclipse.jface.text.BadLocationException;


/**
 * Defines the protocol a client can use to restore lines on a quick diffed document.
 * 
 * @since 3.0
 */
public interface ILineRestorer {
	
	/**
	 * Reverts a single changed line to its original state, not touching any lines that
	 * are deleted at its borders.
	 * 
	 * @param line the line number of the line to be restored.
	 * @throws BadLocationException if <code>line</code> is out of bounds.
	 */
	void revertLine(int line) throws BadLocationException;

	/**
	 * Reverts a block of modified / added lines to their original state, including any deleted
	 * lines inside the block or at its borders. A block is considered to be a range of modified
	 * (e.g. changed, or added) lines.
	 * 
	 * @param line any line in the block to be reverted.
	 * @throws BadLocationException if <code>line</code> is out of bounds.
	 */
	void revertBlock(int line) throws BadLocationException;

	/**
	 * Reverts a range of lines to their original state, including any deleted
	 * lines inside the block or at its borders.
	 * 
	 * @param line any line in the block to be reverted.
	 * @param nLines the number of lines to be reverted, must be &gt; 0.
	 * @throws BadLocationException if <code>line</code> is out of bounds.
	 */
	void revertSelection(int line, int nLines) throws BadLocationException;

	/**
	 * Restores the deleted lines after <code>line</code>.
	 * 
	 * @param line the deleted lines following this line number are restored.
	 * @return the number of restored lines.
	 * @throws BadLocationException if <code>line</code> is out of bounds.
	 */
	int restoreAfterLine(int line) throws BadLocationException;
}
