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

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.history.IFileRevision;
import org.eclipse.team.core.history.provider.FileHistory;
import org.eclipse.team.internal.ccvs.core.*;
import org.eclipse.team.internal.core.LocalFileRevision;

public class CVSFileHistory extends FileHistory {

	protected ICVSFile cvsFile;
	protected IFileRevision[] revisions;
	protected boolean includeLocalRevisions;
	protected boolean includesExists;
	
	public CVSFileHistory(ICVSFile file){
		this.cvsFile = file;
		this.includeLocalRevisions=false;
	}
	
	public IFileRevision[] getFileRevisions() {
		return revisions;
	}

	public void refresh(IProgressMonitor monitor) throws TeamException {
		if (revisions == null){
			monitor.beginTask("Refreshing CVS",300);
			try{
			ILogEntry[] entries = cvsFile.getLogEntries(new SubProgressMonitor(monitor, 200));
			IFileRevision[] convertedLocalRevisions = new IFileRevision[0];
			if (includeLocalRevisions){
				IResource localResource = cvsFile.getIResource();
				includesExists = false;
				if (localResource != null &&
					localResource instanceof IFile){
					//get the local revisions
					IFileState[] localRevisions =((IFile)localResource).getHistory(new SubProgressMonitor(monitor, 100));
					convertedLocalRevisions =convertToFileRevision(localRevisions, new SubProgressMonitor(monitor, 100));
					includesExists = (convertedLocalRevisions.length > 0);
				}
			}
			revisions = new IFileRevision[entries.length + convertedLocalRevisions.length];
			for (int i = 0; i < entries.length; i++) {
				revisions[i] = new CVSFileRevision(entries[i]);
			}
			System.arraycopy(convertedLocalRevisions, 0, revisions, entries.length,convertedLocalRevisions.length);
			
			
		} catch (CoreException e) {
		} finally {
			monitor.done();
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

	public IFileRevision getPredecessor(IFileRevision revision) {
		
		IFileRevision[] revisions = getFileRevisions();

		//the predecessor is the file with a timestamp that is the largest timestamp
		//from the set of all timestamps smaller than the root file's timestamp
		IFileRevision fileRevision=null;
		for (int i = 0; i < revisions.length; i++) {
			if(((CVSFileRevision)revisions[i]).isPredecessorOf(revision)){
				//no revision has been set as of yet
				if (fileRevision == null)
					fileRevision=revisions[i];
				//this revision is a predecessor - now check to see if it comes
				//after the current predecessor, if it does make it the current predecessor
				if (revisions[i].getTimestamp()>fileRevision.getTimestamp()){
					fileRevision=revisions[i];
				}
			}
		}
		
		return fileRevision;
	}



	public IFileRevision[] getDirectDescendents(IFileRevision revision)  {
		IFileRevision[] revisions = getFileRevisions();

		//the predecessor is the file with a timestamp that is the largest timestamp
		//from the set of all timestamps smaller than the root file's timestamp
		ArrayList directDescendents = new ArrayList();
		
		for (int i = 0; i < revisions.length; i++) {
			if(((CVSFileRevision)revisions[i]).isDescendentOf(revision)){
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
		
		int arrayLength=0;
		if (modified)
			arrayLength++;
		
		arrayLength+=localRevisions.length;
		
		IFileRevision[] fileRevisions = new IFileRevision[arrayLength];
		for (int i = 0; i < localRevisions.length; i++) {
			IFileState localFileState = localRevisions[i];
			LocalFileRevision localRevision = new LocalFileRevision(localFileState);
			fileRevisions[i] = localRevision;
		}
		
		if (modified){
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


}
