package org.eclipse.core.internal.boot.update;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.File;import java.io.FileWriter;import java.io.IOException;import java.io.InputStream;import java.io.InputStreamReader;import java.io.LineNumberReader;import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;import java.net.URL;import java.net.URLConnection;import org.eclipse.core.internal.boot.Policy;
/**
 * This class manages the loading, storing, parsing and creation of
 * the element tree.
 */

public class XmlLiteStore {
	
	private final static int MODE_UNKNOWN           =  0;
	private final static int MODE_ATTRIBUTESTART    =  1;
	private final static int MODE_COMMENT           =  2;
	private final static int MODE_COMMENTSTART      =  3;
	private final static int MODE_DOCTYPESTART      =  4;
	private final static int MODE_ELEMENT           =  5;
	private final static int MODE_ELEMENTSTART      =  6;
	private final static int MODE_ELEMENTENDSTART   =  7;
	private final static int MODE_TEXT              =  8;
	private final static int MODE_TEXTSTART         =  9;
	private final static int MODE_XMLSTART          = 10;

	protected URL                          _url                  = null;
	protected int                          _iMode                = MODE_UNKNOWN;
	protected XmlLiteReader                _reader               = null;
	protected String                       _strLine              = null;
	protected XmlLiteElement               _parserElementCurrent = null;
	protected XmlLiteElement               _parserElementRoot    = null;
	protected XmlLiteAttribute             _parserTextAttributeCurrent = null;
	protected int                          _iLine                = -1; // 0 based
	protected int                          _iColumn              = -1; // 0 based
	protected InputStream 				   _inputStream			 = null;
	protected LineNumberReader 			   _lineReader			 = null;

/**
 * LogModel constructor comment.
 */
public XmlLiteStore() {
}
/**
 * @return java.io.File
 * @param url java.net.URL
 */
public File createFile(URL url) throws XmlLiteException {

	String strFilespec = url.getFile().replace('/',File.separatorChar);
	int k = strFilespec.indexOf(":");
	if (k != -1 && strFilespec.startsWith(File.separator)) {
		strFilespec = strFilespec.substring(1);
	}


	File file = new File(strFilespec);
	boolean bExists = file.exists();

	if (bExists == false) {
		try {
			bExists = file.createNewFile();
			return file;
		}
		catch (IOException ex) {
			throw new XmlLiteException(Policy.bind("update.unableToOpen"), strFilespec, null, -1, -1);
		}
	}
	
	return file;
}
/**
 */
protected void handleAttribute() throws XmlLiteException
{
//	Trace.functionEntry( this, "handleAttribute" );

//	Trace.value( "Line number", _iLine );
//	Trace.stringValue( "Line", _strLine.substring( _iColumn ) );
	
	StringBuffer strbName  = new StringBuffer();
	StringBuffer strbValue = new StringBuffer();

	// Need to save the first column of the value
	//-------------------------------------------
	int iValueColumnNumber = -1;

	char character = ' ';

	// Obtain the attribute's name
	//----------------------------
	for( ; _iColumn<_strLine.length(); ++_iColumn )
	{
		character = _strLine.charAt( _iColumn );
		
		if( character == ' ' || character == '\t' || character == '=' )
		{
			break;
		}
		
		else if( character == '\n' || character == '\r' )
		{
			throw new XmlLiteException( Policy.bind("update.endOfLine"), _url.toString(), _strLine, _iLine, _iColumn );
		}
		
		else
		{
			strbName.append( character );
		}
	}

	// Look for equal sign
	//--------------------
	for( ; _iColumn<_strLine.length(); ++_iColumn )
	{
		character = _strLine.charAt( _iColumn );
		
		if( character == ' ' || character == '\t' )
		{
			continue;
		}
		
		else if( character == '=' )
		{
			_iColumn++;
			break;
		}
	}

	// Look for double quote
	//----------------------
	for( ; _iColumn<_strLine.length(); ++_iColumn )
	{
		character = _strLine.charAt( _iColumn );
		
		if( character == ' ' || character == '\t' )
		{
			continue;
		}
		
		else if( character == '\"' )
		{
			// Remember where the value of the attribute started
			//--------------------------------------------------
			iValueColumnNumber = ++_iColumn;
			break;
		}
	}

	// Look for trailing double quote
	//-------------------------------
	for( ; _iColumn<_strLine.length(); ++_iColumn )
	{
		character = _strLine.charAt( _iColumn );
		
		if( character == '\"' )
		{
			_iColumn++;
			_iMode = MODE_ELEMENT;
			break;
		}

		// Add the character to the value string
		//--------------------------------------	
		else
		{
			strbValue.append( character );
		}
	}

	// Create an attribute object, and add it to
	// its parent element object
	//------------------------------------------
	if( strbName.length() > 0 )
	{
		XmlLiteAttribute attribute = new XmlLiteAttribute( _parserElementCurrent, strbName.toString(), strbValue.toString() );
		if( _parserElementCurrent != null )
		{
			_parserElementCurrent.addAttribute( attribute );
		}
		
//		Trace.stringValue( strbName.toString(), strbValue.toString() );
	}
	
//	Trace.functionExit( this, "handleAttribute" );
}
/**
 */
protected void handleComment() 
{
//	Trace.functionEntry( this, "handleCommentBlock" );

	String strText = null;
	
	int iIndex = _strLine.indexOf( "-->", _iColumn );
	
	if( iIndex > _iColumn )
	{
		strText = _strLine.substring( _iColumn, iIndex );
//		Trace.string( strText );

		// Point to after the -->
		//-----------------------
		_iColumn = iIndex + 3;
		_iMode = MODE_UNKNOWN;
	}
	
	else
	{
		strText = _strLine.substring( _iColumn );
		
//		Trace.string( strText );
		_iColumn = _strLine.length();
		_iMode = MODE_COMMENT;
	}

	// Append the text to the current text attribute's text value
	//-----------------------------------------------------------
	if( strText != null && _parserTextAttributeCurrent != null )
	{
		_parserTextAttributeCurrent._strValue = _parserTextAttributeCurrent._strValue + strText;
//		Trace.stringValue( "string", _parserTextAttributeCurrent._strValue );
	}
	
//	Trace.functionExit( this, "handleCommentBlock" );
}
/**
 */
protected void handleCommentStart()
{
//	Trace.functionEntry( this, "handleCommentStart" );

	String strText = null;

	// Remember where the content starts
	//----------------------------------
	int iCommentStart = _iColumn + 4;
	
	int iIndex = _strLine.indexOf( "-->" );
	
	if( iIndex > _iColumn + 4 )
	{
		strText = _strLine.substring( _iColumn + 4, iIndex );
		_iColumn = iIndex + 3;
		_iMode = MODE_UNKNOWN;
	}
	
	else
	{
		strText = _strLine.substring( _iColumn + 4 );
		_iColumn = _strLine.length();
		_iMode = MODE_COMMENT;
	}
	
	if( strText != null )
	{
//		Trace.string( strText );
		
		// Create a COMMENT element
		//-------------------------
		XmlLiteElement parserElement = new XmlLiteElement( _parserElementCurrent, "#comment" );

		// Add the child element to its parent
		//------------------------------------
		if( _parserElementCurrent != null )
		{
			_parserElementCurrent.addChildElement( parserElement );
		}

		// Create a "text" attribute
		//--------------------------
		XmlLiteAttribute attribute = new XmlLiteAttribute( parserElement, "text", strText );
		parserElement.addAttribute( attribute );

		// Remember this attribute for the next line
		//------------------------------------------
		if( _iMode == MODE_COMMENT )
		{
			_parserTextAttributeCurrent = attribute;
		}
	}
	
//	Trace.functionExit( this, "handleCommentStart" );
}
/**
 */
protected void handleDoctype()
{
//	Trace.functionEntry( this, "handleDoctype" );

	int iIndex = _strLine.indexOf( ">" );;
	
	if( iIndex > _iColumn )
	{
		_iColumn = iIndex + 1;
		_iMode = MODE_UNKNOWN;
	}
	else
	{
		_iColumn = _strLine.length();
	}
	
//	Trace.functionExit( this, "handleDoctype" );
}
/**
 */
protected void handleElement()
{
//	Trace.functionEntry( this, "handleElement" );
	
//	Trace.stringValue( "Line", _strLine.substring( _iColumn ) );
	
	for( ; _iColumn<_strLine.length(); ++_iColumn )
	{
		char character = _strLine.charAt( _iColumn );

		// Look for non space
		//-------------------	
		if( character == ' ' || character == '\t' || character == '\r' || character == '\n' )
		{
			continue;
		}

		// End of element start
		//---------------------
		else if( character == '>' )
		{
			_iColumn++;
			_iMode = MODE_UNKNOWN;
			break;
		}

		// End of element
		//---------------
		else if( _strLine.indexOf( "/>", _iColumn ) == _iColumn )
		{
			_iColumn += 2;
			_iMode = MODE_UNKNOWN;

			// Pop the stack
			// Replace the current element with its parent
			//--------------------------------------------
			if( _parserElementCurrent != null )
			{
//				_parserElementCurrent._iLineNumberLast = _iLine;
				_parserElementCurrent = (XmlLiteElement)_parserElementCurrent._entryParent;
			}
			break;
		}

		// Attribute start
		//----------------
		else
		{
			_iMode = MODE_ATTRIBUTESTART;
			break;
		}
	}

	
//	Trace.functionExit( this, "handleElement" );
}
/**
 */
protected void handleElementEnd() throws XmlLiteException
{
	// </x>
	//-----
	int iIndex = _strLine.indexOf( '>', _iColumn );

	if( iIndex > _iColumn + 2 )
	{
		
		String strElementName = _strLine.substring( _iColumn + 2, iIndex );

		if( strElementName.equals( _parserElementCurrent.getName() ) == false )
		{
			throw new XmlLiteException( Policy.bind("update.expectingEnd", _parserElementCurrent._strName), _url.toString(), _strLine, _iLine, _iColumn );
		}
			
		_iColumn = iIndex + 1;
		_iMode = MODE_UNKNOWN;
		
		// Pop the stack
		// Replace the current element with its parent
		//--------------------------------------------
		if( _parserElementCurrent != null )
		{
//			_parserElementCurrent._iLineNumberLast = _iLine;
			_parserElementCurrent = (XmlLiteElement)_parserElementCurrent._entryParent;
		}
	}

	else
	{
		throw new XmlLiteException( Policy.bind("update.expecting","\">\""), _url.toString(), _strLine, _iLine, _iColumn );
	}
}
/**
 */
protected void handleElementStart() throws XmlLiteException
{
//	Trace.functionEntry( this, "handleElementStart" );
	
		if( _strLine.indexOf( "<!--", _iColumn ) == _iColumn )
	{
		_iMode = MODE_COMMENTSTART;
	}
	
	else if( _strLine.indexOf( "<!", _iColumn ) == _iColumn )
	{
		_iMode = MODE_DOCTYPESTART;
	}
		
	else if( _strLine.indexOf( "<?", _iColumn ) == _iColumn )
	{
		_iMode = MODE_XMLSTART;
	}

	else if( _strLine.indexOf( "</", _iColumn ) == _iColumn )
	{
		_iMode = MODE_ELEMENTENDSTART;
	}

	// Real element
	//-------------
	else
	{
		// Look for first non space character
		//-----------------------------------
		++_iColumn;
		
		StringBuffer strbName = new StringBuffer();

		for( ; _iColumn<_strLine.length(); ++_iColumn )
		{
			if( _strLine.charAt( _iColumn ) != ' ' &&
				_strLine.charAt( _iColumn ) != '\t'   )
			{
				break;
			}	
		}

		// Look for name
		//--------------
		char character = ' ';

		for( ; _iColumn<_strLine.length(); ++_iColumn )
		{
			character = _strLine.charAt( _iColumn );

			// End of element name
			//--------------------
			if( character == '>' )
			{
				_iColumn++;
				_iMode = MODE_UNKNOWN;
				break;
			}

			else if( character == '\r' || character == '\n' )
			{
				throw new XmlLiteException( Policy.bind("update.expecting","\">\""), _url.toString(), _strLine, _iLine, _iColumn );
			}
			
			else if( _strLine.indexOf( "/>", _iColumn ) == _iColumn )
			{
				_iColumn += 2;
				_iMode = MODE_UNKNOWN;
				break;
			}
			
			// Part of name
			//-------------
			else if( character != ' ' && character != '\t' )
			{
				strbName.append( character );
				_iMode = MODE_ELEMENT;
			}

			// End of name
			//------------
			else
			{
				_iMode = MODE_ELEMENT;
				break;
			}
		}

		// Create a new element node, and link it to its parent
		//-----------------------------------------------------
		if( strbName.length() > 0 )
		{
			XmlLiteElement parserElement = new XmlLiteElement( _parserElementCurrent, strbName.toString() );

			// Add the child element to its parent
			//------------------------------------
			if( _parserElementCurrent != null )
			{
				_parserElementCurrent.addChildElement( parserElement );
			}

			// This must be the root element
			// since it doesn't have a parent
			//-------------------------------
			else
			{
				_parserElementRoot = parserElement;
			}

			// Set this element as the current element
			//----------------------------------------
			_parserElementCurrent = parserElement;
		}
		
//		Trace.stringValue( "Element Name", strbName.toString() );
	}

//	Trace.functionExit( this, "handleElementStart" );
}
/**
 */
protected void handleText()
{
	String strText = null;
	
	int iIndex = _strLine.indexOf( "<", _iColumn );
	
	if( iIndex > _iColumn )
	{
		strText = _strLine.substring( _iColumn, iIndex );
		_iColumn = iIndex;
		_iMode = MODE_UNKNOWN;
	}
	
	else
	{
		strText = _strLine.substring( _iColumn );
		
		_iColumn = _strLine.length();
		_iMode = MODE_TEXT;
	}

	// Append the text to the current text attribute's text value
	// Ensure carriage return between lines
	//-----------------------------------------------------------
	if( strText != null && _parserTextAttributeCurrent != null )
	{
		_parserTextAttributeCurrent._strValue = _parserTextAttributeCurrent._strValue + "\n" + strText;
	}
}
/**
 */
protected void handleTextStart()
{
	String strText = null;

	// Remember the start of this attribute
	//-------------------------------------
	int iTextStart = _iColumn;
	
	int iIndex = _strLine.indexOf( "<", _iColumn );
	
	if( iIndex > _iColumn )
	{
		strText = _strLine.substring( _iColumn, iIndex );
//		Trace.string( strText );
		_iColumn = iIndex;
		_iMode = MODE_UNKNOWN;
	}
	
	else
	{
		strText = _strLine.substring( _iColumn );
		
//		Trace.string( strText );
		_iColumn = _strLine.length();
		_iMode = MODE_TEXT;
	}

	if( strText != null )
	{
		// Create a TEXT element
		//----------------------
		XmlLiteElement parserElement = new XmlLiteElement( _parserElementCurrent, "#text" );

		// Add the child element to its parent
		//------------------------------------
		if( _parserElementCurrent != null )
		{
			_parserElementCurrent.addChildElement( parserElement );
		}

		// Create a "text" attribute
		//--------------------------
		XmlLiteAttribute attribute = new XmlLiteAttribute( parserElement, "text", strText);
		parserElement.addAttribute( attribute );

		// Remember this attribute for the next line
		//------------------------------------------
		if( _iMode == MODE_TEXT )
		{
			_parserTextAttributeCurrent = attribute;
		}
	}
	
}
/**
 */
protected void handleUnknown()
{
//	Trace.functionEntry( this, "handleUnknown" );
	
	for( ; _iColumn<_strLine.length(); ++_iColumn )
	{
		char character = _strLine.charAt( _iColumn );
		
		if( character == ' ' || character == '\t' || character == '\n' || character == '\r' )
		{
			continue;
		}

		else if( character == '<' )
		{
			_iMode = MODE_ELEMENTSTART;
			break;
		}
		
		else
		{
			_iMode = MODE_TEXTSTART;
			break;
		}
	}

//	Trace.functionExit( this, "handleUnknown" );
}
/**
 */
protected void handleXml()
{
//	Trace.functionEntry( this, "handleXml" );
	
	if ( _strLine.indexOf("UTF-8") > 0) {
		int linesRead = _lineReader.getLineNumber();
		// switch stream reader to UTF8.  Close and reopen the streams 
		try {
			InputStream newInputStream = null;
			BaseURLHandler.Response response = BaseURLHandler.open(_url);
			if( response.getResponseCode() == HttpURLConnection.HTTP_OK )
				newInputStream = response.getInputStream();
			
			if (newInputStream != null) {
				try{_inputStream.close();} catch(Exception x) {}				
				_inputStream = newInputStream;
				InputStreamReader reader = new InputStreamReader(_inputStream, "UTF-8");
				_lineReader = new LineNumberReader(reader);			
				for (int i = 0; i < linesRead; i++)
					_lineReader.readLine();
			}
		} catch (UnsupportedEncodingException ex) {}
		catch (IOException ex) {}

	}

		
	int iIndex = _strLine.indexOf( "?>" );
		
	if( iIndex > _iColumn )
	{
		_iColumn = iIndex + 2;
		_iMode = MODE_UNKNOWN;
	}
	else
	{
		_iColumn = _strLine.length();
	}
	
//	Trace.functionExit( this, "handleXml" );
}
/**
 * 
 * @param strLog java.lang.String
 */
public boolean load(XmlLite lite, URL url) throws XmlLiteException {

	_url = url;
	_parserElementCurrent = lite;

	// Obtain the content of the URL
	//------------------------------
	_inputStream = null;
	Object objContent = null;

	try {
		BaseURLHandler.Response response = BaseURLHandler.open(url);
		if( response.getResponseCode() == HttpURLConnection.HTTP_OK )
			_inputStream = response.getInputStream();
	}
	catch (IOException ex) {

	}

	// Cannot open the stream, create a new file
	//------------------------------------------
//	if (inputStream == null) {
//		createFile(url);
//	}

	// Read in the file
	//-----------------
	if (_inputStream != null) {

		InputStreamReader reader = new InputStreamReader(_inputStream);
		_lineReader = new LineNumberReader(reader);

		_iMode = MODE_UNKNOWN;

		do {
			try {
				_strLine = _lineReader.readLine();
			}
			catch (IOException ex) {
				_strLine = null;
			}
			if (_strLine != null) {
				processLine();
			}
		}
		while (_strLine != null);

		if (_parserElementCurrent != null && _parserElementCurrent.getName().equals("root") == false) {
			throw new XmlLiteException(Policy.bind("update.expectingEnd", _parserElementCurrent.getName()), _url.toString(), _strLine, _iLine, _iColumn);
		}
		try{_inputStream.close();} catch(Exception x) {}
		return true;
	}

	else if (objContent instanceof String) {

		_reader = new XmlLiteReader((String) objContent);

		_iMode = MODE_UNKNOWN;

		do {
			_strLine = _reader.readLine();

			if (_strLine != null) {
				processLine();
			}
		}
		while (_strLine != null);

		if (_parserElementCurrent != null) {
			throw new XmlLiteException(Policy.bind("update.expectingEnd", _parserElementCurrent.getName()), _url.toString(), _strLine, _iLine, _iColumn);
		}

		return true;
	}

	return false;
}
/**
 */
protected void processLine() throws XmlLiteException
{
//	Trace.functionEntry( this, "processLine" );

	_iLine++;
	_iColumn = 0;

	// Continue in the mode that was in effect at the end of the last line
	//--------------------------------------------------------------------
	while( _iColumn < _strLine.length() )
	{
		switch( _iMode )
		{
			case MODE_UNKNOWN:           handleUnknown();      break;
			case MODE_ATTRIBUTESTART:    handleAttribute();    break;
			case MODE_COMMENT:           handleComment();      break;
			case MODE_COMMENTSTART:      handleCommentStart(); break;
			case MODE_DOCTYPESTART:      handleDoctype();      break;
			case MODE_ELEMENT:           handleElement();      break;
			case MODE_ELEMENTSTART:      handleElementStart(); break;
			case MODE_ELEMENTENDSTART:   handleElementEnd();   break;
			case MODE_TEXT:              handleText();         break;
			case MODE_TEXTSTART:         handleTextStart();    break;
			case MODE_XMLSTART:          handleXml();          break;
		}
	}

//	Trace.functionExit( this, "processLine" );
}
/**
 * Attempts to write to the url connection's output stream.  If this fails,
 * then attempts to write to a file.
 * @return boolean
 */
public void save(XmlLite lite, URL url) throws XmlLiteException {

	// Attempt to open a connection
	//-----------------------------
	URLConnection connection = null;

	try {
		connection = url.openConnection();
	}
	catch (IOException ex) {
		throw new XmlLiteException(ex.getLocalizedMessage(), url.toString(), null, -1, -1);
	}

	// Attempt to obtain an output stream
	// This fails if the protocol doesn't support writing
	//---------------------------------------------------
	OutputStream outputStream = null;

	if (connection != null) {

		try {
			connection.setDoOutput( true );
			outputStream = connection.getOutputStream();
		}
		catch (IOException ex) {
			outputStream = null;
		}
	}

	// Attempt to write to the output stream
	//--------------------------------------
	if (outputStream != null) {
		try {
			outputStream.write(lite.getPersistentString().getBytes());
		}
		catch (IOException ex) {
			throw new XmlLiteException(ex.getLocalizedMessage(), url.toString(), null, -1, -1);
		}
	}

	// Attempt to save it as a file
	//-----------------------------
	else
	{
		saveAsFile( lite, url );
	}

	return;
}
/**
 * @param log org.eclipse.update.log.Log
 * @param url java.net.URL
 */
public void saveAsFile(XmlLite lite, URL url) throws XmlLiteException {

	boolean bSaved = false;

	File file = createFile( url );

	String strPersistent = lite.getPersistentString();

	FileWriter writer = null;

	try {
		writer = new FileWriter(file);
		writer.write(strPersistent);
		writer.flush();
		writer.close();
	}
	catch (IOException ex) {
		throw new XmlLiteException(Policy.bind("update.unableToWrite"), url.getFile(), null, -1, -1);
	}

	return;
}
}
