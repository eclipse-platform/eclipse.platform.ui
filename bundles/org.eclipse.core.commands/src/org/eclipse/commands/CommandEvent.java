/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.commands;

import java.util.Map;

import org.eclipse.commands.misc.Util;

/**
 * An instance of this class describes changes to an instance of
 * <code>ICommand</code>.
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 * 
 * @since 3.1
 * @see ICommandListener#commandChanged(CommandEvent)
 */
public class CommandEvent {

    /**
     * Whether the attributes of the command have changed. These are name and
     * value pairs representing properties of the command.
     */
    private final boolean attributeValuesByNameChanged;

    /**
     * The command that has changed; this value is never <code>null</code>.
     */
    private final Command command;

    /**
     * Whether the defined state of the command has changed.
     */
    private final boolean definedChanged;

    /**
     * Whether the description of the command has changed.
     */
    private final boolean descriptionChanged;

    /**
     * Whether the command has either gained or lost a handler.
     */
    private final boolean handledChanged;

    /**
     * Whether the name of the command has changed.
     */
    private final boolean nameChanged;

    /**
     * The map of attributes before the change. This is a map of attribute name
     * (strings) to values (any object).
     */
    private Map previousAttributeValuesByName;

    /**
     * Creates a new instance of this class.
     * 
     * @param command
     *            the instance of the interface that changed.
     * @param attributeValuesByNameChanged
     *            true, iff the attributeValuesByName property changed.
     * @param definedChanged
     *            true, iff the defined property changed.
     * @param descriptionChanged
     *            true, iff the description property changed.
     * @param handledChanged
     *            true, iff the handled property changed.
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
    public CommandEvent(final Command command,
            final boolean attributeValuesByNameChanged,
            final boolean definedChanged, final boolean descriptionChanged,
            final boolean handledChanged, final boolean nameChanged,
            final Map previousAttributeValuesByName) {
        if (command == null)
            throw new NullPointerException();

        if (!attributeValuesByNameChanged
                && previousAttributeValuesByName != null)
            throw new IllegalArgumentException();

        if (attributeValuesByNameChanged)
            this.previousAttributeValuesByName = Util.safeCopy(
                    previousAttributeValuesByName, String.class, Object.class,
                    false, true);

        this.command = command;
        this.attributeValuesByNameChanged = attributeValuesByNameChanged;
        this.definedChanged = definedChanged;
        this.descriptionChanged = descriptionChanged;
        this.handledChanged = handledChanged;
        this.nameChanged = nameChanged;
    }

    /**
     * Returns the instance of the interface that changed.
     * 
     * @return the instance of the interface that changed. Guaranteed not to be
     *         <code>null</code>.
     */
    public final Command getCommand() {
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
    public final Map getPreviousAttributeValuesByName() {
        return previousAttributeValuesByName;
    }

    /**
     * Returns whether or not the defined property changed.
     * 
     * @return true, iff the defined property changed.
     */
    public final boolean hasDefinedChanged() {
        return definedChanged;
    }

    /**
     * Returns whether or not the description property changed.
     * 
     * @return true, iff the description property changed.
     */
    public final boolean hasDescriptionChanged() {
        return descriptionChanged;
    }

    /**
     * Returns whether or not the handled property changed.
     * 
     * @return true, iff the handled property changed.
     */
    public final boolean hasHandledChanged() {
        return handledChanged;
    }

    /**
     * Returns whether or not the name property changed.
     * 
     * @return true, iff the name property changed.
     */
    public final boolean hasNameChanged() {
        return nameChanged;
    }

    /**
     * Returns whether or not the attributeValuesByName property changed.
     * 
     * @return true, iff the attributeValuesByName property changed.
     */
    public final boolean haveAttributeValuesByNameChanged() {
        return attributeValuesByNameChanged;
    }
}
