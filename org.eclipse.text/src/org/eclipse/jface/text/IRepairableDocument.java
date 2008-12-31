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
package org.eclipse.jface.text;


/**
 * Tagging interface to be implemented by
 * {@link org.eclipse.jface.text.IDocument} implementers that offer a line
 * repair method on the documents.
 * <p>
 * In order to provide backward compatibility for clients of
 * <code>IRepairableDocument</code>, extension interfaces are used to provide
 * a means of evolution. The following extension interfaces exist:
 * <ul>
 * <li> {@link org.eclipse.jface.text.IRepairableDocumentExtension} since version 3.4
 *      adds the ability to query whether the repairable document needs to be repaired.</li>
 * </ul>
 *
 *
 * @see org.eclipse.jface.text.IDocument
 * @see org.eclipse.jface.text.IRepairableDocumentExtension
 * @since 3.0
 */
public interface IRepairableDocument {

	/**
	 * Repairs the line information of the document implementing this interface.
	 */
	void repairLineInformation();
}
