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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ui.IMemento;
import org.eclipse.ui.commands.ICategory;
import org.eclipse.ui.commands.ICommand;
import org.eclipse.ui.commands.IContextBinding;
import org.eclipse.ui.commands.IImageBinding;
import org.eclipse.ui.commands.IKeyBinding;
import org.eclipse.ui.commands.IKeyConfiguration;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.keys.KeySequence;
import org.eclipse.ui.keys.ParseException;

final class Persistence {

	final static String PACKAGE_BASE = "commands"; //$NON-NLS-1$
	final static String PACKAGE_FULL = "org.eclipse.ui." + PACKAGE_BASE; //$NON-NLS-1$
	final static String TAG_CATEGORY = "category"; //$NON-NLS-1$	
	final static String TAG_CATEGORY_ID = "categoryId"; //$NON-NLS-1$
	final static String TAG_COMMAND = "command"; //$NON-NLS-1$	
	final static String TAG_COMMAND_ID = "commandId"; //$NON-NLS-1$	
	final static String TAG_CONTEXT_BINDING = "contextBinding"; //$NON-NLS-1$	
	final static String TAG_CONTEXT_ID = "contextId"; //$NON-NLS-1$	
	final static String TAG_DESCRIPTION = "description"; //$NON-NLS-1$
	final static String TAG_IMAGE_BINDING = "imageBinding"; //$NON-NLS-1$	
	final static String TAG_IMAGE_STYLE = "imageStyle"; //$NON-NLS-1$	
	final static String TAG_IMAGE_URI = "imageUri"; //$NON-NLS-1$
	final static String TAG_KEY_BINDING = "keyBinding"; //$NON-NLS-1$
	final static String TAG_KEY_CONFIGURATION = "keyConfiguration"; //$NON-NLS-1$	
	final static String TAG_KEY_CONFIGURATION_ID = "keyConfigurationId"; //$NON-NLS-1$	
	final static String TAG_KEY_SEQUENCE = "keySequence"; //$NON-NLS-1$	
	final static String TAG_ID = "id"; //$NON-NLS-1$
	final static String TAG_LOCALE = "locale"; //$NON-NLS-1$
	final static String TAG_NAME = "name"; //$NON-NLS-1$	
	final static String TAG_PARENT_ID = "parentId"; //$NON-NLS-1$
	final static String TAG_PLATFORM = "platform"; //$NON-NLS-1$	
	final static String TAG_PLUGIN_ID = "pluginId"; //$NON-NLS-1$

	static List readCategories(IMemento memento, String name, String pluginIdOverride) {
		if (memento == null || name == null)
			throw new NullPointerException();			
	
		IMemento[] mementos = memento.getChildren(name);
	
		if (mementos == null)
			throw new NullPointerException();
	
		List list = new ArrayList(mementos.length);
	
		for (int i = 0; i < mementos.length; i++)
			list.add(readCategory(mementos[i], pluginIdOverride));
	
		return list;				
	}

	static ICategory readCategory(IMemento memento, String pluginIdOverride) {
		if (memento == null)
			throw new NullPointerException();			

		String description = memento.getString(TAG_DESCRIPTION);
		String id = memento.getString(TAG_ID);

		if (id == null)
			id = Util.ZERO_LENGTH_STRING;
		
		String name = memento.getString(TAG_NAME);

		if (name == null)
			name = Util.ZERO_LENGTH_STRING;
		
		String pluginId = pluginIdOverride != null ? pluginIdOverride : memento.getString(TAG_PLUGIN_ID);
		return new Category(description, id, name, pluginId);
	}

	static ICommand readCommand(IMemento memento, String pluginIdOverride) {
		if (memento == null)
			throw new NullPointerException();			

		String categoryId = memento.getString(TAG_CATEGORY_ID);
		String description = memento.getString(TAG_DESCRIPTION);
		String id = memento.getString(TAG_ID);

		if (id == null)
			id = Util.ZERO_LENGTH_STRING;
		
		String name = memento.getString(TAG_NAME);

		if (name == null)
			name = Util.ZERO_LENGTH_STRING;
		
		String pluginId = pluginIdOverride != null ? pluginIdOverride : memento.getString(TAG_PLUGIN_ID);
		return new Command(categoryId, description, id, name, pluginId);
	}

	static List readCommands(IMemento memento, String name, String pluginIdOverride) {
		if (memento == null || name == null)
			throw new NullPointerException();			
	
		IMemento[] mementos = memento.getChildren(name);
	
		if (mementos == null)
			throw new NullPointerException();
	
		List list = new ArrayList(mementos.length);
	
		for (int i = 0; i < mementos.length; i++)
			list.add(readCommand(mementos[i], pluginIdOverride));
	
		return list;				
	}

	static IContextBinding readContextBinding(IMemento memento, String pluginIdOverride) {
		if (memento == null)
			throw new NullPointerException();			

		String commandId = memento.getString(TAG_COMMAND_ID);

		if (commandId == null)
			commandId = Util.ZERO_LENGTH_STRING;

		String contextId = memento.getString(TAG_CONTEXT_ID);

		if (contextId == null)
			contextId = Util.ZERO_LENGTH_STRING;

		String pluginId = pluginIdOverride != null ? pluginIdOverride : memento.getString(TAG_PLUGIN_ID);
		return new ContextBinding(commandId, contextId, pluginId);
	}

	static List readContextBindings(IMemento memento, String name, String pluginIdOverride) {
		if (memento == null || name == null)
			throw new NullPointerException();			
	
		IMemento[] mementos = memento.getChildren(name);
	
		if (mementos == null)
			throw new NullPointerException();
	
		List list = new ArrayList(mementos.length);
	
		for (int i = 0; i < mementos.length; i++)
			list.add(readContextBinding(mementos[i], pluginIdOverride));
	
		return list;				
	}

	static IImageBinding readImageBinding(IMemento memento, String pluginIdOverride) {
		if (memento == null)
			throw new NullPointerException();			

		String commandId = memento.getString(TAG_COMMAND_ID);

		if (commandId == null)
			commandId = Util.ZERO_LENGTH_STRING;

		String imageStyle = memento.getString(TAG_IMAGE_STYLE);

		if (imageStyle == null)
			imageStyle = Util.ZERO_LENGTH_STRING;

		String imageUri = memento.getString(TAG_IMAGE_URI);

		if (imageUri == null)
			imageUri = Util.ZERO_LENGTH_STRING;

		String locale = memento.getString(TAG_LOCALE);

		if (locale == null)
			locale = Util.ZERO_LENGTH_STRING;

		String platform = memento.getString(TAG_PLATFORM);

		if (platform == null)
			platform = Util.ZERO_LENGTH_STRING;

		String pluginId = pluginIdOverride != null ? pluginIdOverride : memento.getString(TAG_PLUGIN_ID);
		return new ImageBinding(commandId, imageStyle, imageUri, locale, platform, pluginId);
	}

	static List readImageBindings(IMemento memento, String name, String pluginIdOverride) {
		if (memento == null || name == null)
			throw new NullPointerException();			
	
		IMemento[] mementos = memento.getChildren(name);
	
		if (mementos == null)
			throw new NullPointerException();
	
		List list = new ArrayList(mementos.length);
	
		for (int i = 0; i < mementos.length; i++)
			list.add(readImageBinding(mementos[i], pluginIdOverride));
	
		return list;				
	}

	static IKeyBinding readKeyBinding(IMemento memento, String pluginIdOverride) {
		if (memento == null)
			throw new NullPointerException();			

		String commandId = memento.getString(TAG_COMMAND_ID);

		if (commandId == null)
			commandId = Util.ZERO_LENGTH_STRING;

		String contextId = memento.getString(TAG_CONTEXT_ID);

		if (contextId == null)
			contextId = Util.ZERO_LENGTH_STRING;

		String keyConfigurationId = memento.getString(TAG_KEY_CONFIGURATION_ID);

		if (keyConfigurationId == null)
			keyConfigurationId = Util.ZERO_LENGTH_STRING;
	
		String keySequenceAsString = memento.getString(TAG_KEY_SEQUENCE);

		if (keySequenceAsString == null)
			keySequenceAsString = Util.ZERO_LENGTH_STRING;
			
		KeySequence keySequence = null;
		
		try {
			keySequence = KeySequence.getInstance(keySequenceAsString);
		} catch (ParseException eParse) {
			keySequence = KeySequence.getInstance();
		}
			
		String locale = memento.getString(TAG_LOCALE);

		if (locale == null)
			locale = Util.ZERO_LENGTH_STRING;

		String platform = memento.getString(TAG_PLATFORM);

		if (platform == null)
			platform = Util.ZERO_LENGTH_STRING;

		String pluginId = pluginIdOverride != null ? pluginIdOverride : memento.getString(TAG_PLUGIN_ID);
		return new KeyBinding(commandId, contextId, keyConfigurationId, keySequence, locale, platform, pluginId);
	}

	static List readKeyBindings(IMemento memento, String name, String pluginIdOverride) {
		if (memento == null || name == null)
			throw new NullPointerException();			
	
		IMemento[] mementos = memento.getChildren(name);
	
		if (mementos == null)
			throw new NullPointerException();
	
		List list = new ArrayList(mementos.length);
	
		for (int i = 0; i < mementos.length; i++)
			list.add(readKeyBinding(mementos[i], pluginIdOverride));
	
		return list;				
	}

	static IKeyConfiguration readKeyConfiguration(IMemento memento, String pluginIdOverride) {
		if (memento == null)
			throw new NullPointerException();			

		String description = memento.getString(TAG_DESCRIPTION);
		String id = memento.getString(TAG_ID);

		if (id == null)
			id = Util.ZERO_LENGTH_STRING;
		
		String name = memento.getString(TAG_NAME);

		if (name == null)
			name = Util.ZERO_LENGTH_STRING;
		
		String pluginId = pluginIdOverride != null ? pluginIdOverride : memento.getString(TAG_PLUGIN_ID);
		return new KeyConfiguration(description, id, name, pluginId);
	}

	static List readKeyConfigurations(IMemento memento, String name, String pluginIdOverride) {
		if (memento == null || name == null)
			throw new NullPointerException();			
	
		IMemento[] mementos = memento.getChildren(name);
	
		if (mementos == null)
			throw new NullPointerException();
	
		List list = new ArrayList(mementos.length);
	
		for (int i = 0; i < mementos.length; i++)
			list.add(readKeyConfiguration(mementos[i], pluginIdOverride));
	
		return list;				
	}

	static void writeCategories(IMemento memento, String name, List categories) {
		if (memento == null || name == null || categories == null)
			throw new NullPointerException();
		
		categories = new ArrayList(categories);
		Iterator iterator = categories.iterator();

		while (iterator.hasNext())
			Util.assertInstance(iterator.next(), ICategory.class);

		iterator = categories.iterator();

		while (iterator.hasNext()) 
			writeCategory(memento.createChild(name), (ICategory) iterator.next());
	}
	
	static void writeCategory(IMemento memento, ICategory category) {
		if (memento == null || category == null)
			throw new NullPointerException();

		memento.putString(TAG_DESCRIPTION, category.getDescription());
		memento.putString(TAG_ID, category.getId());
		memento.putString(TAG_NAME, category.getName());
		memento.putString(TAG_PLUGIN_ID, category.getPluginId());
	}

	static void writeCommand(IMemento memento, ICommand command) {
		if (memento == null || command == null)
			throw new NullPointerException();

		memento.putString(TAG_CATEGORY_ID, command.getCategoryId());
		memento.putString(TAG_DESCRIPTION, command.getDescription());
		memento.putString(TAG_ID, command.getId());
		memento.putString(TAG_NAME, command.getName());
		memento.putString(TAG_PLUGIN_ID, command.getPluginId());
	}

	static void writeCommands(IMemento memento, String name, List commands) {
		if (memento == null || name == null || commands == null)
			throw new NullPointerException();
		
		commands = new ArrayList(commands);
		Iterator iterator = commands.iterator();

		while (iterator.hasNext())
			Util.assertInstance(iterator.next(), ICommand.class);

		iterator = commands.iterator();

		while (iterator.hasNext()) 
			writeCommand(memento.createChild(name), (ICommand) iterator.next());
	}
	
	static void writeContextBinding(IMemento memento, IContextBinding contextBinding) {
		if (memento == null || contextBinding == null)
			throw new NullPointerException();

		memento.putString(TAG_COMMAND_ID, contextBinding.getCommandId());
		memento.putString(TAG_CONTEXT_ID, contextBinding.getContextId());
		memento.putString(TAG_PLUGIN_ID, contextBinding.getPluginId());
	}

	static void writeContextBindings(IMemento memento, String name, List contextBindings) {
		if (memento == null || name == null || contextBindings == null)
			throw new NullPointerException();
		
		contextBindings = new ArrayList(contextBindings);
		Iterator iterator = contextBindings.iterator();

		while (iterator.hasNext())
			Util.assertInstance(iterator.next(), IContextBinding.class);

		iterator = contextBindings.iterator();

		while (iterator.hasNext()) 
			writeContextBinding(memento.createChild(name), (IContextBinding) iterator.next());
	}
	
	static void writeImageBinding(IMemento memento, IImageBinding imageBinding) {
		if (memento == null || imageBinding == null)
			throw new NullPointerException();

		memento.putString(TAG_COMMAND_ID, imageBinding.getCommandId());
		memento.putString(TAG_IMAGE_STYLE, imageBinding.getImageStyle());
		memento.putString(TAG_IMAGE_URI, imageBinding.getImageUri());
		memento.putString(TAG_LOCALE, imageBinding.getLocale());
		memento.putString(TAG_PLATFORM, imageBinding.getPlatform());
		memento.putString(TAG_PLUGIN_ID, imageBinding.getPluginId());
	}

	static void writeImageBindings(IMemento memento, String name, List imageBindings) {
		if (memento == null || name == null || imageBindings == null)
			throw new NullPointerException();
		
		imageBindings = new ArrayList(imageBindings);
		Iterator iterator = imageBindings.iterator();

		while (iterator.hasNext())
			Util.assertInstance(iterator.next(), IImageBinding.class);

		iterator = imageBindings.iterator();

		while (iterator.hasNext()) 
			writeImageBinding(memento.createChild(name), (IImageBinding) iterator.next());
	}

	static void writeKeyBinding(IMemento memento, IKeyBinding keyBinding) {
		if (memento == null || keyBinding == null)
			throw new NullPointerException();

		memento.putString(TAG_COMMAND_ID, keyBinding.getCommandId());
		memento.putString(TAG_CONTEXT_ID, keyBinding.getContextId());
		memento.putString(TAG_KEY_CONFIGURATION_ID, keyBinding.getKeyConfigurationId());
		memento.putString(TAG_KEY_SEQUENCE, keyBinding.getKeySequence().toString());
		memento.putString(TAG_LOCALE, keyBinding.getLocale());
		memento.putString(TAG_PLATFORM, keyBinding.getPlatform());
		memento.putString(TAG_PLUGIN_ID, keyBinding.getPluginId());
	}

	static void writeKeyBindings(IMemento memento, String name, List keyBindings) {
		if (memento == null || name == null || keyBindings == null)
			throw new NullPointerException();
		
		keyBindings = new ArrayList(keyBindings);
		Iterator iterator = keyBindings.iterator();

		while (iterator.hasNext())
			Util.assertInstance(iterator.next(), IKeyBinding.class);

		iterator = keyBindings.iterator();

		while (iterator.hasNext()) 
			writeKeyBinding(memento.createChild(name), (IKeyBinding) iterator.next());
	}

	static void writeKeyConfiguration(IMemento memento, IKeyConfiguration keyConfiguration) {
		if (memento == null || keyConfiguration == null)
			throw new NullPointerException();
	
		memento.putString(TAG_DESCRIPTION, keyConfiguration.getDescription());
		memento.putString(TAG_ID, keyConfiguration.getId());
		memento.putString(TAG_NAME, keyConfiguration.getName());
		memento.putString(TAG_PLUGIN_ID, keyConfiguration.getPluginId());
	}
	
	static void writeKeyConfigurations(IMemento memento, String name, List keyConfigurations) {
		if (memento == null || name == null || keyConfigurations == null)
			throw new NullPointerException();
			
		keyConfigurations = new ArrayList(keyConfigurations);
		Iterator iterator = keyConfigurations.iterator();
	
		while (iterator.hasNext())
			Util.assertInstance(iterator.next(), IKeyConfiguration.class);
	
		iterator = keyConfigurations.iterator();
	
		while (iterator.hasNext()) 
			writeKeyConfiguration(memento.createChild(name), (IKeyConfiguration) iterator.next());
	}

	private Persistence() {
		super();
	}	
}
