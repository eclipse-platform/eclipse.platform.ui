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
import org.eclipse.ui.internal.util.Util;

final class Persistence {

	final static String PACKAGE_BASE = "commands"; //$NON-NLS-1$
	final static String PACKAGE_FULL = "org.eclipse.ui." + PACKAGE_BASE; //$NON-NLS-1$
	final static String TAG_COMMAND = "command"; //$NON-NLS-1$	
	final static String TAG_COMMAND_ID = "commandId"; //$NON-NLS-1$	
	final static String TAG_CONTEXT_BINDING = "contextBinding"; //$NON-NLS-1$	
	final static String TAG_CONTEXT_ID = "contextId"; //$NON-NLS-1$	
	final static String TAG_DESCRIPTION = "description"; //$NON-NLS-1$
	final static String TAG_ID = "id"; //$NON-NLS-1$
	final static String TAG_IMAGE_BINDING = "imageBinding"; //$NON-NLS-1$	
	final static String TAG_IMAGE_STYLE = "imageStyle"; //$NON-NLS-1$	
	final static String TAG_IMAGE_URI = "imageUri"; //$NON-NLS-1$	
	final static String TAG_LOCALE = "locale"; //$NON-NLS-1$	
	final static String TAG_NAME = "name"; //$NON-NLS-1$	
	final static String TAG_PARENT_ID = "parentId"; //$NON-NLS-1$
	final static String TAG_PLATFORM = "platform"; //$NON-NLS-1$	
	final static String TAG_PLUGIN_ID = "pluginId"; //$NON-NLS-1$

	static CommandElement readCommandElement(IMemento memento, String pluginIdOverride)
		throws IllegalArgumentException {
		if (memento == null)
			throw new IllegalArgumentException();			

		String description = memento.getString(TAG_DESCRIPTION);
		String id = memento.getString(TAG_ID);

		if (id == null)
			id = Util.ZERO_LENGTH_STRING;
		
		String name = memento.getString(TAG_NAME);

		if (name == null)
			name = Util.ZERO_LENGTH_STRING;
		
		String parentId = memento.getString(TAG_PARENT_ID);
		String pluginId = pluginIdOverride != null ? pluginIdOverride : memento.getString(TAG_PLUGIN_ID);
		return CommandElement.create(description, id, name, parentId, pluginId);
	}

	static List readCommandElements(IMemento memento, String name, String pluginIdOverride)
		throws IllegalArgumentException {		
		if (memento == null || name == null)
			throw new IllegalArgumentException();			
	
		IMemento[] mementos = memento.getChildren(name);
	
		if (mementos == null)
			throw new IllegalArgumentException();
	
		List list = new ArrayList(mementos.length);
	
		for (int i = 0; i < mementos.length; i++)
			list.add(readCommandElement(mementos[i], pluginIdOverride));
	
		return list;				
	}

	static ContextBindingElement readContextBindingElement(IMemento memento, String pluginIdOverride)
		throws IllegalArgumentException {
		if (memento == null)
			throw new IllegalArgumentException();			

		String commandId = memento.getString(TAG_COMMAND_ID);

		if (commandId == null)
			commandId = Util.ZERO_LENGTH_STRING;

		String contextId = memento.getString(TAG_CONTEXT_ID);

		if (contextId == null)
			contextId = Util.ZERO_LENGTH_STRING;

		String pluginId = pluginIdOverride != null ? pluginIdOverride : memento.getString(TAG_PLUGIN_ID);
		return ContextBindingElement.create(commandId, contextId, pluginId);
	}

	static List readContextBindingElements(IMemento memento, String name, String pluginIdOverride)
		throws IllegalArgumentException {		
		if (memento == null || name == null)
			throw new IllegalArgumentException();			
	
		IMemento[] mementos = memento.getChildren(name);
	
		if (mementos == null)
			throw new IllegalArgumentException();
	
		List list = new ArrayList(mementos.length);
	
		for (int i = 0; i < mementos.length; i++)
			list.add(readContextBindingElement(mementos[i], pluginIdOverride));
	
		return list;				
	}

	static ImageBindingElement readImageBindingElement(IMemento memento, String pluginIdOverride)
		throws IllegalArgumentException {
		if (memento == null)
			throw new IllegalArgumentException();			

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
		return ImageBindingElement.create(commandId, imageStyle, imageUri, locale, platform, pluginId);
	}

	static List readImageBindingElements(IMemento memento, String name, String pluginIdOverride)
		throws IllegalArgumentException {		
		if (memento == null || name == null)
			throw new IllegalArgumentException();			
	
		IMemento[] mementos = memento.getChildren(name);
	
		if (mementos == null)
			throw new IllegalArgumentException();
	
		List list = new ArrayList(mementos.length);
	
		for (int i = 0; i < mementos.length; i++)
			list.add(readImageBindingElement(mementos[i], pluginIdOverride));
	
		return list;				
	}

	static void writeCommandElement(IMemento memento, CommandElement commandElement)
		throws IllegalArgumentException {
		if (memento == null || commandElement == null)
			throw new IllegalArgumentException();

		memento.putString(TAG_DESCRIPTION, commandElement.getDescription());
		memento.putString(TAG_ID, commandElement.getId());
		memento.putString(TAG_NAME, commandElement.getName());
		memento.putString(TAG_PARENT_ID, commandElement.getParentId());
		memento.putString(TAG_PLUGIN_ID, commandElement.getPluginId());
	}

	static void writeCommandElements(IMemento memento, String name, List commandElements)
		throws IllegalArgumentException {
		if (memento == null || name == null || commandElements == null)
			throw new IllegalArgumentException();
		
		commandElements = new ArrayList(commandElements);
		Iterator iterator = commandElements.iterator();
		
		while (iterator.hasNext()) 
			if (!(iterator.next() instanceof CommandElement))
				throw new IllegalArgumentException();

		iterator = commandElements.iterator();

		while (iterator.hasNext()) 
			writeCommandElement(memento.createChild(name), (CommandElement) iterator.next());
	}

	static void writeContextBindingElement(IMemento memento, ContextBindingElement contextBindingElement)
		throws IllegalArgumentException {
		if (memento == null || contextBindingElement == null)
			throw new IllegalArgumentException();

		memento.putString(TAG_COMMAND_ID, contextBindingElement.getCommandId());
		memento.putString(TAG_CONTEXT_ID, contextBindingElement.getContextId());
		memento.putString(TAG_PLUGIN_ID, contextBindingElement.getPluginId());
	}

	static void writeContextBindingElements(IMemento memento, String name, List contextBindingElements)
		throws IllegalArgumentException {
		if (memento == null || name == null || contextBindingElements == null)
			throw new IllegalArgumentException();
		
		contextBindingElements = new ArrayList(contextBindingElements);
		Iterator iterator = contextBindingElements.iterator();
		
		while (iterator.hasNext()) 
			if (!(iterator.next() instanceof ContextBindingElement))
				throw new IllegalArgumentException();

		iterator = contextBindingElements.iterator();

		while (iterator.hasNext()) 
			writeContextBindingElement(memento.createChild(name), (ContextBindingElement) iterator.next());
	}

	static void writeImageBindingElement(IMemento memento, ImageBindingElement imageBindingElement)
		throws IllegalArgumentException {
		if (memento == null || imageBindingElement == null)
			throw new IllegalArgumentException();

		memento.putString(TAG_COMMAND_ID, imageBindingElement.getCommandId());
		memento.putString(TAG_IMAGE_STYLE, imageBindingElement.getImageStyle());
		memento.putString(TAG_IMAGE_URI, imageBindingElement.getImageUri());
		memento.putString(TAG_LOCALE, imageBindingElement.getLocale());
		memento.putString(TAG_PLATFORM, imageBindingElement.getPlatform());
		memento.putString(TAG_PLUGIN_ID, imageBindingElement.getPluginId());
	}

	static void writeImageBindingElements(IMemento memento, String name, List imageBindingElements)
		throws IllegalArgumentException {
		if (memento == null || name == null || imageBindingElements == null)
			throw new IllegalArgumentException();
		
		imageBindingElements = new ArrayList(imageBindingElements);
		Iterator iterator = imageBindingElements.iterator();
		
		while (iterator.hasNext()) 
			if (!(iterator.next() instanceof ImageBindingElement))
				throw new IllegalArgumentException();

		iterator = imageBindingElements.iterator();

		while (iterator.hasNext()) 
			writeImageBindingElement(memento.createChild(name), (ImageBindingElement) iterator.next());
	}

	private Persistence() {
		super();
	}	
}
