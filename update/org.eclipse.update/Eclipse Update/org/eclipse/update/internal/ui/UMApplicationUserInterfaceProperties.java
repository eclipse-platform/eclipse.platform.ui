package org.eclipse.update.internal.ui;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.FileInputStream;import java.io.FileOutputStream;import java.io.IOException;import java.io.ObjectInputStream;import java.io.ObjectOutputStream;import java.io.Serializable;import java.util.Vector;import org.eclipse.core.runtime.IPath;import org.eclipse.update.internal.core.UpdateManagerPlugin;

public class UMApplicationUserInterfaceProperties implements Serializable {
	Vector _vectorBookmarkURLStrings = new Vector();
	/**
	 * UpdateManagerProperties constructor comment.
	 */
	public UMApplicationUserInterfaceProperties() {

		load();
	}
	/**
	 *
	 */
	public void addBookmarkURL(String strURL) {
		_vectorBookmarkURLStrings.addElement(strURL);
	}
	/**
	 */
	public String[] getBookmarkStrings() {

		Object objArray[] = _vectorBookmarkURLStrings.toArray();

		String[] straBookmarks = new String[objArray.length];
		System.arraycopy(objArray, 0, straBookmarks, 0, objArray.length);

		return straBookmarks;
	}
	/**
	 *
	 */
	public void load() {

		UpdateManagerPlugin plugin = UpdateManagerPlugin.getPluginInstance();
		IPath pathPlugin = plugin.getStateLocation();

		pathPlugin = pathPlugin.addTrailingSeparator();
		pathPlugin = pathPlugin.append("updateManager.ser");

		try {
			FileInputStream fileInputStream = new FileInputStream(pathPlugin.toOSString());
			ObjectInputStream objInputStream = new ObjectInputStream(fileInputStream);
			Object obj = objInputStream.readObject();
			if (obj instanceof UMApplicationUserInterfaceProperties) {
				UMApplicationUserInterfaceProperties properties = (UMApplicationUserInterfaceProperties) obj;

				_vectorBookmarkURLStrings = properties._vectorBookmarkURLStrings;
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
	public void removeBookmarkURL(String strURL) {
		_vectorBookmarkURLStrings.removeElement(strURL);
	}
	/**
	 *
	 */
	public void save() {

		UpdateManagerPlugin plugin = UpdateManagerPlugin.getPluginInstance();
		IPath pathPlugin = plugin.getStateLocation();

		pathPlugin = pathPlugin.addTrailingSeparator();
		pathPlugin = pathPlugin.append("updateManager.ser");

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