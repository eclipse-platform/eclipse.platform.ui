package org.eclipse.update.internal.core;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.internal.boot.update.*;
import java.util.*;

/**
 * This class represents either a component or a product.  A parcel can
 * have sub parcels, and copy objects.  Attributes are action (add, fix), id, status (pending, complete),
 * type (component / product).
 */

public class UMSessionManagerParcel extends UMSessionManagerEntry {
	
	protected ArrayList _alParcels = new ArrayList();
	protected ArrayList _alOperations   = new ArrayList();
	
	protected ILogEntryProperty _propertyAction = null;
	protected ILogEntryProperty _propertyID     = null;
	protected ILogEntryProperty _propertyType   = null;
/**
 * UpdateManagerParcel constructor comment.
 */
public UMSessionManagerParcel( ILogEntry logEntry ) {
	super( logEntry );

	// Action
	//-------
	_propertyAction = _logEntry.getProperty(UpdateManagerConstants.STRING_ACTION);

	if (_propertyAction == null) {
		_propertyAction = new LogEntryProperty(logEntry, UpdateManagerConstants.STRING_ACTION, UpdateManagerConstants.STRING_EMPTY );
		logEntry.addProperty( _propertyAction );
	}

	// Id
	//---
	_propertyID = _logEntry.getProperty(UpdateManagerConstants.STRING_ID);

	if (_propertyID == null) {
		_propertyID = new LogEntryProperty(logEntry, UpdateManagerConstants.STRING_ID, UpdateManagerConstants.STRING_EMPTY );
		logEntry.addProperty( _propertyID );
	}

	// Type
	//-----
	_propertyType = _logEntry.getProperty(UpdateManagerConstants.STRING_TYPE);

	if (_propertyType == null) {
		_propertyType = new LogEntryProperty(logEntry, UpdateManagerConstants.STRING_TYPE, UpdateManagerConstants.STRING_EMPTY );
		logEntry.addProperty( _propertyType );
	}
}
/**
 * @param logEntry org.eclipse.update.internal.core.LogEntry
 */
public void buildTreeFromLog(ILogEntry logEntry) {

	super.buildTreeFromLog( logEntry );
	
	// Action
	//-------
	ILogEntryProperty property = _logEntry.getProperty(UpdateManagerConstants.STRING_ACTION);

	if (property != null) {
		_propertyAction = property;
	}

	// Id
	//---
	property = _logEntry.getProperty(UpdateManagerConstants.STRING_ID);

	if (property != null) {
		_propertyID = property;
	}

	// Type  
	//-----
	property = _logEntry.getProperty(UpdateManagerConstants.STRING_TYPE);

	if (property != null) {
		_propertyType = property;
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

	// Operations
	//-----------
	iIndex = 0;
	UMSessionManagerOperation operation = null;
	
	for( int i = 0; i < entries.length; ++i) {
		if (entries[i] != null && entries[i].getName().equals(UpdateManagerConstants.STRING_OPERATION) == true) {
			operation = new UMSessionManagerOperation( entries[i] );			
	        _alOperations.add( operation );

	        operation.buildTreeFromLog( entries[i] );
		}
	}
}
/**
 * Insert the method's description here.
 * Creation date: (2001/02/15 13:58:59)
 * @return org.eclipse.update.internal.core.UMSessionManagerOperation
 * @param actionType java.lang.String
 */
public UMSessionManagerOperation createOperation() {

	// Create a new log entry
	//-----------------------
	ILogEntry logEntryChild = new LogEntry( _logEntry, UpdateManagerConstants.STRING_OPERATION );
	_logEntry.addChildEntry( logEntryChild );

	// Create the operation object
	//----------------------------
	UMSessionManagerOperation operation = new UMSessionManagerOperation( logEntryChild );
	_alOperations.add( operation );
	
	return operation;
}
/**
 * Creates a sub-parcel
 * @return org.eclipse.update.internal.core.UpdateManagerParcel
 * @param actionType java.lang.String
 */
public UMSessionManagerParcel createParcel() {

	// Create a new log entry
	//-----------------------
	ILogEntry logEntryChild = new LogEntry( _logEntry, UpdateManagerConstants.STRING_PARCEL );
	_logEntry.addChildEntry( logEntryChild );

	// Create the parcel object
	//-------------------------
	UMSessionManagerParcel parcel = new UMSessionManagerParcel( logEntryChild );
	_alParcels.add( parcel );
	
	return parcel;
}
/**
 * Execute any pending or failed updates.
 */
public boolean execute(IProgressMonitor progressMonitor) {

	// Stop if any failure occurs
	//---------------------------
	boolean bSuccess = true;

	// Parcels may be sequential, do in forward order
	//-----------------------------------------------
	for (int i = 0; i < _alParcels.size(); ++i) {

		if (((UMSessionManagerParcel) _alParcels.get(i)).execute(progressMonitor) == false) {
			bSuccess = false;
			break;
		}
	}

	// Operations may be sequential, do in forward order
	//--------------------------------------------------
	if (bSuccess == true) {
		for (int i = 0; i < _alOperations.size(); ++i) {

			if (((UMSessionManagerOperation) _alOperations.get(i)).execute(progressMonitor) == false) {
				bSuccess = false;
				break;
			}
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

	// Operations may be sequential, undo in reverse order
	//----------------------------------------------------
	for (int i = _alOperations.size() - 1; i >= 0; --i) {

		if (((UMSessionManagerOperation) _alOperations.get(i)).executeUndo(progressMonitor) == false) {
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
 * Insert the method's description here.
 * Creation date: (2001/02/15 14:21:48)
 * @return java.lang.String
 */
public String getAction() {
	
	return _propertyAction.getValue();
}
/**
 * Insert the method's description here.
 * Creation date: (2001/02/15 14:21:48)
 * @return java.lang.String
 */
public String getId() {
	
	return _propertyID.getValue();
}
/**
 * Returns all operations
 * @return org.eclipse.update.internal.core.UMSessionManagerOperation[]
 */
public UMSessionManagerOperation[] getOperations() {

	UMSessionManagerOperation[] operations = new UMSessionManagerOperation[_alOperations.size()];
	return (UMSessionManagerOperation[])_alOperations.toArray( operations );
}
/**
 * Returns all sub-parcels
 * @return org.eclipse.update.internal.core.UpdateManagerParcel[]
 */
public UMSessionManagerParcel[] getParcels() {

	UMSessionManagerParcel[] parcels = new UMSessionManagerParcel[_alParcels.size()];
	return (UMSessionManagerParcel[])_alParcels.toArray( parcels );
}
/**
 * @param strb java.lang.StringBuffer
 * @param iIndentation int
 */
public void getStatusString(java.lang.StringBuffer strb, int iIndentation) {

	if (getStatus().equals(UpdateManagerConstants.STATUS_SUCCEEDED) == true) {
		return;
	}

	else {
		// Parcel label
		//-------------
		IInstallable descriptor = (IInstallable) getData();

		indent(strb, iIndentation);
		strb.append( descriptor.getLabel());


		// Parcels may be sequential, do in forward order
		//-----------------------------------------------
		for (int i = 0; i < _alParcels.size(); ++i) {

			((UMSessionManagerParcel) _alParcels.get(i)).getStatusString(strb, iIndentation + 2);
		}

		// Operations may be sequential, do in forward order
		//--------------------------------------------------
		for (int i = 0; i < _alOperations.size(); ++i) {

			((UMSessionManagerOperation) _alOperations.get(i)).getStatusString(strb, iIndentation + 2);
		}
	}
}
/**
 * Insert the method's description here.
 * Creation date: (2001/02/15 14:21:48)
 * @return java.lang.String
 */
public String getType() {
	
	return _propertyType.getValue();
}
/**
 *
 */
public void setAction( String strAction ) {
	_propertyAction.setValue( strAction != null ? strAction : UpdateManagerConstants.STRING_EMPTY );
}
/**
 *
 */
public void setId( String strID ) {
	_propertyID.setValue( strID != null ? strID : UpdateManagerConstants.STRING_EMPTY );
}
/**
 *
 */
public void setType( String strType ) {
	_propertyType.setValue( strType != null ? strType : UpdateManagerConstants.STRING_EMPTY );
}
}
