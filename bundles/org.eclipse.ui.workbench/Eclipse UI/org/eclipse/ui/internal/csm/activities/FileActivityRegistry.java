/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.csm.activities;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Collections;
import java.util.List;

import org.eclipse.ui.IMemento;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;

final class FileActivityRegistry extends AbstractMutableActivityRegistry {

	private final static String TAG_ROOT = Persistence.PACKAGE_FULL;

	private File file;

	FileActivityRegistry(File file) {
		if (file == null)
			throw new NullPointerException();
		
		this.file = file;
	}

	void load() 
		throws IOException {
		Reader reader = new BufferedReader(new FileReader(file));
			
		try {
			IMemento memento = XMLMemento.createReadRoot(reader);
			List activityDefinitions = Collections.unmodifiableList(Persistence.readActivityDefinitions(memento, Persistence.TAG_ACTIVITY, null));
			boolean activityRegistryChanged = false;
			
			if (!activityDefinitions.equals(this.activityDefinitions)) {
				this.activityDefinitions = activityDefinitions;			
				activityRegistryChanged = true;
			}				
				
			if (activityRegistryChanged)
				fireActivityRegistryChanged();
		} catch (WorkbenchException eWorkbench) {
			throw new IOException();
		} finally {
			reader.close();
		}
	}
	
	void save()
		throws IOException {
		XMLMemento xmlMemento = XMLMemento.createWriteRoot(TAG_ROOT);		
		Persistence.writeActivityDefinitions(xmlMemento, Persistence.TAG_ACTIVITY, activityDefinitions);
		Writer writer = new BufferedWriter(new FileWriter(file));		
		
		try {
			xmlMemento.save(writer);
		} finally {
			writer.close();
		}		
	}
}
