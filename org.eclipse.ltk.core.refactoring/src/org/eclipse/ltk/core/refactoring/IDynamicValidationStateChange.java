/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring;

import org.eclipse.core.runtime.IProgressMonitor;


/**
 * This is a tagging interface to indicate that the validation
 * state of a change changes dynamically. This gives clients
 * that rely on the change's validation state (the value returned
 * form {@link org.eclipse.ltk.core.refactoring.Change#isValid(IProgressMonitor)
 * IChange#isValid}) the opportunity to dynamically update their state 
 * as well. For example, the undo/redo stack listens to validation 
 * state changs and removes undo/redo changes from the undo/redo stack
 * when a change becomes invalid.
 * 
 * @since 3.0
 */
public interface IDynamicValidationStateChange {

	/**
	 * Adds a validation state listener. Has no effect if an 
	 * identical listener is already registered.
	 * 
	 * @param listener the listener to add
	 */
	public void addValidationStateListener(IValidationStateListener listener);
	
	/**
	 * Removes the validation state listener. Has no effect if
	 * the listener isn't registered.
	 * 
	 * @param listener the listener to remove
	 */
	public void removeValidationStateListener(IValidationStateListener listener);
	
	/**
	 * Hook method that gets called when a different change is going
	 * to be executed. Implementors of this interface should postpone
	 * informing the registered <code>IValidationStateListner</code>
	 * until the method <code>changePerformed</code> was called.
	 * 
	 * @param change the change to be executed
	 */
	public void aboutToPerformChange(Change change);
	
	/**
	 * Hook method that gets called when the given change has been 
	 * executed. Implementors of this interface should decide on the 
	 * state of the passed parameters if validation state changes recorded 
	 * between the call to <code>aboutToPeformChange</code> and now should be
	 * flushed or should be notified to the registered listeners. For
	 * example, if the executed change is undoable then this change is still
	 * valid and the recorded validation state changes should be flushed.
	 * 
	 * @param change the change that has been executed
	 * @param undo the corresponding undo change or <code>null</code> if no
	 *  undo exists
	 * @param e <code>null</code> if the change has been executed
	 *  successfully; otherwise the catched exception
	 */
	public void changePerformed(Change change, Change undo, Exception e);
}
