package org.eclipse.core.internal.boot.update;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2001
 */
public class XmlLiteAttribute implements ILogEntryProperty {

	protected static final String _strEmpty = new String();
	
	protected String _strName;
	protected String _strValue;
	protected String _strDateCreated = null;
	protected String _strDateModified = null;
	protected XmlLiteElement _entryParent = null;
/**
 * 
 * @param name java.lang.String
 * @param value java.lang.String
 */
public XmlLiteAttribute( XmlLiteElement entryParent, String name, String value) {

	_entryParent = entryParent;
	_strName     = name;
	_strValue    = value;	
}
/**
 * 
 * @return java.lang.String
 */
public String getName() {
	return _strName == null ? _strEmpty : _strName;
}
/**
 * @return org.eclipse.update.internal.core#ILogEntry
 */
public XmlLiteElement getParentElement() {
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
public void printAttribute( int iIndentation )
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
 * 
 * @param strb java.lang.StringBuffer
 */
public void printAttributeString(StringBuffer strb) {

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
 * 
 * @param strName java.lang.String
 */
public void setName(String name) {
	_strName = name;
}
/**
 * 
 * @param strValue java.lang.String
 */
public void setValue(String value) {
	_strValue = value;
}
}
