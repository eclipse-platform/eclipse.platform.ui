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
package org.eclipse.team.internal.ccvs.core.client.listeners;

 
import java.util.Date;

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFile;
import org.eclipse.team.internal.ccvs.core.ILogEntry;
import org.eclipse.team.internal.ccvs.core.resources.RemoteFile;

public class LogEntry extends PlatformObject implements ILogEntry {

	private RemoteFile file;
	private String author;
	private Date date;
	private String comment;
	private String state;
	private CVSTag[] tags;

	public LogEntry(RemoteFile file, String revision, String author, Date date, String comment, String state, CVSTag[] tags) {
		this.file = file.toRevision(revision);
		this.author = author;
		this.date = date;
		this.comment = comment;
		this.state = state;
		this.tags = tags;
	}
	
	/**
	 * @see ILogEntry#getRevision()
	 */
	public String getRevision() {
		return file.getRevision();
	}

	/**
	 * @see ILogEntry#getAuthor()
	 */
	public String getAuthor() {
		return author;
	}

	/**
	 * @see ILogEntry#getDate()
	 */
	public Date getDate() {
		return date;
	}

	/**
	 * @see ILogEntry#getComment()
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * @see ILogEntry#getState()
	 */
	public String getState() {
		return state;
	}

	/**
	 * @see ILogEntry#getTags()
	 */
	public CVSTag[] getTags() {
		CVSTag[] result = new CVSTag[tags.length];
		System.arraycopy(tags, 0, result, 0, tags.length);
		return result;
	}

	/**
	 * @see ILogEntry#getRemoteFile()
	 */
	public ICVSRemoteFile getRemoteFile() {
		return file;
	}
	
	/**
	 * @see ILogEntry#isDeletion()
	 */
	public boolean isDeletion() {
		return getState().equals("dead"); //$NON-NLS-1$
	}

}

