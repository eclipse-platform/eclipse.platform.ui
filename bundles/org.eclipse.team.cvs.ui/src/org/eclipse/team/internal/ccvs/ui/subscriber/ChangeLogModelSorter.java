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
package org.eclipse.team.internal.ccvs.ui.subscriber;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.team.internal.ccvs.core.ILogEntry;
import org.eclipse.team.ui.synchronize.ISynchronizeModelElement;

/**
 * Sorter for the change log model provider. 
 * 
 * @since 3.0
 */
public class ChangeLogModelSorter extends ViewerSorter {
	
	private int commentCriteria;
	private ChangeLogModelProvider provider;
	
	// Comment sorting options
	public final static int DATE = 1;
	public final static int COMMENT = 2;
	public final static int USER = 3;
	
	public ChangeLogModelSorter(ChangeLogModelProvider provider, int commentCriteria) {
		this.provider = provider;
		this.commentCriteria = commentCriteria;
	}
	
	protected int classComparison(Object element) {
		if (element instanceof CommitSetDiffNode) {
			return 0;
		}
		if (element instanceof ChangeLogDiffNode) {
			return 1;
		}
		return 2;
	}
	
	protected int compareClass(Object element1, Object element2) {
		return classComparison(element1) - classComparison(element2);
	}
	
	protected int compareNames(String s1, String s2) {
		return collator.compare(s1, s2);
	}
	
	/* (non-Javadoc)
	 * Method declared on ViewerSorter.
	 */
	public int compare(Viewer viewer, Object o1, Object o2) {
		//have to deal with non-resources in navigator
		//if one or both objects are not resources, returned a comparison 
		//based on class.
		if (o1 instanceof  CommitSetDiffNode && o2 instanceof CommitSetDiffNode) {
		    CommitSet s1 = ((CommitSetDiffNode) o1).getSet();
		    CommitSet s2 = ((CommitSetDiffNode) o2).getSet();
			return compareNames(s1.getTitle(), s2.getTitle());
		}
		
		if (o1 instanceof  ChangeLogDiffNode && o2 instanceof ChangeLogDiffNode) {
			ILogEntry r1 = ((ChangeLogDiffNode) o1).getComment();
			ILogEntry r2 = ((ChangeLogDiffNode) o2).getComment();
					
			if (commentCriteria == DATE)
				return r1.getDate().compareTo(r2.getDate());
			else if (commentCriteria == COMMENT)
				return compareNames(r1.getComment(), r2.getComment());
			else if (commentCriteria == USER)
				return compareNames(r1.getAuthor(), r2.getAuthor());
			else
				return 0;
		}
		
		if (o1 instanceof CommitSetDiffNode)
			return 1;
		else if (o2 instanceof CommitSetDiffNode)
			return -1;
		
		if (o1 instanceof ChangeLogDiffNode)
			return 1;
		else if (o2 instanceof ChangeLogDiffNode)
			return -1;

		if (o1 instanceof ISynchronizeModelElement && o2 instanceof ISynchronizeModelElement) {
			ViewerSorter embeddedSorter = provider.getEmbeddedSorter();
			if (embeddedSorter != null) {
			    return embeddedSorter.compare(viewer, o1, o2);
			} else {
			    compareNames(((ISynchronizeModelElement)o1).getName(), ((ISynchronizeModelElement)o2).getName());
			}
		} else if (o1 instanceof ISynchronizeModelElement)
			return 1;
		else if (o2 instanceof ISynchronizeModelElement)
			return -1;
		
		return 0;
	}

	public int getCommentCriteria() {
		return commentCriteria;
	}
}
