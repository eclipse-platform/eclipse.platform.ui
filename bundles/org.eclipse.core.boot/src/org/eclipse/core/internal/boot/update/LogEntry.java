package org.eclipse.core.internal.boot.update;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2001
 */
import java.util.*;
/**
 * This class contains its own properties, as well as child entries.
 */

public class LogEntry implements ILogEntry{
	
	protected static final String _strEmpty = new String();
		
	protected String    _strDateCreated   = null;
	protected String    _strName          = null;
	protected Vector    _vectorChildEntries   = null;
	protected Vector    _vectorProperties = null;
	protected ILogEntry _entryParent      = null;
/**
 * Constructs a new entry.
 * @param entryParent org.eclipse.update.internal.core#LogEntry
 * @param name java.lang.String
 */
public LogEntry(ILogEntry entryParent, String name) {
	
	_entryParent = entryParent;
	_strName     = name;
}
/**
 * Adds a sub-entry to this entry
 * @param entryChild org.eclipse.update.internal.core#LogEntry
 */
public void addChildEntry(ILogEntry entryChild) {
	
	if (_vectorChildEntries == null) {
		_vectorChildEntries = new Vector();
	}

	_vectorChildEntries.add(entryChild);
}
/**
 * Adds a property to this entry
 * @param attribute org.eclipse.update.internal.core#LogEntryAttribute
 */
public void addProperty(ILogEntryProperty property) {

	if (_vectorProperties == null) {
		_vectorProperties = new Vector();
	}

	_vectorProperties.add(property);
}
/**
 * Returns all child entries
 * @return org.eclipse.update.internal.core#LogEntry[]
 */
public ILogEntry[] getChildEntries() {

	if (_vectorChildEntries == null) {
		return new ILogEntry[0];
	}

	ILogEntry[] entries = new ILogEntry[_vectorChildEntries.size()];

	for (int i = 0; i < _vectorChildEntries.size(); ++i) {
		entries[i] = (ILogEntry) _vectorChildEntries.elementAt(i);
	}

	return entries;
}
/**
 * Returns an array of all child elements with the specified name
 *
 * @param elementName java.lang.String
 */
public ILogEntry[] getChildEntries(String name) {

	if (_vectorChildEntries == null) {
		return new ILogEntry[0];
	}

	// Count the number of entries with the name
	//------------------------------------------
	int iCount = 0;
	
	for (int i = 0; i < _vectorChildEntries.size(); ++i) {
		if (((ILogEntry) _vectorChildEntries.elementAt(i)).getName().equals(name) == true) {
			iCount++;
		}
	}
 
	// Create and fill an array with the entries
	//------------------------------------------	
	ILogEntry[] entries = new ILogEntry[iCount];

   	int iIndex = 0;
	
	for (int i = 0; i < _vectorChildEntries.size(); ++i) {
		if (((ILogEntry) _vectorChildEntries.elementAt(i)).getName().equals(name) == true) {
			entries[iIndex++] = (ILogEntry) _vectorChildEntries.elementAt(i);
		}
	}

	return entries;
}
/**
 * Returns the first child entry with the specified name.
 * @return org.eclipse.update.internal.core#ILogEntry
 * @param name java.lang.String
 */
public ILogEntry getChildEntry(String name) {

	if (_vectorChildEntries != null) {
		for (int i = 0; i < _vectorChildEntries.size(); ++i) {
			if (((ILogEntry) _vectorChildEntries.elementAt(i)).getName().equals(name) == true) {
				return (ILogEntry) _vectorChildEntries.elementAt(i);
			}
		}
	}
	
	return null;
}
/**
 * Returns the name of the entry.
 * @return java.lang.String
 */
public String getName() {
	return _strName == null ? _strEmpty : _strName;
}
/**
 * Returns the number of child entries.
 * @return int
 */
public int getNumberOfChildEntries() {
	return _vectorChildEntries == null ? 0 : _vectorChildEntries.size();
}
/**
 * Returns the number of properties that this entry has.
 * @return int
 */
public int getNumberOfProperties() {
	return _vectorProperties == null ? 0 : _vectorProperties.size();
}
/**
 * Returns the parent entry.
 * @return org.eclipse.update.internal.core#ILogEntry
 */
public ILogEntry getParentEntry() {
	return _entryParent;
}
/**
 * Returns an array of properties that this entry has.
 * @return org.eclipse.update.internal.core#LogEntryAttribute[]
 */
public ILogEntryProperty[] getProperties() {

	if (_vectorProperties == null) {
		return new ILogEntryProperty[0];
	}

	ILogEntryProperty[] properties = new ILogEntryProperty[_vectorProperties.size()];

	for (int i = 0; i < _vectorProperties.size(); ++i) {
		properties[i] = (ILogEntryProperty) _vectorProperties.elementAt(i);
	}

	return properties;
}
/**
 * Returns the first property with this name belonging to this entry.
 * @param attributeName java.lang.String
 */
public ILogEntryProperty getProperty(String name) {

	if (_vectorProperties == null) {
		return null;
	}

	for (int i = 0; i < _vectorProperties.size(); ++i) {
		if (((ILogEntryProperty) _vectorProperties.elementAt(i)).getName().equals(name) == true) {
			return (ILogEntryProperty) _vectorProperties.elementAt(i);
		}
	}

	return null;
}
/**
 * Debug function that prints the entire element tree, and each element's
 * attributes starting at this element.
 */
public void printEntryTree( int iIndentation )
{
//	Trace.functionEntry( this, "printElementTree" );
	
	// Print the name of this element
	//-------------------------------
	StringBuffer strb = new StringBuffer();

	for( int i=0; i<iIndentation; ++i )
	{
		strb.append( ' ' );
	}
	
	if( _strName != null )
	{
		strb.append( "<" + _strName + ">" );
	}
	else
	{
		strb.append( "<>" );
	}

	System.out.println( strb.toString() );

	// Print out my attributes
	//------------------------
	if( _vectorProperties != null )
	{		
		for( int i=0; i<_vectorProperties.size(); ++i )
		{
			((LogEntryProperty)_vectorProperties.elementAt( i )).printProperty( iIndentation + 4 );
		}
	}

	// Get my children to print themselves
	//------------------------------------
	if( _vectorChildEntries != null )
	{
		for( int i=0; i<_vectorChildEntries.size(); ++i )
		{
			((LogEntry)_vectorChildEntries.elementAt( i )).printEntryTree( iIndentation + 4 );
		}
	}

//	Trace.functionExit( this, "printElementTree" );
}
/**
 * Prints out this entry and its attributes.
 */
public void printPersistentEntryString(StringBuffer strb, int iIndentation) {

	// Indentation
	//------------
	printPersistentEntryStringIndentation(strb, iIndentation);

	// Begin element
	//--------------
	strb.append('<');
	printPersistentEntryStringName(strb);

	// Print out my properties and their values
	//-----------------------------------------
	LogEntryProperty attribute = null;

	if (_vectorProperties != null && _vectorProperties.size() > 0) {

		for (int i = 0; i < _vectorProperties.size(); ++i) {
			attribute = (LogEntryProperty) _vectorProperties.elementAt(i);
			strb.append(' ');
			attribute.printPropertyString(strb);
		}
	}

	// Print the rest of the element string
	// with the end element on a separate line
	//----------------------------------------
	if (_vectorChildEntries != null && _vectorChildEntries.size() > 0 ) {

		// Terminate the begin element
		//----------------------------	
		strb.append(">\r\n");

		// Get my children to print themselves
		//------------------------------------
		if (_vectorChildEntries != null) {
			for (int i = 0; i < _vectorChildEntries.size(); ++i) {
				((LogEntry) _vectorChildEntries.elementAt(i)).printPersistentEntryString(strb, iIndentation + 4);
			}
		}

		// End element
		//------------
		printPersistentEntryStringIndentation(strb, iIndentation);
		strb.append("</");
		printPersistentEntryStringName(strb);
		strb.append(">\r\n");
	}

	// End element on a single line
	//-----------------------------
	else {
		strb.append(" />\r\n");
	}
}
/**
 */
protected void printPersistentEntryStringIndentation(StringBuffer strb, int iIndentation) {
	for (int i = 0; i < iIndentation; ++i) {
		strb.append(' ');
	}
}
/**
 * 
 */
public void printPersistentEntryStringName(StringBuffer strb ) {

	strb.append( _strName != null ? _strName : "undefined" );
}
/**
 * 
 */
public void removeAllChildEntries() {
	if (_vectorChildEntries != null) {
		_vectorChildEntries.removeAllElements();
	}
}
/**
 * 
 */
public void removeAllProperties() {
	if (_vectorProperties != null) {
		_vectorProperties.removeAllElements();
	}
}
/**
 */
public boolean removeChildEntry(ILogEntry entry) {

	if (_vectorChildEntries != null) {
		return _vectorChildEntries.removeElement(entry);
	}
	
	return false;
}
/**
 * 
 */
public boolean removeProperty(String name) {

	if (_vectorProperties != null) {
		return _vectorProperties.removeElement( name );
	}
	
	return false;
}
}
