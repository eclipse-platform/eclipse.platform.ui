/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/
package org.eclipse.team.internal.ccvs.core.syncinfo;

import java.util.Date;

import org.eclipse.team.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.util.Assert;

/**
 * Mutable version of ResourceSyncInfo. Can be used when either creating a resource sync
 * object from scratch (e.g. without an entry line) or want to modify an existing
 * <code>ResourceSyncInfo</code> instance. Example usage:
 * <pre>
 * ResourceSyncInfo info = resource.getSyncInfo();
 * if(info!=null) {
 *   MutableResourceSyncInfo newInfo = info.cloneMutable();
 *   newInfo.setRevision("1.22");
 *   resource.setSyncInfo(newInfo);
 * }
 * </pre>
 * @see ResourceSyncInfo
 */
public class MutableResourceSyncInfo extends ResourceSyncInfo {
	
	protected MutableResourceSyncInfo(ResourceSyncInfo info) {
		this.name = info.getName();
		setRevision(info.getRevision());
		setTag(info.getTag());
		this.permissions = info.getPermissions();
		this.timeStamp = info.getTimeStamp();
		this.isDirectory = info.isDirectory();
		this.keywordMode = info.getKeywordMode();
		this.isDeleted = info.isDeleted();
	}
	
	public MutableResourceSyncInfo(String name, String revision) {
		Assert.isNotNull(name);
		Assert.isNotNull(name);
		this.name = name;
		setRevision(revision);
	}
	
	void setResourceInfoType(int type) {
		this.syncType = type;
	}
	
	/**
	 * Sets the revision.
	 * @param revision The revision to set
	 */
	public void setRevision(String revision) {
		super.setRevision(revision);
	}
	
	/**
	 * Sets the timeStamp.
	 * @param timeStamp The timeStamp to set
	 */
	public void setTimeStamp(Date timeStamp) {
		this.timeStamp = timeStamp;
	}
	
	/**
	 * Sets the keywordMode.
	 * @param keywordMode The keywordMode to set
	 */
	public void setKeywordMode(String keywordMode) {
		this.keywordMode = keywordMode;
	}

	/**
	 * Sets the tag.
	 * @param tag The tag to set
	 */
	public void setTag(CVSTag tag) {
		super.setTag(tag);
	}
	
	/**
	 * Sets the permissions.
	 * @param permissions The permissions to set
	 */
	public void setPermissions(String permissions) {
		this.permissions = permissions;
	}
	
	/**
	 * Sets the deleted state.
	 * @param isDeleted The deleted state of this resource sync
	 */
	public void setDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}
}
