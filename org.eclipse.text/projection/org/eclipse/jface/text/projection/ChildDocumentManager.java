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
 * ChildDocumentManager
 */
public class ChildDocumentManager extends ProjectionDocumentManager {
	
	/*
	 * @see org.eclipse.jface.text.projection.ProjectionDocumentManager#createProjectionDocument(org.eclipse.jface.text.IDocument, org.eclipse.jface.text.projection.FragmentUpdater)
	 */
	protected ProjectionDocument createProjectionDocument(IDocument master, FragmentUpdater fragmentUpdater) {
		return new ChildDocument(master, FRAGMENTS_CATEGORY, fragmentUpdater, SEGMENTS_CATEGORY);
	}
}
