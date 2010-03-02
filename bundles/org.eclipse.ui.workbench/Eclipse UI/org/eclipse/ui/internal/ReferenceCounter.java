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
package org.eclipse.ui.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A ReferenceCounter is used to reference counting objects.
 * Each object is identified by a unique ID.  Together they form
 * an ID - value pair. An object is added to the counter by calling
 * #put(id, object).  From this point on additional refs can be made
 * by calling #addRef(id) or #removeRef(id).
 */
public class ReferenceCounter {
    private Map mapIdToRec = new HashMap(11);

    /**
     * Capture the information about an object.
     */
    public class RefRec {
        public RefRec(Object id, Object value) {
            this.id = id;
            this.value = value;
            addRef();
        }

        public Object getId() {
            return id;
        }

        public Object getValue() {
            return value;
        }

        public int addRef() {
            ++refCount;
            return refCount;
        }

        public int removeRef() {
            --refCount;
            return refCount;
        }

        public int getRef() {
            return refCount;
        }

        public boolean isNotReferenced() {
            return (refCount <= 0);
        }

        public Object id;

        public Object value;

        private int refCount;
    }

    /**
     * Creates a new counter.
     */
    public ReferenceCounter() {
        super();
    }

    /**
     * Adds one reference to an object in the counter.
     *
     * @param id is a unique ID for the object.
     * @return the new ref count
     */
    public int addRef(Object id) {
        RefRec rec = (RefRec) mapIdToRec.get(id);
        if (rec == null) {
			return 0;
		}
        return rec.addRef();
    }

    /**
     * Returns the object defined by an ID.  If the ID is not
     * found <code>null</code> is returned.
     *
     * @return the object or <code>null</code>
     */
    public Object get(Object id) {
        RefRec rec = (RefRec) mapIdToRec.get(id);
        if (rec == null) {
			return null;
		}
        return rec.getValue();
    }

    /**
     * Returns a complete list of the keys in the counter.
     *
     * @return a Set containing the ID for each.
     */
    public Set keySet() {
        return mapIdToRec.keySet();
    }

    /**
     * Adds an object to the counter for counting and gives
     * it an initial ref count of 1.
     *
     * @param id is a unique ID for the object.
     * @param value is the object itself.
     */
    public void put(Object id, Object value) {
        RefRec rec = new RefRec(id, value);
        mapIdToRec.put(id, rec);
    }

    /**
     * @param id is a unique ID for the object.
     * @return the current ref count
     */
    public int getRef(Object id) {
        RefRec rec = (RefRec) mapIdToRec.get(id);
        if (rec == null) {
			return 0;
		}
        return rec.refCount;
    }

    /**
     * Removes one reference from an object in the counter.
     * If the ref count drops to 0 the object is removed from
     * the counter completely.
     *
     * @param id is a unique ID for the object.
     * @return the new ref count
     */
    public int removeRef(Object id) {
    	RefRec rec = (RefRec) mapIdToRec.get(id);
    	if (rec == null) {
    		return 0;
    	}
    	int newCount = rec.removeRef();
    	if (newCount <= 0) {
    		mapIdToRec.remove(id);
    	}
    	return newCount;
    }
    
    /**
     * Returns a complete list of the values in the counter.
     *
     * @return a Collection containing the values.
     */
    public List values() {
        int size = mapIdToRec.size();
        ArrayList list = new ArrayList(size);
        Iterator iter = mapIdToRec.values().iterator();
        while (iter.hasNext()) {
            RefRec rec = (RefRec) iter.next();
            list.add(rec.getValue());
        }
        return list;
    }
}
