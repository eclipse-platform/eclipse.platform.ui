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

package org.eclipse.ui.internal.contexts;

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
			contexts = Collections.unmodifiableList(Persistence.readContexts(memento, Persistence.TAG_CONTEXT, null));
		} catch (WorkbenchException eWorkbench) {
			throw new IOException();
		} finally {
			reader.close();
		}
	}
	
	public void save()
		throws IOException {
		XMLMemento xmlMemento = XMLMemento.createWriteRoot(TAG_ROOT);		
		Persistence.writeContexts(xmlMemento, Persistence.TAG_CONTEXT, contexts);
		Writer writer = new BufferedWriter(new FileWriter(file));		
		
		try {
			xmlMemento.save(writer);
		} finally {
			writer.close();
		}		
	}
}
