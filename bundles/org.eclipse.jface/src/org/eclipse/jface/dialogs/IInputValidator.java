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
package org.eclipse.jface.dialogs;

 
/*
 * Minimal interface to an input validator.
 * Input validators are used in <code>InputDialog</code>.
 */
public interface IInputValidator {
/**
 * Validates the given string.  Returns an error message to display
 * if the new text is invalid.  Returns <code>null</code> if there
 * is no error.  Note that the empty string is not treated the same
 * as <code>null</code>; it indicates an error state but with no message
 * to display.
 * 
 * @return an error message or <code>null</code> if no error
 */
public String isValid(String newText);
}
