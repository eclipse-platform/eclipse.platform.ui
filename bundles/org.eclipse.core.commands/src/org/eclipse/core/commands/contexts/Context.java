/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.core.commands.contexts;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.commands.common.NamedHandleObject;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.internal.commands.util.Util;

/**
 * <p>
 * A context is an answer to the question "when". Other services can listen for
 * the activation and deactivation of contexts, and change their own state in
 * response to these changes. For example, Eclipse's key binding service listens
 * to context activation and deactivation to determine which key bindings should
 * be active.
 * </p>
 * <p>
 * An instance of this interface can be obtained from an instance of
 * <code>ContextManager</code> for any identifier, whether or not an context
 * with that identifier is defined in the extension registry.
 * </p>
 * <p>
 * The handle-based nature of this API allows it to work well with runtime
 * plugin activation and deactivation. If a context is defined, that means that
 * its corresponding plug-in is active. If the plug-in is then deactivated, the
 * context will still exist but it will be undefined. An attempts to use an
 * undefined context will result in a <code>NotDefinedException</code> being
 * thrown.
 * </p>
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 * 
 * @since 3.1
 * @see ContextManager
 */
public final class Context extends NamedHandleObject implements Comparable {

    /**
     * The collection of all objects listening to changes on this context. This
     * value is <code>null</code> if there are no listeners.
     */
    private Set listeners = null;

    /**
     * The parent identifier for this context. The meaning of a parent is
     * dependent on the system using contexts. This value can be
     * <code>null</code> if the context has no parent.
     */
    private String parentId = null;

    /**
     * Constructs a new instance of <code>Context</code>.
     * 
     * @param id
     *            The id for this context; must not be <code>null</code>.
     */
    Context(final String id) {
        super(id);
    }

    /**
     * Registers an instance of <code>IContextListener</code> to listen for
     * changes to properties of this instance.
     * 
     * @param listener
     *            the instance to register. Must not be <code>null</code>. If
     *            an attempt is made to register an instance which is already
     *            registered with this instance, no operation is performed.
     */
    public final void addContextListener(final IContextListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        }

        if (listeners == null) {
            listeners = new HashSet();
        }

        listeners.add(listener);
    } 
    
    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public final int compareTo(final Object object) {
        final Context scheme = (Context) object;
        int compareTo = Util.compare(this.id, scheme.id);
        if (compareTo == 0) {
            compareTo = Util.compare(this.name, scheme.name);
            if (compareTo == 0) {
                compareTo = Util.compare(this.parentId, scheme.parentId);
                if (compareTo == 0) {
                    compareTo = Util.compare(this.description,
                            scheme.description);
                    if (compareTo == 0) {
                        compareTo = Util.compare(this.defined, scheme.defined);
                    }
                }
            }
        }

        return compareTo;
    }

    /**
     * <p>
     * Defines this context by giving it a name, and possibly a description and
     * a parent identifier as well. The defined property automatically becomes
     * <code>true</code>.
     * </p>
     * <p>
     * Notification is sent to all listeners that something has changed.
     * </p>
     * 
     * @param name
     *            The name of this context; must not be <code>null</code>.
     * @param description
     *            The description for this context; may be <code>null</code>.
     * @param parentId
     *            The parent identifier for this context; may be
     *            <code>null</code>.
     */
    public final void define(final String name, final String description,
            final String parentId) {
        if (name == null) {
            throw new NullPointerException(
                    "The name of a scheme cannot be null"); //$NON-NLS-1$
        }

        final boolean definedChanged = !this.defined;
        this.defined = true;

        final boolean nameChanged = !Util.equals(this.name, name);
        this.name = name;

        final boolean descriptionChanged = !Util.equals(this.description,
                description);
        this.description = description;

        final boolean parentIdChanged = !Util.equals(this.parentId, parentId);
        this.parentId = parentId;

        fireContextChanged(new ContextEvent(this, definedChanged, nameChanged,
                descriptionChanged, parentIdChanged));
    }

    /**
     * Notifies all listeners that this context has changed. This sends the
     * given event to all of the listeners, if any.
     * 
     * @param event
     *            The event to send to the listeners; must not be
     *            <code>null</code>.
     */
    private final void fireContextChanged(final ContextEvent event) {
        if (event == null) {
            throw new NullPointerException(
                    "Cannot send a null event to listeners."); //$NON-NLS-1$
        }

        if (listeners == null) {
            return;
        }

        final Iterator listenerItr = listeners.iterator();
        while (listenerItr.hasNext()) {
            final IContextListener listener = (IContextListener) listenerItr
                    .next();
            listener.contextChanged(event);
        }
    }

    /**
     * Returns the identifier of the parent of this instance.
     * <p>
     * Notification is sent to all registered listeners if this property
     * changes.
     * </p>
     * 
     * @return the identifier of the parent of this instance. May be
     *         <code>null</code>.
     * @throws NotDefinedException
     *             if this instance is not defined.
     */
    public final String getParentId() throws NotDefinedException {
        if (!defined) {
            throw new NotDefinedException(
                    "Cannot get the parent identifier from an undefined context. " //$NON-NLS-1$
            		+id);
        }

        return parentId;
    }

    /**
     * Unregisters an instance of <code>IContextListener</code> listening for
     * changes to properties of this instance.
     * 
     * @param contextListener
     *            the instance to unregister. Must not be <code>null</code>.
     *            If an attempt is made to unregister an instance which is not
     *            already registered with this instance, no operation is
     *            performed.
     */
    public final void removeContextListener(
            final IContextListener contextListener) {
        if (contextListener == null) {
            throw new NullPointerException("Cannot remove a null listener."); //$NON-NLS-1$
        }

        if (listeners == null) {
            return;
        }

        listeners.remove(contextListener);

        if (listeners.isEmpty()) {
            listeners = null;
        }
    }

    /**
     * The string representation of this context -- for debugging purposes only.
     * This string should not be shown to an end user.
     * 
     * @return The string representation; never <code>null</code>.
     */
    public final String toString() {
        if (string == null) {
            final StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("Context("); //$NON-NLS-1$
            stringBuffer.append(id);
            stringBuffer.append(',');
            stringBuffer.append(name);
            stringBuffer.append(',');
            stringBuffer.append(description);
            stringBuffer.append(',');
            stringBuffer.append(parentId);
            stringBuffer.append(',');
            stringBuffer.append(defined);
            stringBuffer.append(')');
            string = stringBuffer.toString();
        }
        return string;
    }

    /**
     * Makes this context become undefined. This has the side effect of changing
     * the name, description and parent identifier to <code>null</code>.
     * Notification is sent to all listeners.
     */
    public final void undefine() {
        string = null;

        final boolean definedChanged = defined;
        defined = false;

        final boolean nameChanged = name != null;
        name = null;

        final boolean descriptionChanged = description != null;
        description = null;

        final boolean parentIdChanged = parentId != null;
        parentId = null;

        fireContextChanged(new ContextEvent(this, definedChanged, nameChanged,
                descriptionChanged, parentIdChanged));
    }
}
