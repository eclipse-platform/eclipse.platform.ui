/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui;

import java.util.Date;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.team.internal.ccvs.core.ILogEntry;
//TODO: Remove for M6
public class HistoryFilter extends ViewerFilter {
	public String author;
	public Date fromDate;
	public Date toDate;
	public String comment;
	public boolean isOr;

	public HistoryFilter(String author, String comment, Date fromDate, Date toDate, boolean isOr) {
		this.author = author;
		this.comment = comment;
		this.fromDate = fromDate;
		this.toDate = toDate;
		this.isOr = isOr;
	}
	/**
	 * @see ViewerFilter#select(Viewer, Object, Object)
	 */
	public boolean select(Viewer aviewer, Object parentElement, Object element) {
		if (element instanceof ILogEntry) {
			ILogEntry entry = (ILogEntry)element;
			if (isOr) {
				//empty fields should be considered a non-match
				return (hasAuthor() && authorMatch(entry) )
				|| (hasDate() && dateMatch(entry))
				|| (hasComment() && commentMatch(entry));
			} else {
				//"and" search
				//empty fields should be considered a match
				return (!hasAuthor() || authorMatch(entry))
					&& (!hasDate() || dateMatch(entry))
					&& (!hasComment() || commentMatch(entry));
			}
		}
		return false;
	}
	protected boolean authorMatch(ILogEntry entry) {
		return entry.getAuthor().equals(author);
	}
	protected boolean commentMatch(ILogEntry entry) {
		return !(entry.getComment().toLowerCase().indexOf(comment.toLowerCase()) == -1);
	}
	protected boolean dateMatch(ILogEntry entry) {
		return (fromDate.before(entry.getDate()))
			&& (toDate.after(entry.getDate()));
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
}
