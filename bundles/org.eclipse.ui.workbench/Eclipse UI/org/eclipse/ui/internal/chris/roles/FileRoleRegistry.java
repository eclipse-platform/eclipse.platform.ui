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

package org.eclipse.ui.internal.chris.roles;

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

final class FileRoleRegistry extends AbstractMutableRoleRegistry {

	private final static String TAG_ROOT = Persistence.PACKAGE_FULL;

	private File file;

	FileRoleRegistry(File file) {
		if (file == null)
			throw new NullPointerException();
		
		this.file = file;
	}

	void load() 
		throws IOException {
		Reader reader = new BufferedReader(new FileReader(file));
			
		try {
			IMemento memento = XMLMemento.createReadRoot(reader);
			List roleDefinitions = Collections.unmodifiableList(Persistence.readRoleDefinitions(memento, Persistence.TAG_ROLE, null));
			boolean roleRegistryChanged = false;
			
			if (!roleDefinitions.equals(this.roleDefinitions)) {
				this.roleDefinitions = roleDefinitions;			
				roleRegistryChanged = true;
			}				
				
			if (roleRegistryChanged)
				fireRoleRegistryChanged();
		} catch (WorkbenchException eWorkbench) {
			throw new IOException();
		} finally {
			reader.close();
		}
	}
	
	void save()
		throws IOException {
		XMLMemento xmlMemento = XMLMemento.createWriteRoot(TAG_ROOT);		
		Persistence.writeRoleDefinitions(xmlMemento, Persistence.TAG_ROLE, roleDefinitions);
		Writer writer = new BufferedWriter(new FileWriter(file));		
		
		try {
			xmlMemento.save(writer);
		} finally {
			writer.close();
		}		
	}
}
