package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 */
import org.eclipse.core.internal.boot.update.*;
import java.io.*;
import java.net.*;
public class UMLock implements IUMLock {
	File file = null;
/**
 * UMLock constructor comment.
 */
public UMLock() {
	super();
	URL lockfileURL = null;
	try {
		lockfileURL = new URL(UMEclipseTree.getInstallTreeURL(), IManifestAttributes.UM_LOCK);
	} catch (MalformedURLException ex) {
	}
	file = getFile(lockfileURL);
}
/**
 * Checks to see if the UMLock file is there.  If yes, returns true
 */
public boolean exists() {
	
	if (file != null) {
		if (file.exists() == true) {
			return true;
		}
	}
	return false;
}
/**
 */
public static File getFile(URL url) {
	File file = null;
	if (url != null) {

		// Convert the URL to a string
		//----------------------------
		String strFilespec = UMEclipseTree.getFileInPlatformString(url);

		file = new File(strFilespec);
	}
	return file;
}
/**
 */
public void remove() {
	if (exists())
		file.delete();
		
}
/**
 */
public void set() {
	set("");
}
/**
 */
public void set(String msg) {
	URL lockfileURL = null;
	try {
		lockfileURL = new URL(UMEclipseTree.getInstallTreeURL(), IManifestAttributes.UM_LOCK);
	} catch (MalformedURLException ex) {
	}
	
	// Create a lock file
	//-------------------
	Log lockfile = new Log();

	try {
		lockfile.load(lockfileURL);		// creates file if it doesn't exist
	}
	catch (LogStoreException ex) {
		return;
	}

	// Add an entry
	//-------------
	LogEntry lockEntry = new LogEntry( lockfile, msg );
	lockfile.addChildEntry( lockEntry );


	try {
		lockfile.save(lockfileURL);
	}
	catch (LogStoreException ex) {
		return;
	}
}
}
