/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.source;

import org.eclipse.jface.text.BadLocationException;


/**
 * Protocol that allows direct access to line information. Usually, implementations will also
 * implement <code>IAnnotationModel</code>, which only allows <code>Iterator</code> based access
 * to annotations.
 * <p>
 * <code>ILineDiffer</code> also allows to revert any lines to their original
 * contents as defined by the quick diff reference used by the receiver.
 * </p>
 * <p>
 * This interface may be implemented by clients.
 * </p>
 * <p>
 * In order to provide backward compatibility for clients of <code>ILineDiffer</code>, extension
 * interfaces are used to provide a means of evolution. The following extension interface
 * exists:
 * <ul>
 * <li> {@link ILineDifferExtension} (since version 3.1): introducing the concept
 *      suspending and resuming an <code>ILineDiffer</code>.</li>
 * <li> {@link ILineDifferExtension2} (since version 3.3): allowing to query the suspension state
 * of an <code>ILineDiffer</code>.</li>
 * </ul>
 * </p>
 *
 * @since 3.0
 */
public interface ILineDiffer {

	/**
	 * Determines the line state for line <code>line</code> in the targeted document.
	 *
	 * @param line the line to get diff information for
	 * @return the line information object for <code>line</code> or <code>null</code> if none
	 */
	ILineDiffInfo getLineInfo(int line);

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
