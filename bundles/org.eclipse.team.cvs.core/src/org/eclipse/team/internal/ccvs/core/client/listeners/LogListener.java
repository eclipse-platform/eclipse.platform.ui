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


import java.util.*;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.*;
import org.eclipse.team.internal.ccvs.core.client.CommandOutputListener;
import org.eclipse.team.internal.ccvs.core.resources.RemoteFile;
import org.eclipse.team.internal.ccvs.core.util.Util;

public class LogListener extends CommandOutputListener {
	private Map entries = new HashMap(); /* Map repo relative path->List of LogEntry */
	private RemoteFile currentFile;
	private List currentFileEntries;
	
	// state
	private final int BEGIN = 0, SYMBOLIC_NAMES = 1, REVISION = 2, COMMENT = 3, DONE = 4;
	private List tagNames = new ArrayList(5);
	private List tagRevisions = new ArrayList(5);
	private int state = BEGIN;  // current state
	private String creationDate;
	private String author;
	private String revision;    // revision number
	private String fileState;   //
	private StringBuffer comment; // comment
	

	private static final String NOTHING_KNOWN_ABOUT = "nothing known about "; //$NON-NLS-1$
	
	/**
	 * Constructor used to get the log information for one or more files.
	 */
	public LogListener() {
		this.currentFile = null;
		this.currentFileEntries = new ArrayList();
	}
	
	/**
	 * Constructor used to get the log information for one file.
	 */
	public LogListener(RemoteFile file, List entries) {
		this.currentFile = file;
		this.currentFileEntries = entries;
		this.entries.put(file.getRepositoryRelativePath(), entries);
	}
	
	/**
	 * Return the log entry for the given remote file. The revision
	 * of the remote file is used to determine which log entry to
	 * return. If no log entry was fetched, <code>null</code>
	 * is returned.
	 */
	public ILogEntry getEntryFor(ICVSRemoteFile file) {
		List fileEntries = (List)entries.get(file.getRepositoryRelativePath());
		if (fileEntries != null) {
			for (Iterator iter = fileEntries.iterator(); iter.hasNext();) {
				ILogEntry entry = (ILogEntry) iter.next();
				try {
					if (entry.getRevision().equals(file.getRevision())) {
						return entry;
					}
				} catch (TeamException e) {
					// Log and continue
					CVSProviderPlugin.log(e);
				}
			}
		}
		return null;
	}
	
	public ILogEntry[] getEntriesFor(ICVSRemoteFile file) {
		List fileEntries = (List)entries.get(file.getRepositoryRelativePath());
		if (fileEntries != null) {
			return (ILogEntry[]) fileEntries.toArray(new ILogEntry[fileEntries.size()]);
		}
		return new ILogEntry[0];
	}

	public IStatus messageLine(String line, ICVSRepositoryLocation location, ICVSFolder commandRoot,
		IProgressMonitor monitor) {
		// Fields we will find in the log for a file
		// keys = String (tag name), values = String (tag revision number) */
		switch (state) {
			case BEGIN:
				if (line.startsWith("RCS file: ")) { //$NON-NLS-1$
					// We are starting to recieve the log for a file
					String fileName = getRelativeFilePath(location, line.substring(10).trim());
					if (fileName == null) {
						// We couldn't determine the file name so dump the entries
						currentFile = null;
						currentFileEntries = new ArrayList();
					} else {
						if (currentFile == null || !currentFile.getRepositoryRelativePath().equals(fileName)) {
							// We are starting another file
							currentFile = RemoteFile.create(fileName, location);
							currentFileEntries = (List)entries.get(currentFile.getRepositoryRelativePath());
							if (currentFileEntries == null) {
								currentFileEntries = new ArrayList();
								entries.put(currentFile.getRepositoryRelativePath(), currentFileEntries);
							}
							tagNames.clear();
							tagRevisions.clear();
						}
					}
				} else  if (line.startsWith("symbolic names:")) { //$NON-NLS-1$
					state = SYMBOLIC_NAMES;
				} else if (line.startsWith("revision ")) { //$NON-NLS-1$
					revision = line.substring(9);
					state = REVISION;
				}
				break;
			case SYMBOLIC_NAMES:
				if (line.startsWith("keyword substitution:")) { //$NON-NLS-1$
					state = BEGIN;
				} else {
					int firstColon = line.indexOf(':');
					String tagName = line.substring(1, firstColon);
					String tagRevision = line.substring(firstColon + 2);
					tagNames.add(tagName);
					tagRevisions.add(tagRevision);
				}
				break;
			case REVISION:
				// date: 2000/06/19 04:56:21;  author: somebody;  state: Exp;  lines: +114 -45
				// get the creation date
				int endOfDateIndex = line.indexOf(';', 6);
				creationDate = line.substring(6, endOfDateIndex) + " GMT"; //$NON-NLS-1$
	
				// get the author name
				int endOfAuthorIndex = line.indexOf(';', endOfDateIndex + 1);
				author = line.substring(endOfDateIndex + 11, endOfAuthorIndex);
	
				// get the file state (because this revision might be "dead")
				fileState = line.substring(endOfAuthorIndex + 10, line.indexOf(';', endOfAuthorIndex + 1));
				comment = new StringBuffer();
				state = COMMENT;
				break;
			case COMMENT:
				// skip next line (info about branches) if it exists, if not then it is a comment line.
				if (line.startsWith("branches:")) break; //$NON-NLS-1$
				if (line.equals("=============================================================================") //$NON-NLS-1$
					|| line.equals("----------------------------")) { //$NON-NLS-1$
					state = DONE;
					break;
				}
				if (comment.length() != 0) comment.append('\n');
				comment.append(line);
				break;
		}
		if (state == DONE) {
			// we are only interested in tag names for this revision, remove all others.
			List thisRevisionTags = new ArrayList(3);
			for (int i = 0; i < tagNames.size(); i++) {
				String tagName = (String) tagNames.get(i);
				String tagRevision = (String) tagRevisions.get(i);
				// If this is a branch tag then only include this tag with the revision
				// that is the root of this branch (e.g. 1.1 is root of branch 1.1.2).
				boolean isBranch = isBranchTag(tagRevision);
				if (isBranch) {
					int lastDot = tagRevision.lastIndexOf('.');
					if (lastDot == -1) {
						CVSProviderPlugin.log(IStatus.ERROR, 
							Policy.bind("LogListener.invalidRevisionFormat", tagName, tagRevision), null); //$NON-NLS-1$
					} else {
						if (tagRevision.charAt(lastDot - 1) == '0' && tagRevision.charAt(lastDot - 2) == '.') {
							lastDot = lastDot - 2;
						}
						tagRevision = tagRevision.substring(0, lastDot);
					}
				}
				if (tagRevision.equals(revision)) {
					int type = isBranch ? CVSTag.BRANCH : CVSTag.VERSION;
					thisRevisionTags.add(new CVSTag(tagName, type));
				}
			}
			Date date = DateUtil.convertFromLogTime(creationDate);
			if (currentFile != null) {
				LogEntry entry = new LogEntry(currentFile, revision, author, date,
					comment.toString(), fileState, (CVSTag[]) thisRevisionTags.toArray(new CVSTag[0]));
				currentFileEntries.add(entry);
			}
			state = BEGIN;
		}
		return OK;
	}

	public IStatus errorLine(String line, ICVSRepositoryLocation location, ICVSFolder commandRoot, IProgressMonitor monitor) {
		String serverMessage = getServerMessage(line, location);
		if (serverMessage != null) {
			// look for the following condition
			// E cvs server: nothing known about fileName
			if (serverMessage.startsWith(NOTHING_KNOWN_ABOUT)) {
				return new CVSStatus(IStatus.ERROR, CVSStatus.DOES_NOT_EXIST, commandRoot, line);
			}
		}
		return OK;
	}
	
	/** branch tags have odd number of segments or have
	 *  an even number with a zero as the second last segment
	 *  e.g: 1.1.1, 1.26.0.2 are branch revision numbers */
	protected boolean isBranchTag(String tagName) {
		// First check if we have an odd number of segments (i.e. even number of dots)
		int numberOfDots = 0;
		int lastDot = 0;
		for (int i = 0; i < tagName.length(); i++) {
			if (tagName.charAt(i) == '.') {
				numberOfDots++;
				lastDot = i;
			}
		}
		if ((numberOfDots % 2) == 0) return true;
		if (numberOfDots == 1) return false;
		
		// If not, check if the second lat segment is a zero
		if (tagName.charAt(lastDot - 1) == '0' && tagName.charAt(lastDot - 2) == '.') return true;
		return false;
	}
	
	/*
	 * Return the file path as a relative path to the 
	 * repository location
	 */
	private String getRelativeFilePath(ICVSRepositoryLocation location, String fileName) {
		if (fileName.endsWith(",v")) { //$NON-NLS-1$
			fileName = fileName.substring(0, fileName.length() - 2);
		}
		fileName = Util.removeAtticSegment(fileName);
		String rootDirectory = location.getRootDirectory();
		if (fileName.startsWith(rootDirectory)) {
			try {
				fileName = Util.getRelativePath(rootDirectory, fileName);
			} catch (CVSException e) {
				CVSProviderPlugin.log(e);
				return null;
			}
		}
		return fileName;
	}
}
