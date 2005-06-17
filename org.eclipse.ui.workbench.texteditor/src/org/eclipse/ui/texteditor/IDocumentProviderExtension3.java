/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
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
 * Extension interface for {@link org.eclipse.ui.texteditor.IDocumentProvider}. The method
 * <code>isSynchronized</code> replaces the original <code>getSynchronizationStamp</code> method.
 *
 * @since 3.0
 */
public interface IDocumentProviderExtension3 {

	/**
	 * Returns whether the information provided for the given element is in sync with the element.
	 *
	 * @param element the element
	 * @return <code>true</code> if the information is in sync with the element, <code>false</code> otherwise
	 */
	boolean isSynchronized(Object element);
}
