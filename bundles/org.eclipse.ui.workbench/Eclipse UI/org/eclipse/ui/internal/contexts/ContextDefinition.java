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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.ui.internal.util.Util;

final class ContextDefinition implements Comparable {
    private final static int HASH_FACTOR = 89;

    private final static int HASH_INITIAL = ContextDefinition.class.getName()
            .hashCode();

    static Map contextDefinitionsById(Collection contextDefinitions,
            boolean allowNullIds) {
        if (contextDefinitions == null)
            throw new NullPointerException();

        Map map = new HashMap();
        Iterator iterator = contextDefinitions.iterator();

        while (iterator.hasNext()) {
            Object object = iterator.next();
            Util.assertInstance(object, ContextDefinition.class);
            ContextDefinition contextDefinition = (ContextDefinition) object;
            String id = contextDefinition.getId();

            if (allowNullIds || id != null)
                map.put(id, contextDefinition);
        }

        return map;
    }

    static Map contextDefinitionsByName(Collection contextDefinitions,
            boolean allowNullNames) {
        if (contextDefinitions == null)
            throw new NullPointerException();

        Map map = new HashMap();
        Iterator iterator = contextDefinitions.iterator();

        while (iterator.hasNext()) {
            Object object = iterator.next();
            Util.assertInstance(object, ContextDefinition.class);
            ContextDefinition contextDefinition = (ContextDefinition) object;
            String name = contextDefinition.getName();

            if (allowNullNames || name != null) {
                Collection contextDefinitions2 = (Collection) map.get(name);

                if (contextDefinitions2 == null) {
                    contextDefinitions2 = new HashSet();
                    map.put(name, contextDefinitions2);
                }

                contextDefinitions2.add(contextDefinition);
            }
        }

        return map;
    }

    private transient int hashCode;

    private transient boolean hashCodeComputed;

    private String id;

    private String name;

    private String parentId;

    private String sourceId;

    private transient String string;

    ContextDefinition(String id, String name, String parentId, String sourceId) {
        this.id = id;
        this.name = name;
        this.parentId = parentId;
        this.sourceId = sourceId;
    }

    public int compareTo(Object object) {
        ContextDefinition castedObject = (ContextDefinition) object;
        int compareTo = Util.compare(id, castedObject.id);

        if (compareTo == 0) {
            compareTo = Util.compare(name, castedObject.name);

            if (compareTo == 0) {
                compareTo = Util.compare(parentId, castedObject.parentId);

                if (compareTo == 0)
                    compareTo = Util.compare(sourceId, castedObject.sourceId);
            }
        }

        return compareTo;
    }

    public boolean equals(Object object) {
        if (!(object instanceof ContextDefinition))
            return false;

        ContextDefinition castedObject = (ContextDefinition) object;
        boolean equals = true;
        equals &= Util.equals(id, castedObject.id);
        equals &= Util.equals(name, castedObject.name);
        equals &= Util.equals(parentId, castedObject.parentId);
        equals &= Util.equals(sourceId, castedObject.sourceId);
        return equals;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getParentId() {
        return parentId;
    }

    public String getSourceId() {
        return sourceId;
    }

    public int hashCode() {
        if (!hashCodeComputed) {
            hashCode = HASH_INITIAL;
            hashCode = hashCode * HASH_FACTOR + Util.hashCode(id);
            hashCode = hashCode * HASH_FACTOR + Util.hashCode(name);
            hashCode = hashCode * HASH_FACTOR + Util.hashCode(parentId);
            hashCode = hashCode * HASH_FACTOR + Util.hashCode(sourceId);
            hashCodeComputed = true;
        }

        return hashCode;
    }

    public String toString() {
        if (string == null) {
            final StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append('[');
            stringBuffer.append(id);
            stringBuffer.append(',');
            stringBuffer.append(name);
            stringBuffer.append(',');
            stringBuffer.append(parentId);
            stringBuffer.append(',');
            stringBuffer.append(sourceId);
            stringBuffer.append(']');
            string = stringBuffer.toString();
        }

        return string;
    }
}