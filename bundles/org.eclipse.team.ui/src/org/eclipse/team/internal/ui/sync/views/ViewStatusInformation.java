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
package org.eclipse.team.internal.ui.sync.views;

import org.eclipse.team.internal.ui.sync.sets.SubscriberInput;
import org.eclipse.ui.IWorkingSet;

class ViewStatusInformation {

	private String subscriberName;
	private String workingSetName;
	private long numShowing = 0;
	private long numInWorkingSet = 0;
	private long numInWorkspace = 0;
	private SubscriberInput input;
	
	ViewStatusInformation(SubscriberInput input) {
		this.subscriberName = input.getSubscriber().getName();
		this.input = input;
		IWorkingSet set = input.getWorkingSet();
		if(set != null) {
			this.workingSetName = set.getName();
		} else {
			this.workingSetName = ""; //$NON-NLS-1$
		}
		this.numShowing = input.getFilteredSyncSet().size();
		this.numInWorkingSet = input.getWorkingSetSyncSet().size();
		this.numInWorkspace = input.getSubscriberSyncSet().size();
	}

	public long getNumInWorkingSet() {
		return numInWorkingSet;
	}

	public long getNumInWorkspace() {
		return numInWorkspace;
	}

	public long getNumShowing() {
		return numShowing;
	}

	public String getSubscriberName() {
		return subscriberName;
	}

	public String getWorkingSetName() {
		return workingSetName;
	}
	
	public SubscriberInput getSubscriberInput() {
		return input;
	}
	
	public boolean equals(Object other) {
		if(other == this) return true;
		if(other instanceof ViewStatusInformation) {
			ViewStatusInformation otherStatus = (ViewStatusInformation)other;
			return
				subscriberName.equals(otherStatus.subscriberName) &&
				workingSetName.equals(otherStatus.workingSetName) &&
				numShowing == otherStatus.numShowing &&
				numInWorkingSet == otherStatus.numInWorkingSet &&
				numInWorkspace == otherStatus.numInWorkspace;
		}
		return false;
	}
}