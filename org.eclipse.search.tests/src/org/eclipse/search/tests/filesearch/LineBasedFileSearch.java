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

package org.eclipse.search.tests.filesearch;

import org.eclipse.core.resources.IFile;
import org.eclipse.search.internal.core.text.TextSearchScope;
import org.eclipse.search.internal.ui.text.FileMatch;
import org.eclipse.search.internal.ui.text.FileSearchQuery;
import org.eclipse.search.ui.text.Match;

/**
 */
public class LineBasedFileSearch extends FileSearchQuery {
	public LineBasedFileSearch(TextSearchScope scope, String options, String searchString) {
		super(scope, options, searchString);
	}

	protected FileMatch createMatch(IFile file, int start, int length, int lineNumber) {
		return new FileMatch(file, lineNumber, 1) {
			/* (non-Javadoc)
			 * @see org.eclipse.search.ui.text.Match#getBaseUnit()
			 */
			public int getBaseUnit() {
				return Match.UNIT_LINE;
			}
		};
	}

}