package org.eclipse.core.internal.boot.update;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2001
 */
/**
 * This class reads and writes logs to/from URL input/output streams.
 * If obtaining an output stream fails, an attempt is made to write
 * to the output URL as a file.  A save operation always attempts to
 * write out a well formed document, even if the load operation was
 * incomplete.
 * 
 * See main() as an example of how to use this class.
 *
 * This class is the root log entry.  It has the name "root".  This was
 * done in case multiple first level entries are required.
 * Valid log entries are children of this one.  Obtain the children
 * by calling getChildEntries().  Child entries may have identical names.
 */
// Sample log file:
//
// <logEntry1 name="abc" type="def" >
//     <subelement name="ghi" anotherproperty="propertyvalue" />
//     <subelement name="jkl" />
//     <anothersubelement />
// </logEntry1>
//
//
//
import java.net.URL;

public class XmlLite extends XmlLiteElement {

/**
 * Log constructor comment.
 */
public XmlLite() {
	super( null, "root" );
}
/**
 * @return java.lang.String
 */
public String getPersistentString() {

	// Do not print the root entry
	//----------------------------
	StringBuffer strb = new StringBuffer();

	if (_vectorChildEntries != null) {

		for (int i = 0; i < _vectorChildEntries.size(); ++i) {
			((XmlLiteElement) _vectorChildEntries.elementAt(i)).printPersistentElementString(strb, 0);
		}
	}
	
	return strb.toString();
}
/**
 * 
 * @return boolean
 */
public boolean load( URL url ) throws XmlLiteException {

	boolean bLoaded = false;
	
	// Attempt to open the URL
	//------------------------
	if( url != null ) {
		XmlLiteStore store = new XmlLiteStore();
		bLoaded = store.load( this, url );
	}

	return bLoaded;
}
/**
 * 
 * @return boolean
 */
public void save(URL url) throws XmlLiteException{

	boolean bSaved = true;

	if (url != null) {
		XmlLiteStore store = new XmlLiteStore();

		store.save(this, url);
	}

	return;
}
}
