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

package org.eclipse.ui.internal.commands;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Collections;

import org.eclipse.ui.IMemento;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;

final class FileRegistry extends AbstractMutableRegistry {

	private final static String TAG_ROOT = Persistence.PACKAGE_FULL;

	private File file;

	private FileRegistry(File file) {
		super();
		
		if (file == null)
			throw new NullPointerException();
		
		this.file = file;
	}

	public void load() 
		throws IOException {
		Reader reader = new BufferedReader(new FileReader(file));
			
		try {
			IMemento memento = XMLMemento.createReadRoot(reader);
			activeKeyConfigurations = Collections.unmodifiableList(Persistence.readActiveKeyConfigurations(memento, Persistence.TAG_ACTIVE_KEY_CONFIGURATION, null));
			categories = Collections.unmodifiableList(Persistence.readCategories(memento, Persistence.TAG_CATEGORY, null));
			commands = Collections.unmodifiableList(Persistence.readCommands(memento, Persistence.TAG_COMMAND, null));
			contextBindings = Collections.unmodifiableList(Persistence.readContextBindings(memento, Persistence.TAG_CONTEXT_BINDING, null));
			imageBindings = Collections.unmodifiableList(Persistence.readImageBindings(memento, Persistence.TAG_IMAGE_BINDING, null));
			keyBindings = Collections.unmodifiableList(Persistence.readKeyBindings(memento, Persistence.TAG_KEY_BINDING, null));
			keyConfigurations = Collections.unmodifiableList(Persistence.readKeyConfigurations(memento, Persistence.TAG_KEY_CONFIGURATION, null));
		} catch (WorkbenchException eWorkbench) {
			throw new IOException();
		} finally {
			reader.close();
		}
	}
	
	public void save()
		throws IOException {
		XMLMemento xmlMemento = XMLMemento.createWriteRoot(TAG_ROOT);		
		Persistence.writeActiveKeyConfigurations(xmlMemento, Persistence.TAG_ACTIVE_KEY_CONFIGURATION, activeKeyConfigurations);		
		Persistence.writeCategories(xmlMemento, Persistence.TAG_CATEGORY, categories);		
		Persistence.writeCommands(xmlMemento, Persistence.TAG_COMMAND, commands);
		Persistence.writeContextBindings(xmlMemento, Persistence.TAG_CONTEXT_BINDING, contextBindings);
		Persistence.writeImageBindings(xmlMemento, Persistence.TAG_IMAGE_BINDING, imageBindings);
		Persistence.writeKeyBindings(xmlMemento, Persistence.TAG_KEY_BINDING, keyBindings);
		Persistence.writeKeyConfigurations(xmlMemento, Persistence.TAG_KEY_CONFIGURATION, keyConfigurations);
		Writer writer = new BufferedWriter(new FileWriter(file));		
		
		try {
			xmlMemento.save(writer);
		} finally {
			writer.close();
		}		
	}
}
