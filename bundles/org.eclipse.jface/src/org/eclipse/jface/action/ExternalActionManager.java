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

package org.eclipse.jface.action;

import org.eclipse.jface.util.IPropertyChangeListener;

/**
 * <p>
 * A manager for a callback facility which is capable of querying external
 * interfaces for additional information about actions and action contribution
 * items. This information typically includes things like accelerators and
 * textual representations.
 * </p>
 * <p>
 * For example, in the Eclipse workbench, this mechanism is used to allow the
 * command architecture to override certain values in action contribution items.
 * </p>
 * <p>
 * This class is not intended to be called or extended by any external clients.
 * This API is still under flux, and is expected to change in 3.1.
 * </p>
 * 
 * @since 3.0
 */
public final class ExternalActionManager {

    /**
     * A callback mechanism for some external tool to communicate extra
     * information to actions and action contribution items.
     * 
     * @since 3.0
     */
    public static interface ICallback {

        /**
         * <p>
         * Adds a listener to the object referenced by <code>identifier</code>.
         * This listener will be notified if a property of the item is to be
         * changed. This identifier is specific to mechanism being used. In the
         * case of the Eclipse workbench, this is the command identifier.
         * </p>
         * <p>
         * A single instance of the listener may only ever be associated with
         * one identifier. Attempts to add the listener twice (without a removal
         * inbetween) has undefined behaviour.
         * </p>
         * 
         * @param identifier
         *            The identifier of the item to which the listener should be
         *            attached; must not be <code>null</code>.
         * @param listener
         *            The listener to be added; must not be <code>null</code>.
         */
        public void addPropertyChangeListener(String identifier,
                IPropertyChangeListener listener);

        /**
         * An accessor for the accelerator associated with the item indicated by
         * the identifier. This identifier is specific to mechanism being used.
         * In the case of the Eclipse workbench, this is the command identifier.
         * 
         * @param identifier
         *            The identifier of the item from which the accelerator
         *            should be obtained ; must not be <code>null</code>.
         * @return An integer representation of the accelerator. This is the
         *         same accelerator format used by SWT.
         */
        public Integer getAccelerator(String identifier);

        /**
         * An accessor for the accelerator text associated with the item
         * indicated by the identifier. This identifier is specific to mechanism
         * being used. In the case of the Eclipse workbench, this is the command
         * identifier.
         * 
         * @param identifier
         *            The identifier of the item from which the accelerator text
         *            should be obtained ; must not be <code>null</code>.
         * @return A string representation of the accelerator. This is the
         *         string representation that should be displayed to the user.
         */
        public String getAcceleratorText(String identifier);

        /**
         * Checks to see whether the given accelerator is being used by some
         * other mechanism (outside of the menus controlled by JFace). This is
         * used to keep JFace from trying to grab accelerators away from someone
         * else.
         * 
         * @param accelerator
         *            The accelerator to check -- in SWT's internal accelerator
         *            format.
         * @return <code>true</code> if the accelerator is already being used
         *         and shouldn't be used again; <code>false</code> otherwise.
         */
        public boolean isAcceleratorInUse(int accelerator);

        /**
         * Checks whether the item matching this identifier is active. This is
         * used to decide whether a contribution item with this identifier
         * should be made visible. An inactive item is not visible.
         * 
         * @param identifier
         *            The identifier of the item from which the active state
         *            should be retrieved; must not be <code>null</code>.
         * @return <code>true</code> if the item is active; <code>false</code>
         *         otherwise.
         */
        public boolean isActive(String identifier);

        /**
         * Removes a listener from the object referenced by
         * <code>identifier</code>. This identifier is specific to mechanism
         * being used. In the case of the Eclipse workbench, this is the command
         * identifier.
         * 
         * @param identifier
         *            The identifier of the item to from the listener should be
         *            removed; must not be <code>null</code>.
         * @param listener
         *            The listener to be removed; must not be <code>null</code>.
         */
        public void removePropertyChangeListener(String identifier,
                IPropertyChangeListener listener);
    }

    /**
     * The singleton instance of this class. This value may be <code>null</code>--
     * if it has not yet been initialized.
     */
    private static ExternalActionManager instance;

    /**
     * Retrieves the current singleton instance of this class.
     * 
     * @return The singleton instance; this value is never <code>null</code>.
     */
    public static ExternalActionManager getInstance() {
        if (instance == null) instance = new ExternalActionManager();

        return instance;
    }

    /**
     * The callback mechanism to use to retrieve extra information.
     */
    private ICallback callback;

    /**
     * Constructs a new instance of <code>ExternalActionManager</code>.
     */
    private ExternalActionManager() {
        // This is a singleton class. Only this class should create an instance.
    }

    /**
     * An accessor for the current call back.
     * 
     * @return The current callback mechanism being used. This is the callback
     *         that should be queried for extra information about actions and
     *         action contribution items. This value may be <code>null</code>
     *         if there is no extra information.
     */
    public ICallback getCallback() {
        return callback;
    }

    /**
     * A mutator for the current call back
     * 
     * @param callbackToUse
     *            The new callback mechanism to use; this value may be
     *            <code>null</code> if the default is acceptable (i.e., no
     *            extra information will provided to actions).
     */
    public void setCallback(ICallback callbackToUse) {
        callback = callbackToUse;
    }
}
