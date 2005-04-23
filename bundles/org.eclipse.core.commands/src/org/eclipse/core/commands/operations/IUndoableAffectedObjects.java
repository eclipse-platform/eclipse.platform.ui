/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
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
 * <p>
 * IUndoableAffectedObjects defines an interface for undoable
 * operations that modify one or more elements in a model.  The interface
 * allows clients to determine which objects are affected by an undoable
 * operation.
 * </p>
 * <p>
 * This interface is intended to be used by legacy frameworks that are adapting
 * their original undo and redo support to this framework.   In some cases,
 * clients implement special prompting or warning when undoing an operation affects
 * certain elements.
 * </p>
 * 
 * @since 3.1
 * 
 */
public interface IUndoableAffectedObjects {

	/**
	 * <p>
	 * Return the an array of objects that are affected by executing, undoing,
	 * or redoing this operation.  If it cannot be determined which objects
	 * are affected, return null.
	 * </p>
	 * @return the array of Objects modified by this operation, or <code>null</code>
	 * if the affected objects cannot be determined.
	 */
	Object [] getAffectedObjects();

}
