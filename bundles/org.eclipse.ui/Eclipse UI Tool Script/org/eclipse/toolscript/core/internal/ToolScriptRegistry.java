package org.eclipse.toolscript.core.internal;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
**********************************************************************/
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.toolscript.ui.internal.ToolScriptMessages;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.XMLMemento;

/**
 * The registry of user defined tool scripts that can
 * be run using the tool script menu. It does not include
 * any tool scripts that are assigned as builders on a
 * project.
 */
public final class ToolScriptRegistry {
	private static final String STATE_FILE_NAME = "toolscripts.xml"; //$NON-NLS-1$
	private static final String TAG_TOOLSCRIPTS = "toolscripts"; //$NON-NLS-1$
	private static final String TAG_SCRIPT = "script"; //$NON-NLS-1$
	private static final String TAG_ENTRY = "entry"; //$NON-NLS-1$
	private static final String TAG_KEY = "key"; //$NON-NLS-1$
	private static final String TAG_VALUE = "value"; //$NON-NLS-1$
	
	private ArrayList toolScripts;

	/**
	 * Creates the registry and loads the saved
	 * tool scripts.
	 */
	/*package*/ ToolScriptRegistry() {
		super();
		loadToolScripts();
	}

	/**
	 * Returns the tool scripts of the registry
	 */
	public ArrayList getToolScripts() {
		return toolScripts;
	}
	
	/**
	 * Sets the tool scripts for the registry.
	 * Causes them to be saved to disk.
	 */
	public boolean setToolScripts(ArrayList scripts) {
		this.toolScripts = scripts;
		return saveToolScripts();
	}
	
	/**
	 * Loads the tool scripts from storage and
	 * adds them to the registry.
	 */
	private void loadToolScripts() {
		IPath path = ToolScriptPlugin.getDefault().getStateLocation();
		path = path.append(STATE_FILE_NAME);
		InputStreamReader reader = null;
		try {
			FileInputStream input = new FileInputStream(path.toFile());
			reader = new InputStreamReader(input, "utf-8"); //$NON-NLS-1$
			XMLMemento memento = XMLMemento.createReadRoot(reader);
			
			// Get the tool script children element
			IMemento[] scripts = memento.getChildren(TAG_SCRIPT);
			toolScripts = new ArrayList(scripts.length);
			for (int i = 0; i < scripts.length; i++) {
				HashMap args = new HashMap();
				IMemento[] entries = scripts[i].getChildren(TAG_ENTRY);
				for (int j = 0; j < entries.length; j++) {
					String key = entries[j].getString(TAG_KEY);
					if (key != null) {
						String value = entries[j].getTextData();
						args.put(key, value);
					}
				}
				ToolScript script = ToolScript.fromArgumentMap(args);
				if (script != null)
					toolScripts.add(script);
			}
		}
		catch (IOException e) {
			ToolScriptPlugin.getDefault().log("File I/O error with script state reader.", e); //$NON-NLS-1$
		}
		finally {
			if (reader != null) {
				try {
					reader.close();
				} catch(IOException e) {
					ToolScriptPlugin.getDefault().log("Unable to close tool script state reader.", e); //$NON-NLS-1$
				}
			}
			if (toolScripts == null)
				toolScripts = new ArrayList(0);
		}
	}
	
	/**
	 * Saves the tool scripts to storage.
	 * 
	 * @return true if save is successful, false otherwise.
	 */
	/*package*/ boolean saveToolScripts() {
		boolean successful = true;
		
		// Populate the memento
		XMLMemento memento = XMLMemento.createWriteRoot(TAG_TOOLSCRIPTS);
		Iterator enum = toolScripts.iterator();
		while (enum.hasNext()) {
			IMemento scriptMemento = memento.createChild(TAG_SCRIPT);
			ToolScript script = (ToolScript)enum.next();
			Map args = script.toArgumentMap();
			Iterator entries = args.entrySet().iterator();
			while (entries.hasNext()) {
				Map.Entry entry = (Map.Entry)entries.next();
				IMemento entryMemento = scriptMemento.createChild(TAG_ENTRY);
				entryMemento.putString(TAG_KEY, (String)entry.getKey());
				entryMemento.putTextData((String)entry.getValue());
			}
		}

		// Write the memento to the state file		
		IPath path = ToolScriptPlugin.getDefault().getStateLocation();
		path = path.append(STATE_FILE_NAME);
		File stateFile = path.toFile();
		try {
			FileOutputStream stream = new FileOutputStream(stateFile);
			OutputStreamWriter writer = new OutputStreamWriter(stream, "utf-8"); //$NON-NLS-1$
			memento.save(writer);
			writer.close();
		} catch (IOException e) {
			stateFile.delete();
			MessageDialog.openError(
				null,
				ToolScriptMessages.getString("ToolScriptRegistry.saveStateErrorTitle"), //$NON-NLS-1$
				ToolScriptMessages.getString("ToolScriptRegistry.saveStateError")); //$NON-NLS-1$
			successful = false;
		}
		
		return successful;
	}
}
