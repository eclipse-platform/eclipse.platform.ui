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
package org.eclipse.jface.text;

/**
 * Extension interface for {@link org.eclipse.jface.text.ISlaveDocumentManager}.
 * <p>
 * Adds access to the list of all slave documents for a given master document.
 *
 * @see org.eclipse.jface.text.ISlaveDocumentManager
 * @since 3.0
 */
public interface ISlaveDocumentManagerExtension {

	/**
	 * Returns the list of slave documents for the given master document or
	 * <code>null</code> if there are no such slave document.
	 *
	 * @param master the master document
	 * @return the list of slave documents or <code>null</code>
	 */
	IDocument[] getSlaveDocuments(IDocument master);
}
