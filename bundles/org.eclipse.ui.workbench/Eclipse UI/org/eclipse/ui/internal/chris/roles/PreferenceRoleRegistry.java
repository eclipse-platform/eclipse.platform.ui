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

final class PreferenceRoleRegistry extends AbstractMutableRoleRegistry {

	private final static String KEY = Persistence.PACKAGE_FULL;
	private final static String TAG_ROOT = Persistence.PACKAGE_FULL;

	private IPreferenceStore preferenceStore;

	PreferenceRoleRegistry(IPreferenceStore preferenceStore) {
		if (preferenceStore == null)
			throw new NullPointerException();
		
		this.preferenceStore = preferenceStore;
	}

	void load() 
		throws IOException {
		String preferenceString = preferenceStore.getString(KEY);
		
		if (preferenceString != null && preferenceString.length() != 0) {
			Reader reader = new StringReader(preferenceString);
			
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
	}
	
	void save()
		throws IOException {
		XMLMemento xmlMemento = XMLMemento.createWriteRoot(TAG_ROOT);		
		Persistence.writeRoleDefinitions(xmlMemento, Persistence.TAG_ROLE, roleDefinitions);
		Writer writer = new StringWriter();

		try {
			xmlMemento.save(writer);
			preferenceStore.setValue(KEY, writer.toString());					
		} finally {
			writer.close();
		}
	}
}
