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
package org.eclipse.team.internal.ccvs.core.syncinfo;

import org.eclipse.team.internal.ccvs.core.CVSTag;

/**
 * Mutable version of FolderSyncInfo. Can be used when either creating a
 * folder sync object from scratch or when modifying an existing
 * <code>FolderSyncInfo</code> instance. Example usage:
 * <pre>
 * FolderSyncInfo info = folder.getFolderSyncInfo(); 
 * if(info!=null) {
 *    MutableFolderSyncInfo newInfo = info.cloneMutable(); 
 *    newInfo.setTag(CVSTag.DEFAULT);
 *    folder.setFolderSyncInfo(newInfo);
 * }
 * </pre>
 * @see FolderSyncInfo
 */
public class MutableFolderSyncInfo extends FolderSyncInfo {
	
	/**
	 * Constructor MutableFolderSyncInfo.
	 * @param folderSyncInfo
	 */
	public MutableFolderSyncInfo(FolderSyncInfo info) {
		this(info.getRepository(), info.getRoot(), info.getTag(), info.getIsStatic());
	}

	/**
	 * Constructor for MutableFolderSyncInfo.
	 * @param repo
	 * @param root
	 * @param tag
	 * @param isStatic
	 */
	public MutableFolderSyncInfo(String repo, String root, CVSTag tag, boolean isStatic) {
		super(repo, root, tag, isStatic);
	}

	/**
	 * @see org.eclipse.team.internal.ccvs.core.syncinfo.FolderSyncInfo#setTag(org.eclipse.team.internal.ccvs.core.CVSTag)
	 */
	public void setTag(CVSTag tag) {
		super.setTag(tag);
	}
	/**
	 * Method setRepository.
	 * @param string
	 */
	public void setRepository(String repository) {
		this.repository = repository;
	}
}
