package org.eclipse.update.internal.core;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.webdav.http.client.Response;
import java.util.jar.*;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.update.internal.ui.*;
import org.eclipse.core.internal.boot.update.*;
import java.io.*;
import java.net.*;
import java.util.*;

/**
 * This class represents an operation such as "copy" or "unzip".
 * Attributes are id, status (pending/complete), action,
 * source, target.  Children are messages.
 */

public class UMSessionManagerOperation extends UMSessionManagerEntry {
		
	protected ArrayList         _alMessages     = new ArrayList();
	protected ILogEntryProperty _propertyAction = null;
	protected ILogEntryProperty _propertyID     = null;
	protected ILogEntryProperty _propertySource = null;
	protected ILogEntryProperty _propertyTarget = null;
/**
 * UpdateManagerOperation constructor comment.
 */
public UMSessionManagerOperation( ILogEntry logEntry ) {
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

	// Source
	//-------
	_propertySource = _logEntry.getProperty(UpdateManagerConstants.STRING_SOURCE);

	if (_propertySource == null) {
		_propertySource = new LogEntryProperty(logEntry, UpdateManagerConstants.STRING_SOURCE, UpdateManagerConstants.STRING_EMPTY );
		logEntry.addProperty( _propertySource );
	}

	// Target
	//-------
	_propertyTarget = _logEntry.getProperty(UpdateManagerConstants.STRING_TARGET);

	if (_propertyTarget == null) {
		_propertyTarget = new LogEntryProperty(logEntry, UpdateManagerConstants.STRING_TARGET, UpdateManagerConstants.STRING_EMPTY );
		logEntry.addProperty( _propertyTarget);
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

	// Source
	//-------
	property = _logEntry.getProperty(UpdateManagerConstants.STRING_SOURCE);

	if (property != null) {
		_propertySource = property;
	}

	// Target
	//-------
	property = _logEntry.getProperty(UpdateManagerConstants.STRING_TARGET);

	if (property != null) {
		_propertyTarget = property;
	}


	ILogEntry[] entries = logEntry.getChildEntries();

	// Messages
	//---------
	int iIndex = 0;
	UMSessionManagerMessage message = null;
	
	for( int i = 0; i < entries.length; ++i) {
		if (entries[i] != null && entries[i].getName().equals(UpdateManagerConstants.STRING_MESSAGE) == true) {
			message = new UMSessionManagerMessage( entries[i] );			
	        _alMessages.add( message );

	        message.buildTreeFromLog( entries[i] );
		}
	}
}
/**
 * Copies all data from input stream to output stream.
 * @param inputStream java.io.InputStream
 * @param outputStream java.io.OutputStream
 */
public String copyStream(InputStream streamInput, OutputStream streamOutput, long lContentLength, String strTaskName, IProgressMonitor progressMonitor, boolean bSubtask) {

	String strErrorMessage = null;

	// Allocate buffer
	//---------------- 
	byte[] byteArray = null;

	if (streamInput != null && streamOutput != null) {
		byteArray = new byte[64000];
	}

	if (byteArray != null) {

		// Copy
		//-----
		if (progressMonitor != null) {
			if (bSubtask == false) {
				if (lContentLength > 0 && lContentLength < Integer.MAX_VALUE)
					progressMonitor.beginTask(UpdateManagerStrings.getString("S_Copy") + ": " +strTaskName, (int) lContentLength);
				else
					progressMonitor.beginTask(UpdateManagerStrings.getString("S_Copy") + ": "  + strTaskName, IProgressMonitor.UNKNOWN);
			}
			else {
				progressMonitor.subTask(UpdateManagerStrings.getString("S_Copy") + ": "  + strTaskName);
			}
		}

		int iBytesReceived = 0;

		do {
			// Read
			//-----
			try {
				iBytesReceived = streamInput.read(byteArray);
			}
			catch (IOException ex) {
				iBytesReceived = 0;
				strErrorMessage = UpdateManagerStrings.getString("S_Error_reading_from_input_stream") + ": " + ex.getMessage();
			}

			// Write
			//------
			if (iBytesReceived > 0) {
				try {
					streamOutput.write(byteArray, 0, iBytesReceived);

					if (progressMonitor != null && bSubtask == false)
						progressMonitor.worked(iBytesReceived);
				}
				catch (IOException ex) {
					iBytesReceived = 0;
					strErrorMessage = UpdateManagerStrings.getString("S_Error_writing_to_output_stream") + ": " + ex.getMessage();
				}
			}
		}
		while (iBytesReceived > 0);

		if (progressMonitor != null && bSubtask == false) {
			progressMonitor.done();
		}
	}

	return strErrorMessage;
}
/**
 *
 * @return org.eclipse.update.internal.core.UMSessionManagerMessage
 * @param actionType java.lang.String
 */
public UMSessionManagerMessage createMessage() {

	// Create a new log entry
	//-----------------------
	ILogEntry logEntryChild = new LogEntry( _logEntry, UpdateManagerConstants.STRING_MESSAGE );
	_logEntry.addChildEntry( logEntryChild );

	// Create the operation object
	//----------------------------
	UMSessionManagerMessage message = new UMSessionManagerMessage( logEntryChild );
	
	message.buildTreeFromLog( logEntryChild );
	
	_alMessages.add( message );
	
	return message;
}
/**
 *
 * 
 */
public boolean doCopy(IProgressMonitor progressMonitor) {

	String strErrorMessage = null;

	// Input URL
	//----------
	URL urlInput = null;

	try {
		urlInput = new URL(getSource());
	}
	catch (MalformedURLException ex) {
		strErrorMessage = createMessageString(UpdateManagerStrings.getString("S_Source_URL_is_malformed"), ex);
	}

	URL urlOutput = null;

	// Output URL
	//-----------
	try {
		urlOutput = new URL(getTarget());
	}
	catch (MalformedURLException ex) {
		strErrorMessage = createMessageString(UpdateManagerStrings.getString("S_Target_URL_is_malformed"), ex);
	}

	// Input
	//-------
	InputStream streamInput = null;

	long lContentLength = 0;

	if (urlInput != null && urlOutput != null) {

		// Input stream
		//-------------
		try {
			URLHandler.Response response = (URLHandler.Response)URLHandler.open(urlInput);
			lContentLength = response.getContentLength();
			streamInput = response.getInputStream();
		}
		catch (IOException ex) {
			strErrorMessage = createMessageString(UpdateManagerStrings.getString("S_Unable_to_obtain_source_input_stream"), ex);
		}

	}

	// Output
	//-------
	OutputStream streamOutput = null;

	if (streamInput != null) {

		// Output connection
		//------------------
		URLConnection urlConnectionOutput = null;
		
		try {
			urlConnectionOutput = urlOutput.openConnection();
			urlConnectionOutput.setDoOutput(true);
		}
		catch (IOException ex) {
			strErrorMessage = createMessageString( UpdateManagerStrings.getString("S_Unable_to_connect"), ex );
		}

		// Output stream
		//--------------
		if (urlConnectionOutput != null) {
			try {
				streamOutput = urlConnectionOutput.getOutputStream();
			}
			catch (IOException ex) {
				strErrorMessage = createMessageString( UpdateManagerStrings.getString("S_Unable_to_obtain_target_output_stream"), ex );
			}
		}

		// Attempt to create a file output stream
		// This is used when protocol is file: or valoader:
		// thus our local writes are all file I/O currently
		//-------------------------------------------------
		if (streamOutput == null) {
	        try
	        {
		 		streamOutput = getFileOutputStream(urlOutput);
	        }
	        catch( IOException ex){
				strErrorMessage = createMessageString( UpdateManagerStrings.getString("S_Unable_to_create_file"), ex );
			}
		}
	}

	if( streamInput != null && streamOutput != null )
	{
	    strErrorMessage = copyStream( streamInput, streamOutput, lContentLength, urlInput.toExternalForm(), progressMonitor, false );
	}    

	// Close input stream
	//-------------------
	if (streamInput != null) {
		try {
			streamInput.close();
		}
		catch (IOException ex) {
		}
	}

	// Close output stream
	//--------------------
	if (streamOutput != null) {
		try {
			streamOutput.flush();
			streamOutput.close();
		}
		catch (IOException ex) {
			strErrorMessage = createMessageString( UpdateManagerStrings.getString("S_Error_closing_output_stream"), ex );
		}
	}
	
	// Increment the number of attempts
	//---------------------------------
	incrementAttemptCount();

	// Error return
	//-------------
	if (strErrorMessage != null) {
		UMSessionManagerMessage message = createMessage();
		message.setText(strErrorMessage);
		setStatus(UpdateManagerConstants.STATUS_FAILED);
		return false;
	}

	// Successful return
	//------------------
	setStatus(UpdateManagerConstants.STATUS_SUCCEEDED);

	return true;
}
/**
 *
 * 
 */
public boolean doUnzip(IProgressMonitor progressMonitor) {

	String strErrorMessage = null;

	// Input URL
	//----------
	URL urlInput = null;
	try {
		urlInput = new URL(getSource());
	}
	catch (MalformedURLException ex) {
		strErrorMessage = createMessageString(UpdateManagerStrings.getString("S_Source_URL_is_malformed"), ex);
	}

	// Output URL
	//-----------
	URL urlOutput = null;
	try {
		urlOutput = new URL(getTarget());
	}
	catch (MalformedURLException ex) {
		strErrorMessage = createMessageString(UpdateManagerStrings.getString("S_Target_URL_is_malformed"), ex);
	}

	// For unzipping plugins or component/configuration jar, 
	// set up the list of directories to look for
	//-----------------------------------------------------
	Vector dirNames = new Vector();
	if (getAction() == UpdateManagerConstants.OPERATION_UNZIP_PLUGINS ) {
		IComponentDescriptor comp = null;
		if (getData() instanceof IComponentEntryDescriptor) {
			comp = ((IComponentEntryDescriptor) getData()).getComponentDescriptor();
		} else {
			comp = (IComponentDescriptor) getData();
		}
		IPluginEntryDescriptor[] plugins = comp.getPluginEntries();
		for (int i=0; i<plugins.length; i++) 
			dirNames.addElement( UMEclipseTree.PLUGINS_DIR + "/" + plugins[i].getDirName());
		IFragmentEntryDescriptor[] fragments = comp.getFragmentEntries();
		for (int i=0; i<fragments.length; i++) 
			dirNames.addElement( UMEclipseTree.FRAGMENTS_DIR + "/" + fragments[i].getDirName());	
	} else if (getAction() == UpdateManagerConstants.OPERATION_UNZIP_INSTALL) {
		IInstallable desc = (IInstallable) getData();
		if (getData() instanceof IProductDescriptor) 
			dirNames.addElement(UMEclipseTree.INSTALL_DIR + "/" + UMEclipseTree.PRODUCTS_DIR + "/" + desc.getDirName());
		else 
			dirNames.addElement(UMEclipseTree.INSTALL_DIR + "/" + UMEclipseTree.COMPONENTS_DIR + "/" + desc.getDirName());
	}
	
	// Create a file specification from the input URL
	//-----------------------------------------------
	String strFilespec = UMEclipseTree.getFileInPlatformString(urlInput);

	JarFile jarFile = null;

	try {
		jarFile = new JarFile(strFilespec);
	}
	catch (IOException ex) {
		strErrorMessage = createMessageString(UpdateManagerStrings.getString("S_Unable_to_open_Jar_file"), ex);
	}

	if (jarFile != null) {

		JarEntry entry = null;
		InputStream streamInputEntry = null;

		int iCount = jarFile.size();
		
		// Set up progress monitor
		// Compute the filename without the path information
		//--------------------------------------------------
		String strFilename = strFilespec;
		int iIndex = strFilespec.lastIndexOf(File.separatorChar);
		
		if (iIndex >= 0 && iIndex < strFilespec.length() - 1) {
			strFilename = strFilespec.substring(iIndex + 1);
		}

		if (progressMonitor != null) {
			progressMonitor.beginTask(UpdateManagerStrings.getString("S_Install") + ": " + strFilename, iCount);
		}

		// Write out a safety lock
		//------------------------
		IUMLock lock = new UMLock();
		if (!lock.exists())
			lock.set(strFilename);					

		// Do each jar file entry
		//-----------------------
		Enumeration enum = jarFile.entries();
		while (enum.hasMoreElements() == true) {
			entry = (JarEntry) enum.nextElement();
			String entryName = entry.getName();
			
			if (entryName.startsWith(IManifestAttributes.MANIFEST_DIR))  {
				if (progressMonitor != null) progressMonitor.worked(1);
				continue;
			}
			
			if (getAction() == UpdateManagerConstants.OPERATION_UNZIP_PLUGINS ) {
				// Unzip plugins and fragments.  Skip entries not under plugins/  or fragments/ trees
				//-----------------------------------------------------------------------------------
				if ((!entryName.startsWith(UMEclipseTree.PLUGINS_DIR) &&
					!entryName.startsWith(UMEclipseTree.FRAGMENTS_DIR)) 
					|| entryName.endsWith("/")) {
					if (progressMonitor != null) progressMonitor.worked(1);
					continue;
				}
				int second_slash = entryName.indexOf("/", (entryName.indexOf("/")+1));
				String prefix = entryName.substring(0,second_slash);
				if (!dirNames.contains(prefix)) 
					continue;
			} else if (getAction() == UpdateManagerConstants.OPERATION_UNZIP_INSTALL) {
				// Skip over entries that don't start with the right dir naming convention (id_version)
				//-------------------------------------------------------------------------------------
				if (!entryName.startsWith((String)dirNames.firstElement()) || entryName.endsWith("/")) {
					if (progressMonitor != null) progressMonitor.worked(1);
					continue;
				}
			} else if (getAction() == UpdateManagerConstants.OPERATION_UNZIP_BINDIR) {
				// Unzip the bin directory, if it exists
				//--------------------------------------
				if (!entryName.startsWith(UMEclipseTree.BIN_DIR) || entryName.endsWith("/")) {
					if (progressMonitor != null) progressMonitor.worked(1);
					continue;
				}
			}
			try {
				streamInputEntry = jarFile.getInputStream(entry);
			}
			catch (IOException ex) {
				streamInputEntry = null;
				strErrorMessage = createMessageString(UpdateManagerStrings.getString("S_Unable_to_open_jar_entry_input_stream"), ex);
				break;
			}
			catch (SecurityException ex) {
				streamInputEntry = null;
				strErrorMessage = createMessageString(UpdateManagerStrings.getString("S_Encountered_jar_entry_security_exception"), ex);
				break;
			}


			// Create an output URL
			//---------------------
			URL urlOutputFile = null;

			if (streamInputEntry != null) {
				try {
					// Ensure file separator between target directory, and source file path
					//---------------------------------------------------------------------
					StringBuffer strbTarget = new StringBuffer(getTarget());
					if (getTarget().endsWith("/") == false) {
						strbTarget.append('/');
					}
					strbTarget.append(entryName);
					urlOutputFile = new URL(strbTarget.toString());
					File fTarget = new File(UMEclipseTree.getFileInPlatformString(urlOutputFile));
					if (fTarget.exists()) {		// we will not override existing files
						if (progressMonitor != null) progressMonitor.worked(1);
						continue;
					}
				}
				catch (MalformedURLException ex) {
					strErrorMessage = createMessageString(UpdateManagerStrings.getString("S_Unable_to_create_jar_entry_URL"), ex);
				}
			}


			// Create a file output stream
			//----------------------------
			OutputStream streamOutputFile = null;

			if (urlOutputFile != null) {
				try {
					streamOutputFile = getFileOutputStream(urlOutputFile);
				}
				catch (IOException ex) {
					strErrorMessage = createMessageString(UpdateManagerStrings.getString("S_Unable_to_create_output_file_for_Jar_entry") + ": " + urlOutputFile.toExternalForm(), ex);
					break;
				}
			}

			// Copy from input to output stream
			//---------------------------------
			if (streamInputEntry != null && streamOutputFile != null) {

	            // Do not alter progress monitor
	            //------------------------------
				strErrorMessage = copyStream(streamInputEntry, streamOutputFile, 0, entryName, null, true );
				if (strErrorMessage != null) {
					break;
				}
			}
			try {
				if (streamInputEntry != null) 	streamInputEntry.close();
				if (streamOutputFile != null) 	streamOutputFile.close();
			} catch (java.io.IOException ex) {
				// unchecked
			}
			if (progressMonitor != null) progressMonitor.worked(1);
		}	// while
//		strErrorMessage = "Error Injected!";
		try {
			jarFile.close();
		} catch (java.io.IOException ex) {
			// unchecked
		}
							
		// Remove safety lock
		//-------------------
		lock.remove();
	}	// if jarFile is not null

	

	if (progressMonitor != null) progressMonitor.done();

	// Increment the number of attempts
	//---------------------------------
	incrementAttemptCount();

	// Error return
	//-------------
	if (strErrorMessage != null) {
		UMSessionManagerMessage message = createMessage();
		message.setText(strErrorMessage);
		setStatus(UpdateManagerConstants.STATUS_FAILED);
		return false;
	}

	// Successful return
	//------------------
	setStatus(UpdateManagerConstants.STATUS_SUCCEEDED);

	return true;
}
/**
 *
 * 
 */
public boolean doVerify(IProgressMonitor progressMonitor) {

	String strErrorMessage = null;

	// Input URL
	//----------
	URL urlInput = null;

	try {
		urlInput = new URL(getSource());
	}
	catch (MalformedURLException ex) {
		strErrorMessage = createMessageString(UpdateManagerStrings.getString("S_Source_URL_is_malformed"), ex);
	}

	IInstallable installable = (IInstallable) getData();

	String strName = installable.getLabel();
	String strId = null;
	String strProviderName = null;

	if (installable instanceof IProductDescriptor) {
		strId = ((IProductDescriptor) installable).getUniqueIdentifier();
		strProviderName = ((IProductDescriptor) installable).getProviderName();
	}
	else if (installable instanceof IComponentDescriptor) {
		strId = ((IComponentDescriptor) installable).getUniqueIdentifier();
		strProviderName = ((IComponentDescriptor) installable).getProviderName();
	}
	else if (installable instanceof IComponentEntryDescriptor) {
		strId = ((IComponentEntryDescriptor) installable).getUniqueIdentifier();
	}
	else if (installable instanceof IPluginEntryDescriptor) {
		strId = ((IPluginEntryDescriptor) installable).getUniqueIdentifier();
	}
	else if (installable instanceof IFragmentEntryDescriptor) {
		strId = ((IFragmentEntryDescriptor) installable).getUniqueIdentifier();
	}

	JarVerificationService verifier = UpdateManager.getCurrentInstance().getJarVerifier();

	JarVerificationResult result = verifier.okToInstall(new File(urlInput.getFile()), strId, strName, strProviderName, progressMonitor);

	if (result.getResultCode() == JarVerificationResult.CANCEL_INSTALL) {
		Exception ex = result.getResultException();
		strErrorMessage = createMessageString(UpdateManagerStrings.getString("S_Installation_cancelled_by_user"), ex);
	}

	// Increment the number of attempts
	//---------------------------------
	incrementAttemptCount();

	// Error return
	//-------------
	if (strErrorMessage != null) {
		UMSessionManagerMessage message = createMessage();
		message.setText(strErrorMessage);
		setStatus(UpdateManagerConstants.STATUS_FAILED);
		return false;
	}

	// Successful return
	//------------------
	setStatus(UpdateManagerConstants.STATUS_SUCCEEDED);

	return true;
}
/**
 * Execute copy from source URL to target URL.
 */
public boolean execute(IProgressMonitor progressMonitor) {

	if (getAction() == UpdateManagerConstants.OPERATION_UNZIP_INSTALL ||
		getAction() == UpdateManagerConstants.OPERATION_UNZIP_PLUGINS ||
		getAction() == UpdateManagerConstants.OPERATION_UNZIP_BINDIR) {
		return doUnzip(progressMonitor);
	}
	
	else if (getAction() == UpdateManagerConstants.OPERATION_COPY) {
		return doCopy(progressMonitor);
	}
	else if (getAction() == UpdateManagerConstants.OPERATION_VERIFY_JAR) {
		return doVerify(progressMonitor);
	}
	return false;
}
/**
 * Execute any pending or failed updates.
 */
public boolean executeUndo(IProgressMonitor progressMonitor) {

	// Undo all successful and failed operations
	//------------------------------------------
	if (getStatus().equals(UpdateManagerConstants.STATUS_PENDING) == false) {
		if (getAction() == UpdateManagerConstants.OPERATION_UNZIP_INSTALL ||
			getAction() == UpdateManagerConstants.OPERATION_UNZIP_PLUGINS) {
			return undoUnzip(progressMonitor);
		}
		else if (getAction() == UpdateManagerConstants.OPERATION_COPY) {
			return undoCopy(progressMonitor);
		}
		else if (getAction() == UpdateManagerConstants.OPERATION_VERIFY_JAR){
			return undoVerify(progressMonitor);
		}
	}

	return true;
}
/**
 * 
 * @return java.lang.String
 */
public String getAction() {
	
	return _propertyAction.getValue();
}
/**
 * Creates a file output stream from the URL.
 * @param url java.net.URL
 */
private FileOutputStream getFileOutputStream(URL url) throws IOException {

	// Convert the URL to a string
	//----------------------------
	String strFilespec = UMEclipseTree.getFileInPlatformString(url);


	// Create directory structure
	//---------------------------
	int iIndex = strFilespec.lastIndexOf(File.separator);
	if (iIndex >= 0) {
		String strPath = strFilespec.substring(0, iIndex+1);

		File fileDirectory = new File(strPath);
		if (fileDirectory.exists() == false) {
			fileDirectory.mkdirs();
		}
	}

	// Open / create the file
	//-----------------------
	File file = new File(strFilespec);
	boolean bExists = file.exists();

	if (bExists == false) {
		bExists = file.createNewFile();
	}

	// Create the output stream
	//-------------------------
	return new FileOutputStream(file);
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
 *
 * @return java.lang.String
 */
public String getSource() {
	
	return _propertySource.getValue();
}
/**
 * @param strb java.lang.StringBuffer
 * @param iIndentation int
 */
public void getStatusString(java.lang.StringBuffer strb, int iIndentation) {

	if (getStatus().equals(UpdateManagerConstants.STATUS_SUCCEEDED) == true) {
		return;
	}
	else if (getStatus().equals(UpdateManagerConstants.STATUS_PENDING) == true) {
		return;
	}

	else {
		if (getAction().equals(UpdateManagerConstants.OPERATION_COPY) == true) {
			indent(strb, iIndentation);
			strb.append(UpdateManagerStrings.getString("S_Copy") + ": " + getSource());
		}
		else if (getAction().equals(UpdateManagerConstants.OPERATION_UNZIP_INSTALL) ||
				getAction().equals(UpdateManagerConstants.OPERATION_UNZIP_PLUGINS) == true) {
			indent(strb, iIndentation);
			strb.append(UpdateManagerStrings.getString("S_Install") + ": " + getSource());
		}

		// Obtain messages
		//----------------
		for (int i = 0; i < _alMessages.size(); ++i) {
			indent(strb, iIndentation + 2);
			strb.append(((UMSessionManagerMessage) _alMessages.get(i)).getText());
		}
	}
}
/**
 *
 * @return java.lang.String
 */
public String getTarget() {
	
	return _propertyTarget.getValue();
}
/**
 *
 * @param actionType java.lang.String
 */
public void setAction(String strAction) {
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
public void setSource( String strSource ) {
	_propertySource.setValue( strSource != null ? strSource : UpdateManagerConstants.STRING_EMPTY );
}
/**
 *
 */
public void setTarget( String strTarget ) {
	_propertyTarget.setValue( strTarget != null ? strTarget : UpdateManagerConstants.STRING_EMPTY );
}
/**
 *
 * 
 */
public boolean undoCopy(IProgressMonitor progressMonitor) {

	String strErrorMessage = null;

	// Output URL
	//-----------
	URL urlOutput = null;

	try {
		urlOutput = new URL(getTarget());
	}
	catch (MalformedURLException ex) {
		strErrorMessage = createMessageString(UpdateManagerStrings.getString("S_Target_URL_is_malformed"), ex);
	}

	if (urlOutput != null) {

		// Convert the URL to a string
		//----------------------------
		String strFilespec = UMEclipseTree.getFileInPlatformString(urlOutput);

		// Delete the file
		//----------------
		File file = new File(strFilespec);
		if (file.exists() == true) {
			if (file.delete() == false) {
				strErrorMessage = UpdateManagerStrings.getString("S_Unable_to_delete_file") + ": " + strFilespec;
			}
		}
	}

	// Reset the number of attempts
	//-----------------------------
	resetAttemptCount();

	// Error return
	//-------------
	if (strErrorMessage != null) {
		if (getStatus().equals(UpdateManagerConstants.STATUS_SUCCEEDED) == true)
			setStatus(UpdateManagerConstants.STATUS_SUCCEEDED_UNDO_FAILED);
		else
			setStatus(UpdateManagerConstants.STATUS_FAILED_UNDO_FAILED);

		return false;
	}

	// Successful return
	//------------------
	if (getStatus().equals(UpdateManagerConstants.STATUS_SUCCEEDED) == true)
		setStatus(UpdateManagerConstants.STATUS_SUCCEEDED_UNDO_SUCCEEDED);
	else
		setStatus(UpdateManagerConstants.STATUS_FAILED_UNDO_SUCCEEDED);

	return true;
}
/**
 *
 * 
 */
public boolean undoUnzip(IProgressMonitor progressMonitor) {

	String strErrorMessage = null;

	// Input URL
	//----------
	URL urlInput = null;
	try {
		urlInput = new URL(getSource());
	}
	catch (MalformedURLException ex) {
		strErrorMessage = createMessageString(UpdateManagerStrings.getString("S_Source_URL_is_malformed"), ex);
	}

	// Output URL
	//-----------
	URL urlOutput = null;
	try {
		urlOutput = new URL(getTarget());
	}
	catch (MalformedURLException ex) {
		strErrorMessage = createMessageString(UpdateManagerStrings.getString("S_Target_URL_is_malformed"), ex);
	}

	// For unzipping plugins or component/configuration jar, 
	// set up the list of directories to look for
	//-----------------------------------------------------
	Vector dirNames = new Vector();
	if (getAction() == UpdateManagerConstants.OPERATION_UNZIP_PLUGINS ) {
		IComponentDescriptor comp = (IComponentDescriptor) getData();
		IPluginEntryDescriptor[] plugins = comp.getPluginEntries();
		for (int i=0; i<plugins.length; i++) 
			dirNames.addElement( UMEclipseTree.PLUGINS_DIR + "/" + plugins[i].getDirName());
		IFragmentEntryDescriptor[] fragments = comp.getFragmentEntries();
		for (int i=0; i<fragments.length; i++) 
			dirNames.addElement( UMEclipseTree.FRAGMENTS_DIR + "/" + fragments[i].getDirName());	
	} else if (getAction() == UpdateManagerConstants.OPERATION_UNZIP_INSTALL) {
		IManifestDescriptor desc = (IManifestDescriptor) getData();
		dirNames.addElement(UMEclipseTree.INSTALL_DIR + "/" + UMEclipseTree.COMPONENTS_DIR + "/" + desc.getDirName());
	}
	
	// Create a file specification from the input URL
	//-----------------------------------------------
	String strFilespec = UMEclipseTree.getFileInPlatformString(urlInput);

	JarFile jarFile = null;
	
	try {
		jarFile = new JarFile(strFilespec);
	}
	catch (IOException ex) {
		strErrorMessage = createMessageString(UpdateManagerStrings.getString("S_Unable_to_open_Jar_file"), ex);
	}

	if (jarFile != null) {

		JarEntry entry = null;

		int iCount = jarFile.size();

		// Set up progress monitor
		// Compute the filename without the path information
		//--------------------------------------------------
		String strFilename = strFilespec;
		int iIndex = strFilespec.lastIndexOf(File.separatorChar);
		
		if (iIndex >= 0 && iIndex < strFilespec.length() - 1) {
			strFilename = strFilespec.substring(iIndex + 1);
		}
		
		if (progressMonitor != null) progressMonitor.beginTask(UpdateManagerStrings.getString("S_Undo") + ": " + strFilename, iCount);
		
		// Write out a safety lock
		//------------------------
		IUMLock lock = new UMLock();
		if (!lock.exists())
			lock.set(strFilename);		

		// Do each jar file entry
		//-----------------------
		Enumeration enum = jarFile.entries();

		while (enum.hasMoreElements() == true) {
			entry = (JarEntry) enum.nextElement();
			String entryName = entry.getName();

			if (entryName.startsWith(IManifestAttributes.MANIFEST_DIR))  {
				if (progressMonitor != null) progressMonitor.worked(1);
				continue;
			}
			
			if (getAction().equals(UpdateManagerConstants.OPERATION_UNZIP_PLUGINS)) {
				// Remove the plugins and fragments.  Skip entries not under plugins/ or fragments/ trees
				//----------------------------------------------------------------------------------------
				if ((!entryName.startsWith(UMEclipseTree.PLUGINS_DIR)) &&
					(!entryName.startsWith(UMEclipseTree.FRAGMENTS_DIR))) {
					if (progressMonitor != null) progressMonitor.worked(1);
					continue;
				}												
				if ((entryName.equals(UMEclipseTree.PLUGINS_DIR + "/")) ||
					(entryName.equals(UMEclipseTree.FRAGMENTS_DIR + "/"))) {
					if (progressMonitor != null) progressMonitor.worked(1);
					continue;
				}
			} else if (getAction().equals(UpdateManagerConstants.OPERATION_UNZIP_INSTALL)) {
				// Remove the component/product items.  Skip over entries that don't match the dirname
				//------------------------------------------------------------------------------------
				if (!entryName.startsWith((String)dirNames.firstElement()) || entryName.endsWith("/")) {
					if (progressMonitor != null) progressMonitor.worked(1);
					continue;
				}
			} else if (getAction().equals(UpdateManagerConstants.OPERATION_UNZIP_BINDIR)) {
				// No-op.   The bin directory contents cannot be undone
				//-----------------------------------------------------
				break;
			}

			if (urlOutput != null) {
				// Build pathname to actual install location
				//------------------------------------------
				strFilespec = urlOutput.getFile() + "/" + entryName;
				int j = strFilespec.indexOf(UMEclipseTree.DEVICE_SEPARATOR);
				if (j != -1) {		// we're on windoze
					strFilespec = strFilespec.replace('/', File.separatorChar).substring(1);
				} else {
					strFilespec = strFilespec.replace('/', File.separatorChar).substring(0);
				}

				// Delete the file or directory
				//-----------------------------
				File file = new File(strFilespec);
				if (file.exists() == true) {
					if (file.isDirectory())
						UpdateManager.cleanupDirectory(file);
					if (file.delete() == false) {
						strErrorMessage = UpdateManagerStrings.getString("S_Unable_to_delete_file") + ": " + strFilespec;
					}
				}
			}
		if (progressMonitor != null) progressMonitor.worked(1);		
		} // while

		try {
			jarFile.close();
		} catch (java.io.IOException ex) {
			// unchecked
		}
									
		// Remove safety lock
		//-------------------
		lock.remove();			
	} // if jarFile is not null



	if (progressMonitor != null) progressMonitor.done();
	
	// Reset the number of attempts
	//-----------------------------
	resetAttemptCount();

	// Error return
	//-------------
	if (strErrorMessage != null) {
		if (getStatus().equals(UpdateManagerConstants.STATUS_SUCCEEDED) == true)
			setStatus(UpdateManagerConstants.STATUS_SUCCEEDED_UNDO_FAILED);
		else
			setStatus(UpdateManagerConstants.STATUS_FAILED_UNDO_FAILED);

		return false;
	}

	// Successful return
	//------------------
	if (getStatus().equals(UpdateManagerConstants.STATUS_SUCCEEDED) == true)
		setStatus(UpdateManagerConstants.STATUS_SUCCEEDED_UNDO_SUCCEEDED);
	else
		setStatus(UpdateManagerConstants.STATUS_FAILED_UNDO_SUCCEEDED);

	return true;
}
/**
 *
 * 
 */
public boolean undoVerify(IProgressMonitor progressMonitor) {
	
	resetAttemptCount();
	
	if (getStatus().equals(UpdateManagerConstants.STATUS_SUCCEEDED) == true)
		setStatus(UpdateManagerConstants.STATUS_SUCCEEDED_UNDO_SUCCEEDED);
	else
		setStatus(UpdateManagerConstants.STATUS_FAILED_UNDO_SUCCEEDED);

	return true;
}
}
