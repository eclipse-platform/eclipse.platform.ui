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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.commands.IHandler;
import org.eclipse.ui.internal.commands.ws.HandlerProxy;
import org.eclipse.ui.internal.util.Util;

final class Persistence {

    final static String PACKAGE_BASE = "commands"; //$NON-NLS-1$

    final static String PACKAGE_PREFIX = "org.eclipse.ui"; //$NON-NLS-1$	

    final static String PACKAGE_FULL = PACKAGE_PREFIX + '.' + PACKAGE_BASE;

    final static String TAG_ACTIVE_KEY_CONFIGURATION = "activeKeyConfiguration"; //$NON-NLS-1$

    final static String TAG_CATEGORY = "category"; //$NON-NLS-1$	

    final static String TAG_CATEGORY_ID = "categoryId"; //$NON-NLS-1$

    final static String TAG_COMMAND = "command"; //$NON-NLS-1$	

    final static String TAG_COMMAND_ID = "commandId"; //$NON-NLS-1$	

    final static String TAG_CONTEXT_ID = "contextId"; //$NON-NLS-1$	

    final static String TAG_DESCRIPTION = "description"; //$NON-NLS-1$

    final static String TAG_HANDLER = "handlerSubmission"; //$NON-NLS-1$

    final static String TAG_ID = "id"; //$NON-NLS-1$

    final static String TAG_KEY_CONFIGURATION = "keyConfiguration"; //$NON-NLS-1$	

    final static String TAG_KEY_CONFIGURATION_ID = "keyConfigurationId"; //$NON-NLS-1$	

    final static String TAG_KEY_SEQUENCE = "keySequence"; //$NON-NLS-1$	

    final static String TAG_KEY_SEQUENCE_BINDING = "keyBinding"; //$NON-NLS-1$

    final static String TAG_LOCALE = "locale"; //$NON-NLS-1$

    final static String TAG_NAME = "name"; //$NON-NLS-1$	

    final static String TAG_PARENT_ID = "parentId"; //$NON-NLS-1$

    final static String TAG_PLATFORM = "platform"; //$NON-NLS-1$	

    final static String TAG_SOURCE_ID = "sourceId"; //$NON-NLS-1$

    static CategoryDefinition readCategoryDefinition(IMemento memento,
            String sourceIdOverride) {
        if (memento == null)
            throw new NullPointerException();

        String description = memento.getString(TAG_DESCRIPTION);
        String id = memento.getString(TAG_ID);
        String name = memento.getString(TAG_NAME);
        String sourceId = sourceIdOverride != null ? sourceIdOverride : memento
                .getString(TAG_SOURCE_ID);

        // TODO deprecated start
        if (sourceIdOverride == null && sourceId == null)
            sourceId = memento.getString("plugin"); //$NON-NLS-1$ 
        // TODO deprecated end

        return new CategoryDefinition(description, id, name, sourceId);
    }

    static List readCategoryDefinitions(IMemento memento, String name,
            String sourceIdOverride) {
        if (memento == null || name == null)
            throw new NullPointerException();

        IMemento[] mementos = memento.getChildren(name);

        if (mementos == null)
            throw new NullPointerException();

        List list = new ArrayList(mementos.length);

        for (int i = 0; i < mementos.length; i++)
            list.add(readCategoryDefinition(mementos[i], sourceIdOverride));

        return list;
    }

    static CommandDefinition readCommandDefinition(IMemento memento,
            String sourceIdOverride) {
        if (memento == null)
            throw new NullPointerException();

        String categoryId = memento.getString(TAG_CATEGORY_ID);

        // TODO deprecated start
        if (categoryId == null)
            categoryId = memento.getString("category"); //$NON-NLS-1$ 
        // TODO deprecated end

        String description = memento.getString(TAG_DESCRIPTION);
        String id = memento.getString(TAG_ID);
        String name = memento.getString(TAG_NAME);
        String sourceId = sourceIdOverride != null ? sourceIdOverride : memento
                .getString(TAG_SOURCE_ID);

        // TODO deprecated start
        if (sourceIdOverride == null && sourceId == null)
            sourceId = memento.getString("plugin"); //$NON-NLS-1$ 
        // TODO deprecated end

        return new CommandDefinition(categoryId, description, id, name,
                sourceId);
    }

    static List readCommandDefinitions(IMemento memento, String name,
            String sourceIdOverride) {
        if (memento == null || name == null)
            throw new NullPointerException();

        IMemento[] mementos = memento.getChildren(name);

        if (mementos == null)
            throw new NullPointerException();

        List list = new ArrayList(mementos.length);

        for (int i = 0; i < mementos.length; i++)
            list.add(readCommandDefinition(mementos[i], sourceIdOverride));

        return list;
    }

    /**
     * Reads the handler from XML, and creates a proxy to contain it. The proxy
     * will only instantiate the handler when the handler is first asked for
     * information.
     * 
     * @param configurationElement
     *            The configuration element to read; must not be
     *            <code>null</code>.
     * @return The handler proxy for the given definition; never
     *         <code>null</code>.
     */
    static IHandler readHandlerSubmissionDefinition(
            IConfigurationElement configurationElement) {
        final String commandId = configurationElement
                .getAttribute(TAG_COMMAND_ID);

        return new HandlerProxy(commandId, configurationElement);
    }

    static void writeCategoryDefinition(IMemento memento,
            CategoryDefinition categoryDefinition) {
        if (memento == null || categoryDefinition == null)
            throw new NullPointerException();

        memento.putString(TAG_DESCRIPTION, categoryDefinition.getDescription());
        memento.putString(TAG_ID, categoryDefinition.getId());
        memento.putString(TAG_NAME, categoryDefinition.getName());
        memento.putString(TAG_SOURCE_ID, categoryDefinition.getSourceId());
    }

    static void writeCategoryDefinitions(IMemento memento, String name,
            List categoryDefinitions) {
        if (memento == null || name == null || categoryDefinitions == null)
            throw new NullPointerException();

        categoryDefinitions = new ArrayList(categoryDefinitions);
        Iterator iterator = categoryDefinitions.iterator();

        while (iterator.hasNext())
            Util.assertInstance(iterator.next(), CategoryDefinition.class);

        iterator = categoryDefinitions.iterator();

        while (iterator.hasNext())
            writeCategoryDefinition(memento.createChild(name),
                    (CategoryDefinition) iterator.next());
    }

    static void writeCommandDefinition(IMemento memento,
            CommandDefinition commandDefinition) {
        if (memento == null || commandDefinition == null)
            throw new NullPointerException();

        memento.putString(TAG_CATEGORY_ID, commandDefinition.getCategoryId());
        memento.putString(TAG_DESCRIPTION, commandDefinition.getDescription());
        memento.putString(TAG_ID, commandDefinition.getId());
        memento.putString(TAG_NAME, commandDefinition.getName());
        memento.putString(TAG_SOURCE_ID, commandDefinition.getSourceId());
    }

    static void writeCommandDefinitions(IMemento memento, String name,
            List commandDefinitions) {
        if (memento == null || name == null || commandDefinitions == null)
            throw new NullPointerException();

        commandDefinitions = new ArrayList(commandDefinitions);
        Iterator iterator = commandDefinitions.iterator();

        while (iterator.hasNext())
            Util.assertInstance(iterator.next(), CommandDefinition.class);

        iterator = commandDefinitions.iterator();

        while (iterator.hasNext())
            writeCommandDefinition(memento.createChild(name),
                    (CommandDefinition) iterator.next());
    }

    private Persistence() {
    }
}