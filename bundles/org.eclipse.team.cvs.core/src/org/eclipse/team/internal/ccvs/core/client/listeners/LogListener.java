/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.core.client.listeners;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.osgi.util.NLS;
import org.eclipse.team.internal.ccvs.core.*;
import org.eclipse.team.internal.ccvs.core.client.CommandOutputListener;
import org.eclipse.team.internal.ccvs.core.resources.RemoteFile;
import org.eclipse.team.internal.ccvs.core.util.Util;

/**
 * Log listener that parses the log entries returned from the
 * server but delegates the handling of the entries to a subclass.
 */
public class LogListener extends CommandOutputListener {
    
    /*
     * A new format for log dates was introduced in 1.12.9
     */
    private static final String LOG_TIMESTAMP_FORMAT_OLD= "yyyy/MM/dd HH:mm:ss zzz";//$NON-NLS-1$
    private static final String LOG_TIMESTAMP_FORMAT= "yyyy-MM-dd HH:mm:ss zzz";//$NON-NLS-1$
    private static final Locale LOG_TIMESTAMP_LOCALE= Locale.US;
    
    // Server message prefix used for error detection
    private static final String NOTHING_KNOWN_ABOUT = "nothing known about "; //$NON-NLS-1$

    // States of log accumulation.
    private final int DONE = 4;
    private final int COMMENT = 3;
    private final int REVISION = 2;
    private final int SYMBOLIC_NAMES = 1;
    private final int BEGIN = 0;
    
    // Instance variables for accumulating Log information
    private RemoteFile currentFile;
    private int state = BEGIN;
    private StringBuffer comment;
    private String fileState;
    private String revision;
    private String author;
    private String creationDate;
    private List tagRevisions = new ArrayList(5);
    private List tagNames = new ArrayList(5);
    
    private final ILogEntryListener listener;
    
    /**
     * Create a log listener for receiving entries for one or more files.
     */
    public LogListener(ILogEntryListener listener) {
        this.listener = listener;
    }

    public LogListener(RemoteFile file, ILogEntryListener listener) {
        this(listener);
        this.currentFile = file;   
    }

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

    public IStatus messageLine(String line, ICVSRepositoryLocation location, ICVSFolder commandRoot, IProgressMonitor monitor) {
    	// Fields we will find in the log for a file
    	// keys = String (tag name), values = String (tag revision number) */
    	switch (state) {
    		case BEGIN:
    			if (line.startsWith("RCS file: ")) { //$NON-NLS-1$
    				// We are starting to recieve the log for a file
    				String fileName = getRelativeFilePath(location, line.substring(10).trim());
    				if (fileName == null) {
    				    currentFile = null;
    					handleInvalidFileName(location, fileName);
    				} else {
    			        if (currentFile == null || !currentFile.getRepositoryRelativePath().equals(fileName)) {
    			        	// We are starting another file
    			        	beginFile(location, fileName);
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
    						NLS.bind(CVSMessages.LogListener_invalidRevisionFormat, new String[] { tagName, tagRevision }), null); 
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
    		Date date = convertFromLogTime(creationDate);
    		if (currentFile != null) {
    			LogEntry entry = new LogEntry(currentFile, revision, author, date,
    				comment.toString(), fileState, (CVSTag[]) thisRevisionTags.toArray(new CVSTag[0]));
    			addEntry(entry);
    		}
    		state = BEGIN;
    	}
    	return OK;
    }

    protected void beginFile(ICVSRepositoryLocation location, String fileName) {
    	currentFile = RemoteFile.create(fileName, location);
    	tagNames.clear();
    	tagRevisions.clear();  
    }

    protected void addEntry(LogEntry entry) {
        listener.handleLogEntryReceived(entry);
    }

    protected void handleInvalidFileName(ICVSRepositoryLocation location, String badFilePath) {
        CVSProviderPlugin.log(IStatus.WARNING, "Invalid file path '" + badFilePath + "' received from " + location.toString(), null); //$NON-NLS-1$ //$NON-NLS-2$
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
    
    /**
     * Converts a time stamp as sent from a cvs server for a "log" command into a
     * <code>Date</code>.
     */
    private Date convertFromLogTime(String modTime) {
        String timestampFormat = LOG_TIMESTAMP_FORMAT;
        // Compatibility for older cvs version (pre 1.12.9)
        if (modTime.length() > 4 && modTime.charAt(4) == '/')
            timestampFormat = LOG_TIMESTAMP_FORMAT_OLD;
            
        SimpleDateFormat format= new SimpleDateFormat(timestampFormat, 
            LOG_TIMESTAMP_LOCALE);
        try {
            return format.parse(modTime);
        } catch (ParseException e) {
            // fallback is to return null
            return null;
        }
    }
}
