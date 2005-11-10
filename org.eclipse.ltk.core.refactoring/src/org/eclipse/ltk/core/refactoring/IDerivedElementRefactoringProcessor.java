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
package org.eclipse.ltk.core.refactoring;

/**
 * Interface for refactoring processors which refactor derived elements.
 * <p>
 * This interface may be implemented by clients.
 * </p>
 * <p>
 * Note: This API is considered experimental and may change in the near future.
 * </p>
 * 
 * @since 3.2
 */
public interface IDerivedElementRefactoringProcessor {

	/**
	 * This method is used to ask the refactoring component for derived elements
	 * which are refactored as well.
	 * 
	 * @return derived elements which are refactored as well
	 */
	public Object[] getDerivedElements();

	/**
	 * This method is used to ask the refactoring processor for an updated
	 * element handle. The new handle reflects all changes and derived changes
	 * carried out by the refactoring.
	 * 
	 * @param element the old element before the refactoring has been started
	 * @return the element after the processor's changes have been executed
	 */
	public Object getRefactoredElement(Object element);

}
