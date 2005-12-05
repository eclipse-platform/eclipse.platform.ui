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

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.filehistory.IFileRevision;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.ICVSFile;
import org.eclipse.team.internal.ccvs.core.ILogEntry;
import org.eclipse.team.internal.core.FileHistory;

public class CVSFileHistory extends FileHistory {

	protected ICVSFile cvsFile;
	protected CVSFileRevision[] revisions;
	
	public CVSFileHistory(ICVSFile file){
		this.cvsFile = file;
	}
	
	public IFileRevision[] getFileRevisions() throws CVSException {
		
		try {
			return getCVSFileRevisions();
		} catch (TeamException e) {
			throw CVSException.wrapException(e);
		} 
	}

	private CVSFileRevision[] getCVSFileRevisions() throws TeamException {
		if (revisions == null){
			ILogEntry[] entries = cvsFile.getLogEntries(new NullProgressMonitor());
			revisions = new CVSFileRevision[entries.length];
			for (int i = 0; i < entries.length; i++) {
				revisions[i] = new CVSFileRevision(entries[i]);
			}
		}
		
		return revisions;
	}

	public IFileRevision getFileRevision(String id) throws TeamException {
		
		CVSFileRevision[] revisions = getCVSFileRevisions();
		
		for (int i = 0; i < revisions.length; i++) {
			if (revisions[i].getContentIndentifier().equals(id))
				return revisions[i];
		}
		return null;
	}

	public IFileRevision getPredecessor(IFileRevision revision) throws TeamException {
		
		CVSFileRevision[] revisions = getCVSFileRevisions();

		//the predecessor is the file with a timestamp that is the largest timestamp
		//from the set of all timestamps smaller than the root file's timestamp
		CVSFileRevision fileRevision=null;
		for (int i = 0; i < revisions.length; i++) {
			if(revisions[i].isPredecessorOf(revision)){
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



	public IFileRevision[] getDirectDescendents(IFileRevision revision) throws TeamException {
		CVSFileRevision[] revisions = getCVSFileRevisions();

		//the predecessor is the file with a timestamp that is the largest timestamp
		//from the set of all timestamps smaller than the root file's timestamp
		CVSFileRevision fileRevision=null;
		ArrayList directDescendents = new ArrayList();
		
		for (int i = 0; i < revisions.length; i++) {
			if(revisions[i].isDescendentOf(revision)){
				directDescendents.add(revisions[i]);
			}
		}
		return (IFileRevision[]) directDescendents.toArray(new IFileRevision[directDescendents.size()]);
	}

}
