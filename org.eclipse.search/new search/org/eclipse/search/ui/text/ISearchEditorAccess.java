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

package org.eclipse.search.ui.text;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.IAnnotationModel;

public interface ISearchEditorAccess {
	/**
	 * Finds the document displaying the match.
	 * @param match
	 * @return the document displaying the given match.
	 */
	IDocument getDocument(Match match);
	/**
	 * Finds the annotation model for the given match
	 * @param match
	 * @return the annotation model displaying the given match.
	 */
	IAnnotationModel getAnnotationModel(Match match);
}
