/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.commands.operations;

/**
 * IContextReplacingOperation defines an interface for undoable operations that
 * can replace one undo context with another undo context. It is used by
 * operations, such as composite operations, where removing and adding an undo
 * context would not have the same semantic as replacing one undo context with
 * another.
 *
 * @since 3.2
 *
 */
public interface IContextReplacingOperation {

	/**
	 * Replace the undo context of the receiver with the provided replacement
	 * undo context.
	 * <p>
	 * This message has no effect if the original undo context is not present in
	 * the receiver.
	 *
	 * @param original the undo context which is to be replaced
	 * @param replacement the undo context which is replacing the original
	 *
	 */
	void replaceContext(IUndoContext original, IUndoContext replacement);
}
