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

/**
 * A <code>RefactoringStatusContext<code> can be used to annotate a 
 * {@link RefactoringStatusEntry} with additional information 
 * typically presented in the UI.
 * 
 * @since 3.0
 */
public abstract class RefactoringStatusContext {
	/**
	 * Returns the element that corresponds directly to this context,
	 * or <code>null</code> if there is no corresponding element.
	 * <p>
	 * For example, the corresponding element of a context for a problem 
	 * detected in an <code>IResource</code> would the the resource itself.
	 * <p>
	 *
	 * @return the corresponding element
	 */
	public abstract Object getCorrespondingElement();
}
