/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.projection;


import org.eclipse.jface.text.IDocument;


/**
 * Implementation of a child document manager based on
 * {@link org.eclipse.jface.text.projection.ProjectionDocumentManager}. This
 * class exists for compatibility reasons.
 * <p>
 * Internal class. This class is not intended to be used by clients.</p>
 *
 * @since 3.0
 */
public class ChildDocumentManager extends ProjectionDocumentManager {

	/*
	 * @see org.eclipse.jface.text.projection.ProjectionDocumentManager#createProjectionDocument(org.eclipse.jface.text.IDocument)
	 */
	protected ProjectionDocument createProjectionDocument(IDocument master) {
		return new ChildDocument(master);
	}
}
