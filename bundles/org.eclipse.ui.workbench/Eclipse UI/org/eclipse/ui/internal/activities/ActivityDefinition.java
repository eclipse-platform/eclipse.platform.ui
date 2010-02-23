/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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

import org.eclipse.core.expressions.Expression;
import org.eclipse.ui.internal.util.Util;

public final class ActivityDefinition implements Comparable {
    private final static int HASH_FACTOR = 89;

    private final static int HASH_INITIAL = ActivityDefinition.class.getName()
            .hashCode();

    static Map activityDefinitionsById(Collection activityDefinitions,
            boolean allowNullIds) {
        if (activityDefinitions == null) {
			throw new NullPointerException();
		}

        Map map = new HashMap();
        Iterator iterator = activityDefinitions.iterator();

        while (iterator.hasNext()) {
            Object object = iterator.next();
            Util.assertInstance(object, ActivityDefinition.class);
            ActivityDefinition activityDefinition = (ActivityDefinition) object;
            String id = activityDefinition.getId();

            if (allowNullIds || id != null) {
				map.put(id, activityDefinition);
			}
        }

        return map;
    }

    static Map activityDefinitionsByName(Collection activityDefinitions,
            boolean allowNullNames) {
        if (activityDefinitions == null) {
			throw new NullPointerException();
		}

        Map map = new HashMap();
        Iterator iterator = activityDefinitions.iterator();

        while (iterator.hasNext()) {
            Object object = iterator.next();
            Util.assertInstance(object, ActivityDefinition.class);
            ActivityDefinition activityDefinition = (ActivityDefinition) object;
            String name = activityDefinition.getName();

            if (allowNullNames || name != null) {
                Collection activityDefinitions2 = (Collection) map.get(name);

                if (activityDefinitions2 == null) {
                    activityDefinitions2 = new HashSet();
                    map.put(name, activityDefinitions2);
                }

                activityDefinitions2.add(activityDefinition);
            }
        }

        return map;
    }

    private transient int hashCode = HASH_INITIAL;

    private String id;

    private String name;

    private String sourceId;

    private String description;

    private transient String string;

	private Expression enabledWhen;

    public ActivityDefinition(String id, String name, String sourceId,
            String description) {
        this.id = id;
        this.name = name;
        this.sourceId = sourceId;
        this.description = description;
    }

    public int compareTo(Object object) {
        ActivityDefinition castedObject = (ActivityDefinition) object;
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
        if (!(object instanceof ActivityDefinition)) {
			return false;
		}

        final ActivityDefinition castedObject = (ActivityDefinition) object;
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
    
    void setEnabledWhen(Expression expression) {
    	enabledWhen = expression;
    }
    
    public Expression getEnabledWhen() {
    	return enabledWhen;
    }
}
