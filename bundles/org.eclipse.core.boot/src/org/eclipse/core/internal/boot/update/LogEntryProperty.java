package org.eclipse.core.internal.boot.update;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

public class LogEntryProperty implements ILogEntryProperty {

	protected static final String _strEmpty = new String();
	
	protected String _strName;
	protected String _strValue;
	protected String _strDateCreated = null;
	protected String _strDateModified = null;
	protected ILogEntry _entryParent = null;
/**
 * 
 */
public LogEntryProperty( ILogEntry entryParent, String name, String value) {

	_entryParent = entryParent;
	_strName     = name;
	_strValue    = value;	
}
/**
 * 
 */
public String getName() {
	return _strName == null ? _strEmpty : _strName;
}
/**
 */
public ILogEntry getParentEntry() {
	return _entryParent;
}
/**
 * 
 */
public String getValue() {
	return _strValue == null ? _strEmpty : _strValue;
}
/**
 */
public void printProperty( int iIndentation )
{
//	Trace.functionEntry( this, "printAttribute" );
	
	StringBuffer strb = new StringBuffer();

	for( int i=0; i<iIndentation; ++i )
	{
		strb.append( ' ' );
	}

	// Print line / column numbers as 1 based
	//---------------------------------------
	if( _strName != null )
	{
		strb.append( _strName );
	}
	else
	{
		strb.append( "null" );
	}

	if( _strValue != null )
	{
		strb.append( ", <" + _strValue + ">" );
	}
	else
	{
		strb.append( ", null" );
	}

	System.out.println( strb.toString() );
	
//	Trace.functionExit( this, "printAttribute" );
}
/**
 */
public void printPropertyString(StringBuffer strb) {

	if (_strName != null) {
		strb.append(_strName);
	}
	else {
		strb.append("undefined");
	}

	strb.append("=\"");

	if (_strValue != null) {
		strb.append(_strValue);
	}

	strb.append("\"");
}
/**
 */
public void setName(String name) {
	_strName = name;
}
/**
 */
public void setValue(String value) {
	_strValue = value;
}
}
