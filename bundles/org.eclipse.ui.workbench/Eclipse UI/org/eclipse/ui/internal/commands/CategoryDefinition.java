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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.ui.internal.util.Util;

public final class CategoryDefinition implements Comparable {

    private final static int HASH_FACTOR = 89;

    private final static int HASH_INITIAL = CategoryDefinition.class.getName()
            .hashCode();

    public static Map categoryDefinitionsById(Collection categoryDefinitions,
            boolean allowNullIds) {
        if (categoryDefinitions == null)
            throw new NullPointerException();

        Map map = new HashMap();
        Iterator iterator = categoryDefinitions.iterator();

        while (iterator.hasNext()) {
            Object object = iterator.next();
            Util.assertInstance(object, CategoryDefinition.class);
            CategoryDefinition categoryDefinition = (CategoryDefinition) object;
            String id = categoryDefinition.getId();

            if (allowNullIds || id != null)
                map.put(id, categoryDefinition);
        }

        return map;
    }

    public static Map categoryDefinitionsByName(Collection categoryDefinitions,
            boolean allowNullNames) {
        if (categoryDefinitions == null)
            throw new NullPointerException();

        Map map = new HashMap();
        Iterator iterator = categoryDefinitions.iterator();

        while (iterator.hasNext()) {
            Object object = iterator.next();
            Util.assertInstance(object, CategoryDefinition.class);
            CategoryDefinition categoryDefinition = (CategoryDefinition) object;
            String name = categoryDefinition.getName();

            if (allowNullNames || name != null) {
                Set categoryDefinitions2 = (Set) map.get(name);

                if (categoryDefinitions2 == null) {
                    categoryDefinitions2 = new HashSet();
                    map.put(name, categoryDefinitions2);
                }

                categoryDefinitions2.add(categoryDefinition);
            }
        }

        return map;
    }

    private String description;

    private transient int hashCode;

    private transient boolean hashCodeComputed;

    private String id;

    private String name;

    private String sourceId;

    private transient String string;

    public CategoryDefinition(String description, String id, String name,
            String sourceId) {
        this.description = description;
        this.id = id;
        this.name = name;
        this.sourceId = sourceId;
    }

    public int compareTo(Object object) {
        CategoryDefinition castedObject = (CategoryDefinition) object;
        int compareTo = Util.compare(description, castedObject.description);

        if (compareTo == 0) {
            compareTo = Util.compare(id, castedObject.id);

            if (compareTo == 0) {
                compareTo = Util.compare(name, castedObject.name);

                if (compareTo == 0)
                    compareTo = Util.compare(sourceId, castedObject.sourceId);
            }
        }

        return compareTo;
    }

    public boolean equals(Object object) {
        if (!(object instanceof CategoryDefinition))
            return false;

        CategoryDefinition castedObject = (CategoryDefinition) object;
        boolean equals = true;
        equals &= Util.equals(description, castedObject.description);
        equals &= Util.equals(id, castedObject.id);
        equals &= Util.equals(name, castedObject.name);
        equals &= Util.equals(sourceId, castedObject.sourceId);
        return equals;
    }

    public String getDescription() {
        return description;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSourceId() {
        return sourceId;
    }

    public int hashCode() {
        if (!hashCodeComputed) {
            hashCode = HASH_INITIAL;
            hashCode = hashCode * HASH_FACTOR + Util.hashCode(description);
            hashCode = hashCode * HASH_FACTOR + Util.hashCode(id);
            hashCode = hashCode * HASH_FACTOR + Util.hashCode(name);
            hashCode = hashCode * HASH_FACTOR + Util.hashCode(sourceId);
            hashCodeComputed = true;
        }

        return hashCode;
    }

    public String toString() {
        if (string == null) {
            final StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append('[');
            stringBuffer.append(description);
            stringBuffer.append(',');
            stringBuffer.append(id);
            stringBuffer.append(',');
            stringBuffer.append(name);
            stringBuffer.append(',');
            stringBuffer.append(sourceId);
            stringBuffer.append(']');
            string = stringBuffer.toString();
        }

        return string;
    }
}