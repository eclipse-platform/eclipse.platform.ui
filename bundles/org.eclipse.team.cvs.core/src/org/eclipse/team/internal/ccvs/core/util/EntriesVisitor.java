package org.eclipse.team.internal.ccvs.core.util;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;

import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.resources.api.FileProperties;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedFile;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedFolder;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedVisitor;

/**
 * Merges all the Entries.Log files with the corriponding
 * Entries files in the CVS folders in this folder and all
 * the subfolders (deep).<br>
 * 
 * (Entries.Log files contain changes in the Entries files 
 * read a CVS command line client manual)
 */
public class EntriesVisitor implements IManagedVisitor {
	
	public static final String ENTRIES_PROPERTY = FileProperties.ENTRIES;
	public static final String ENTRIES_LOG_PROPERTY="Entries.Log";
	public static final String FOLDER_TAG="D";
	public static final String ADD_TAG="A ";
	public static final String REMOVE_TAG="R ";
	
	/**
	 * Constructor for EntriesVisitor.
	 */
	public EntriesVisitor() {
		super();
	}

	/**
	 * @see IManagedVisitor#visitFile(IManagedFile)
	 */
	public void visitFile(IManagedFile mFile) {
		// Do nothing ... files do not have cvs-folders in that we could
		// possible merge
	}

	/**
	 * @see IManagedVisitor#visitFolder(IManagedFolder)
	 */
	public void visitFolder(IManagedFolder mFolder) throws CVSException {
		
		if (!mFolder.exists()) {
			return;
		}
		
		// We still go into the subdirectories if we are not in a cvs-folder,
		// because we might be in the root (on a checkout in a testcase).
		//
		// The case that we are not in a cvsFolder could be changed to an 
		// assert or even an exception. Make sure you break nothing.
		if (mFolder.isCVSFolder()) {
			mergeEntries(mFolder);
		}
		
		mFolder.acceptChildren(this);
		
	}

	/**
	 * Get an entries and an entries.log file in a cvs-folder of
	 * the cvsfolder and merge them into one new entries-file
	 */
	public static void mergeEntries(IManagedFolder mFolder) throws CVSException {
		
		String[] entrieLogs;
		String[] entries;
		ArrayList newEntries = new ArrayList();
		
		entrieLogs = mFolder.getProperty(ENTRIES_LOG_PROPERTY);
		entries =  mFolder.getProperty(ENTRIES_PROPERTY);
		
		if (entrieLogs == null) {
			return;
		}
		
		for (int i=0; i<entries.length; i++) {
			if (!entries[i].equals(FOLDER_TAG)) {
				newEntries.add(entries[i]);
			}
		}
		
		for (int i=0; i<entrieLogs.length; i++) {
			if (entrieLogs[i].startsWith(ADD_TAG)) {
				newEntries.add(entrieLogs[i].substring(ADD_TAG.length()));
			} else if (entrieLogs[i].startsWith(REMOVE_TAG)) {
				newEntries.remove(entrieLogs[i].substring(REMOVE_TAG.length()));
			}
		}
		
		newEntries.add(FOLDER_TAG);
		
		entries = (String[]) newEntries.toArray(new String[newEntries.size()]);
		
		mFolder.setProperty(ENTRIES_PROPERTY,entries);
		mFolder.setProperty(ENTRIES_LOG_PROPERTY,null);
	}
}

