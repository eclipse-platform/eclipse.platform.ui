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
 * An instance of this class describes changes to an instance of
 * <code>ICommand</code>.
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 * 
 * @since 3.0
 * @see ICommandListener#commandChanged
 */
public final class CommandEvent {

    private boolean categoryIdChanged;

    private ICommand command;

    private boolean contextBindingsChanged;

    private boolean definedAttributeNamesChanged;

    private boolean definedChanged;

    private boolean descriptionChanged;

    private boolean imageBindingsChanged;

    private boolean keySequenceBindingsChanged;

    private boolean nameChanged;

    private Set previouslyDefinedAttributeNames;

    /**
     * Creates a new instance of this class.
     * 
     * @param command
     *            the instance of the interface that changed.
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
     * @param previouslyAttributeNames
     *            the set of previously defined attribute names. This set may be
     *            empty. If this set is not empty, it must only contain
     *            instances of <code>String</code>. This set must be
     *            <code>null</code> if definedAttributeNamesChanged is
     *            <code>false</code> and must not be null if
     *            definedAttributeNamesChanged is <code>true</code>.
     */
    public CommandEvent(ICommand command, boolean categoryIdChanged,
            boolean contextBindingsChanged,
            boolean definedAttributeNamesChanged, boolean definedChanged,
            boolean descriptionChanged, boolean imageBindingsChanged,
            boolean keySequenceBindingsChanged, boolean nameChanged,
            Set previouslyDefinedAttributeNames) {
        if (command == null) throw new NullPointerException();

        if (!definedAttributeNamesChanged
                && previouslyDefinedAttributeNames != null)
                throw new IllegalArgumentException();

        if (definedAttributeNamesChanged)
                this.previouslyDefinedAttributeNames = Util.safeCopy(
                        previouslyDefinedAttributeNames, String.class);

        this.command = command;
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
     * Returns the instance of the interface that changed.
     * 
     * @return the instance of the interface that changed. Guaranteed not to be
     *         <code>null</code>.
     */
    public ICommand getCommand() {
        return command;
    }

    /**
     * Returns the set of previously defined attribute names.
     * 
     * @return the set of previously defined attribute names. This set may be
     *         empty. If this set is not empty, it is guaranteed to only contain
     *         instances of <code>String</code>. This set is guaranteed to be
     *         <code>null</code> if haveDefinedAttributeNamesChanged() is
     *         <code>false</code> and is guaranteed to not be null if
     *         haveDefinedAttributeNamesChanged() is <code>true</code>.
     */
    public Set getPreviouslyDefinedAttributeNames() {
        return previouslyDefinedAttributeNames;
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