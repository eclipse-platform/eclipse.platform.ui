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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.ui.contexts.ContextEvent;
import org.eclipse.ui.contexts.IContext;
import org.eclipse.ui.contexts.IContextListener;
import org.eclipse.ui.contexts.NotDefinedException;
import org.eclipse.ui.internal.util.Util;

final class Context implements IContext {

    private final static int HASH_FACTOR = 89;

    private final static int HASH_INITIAL = Context.class.getName().hashCode();

    private final static Set strongReferences = new HashSet();

    private List contextListeners;

    private boolean defined;

    private boolean enabled;

    private transient int hashCode;

    private transient boolean hashCodeComputed;

    private String id;

    private String name;

    private String parentId;

    private transient String string;

    Context(String id) {
        if (id == null) throw new NullPointerException();

        this.id = id;
    }

    public void addContextListener(IContextListener contextListener) {
        if (contextListener == null) throw new NullPointerException();

        if (contextListeners == null) contextListeners = new ArrayList();

        if (!contextListeners.contains(contextListener))
                contextListeners.add(contextListener);

        strongReferences.add(this);
    }

    public int compareTo(Object object) {
        Context castedObject = (Context) object;
        int compareTo = Util.compare(defined, castedObject.defined);

        if (compareTo == 0) {
            compareTo = Util.compare(enabled, castedObject.enabled);

            if (compareTo == 0) {
                compareTo = Util.compare(id, castedObject.id);

                if (compareTo == 0) {
                    compareTo = Util.compare(name, castedObject.name);

                    if (compareTo == 0)
                            compareTo = Util.compare(parentId,
                                    castedObject.parentId);
                }
            }
        }

        return compareTo;
    }

    public boolean equals(Object object) {
        if (!(object instanceof Context)) return false;

        Context castedObject = (Context) object;
        boolean equals = true;
        equals &= Util.equals(defined, castedObject.defined);
        equals &= Util.equals(enabled, castedObject.enabled);
        equals &= Util.equals(id, castedObject.id);
        equals &= Util.equals(name, castedObject.name);
        equals &= Util.equals(parentId, castedObject.parentId);
        return equals;
    }

    void fireContextChanged(ContextEvent contextEvent) {
        if (contextEvent == null) throw new NullPointerException();

        if (contextListeners != null)
                for (int i = 0; i < contextListeners.size(); i++)
                    ((IContextListener) contextListeners.get(i))
                            .contextChanged(contextEvent);
    }

    public String getId() {
        return id;
    }

    public String getName() throws NotDefinedException {
        if (!defined)
                throw new NotDefinedException(
                        "Cannot get the name from an undefined context."); //$NON-NLS-1$

        return name;
    }

    public String getParentId() throws NotDefinedException {
        if (!defined)
                throw new NotDefinedException(
                        "Cannot get the parent identifier from an undefined context."); //$NON-NLS-1$

        return parentId;
    }

    public int hashCode() {
        if (!hashCodeComputed) {
            hashCode = HASH_INITIAL;
            hashCode = hashCode * HASH_FACTOR + Util.hashCode(defined);
            hashCode = hashCode * HASH_FACTOR + Util.hashCode(enabled);
            hashCode = hashCode * HASH_FACTOR + Util.hashCode(id);
            hashCode = hashCode * HASH_FACTOR + Util.hashCode(name);
            hashCode = hashCode * HASH_FACTOR + Util.hashCode(parentId);
            hashCodeComputed = true;
        }

        return hashCode;
    }

    public boolean isDefined() {
        return defined;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void removeContextListener(IContextListener contextListener) {
        if (contextListener == null) throw new NullPointerException();

        if (contextListeners != null) contextListeners.remove(contextListener);

        if (contextListeners.isEmpty()) strongReferences.remove(this);
    }

    boolean setDefined(boolean defined) {
        if (defined != this.defined) {
            this.defined = defined;
            hashCodeComputed = false;
            hashCode = 0;
            string = null;
            return true;
        }

        return false;
    }

    boolean setEnabled(boolean enabled) {
        if (enabled != this.enabled) {
            this.enabled = enabled;
            hashCodeComputed = false;
            hashCode = 0;
            string = null;
            return true;
        }

        return false;
    }

    boolean setName(String name) {
        if (!Util.equals(name, this.name)) {
            this.name = name;
            hashCodeComputed = false;
            hashCode = 0;
            string = null;
            return true;
        }

        return false;
    }

    boolean setParentId(String parentId) {
        if (!Util.equals(parentId, this.parentId)) {
            this.parentId = parentId;
            hashCodeComputed = false;
            hashCode = 0;
            string = null;
            return true;
        }

        return false;
    }

    public String toString() {
        if (string == null) {
            final StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append('[');
            stringBuffer.append(defined);
            stringBuffer.append(',');
            stringBuffer.append(enabled);
            stringBuffer.append(',');
            stringBuffer.append(id);
            stringBuffer.append(',');
            stringBuffer.append(name);
            stringBuffer.append(',');
            stringBuffer.append(parentId);
            stringBuffer.append(']');
            string = stringBuffer.toString();
        }

        return string;
    }
}
