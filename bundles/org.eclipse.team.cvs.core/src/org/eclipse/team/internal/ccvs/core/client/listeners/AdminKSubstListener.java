package org.eclipse.team.internal.ccvs.core.client.listeners;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSStatus;
import org.eclipse.team.internal.ccvs.core.ICVSFile;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.client.CommandOutputListener;
import org.eclipse.team.internal.ccvs.core.client.Command.KSubstOption;
import org.eclipse.team.internal.ccvs.core.syncinfo.FolderSyncInfo;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo;

/**
 * Used with 'admin -ksubst' to capture lines of text that are issued
 * as confirmation that the remote keyword substitution mode has been
 * changed.  When encountered, updates the local ResourceSyncInfo for
 * the file in question to reflect
 * 
 * e.g.
 *   RCS file: path/filename,v
 *   done
 * 
 * We don't expect to see anything special on stderr if the command succeeds.
 */
public class AdminKSubstListener extends CommandOutputListener {
	private KSubstOption ksubstMode;
	
	public AdminKSubstListener(KSubstOption ksubstMode) {
		this.ksubstMode = ksubstMode;
	}
	
	public IStatus messageLine(String line, ICVSRepositoryLocation location, ICVSFolder commandRoot,
		IProgressMonitor monitor) {
		if (line.startsWith("RCS file:")) { //$NON-NLS-1$
			String rcsFile = line.substring(10).trim();
			if (! rcsFile.endsWith(",v")) { //$NON-NLS-1$
				return new CVSStatus(CVSStatus.ERROR,
					Policy.bind("AdminKSubstListener.expectedRCSFile", rcsFile)); //$NON-NLS-1$
			}
			String remoteRootLocation = null;
			try {
				FolderSyncInfo info = commandRoot.getFolderSyncInfo();
				remoteRootLocation = info.getRemoteLocation();
			} catch (CVSException e) {
				return e.getStatus();
			}
			if (remoteRootLocation == null) {
				return new CVSStatus(CVSStatus.ERROR,
					Policy.bind("AdminKSubstListener.commandRootNotManaged")); //$NON-NLS-1$
			}
			IPath rcsFilePath = new Path(rcsFile.substring(0, rcsFile.length() - 2));
			IPath remoteRootPath = new Path(remoteRootLocation);
			if (! remoteRootPath.isPrefixOf(rcsFilePath)) {
				return new CVSStatus(CVSStatus.ERROR,
					Policy.bind("AdminKSubstListener.expectedChildOfCommandRoot", //$NON-NLS-1$
						rcsFilePath.toString(), remoteRootPath.toString()));
			}
			rcsFilePath = rcsFilePath.removeFirstSegments(remoteRootPath.segmentCount());
			try {
				ICVSFile file = commandRoot.getFile(rcsFilePath.toString());
				//ResourceSyncInfo info = file.getSyncInfo();
				byte[] syncBytes = file.getSyncBytes();
				if (syncBytes != null) {
					// only update sync info if we have it locally
					file.setSyncBytes(ResourceSyncInfo.setKeywordMode(syncBytes, ksubstMode), ICVSFile.UNKNOWN);
				}
			} catch (CVSException e) {
				return new CVSStatus(CVSStatus.ERROR,
					Policy.bind("AdminKSubstListener.couldNotSetResourceSyncInfo", //$NON-NLS-1$
						rcsFilePath.toString(), e.toString()));
			}
		}
		return OK;
	}
}
