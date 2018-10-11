/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.templates.persistence;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;

import org.eclipse.core.runtime.Assert;

import org.eclipse.text.templates.ContextTypeRegistry;
import org.eclipse.text.templates.TemplatePersistenceData;
import org.eclipse.text.templates.TemplateReaderWriter;
import org.eclipse.text.templates.TemplateStoreCore;

import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * A collection of templates. Clients may instantiate this class. In order to
 * load templates contributed using the <code>org.eclipse.ui.editors.templates</code>
 * extension point, use a <code>ContributionTemplateStore</code>.
 *
 * @since 3.0
 */
public class TemplateStore extends TemplateStoreCore {
	/** The preference store. */
	private IPreferenceStore fPreferenceStore;

	/**
	 * The property listener, if any is registered, <code>null</code> otherwise.
	 *
	 * @since 3.2
	 */
	private IPropertyChangeListener fPropertyListener;
	/**
	 * Set to <code>true</code> if property change events should be ignored (e.g. during writing
	 * to the preference store).
	 *
	 * @since 3.2
	 */
	private boolean fIgnorePreferenceStoreChanges= false;

	/**
	 * Creates a new template store.
	 *
	 * @param store the preference store in which to store custom templates
	 *        under <code>key</code>
	 * @param key the key into <code>store</code> where to store custom
	 *        templates
	 */
	public TemplateStore(IPreferenceStore store, String key) {
		super(null, key);
		Assert.isNotNull(store);
		Assert.isNotNull(key);
		fPreferenceStore= store;
	}

	/**
	 * Creates a new template store with a context type registry. Only templates
	 * that specify a context type contained in the registry will be loaded by
	 * this store if the registry is not <code>null</code>.
	 *
	 * @param registry a context type registry, or <code>null</code> if all
	 *        templates should be loaded
	 * @param store the preference store in which to store custom templates
	 *        under <code>key</code>
	 * @param key the key into <code>store</code> where to store custom
	 *        templates
	 */
	public TemplateStore(org.eclipse.jface.text.templates.ContextTypeRegistry registry, IPreferenceStore store, String key) {
		super(registry, null, key);
		fPreferenceStore= store;
	}

	/**
	 * Loads the templates from contributions and preferences.
	 *
	 * @throws IOException if loading fails.
	 */
	@Override
	public void load() throws IOException {
		internalGetTemplates().clear();
		loadContributedTemplates();
		loadCustomTemplates();
	}

	/**
	 * Starts listening for property changes on the preference store. If the configured preference
	 * key changes, the template store is {@link #load() reloaded}. Call
	 * {@link #stopListeningForPreferenceChanges()} to remove any listener and stop the
	 * auto-updating behavior.
	 *
	 * @since 3.2
	 */
	@Override
	public final void startListeningForPreferenceChanges() {
		if (fPropertyListener == null) {
			fPropertyListener= new IPropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent event) {
					/*
					 * Don't load if we are in the process of saving ourselves. We are in sync anyway after the
					 * save operation, and clients may trigger reloading by listening to preference store
					 * updates.
					 */
					if (!fIgnorePreferenceStoreChanges && getKey().equals(event.getProperty()))
						try {
							load();
						} catch (IOException x) {
							handleException(x);
						}
				}
			};
			fPreferenceStore.addPropertyChangeListener(fPropertyListener);
		}

	}

	/**
	 * Stops the auto-updating behavior started by calling
	 * {@link #startListeningForPreferenceChanges()}.
	 *
	 * @since 3.2
	 */
	@Override
	public final void stopListeningForPreferenceChanges() {
		if (fPropertyListener != null) {
			fPreferenceStore.removePropertyChangeListener(fPropertyListener);
			fPropertyListener= null;
		}
	}

	/**
	 * Saves the templates to the preferences.
	 *
	 * @throws IOException if the templates cannot be written
	 */
	@Override
	public void save() throws IOException {
		ArrayList<TemplatePersistenceData> custom= new ArrayList<>();
		for (TemplatePersistenceData data : internalGetTemplates()) {
			if (data.isCustom() && !(data.isUserAdded() && data.isDeleted())) // don't save deleted user-added templates
				custom.add(data);
		}

		StringWriter output= new StringWriter();
		TemplateReaderWriter writer= new TemplateReaderWriter();
		writer.save(custom.toArray(new TemplatePersistenceData[custom.size()]), output);

		fIgnorePreferenceStoreChanges= true;
		try {
			fPreferenceStore.setValue(getKey(), output.toString());
			if (fPreferenceStore instanceof IPersistentPreferenceStore)
				((IPersistentPreferenceStore)fPreferenceStore).save();
		} finally {
			fIgnorePreferenceStoreChanges= false;
		}
	}

	/**
	 * Deletes all user-added templates and reverts all contributed templates.
	 *
	 * @param doSave <code>true</code> if the store should be saved after restoring
	 * @since 3.5
	 */
	@Override
	public void restoreDefaults(boolean doSave) {
		String oldValue= null;
		if (!doSave)
			oldValue= fPreferenceStore.getString(getKey());

		try {
			fIgnorePreferenceStoreChanges= true;
			fPreferenceStore.setToDefault(getKey());
		} finally {
			fIgnorePreferenceStoreChanges= false;
		}

		try {
			load();
		} catch (IOException x) {
			// can't log from jface-text
			handleException(x);
		}

		if (oldValue != null) {
			try {
				fIgnorePreferenceStoreChanges= true;
				fPreferenceStore.putValue(getKey(), oldValue);
			} finally {
				fIgnorePreferenceStoreChanges= false;
			}
		}
	}

	private void loadCustomTemplates() throws IOException {
		String pref= fPreferenceStore.getString(getKey());
		if (pref != null && pref.trim().length() > 0) {
			Reader input= new StringReader(pref);
			TemplateReaderWriter reader= new TemplateReaderWriter();
			TemplatePersistenceData[] datas= reader.read(input);
			for (TemplatePersistenceData data : datas) {
				add(data);
			}
		}
	}

	@Override
	protected final org.eclipse.jface.text.templates.ContextTypeRegistry getRegistry() {
		ContextTypeRegistry registry= super.getRegistry();
		if (registry == null) {
			return null;
		}
		org.eclipse.jface.text.templates.ContextTypeRegistry res= new org.eclipse.jface.text.templates.ContextTypeRegistry();
		registry.contextTypes().forEachRemaining(t -> res.addContextType(t));
		return res;
	}

	public void add(org.eclipse.jface.text.templates.persistence.TemplatePersistenceData data) {
		super.add(data);
	}

	public void delete(org.eclipse.jface.text.templates.persistence.TemplatePersistenceData data) {
		super.delete(data);
	}

	@Override
	public org.eclipse.jface.text.templates.persistence.TemplatePersistenceData[] getTemplateData(boolean includeDeleted) {
		TemplatePersistenceData[] list= super.getTemplateData(includeDeleted);
		org.eclipse.jface.text.templates.persistence.TemplatePersistenceData[] wraps= new org.eclipse.jface.text.templates.persistence.TemplatePersistenceData[list.length];
		for (int i= 0; i < wraps.length; i++) {
			wraps[i]= new org.eclipse.jface.text.templates.persistence.TemplatePersistenceData(list[i]);
		}
		return wraps;
	}

	@Override
	public org.eclipse.jface.text.templates.persistence.TemplatePersistenceData getTemplateData(String id) {
		TemplatePersistenceData data= super.getTemplateData(id);
		org.eclipse.jface.text.templates.persistence.TemplatePersistenceData wrap= new org.eclipse.jface.text.templates.persistence.TemplatePersistenceData(data);
		return wrap;
	}

	protected void internalAdd(org.eclipse.jface.text.templates.persistence.TemplatePersistenceData data) {
		super.internalAdd(data);
	}

}

