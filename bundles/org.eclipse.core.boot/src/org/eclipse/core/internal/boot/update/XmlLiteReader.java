package org.eclipse.core.internal.boot.update;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2001
 */
import java.io.IOException;
import java.io.StringReader;
/**
 * This class reads the contents of an ascii file system file.
 */

public class XmlLiteReader {
	
	protected String _strText = null;
	protected int _iPosition = 0;

/**
 * Constructor.
 */
public XmlLiteReader(String strText) {

	_strText = strText;
}
/**
 * Reads a line of ascii text from the file.
 */
public String readLine() {

	// Read a line
	//------------
	String strLine = null;

	if (_iPosition < _strText.length()) {

		int iIndex = _strText.indexOf("\n", _iPosition );

		if (iIndex != -1) {
			strLine = _strText.substring(_iPosition, iIndex);
			_iPosition = iIndex + 1;
		}
		
		// End of string
		//--------------
		else
		{
			strLine = _strText.substring( _iPosition );
			_iPosition = _strText.length();
		}
	}
	
	return strLine;
}
}
