/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Markus Schorn - initial API and implementation 
 *******************************************************************************/

package org.eclipse.search.ui.text;

import org.eclipse.search.core.text.TextSearchMatchAccess;

/**
 * Abstract base class for information providers  supplied via the <code>org.eclipse.search.textSearchMatchInformationProvider</code>
 * extension point. An implementation of a {@link SearchMatchInformationProvider} can evaluate line information and locations 
 * for a given match.
 * <dl>
 *   <li>{@link #LOCATION_STRING_LITERAL} match inside a string literal</li>
 *   <li>{@link #LOCATION_COMMENT} match inside a comment</li>
 *   <li>{@link #LOCATION_IMPORT_OR_INCLUDE_STATEMENT} match inside import / include statement</li>
 *   <li>{@link #LOCATION_PREPROCESSOR_DIRECTIVE} match inside a preprocessor directive</li>
 *   <li>{@link #LOCATION_FUNCTION} match inside function</li>
 *   <li>{@link #LOCATION_OTHER} match if the location can not be assigned to any of the other locations.</li>
 * </dl>
 * 
 * @since 3.2
 * 
 * This API is experimental and might be removed before 3.2
 * 
 */
public abstract class SearchMatchInformationProvider {
	/**
	 * Id for the 'Other' location
	 */
	public final static int LOCATION_OTHER= 0;
	/**
	 * Id for the String literal location
	 */
	public final static int LOCATION_STRING_LITERAL= 1;
	/**
	 * Id for the comment location
	 */
	public final static int LOCATION_COMMENT= 2;
	/**
	 * Id for the import and include location
	 */
	public final static int LOCATION_IMPORT_OR_INCLUDE_STATEMENT= 3;
	/**
	 * Id for the preprocessor location
	 */
	public final static int LOCATION_PREPROCESSOR_DIRECTIVE= 4;
	/**
	 * Id for the function location
	 */
	public final static int LOCATION_FUNCTION= 5;

	/**
	 * Information about a line of a match.
	 */
	public final static class LineInformation {
		private final int fLineNumber;
		private final int fLineOffset;
		private final int fLineLength;

		/**
		 * Creates a line information instance for a match.
		 * 
		 * @param lineNumber the line number where the match starts.
		 * @param lineOffset the file offset of the beginning of the line where the match starts.
		 * @param lineLength the length of the lines containing the match.
		 */
		public LineInformation(int lineNumber, int lineOffset, int lineLength) {
			fLineNumber= lineNumber;
			fLineOffset= lineOffset;
			fLineLength= lineLength;
		}

		/**
		 * Returns the line number of the line where the match starts.
		 * 
		 * @return the line number of the line where the match starts.
		 */
		public int getLineNumber() {
			return fLineNumber;
		}

		/**
		 * Returns the total length of the lines containing the match. This also includes
		 * the characters of the line terminator.
		 * 
		 * @return the length of the lines containing the match.
		 */
		public int getLineLength() {
			return fLineLength;
		}

		/**
		 * Returns the file offset of the beginning of the line where the match starts.
		 * 
		 * @return the offset of the beginning of the line where the match starts.
		 */
		public int getLineOffset() {
			return fLineOffset;
		}
	}

	/**
	 * Notification sent to tell a information provider to clear all cached information.
	 */
	public void reset() {
	}


	/**
	 * Returns information about the lines containing the given match.
	 * 
	 * @param match the match to get the line information for.
	 * @return the information about the provided match.
	 */
	public abstract LineInformation getLineInformation(TextSearchMatchAccess match);

	/**
	 * Returns the kind of location the given match starts in.
	 * 
	 * @param match the match to get the location for
	 * @return one of the location constants defined in {@link SearchMatchInformationProvider}.
	 */
	public int getLocationKind(TextSearchMatchAccess match) {
		return LOCATION_OTHER;
	}

}
