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
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.eclipse.swt.SWT;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.commands.IActiveKeyConfiguration;
import org.eclipse.ui.commands.ICategory;
import org.eclipse.ui.commands.ICommand;
import org.eclipse.ui.commands.IContextBinding;
import org.eclipse.ui.commands.IImageBinding;
import org.eclipse.ui.commands.IKeyBinding;
import org.eclipse.ui.commands.IKeyConfiguration;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.keys.KeySequence;
import org.eclipse.ui.keys.KeyStroke;
import org.eclipse.ui.keys.KeySupport;
import org.eclipse.ui.keys.ParseException;

final class Persistence {

	final static String PACKAGE_BASE = "commands"; //$NON-NLS-1$
	final static String PACKAGE_FULL = "org.eclipse.ui." + PACKAGE_BASE; //$NON-NLS-1$
	final static String TAG_ACTIVE_KEY_CONFIGURATION = "activeKeyConfiguration"; //$NON-NLS-1$	
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

	static IActiveKeyConfiguration readActiveKeyConfiguration(IMemento memento, String pluginIdOverride) {
		if (memento == null)
			throw new NullPointerException();			

		String keyConfigurationId = memento.getString(TAG_KEY_CONFIGURATION_ID);

		// TODO deprecated start
		if (keyConfigurationId == null)
			keyConfigurationId = memento.getString("value"); //$NON-NLS-1$ 
		// TODO deprecated end

		if (keyConfigurationId == null)
			keyConfigurationId = Util.ZERO_LENGTH_STRING;
	
		String pluginId = pluginIdOverride != null ? pluginIdOverride : memento.getString(TAG_PLUGIN_ID);
		
		// TODO deprecated start
		if (pluginIdOverride == null && pluginId == null)
			pluginId = memento.getString("plugin"); //$NON-NLS-1$ 
		// TODO deprecated end		
		
		return new ActiveKeyConfiguration(keyConfigurationId, pluginId);
	}

	static List readActiveKeyConfigurations(IMemento memento, String name, String pluginIdOverride) {
		if (memento == null || name == null)
			throw new NullPointerException();			
	
		IMemento[] mementos = memento.getChildren(name);
	
		if (mementos == null)
			throw new NullPointerException();
	
		List list = new ArrayList(mementos.length);
	
		for (int i = 0; i < mementos.length; i++)
			list.add(readActiveKeyConfiguration(mementos[i], pluginIdOverride));
	
		return list;				
	}

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
		
		// TODO deprecated start
		if (pluginIdOverride == null && pluginId == null)
			pluginId = memento.getString("plugin"); //$NON-NLS-1$ 
		// TODO deprecated end		
		
		return new Category(description, id, name, pluginId);
	}

	static ICommand readCommand(IMemento memento, String pluginIdOverride) {
		if (memento == null)
			throw new NullPointerException();			

		String categoryId = memento.getString(TAG_CATEGORY_ID);
		
		// TODO deprecated start
		if (categoryId == null)
			categoryId = memento.getString("category"); //$NON-NLS-1$ 
		// TODO deprecated end		
		
		String description = memento.getString(TAG_DESCRIPTION);
		String id = memento.getString(TAG_ID);

		if (id == null)
			id = Util.ZERO_LENGTH_STRING;
		
		String name = memento.getString(TAG_NAME);

		if (name == null)
			name = Util.ZERO_LENGTH_STRING;
		
		String pluginId = pluginIdOverride != null ? pluginIdOverride : memento.getString(TAG_PLUGIN_ID);

		// TODO deprecated start
		if (pluginIdOverride == null && pluginId == null)
			pluginId = memento.getString("plugin"); //$NON-NLS-1$ 
		// TODO deprecated end		
		
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

	static IKeyBinding readKeyBinding(IMemento memento, String pluginIdOverride, int rank) {
		if (memento == null)
			throw new NullPointerException();			

		String commandId = memento.getString(TAG_COMMAND_ID);

		// TODO deprecated start
		if (commandId == null)
			commandId = memento.getString("command"); //$NON-NLS-1$ 
		// TODO deprecated end		

		if (commandId == null)
			commandId = Util.ZERO_LENGTH_STRING;

		String contextId = memento.getString(TAG_CONTEXT_ID);

		// TODO deprecated start
		if (contextId == null)
			contextId = memento.getString("scope"); //$NON-NLS-1$
		// TODO deprecated end		

		if (contextId == null)
			contextId = Util.ZERO_LENGTH_STRING;

		String keyConfigurationId = memento.getString(TAG_KEY_CONFIGURATION_ID);

		if (keyConfigurationId == null)
			keyConfigurationId = Util.ZERO_LENGTH_STRING;

		// TODO deprecated start
		if (keyConfigurationId == null)
			keyConfigurationId = memento.getString("configuration"); //$NON-NLS-1$
		// TODO deprecated end		

		KeySequence keySequence = KeySequence.getInstance();
		String keySequenceAsString = memento.getString(TAG_KEY_SEQUENCE);

		if (keySequenceAsString != null)
			try {
				keySequence = KeySequence.getInstance(keySequenceAsString);
			} catch (ParseException eParse) {
			}
		// TODO deprecated start
		else {
			IMemento mementoSequence = memento.getChild("sequence"); //$NON-NLS-1$
			
			if (mementoSequence != null)
				keySequence = deprecatedSequenceToKeySequence(readDeprecatedSequence(mementoSequence));
			else {
				String string = memento.getString("string"); //$NON-NLS-1$

				if (string != null)
					keySequence = deprecatedSequenceToKeySequence(parseDeprecatedSequence(string));
			}			
			
		}	
		// TODO deprecated end
		
		String locale = memento.getString(TAG_LOCALE);

		if (locale == null)
			locale = Util.ZERO_LENGTH_STRING;

		String platform = memento.getString(TAG_PLATFORM);

		if (platform == null)
			platform = Util.ZERO_LENGTH_STRING;

		String pluginId = pluginIdOverride != null ? pluginIdOverride : memento.getString(TAG_PLUGIN_ID);
		
		// TODO deprecated start
		if (pluginIdOverride == null && pluginId == null)
			pluginId = memento.getString("plugin"); //$NON-NLS-1$ 
		// TODO deprecated end			
		
		return new KeyBinding(commandId, contextId, keyConfigurationId, keySequence, locale, platform, pluginId, rank);
	}

	static List readKeyBindings(IMemento memento, String name, String pluginIdOverride, int rank) {
		if (memento == null || name == null)
			throw new NullPointerException();			
	
		IMemento[] mementos = memento.getChildren(name);
	
		if (mementos == null)
			throw new NullPointerException();
	
		List list = new ArrayList(mementos.length);
	
		for (int i = 0; i < mementos.length; i++)
			list.add(readKeyBinding(mementos[i], pluginIdOverride, rank));
	
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

		String parentId = memento.getString(TAG_PARENT_ID);		

		// TODO deprecated start
		if (parentId == null)
			parentId = memento.getString("parent"); //$NON-NLS-1$ 
		// TODO deprecated end		

		String pluginId = pluginIdOverride != null ? pluginIdOverride : memento.getString(TAG_PLUGIN_ID);

		// TODO deprecated start
		if (pluginIdOverride == null && pluginId == null)
			pluginId = memento.getString("plugin"); //$NON-NLS-1$ 
		// TODO deprecated end				
		
		return new KeyConfiguration(description, id, name, parentId, pluginId);
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

	static void writeActiveKeyConfiguration(IMemento memento, IActiveKeyConfiguration activeKeyConfiguration) {
		if (memento == null || activeKeyConfiguration == null)
			throw new NullPointerException();

		memento.putString(TAG_KEY_CONFIGURATION_ID, activeKeyConfiguration.getKeyConfigurationId());
		memento.putString(TAG_PLUGIN_ID, activeKeyConfiguration.getPluginId());
	}

	static void writeActiveKeyConfigurations(IMemento memento, String name, List activeKeyConfigurations) {
		if (memento == null || name == null || activeKeyConfigurations == null)
			throw new NullPointerException();
		
		activeKeyConfigurations = new ArrayList(activeKeyConfigurations);
		Iterator iterator = activeKeyConfigurations.iterator();

		while (iterator.hasNext())
			Util.assertInstance(iterator.next(), IActiveKeyConfiguration.class);

		iterator = activeKeyConfigurations.iterator();

		while (iterator.hasNext()) 
			writeActiveKeyConfiguration(memento.createChild(name), (IActiveKeyConfiguration) iterator.next());
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
		memento.putString(TAG_PARENT_ID, keyConfiguration.getParentId());
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

	// TODO deprecated start
	
	private final static String ALT = "Alt"; //$NON-NLS-1$
	private final static String COMMAND = "Command"; //$NON-NLS-1$
	private final static String CTRL = "Ctrl"; //$NON-NLS-1$
	private final static String MODIFIER_SEPARATOR = "+"; //$NON-NLS-1$
	private final static String SHIFT = "Shift"; //$NON-NLS-1$
	private final static String STROKE_SEPARATOR = " "; //$NON-NLS-1$

	private static Map stringToValueMap = new TreeMap();	

	static {		
		stringToValueMap.put("BACKSPACE", new Integer(8)); //$NON-NLS-1$
		stringToValueMap.put("TAB", new Integer(9)); //$NON-NLS-1$
		stringToValueMap.put("RETURN", new Integer(13)); //$NON-NLS-1$
		stringToValueMap.put("ENTER", new Integer(13)); //$NON-NLS-1$
		stringToValueMap.put("ESCAPE", new Integer(27)); //$NON-NLS-1$
		stringToValueMap.put("ESC", new Integer(27)); //$NON-NLS-1$
		stringToValueMap.put("DELETE", new Integer(127)); //$NON-NLS-1$
		stringToValueMap.put("SPACE", new Integer(' ')); //$NON-NLS-1$
		stringToValueMap.put("ARROW_UP", new Integer(SWT.ARROW_UP)); //$NON-NLS-1$
		stringToValueMap.put("ARROW_DOWN", new Integer(SWT.ARROW_DOWN)); //$NON-NLS-1$
		stringToValueMap.put("ARROW_LEFT", new Integer(SWT.ARROW_LEFT)); //$NON-NLS-1$
		stringToValueMap.put("ARROW_RIGHT", new Integer(SWT.ARROW_RIGHT)); //$NON-NLS-1$
		stringToValueMap.put("PAGE_UP", new Integer(SWT.PAGE_UP)); //$NON-NLS-1$
		stringToValueMap.put("PAGE_DOWN", new Integer(SWT.PAGE_DOWN)); //$NON-NLS-1$
		stringToValueMap.put("HOME", new Integer(SWT.HOME)); //$NON-NLS-1$
		stringToValueMap.put("END", new Integer(SWT.END)); //$NON-NLS-1$
		stringToValueMap.put("INSERT", new Integer(SWT.INSERT)); //$NON-NLS-1$
		stringToValueMap.put("F1", new Integer(SWT.F1)); //$NON-NLS-1$
		stringToValueMap.put("F2", new Integer(SWT.F2)); //$NON-NLS-1$
		stringToValueMap.put("F3", new Integer(SWT.F3)); //$NON-NLS-1$
		stringToValueMap.put("F4", new Integer(SWT.F4)); //$NON-NLS-1$
		stringToValueMap.put("F5", new Integer(SWT.F5)); //$NON-NLS-1$
		stringToValueMap.put("F6", new Integer(SWT.F6)); //$NON-NLS-1$
		stringToValueMap.put("F7", new Integer(SWT.F7)); //$NON-NLS-1$
		stringToValueMap.put("F8", new Integer(SWT.F8)); //$NON-NLS-1$
		stringToValueMap.put("F9", new Integer(SWT.F9)); //$NON-NLS-1$
		stringToValueMap.put("F10", new Integer(SWT.F10)); //$NON-NLS-1$
		stringToValueMap.put("F11", new Integer(SWT.F11)); //$NON-NLS-1$
		stringToValueMap.put("F12", new Integer(SWT.F12)); //$NON-NLS-1$		
	}

	private static KeySequence deprecatedSequenceToKeySequence(int[] sequence) {
		List keyStrokes = new ArrayList();
		
		for (int i = 0; i < sequence.length; i++)
			keyStrokes.add(deprecatedStrokeToKeyStroke(sequence[i]));
		
		return KeySequence.getInstance(keyStrokes);
	}

	private static KeyStroke deprecatedStrokeToKeyStroke(int stroke) {
		return KeySupport.convertFromSWT(stroke);
	}

	public static int[] parseDeprecatedSequence(String string) {
		if (string == null)
			throw new NullPointerException();
			
		StringTokenizer stringTokenizer = new StringTokenizer(string);
		int length = stringTokenizer.countTokens();
		int[] strokes = new int[length];
		
		for (int i = 0; i < length; i++)
			strokes[i] = parseDeprecatedStroke(stringTokenizer.nextToken());
		
		return strokes;		
	}

	public static int parseDeprecatedStroke(String string) {
		if (string == null)
			throw new NullPointerException();
		
		List list = new ArrayList();
		StringTokenizer stringTokenizer = new StringTokenizer(string, MODIFIER_SEPARATOR, true);
		
		while (stringTokenizer.hasMoreTokens())
			list.add(stringTokenizer.nextToken());

		int size = list.size();
		int value = 0;

		if (size % 2 == 1) {
			String token = (String) list.get(size - 1);			
			Integer integer = (Integer) stringToValueMap.get(token.toUpperCase());
		
			if (integer != null)
				value = integer.intValue();
			else if (token.length() == 1)
				value = token.toUpperCase().charAt(0);

			if (value != 0) {
				for (int i = 0; i < size - 1; i++) {
					token = (String) list.get(i);			
					
					if (i % 2 == 0) {
						if (token.equalsIgnoreCase(CTRL)) {
							if ((value & SWT.CTRL) != 0)
								return 0;
							
							value |= SWT.CTRL;
						} else if (token.equalsIgnoreCase(ALT)) {
							if ((value & SWT.ALT) != 0)
								return 0;

							value |= SWT.ALT;
						} else if (token.equalsIgnoreCase(SHIFT)) {
							if ((value & SWT.SHIFT) != 0)
								return 0;

							value |= SWT.SHIFT;
						} else if (token.equalsIgnoreCase(COMMAND)) {
							if ((value & SWT.COMMAND) != 0)
								return 0;

							value |= SWT.COMMAND;
						} else
							return 0;
					} else if (!MODIFIER_SEPARATOR.equals(token))
						return 0;
				}				
			}				
		}

		return value;
	}

	private static int[] readDeprecatedSequence(IMemento memento) {
		if (memento == null)
			throw new IllegalArgumentException();
			
		IMemento[] mementos = memento.getChildren("stroke"); //$NON-NLS-1$ 

		if (mementos == null)
			throw new IllegalArgumentException();
		
		int[] strokes = new int[mementos.length];
		
		for (int i = 0; i < mementos.length; i++)
			strokes[i] = readDeprecatedStroke(mementos[i]);
		
		return strokes;
	}
	
	private static int readDeprecatedStroke(IMemento memento) {
		if (memento == null)
			throw new IllegalArgumentException();

		Integer value = memento.getInteger("value"); //$NON-NLS-1$
		return value != null ? value.intValue() : 0;
	}

	// TODO deprecated end

	private Persistence() {
		super();
	}	
}
