/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.texteditor;


/**
 * Extension interface for actions. Actions implementing this interface not
 * only manage an enable/disable state but also manage a "hypothetical"
 * enable state, depending on whether the target they work on is writable
 * or read-only.
 *
 * @since 2.0
 */
public interface IReadOnlyDependent {

	/**
	 * Returns whether the actions would be enabled if its target would be enabled given the
	 * writable state described by <code>isWritable</code>. <code>isEnabled()</code> and
	 * <code>isEnabled(boolean)</code> holds the following invariants: isEnabled() == false, if
	 * isEnabled(true) == false || isEnabled(false) == false isEnabled() == true, if isEnabled(true)
	 * == true || isEnabled(false) == true
	 *
	 * @param isWritable the writable state
	 * @return the hypothetical enable state of the action
	 */
	boolean isEnabled(boolean isWritable);
}
