package org.eclipse.update.internal.ui;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * Persistent properties stored in plugin directory.
 */
import java.io.FileInputStream;import java.io.FileOutputStream;import java.io.IOException;import java.io.ObjectInputStream;import java.io.ObjectOutputStream;import java.io.Serializable;import java.util.Vector;import org.eclipse.core.internal.boot.update.URLNamePair;import org.eclipse.core.runtime.IPath;import org.eclipse.update.internal.core.UpdateManagerPlugin;

public class UMWizardPersistentProperties implements Serializable {
	Vector _vectorUpdateBookmarks = new Vector();
	Vector _vectorDiscoveryBookmarks = new Vector();

	protected static final String _strFilename = "umwizard.ser";
	/**
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
	 */
	public URLNamePair[] getDiscoveryBookmarks() {

		Object objArray[] = _vectorDiscoveryBookmarks.toArray();

		URLNamePair[] pairs = new URLNamePair[objArray.length];
		System.arraycopy(objArray, 0, pairs, 0, objArray.length);

		return pairs;
	}
	/**
	 */
	public URLNamePair[] getUpdateBookmarks() {

		Object objArray[] = _vectorUpdateBookmarks.toArray();

		URLNamePair[] pairs = new URLNamePair[objArray.length];
		System.arraycopy(objArray, 0, pairs, 0, objArray.length);

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

		try {
			FileInputStream fileInputStream = new FileInputStream(pathPlugin.toOSString());
			ObjectInputStream objInputStream = new ObjectInputStream(fileInputStream);
			Object obj = objInputStream.readObject();
			if (obj instanceof UMWizardPersistentProperties) {
				UMWizardPersistentProperties properties = (UMWizardPersistentProperties) obj;

				_vectorDiscoveryBookmarks = properties._vectorDiscoveryBookmarks;
				_vectorUpdateBookmarks = properties._vectorUpdateBookmarks;
			}
		}
		catch (IOException ex) {
		}
		catch (ClassNotFoundException ex) {
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
			pairUpdate = (URLNamePair) _vectorUpdateBookmarks.elementAt(i);
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

		try {
			FileOutputStream outputStream = new FileOutputStream(pathPlugin.toOSString());
			ObjectOutputStream objOutputStream = new ObjectOutputStream(outputStream);
			objOutputStream.writeObject(this);
			objOutputStream.close();
		}
		catch (IOException ex) {
		}
	}
}