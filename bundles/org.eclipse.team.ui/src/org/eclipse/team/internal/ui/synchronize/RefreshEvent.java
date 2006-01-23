/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.synchronize;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.ui.synchronize.ISynchronizeParticipant;

/**
 * A refresh event generated to notify clients of the refresh lifecycle.
 */
public class RefreshEvent implements IRefreshEvent {
	int type; 
	SyncInfo[] changes;
	long startTime = 0;
	long stopTime = 0;
	IStatus status;
	IResource[] resources;
	private final ISynchronizeParticipant participant;
	
	public RefreshEvent(int type, IResource[] resources, ISynchronizeParticipant participant) {
		this.type = type;
		this.resources = resources;
		this.participant = participant;
	}
	
	public int getRefreshType() {
		return type;
	}

	public SyncInfo[] getChanges() {
		return changes != null ? changes : new SyncInfo[0];
	}
	
	public void setChanges(SyncInfo[] changes) {
		this.changes = changes;
	}
	
	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getStopTime() {
		return stopTime;
	}

	public void setStopTime(long stopTime) {
		this.stopTime = stopTime;
	}

	public IStatus getStatus() {
		return status;
	}
	
	public void setStatus(IStatus status) {
		this.status = status;
	}

	public IResource[] getResources() {
		return resources;
	}

	public ISynchronizeParticipant getParticipant() {
		return participant;
	}
}
