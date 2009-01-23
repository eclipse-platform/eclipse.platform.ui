/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.intro.impl.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.internal.intro.impl.IntroPlugin;

/**
 * Utility class which manages the pseudo preference which determines whether 
 * Intro should always open on startup
 */

public class ReopenUtil {
	
	private static final String STATE = "state"; //$NON-NLS-1$
	private static final String REOPEN = "reopen"; //$NON-NLS-1$

	public static boolean isReopenPreferenceInitialized() {
		return readMemento() != null;
	}
	
	public static void setReopenPreference(boolean reopen) {
		XMLMemento memento = XMLMemento.createWriteRoot(STATE); 
		memento.putBoolean(REOPEN, reopen); 
		saveMemento(memento);
	}
	
	public static boolean isReopenPreference() {
		XMLMemento memento = readMemento();
		if (memento == null) {
			return false;
		}
		return memento.getBoolean(REOPEN).booleanValue(); 
	}
	
	private static XMLMemento readMemento() {
		XMLMemento memento;
		InputStreamReader reader = null;

		try {
			// Read the cheatsheet state file.
			final File stateFile = getStateFile();

			FileInputStream input = new FileInputStream(stateFile);
			reader = new InputStreamReader(input, "utf-8"); //$NON-NLS-1$
			memento = XMLMemento.createReadRoot(reader);

			
		} catch (FileNotFoundException e) {
			memento = null;
			// Do nothing, the file will not exist the first time the workbench in used.
		} catch (Exception e) {
			memento = null;
		} finally {
			try {
				if (reader != null)
					reader.close();
			} catch (IOException e) {
				// Not much to do, just catch the exception and keep going.
	        }
		}
		return memento;
	}

	private static void saveMemento(XMLMemento memento) {
		// Save the IMemento to a file.
		File stateFile = getStateFile();
		OutputStreamWriter writer = null;
		try {
			FileOutputStream stream = new FileOutputStream(stateFile);
			writer = new OutputStreamWriter(stream, "utf-8"); //$NON-NLS-1$
			memento.save(writer);
		} catch (IOException e) {
			stateFile.delete();
		} finally {
			try {
				if (writer != null)
					writer.close();
			} catch (IOException e) {
				
			}
		}
	}

	private static File getStateFile() {
		IPath path = IntroPlugin.getDefault().getStateLocation();
		path = path.append("introstate"); //$NON-NLS-1$
		File stateFile = path.toFile();
		return stateFile;
	}

}
