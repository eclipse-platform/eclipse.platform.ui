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
import org.eclipse.ui.commands.IActiveKeyConfigurationDefinition;
import org.eclipse.ui.commands.ICategoryDefinition;
import org.eclipse.ui.commands.ICommandDefinition;
import org.eclipse.ui.commands.IContextBindingDefinition;
import org.eclipse.ui.commands.IImageBindingDefinition;
import org.eclipse.ui.commands.IKeyBindingDefinition;
import org.eclipse.ui.commands.IKeyConfigurationDefinition;
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

	static IActiveKeyConfigurationDefinition readActiveKeyConfigurationDefinition(IMemento memento, String pluginIdOverride) {
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
		
		return new ActiveKeyConfigurationDefinition(keyConfigurationId, pluginId);
	}

	static List readActiveKeyConfigurationDefinitions(IMemento memento, String name, String pluginIdOverride) {
		if (memento == null || name == null)
			throw new NullPointerException();			
	
		IMemento[] mementos = memento.getChildren(name);
	
		if (mementos == null)
			throw new NullPointerException();
	
		List list = new ArrayList(mementos.length);
	
		for (int i = 0; i < mementos.length; i++)
			list.add(readActiveKeyConfigurationDefinition(mementos[i], pluginIdOverride));
	
		return list;				
	}

	static ICategoryDefinition readCategoryDefinition(IMemento memento, String pluginIdOverride) {
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
		
		return new CategoryDefinition(description, id, name, pluginId);
	}

	static List readCategoryDefinitions(IMemento memento, String name, String pluginIdOverride) {
		if (memento == null || name == null)
			throw new NullPointerException();			
	
		IMemento[] mementos = memento.getChildren(name);
	
		if (mementos == null)
			throw new NullPointerException();
	
		List list = new ArrayList(mementos.length);
	
		for (int i = 0; i < mementos.length; i++)
			list.add(readCategoryDefinition(mementos[i], pluginIdOverride));
	
		return list;				
	}

	static ICommandDefinition readCommandDefinition(IMemento memento, String pluginIdOverride) {
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
		
		return new CommandDefinition(categoryId, description, id, name, pluginId);
	}

	static List readCommandDefinitions(IMemento memento, String name, String pluginIdOverride) {
		if (memento == null || name == null)
			throw new NullPointerException();			
	
		IMemento[] mementos = memento.getChildren(name);
	
		if (mementos == null)
			throw new NullPointerException();
	
		List list = new ArrayList(mementos.length);
	
		for (int i = 0; i < mementos.length; i++)
			list.add(readCommandDefinition(mementos[i], pluginIdOverride));
	
		return list;				
	}

	static IContextBindingDefinition readContextBindingDefinition(IMemento memento, String pluginIdOverride) {
		if (memento == null)
			throw new NullPointerException();			

		String commandId = memento.getString(TAG_COMMAND_ID);

		if (commandId == null)
			commandId = Util.ZERO_LENGTH_STRING;

		String contextId = memento.getString(TAG_CONTEXT_ID);

		if (contextId == null)
			contextId = Util.ZERO_LENGTH_STRING;

		String pluginId = pluginIdOverride != null ? pluginIdOverride : memento.getString(TAG_PLUGIN_ID);
		return new ContextBindingDefinition(commandId, contextId, pluginId);
	}

	static List readContextBindingDefinitions(IMemento memento, String name, String pluginIdOverride) {
		if (memento == null || name == null)
			throw new NullPointerException();			
	
		IMemento[] mementos = memento.getChildren(name);
	
		if (mementos == null)
			throw new NullPointerException();
	
		List list = new ArrayList(mementos.length);
	
		for (int i = 0; i < mementos.length; i++)
			list.add(readContextBindingDefinition(mementos[i], pluginIdOverride));
	
		return list;				
	}

	static IImageBindingDefinition readImageBindingDefinition(IMemento memento, String pluginIdOverride) {
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
		return new ImageBindingDefinition(commandId, imageStyle, imageUri, locale, platform, pluginId);
	}

	static List readImageBindingDefinitions(IMemento memento, String name, String pluginIdOverride) {
		if (memento == null || name == null)
			throw new NullPointerException();			
	
		IMemento[] mementos = memento.getChildren(name);
	
		if (mementos == null)
			throw new NullPointerException();
	
		List list = new ArrayList(mementos.length);
	
		for (int i = 0; i < mementos.length; i++)
			list.add(readImageBindingDefinition(mementos[i], pluginIdOverride));
	
		return list;				
	}

	static IKeyBindingDefinition readKeyBindingDefinition(IMemento memento, String pluginIdOverride, int rank) {
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
		
		return new KeyBindingDefinition(commandId, contextId, keyConfigurationId, keySequence, locale, platform, pluginId, rank);
	}

	static List readKeyBindingDefinitions(IMemento memento, String name, String pluginIdOverride, int rank) {
		if (memento == null || name == null)
			throw new NullPointerException();			
	
		IMemento[] mementos = memento.getChildren(name);
	
		if (mementos == null)
			throw new NullPointerException();
	
		List list = new ArrayList(mementos.length);
	
		for (int i = 0; i < mementos.length; i++)
			list.add(readKeyBindingDefinition(mementos[i], pluginIdOverride, rank));
	
		return list;				
	}

	static IKeyConfigurationDefinition readKeyConfigurationDefinition(IMemento memento, String pluginIdOverride) {
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
		
		return new KeyConfigurationDefinition(description, id, name, parentId, pluginId);
	}

	static List readKeyConfigurationDefinitions(IMemento memento, String name, String pluginIdOverride) {
		if (memento == null || name == null)
			throw new NullPointerException();			
	
		IMemento[] mementos = memento.getChildren(name);
	
		if (mementos == null)
			throw new NullPointerException();
	
		List list = new ArrayList(mementos.length);
	
		for (int i = 0; i < mementos.length; i++)
			list.add(readKeyConfigurationDefinition(mementos[i], pluginIdOverride));
	
		return list;				
	}

	static void writeActiveKeyConfigurationDefinition(IMemento memento, IActiveKeyConfigurationDefinition activeKeyConfigurationDefinition) {
		if (memento == null || activeKeyConfigurationDefinition == null)
			throw new NullPointerException();

		memento.putString(TAG_KEY_CONFIGURATION_ID, activeKeyConfigurationDefinition.getKeyConfigurationId());
		memento.putString(TAG_PLUGIN_ID, activeKeyConfigurationDefinition.getPluginId());
	}

	static void writeActiveKeyConfigurationDefinitions(IMemento memento, String name, List activeKeyConfigurationDefinitions) {
		if (memento == null || name == null || activeKeyConfigurationDefinitions == null)
			throw new NullPointerException();
		
		activeKeyConfigurationDefinitions = new ArrayList(activeKeyConfigurationDefinitions);
		Iterator iterator = activeKeyConfigurationDefinitions.iterator();

		while (iterator.hasNext())
			Util.assertInstance(iterator.next(), IActiveKeyConfigurationDefinition.class);

		iterator = activeKeyConfigurationDefinitions.iterator();

		while (iterator.hasNext()) 
			writeActiveKeyConfigurationDefinition(memento.createChild(name), (IActiveKeyConfigurationDefinition) iterator.next());
	}

	static void writeCategoryDefinition(IMemento memento, ICategoryDefinition categoryDefinition) {
		if (memento == null || categoryDefinition == null)
			throw new NullPointerException();

		memento.putString(TAG_DESCRIPTION, categoryDefinition.getDescription());
		memento.putString(TAG_ID, categoryDefinition.getId());
		memento.putString(TAG_NAME, categoryDefinition.getName());
		memento.putString(TAG_PLUGIN_ID, categoryDefinition.getPluginId());
	}

	static void writeCategoryDefinitions(IMemento memento, String name, List categoryDefinitions) {
		if (memento == null || name == null || categoryDefinitions == null)
			throw new NullPointerException();
		
		categoryDefinitions = new ArrayList(categoryDefinitions);
		Iterator iterator = categoryDefinitions.iterator();

		while (iterator.hasNext())
			Util.assertInstance(iterator.next(), ICategoryDefinition.class);

		iterator = categoryDefinitions.iterator();

		while (iterator.hasNext()) 
			writeCategoryDefinition(memento.createChild(name), (ICategoryDefinition) iterator.next());
	}

	static void writeCommandDefinition(IMemento memento, ICommandDefinition commandDefinition) {
		if (memento == null || commandDefinition == null)
			throw new NullPointerException();

		memento.putString(TAG_CATEGORY_ID, commandDefinition.getCategoryId());
		memento.putString(TAG_DESCRIPTION, commandDefinition.getDescription());
		memento.putString(TAG_ID, commandDefinition.getId());
		memento.putString(TAG_NAME, commandDefinition.getName());
		memento.putString(TAG_PLUGIN_ID, commandDefinition.getPluginId());
	}

	static void writeCommandDefinitions(IMemento memento, String name, List commandDefinitions) {
		if (memento == null || name == null || commandDefinitions == null)
			throw new NullPointerException();
		
		commandDefinitions = new ArrayList(commandDefinitions);
		Iterator iterator = commandDefinitions.iterator();

		while (iterator.hasNext())
			Util.assertInstance(iterator.next(), ICommandDefinition.class);

		iterator = commandDefinitions.iterator();

		while (iterator.hasNext()) 
			writeCommandDefinition(memento.createChild(name), (ICommandDefinition) iterator.next());
	}
	
	static void writeContextBindingDefinition(IMemento memento, IContextBindingDefinition contextBindingDefinition) {
		if (memento == null || contextBindingDefinition == null)
			throw new NullPointerException();

		memento.putString(TAG_COMMAND_ID, contextBindingDefinition.getCommandId());
		memento.putString(TAG_CONTEXT_ID, contextBindingDefinition.getContextId());
		memento.putString(TAG_PLUGIN_ID, contextBindingDefinition.getPluginId());
	}

	static void writeContextBindingDefinitions(IMemento memento, String name, List contextBindingDefinitions) {
		if (memento == null || name == null || contextBindingDefinitions == null)
			throw new NullPointerException();
		
		contextBindingDefinitions = new ArrayList(contextBindingDefinitions);
		Iterator iterator = contextBindingDefinitions.iterator();

		while (iterator.hasNext())
			Util.assertInstance(iterator.next(), IContextBindingDefinition.class);

		iterator = contextBindingDefinitions.iterator();

		while (iterator.hasNext()) 
			writeContextBindingDefinition(memento.createChild(name), (IContextBindingDefinition) iterator.next());
	}
	
	static void writeImageBindingDefinition(IMemento memento, IImageBindingDefinition imageBindingDefinition) {
		if (memento == null || imageBindingDefinition == null)
			throw new NullPointerException();

		memento.putString(TAG_COMMAND_ID, imageBindingDefinition.getCommandId());
		memento.putString(TAG_IMAGE_STYLE, imageBindingDefinition.getImageStyle());
		memento.putString(TAG_IMAGE_URI, imageBindingDefinition.getImageUri());
		memento.putString(TAG_LOCALE, imageBindingDefinition.getLocale());
		memento.putString(TAG_PLATFORM, imageBindingDefinition.getPlatform());
		memento.putString(TAG_PLUGIN_ID, imageBindingDefinition.getPluginId());
	}

	static void writeImageBindingDefinitions(IMemento memento, String name, List imageBindingDefinitions) {
		if (memento == null || name == null || imageBindingDefinitions == null)
			throw new NullPointerException();
		
		imageBindingDefinitions = new ArrayList(imageBindingDefinitions);
		Iterator iterator = imageBindingDefinitions.iterator();

		while (iterator.hasNext())
			Util.assertInstance(iterator.next(), IImageBindingDefinition.class);

		iterator = imageBindingDefinitions.iterator();

		while (iterator.hasNext()) 
			writeImageBindingDefinition(memento.createChild(name), (IImageBindingDefinition) iterator.next());
	}

	static void writeKeyBindingDefinition(IMemento memento, IKeyBindingDefinition keyBindingDefinition) {
		if (memento == null || keyBindingDefinition == null)
			throw new NullPointerException();

		memento.putString(TAG_COMMAND_ID, keyBindingDefinition.getCommandId());
		memento.putString(TAG_CONTEXT_ID, keyBindingDefinition.getContextId());
		memento.putString(TAG_KEY_CONFIGURATION_ID, keyBindingDefinition.getKeyConfigurationId());
		memento.putString(TAG_KEY_SEQUENCE, keyBindingDefinition.getKeySequence().toString());
		memento.putString(TAG_LOCALE, keyBindingDefinition.getLocale());
		memento.putString(TAG_PLATFORM, keyBindingDefinition.getPlatform());
		memento.putString(TAG_PLUGIN_ID, keyBindingDefinition.getPluginId());
	}

	static void writeKeyBindingDefinitions(IMemento memento, String name, List keyBindingDefinitions) {
		if (memento == null || name == null || keyBindingDefinitions == null)
			throw new NullPointerException();
		
		keyBindingDefinitions = new ArrayList(keyBindingDefinitions);
		Iterator iterator = keyBindingDefinitions.iterator();

		while (iterator.hasNext())
			Util.assertInstance(iterator.next(), IKeyBindingDefinition.class);

		iterator = keyBindingDefinitions.iterator();

		while (iterator.hasNext()) 
			writeKeyBindingDefinition(memento.createChild(name), (IKeyBindingDefinition) iterator.next());
	}

	static void writeKeyConfigurationDefinition(IMemento memento, IKeyConfigurationDefinition keyConfigurationDefinition) {
		if (memento == null || keyConfigurationDefinition == null)
			throw new NullPointerException();
	
		memento.putString(TAG_DESCRIPTION, keyConfigurationDefinition.getDescription());
		memento.putString(TAG_ID, keyConfigurationDefinition.getId());
		memento.putString(TAG_NAME, keyConfigurationDefinition.getName());
		memento.putString(TAG_PARENT_ID, keyConfigurationDefinition.getParentId());
		memento.putString(TAG_PLUGIN_ID, keyConfigurationDefinition.getPluginId());
	}
	
	static void writeKeyConfigurationDefinitions(IMemento memento, String name, List keyConfigurationDefinitions) {
		if (memento == null || name == null || keyConfigurationDefinitions == null)
			throw new NullPointerException();
			
		keyConfigurationDefinitions = new ArrayList(keyConfigurationDefinitions);
		Iterator iterator = keyConfigurationDefinitions.iterator();
	
		while (iterator.hasNext())
			Util.assertInstance(iterator.next(), IKeyConfigurationDefinition.class);
	
		iterator = keyConfigurationDefinitions.iterator();
	
		while (iterator.hasNext()) 
			writeKeyConfigurationDefinition(memento.createChild(name), (IKeyConfigurationDefinition) iterator.next());
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
