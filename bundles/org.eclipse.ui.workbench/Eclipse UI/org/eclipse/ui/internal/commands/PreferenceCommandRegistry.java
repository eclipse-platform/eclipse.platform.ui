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

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;

final class PreferenceCommandRegistry extends AbstractMutableCommandRegistry {

	private final static String KEY = Persistence.PACKAGE_FULL;
	private final static String TAG_ROOT = Persistence.PACKAGE_FULL;

	private IPreferenceStore preferenceStore;

	PreferenceCommandRegistry(IPreferenceStore preferenceStore) {
		super();
		
		if (preferenceStore == null)
			throw new NullPointerException();
		
		this.preferenceStore = preferenceStore;
	}

	public void load() 
		throws IOException {
		String preferenceString = preferenceStore.getString(KEY);
		
		if (preferenceString != null && preferenceString.length() != 0) {
			Reader reader = new StringReader(preferenceString);
			
			try {
				IMemento memento = XMLMemento.createReadRoot(reader);
				List activeKeyConfigurationDefinitions = Collections.unmodifiableList(Persistence.readActiveKeyConfigurationDefinitions(memento, Persistence.TAG_ACTIVE_KEY_CONFIGURATION, null));
				List categoryDefinitions = Collections.unmodifiableList(Persistence.readCategoryDefinitions(memento, Persistence.TAG_CATEGORY, null));
				List commandDefinitions = Collections.unmodifiableList(Persistence.readCommandDefinitions(memento, Persistence.TAG_COMMAND, null));
				List contextBindingDefinitions = Collections.unmodifiableList(Persistence.readContextBindingDefinitions(memento, Persistence.TAG_CONTEXT_BINDING, null));
				List imageBindingDefinitions = Collections.unmodifiableList(Persistence.readImageBindingDefinitions(memento, Persistence.TAG_IMAGE_BINDING, null));
				List keyBindingDefinitions = Collections.unmodifiableList(Persistence.readKeyBindingDefinitions(memento, Persistence.TAG_KEY_BINDING, null));
				List keyConfigurationDefinitions = Collections.unmodifiableList(Persistence.readKeyConfigurationDefinitions(memento, Persistence.TAG_KEY_CONFIGURATION, null));			
				boolean commandRegistryChanged = false;
			
				if (!activeKeyConfigurationDefinitions.equals(this.activeKeyConfigurationDefinitions)) {
					this.activeKeyConfigurationDefinitions = activeKeyConfigurationDefinitions;			
					commandRegistryChanged = true;
				}	

				if (!categoryDefinitions.equals(this.categoryDefinitions)) {
					this.categoryDefinitions = categoryDefinitions;			
					commandRegistryChanged = true;
				}	
				
				if (!commandDefinitions.equals(this.commandDefinitions)) {
					this.commandDefinitions = commandDefinitions;			
					commandRegistryChanged = true;
				}	
				
				if (!contextBindingDefinitions.equals(this.contextBindingDefinitions)) {
					this.contextBindingDefinitions = contextBindingDefinitions;			
					commandRegistryChanged = true;
				}	
			
				if (!imageBindingDefinitions.equals(this.imageBindingDefinitions)) {
					this.imageBindingDefinitions = imageBindingDefinitions;			
					commandRegistryChanged = true;
				}	
				
				if (!keyBindingDefinitions.equals(this.keyBindingDefinitions)) {
					this.keyBindingDefinitions = keyBindingDefinitions;			
					commandRegistryChanged = true;
				}	
				
				if (!keyConfigurationDefinitions.equals(this.keyConfigurationDefinitions)) {
					this.keyConfigurationDefinitions = keyConfigurationDefinitions;			
					commandRegistryChanged = true;
				}	
				
				if (commandRegistryChanged)
					fireCommandRegistryChanged();
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
		Persistence.writeActiveKeyConfigurationDefinitions(xmlMemento, Persistence.TAG_ACTIVE_KEY_CONFIGURATION, activeKeyConfigurationDefinitions);		
		Persistence.writeCategoryDefinitions(xmlMemento, Persistence.TAG_CATEGORY, categoryDefinitions);		
		Persistence.writeCommandDefinitions(xmlMemento, Persistence.TAG_COMMAND, commandDefinitions);
		Persistence.writeContextBindingDefinitions(xmlMemento, Persistence.TAG_CONTEXT_BINDING, contextBindingDefinitions);
		Persistence.writeImageBindingDefinitions(xmlMemento, Persistence.TAG_IMAGE_BINDING, imageBindingDefinitions);
		Persistence.writeKeyBindingDefinitions(xmlMemento, Persistence.TAG_KEY_BINDING, keyBindingDefinitions);
		Persistence.writeKeyConfigurationDefinitions(xmlMemento, Persistence.TAG_KEY_CONFIGURATION, keyConfigurationDefinitions);
		Writer writer = new StringWriter();

		try {
			xmlMemento.save(writer);
			preferenceStore.setValue(KEY, writer.toString());					
		} finally {
			writer.close();
		}
	}
}
