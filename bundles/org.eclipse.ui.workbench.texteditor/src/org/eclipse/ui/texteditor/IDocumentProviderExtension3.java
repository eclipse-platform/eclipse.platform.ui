/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
