/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.commands;

import java.util.Set;

import org.eclipse.ui.internal.util.Util;

/**
 * An instance of this class describes changes to an instance of <code>ICommand</code>.
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 * 
 * @since 3.0
 * @see ICommandListener#commandChanged
 */
public final class CommandEvent {

    private Set attributeNames;

    private boolean categoryIdChanged;

    private ICommand command;

    private boolean contextBindingsChanged;

    private boolean definedAttributeNamesChanged;

    private boolean definedChanged;

    private boolean descriptionChanged;

    private boolean imageBindingsChanged;

    private boolean keySequenceBindingsChanged;

    private boolean nameChanged;

    /**
     * Creates a new instance of this class.
     * 
     * @param command
     *            the instance of the interface that changed.
     * @param attributeNames
     *            the set of names of attributes whose values have changed.
     *            This set may be empty, but it must not be <code>null</code>.
     *            If this set is not empty, it must only contain instances of
     *            <code>String</code>.
     * @param categoryIdChanged
     *            true, iff the categoryId property changed.
     * @param contextBindingsChanged
     *            true, iff the contextBindings property changed.
     * @param definedAttributeNamesChanged
     *            true, iff the definedAttributeNames property changed.
     * @param definedChanged
     *            true, iff the defined property changed.
     * @param descriptionChanged
     *            true, iff the description property changed.
     * @param imageBindingsChanged
     *            true, iff the imageBindings property changed.
     * @param keySequenceBindingsChanged
     *            true, iff the keySequenceBindings property changed.
     * @param nameChanged
     *            true, iff the name property changed.
     */
    public CommandEvent(ICommand command, Set attributeNames,
            boolean categoryIdChanged, boolean contextBindingsChanged,
            boolean definedAttributeNamesChanged, boolean definedChanged,
            boolean descriptionChanged, boolean imageBindingsChanged,
            boolean keySequenceBindingsChanged, boolean nameChanged) {
        if (command == null) throw new NullPointerException();

        this.command = command;
        this.attributeNames = Util.safeCopy(attributeNames, String.class);
        this.categoryIdChanged = categoryIdChanged;
        this.contextBindingsChanged = contextBindingsChanged;
        this.definedAttributeNamesChanged = definedAttributeNamesChanged;
        this.definedChanged = definedChanged;
        this.descriptionChanged = descriptionChanged;
        this.imageBindingsChanged = imageBindingsChanged;
        this.keySequenceBindingsChanged = keySequenceBindingsChanged;
        this.nameChanged = nameChanged;
    }

    /**
     * Returns the set of names of attributes whose values have changed.
     * <p>
     * Notification is sent to all registered listeners if this property
     * changes.
     * </p>
     * 
     * @return the set of names of attributes whose values have changed. This
     *         set may be empty, but is guaranteed not to be <code>null</code>.
     *         If this set is not empty, it is guaranteed to only contain
     *         instances of <code>String</code>.
     */
    public Set getAttributeNames() {
        return attributeNames;
    }

    /**
     * Returns the instance of the interface that changed.
     * 
     * @return the instance of the interface that changed. Guaranteed not to be
     *         <code>null</code>.
     */
    public ICommand getCommand() {
        return command;
    }

    /**
     * Returns whether or not the categoryId property changed.
     * 
     * @return true, iff the categoryId property changed.
     */
    public boolean hasCategoryIdChanged() {
        return categoryIdChanged;
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
     * Returns whether or not the description property changed.
     * 
     * @return true, iff the description property changed.
     */
    public boolean hasDescriptionChanged() {
        return descriptionChanged;
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
     * Returns whether or not the contextBindings property changed.
     * 
     * @return true, iff the contextBindings property changed.
     */
    public boolean haveContextBindingsChanged() {
        return contextBindingsChanged;
    }

    /**
     * Returns whether or not the definedAttributeNames property changed.
     * 
     * @return true, iff the definedAttributeNames property changed.
     */
    public boolean haveDefinedAttributeNamesChanged() {
        return definedAttributeNamesChanged;
    }

    /**
     * Returns whether or not the imageBindings property changed.
     * 
     * @return true, iff the imageBindings property changed.
     */
    public boolean haveImageBindingsChanged() {
        return imageBindingsChanged;
    }

    /**
     * Returns whether or not the keySequenceBindings property changed.
     * 
     * @return true, iff the keySequenceBindings property changed.
     */
    public boolean haveKeySequenceBindingsChanged() {
        return keySequenceBindingsChanged;
    }
}
