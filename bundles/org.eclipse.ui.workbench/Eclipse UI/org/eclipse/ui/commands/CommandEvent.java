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

import java.util.Map;

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

    private boolean attributeValuesByNameChanged;

    private boolean categoryIdChanged;

    private ICommand command;

    private boolean contextBindingsChanged;

    private boolean definedChanged;

    private boolean descriptionChanged;

    private boolean handledChanged;

    private boolean imageBindingsChanged;

    private boolean keySequenceBindingsChanged;

    private boolean nameChanged;

    private Map previousAttributeValuesByName;

    /**
     * Creates a new instance of this class.
     * 
     * @param command
     *            the instance of the interface that changed.
     * @param attributeValuesByNameChanged
     *            true, iff the attributeValuesByName property changed.
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
     * @param handledChanged
     *            true, iff the handled property changed.
     * @param imageBindingsChanged
     *            true, iff the imageBindings property changed.
     * @param keySequenceBindingsChanged
     *            true, iff the keySequenceBindings property changed.
     * @param nameChanged
     *            true, iff the name property changed.
     * @param previousAttributeValuesByName
     *            the map of previous attribute values by name. This map may be
     *            empty. If this map is not empty, it's collection of keys must
     *            only contain instances of <code>String</code>. This map
     *            must be <code>null</code> if attributeValuesByNameChanged is
     *            <code>false</code> and must not be null if
     *            attributeValuesByNameChanged is <code>true</code>.
     */
    public CommandEvent(ICommand command, boolean attributeValuesByNameChanged,
            boolean categoryIdChanged, boolean contextBindingsChanged,
            boolean definedChanged, boolean descriptionChanged,
            boolean handledChanged, boolean imageBindingsChanged, boolean keySequenceBindingsChanged,
            boolean nameChanged, Map previousAttributeValuesByName) {
        if (command == null) throw new NullPointerException();

        if (!attributeValuesByNameChanged
                && previousAttributeValuesByName != null)
                throw new IllegalArgumentException();

        if (attributeValuesByNameChanged)
                this.previousAttributeValuesByName = Util.safeCopy(
                        previousAttributeValuesByName, String.class,
                        Object.class, false, true);

        this.command = command;
        this.attributeValuesByNameChanged = attributeValuesByNameChanged;
        this.categoryIdChanged = categoryIdChanged;
        this.contextBindingsChanged = contextBindingsChanged;
        this.definedChanged = definedChanged;
        this.descriptionChanged = descriptionChanged;
        this.handledChanged = handledChanged;
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
     * Returns the map of previous attribute values by name.
     * 
     * @return the map of previous attribute values by name. This map may be
     *         empty. If this map is not empty, it's collection of keys is
     *         guaranteed to only contain instances of <code>String</code>.
     *         This map is guaranteed to be <code>null</code> if
     *         haveAttributeValuesByNameChanged() is <code>false</code> and is
     *         guaranteed to not be null if haveAttributeValuesByNameChanged()
     *         is <code>true</code>.
     */
    public Map getPreviousAttributeValuesByName() {
        return previousAttributeValuesByName;
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
     * Returns whether or not the handled property changed.
     * 
     * @return true, iff the handled property changed.
     */
    public boolean hasHandledChanged() {
        return handledChanged;
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
     * Returns whether or not the attributeValuesByName property changed.
     * 
     * @return true, iff the attributeValuesByName property changed.
     */
    public boolean haveAttributeValuesByNameChanged() {
        return attributeValuesByNameChanged;
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