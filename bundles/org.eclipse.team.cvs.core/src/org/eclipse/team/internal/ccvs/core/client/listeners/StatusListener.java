package org.eclipse.team.internal.ccvs.core.client.listeners;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.team.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.resources.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.client.listeners.IStatusListener;

public class StatusListener implements ICommandOutputListener {
	private static boolean isFolder = false;
	private IStatusListener statusListener;

	public StatusListener(IStatusListener statusListener) {
		this.statusListener = statusListener;
	}

	public IStatus messageLine(String line, ICVSFolder commandRoot,
		IProgressMonitor monitor) {
		
		// We're only concerned about file revisions.
		if (line.startsWith("   Repository revision:")) {
			if (!line.startsWith("   Repository revision:	No revision control file")) {
				int separatingTabIndex = line.indexOf('\t', 24);
				String remoteRevision = line.substring(24, separatingTabIndex);

				// This is the full location on the server (e.g. /home/cvs/repo/project/file.txt)
				String fileLocation = line.substring(separatingTabIndex + 1, line.length() - 2);

				// This is the absolute remote pathincluding the repository root directory
				IPath fullPath = new Path(fileLocation);

				// If the status returns that the file is in the Attic, then remove the
				// Attic segment. This is because files added to a branch that are not in
				// the main trunk (HEAD) are added to the Attic but cvs does magic on update
				// to put them in the correct location.
				// (e.g. /project/Attic/file.txt -> /project/file.txt)
				if ((fullPath.segmentCount() >= 2) && (fullPath.segment(fullPath.segmentCount() - 2).equals("Attic"))) {
					String filename = fullPath.lastSegment();
					fullPath = fullPath.removeLastSegments(2);
					fullPath = fullPath.append(filename);
				}

				// Inform the listener about the file revision
				statusListener.fileStatus(fullPath, remoteRevision);
			}
		}
		return OK;
	}

	public IStatus errorLine(String line, ICVSFolder commandRoot,
		IProgressMonitor monitor) {
		if (line.startsWith("cvs server: conflict:")) {
			// We get this because we made up an entry line to send to the server
			// Just ignore it
			// XXX We should make this a warning!!!
			return OK;
		}
		if (line.startsWith("cvs server: Examining")) {
			isFolder = true;
			return OK;
		}
		if (isFolder && line.startsWith("cvs [server aborted]: could not chdir to")) {
			String folderPath = line.substring(41, line.indexOf(':', 42));
			// Pass null to listener indicating that the resource exists but does not have a revision number
			// (i.e. the resource is a folder)
			if (statusListener != null)
				// XXX We should be using that path relative to the root of the command (mRoot)!!!
				statusListener.fileStatus(new Path(folderPath).removeFirstSegments(1), IStatusListener.FOLDER_REVISION);
			isFolder = false;
			return OK;
		}
		return new Status(IStatus.ERROR, CVSProviderPlugin.ID, CVSException.IO_FAILED, line, null);
	}
}
