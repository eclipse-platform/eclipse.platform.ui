package org.eclipse.update.internal.core;

import org.eclipse.core.internal.boot.update.*;
import java.util.Date;

/**
 * This class represents a failure message and consists of a timestamp and text.
 *
 */

public class UMSessionManagerMessage {
	protected ILogEntry _logEntry = null;
	protected ILogEntryProperty _propertyTimestamp = null;
	protected ILogEntryProperty _propertyText      = null;
/**
 *
 * @param logEntry org.eclipse.update.internal.core.ILogEntry
 */
public UMSessionManagerMessage(ILogEntry logEntry) {

	_logEntry = logEntry;
	
	// Text
	//-----
	_propertyText = _logEntry.getProperty(UpdateManagerConstants.STRING_TEXT);

	if (_propertyText == null) {
		_propertyText = new LogEntryProperty(logEntry, UpdateManagerConstants.STRING_TEXT, UpdateManagerConstants.STRING_EMPTY );
		logEntry.addProperty( _propertyText );
	}

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
		
	// Text
	//-----
	ILogEntryProperty property = _logEntry.getProperty(UpdateManagerConstants.STRING_TEXT);

	if (property != null) {
		_propertyText = property;
	}

	// Source
	//-------
	property = _logEntry.getProperty(UpdateManagerConstants.STRING_TIMESTAMP);

	if (property != null) {
		_propertyTimestamp = property;
	}
}
/**
 * Insert the method's description here.
 * Creation date: (2001/02/15 14:21:48)
 * @return java.lang.String
 */
public String getText() {
	
	return _propertyText.getValue();
}
/**
 *
 */
public void setText( String strID ) {
	_propertyText.setValue( strID != null ? strID : UpdateManagerConstants.STRING_EMPTY );
}
}
