/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
package org.eclipse.search.ui.text;

import org.eclipse.ui.IEditorPart;

/**
 * This interface serves as an adapter between matches and editors. It is used to
 * highlight matches in editors. Search implementors who want their matches highlighted
 * must return an implementation of <code>IEditorMatchAdapter</code> from the <code>getEditorMatchAdapter()</code>
 * method in their search result subclass.
 * It is assumed that the match adapters are stateless, and no lifecycle management
 * is provided.
 * <p>
 * Clients may implement this interface.
 * </p>
 * @see org.eclipse.search.ui.text.AbstractTextSearchResult
 *
 * @since 3.0
 */
public interface IEditorMatchAdapter {
	/**
	 * Determines whether a match should be displayed in the given editor.
	 * For example, if a match is reported in a file, This method should return
	 * <code>true</code>, if the given editor displays the file.
	 *
	 * @param match The match
	 * @param editor The editor that possibly contains the matches element
	 * @return whether the given match should be displayed in the editor
	 */
	public abstract boolean isShownInEditor(Match match, IEditorPart editor);
	/**
	 * Returns all matches that are contained in the element shown in the given
	 * editor.
	 * For example, if the editor shows a particular file, all matches in that file should
	 * be returned.
	 * @param result the result to search for matches
	 *
	 * @param editor The editor.
	 * @return All matches that are contained in the element that is shown in
	 *         the given editor.
	 */
	public abstract Match[] computeContainedMatches(AbstractTextSearchResult result, IEditorPart editor);

}
