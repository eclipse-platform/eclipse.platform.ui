package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.internal.boot.update.*;
import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Session Manager executes defined sessions, and manages the update and update history logs.
 * Logs look like this:
 *
 * <session timestamp="xxxx" status="complete" attempts="1">
 *   <operation action="urltocache" status="complete" attempts="1">
 *      <parcel type="component" id="xxx" action="add" status="complete" attempts="1">
 *         <item id="xxx" source="file:///C:/temp/sample_in.java" target="file:///C:/temp/sample_out.java" status="complete" attempts="1">
 *            <message text="successful" timestamp="Thu Mar 01 09:56:49 EST 2001" />
 *         </item>
 *      </parcel>
 *   </operation>
 * </session>
 *
 *
 *  Each element is represented by an UpdateManagerXyz class and have specific
 * member functions for accessing domain specific attributes and child elements.
 * The "status" attribute can be one of "pending", "failed", or "complete".
 */

public class UMSessionManager {
	
	protected Log _logUpdate  = null;
	protected Log _logHistory = null;
	
	protected URL _urlUpdate  = null;
	protected URL _urlHistory = null;

	protected ArrayList _alUpdateSessions = new ArrayList();
/**
 * Constructs an UpdateManager with the update log and history log URLs.
 * The boolean specifies whether an exception should be thrown if one of
 * the logs is not found.
 */
public UMSessionManager(URL urlUpdateLog, URL urlHistoryLog, boolean notifyIfLogNotFound) throws LogStoreException {

	_urlUpdate = urlUpdateLog;
	_urlHistory = urlHistoryLog;

	// Attempt to open the two logs
	//-----------------------------
	_logUpdate = new Log();
	_logHistory = new Log();

	// If the files do not exist
	// notify if required.  Otherwise assume new files
	//------------------------------------------------
	try {
		_logUpdate.load(_urlUpdate);
	}
	catch (LogStoreException ex) {
//		_logUpdate.printEntryTree(0);
		if (notifyIfLogNotFound == true) {
			throw ex;
		}
	}

	// Create the update session structure from the log
	//-------------------------------------------------
	buildTreeFromLog( _logUpdate );

/*	
	try {
		_logHistory.load(_urlUpdate);
	}
	catch (LogStoreException ex) {
		
		if (notifyIfLogNotFound == true) {
			throw ex;
		}
	}

	_logHistory.printEntryTree(0);
*/
}
/**
 * Creates a tree of sessions from the given log.
 * @param logUpdate org.eclipse.update.internal.core.Log
 * @param alUpdate java.util.ArrayList
 */
private void buildTreeFromLog(Log logUpdate ) {
	
	ILogEntry[] entryChildren = logUpdate.getChildEntries();

	UMSessionManagerSession session = null;
	
	for( int i=0; i<entryChildren.length; ++i )
	{
		session = new UMSessionManagerSession( entryChildren[i] );

		_alUpdateSessions.add( session );
		
		session.buildTreeFromLog( entryChildren[i] );
	}
	
	return;
}
/**
 * Creates a new update session where pending operations may be defined for
 * completion.
 * @return org.eclipse.update.internal.core.UMSessionManagerSession
 */
public UMSessionManagerSession createUpdateSession() {

	UMSessionManagerSession umSession = null;
	
	if( _logUpdate != null )
	{
		ILogEntry entrySession = new LogEntry( _logUpdate, UpdateManagerConstants.STRING_SESSION );
		
		_logUpdate.addChildEntry( entrySession );

		umSession = new UMSessionManagerSession( entrySession );

		umSession.buildTreeFromLog( entrySession );

		_alUpdateSessions.add( umSession );
	}
	
	return umSession;
}
/**
 * Runs all sessions
 */
public boolean executePendingSessions(IProgressMonitor progressMonitor) {

	// Run each of the failed sessions
	//--------------------------------
	boolean bSuccess = true;

	UMSessionManagerSession session = null;

	Iterator iter = _alUpdateSessions.iterator();

	while (iter.hasNext() == true) {

		session = (UMSessionManagerSession) iter.next();

		if (session.getStatus().equals(UpdateManagerConstants.STATUS_PENDING) == true) {
			if (session.execute(progressMonitor) == false) {
				bSuccess = false;
			}
		}
	}

	return bSuccess;
}
/**
 * Runs one session
 */
public boolean executeSession(UMSessionManagerSession session, IProgressMonitor progressMonitor) {

	if (session == null)
		return false;

	// Run the session
	//----------------
	return session.execute(progressMonitor);
}
/**
 * Runs one session
 */
public boolean executeSessionUndo(UMSessionManagerSession session, IProgressMonitor progressMonitor) {

	if (session == null)
		return false;

	// Run the session
	//----------------
	return session.executeUndo(progressMonitor);
}
/**
 * Returns an array of sessions that are complete.
 * @return org.eclipse.update.internal.core.UMSessionManagerSession[]
 */
private UMSessionManagerSession[] getUpdateSessionsCompleted() {

	// Obtain the successfully completed sessions
	//-------------------------------------------
	UMSessionManagerSession session = null;

	ArrayList alSessionsCompleted = new ArrayList();

	Iterator iter = _alUpdateSessions.iterator();

	while (iter.hasNext() == true) {

		session = (UMSessionManagerSession) iter.next();

		if (session.isComplete() == true) {
			alSessionsCompleted.add(session);
		}
	}

	Object[] objArray = alSessionsCompleted.toArray();
	UMSessionManagerSession[] sessions = new UMSessionManagerSession[objArray.length];
	System.arraycopy(objArray, 0, sessions, 0, objArray.length);

	return sessions;
}
/**
 * Removes an existing update session
 *
 */
private boolean removeUpdateSession(UMSessionManagerSession session) {

	if (session != null && _logUpdate != null) {

	    // Ensure session exists
	    //----------------------
		if (_alUpdateSessions.contains(session) == false) {
			return false;
		}

		// Remove the log entry
		//---------------------
		if (session.getLogEntry() != null) {

			if (_logUpdate.removeChildEntry(session.getLogEntry()) == false) {
				return false;
			}

			// Remove the session
			//-------------------
			_alUpdateSessions.remove(session);
		}
	}

	return true;
}
/**
 * Saves the current state of the update log.
 */
public void saveUpdateLog() throws LogStoreException {
	_logUpdate.save( _urlUpdate );
}
/**
 * Moves all completed sessions in the update log to the history log.
 * This method does not parse the history file.  Sessions are written to
 * the end of the history log and removed from the update log tree, and update
 * session tree.  The update log is saved.
 */
public void updateAndSaveLogs() throws LogStoreException {

	// Check for history log URL
	// If none, save the update log
	//-----------------------------
	if (_urlHistory == null) {
		saveUpdateLog();
		return;
	}

	// Obtain a list of completed update sessions
	//-------------------------------------------
	UMSessionManagerSession[] sessionsCompleted = getUpdateSessionsCompleted();

	// If there are none, save the update log
	//---------------------------------------
	if (sessionsCompleted.length <= 0) {
		saveUpdateLog();
		return;
	}

	// Attempt to open the output log
	//-------------------------------
	String strFilespec = UMEclipseTree.getFileInPlatformString(_urlHistory);
	

	File file = new File(strFilespec);

	boolean bExists = file.exists();

	if (bExists == false) {
		try {
			bExists = file.createNewFile();
		}
		catch (IOException ex) {
			return;
		}
	}

	// Create a file writer and seek to the end of the file
	//-----------------------------------------------------
	RandomAccessFile fileWriter = null;

	if (bExists == true) {
		try {
			fileWriter = new RandomAccessFile(file, "rw");
			long lLength = fileWriter.length();
			fileWriter.seek(lLength);
		}
		catch (IOException ex) {
			fileWriter = null;
		}
	}

	// Write out each session log to the history log
	// Remove the session log from the update log
	// Save both logs
	//----------------------------------------------
	StringBuffer strb = new StringBuffer();

	if (fileWriter != null) {

		ILogEntry logEntry = null;

		for (int i = 0; i < sessionsCompleted.length; ++i) {

			logEntry = sessionsCompleted[i].getLogEntry();

			if (logEntry != null) {
				logEntry.printPersistentEntryString(strb, 0);
			}
		}
	}

	// Write the buffer to the end of the history file
	//------------------------------------------------
	if (strb.length() > 0) {
		try {
			fileWriter.write(strb.toString().getBytes());
			fileWriter.close();
		}
		catch (IOException ex) {

		}
	}

	// Remove the update sessions from the update log
	//-----------------------------------------------
	for (int i = 0; i < sessionsCompleted.length; ++i) {
		removeUpdateSession(sessionsCompleted[i]);
	}

	// Save the update log to keep logs in sync
	//-----------------------------------------
	saveUpdateLog();
}
}
