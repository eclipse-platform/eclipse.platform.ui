/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
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
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;

/**
 * The persistent store for the preferences related to the commands
 * infrastructure. This store handles loading and storing of preferences.
 * 
 * @since 3.0
 */
public final class PreferenceCommandRegistry extends
        AbstractMutableCommandRegistry {

    /**
     * The full package name for the store.
     */
    private final static String KEY = Persistence.PACKAGE_FULL;

    /**
     * The underlying preference store. This value will never be
     * <code>null</code>.
     */
    private IPreferenceStore preferenceStore;

    /**
     * Constructs a new instance of <code>PreferenceCommandRegistry</code>
     * with the preference store it is supposed to use.
     * 
     * @param preferenceStore
     *            The preference store to use; must not be <code>null</code>.
     */
    public PreferenceCommandRegistry(IPreferenceStore preferenceStore) {
        if (preferenceStore == null)
            throw new NullPointerException();

        this.preferenceStore = preferenceStore;

        this.preferenceStore
                .addPropertyChangeListener(new IPropertyChangeListener() {

                    public void propertyChange(
                            PropertyChangeEvent propertyChangeEvent) {
                        if (KEY.equals(propertyChangeEvent.getProperty()))
                            try {
                                load();
                            } catch (final IOException e) {
                                e.printStackTrace();
                            }
                    }
                });

        try {
            load();
        } catch (IOException eIO) {
            // At least we tried....
        }
    }

    /**
     * Loads all of the preferences from the store, and sets member variables
     * containing all of the values.
     * 
     * @throws IOException
     *             If something happens while trying to read the store.
     */
    public void load() throws IOException {
        String preferenceString = preferenceStore.getString(KEY);

        if (preferenceString != null && preferenceString.length() != 0) {
            Reader reader = new StringReader(preferenceString);

            try {
                IMemento memento = XMLMemento.createReadRoot(reader);
                List activeKeyConfigurationDefinitions = Collections
                        .unmodifiableList(Persistence
                                .readActiveKeyConfigurationDefinitions(
                                        memento,
                                        Persistence.TAG_ACTIVE_KEY_CONFIGURATION,
                                        null));
                List categoryDefinitions = Collections
                        .unmodifiableList(Persistence.readCategoryDefinitions(
                                memento, Persistence.TAG_CATEGORY, null));
                List commandDefinitions = Collections
                        .unmodifiableList(Persistence.readCommandDefinitions(
                                memento, Persistence.TAG_COMMAND, null));
                List keyConfigurationDefinitions = Collections
                        .unmodifiableList(Persistence
                                .readKeyConfigurationDefinitions(memento,
                                        Persistence.TAG_KEY_CONFIGURATION, null));
                List keySequenceBindingDefinitions = Collections
                        .unmodifiableList(Persistence
                                .readKeySequenceBindingDefinitions(memento,
                                        Persistence.TAG_KEY_SEQUENCE_BINDING,
                                        null));
                boolean commandRegistryChanged = false;

                if (!activeKeyConfigurationDefinitions
                        .equals(this.activeKeyConfigurationDefinitions)) {
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

                if (!keyConfigurationDefinitions
                        .equals(this.keyConfigurationDefinitions)) {
                    this.keyConfigurationDefinitions = keyConfigurationDefinitions;
                    commandRegistryChanged = true;
                }

                if (!keySequenceBindingDefinitions
                        .equals(this.keySequenceBindingDefinitions)) {
                    this.keySequenceBindingDefinitions = keySequenceBindingDefinitions;
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

    /**
     * Saves all of the preferences to the preference store.
     * 
     * @throws IOException
     *             If something happens while trying to write to the preference
     *             store.
     */
    public void save() throws IOException {
        XMLMemento xmlMemento = XMLMemento.createWriteRoot(KEY);
        Persistence.writeActiveKeyConfigurationDefinitions(xmlMemento,
                Persistence.TAG_ACTIVE_KEY_CONFIGURATION,
                activeKeyConfigurationDefinitions);
        Persistence.writeCategoryDefinitions(xmlMemento,
                Persistence.TAG_CATEGORY, categoryDefinitions);
        Persistence.writeCommandDefinitions(xmlMemento,
                Persistence.TAG_COMMAND, commandDefinitions);
        Persistence.writeKeyConfigurationDefinitions(xmlMemento,
                Persistence.TAG_KEY_CONFIGURATION, keyConfigurationDefinitions);
        Persistence.writeKeySequenceBindingDefinitions(xmlMemento,
                Persistence.TAG_KEY_SEQUENCE_BINDING,
                keySequenceBindingDefinitions);
        Writer writer = new StringWriter();

        try {
            xmlMemento.save(writer);
            preferenceStore.setValue(KEY, writer.toString());
        } finally {
            writer.close();
        }
    }
}