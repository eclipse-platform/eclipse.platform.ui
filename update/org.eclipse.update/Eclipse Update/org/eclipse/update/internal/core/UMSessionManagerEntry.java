package org.eclipse.update.internal.core;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.internal.boot.update.*;
/**
 * Abstract class for update manager objects that have an ILogEntry
 */
public abstract class UMSessionManagerEntry {
	protected ILogEntry _logEntry = null;
	protected ILogEntryProperty _propertyStatus = null;
	protected ILogEntryProperty _propertyAttempts = null;
	protected Object            _objData = null;
/**
 * UpdateManagerEntry constructor comment.
 */
public UMSessionManagerEntry( ILogEntry logEntry ) {
	_logEntry = logEntry;

	// Status
	//-------
	_propertyStatus = _logEntry.getProperty(UpdateManagerConstants.STRING_STATUS);

	if (_propertyStatus == null) {
		_propertyStatus = new LogEntryProperty(logEntry, UpdateManagerConstants.STRING_STATUS, UpdateManagerConstants.STATUS_PENDING );
		logEntry.addProperty( _propertyStatus );
	}

	// Attempts
	//---------
	_propertyAttempts = _logEntry.getProperty(UpdateManagerConstants.STRING_ATTEMPTS);

	if (_propertyAttempts == null) {
		_propertyAttempts = new LogEntryProperty(logEntry, UpdateManagerConstants.STRING_ATTEMPTS, UpdateManagerConstants.STRING_0 );
		logEntry.addProperty( _propertyAttempts );
	}

}
/**
 * @param logEntry org.eclipse.update.internal.core.ILogEntry
 */
public void buildTreeFromLog(ILogEntry logEntry) {
	
	// Attempts
	//---------
	ILogEntryProperty property = _logEntry.getProperty(UpdateManagerConstants.STRING_ATTEMPTS);

	if (property != null) {
		_propertyAttempts = property;
	}
	
	// Status
	//-------
	property = _logEntry.getProperty(UpdateManagerConstants.STRING_STATUS);

	if (property != null) {
		_propertyStatus = property;
	}
}
/**
 * 
 * @return java.lang.String
 * @param strPrefix java.lang.String
 * @param ex java.lang.Exception
 */
public String createMessageString(String strPrefix, Exception ex) {

	StringBuffer strb = new StringBuffer(strPrefix);

	if (ex != null) {
		if (ex.getMessage() != null) {
			strb.append(": " + ex.getMessage());
		}

		else if (ex.toString() != null && ex.toString().length() > 0) {
			strb.append(": " + ex.toString());
		}
	}

	return strb.toString();
}
/**
 * Execute any pending or failed updates.
 */
public abstract boolean execute( IProgressMonitor progressMonitor );
/**
 * Execute any pending or failed updates.
 */
public abstract boolean executeUndo( IProgressMonitor progressMonitor );
/**
 * Insert the method's description here.
 * Creation date: (2001/03/14 08:11:12)
 * @return java.lang.Object
 */
public java.lang.Object getData() {
	return _objData;
}
/**
 * Returns the log entry associated with this session object.
 * @return org.eclipse.update.internal.core.ILogEntry
 */
public ILogEntry getLogEntry() {
	return _logEntry;
}
/**
 *
 * @return java.lang.String
 */
public String getStatus() {

	return _propertyStatus.getValue();
}
/**
 * @param strb java.lang.StringBuffer
 * @param iIndentation int
 */
public abstract void getStatusString(StringBuffer strb, int iIndentation);
/**
 * Increments the value of the number of attempts
 */
public void incrementAttemptCount() {

	int iAttempts = 0;

	try {
		iAttempts = new Integer(_propertyAttempts.getValue()).intValue();
	}
	catch (Exception ex) {
	}

	_propertyAttempts.setValue( Integer.toString( ++iAttempts ) );
}
/**
 * @param strb java.lang.StringBuffer
 * @param iIndentation int
 */
public void indent(StringBuffer strb, int iIndentation) {

	// Line feed
	//----------
	strb.append( "\n" );

	// Indentation spaces
	//-------------------
	for( int i=0; i<iIndentation; ++i ){
		strb.append( " " );
	}
}
/**
 */
public void resetAttemptCount() {
	_propertyAttempts.setValue( UpdateManagerConstants.STRING_0 );
}
/**
 * Stores IManifestDescriptor
 * @param newData java.lang.Object
 */
public void setData(java.lang.Object objData) {
	_objData = objData;
}
/**
 * Every entry has a status property.
 * @return java.lang.String
 * @param status java.lang.String
 */
public void setStatus(String strStatus) {

	_propertyStatus.setValue( strStatus != null ? strStatus : UpdateManagerConstants.STRING_EMPTY );
}
}
