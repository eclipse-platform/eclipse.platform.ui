package org.eclipse.update.internal.ui;

/**
 * Persistent properties stored in plugin directory.
 */
import java.util.*;
import org.eclipse.update.internal.core.*;
import org.eclipse.core.internal.boot.update.*;
import java.io.*;
import org.eclipse.core.runtime.*;

public class UMWizardPersistentProperties implements Serializable {
	Vector  _vectorUpdateBookmarks    = new Vector();
	Vector  _vectorDiscoveryBookmarks = new Vector();

	protected static final String _strFilename = "umwizard.ser";
/**
 * UpdateManagerProperties constructor comment.
 */
public UMWizardPersistentProperties() {

	load();
}
/**
 *
 */
public void addDiscoveryBookmark(URLNamePair pair) {
	_vectorDiscoveryBookmarks.addElement(pair);
	save();
}
/**
 *
 */
public void addUpdateBookmark(URLNamePair pair) {
	_vectorUpdateBookmarks.addElement(pair);
	save();
}
/**
 * Insert the method's description here.
 * Creation date: (2001/03/09 10:56:53)
 * @return java.lang.String[]
 */
public URLNamePair[] getDiscoveryBookmarks() {

	Object objArray[] = _vectorDiscoveryBookmarks.toArray();

	URLNamePair[] pairs = new URLNamePair[ objArray.length ];
	System.arraycopy( objArray, 0, pairs, 0, objArray.length );
	
	return pairs;
}
/**
 * Insert the method's description here.
 * Creation date: (2001/03/09 10:56:53)
 * @return java.lang.String[]
 */
public URLNamePair[] getUpdateBookmarks() {

	Object objArray[] = _vectorUpdateBookmarks.toArray();

	URLNamePair[] pairs = new URLNamePair[ objArray.length ];
	System.arraycopy( objArray, 0, pairs, 0, objArray.length );
	
	return pairs;
}
/**
 *
 */
public void load() {

	UpdateManagerPlugin plugin = UpdateManagerPlugin.getPluginInstance();
	IPath pathPlugin = plugin.getStateLocation();

	pathPlugin = pathPlugin.addTrailingSeparator();
	pathPlugin = pathPlugin.append(_strFilename);
	
	try{
		FileInputStream fileInputStream = new FileInputStream( pathPlugin.toOSString() );
		ObjectInputStream objInputStream = new ObjectInputStream( fileInputStream );
		Object obj = objInputStream.readObject();
		if( obj instanceof UMWizardPersistentProperties )
		{
			UMWizardPersistentProperties properties = (UMWizardPersistentProperties)obj;

			_vectorDiscoveryBookmarks = properties._vectorDiscoveryBookmarks;
			_vectorUpdateBookmarks = properties._vectorUpdateBookmarks;
		}
	}
	catch( IOException ex )
	{
	}
	catch( ClassNotFoundException ex )
	{
	}
}
/**
 *
 */
public void removeDiscoveryBookmark(URLNamePair pair) {

	// Remove all entries with the same url and name
	//----------------------------------------------
	URLNamePair pairDiscovery = null;
	
	for (int i = 0; i < _vectorDiscoveryBookmarks.size(); ++i) {
		pairDiscovery = (URLNamePair) _vectorDiscoveryBookmarks.elementAt(i);
		if (pairDiscovery._getName().equals(pair._getName()) && pairDiscovery._getURL().equals(pair._getURL())) {
			_vectorDiscoveryBookmarks.remove(pairDiscovery);
		}
	}
	save();
}
/**
 *
 */
public void removeUpdateBookmark(URLNamePair pair) {
	
	// Remove all entries with the same url and name
	//----------------------------------------------
	URLNamePair pairUpdate = null;
	
	for (int i = 0; i < _vectorUpdateBookmarks.size(); ++i) {
		pair = (URLNamePair) _vectorUpdateBookmarks.elementAt(i);
		if (pairUpdate._getName().equals(pair._getName()) && pairUpdate._getURL().equals(pair._getURL())) {
			_vectorUpdateBookmarks.remove(pairUpdate);
		}
	}

	save();
}
/**
 *
 */
public void save() {
	
	UpdateManagerPlugin plugin = UpdateManagerPlugin.getPluginInstance();
	IPath pathPlugin = plugin.getStateLocation();

	pathPlugin = pathPlugin.addTrailingSeparator();
	pathPlugin = pathPlugin.append(_strFilename);
	
	try{
		FileOutputStream outputStream = new FileOutputStream( pathPlugin.toOSString() );
		ObjectOutputStream objOutputStream = new ObjectOutputStream( outputStream );
		objOutputStream.writeObject( this );
		objOutputStream.close();
	}
	catch( IOException ex )
	{
	}
}
}
