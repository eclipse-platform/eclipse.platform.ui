/************************************************************************
Copyright (c) 2003 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/

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

import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.internal.WorkbenchPlugin;

public final class LocalRegistry extends AbstractMutableRegistry {

	private final static String PATH = Persistence.PACKAGE_FULL + ".xml"; //$NON-NLS-1$
	private final static int RANK_LOCAL = 1;
	private final static String TAG_ROOT = Persistence.PACKAGE_FULL;
	
	public static LocalRegistry instance;
	
	public static LocalRegistry getInstance() {
		if (instance == null)
			instance = new LocalRegistry();
	
		return instance;
	}

	private LocalRegistry() {
		super();
	}

	public void load()
		throws IOException {
		IPath path = WorkbenchPlugin.getDefault().getStateLocation();
		path = path.append(PATH);
		File file = path.toFile();		
		Reader reader = new BufferedReader(new FileReader(file));

		try {
			IMemento memento = XMLMemento.createReadRoot(reader);
			activeGestureConfigurations = Collections.unmodifiableList(Persistence.readActiveConfigurations(memento, Persistence.TAG_ACTIVE_GESTURE_CONFIGURATION, null));
			activeKeyConfigurations = Collections.unmodifiableList(Persistence.readActiveConfigurations(memento, Persistence.TAG_ACTIVE_KEY_CONFIGURATION, null));
			categories = Collections.unmodifiableList(Persistence.readCategories(memento, Persistence.TAG_CATEGORY, null));
			commands = Collections.unmodifiableList(Persistence.readCommands(memento, Persistence.TAG_COMMAND, null));
			gestureBindings = Collections.unmodifiableList(Persistence.readBindings(memento, Persistence.TAG_GESTURE_BINDING, null, RANK_LOCAL));
			gestureConfigurations = Collections.unmodifiableList(Persistence.readConfigurations(memento, Persistence.TAG_GESTURE_CONFIGURATION, null));
			keyBindings = Collections.unmodifiableList(Persistence.readBindings(memento, Persistence.TAG_KEY_BINDING, null, RANK_LOCAL));
			keyConfigurations = Collections.unmodifiableList(Persistence.readConfigurations(memento, Persistence.TAG_KEY_CONFIGURATION, null));
			scopes = Collections.unmodifiableList(Persistence.readScopes(memento, Persistence.TAG_SCOPE, null));
		} catch (WorkbenchException eWorkbench) {
			throw new IOException();
		} finally {
			reader.close();
		}
	}
	
	public void save()
		throws IOException {
		XMLMemento xmlMemento = XMLMemento.createWriteRoot(TAG_ROOT);		
		Persistence.writeActiveConfigurations(xmlMemento, Persistence.TAG_ACTIVE_GESTURE_CONFIGURATION, activeGestureConfigurations);		
		Persistence.writeActiveConfigurations(xmlMemento, Persistence.TAG_ACTIVE_KEY_CONFIGURATION, activeKeyConfigurations);		
		Persistence.writeCategories(xmlMemento, Persistence.TAG_CATEGORY, categories);		
		Persistence.writeCommands(xmlMemento, Persistence.TAG_COMMAND, commands);
		Persistence.writeBindings(xmlMemento, Persistence.TAG_GESTURE_BINDING, gestureBindings);
		Persistence.writeConfigurations(xmlMemento, Persistence.TAG_GESTURE_CONFIGURATION, gestureConfigurations);
		Persistence.writeBindings(xmlMemento, Persistence.TAG_KEY_BINDING, keyBindings);
		Persistence.writeConfigurations(xmlMemento, Persistence.TAG_KEY_CONFIGURATION, keyConfigurations);
		Persistence.writeScopes(xmlMemento, Persistence.TAG_SCOPE, scopes);
		IPath path = WorkbenchPlugin.getDefault().getStateLocation();
		path = path.append(PATH);
		File file = path.toFile();		
		Writer writer = new BufferedWriter(new FileWriter(file));		
		
		try {
			xmlMemento.save(writer);
		} finally {
			writer.close();
		}
	}
}
