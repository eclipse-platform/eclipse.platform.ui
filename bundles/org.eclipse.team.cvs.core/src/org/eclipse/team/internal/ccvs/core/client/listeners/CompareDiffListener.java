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
package org.eclipse.team.internal.ccvs.core.client.listeners;

import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.CVSStatus;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.client.CommandOutputListener;
import org.eclipse.team.internal.ccvs.core.Policy;

/**
 * This class interprets the output of "cvs diff --brief ..." in order to get the revisions
 * of two compared versions of a project or folder.
 */
public class CompareDiffListener extends CommandOutputListener {
	
	private static ServerMessageLineMatcher LOCAL_FILE_MATCHER; 
	private static ServerMessageLineMatcher REMOTE_FILE_MATCHER;
	private static ServerMessageLineMatcher REVISION_LINE_MATCHER;
	
	static {
		try {
			LOCAL_FILE_MATCHER = new ServerMessageLineMatcher(
				"Index: (localFile:.*:localFile)", new String[] {"localFile"}); //$NON-NLS-1$ //$NON-NLS-2$
			REMOTE_FILE_MATCHER = new ServerMessageLineMatcher(
				"RCS file: (remoteFile:.*:remoteFile),v", new String[] {"remoteFile"}); //$NON-NLS-1$ //$NON-NLS-2$
			REVISION_LINE_MATCHER = new ServerMessageLineMatcher(
				"diff .* -r(leftRevision:.*:leftRevision) -r(rightRevision:.*:rightRevision)", new String[] {"leftRevision", "rightRevision"}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		} catch (CVSException e) {
			// This is serious as the listener will not function properly
			CVSProviderPlugin.log(e);
			LOCAL_FILE_MATCHER = null;
			REMOTE_FILE_MATCHER = null;
			REVISION_LINE_MATCHER = null;
		}
	}
	
	private String localFilePath, remoteFilePath, leftRevision, rightRevision;
	
	private IFileDiffListener listener;
	
	public interface IFileDiffListener {
		public void fileDiff(
				String localFilePath,
				String remoteFilePath,
				String leftRevision,
				String rightRevision);
	}
	
	public CompareDiffListener(IFileDiffListener listener) {
		this.listener = listener;
	}
	
	public IStatus messageLine(
			String line, 
			ICVSRepositoryLocation location, 
			ICVSFolder commandRoot,
			IProgressMonitor monitor) {
		// ignore any server messages	
		if (getServerMessage(line, location) != null) {
			return OK;
		}
		Map map = LOCAL_FILE_MATCHER.processServerMessage(line);
		if (map != null) {
			localFilePath = (String)map.get("localFile"); //$NON-NLS-1$
			return OK;
		}
		map = REMOTE_FILE_MATCHER.processServerMessage(line);
		if (map != null) {
			remoteFilePath = (String)map.get("remoteFile"); //$NON-NLS-1$
			return OK;
		}
		map = REVISION_LINE_MATCHER.processServerMessage(line);
		if (map != null) {
			leftRevision = (String)map.get("leftRevision"); //$NON-NLS-1$
			rightRevision = (String)map.get("rightRevision"); //$NON-NLS-1$
			if (localFilePath == null || remoteFilePath == null) {
				return new CVSStatus(IStatus.ERROR, Policy.bind("CompareDiffListener.11")); //$NON-NLS-1$
			}
			listener.fileDiff(localFilePath, remoteFilePath, leftRevision, rightRevision);
			localFilePath = remoteFilePath = leftRevision = rightRevision  = null;
			return OK;
		}
		// Ignore all other lines
		return OK;
	}

	private IStatus handleUnknownDiffFormat(String line) {
		return new CVSStatus(IStatus.ERROR, Policy.bind("CompareDiffListener.12", line)); //$NON-NLS-1$
	}

	public IStatus errorLine(
			String line, 
			ICVSRepositoryLocation location, 
			ICVSFolder commandRoot,
			IProgressMonitor monitor) {
		// ignore server messages for now - this is used only with the diff
		// request and the errors can be safely ignored.
		if (getServerMessage(line, location) != null) {
			return OK;
		}
		return super.errorLine(line, location, commandRoot, monitor);
	}

}
