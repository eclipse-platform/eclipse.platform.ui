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

package org.eclipse.ui.internal.commands.old;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.internal.WorkbenchPlugin;

public final class PreferenceRegistry extends AbstractMutableRegistry {

	private final static String KEY = Persistence.PACKAGE_FULL;
	private final static int RANK_PREFERENCE = 0;
	private final static String TAG_ROOT = Persistence.PACKAGE_FULL;

	public static PreferenceRegistry instance;
	
	public static PreferenceRegistry getInstance() {
		if (instance == null)
			instance = new PreferenceRegistry();
	
		return instance;
	}

	private PreferenceRegistry() {
		super();
	}

	public void load() 
		throws IOException {
		IPreferenceStore preferenceStore = WorkbenchPlugin.getDefault().getPreferenceStore();	
		String preferenceString = preferenceStore.getString(KEY);
		
		if (preferenceString != null && preferenceString.length() != 0) {
			Reader reader = new StringReader(preferenceString);
			
			try {
				IMemento memento = XMLMemento.createReadRoot(reader);
				activeKeyConfigurations = Collections.unmodifiableList(Persistence.readActiveConfigurations(memento, Persistence.TAG_ACTIVE_KEY_CONFIGURATION, null));
				categories = Collections.unmodifiableList(Persistence.readCategories(memento, Persistence.TAG_CATEGORY, null));
				commands = Collections.unmodifiableList(Persistence.readCommands(memento, Persistence.TAG_COMMAND, null));
				contextBindings = Collections.unmodifiableList(Persistence.readContextBindings(memento, Persistence.TAG_CONTEXT_BINDING, null));
				contexts = Collections.unmodifiableList(Persistence.readContexts(memento, Persistence.TAG_CONTEXT, null));			
				keyBindings = Collections.unmodifiableList(Persistence.readSequenceBindings(memento, Persistence.TAG_KEY_BINDING, null, RANK_PREFERENCE));
				keyConfigurations = Collections.unmodifiableList(Persistence.readConfigurations(memento, Persistence.TAG_KEY_CONFIGURATION, null));
			} catch (WorkbenchException eWorkbench) {
				throw new IOException();
			} finally {
				reader.close();
			}
		}
	}
	
	public void save()
		throws IOException {
		XMLMemento xmlMemento = XMLMemento.createWriteRoot(TAG_ROOT);		

		Persistence.writeActiveConfigurations(xmlMemento, Persistence.TAG_ACTIVE_KEY_CONFIGURATION, activeKeyConfigurations);		
		Persistence.writeCategories(xmlMemento, Persistence.TAG_CATEGORY, categories);		
		Persistence.writeCommands(xmlMemento, Persistence.TAG_COMMAND, commands);
		Persistence.writeContextBindings(xmlMemento, Persistence.TAG_CONTEXT_BINDING, contextBindings);
		Persistence.writeContexts(xmlMemento, Persistence.TAG_CONTEXT, contexts);
		Persistence.writeSequenceBindings(xmlMemento, Persistence.TAG_KEY_BINDING, keyBindings);
		Persistence.writeConfigurations(xmlMemento, Persistence.TAG_KEY_CONFIGURATION, keyConfigurations);
		Writer writer = new StringWriter();

		try {
			xmlMemento.save(writer);
			IPreferenceStore preferenceStore = WorkbenchPlugin.getDefault().getPreferenceStore();
			preferenceStore.setValue(KEY, writer.toString());					
		} finally {
			writer.close();
		}
	}
}
