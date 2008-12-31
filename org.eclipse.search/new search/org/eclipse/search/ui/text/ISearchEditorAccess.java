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

package org.eclipse.search.ui.text;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.IAnnotationModel;

/**
 * <p>This interface allows editors to provide customized access to editor internals for the
 * search implementation to highlight matches. The search system will use the document to
 * do line/character offset conversion if needed and it will add annotations to the annotation
 * model.</p>
 * <p> The search system will ask an editor for an adapter of this class whenever it needs
 * access to the document or the annotation model of the editor. Since an editor might use
 * multiple documents and/or annotation models, the match is passed in when asking the editor.
 * The editor is then expected to return the proper annotation model or document for the given
 * match.</p>
 * <p>
 * This interface is intended to be implemented by clients.
 * </p>
 * @since 3.0
 */
public interface ISearchEditorAccess {
	/**
	 * Finds the document displaying the match.
	 * @param match the match
	 * @return the document displaying the given match.
	 */
	IDocument getDocument(Match match);
	/**
	 * Finds the annotation model for the given match
	 * @param match the match
	 * @return the annotation model displaying the given match.
	 */
	IAnnotationModel getAnnotationModel(Match match);
}
