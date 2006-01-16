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

import java.util.Date;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.team.internal.ccvs.core.filehistory.CVSFileRevision;
import org.eclipse.team.internal.core.LocalFileRevision;

public class CVSHistoryFilter extends ViewerFilter {
	public String author;
	public Date fromDate;
	public Date toDate;
	public String comment;
	public boolean isOr;
	public boolean showLocal;

	public CVSHistoryFilter(String author, String comment, Date fromDate, Date toDate, boolean isOr, boolean showLocal) {
		this.author = author;
		this.comment = comment;
		this.fromDate = fromDate;
		this.toDate = toDate;
		this.isOr = isOr;
		this.showLocal = showLocal;
	}

	/**
	 * @see ViewerFilter#select(Viewer, Object, Object)
	 */
	public boolean select(Viewer aviewer, Object parentElement, Object element) {
		if (element instanceof CVSFileRevision) {
			CVSFileRevision entry = (CVSFileRevision) element;
			if (isOr) {
				//empty fields should be considered a non-match
				return (hasAuthor() && authorMatch(entry)) || (hasDate() && dateMatch(entry)) || (hasComment() && commentMatch(entry));
			} else {
				//"and" search
				//empty fields should be considered a match
				return (!hasAuthor() || authorMatch(entry)) && (!hasDate() || dateMatch(entry)) && (!hasComment() || commentMatch(entry));
			}
		} else if (element instanceof LocalFileRevision) {
			return showLocal();
		}
		return false;
	}

	protected boolean authorMatch(CVSFileRevision revision) {
		return revision.getAuthor().equals(author);
	}

	protected boolean commentMatch(CVSFileRevision revision) {
		return !(revision.getComment().toLowerCase().indexOf(comment.toLowerCase()) == -1);
	}

	protected boolean dateMatch(CVSFileRevision revision) {
		return (fromDate.before(new Date(revision.getTimestamp()))) && (toDate.after(new Date(revision.getTimestamp())));
	}

	protected boolean hasAuthor() {
		return !author.equals(""); //$NON-NLS-1$
	}

	protected boolean hasComment() {
		return !comment.equals(""); //$NON-NLS-1$
	}

	protected boolean hasDate() {
		return fromDate != null && toDate != null;
	}

	/*
	 * Used to indicate if this filter should show local file revisions
	 */
	protected boolean showLocal() {
		return showLocal;
	}
}
