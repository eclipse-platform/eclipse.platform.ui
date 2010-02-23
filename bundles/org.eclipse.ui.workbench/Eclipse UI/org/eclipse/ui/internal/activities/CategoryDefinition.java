/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.activities;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.ui.internal.util.Util;

public final class CategoryDefinition implements Comparable {
    private final static int HASH_FACTOR = 89;

    private final static int HASH_INITIAL = CategoryDefinition.class.getName()
            .hashCode();

    static Map categoryDefinitionsById(Collection categoryDefinitions,
            boolean allowNullIds) {
        if (categoryDefinitions == null) {
			throw new NullPointerException();
		}

        Map map = new HashMap();
        Iterator iterator = categoryDefinitions.iterator();

        while (iterator.hasNext()) {
            Object object = iterator.next();
            Util.assertInstance(object, CategoryDefinition.class);
            CategoryDefinition categoryDefinition = (CategoryDefinition) object;
            String id = categoryDefinition.getId();

            if (allowNullIds || id != null) {
				map.put(id, categoryDefinition);
			}
        }

        return map;
    }

    static Map categoryDefinitionsByName(Collection categoryDefinitions,
            boolean allowNullNames) {
        if (categoryDefinitions == null) {
			throw new NullPointerException();
		}

        Map map = new HashMap();
        Iterator iterator = categoryDefinitions.iterator();

        while (iterator.hasNext()) {
            Object object = iterator.next();
            Util.assertInstance(object, CategoryDefinition.class);
            CategoryDefinition categoryDefinition = (CategoryDefinition) object;
            String name = categoryDefinition.getName();

            if (allowNullNames || name != null) {
                Collection categoryDefinitions2 = (Collection) map.get(name);

                if (categoryDefinitions2 == null) {
                    categoryDefinitions2 = new HashSet();
                    map.put(name, categoryDefinitions2);
                }

                categoryDefinitions2.add(categoryDefinition);
            }
        }

        return map;
    }

    private transient int hashCode = HASH_INITIAL;

    private String id;

    private String name;

    private String sourceId;

    private transient String string;

    private String description;

    public CategoryDefinition(String id, String name, String sourceId,
            String description) {
        this.id = id;
        this.name = name;
        this.sourceId = sourceId;
        this.description = description;
    }

    public int compareTo(Object object) {
        CategoryDefinition castedObject = (CategoryDefinition) object;
        int compareTo = Util.compare(id, castedObject.id);

        if (compareTo == 0) {
            compareTo = Util.compare(name, castedObject.name);

            if (compareTo == 0) {
				compareTo = Util.compare(sourceId, castedObject.sourceId);
			}
        }

        return compareTo;
    }

    public boolean equals(Object object) {
        if (!(object instanceof CategoryDefinition)) {
			return false;
		}

        final CategoryDefinition castedObject = (CategoryDefinition) object;
        if (!Util.equals(id, castedObject.id)) {
            return false;
        }
        
        if (!Util.equals(name, castedObject.name)) {
            return false;
        }
        
        return Util.equals(sourceId, castedObject.sourceId);
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
        if (hashCode == HASH_INITIAL) {
            hashCode = hashCode * HASH_FACTOR + Util.hashCode(id);
            hashCode = hashCode * HASH_FACTOR + Util.hashCode(name);
            hashCode = hashCode * HASH_FACTOR + Util.hashCode(sourceId);
            if (hashCode == HASH_INITIAL) {
				hashCode++;
			}
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
            stringBuffer.append(sourceId);
            stringBuffer.append(']');
            string = stringBuffer.toString();
        }

        return string;
    }

    public String getDescription() {
        return description;
    }
}
