/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.commands;

/**
 * An instance of this class describes changes to an instance of
 * <code>IKeyConfiguration</code>.
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 * 
 * @since 3.0
 * @see IKeyConfigurationListener#keyConfigurationChanged
 */
public final class KeyConfigurationEvent {

    private boolean activeChanged;

    private boolean definedChanged;

    private IKeyConfiguration keyConfiguration;

    private boolean nameChanged;

    private boolean parentIdChanged;

    /**
     * Creates a new instance of this class.
     * 
     * @param keyConfiguration
     *            the instance of the interface that changed.
     * @param activeChanged
     *            true, iff the active property changed.
     * @param definedChanged
     *            true, iff the defined property changed.
     * @param nameChanged
     *            true, iff the name property changed.
     * @param parentIdChanged
     *            true, iff the parentId property changed.
     */
    public KeyConfigurationEvent(IKeyConfiguration keyConfiguration,
            boolean activeChanged, boolean definedChanged,
            boolean descriptionChanged, boolean nameChanged,
            boolean parentIdChanged) {
        if (keyConfiguration == null) throw new NullPointerException();

        this.keyConfiguration = keyConfiguration;
        this.activeChanged = activeChanged;
        this.definedChanged = definedChanged;
        this.nameChanged = nameChanged;
        this.parentIdChanged = parentIdChanged;
    }

    /**
     * Returns the instance of the interface that changed.
     * 
     * @return the instance of the interface that changed. Guaranteed not to be
     *         <code>null</code>.
     */
    public IKeyConfiguration getKeyConfiguration() {
        return keyConfiguration;
    }

    /**
     * Returns whether or not the active property changed.
     * 
     * @return true, iff the active property changed.
     */
    public boolean hasActiveChanged() {
        return activeChanged;
    }

    /**
     * Returns whether or not the defined property changed.
     * 
     * @return true, iff the defined property changed.
     */
    public boolean hasDefinedChanged() {
        return definedChanged;
    }

    /**
     * Returns whether or not the name property changed.
     * 
     * @return true, iff the name property changed.
     */
    public boolean hasNameChanged() {
        return nameChanged;
    }

    /**
     * Returns whether or not the parentId property changed.
     * 
     * @return true, iff the parentId property changed.
     */
    public boolean hasParentIdChanged() {
        return parentIdChanged;
    }
}