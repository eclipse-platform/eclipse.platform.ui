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

package org.eclipse.ui.contexts;

import java.util.Set;

import org.eclipse.ui.internal.util.Util;

/**
 * An instance of this class describes changes to an instance of
 * <code>IContextManager</code>.
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 * 
 * @since 3.0
 * @see IContextManagerListener#contextManagerChanged(ContextManagerEvent)
 * @deprecated Please use the "org.eclipse.core.commands" plug-in instead.
 * @see org.eclipse.core.commands.contexts.ContextManagerEvent
 */
public final class ContextManagerEvent {

    /**
     * The context manager which has changed. This value is never
     * <code>null</code>.
     */
    private final IContextManager contextManager;

    /**
     * Whether the set of defined contexts has changed.
     */
    private final boolean definedContextIdsChanged;

    /**
     * Whether the set of enabled contexts has changed.
     */
    private final boolean enabledContextIdsChanged;

    /**
     * The set of context identifiers (strings) that were defined before the
     * change occurred. If the defined contexts did not changed, then this value
     * is <code>null</code>.
     */
    private final Set previouslyDefinedContextIds;

    /**
     * The set of context identifiers (strings) that were enabled before the
     * change occurred. If the enabled contexts did not changed, then this value
     * is <code>null</code>.
     */
    private final Set previouslyEnabledContextIds;

    /**
     * Creates a new instance of this class.
     * 
     * @param contextManager
     *            the instance of the interface that changed.
     * @param definedContextIdsChanged
     *            true, iff the definedContextIds property changed.
     * @param enabledContextIdsChanged
     *            true, iff the enabledContextIds property changed.
     * @param previouslyDefinedContextIds
     *            the set of identifiers to previously defined contexts. This
     *            set may be empty. If this set is not empty, it must only
     *            contain instances of <code>String</code>. This set must be
     *            <code>null</code> if definedContextIdsChanged is
     *            <code>false</code> and must not be null if
     *            definedContextIdsChanged is <code>true</code>.
     * @param previouslyEnabledContextIds
     *            the set of identifiers to previously enabled contexts. This
     *            set may be empty. If this set is not empty, it must only
     *            contain instances of <code>String</code>. This set must be
     *            <code>null</code> if enabledContextIdsChanged is
     *            <code>false</code> and must not be null if
     *            enabledContextIdsChanged is <code>true</code>.
     */
    public ContextManagerEvent(IContextManager contextManager,
            boolean definedContextIdsChanged, boolean enabledContextIdsChanged,
            Set previouslyDefinedContextIds, Set previouslyEnabledContextIds) {
        if (contextManager == null) {
			throw new NullPointerException();
		}

        if (!definedContextIdsChanged && previouslyDefinedContextIds != null) {
			throw new IllegalArgumentException();
		}

        if (!enabledContextIdsChanged && previouslyEnabledContextIds != null) {
			throw new IllegalArgumentException();
		}

        if (definedContextIdsChanged) {
            this.previouslyDefinedContextIds = Util.safeCopy(
                    previouslyDefinedContextIds, String.class);
        } else {
            this.previouslyDefinedContextIds = null;
        }

        if (enabledContextIdsChanged) {
            this.previouslyEnabledContextIds = Util.safeCopy(
                    previouslyEnabledContextIds, String.class);
        } else {
            this.previouslyEnabledContextIds = null;
        }

        this.contextManager = contextManager;
        this.definedContextIdsChanged = definedContextIdsChanged;
        this.enabledContextIdsChanged = enabledContextIdsChanged;
    }

    /**
     * Returns the instance of the interface that changed.
     * 
     * @return the instance of the interface that changed. Guaranteed not to be
     *         <code>null</code>.
     */
    public IContextManager getContextManager() {
        return contextManager;
    }

    /**
     * Returns the set of identifiers to previously defined contexts.
     * 
     * @return the set of identifiers to previously defined contexts. This set
     *         may be empty. If this set is not empty, it is guaranteed to only
     *         contain instances of <code>String</code>. This set is
     *         guaranteed to be <code>null</code> if
     *         haveDefinedContextIdsChanged() is <code>false</code> and is
     *         guaranteed to not be null if haveDefinedContextIdsChanged() is
     *         <code>true</code>.
     */
    public Set getPreviouslyDefinedContextIds() {
        return previouslyDefinedContextIds;
    }

    /**
     * Returns the set of identifiers to previously enabled contexts.
     * 
     * @return the set of identifiers to previously enabled contexts. This set
     *         may be empty. If this set is not empty, it is guaranteed to only
     *         contain instances of <code>String</code>. This set is
     *         guaranteed to be <code>null</code> if
     *         haveEnabledContextIdsChanged() is <code>false</code> and is
     *         guaranteed to not be null if haveEnabledContextIdsChanged() is
     *         <code>true</code>.
     */
    public Set getPreviouslyEnabledContextIds() {
        return previouslyEnabledContextIds;
    }

    /**
     * Returns whether or not the definedContextIds property changed.
     * 
     * @return true, iff the definedContextIds property changed.
     */
    public boolean haveDefinedContextIdsChanged() {
        return definedContextIdsChanged;
    }

    /**
     * Returns whether or not the enabledContextIds property changed.
     * 
     * @return true, iff the enabledContextIds property changed.
     */
    public boolean haveEnabledContextIdsChanged() {
        return enabledContextIdsChanged;
    }
}
