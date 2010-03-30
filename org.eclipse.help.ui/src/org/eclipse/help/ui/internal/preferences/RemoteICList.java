/*******************************************************************************
 * Copyright (c) 2008, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.ui.internal.preferences;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.eclipse.help.internal.base.remote.DefaultPreferenceFileHandler;
import org.eclipse.help.internal.base.remote.PreferenceFileHandler;
import org.eclipse.help.internal.base.remote.RemoteIC;



/**
 * This class will initialize the model of Remote Infocenters using the values
 * from the preferences.ini file
 * 
 * @author administrator
 * 
 */
public class RemoteICList {

	private Vector remote_ics = new Vector();

	private Set changeListeners = new HashSet();

	private PreferenceFileHandler prefsFileHandler;

	public RemoteICList() {

		loadPreferences();

	}

	private void loadPreferences() {

		// Load the preferences in org.eclipse.help.base/preferences.ini
		prefsFileHandler = new PreferenceFileHandler();
		int totalICs = prefsFileHandler.getTotalRemoteInfocenters();
		String host,name,path,protocol,port,enabledDisabled;
		boolean currEnabled;
		
		for (int i = 0; i < totalICs; i++) {

			host = (prefsFileHandler.getHostEntries())[i];
			name = (prefsFileHandler.getNameEntries())[i];
			path = (prefsFileHandler.getPathEntries())[i];
			port = (prefsFileHandler.getPortEntries())[i];
			protocol = (prefsFileHandler.getProtocolEntries())[i];
			enabledDisabled=(prefsFileHandler.getEnabledEntries())[i];
			if(enabledDisabled.equals("true")) //$NON-NLS-1$
			{
				currEnabled=true;
			}
			else
			{
				currEnabled=false;
			}
			
			// Add preferences to the model
			RemoteIC initRemoteIC;
			initRemoteIC = new RemoteIC(currEnabled, name, host, path, protocol, port);
			remote_ics.add(initRemoteIC);
		}
	}
	
	public void loadDefaultPreferences() {

		// Load the preferences in org.eclipse.help.base/preferences.ini
		DefaultPreferenceFileHandler handler = new DefaultPreferenceFileHandler();
		int totalICs = handler.getTotalRemoteInfocenters();
		String host,name,path,protocol,port,enabledDisabled;
		boolean currEnabled;
		
		for (int i = 0; i < totalICs; i++) {

			host = (handler.getHostEntries())[i];
			name = (handler.getNameEntries())[i];
			path = (handler.getPathEntries())[i];
			protocol = (handler.getProtocolEntries())[i];
			port = (handler.getPortEntries())[i];
			enabledDisabled=(handler.getEnabledEntries())[i];
			if(enabledDisabled.equals("true")) //$NON-NLS-1$
			{
				currEnabled=true;
			}
			else
			{
				currEnabled=false;
			}
			
			// Add preferences to the model
			RemoteIC initRemoteIC;
			initRemoteIC = new RemoteIC(currEnabled, name, host, path, protocol, port);

			addRemoteIC(initRemoteIC);
		}
	}

	/**
	 * Return the collection of remote_ic
	 */
	public Vector getRemoteICs() {
		return remote_ics;
	}

	/**
	 * @param rics the new set of remote ICs
	 */
	public void setRemoteICs(Vector rics) {
		remote_ics = rics;
	}
	
	/**
	 * Return the remote IC at the given index in the table
	 */
	public RemoteIC getRemoteICAtIndex(int index)
	{
		return (RemoteIC)remote_ics.get(index);
		
	}
	/**
	 * Add a new remote_ic to the collection of remote_ic
	 */
	public void addRemoteIC(RemoteIC remote_ic) {
		remote_ics.add(remote_ics.size(), remote_ic);
		Iterator iterator = changeListeners.iterator();
		while (iterator.hasNext())
			((IRemoteHelpListViewer) iterator.next()).addRemoteIC(remote_ic);
	}

	/**
	 * @param remote_ic
	 */
	public void removeRemoteIC(RemoteIC remote_ic) {
		remote_ics.remove(remote_ic);
		Iterator iterator = changeListeners.iterator();
		while (iterator.hasNext())
			((IRemoteHelpListViewer) iterator.next()).removeRemoteIC(remote_ic);
	}

	/**
	 * @param remote_ic
	 */
	public void updateRemoteIC(RemoteIC remote_ic) {
		Iterator iterator = changeListeners.iterator();
		while (iterator.hasNext())
			((IRemoteHelpListViewer) iterator.next()).updateRemoteIC(remote_ic);
	}
	
	public void refreshRemoteIC(RemoteIC remote_ic,int selectedIndex)
	{
		remote_ics.setElementAt(remote_ic, selectedIndex);
		Iterator iterator = changeListeners.iterator();
		while (iterator.hasNext())
			((IRemoteHelpListViewer) iterator.next()).refreshRemoteIC(remote_ic, selectedIndex);
		
		
	}
	
	public void removeAllRemoteICs(Object [] remoteICs)
	{
		remote_ics.clear();
		Iterator iterator = changeListeners.iterator();
		while (iterator.hasNext())
			((IRemoteHelpListViewer) iterator.next()).removeAllRemoteICs(remoteICs);
	}
	/**
	 * @param viewer
	 */
	public void removeChangeListener(IRemoteHelpListViewer viewer) {
		changeListeners.remove(viewer);
		
	}

	/**
	 * @param viewer
	 */
	public void addChangeListener(IRemoteHelpListViewer viewer) {
		changeListeners.add(viewer);
	}

	public PreferenceFileHandler getPrefsReader() {
		return prefsFileHandler;
	}
	
	/**
	 * Return the String [] of Remote ICs
	 * @return String [] containing the latest Remote ICs in the table
	 */
	public RemoteIC[] getRemoteICArray()
	{
		RemoteIC[] latestTableEntries;
		
		latestTableEntries=(RemoteIC[])remote_ics.toArray(new RemoteIC[remote_ics.size()]);
		
		return latestTableEntries;
		
	}
}
