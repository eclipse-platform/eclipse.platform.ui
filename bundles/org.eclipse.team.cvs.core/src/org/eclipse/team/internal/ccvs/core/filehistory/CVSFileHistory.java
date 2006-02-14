/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.internal.ccvs.core.filehistory;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.osgi.util.NLS;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.history.IFileHistoryProvider;
import org.eclipse.team.core.history.IFileRevision;
import org.eclipse.team.core.history.provider.FileHistory;
import org.eclipse.team.internal.ccvs.core.*;
import org.eclipse.team.internal.core.LocalFileRevision;

public class CVSFileHistory extends FileHistory {

	protected ICVSFile cvsFile;
	//used to hold all revisions (changes based on filtering)
	protected IFileRevision[] revisions;
	//used to hold all of the remote revisions
	protected IFileRevision[] remoteRevisions;
	//used to hold all of the local revisions
	protected IFileRevision[] localRevisions;
	protected boolean includeLocalRevisions;
	protected boolean includeRemoteRevisions;
	protected boolean includesExists;
	protected boolean refetchRevisions;

	private int flag;

	/*
	 * Creates a new CVSFile history that will fetch remote revisions by default.
	 */
	public CVSFileHistory(ICVSFile file) {
		this.cvsFile = file;
		this.includeLocalRevisions = false;
		this.includeRemoteRevisions = true;
		this.refetchRevisions = true;
		this.flag = 0;
	}

	/*
	 * 
	 * Creates a new CVSFile history that will fetch remote revisions by default.
	 * The flag passed can be IFileHistoryProvider.SINGLE_REVISION or IFileHistoryProvider.SINGLE_LINE_OF_DESCENT
	 */
	public CVSFileHistory(ICVSFile file, int flag) {
		this.cvsFile = file;
		this.includeLocalRevisions = false;
		this.includeRemoteRevisions = true;
		this.refetchRevisions = true;
		this.flag = flag;
	}

	public IFileRevision[] getFileRevisions() {
		if (revisions == null)
			return new IFileRevision[0];
		return revisions;
	}

	public void refresh(IProgressMonitor monitor) throws TeamException {
		if (refetchRevisions) {
			monitor.beginTask(NLS.bind(CVSMessages.CVSFileHistory_0, cvsFile.getRepositoryRelativePath()), 300);
			try {
				ILogEntry[] entries = cvsFile.getLogEntries(new SubProgressMonitor(monitor, 200));
				if (flag == IFileHistoryProvider.SINGLE_REVISION) {
					String revisionNumber = cvsFile.getSyncInfo().getRevision();
					for (int i = 0; i < entries.length; i++) {
						if (entries[i].getRevision().equals(revisionNumber)) {
							remoteRevisions = new IFileRevision[] {new CVSFileRevision(entries[i])};
							revisions = new IFileRevision[1];
							//copy over remote revisions
							System.arraycopy(remoteRevisions, 0, revisions, 0, remoteRevisions.length);
							break;
						}
					}

				} else if (flag == IFileHistoryProvider.SINGLE_LINE_OF_DESCENT) {
					CVSTag tempTag = cvsFile.getSyncInfo().getTag();
					ArrayList entriesOfInterest = new ArrayList();
					for (int i = 0; i < entries.length; i++) {
						CVSTag[] tags = entries[i].getTags();
						for (int j = 0; j < tags.length; j++) {
							if (tags[j].getType() == tempTag.getType()) {
								if (tempTag.getType() == CVSTag.BRANCH && tempTag.getName().equals(tags[j].getName())) {
									entriesOfInterest.add(entries[i]);
									break;
								} else {
									entriesOfInterest.add(entries[i]);
									break;
								}
							}

						}
					}

					//always fetch the remote revisions, just filter them out from the returned array
					remoteRevisions = new IFileRevision[entriesOfInterest.size()];
					Iterator iter = entriesOfInterest.iterator();
					int i = 0;
					while (iter.hasNext()) {
						remoteRevisions[i++] = new CVSFileRevision((ILogEntry) iter.next());
					}

					//copy over remote revisions
					revisions = new IFileRevision[remoteRevisions.length];
					System.arraycopy(remoteRevisions, 0, revisions, 0, remoteRevisions.length);

				} else {
					localRevisions = new IFileRevision[0];
					//always fetch the local revisions, just filter them out from the returned array if not wanted
					IResource localResource = cvsFile.getIResource();
					includesExists = false;
					if (localResource != null && localResource instanceof IFile) {
						//get the local revisions
						IFileState[] localHistoryState = ((IFile) localResource).getHistory(new SubProgressMonitor(monitor, 100));
						localRevisions = convertToFileRevision(localHistoryState, new SubProgressMonitor(monitor, 100));
						includesExists = (localRevisions.length > 0);
					}

					//always fetch the remote revisions, just filter them out from the returned array
					remoteRevisions = new IFileRevision[entries.length];
					for (int i = 0; i < entries.length; i++) {
						remoteRevisions[i] = new CVSFileRevision(entries[i]);
					}

					revisions = new IFileRevision[0];
					arrangeRevisions();
				}
			} catch (CoreException e) {
			} finally {
				monitor.done();
			}
		} else {
			//don't refetch revisions just return revisions with local revisions as requested
			arrangeRevisions();
		}
	}

	private void arrangeRevisions() {
		if (revisions != null) {
			if (includeLocalRevisions && includesExists && includeRemoteRevisions) {
				//Local + Remote mode
				revisions = new IFileRevision[remoteRevisions.length + localRevisions.length];
				//copy over remote revisions
				System.arraycopy(remoteRevisions, 0, revisions, 0, remoteRevisions.length);
				//copy over local revisions
				System.arraycopy(localRevisions, 0, revisions, remoteRevisions.length, localRevisions.length);
			} else if (includeLocalRevisions && includesExists) {
				//Local mode only
				revisions = new IFileRevision[localRevisions.length];
				//copy over local revisions
				System.arraycopy(localRevisions, 0, revisions, 0, localRevisions.length);
			} else if (includeRemoteRevisions) {
				//Remote mode and fall through for Local + Remote mode where no Locals exist
				revisions = new IFileRevision[remoteRevisions.length];
				//copy over remote revisions
				System.arraycopy(remoteRevisions, 0, revisions, 0, remoteRevisions.length);
			}
		}
	}

	public IFileRevision getFileRevision(String id) {
		IFileRevision[] revisions = getFileRevisions();
		for (int i = 0; i < revisions.length; i++) {
			if (revisions[i].getContentIdentifier().equals(id))
				return revisions[i];
		}
		return null;
	}

	public IFileRevision[] getContributors(IFileRevision revision) {

		IFileRevision[] revisions = getFileRevisions();

		//the predecessor is the file with a timestamp that is the largest timestamp
		//from the set of all timestamps smaller than the root file's timestamp
		IFileRevision fileRevision = null;
		for (int i = 0; i < revisions.length; i++) {
			if (((CVSFileRevision) revisions[i]).isPredecessorOf(revision)) {
				//no revision has been set as of yet
				if (fileRevision == null)
					fileRevision = revisions[i];
				//this revision is a predecessor - now check to see if it comes
				//after the current predecessor, if it does make it the current predecessor
				if (revisions[i].getTimestamp() > fileRevision.getTimestamp()) {
					fileRevision = revisions[i];
				}
			}
		}
		if (fileRevision == null)
			return new IFileRevision[0];
		return new IFileRevision[] {fileRevision};
	}

	public IFileRevision[] getTargets(IFileRevision revision) {
		IFileRevision[] revisions = getFileRevisions();

		//the predecessor is the file with a timestamp that is the largest timestamp
		//from the set of all timestamps smaller than the root file's timestamp
		ArrayList directDescendents = new ArrayList();

		for (int i = 0; i < revisions.length; i++) {
			if (((CVSFileRevision) revisions[i]).isDescendentOf(revision)) {
				directDescendents.add(revisions[i]);
			}
		}
		return (IFileRevision[]) directDescendents.toArray(new IFileRevision[directDescendents.size()]);
	}

	private IFileRevision[] convertToFileRevision(IFileState[] localRevisions, IProgressMonitor monitor) {
		boolean modified = false;
		try {
			modified = cvsFile.isModified(monitor);
		} catch (CVSException e) {
		}

		int arrayLength = 0;
		if (modified)
			arrayLength++;

		arrayLength += localRevisions.length;

		IFileRevision[] fileRevisions = new IFileRevision[arrayLength];
		for (int i = 0; i < localRevisions.length; i++) {
			IFileState localFileState = localRevisions[i];
			LocalFileRevision localRevision = new LocalFileRevision(localFileState);
			fileRevisions[i] = localRevision;
		}

		if (modified) {
			//local file exists
			IFile localFile = (IFile) cvsFile.getIResource();
			LocalFileRevision currentFile = new LocalFileRevision(localFile);
			CVSFileHistoryProvider provider = new CVSFileHistoryProvider();
			currentFile.setBaseRevision(provider.getWorkspaceFileRevision(localFile));
			fileRevisions[localRevisions.length] = currentFile;
		}

		return fileRevisions;
	}

	public void includeLocalRevisions(boolean flag) {
		this.includeLocalRevisions = flag;
	}

	public boolean getIncludesExists() {
		return includesExists;
	}

	public void setRefetchRevisions(boolean refetch) {
		this.refetchRevisions = refetch;
	}

	public void includeRemoteRevisions(boolean flag) {
		this.includeRemoteRevisions = flag;
	}

}
