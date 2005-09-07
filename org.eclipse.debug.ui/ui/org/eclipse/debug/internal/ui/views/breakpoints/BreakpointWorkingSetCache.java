/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.debug.internal.ui.views.breakpoints;

import java.util.HashMap;
import java.util.Vector;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.importexport.breakpoints.IImportExportConstants;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;

/**
 * Class to cache the breakpoint to workingset information in its underlying
 * marker as breakpoints are moved between breakpoint working sets.
 * It resolves the need for constant attribute lookup and parsing to 
 * fix bug 103731
 * 
 * @since 3.2
 */
public class BreakpointWorkingSetCache {

	/**
	 * the entire cache
	 * key: marker
	 * value: vector of workingsets the marker belongs to
	 */
	HashMap fCache = null;
	
	/**
	 * Default constructor
	 * @param organizer the oprganizer that owns this cache
	 */
	public BreakpointWorkingSetCache() {
		fCache = new HashMap(15);
	}//end constructor
	
	/**
	 * Adds an entry into the cache
	 * @param marker the marker to add the workingset information about
	 * @param entry the entry to add to the cache
	 */
	public void addEntry(IMarker marker, Object entry) {
		Vector list = (Vector)fCache.get(marker);
		if (list == null) {
			list = new Vector();
			list.addElement(entry);
			fCache.put(marker, list);
		}//end if
		else {
			if(!list.contains(entry)) {
				list.addElement(entry);
			}//end if
		}//end else
	}//end addEntry
	
	/**
	 * Removes an item from the list contained under the marker key, not the marker entry
	 * @param marker the marker key to remove the item from
	 * @param entry the entry to remove
	 */
	public void removeMappedEntry(IMarker marker, Object entry) {
		Vector list = (Vector)fCache.get(marker);
		if(list != null) {
			list.remove(entry);
		}//end if
	}//end removeMappedEntry
	
	/**
	 * Flushes the cache of only the sepcified marker
	 * @param marker the marker whose cache is to be flushed
	 */
	public void flushMarkerCache(IMarker marker) {
		IWorkingSetManager manager = PlatformUI.getWorkbench().getWorkingSetManager();
		Vector list = (Vector)fCache.get(marker);
		if(list != null) {
			String names = IImportExportConstants.DELIMITER;
			String ids = IImportExportConstants.DELIMITER;
			for(int i = 0; i < list.size(); i++) {
				String name = (String)list.elementAt(i);
				IWorkingSet ws = manager.getWorkingSet(name);
				if(ws != null) {
					names += name+IImportExportConstants.DELIMITER;
					ids += ws.getId()+IImportExportConstants.DELIMITER;
				}//end if
			}//end for
			try {
				marker.setAttribute(IInternalDebugUIConstants.WORKING_SET_NAME, names);
				marker.setAttribute(IInternalDebugUIConstants.WORKING_SET_ID, ids);
			}//end try
			catch(CoreException e) {DebugPlugin.log(e);}
		}//end if
	}
	
}//end class
