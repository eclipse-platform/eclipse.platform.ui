package org.eclipse.team.internal.ccvs.core.response.custom;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.PrintStream;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.connection.Connection;
import org.eclipse.team.internal.ccvs.core.resources.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.response.*;

public class StatusMessageHandler extends ResponseHandler {

	/** The response key */
	public static final String NAME = "M";

	IStatusListener statusListener;

	public StatusMessageHandler(IStatusListener statusListener) {
		this.statusListener = statusListener;
	}
	/**
	 * @see ResponseHandler#getName()
	 */
	public String getName() {
		return NAME;
	}
	/**
	 * Handle the response. This response sends the message to the
	 * progress monitor using <code>IProgressMonitor.subTask(Strin)
	 * </code>.
	 */
	public void handle(
		Connection context,
		PrintStream messageOutput,
		ICVSFolder mRoot,
		IProgressMonitor monitor)
			throws CVSException {
		String line = context.readLine();
		StringBuffer tags = new StringBuffer(10);
		if (line.startsWith("   Repository revision:")) {
			if (!line.startsWith("   Repository revision:	No revision control file")) {
				int separatingTabIndex = line.indexOf('\t', 24);
				String remoteRevision = line.substring(24, separatingTabIndex);

				// This is the full location on the server (e.g. /home/cvs/repo/project/file.txt)
				String fileLocation = line.substring(separatingTabIndex + 1, line.length() - 2);

				// This is the project relative path (e.g. /project/file.txt)
				IPath fullPath =
					new Path(fileLocation.substring(context.getRootDirectory().length() + 1));

				// If the status returns that the file is in the Attic, then remove the
				// Attic segment. This is because files added to a branch that are not in
				// the main trunk (HEAD) are added to the Attic but cvs does magic on update
				// to put them in the correct location.
				// (e.g. /project/Attic/file.txt -> /project/file.txt)
				if (fullPath.segment(fullPath.segmentCount() - 2).equals("Attic")) {
					String filename = fullPath.lastSegment();
					fullPath = fullPath.removeLastSegments(2);
					fullPath = fullPath.append(filename);
				}

				// Try and get the tags from the end of the status output
				statusListener.fileStatus(fullPath.removeFirstSegments(1), remoteRevision);
			}
		}
		return;
	}
}