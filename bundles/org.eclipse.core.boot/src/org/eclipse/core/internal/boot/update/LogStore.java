package org.eclipse.core.internal.boot.update;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.File;import java.io.FileWriter;import java.io.IOException;import java.io.InputStream;import java.io.InputStreamReader;import java.io.LineNumberReader;import java.io.OutputStream;import java.net.URL;import java.net.URLConnection;import org.eclipse.core.internal.boot.Policy;
/**
 * This class manages the loading, storing, parsing and creation of
 * the log entry tree.
 */

public class LogStore {
	
	protected final static int MODE_UNKNOWN           =  0;
	protected final static int MODE_ATTRIBUTESTART    =  1;
	protected final static int MODE_ELEMENT           =  2;
	protected final static int MODE_ELEMENTSTART      =  3;
	protected final static int MODE_ELEMENTENDSTART   =  4;
	protected final static int MODE_TEXT              =  5;
	protected final static int MODE_TEXTSTART         =  6;

	protected URL                          _url                  = null;
	protected int                          _iMode                = MODE_UNKNOWN;
	protected LogStringReader              _reader               = null;
	protected String                       _strLine              = null;
	protected LogEntry                     _parserElementCurrent = null;
	protected LogEntry                     _parserElementRoot    = null;
	protected int                          _iLine                = -1; // 0 based
	protected int                          _iColumn              = -1; // 0 based

/**
 * LogModel constructor comment.
 */
public LogStore() {
}
/**
 */
public File createFile(URL url) throws LogStoreException {

	String strFilespec = url.getFile().replace('/',File.separatorChar);
	int k = strFilespec.indexOf(":");
	if (k != -1 && strFilespec.startsWith(File.separator)) {
		strFilespec = strFilespec.substring(1);
	}

	File file = new File(strFilespec);
	boolean bExists = file.exists();

	if (bExists == false) {
		try {
			// Create directory structure if necessary
			//----------------------------------------
			int iIndex = strFilespec.lastIndexOf(File.separator);
			if (iIndex >= 0) {
				String strPath = strFilespec.substring(0, iIndex+1);

				File fileDirectory = new File(strPath);
				if (fileDirectory.exists() == false) {
					fileDirectory.mkdirs();
				}
			}
			bExists = file.createNewFile();
			return file;
		}
		catch (IOException ex) {
			throw new LogStoreException(Policy.bind("S_Unable_to_open_file"), strFilespec, null, -1, -1);
		}
	}
	
	return file;
}
/**
 */
protected void handleAttribute() throws LogStoreException
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
			throw new LogStoreException( Policy.bind("S_Unexpected_end_of_line"), _url.toString(), _strLine, _iLine, _iColumn );
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
		LogEntryProperty attribute = new LogEntryProperty( _parserElementCurrent, strbName.toString(), strbValue.toString() );
		if( _parserElementCurrent != null )
		{
			_parserElementCurrent.addProperty( attribute );
		}
		
//		Trace.stringValue( strbName.toString(), strbValue.toString() );
	}
	
//	Trace.functionExit( this, "handleAttribute" );
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
				_parserElementCurrent = (LogEntry)_parserElementCurrent._entryParent;
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
protected void handleElementEnd() throws LogStoreException
{
	// </x>
	//-----
	int iIndex = _strLine.indexOf( '>', _iColumn );

	if( iIndex > _iColumn + 2 )
	{
		
		String strElementName = _strLine.substring( _iColumn + 2, iIndex );

		if( strElementName.equals( _parserElementCurrent.getName() ) == false )
		{
			throw new LogStoreException( Policy.bind("S_Expecting_end_element") + ": </" + _parserElementCurrent._strName + ">", _url.toString(), _strLine, _iLine, _iColumn );
		}
			
		_iColumn = iIndex + 1;
		_iMode = MODE_UNKNOWN;
		
		// Pop the stack
		// Replace the current element with its parent
		//--------------------------------------------
		if( _parserElementCurrent != null )
		{
//			_parserElementCurrent._iLineNumberLast = _iLine;
			_parserElementCurrent = (LogEntry)_parserElementCurrent._entryParent;
		}
	}

	else
	{
		throw new LogStoreException( Policy.bind("S_Expecting") +" \">\"", _url.toString(), _strLine, _iLine, _iColumn );
	}
}
/**
 */
protected void handleElementStart() throws LogStoreException
{
//	Trace.functionEntry( this, "handleElementStart" );
	
	if( _strLine.indexOf( "</", _iColumn ) == _iColumn )
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
				throw new LogStoreException( Policy.bind("S_Expecting") + " '>'", _url.toString(), _strLine, _iLine, _iColumn );
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
			LogEntry parserElement = new LogEntry( _parserElementCurrent, strbName.toString() );

			// Add the child element to its parent
			//------------------------------------
			if( _parserElementCurrent != null )
			{
				_parserElementCurrent.addChildEntry( parserElement );
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
public boolean load(Log log, URL url) throws LogStoreException {

	_url = url;
	_parserElementCurrent = log;

	// Obtain the content of the URL
	//------------------------------
	InputStream inputStream = null;
	Object objContent = null;

	try {
		inputStream = BaseURLHandler.open(url).getInputStream();
	}
	catch (IOException ex) {

	}

	// Cannot open the stream, create a new file
	//------------------------------------------
	if (inputStream == null) {
		createFile(url);
	}

	// Read in the file
	//-----------------
	if (inputStream != null) {

		InputStreamReader reader = new InputStreamReader(inputStream);
		LineNumberReader lineReader = new LineNumberReader(reader);

		_iMode = MODE_UNKNOWN;

		do {
			try {
				_strLine = lineReader.readLine();
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
			throw new LogStoreException(Policy.bind("S_Expecting_end_element") + ": </" + _parserElementCurrent.getName() + ">", _url.toString(), _strLine, _iLine, _iColumn);
		}
		try{inputStream.close();} catch(Exception x) {}
		return true;
	}

	else if (objContent instanceof String) {

		_reader = new LogStringReader((String) objContent);

		_iMode = MODE_UNKNOWN;

		do {
			_strLine = _reader.readLine();

			if (_strLine != null) {
				processLine();
			}
		}
		while (_strLine != null);

		if (_parserElementCurrent != null) {
			throw new LogStoreException(Policy.bind("S_Expecting_end_element") + "< :/" + _parserElementCurrent.getName() + ">", _url.toString(), _strLine, _iLine, _iColumn);
		}

		return true;
	}

	return false;
}
/**
 */
protected void processLine() throws LogStoreException
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
			case MODE_ELEMENT:           handleElement();      break;
			case MODE_ELEMENTSTART:      handleElementStart(); break;
			case MODE_ELEMENTENDSTART:   handleElementEnd();   break;
			case MODE_TEXT:              handleText();         break;
			case MODE_TEXTSTART:         handleTextStart();    break;
		}
	}

//	Trace.functionExit( this, "processLine" );
}
/**
 * Attempts to write to the url connection's output stream.  If this fails,
 * then attempts to write to a file.   Fails when protocol is file: or valoader:
 * thus our local writes are all file I/O currently
 */
public void save(Log log, URL url) throws LogStoreException {

	// Attempt to open a connection
	//-----------------------------
	URLConnection connection = null;

	try {
		connection = url.openConnection();
	}
	catch (IOException ex) {
		throw new LogStoreException(ex.getLocalizedMessage(), url.toString(), null, -1, -1);
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
			outputStream.write(log.getPersistentString().getBytes());
		}
		catch (IOException ex) {
			throw new LogStoreException(ex.getLocalizedMessage(), url.toString(), null, -1, -1);
		}
	}

	// Attempt to save it as a file
	//-----------------------------
	else
	{
		saveAsFile( log, url );
	}

	return;
}
/**
 */
public void saveAsFile(Log log, URL url) throws LogStoreException {

	boolean bSaved = false;

	File file = createFile( url );

	String strLog = log.getPersistentString();

	FileWriter writer = null;

	try {
		writer = new FileWriter(file);
		writer.write(strLog);
		writer.flush();
		writer.close();
	}
	catch (IOException ex) {
		throw new LogStoreException(Policy.bind("S_Unable_to_write_to_file"), url.getFile(), null, -1, -1);
	}

	return;
}
}
