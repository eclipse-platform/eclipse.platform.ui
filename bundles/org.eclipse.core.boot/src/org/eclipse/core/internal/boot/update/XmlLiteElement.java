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

public class XmlLiteElement {
	
	protected static final String _strEmpty = new String();
		
	protected String    _strDateCreated   = null;
	protected String    _strName          = null;
	protected Vector    _vectorChildEntries   = null;
	protected Vector    _vectorProperties = null;
	protected XmlLiteElement _entryParent      = null;
/**
 * Constructs a new entry.
 * @param entryParent org.eclipse.update.internal.core#LogEntry
 * @param name java.lang.String
 */
public XmlLiteElement(XmlLiteElement entryParent, String name) {
	
	_entryParent = entryParent;
	_strName     = name;
}
/**
 * Adds a property to this entry
 * @param attribute org.eclipse.update.internal.core#LogEntryAttribute
 */
public void addAttribute(XmlLiteAttribute property) {

	if (_vectorProperties == null) {
		_vectorProperties = new Vector();
	}

	_vectorProperties.add(property);
}
/**
 * Adds a sub-entry to this entry
 * @param entryChild org.eclipse.update.internal.core#LogEntry
 */
public void addChildElement(XmlLiteElement entryChild) {
	
	if (_vectorChildEntries == null) {
		_vectorChildEntries = new Vector();
	}

	_vectorChildEntries.add(entryChild);
}
/**
 * Returns the first property with this name belonging to this entry.
 * @param attributeName java.lang.String
 */
public XmlLiteAttribute getAttribute(String name) {

	if (_vectorProperties == null) {
		return null;
	}

	for (int i = 0; i < _vectorProperties.size(); ++i) {
		if (((XmlLiteAttribute) _vectorProperties.elementAt(i)).getName().equals(name) == true) {
			return (XmlLiteAttribute) _vectorProperties.elementAt(i);
		}
	}

	return null;
}
/**
 * Returns an array of properties that this entry has.
 * @return org.eclipse.update.internal.core#LogEntryAttribute[]
 */
public XmlLiteAttribute[] getAttributes() {

	if (_vectorProperties == null) {
		return new XmlLiteAttribute[0];
	}

	XmlLiteAttribute[] properties = new XmlLiteAttribute[_vectorProperties.size()];

	for (int i = 0; i < _vectorProperties.size(); ++i) {
		properties[i] = (XmlLiteAttribute) _vectorProperties.elementAt(i);
	}

	return properties;
}
/**
 * Returns the first child entry with the specified name.
 * @return org.eclipse.update.internal.core#XmlLiteElement
 * @param name java.lang.String
 */
public XmlLiteElement getChildElement(String name) {

	if (_vectorChildEntries != null) {
		for (int i = 0; i < _vectorChildEntries.size(); ++i) {
			if (((XmlLiteElement) _vectorChildEntries.elementAt(i)).getName().equals(name) == true) {
				return (XmlLiteElement) _vectorChildEntries.elementAt(i);
			}
		}
	}
	
	return null;
}
/**
 * Returns all child entries
 * @return org.eclipse.update.internal.core#LogEntry[]
 */
public XmlLiteElement[] getChildElements() {

	if (_vectorChildEntries == null) {
		return new XmlLiteElement[0];
	}

	XmlLiteElement[] entries = new XmlLiteElement[_vectorChildEntries.size()];

	for (int i = 0; i < _vectorChildEntries.size(); ++i) {
		entries[i] = (XmlLiteElement) _vectorChildEntries.elementAt(i);
	}

	return entries;
}
/**
 * Returns an array of all child elements with the specified name
 *
 * @param elementName java.lang.String
 */
public XmlLiteElement[] getChildElements(String name) {

	if (_vectorChildEntries == null) {
		return new XmlLiteElement[0];
	}

	// Count the number of entries with the name
	//------------------------------------------
	int iCount = 0;
	
	for (int i = 0; i < _vectorChildEntries.size(); ++i) {
		if (((XmlLiteElement) _vectorChildEntries.elementAt(i)).getName().equals(name) == true) {
			iCount++;
		}
	}
 
	// Create and fill an array with the entries
	//------------------------------------------	
	XmlLiteElement[] entries = new XmlLiteElement[iCount];

   	int iIndex = 0;
	
	for (int i = 0; i < _vectorChildEntries.size(); ++i) {
		if (((XmlLiteElement) _vectorChildEntries.elementAt(i)).getName().equals(name) == true) {
			entries[iIndex++] = (XmlLiteElement) _vectorChildEntries.elementAt(i);
		}
	}

	return entries;
}
/**
 * Returns the name of the entry.
 * @return java.lang.String
 */
public String getName() {
	return _strName == null ? _strEmpty : _strName;
}
/**
 * Returns the number of properties that this entry has.
 * @return int
 */
public int getNumberOfAttributes() {
	return _vectorProperties == null ? 0 : _vectorProperties.size();
}
/**
 * Returns the number of child entries.
 * @return int
 */
public int getNumberOfChildElements() {
	return _vectorChildEntries == null ? 0 : _vectorChildEntries.size();
}
/**
 * Returns the parent entry.
 * @return org.eclipse.update.internal.core#XmlLiteElement
 */
public XmlLiteElement getParentElement() {
	return _entryParent;
}
/**
 * Debug function that prints the entire element tree, and each element's
 * attributes starting at this element.
 */
public void printElementTree( int iIndentation )
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
			((XmlLiteAttribute)_vectorProperties.elementAt( i )).printAttribute( iIndentation + 4 );
		}
	}

	// Get my children to print themselves
	//------------------------------------
	if( _vectorChildEntries != null )
	{
		for( int i=0; i<_vectorChildEntries.size(); ++i )
		{
			((XmlLiteElement)_vectorChildEntries.elementAt( i )).printElementTree( iIndentation + 4 );
		}
	}

//	Trace.functionExit( this, "printElementTree" );
}
/**
 * Prints out this entry and its attributes.
 */
public void printPersistentElementString(StringBuffer strb, int iIndentation) {

	// Indentation
	//------------
	printPersistentElementStringIndentation(strb, iIndentation);

	// Begin element
	//--------------
	strb.append('<');
	printPersistentElementStringName(strb);

	// Print out my properties and their values
	//-----------------------------------------
	XmlLiteAttribute attribute = null;

	if (_vectorProperties != null && _vectorProperties.size() > 0) {

		for (int i = 0; i < _vectorProperties.size(); ++i) {
			attribute = (XmlLiteAttribute) _vectorProperties.elementAt(i);
			strb.append(' ');
			attribute.printAttributeString(strb);
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
				((XmlLiteElement) _vectorChildEntries.elementAt(i)).printPersistentElementString(strb, iIndentation + 4);
			}
		}

		// End element
		//------------
		printPersistentElementStringIndentation(strb, iIndentation);
		strb.append("</");
		printPersistentElementStringName(strb);
		strb.append(">\r\n");
	}

	// End element on a single line
	//-----------------------------
	else {
		strb.append(" />\r\n");
	}
}
/**
 * @param strb java.lang.StringBuffer
 * @param iIndentation int
 */
protected void printPersistentElementStringIndentation(StringBuffer strb, int iIndentation) {
	for (int i = 0; i < iIndentation; ++i) {
		strb.append(' ');
	}
}
/**
 * 
 * @param strb java.lang.StringBuffer
 */
public void printPersistentElementStringName(StringBuffer strb ) {

	strb.append( _strName != null ? _strName : "undefined" );
}
/**
 * 
 */
public void removeAllAttributes() {
	if (_vectorProperties != null) {
		_vectorProperties.removeAllElements();
	}
}
/**
 * 
 */
public void removeAllChildElements() {
	if (_vectorChildEntries != null) {
		_vectorChildEntries.removeAllElements();
	}
}
/**
 * 
 * @return boolean
 * @param name java.lang.String
 */
public boolean removeAttribute(String name) {

	if (_vectorProperties != null) {
		return _vectorProperties.removeElement( name );
	}
	
	return false;
}
/**
 * @return boolean
 * @param entry org.eclipse.update.internal.core#ILogEntry
 */
public boolean removeChildElements(ILogEntry entry) {

	if (_vectorChildEntries != null) {
		return _vectorChildEntries.removeElement(entry);
	}
	
	return false;
}
}
