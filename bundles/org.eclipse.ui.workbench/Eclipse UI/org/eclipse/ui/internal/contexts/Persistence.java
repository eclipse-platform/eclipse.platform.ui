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

package org.eclipse.ui.internal.contexts;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ui.IMemento;
import org.eclipse.ui.internal.util.Util;

final class Persistence {

    final static String PACKAGE_BASE = "contexts"; //$NON-NLS-1$

    final static String PACKAGE_FULL = "org.eclipse.ui.contexts"; //$NON-NLS-1$

    final static String PACKAGE_PREFIX = "org.eclipse.ui"; //$NON-NLS-1$		

    final static String TAG_CONTEXT = "context"; //$NON-NLS-1$			

    final static String TAG_ID = "id"; //$NON-NLS-1$

    final static String TAG_NAME = "name"; //$NON-NLS-1$		

    /**
     * Equivalent to <code>TAG_PARENT_ID</code>.
     * 
     * @deprecated This is needed for deprecation support for the "scope"
     *             elements in the "commands" extension point.
     */
    final static String TAG_PARENT = "parent"; //$NON-NLS-1$

    final static String TAG_PARENT_ID = "parentId"; //$NON-NLS-1$

    /**
     * Equivalent to <code>TAG_PARENT_ID</code>.
     * 
     * @deprecated This is needed for deprecation support for
     *             "acceleratorScopes".
     */
    final static String TAG_PARENT_SCOPE = "parentScope"; //$NON-NLS-1$

    final static String TAG_SOURCE_ID = "sourceId"; //$NON-NLS-1$

    static ContextDefinition readContextDefinition(IMemento memento,
            String sourceIdOverride) {
        if (memento == null)
            throw new NullPointerException();

        String id = memento.getString(TAG_ID);
        String name = memento.getString(TAG_NAME);
        String parentId = memento.getString(TAG_PARENT_ID);
        String sourceId = sourceIdOverride != null ? sourceIdOverride : memento
                .getString(TAG_SOURCE_ID);

        /* TODO DEPRECATED Support for the old "commands" extension point way
         * of specifying parents, and for the old "acceleratorScopes" extension
         * point way of specifying parents.
         */
        if (parentId == null) {
            // "acceleratorScopes" support
            parentId = memento.getString(TAG_PARENT_SCOPE);
        }
        if (parentId == null) {
            // "commands" support
            parentId = memento.getString(TAG_PARENT);
        }
        // TODO DEPRECATED END

        return new ContextDefinition(id, name, parentId, sourceId);
    }

    static List readContextDefinitions(IMemento memento, String name,
            String sourceIdOverride) {
        if (memento == null || name == null)
            throw new NullPointerException();

        IMemento[] mementos = memento.getChildren(name);

        if (mementos == null)
            throw new NullPointerException();

        List list = new ArrayList(mementos.length);

        for (int i = 0; i < mementos.length; i++)
            list.add(readContextDefinition(mementos[i], sourceIdOverride));

        return list;
    }

    static void writeContextDefinition(IMemento memento,
            ContextDefinition contextDefinition) {
        if (memento == null || contextDefinition == null)
            throw new NullPointerException();

        memento.putString(TAG_ID, contextDefinition.getId());
        memento.putString(TAG_NAME, contextDefinition.getName());
        memento.putString(TAG_PARENT_ID, contextDefinition.getParentId());
        memento.putString(TAG_SOURCE_ID, contextDefinition.getSourceId());
    }

    static void writeContextDefinitions(IMemento memento, String name,
            List contextDefinitions) {
        if (memento == null || name == null || contextDefinitions == null)
            throw new NullPointerException();

        contextDefinitions = new ArrayList(contextDefinitions);
        Iterator iterator = contextDefinitions.iterator();

        while (iterator.hasNext())
            Util.assertInstance(iterator.next(), ContextDefinition.class);

        iterator = contextDefinitions.iterator();

        while (iterator.hasNext())
            writeContextDefinition(memento.createChild(name),
                    (ContextDefinition) iterator.next());
    }

    /**
     * Constructs a new instance of <code>Persistence</code>. This class
     * should never be instantiated.
     */
    private Persistence() {
        // This class should not be instantiated.
    }
}