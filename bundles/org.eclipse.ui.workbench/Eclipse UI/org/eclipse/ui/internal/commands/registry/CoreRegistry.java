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

package org.eclipse.ui.internal.commands.registry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IPluginRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.PlatformUI;

public final class CoreRegistry extends AbstractRegistry {

	private final class RegistryReader extends org.eclipse.ui.internal.registry.RegistryReader {

		private final static String DEPRECATED_TAG_SCOPE = "scope"; //$NON-NLS-1$	
		private final static int RANK_CORE = 2;
		private final static String TAG_ROOT = Persistence.PACKAGE_BASE;
		
		private List activeGestureConfigurations;
		private List activeKeyConfigurations;		
		private List categories;
		private List commands;
		private List contextBindings;
		private List contexts;
		private List gestureBindings;
		private List gestureConfigurations;
		private List keyBindings;
		private List keyConfigurations;
	
		private RegistryReader(IPluginRegistry pluginRegistry) {
			super();
			activeGestureConfigurations = new ArrayList();
			activeKeyConfigurations = new ArrayList();		
			categories = new ArrayList();
			commands = new ArrayList();
			contextBindings = new ArrayList();
			contexts = new ArrayList();
			gestureBindings = new ArrayList();
			gestureConfigurations = new ArrayList();
			keyBindings = new ArrayList();
			keyConfigurations = new ArrayList();

			if (pluginRegistry != null)	
				readRegistry(pluginRegistry, PlatformUI.PLUGIN_ID, TAG_ROOT);

			CoreRegistry.this.activeGestureConfigurations = Collections.unmodifiableList(activeGestureConfigurations);
			CoreRegistry.this.activeKeyConfigurations = Collections.unmodifiableList(activeKeyConfigurations);
			CoreRegistry.this.categories = Collections.unmodifiableList(categories);
			CoreRegistry.this.commands = Collections.unmodifiableList(commands);			
			CoreRegistry.this.contextBindings = Collections.unmodifiableList(contextBindings);			
			CoreRegistry.this.contexts = Collections.unmodifiableList(contexts);
			CoreRegistry.this.gestureBindings = Collections.unmodifiableList(gestureBindings);
			CoreRegistry.this.gestureConfigurations = Collections.unmodifiableList(gestureConfigurations);
			CoreRegistry.this.keyBindings = Collections.unmodifiableList(keyBindings);
			CoreRegistry.this.keyConfigurations = Collections.unmodifiableList(keyConfigurations);
		}

		protected boolean readElement(IConfigurationElement element) {
			String name = element.getName();

			if (DEPRECATED_TAG_SCOPE.equals(name))
				return readContext(element);

			if (Persistence.TAG_ACTIVE_GESTURE_CONFIGURATION.equals(name))
				return readActiveGestureConfiguration(element);

			if (Persistence.TAG_ACTIVE_KEY_CONFIGURATION.equals(name))
				return readActiveKeyConfiguration(element);

			if (Persistence.TAG_CATEGORY.equals(name))
				return readCategory(element);

			if (Persistence.TAG_COMMAND.equals(name))
				return readCommand(element);

			if (Persistence.TAG_CONTEXT.equals(name))
				return readContext(element);

			if (Persistence.TAG_CONTEXT_BINDING.equals(name))
				return readContextBinding(element);

			if (Persistence.TAG_GESTURE_BINDING.equals(name))
				return readGestureBinding(element);

			if (Persistence.TAG_GESTURE_CONFIGURATION.equals(name))
				return readGestureConfiguration(element);

			if (Persistence.TAG_KEY_BINDING.equals(name))
				return readKeyBinding(element);

			if (Persistence.TAG_KEY_CONFIGURATION.equals(name))
				return readKeyConfiguration(element);

			return false;
		}

		private String getPlugin(IConfigurationElement element) {
			String plugin = null;	
		
			if (element != null) {	
				IExtension extension = element.getDeclaringExtension();
			
				if (extension != null) {
					IPluginDescriptor pluginDescriptor = extension.getDeclaringPluginDescriptor();
				
					if (pluginDescriptor != null) 
						plugin = pluginDescriptor.getUniqueIdentifier();				
				}
			}

			return plugin;
		}

		private boolean readActiveGestureConfiguration(IConfigurationElement element) {
			ActiveConfiguration activeGestureConfiguration = Persistence.readActiveConfiguration(ConfigurationElementMemento.create(element), getPlugin(element));
		
			if (activeGestureConfiguration != null)
				activeGestureConfigurations.add(activeGestureConfiguration);	
			
			return true;
		}

		private boolean readActiveKeyConfiguration(IConfigurationElement element) {
			ActiveConfiguration activeKeyConfiguration = Persistence.readActiveConfiguration(ConfigurationElementMemento.create(element), getPlugin(element));
		
			if (activeKeyConfiguration != null)
				activeKeyConfigurations.add(activeKeyConfiguration);	
			
			return true;
		}

		private boolean readCategory(IConfigurationElement element) {
			Category category = Persistence.readCategory(ConfigurationElementMemento.create(element), getPlugin(element));
		
			if (category != null)
				categories.add(category);	
			
			return true;
		}

		private boolean readCommand(IConfigurationElement element) {
			Command command = Persistence.readCommand(ConfigurationElementMemento.create(element), getPlugin(element));
		
			if (command != null)
				commands.add(command);	
			
			return true;
		}

		private boolean readContext(IConfigurationElement element) {
			Context context = Persistence.readContext(ConfigurationElementMemento.create(element), getPlugin(element));
		
			if (context != null)
				contexts.add(context);	
			
			return true;
		}

		private boolean readContextBinding(IConfigurationElement element) {
			ContextBinding contextBinding = Persistence.readContextBinding(ConfigurationElementMemento.create(element), getPlugin(element));
		
			if (contextBinding != null)
				contextBindings.add(contextBinding);	
			
			return true;
		}

		private boolean readGestureBinding(IConfigurationElement element) {
			SequenceBinding gestureBinding = Persistence.readSequenceBinding(ConfigurationElementMemento.create(element), getPlugin(element), RANK_CORE);

			if (gestureBinding != null)
				gestureBindings.add(gestureBinding);

			return true;
		}

		private boolean readGestureConfiguration(IConfigurationElement element) {
			Configuration gestureConfiguration = Persistence.readConfiguration(ConfigurationElementMemento.create(element), getPlugin(element));
		
			if (gestureConfiguration != null)
				gestureConfigurations.add(gestureConfiguration);	
			
			return true;
		}

		private boolean readKeyBinding(IConfigurationElement element) {
			SequenceBinding keyBinding = Persistence.readSequenceBinding(ConfigurationElementMemento.create(element), getPlugin(element), RANK_CORE);

			if (keyBinding != null)
				keyBindings.add(keyBinding);

			return true;
		}
	
		private boolean readKeyConfiguration(IConfigurationElement element) {
			Configuration keyConfiguration = Persistence.readConfiguration(ConfigurationElementMemento.create(element), getPlugin(element));
		
			if (keyConfiguration != null)
				keyConfigurations.add(keyConfiguration);	
			
			return true;
		}
	}

	private static CoreRegistry instance;
	
	public static CoreRegistry getInstance() {
		if (instance == null)
			instance = new CoreRegistry();
	
		return instance;
	}

	private boolean loaded;

	private CoreRegistry() {
		super();
	}

	public void load()
		throws IOException {		
		if (!loaded) {
			new RegistryReader(Platform.getPluginRegistry());
			loaded = true;
			fireRegistryChanged();
		}
	}
}
