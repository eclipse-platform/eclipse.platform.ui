package org.eclipse.update.internal.core;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.internal.boot.update.*;
import java.util.Date;
import java.util.*;

/**
 * This class represents a session in which update manager operations are
 * executed.  This class contains parcel objects and has status and timestamp
 * as its attributes.  The timestamp attribute is automatically set when the session
 * is created for the first time.
 */

public class UMSessionManagerSession extends UMSessionManagerEntry {
	protected ArrayList _alParcels = new ArrayList();
	protected ILogEntryProperty _propertyTimestamp = null;
/**
 * UpdateManagerSession constructor comment.
 */
public UMSessionManagerSession(ILogEntry logEntry) {
	super( logEntry );

	// Timestamp
	//----------
	_propertyTimestamp = _logEntry.getProperty(UpdateManagerConstants.STRING_TIMESTAMP);

	if (_propertyTimestamp == null) {
		_propertyTimestamp = new LogEntryProperty(logEntry, UpdateManagerConstants.STRING_TIMESTAMP, new Date().toString() );
		logEntry.addProperty( _propertyTimestamp );
	}
}
/**
 * @param logEntry org.eclipse.update.internal.core.LogEntry
 */
public void buildTreeFromLog(ILogEntry logEntry) {
	
	super.buildTreeFromLog( logEntry );
	
	// Timestamp
	//----------
	ILogEntryProperty property = _logEntry.getProperty(UpdateManagerConstants.STRING_TIMESTAMP);

	if (property != null) {
		_propertyTimestamp = property;
	}

	
	ILogEntry[] entries = logEntry.getChildEntries();
	
	// Parcels
	//--------
	int iIndex = 0;
	UMSessionManagerParcel parcel = null;
	
	for( int i = 0; i < entries.length; ++i) {
		if (entries[i] != null && entries[i].getName().equals(UpdateManagerConstants.STRING_PARCEL) == true) {
			parcel = new UMSessionManagerParcel( entries[i] );			
	        _alParcels.add( parcel );

	        parcel.buildTreeFromLog( entries[i] );
		}
	}
}
/**
 *
 * @return org.eclipse.update.internal.core.UMTaskManagerParcel
 * @param actionType java.lang.String
 */
public UMSessionManagerParcel createParcel() {

	// Create a new log entry
	//-----------------------
	ILogEntry logEntryChild = new LogEntry( _logEntry, UpdateManagerConstants.STRING_PARCEL );
	_logEntry.addChildEntry( logEntryChild );

	// Create the copy object
	//-----------------------
	UMSessionManagerParcel parcel = new UMSessionManagerParcel( logEntryChild );
	_alParcels.add( parcel );
	
	return parcel;
}
/**
 * Execute any pending or failed updates.
 */
public boolean execute(IProgressMonitor progressMonitor) {

	// Attempt all even if failure occurs
	//-----------------------------------	
	boolean bSuccess = true;

	// Parcels may be sequential, do in forward order
	//-----------------------------------------------
	for (int i = 0; i < _alParcels.size(); ++i) {

		if (((UMSessionManagerParcel) _alParcels.get(i)).execute(progressMonitor) == false) {
			bSuccess = false;
		}
	}

	// Status
	//-------
	setStatus(bSuccess == true ? UpdateManagerConstants.STATUS_SUCCEEDED : UpdateManagerConstants.STATUS_FAILED);

	incrementAttemptCount();

	return bSuccess;
}
/**
 * Execute any pending or failed updates.
 */
public boolean executeUndo(org.eclipse.core.runtime.IProgressMonitor progressMonitor) {

	// Undo only failed attempts
	//--------------------------
	if (getStatus().equals(UpdateManagerConstants.STATUS_FAILED) == false) {
		return true;
	}
	
	// Attempt all even if failure occurs
	//-----------------------------------	
	boolean bSuccess = true;

	// Parcels may be sequential, undo in reverse order
	//-------------------------------------------------
	for (int i = _alParcels.size() - 1; i >= 0; --i) {

		if (((UMSessionManagerParcel) _alParcels.get(i)).executeUndo(progressMonitor) == false) {
			bSuccess = false;
		}
	}

	// Status
	//-------
	setStatus(bSuccess == true ? UpdateManagerConstants.STATUS_FAILED_UNDO_SUCCEEDED : UpdateManagerConstants.STATUS_FAILED_UNDO_FAILED);

	resetAttemptCount();
	
	return bSuccess;
}
/**
 * Returns all parcels
 * @return org.eclipse.update.internal.core.UMSessionManagerParcel[]
 */
public UMSessionManagerParcel[] getParcels() {

	UMSessionManagerParcel[] parcels = new UMSessionManagerParcel[_alParcels.size()];
	return (UMSessionManagerParcel[])_alParcels.toArray( parcels );
}
/**
 * @return java.lang.String
 */
public String getStatusString() {
	
	StringBuffer strb = new StringBuffer();

	getStatusString( strb, 0 );
	
	return strb.toString();
}
/**
 * @param strb java.lang.StringBuffer
 * @param iIndentation int
 */
public void getStatusString(java.lang.StringBuffer strb, int iIndentation) {

	String strStatus = getStatus();

	if (strStatus.equals(UpdateManagerConstants.STATUS_SUCCEEDED) == true) {
		strb.append(UpdateManagerStrings.getString("S_Installations_have_completed_successfully"));
		strb.append("\n");
		strb.append(UpdateManagerStrings.getString("S_You_must_restart_the_workbench_to_activate_any_changes"));
	}

	else {
		strb.append(UpdateManagerStrings.getString("S_An_installation_error_or_cancellation_has_occurred"));

		// Parcels may be sequential, do in forward order
		//-----------------------------------------------
		for (int i = 0; i < _alParcels.size(); ++i) {

			((UMSessionManagerParcel) _alParcels.get(i)).getStatusString(strb, 2);
		}
	}
}
/**
 * @return java.lang.String
 */
public String getTimestamp() {
	
	return _propertyTimestamp.getValue();
}
/**
 * Returns whether this session is complete
 * @return boolean
 */
public boolean isComplete() {

	return _propertyStatus.getValue().equals(UpdateManagerConstants.STATUS_SUCCEEDED) ||
		   _propertyStatus.getValue().equals(UpdateManagerConstants.STATUS_FAILED_UNDO_SUCCEEDED);
}
}
