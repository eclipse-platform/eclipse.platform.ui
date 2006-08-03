/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui;

import java.util.*;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.team.internal.ccvs.core.filehistory.CVSFileRevision;
import org.eclipse.team.internal.ui.history.AbstractHistoryCategory;

public class CVSHistorySearchFilter extends org.eclipse.jface.viewers.ViewerFilter {

	public String searchString;
	private int matchCounter;
	public ArrayList searchStrings;

	public CVSHistorySearchFilter(String searchStrings) {
		this.searchString = searchStrings;
	}

	/**
	 * @see ViewerFilter#select(Viewer, Object, Object)
	 */
	public boolean select(Viewer aviewer, Object parentElement, Object element) {
		if (element instanceof AbstractHistoryCategory)
			return true;

		if (element instanceof CVSFileRevision) {
			StringTokenizer tokenizer = new StringTokenizer(searchString);
			searchStrings = new ArrayList();
			while (tokenizer.hasMoreElements()) {
				searchStrings.add(tokenizer.nextToken());
			}

			CVSFileRevision entry = (CVSFileRevision) element;
			//empty fields should be considered a non-match
			boolean orSearch = (authorMatch(entry)) || (dateMatch(entry)) || (commentMatch(entry));
			if (orSearch)
				matchCounter++;
			return orSearch;
		}
		return false;
	}

	protected boolean authorMatch(CVSFileRevision revision) {
		String author = revision.getAuthor();
		Iterator iter = searchStrings.iterator();
		while (iter.hasNext()) {
			String nextString = (String) iter.next();
			if (!((author.indexOf(nextString)) == -1))
				return true;
		}

		return false;
	}

	protected boolean commentMatch(CVSFileRevision revision) {
		String comment = revision.getComment().toLowerCase();
		Iterator iter = searchStrings.iterator();
		while (iter.hasNext()) {
			if (!(comment.indexOf(((String) iter.next()).toLowerCase()) == -1))
				return true;
		}

		return false;
	}

	protected boolean dateMatch(CVSFileRevision revision) {
		//No dates for now
		/*String date = DateFormat.getInstance().format(new Date(revision.getTimestamp()));
		 Iterator iter = searchStrings.iterator();
		 while (iter.hasNext()){
		 if (!(date.indexOf(((String) iter.next()).toLowerCase()) == -1))
		 return true;
		 }*/

		return false;
	}

	public int getMatchCount() {
		return matchCounter;
	}

}
