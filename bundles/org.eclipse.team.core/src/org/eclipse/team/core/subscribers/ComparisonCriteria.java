/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.core.subscribers;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.TeamException;

/**
 * A ComparisonCriteria used by a <code>TeamSubscriber</code> to calculate the sync
 * state of the workspace resources. Subscribers are free to use the criteria
 * best suited for their environment. For example, an FTP subscriber could choose to use file
 * size or file size as compasison criterias.
 * <p>
 * Aggregate criterias can be created for cases where a criteria is based on the result
 * of another criteria.</p>
 * 
 * @see org.eclipse.team.core.subscribers.SyncInfo
 * @see org.eclipse.team.core.subscribers.TeamSubscriber
 */
abstract public class ComparisonCriteria {
	
	private ComparisonCriteria[] preConditions;
	
	/**
	 * Default no-args contructor to be called if the comparison criteria does not
	 * depend on other criterias. 
	 */
	public ComparisonCriteria() {
	}

	/**
	 * Constructor used to create a criteria whose comparison is based on the compare
	 * result of other criterias. 
	 * @param preConditions array of preconditions
	 */	
	public ComparisonCriteria(ComparisonCriteria[] preConditions) {
		this.preConditions = preConditions;
	}

	/**
	 * Return the comparison criteria, in a format that is suitable for display to an end 
	 * user.
	 */
	abstract public String getName();
	
	/**
	 * Return the unique id that identified this comparison criteria.
	 */
	abstract public String getId();

	/**
	 * Returns <code>true</code> if e1 and e2 are equal based on this criteria and <code>false</code>
	 * otherwise. Since comparison could be long running the caller should provide a progress monitor.
	 *  
	 * @param e1 object to be compared
	 * @param e2 object to be compared
	 * @param monitor
	 * @return
	 * @throws TeamException
	 */
	abstract public boolean compare(Object e1, Object e2, IProgressMonitor monitor) throws TeamException;
	
	/**
	 * @return
	 */
	protected ComparisonCriteria[] getPreConditions() {
		return preConditions;
	}

	protected boolean checkPreConditions(Object e1, Object e2, IProgressMonitor monitor) throws TeamException {
		for (int i = 0; i < preConditions.length; i++) {
			ComparisonCriteria cc = preConditions[i];
			if(cc.compare(e1, e2, monitor)) {
				return true;
			}
		}	
		return false;
	}
	
	public abstract boolean usesFileContents();
}
