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
package org.eclipse.jface.text.projection;

import org.eclipse.jface.text.IDocument;

/**
 * Implementation of a child document manager based on
 * <code>ProjectionDocumentManager</code>. This class exists for
 * compatibility reasons.
 * <p>
 * Internal class. Do not use. Only public for testing purposes.
 * 
 * @since 3.0
 */
public class ChildDocumentManager extends ProjectionDocumentManager {
	
	/**
	 * The position category used to manage the child documents.
	 */
	public final static String CHILDDOCUMENTS= FRAGMENTS_CATEGORY;
	
	/*
	 * @see org.eclipse.jface.text.projection.ProjectionDocumentManager#createProjectionDocument(org.eclipse.jface.text.IDocument, org.eclipse.jface.text.projection.FragmentUpdater)
	 */
	protected ProjectionDocument createProjectionDocument(IDocument master, FragmentUpdater fragmentUpdater) {
		return new ChildDocument(master, FRAGMENTS_CATEGORY, fragmentUpdater, SEGMENTS_CATEGORY);
	}
}
