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

final class PreferenceContextRegistry extends AbstractMutableContextRegistry {

	private final static String KEY = Persistence.PACKAGE_FULL;
	private final static String TAG_ROOT = Persistence.PACKAGE_FULL;

	private IPreferenceStore preferenceStore;

	PreferenceContextRegistry(IPreferenceStore preferenceStore) {
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
				List contextDefinitions = Collections.unmodifiableList(Persistence.readContextDefinitions(memento, Persistence.TAG_CONTEXT, null));
				boolean contextRegistryChanged = false;
			
				if (!contextDefinitions.equals(this.contextDefinitions)) {
					this.contextDefinitions = contextDefinitions;			
					contextRegistryChanged = true;
				}				
				
				if (contextRegistryChanged)
					fireContextRegistryChanged();				
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
		Persistence.writeContextDefinitions(xmlMemento, Persistence.TAG_CONTEXT, contextDefinitions);
		Writer writer = new StringWriter();

		try {
			xmlMemento.save(writer);
			preferenceStore.setValue(KEY, writer.toString());					
		} finally {
			writer.close();
		}
	}
}
