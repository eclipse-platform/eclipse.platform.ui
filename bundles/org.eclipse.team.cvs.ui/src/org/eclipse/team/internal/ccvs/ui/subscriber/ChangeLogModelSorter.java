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
import org.eclipse.ui.views.navigator.ResourceSorter;

/**
 * Sorter for the change log model provider. 
 * 
 * @since 3.0
 */
public class ChangeLogModelSorter extends ViewerSorter {
	
	private int criteria;
	
	public final static int DATE = 1;
	public final static int COMMENT = 2;
	public final static int USER = 3;
	private ResourceSorter resourceSorter;
	
	public ChangeLogModelSorter(int criteria) {
		super();
		this.criteria = criteria;
		this.resourceSorter = new ResourceSorter(ResourceSorter.NAME);
	}
	
	protected int classComparison(Object element) {
		if (element instanceof ChangeLogDiffNode) {
			return 0;
		}
		return 1;
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
		if (o1 instanceof  ChangeLogDiffNode && o2 instanceof ChangeLogDiffNode) {
			ILogEntry r1 = ((ChangeLogDiffNode) o1).getComment();
			ILogEntry r2 = ((ChangeLogDiffNode) o2).getComment();
			
			
			if (criteria == DATE)
				return r1.getDate().compareTo(r2.getDate());
			else if (criteria == COMMENT)
				return compareNames(r1.getComment(), r2.getComment());
			else if (criteria == USER)
				return compareNames(r1.getAuthor(), r2.getAuthor());
			else
				return 0;
		}

		if (o1 instanceof ISynchronizeModelElement && o2 instanceof ISynchronizeModelElement) 
			return resourceSorter.compare(viewer, ((ISynchronizeModelElement)o1).getResource(), ((ISynchronizeModelElement)o2).getResource());
		else if (o1 instanceof ISynchronizeModelElement)
			return -1;
		else if (o2 instanceof ISynchronizeModelElement)
			return 1;
		
		return 0;
	}

	public int getCriteria() {
		return criteria;
	}

	public void setCriteria(int criteria) {
		this.criteria = criteria;
	}
}
