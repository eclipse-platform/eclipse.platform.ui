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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionDelta;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.runtime.IRegistryChangeListener;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.commands.IHandler;
import org.eclipse.ui.internal.util.ConfigurationElementMemento;

public final class ExtensionCommandRegistry extends AbstractCommandRegistry {

	private List activeKeyConfigurationDefinitions;
	private List categoryDefinitions;
	private List commandDefinitions;
	private IExtensionRegistry extensionRegistry;
	/**
	 * The valid handlers read from XML.  This list is <code>null</code> until
	 * the first call to <code>load</code>.  After this, it will contain a list
	 * of all the handlers read during the most recent call to 
	 * <code>load</code>.
	 */
	private List handlers;
	private List keyConfigurationDefinitions;
	private List keySequenceBindingDefinitions;

	public ExtensionCommandRegistry(IExtensionRegistry extensionRegistry) {
		if (extensionRegistry == null)
			throw new NullPointerException();

		this.extensionRegistry = extensionRegistry;

		this
			.extensionRegistry
			.addRegistryChangeListener(new IRegistryChangeListener() {
			public void registryChanged(IRegistryChangeEvent registryChangeEvent) {
				IExtensionDelta[] extensionDeltas =
					registryChangeEvent.getExtensionDeltas(
						Persistence.PACKAGE_PREFIX,
						Persistence.PACKAGE_BASE);

				if (extensionDeltas.length != 0)
					try {
						load();
					} catch (IOException eIO) {
					    // Do nothing
					}
			}
		});

		try {
			load();
		} catch (IOException eIO) {
		    // Do nothing
		}
	}

	private String getNamespace(IConfigurationElement configurationElement) {
		String namespace = null;

		if (configurationElement != null) {
			IExtension extension = configurationElement.getDeclaringExtension();

			if (extension != null)
				namespace = extension.getNamespace();
		}

		return namespace;
	}

	private void load() throws IOException {
		if (activeKeyConfigurationDefinitions == null)
			activeKeyConfigurationDefinitions = new ArrayList();
		else
			activeKeyConfigurationDefinitions.clear();

		if (categoryDefinitions == null)
			categoryDefinitions = new ArrayList();
		else
			categoryDefinitions.clear();

		if (commandDefinitions == null)
			commandDefinitions = new ArrayList();
		else
			commandDefinitions.clear();
		
		if (handlers == null) {
		    handlers = new ArrayList();
		} else {
		    handlers.clear();
		}

		if (imageBindingDefinitions == null)
			imageBindingDefinitions = new ArrayList();
		else
			imageBindingDefinitions.clear();

		if (keyConfigurationDefinitions == null)
			keyConfigurationDefinitions = new ArrayList();
		else
			keyConfigurationDefinitions.clear();

		if (keySequenceBindingDefinitions == null)
			keySequenceBindingDefinitions = new ArrayList();
		else
			keySequenceBindingDefinitions.clear();

		// TODO deprecated start
		IConfigurationElement[] deprecatedConfigurationElements = extensionRegistry.getConfigurationElementsFor("org.eclipse.ui.acceleratorConfigurations"); //$NON-NLS-1$

		for (int i = 0; i < deprecatedConfigurationElements.length; i++) {
			IConfigurationElement deprecatedConfigurationElement =
				deprecatedConfigurationElements[i];
			String name = deprecatedConfigurationElement.getName();

			if ("acceleratorConfiguration".equals(name)) //$NON-NLS-1$
				readKeyConfigurationDefinition(deprecatedConfigurationElement);
		}

		deprecatedConfigurationElements = extensionRegistry.getConfigurationElementsFor("org.eclipse.ui.acceleratorSets"); //$NON-NLS-1$

		for (int i = 0; i < deprecatedConfigurationElements.length; i++) {
			IConfigurationElement deprecatedConfigurationElement =
				deprecatedConfigurationElements[i];
			String name = deprecatedConfigurationElement.getName();

			if ("acceleratorSet".equals(name)) { //$NON-NLS-1$
				IMemento memento =
					new ConfigurationElementMemento(deprecatedConfigurationElement);
				String keyConfigurationId = memento.getString("configurationId"); //$NON-NLS-1$
				String scopeId = memento.getString("scopeId"); //$NON-NLS-1$		
				IConfigurationElement[] deprecatedConfigurationElements2 = deprecatedConfigurationElement.getChildren("accelerator"); //$NON-NLS-1$

				for (int j = 0;
					j < deprecatedConfigurationElements2.length;
					j++) {
					IConfigurationElement deprecatedConfigurationElement2 =
						deprecatedConfigurationElements2[j];
					KeySequenceBindingDefinition keySequenceBindingDefinition =
						Persistence.readKeySequenceBindingDefinition(
							new ConfigurationElementMemento(deprecatedConfigurationElement2),
							getNamespace(deprecatedConfigurationElement2));

					if (keySequenceBindingDefinition != null) {
						keySequenceBindingDefinition =
							new KeySequenceBindingDefinition(
								scopeId,
								keySequenceBindingDefinition.getCommandId(),
								keyConfigurationId,
								keySequenceBindingDefinition.getKeySequence(),
								keySequenceBindingDefinition.getLocale(),
								keySequenceBindingDefinition.getPlatform(),
								keySequenceBindingDefinition.getSourceId());

						keySequenceBindingDefinitions.add(
							keySequenceBindingDefinition);
					}
				}
			}
		}

		deprecatedConfigurationElements = extensionRegistry.getConfigurationElementsFor("org.eclipse.ui.actionDefinitions"); //$NON-NLS-1$

		for (int i = 0; i < deprecatedConfigurationElements.length; i++) {
			IConfigurationElement deprecatedConfigurationElement =
				deprecatedConfigurationElements[i];
			String name = deprecatedConfigurationElement.getName();

			if ("actionDefinition".equals(name)) //$NON-NLS-1$
				readCommandDefinition(deprecatedConfigurationElement);
		}
		// TODO deprecated end

		IConfigurationElement[] configurationElements =
			extensionRegistry.getConfigurationElementsFor(
				Persistence.PACKAGE_FULL);

		for (int i = 0; i < configurationElements.length; i++) {
            IConfigurationElement configurationElement = configurationElements[i];
            String name = configurationElement.getName();

            if (Persistence.TAG_ACTIVE_KEY_CONFIGURATION.equals(name))
                readActiveKeyConfigurationDefinition(configurationElement);
            else if (Persistence.TAG_CATEGORY.equals(name))
                readCategoryDefinition(configurationElement);
            else if (Persistence.TAG_COMMAND.equals(name))
                readCommandDefinition(configurationElement);
            else if (Persistence.TAG_HANDLER.equals(name)) {
                readHandlerSubmissionDefinition(configurationElement);
            } else if (Persistence.TAG_KEY_CONFIGURATION.equals(name))
                readKeyConfigurationDefinition(configurationElement);
            else if (Persistence.TAG_KEY_SEQUENCE_BINDING.equals(name))
                    readKeySequenceBindingDefinition(configurationElement);
        }

		boolean commandRegistryChanged = false;

		if (!activeKeyConfigurationDefinitions
			.equals(super.activeKeyConfigurationDefinitions)) {
			super.activeKeyConfigurationDefinitions =
				Collections.unmodifiableList(activeKeyConfigurationDefinitions);
			commandRegistryChanged = true;
		}

		if (!categoryDefinitions.equals(super.categoryDefinitions)) {
			super.categoryDefinitions =
				Collections.unmodifiableList(categoryDefinitions);
			commandRegistryChanged = true;
		}

		if (!commandDefinitions.equals(super.commandDefinitions)) {
			super.commandDefinitions =
				Collections.unmodifiableList(commandDefinitions);
			commandRegistryChanged = true;
		}

		if (!handlers.equals(super.handlers)) {
			super.handlers = Collections.unmodifiableList(handlers);
			commandRegistryChanged = true;
		}

		if (!imageBindingDefinitions.equals(super.imageBindingDefinitions)) {
			super.imageBindingDefinitions =
				Collections.unmodifiableList(imageBindingDefinitions);
			commandRegistryChanged = true;
		}

		if (!keyConfigurationDefinitions
			.equals(super.keyConfigurationDefinitions)) {
			super.keyConfigurationDefinitions =
				Collections.unmodifiableList(keyConfigurationDefinitions);
			commandRegistryChanged = true;
		}

		if (!keySequenceBindingDefinitions
			.equals(super.keySequenceBindingDefinitions)) {
			super.keySequenceBindingDefinitions =
				Collections.unmodifiableList(keySequenceBindingDefinitions);
			commandRegistryChanged = true;
		}

		if (commandRegistryChanged)
			fireCommandRegistryChanged();
	}

	private void readActiveKeyConfigurationDefinition(IConfigurationElement configurationElement) {
		ActiveKeyConfigurationDefinition activeKeyConfigurationDefinition =
			Persistence.readActiveKeyConfigurationDefinition(
				new ConfigurationElementMemento(configurationElement),
				getNamespace(configurationElement));

		if (activeKeyConfigurationDefinition != null)
			activeKeyConfigurationDefinitions.add(
				activeKeyConfigurationDefinition);
	}

	private void readCategoryDefinition(IConfigurationElement configurationElement) {
		CategoryDefinition categoryDefinition =
			Persistence.readCategoryDefinition(
				new ConfigurationElementMemento(configurationElement),
				getNamespace(configurationElement));

		if (categoryDefinition != null)
			categoryDefinitions.add(categoryDefinition);
	}

	private void readCommandDefinition(IConfigurationElement configurationElement) {
		CommandDefinition commandDefinition =
			Persistence.readCommandDefinition(
				new ConfigurationElementMemento(configurationElement),
				getNamespace(configurationElement));

		if (commandDefinition != null)
			commandDefinitions.add(commandDefinition);
	}

	/**
	 * Reads the handler definition from XML -- creating a proxy to submit to
	 * the workbench command support.  If the handler definition is valid, then
	 * it will be added to <code>handlers</code> to be picked up later.
	 * 
	 * @param configurationElement The configuration element from which to read;
	 * must not be <code>null</code>.
	 */
	private final void readHandlerSubmissionDefinition(final IConfigurationElement configurationElement) {
	    final IHandler handler =
			Persistence.readHandlerSubmissionDefinition(configurationElement);

		if (handler != null)
			handlers.add(handler);
	}

	private void readKeyConfigurationDefinition(IConfigurationElement configurationElement) {
		KeyConfigurationDefinition keyConfigurationDefinition =
			Persistence.readKeyConfigurationDefinition(
				new ConfigurationElementMemento(configurationElement),
				getNamespace(configurationElement));

		if (keyConfigurationDefinition != null)
			keyConfigurationDefinitions.add(keyConfigurationDefinition);
	}

	private void readKeySequenceBindingDefinition(IConfigurationElement configurationElement) {
		KeySequenceBindingDefinition keySequenceBindingDefinition =
			Persistence.readKeySequenceBindingDefinition(
				new ConfigurationElementMemento(configurationElement),
				getNamespace(configurationElement));

		if (keySequenceBindingDefinition != null)
			keySequenceBindingDefinitions.add(keySequenceBindingDefinition);
	}
}
