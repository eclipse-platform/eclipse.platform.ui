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

package org.eclipse.ui.activities;

/**
 * An instance of this class describes changes to an instance of 
 * <code>ICategory</code>.  This class does not give details as to the 
 * specifics of a change, only that the given property on the source object has 
 * changed.
 * 
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 * 
 * @since 3.0
 * @see ICategoryListener#categoryChanged(CategoryEvent)
 */
public final class CategoryEvent {
    private ICategory category;

    private boolean categoryActivityBindingsChanged;

    private boolean definedChanged;

    private boolean nameChanged;

    private boolean descriptionChanged;

    /**
     * Creates a new instance of this class.
     * 
     * @param category
     *        the instance of the interface that changed.
     * @param categoryActivityBindingsChanged
     *        <code>true</code>, iff the categoryActivityBindings property changed.
     * @param definedChanged
     *        <code>true</code>, iff the defined property changed.
     * @param descriptionChanged
     *        <code>true</code>, iff the description property changed. 
     * @param nameChanged
     *        <code>true</code>, iff the name property changed.
     */
    public CategoryEvent(ICategory category,
            boolean categoryActivityBindingsChanged, boolean definedChanged,
            boolean descriptionChanged, boolean nameChanged) {
        if (category == null) {
			throw new NullPointerException();
		}

        this.category = category;
        this.categoryActivityBindingsChanged = categoryActivityBindingsChanged;
        this.definedChanged = definedChanged;
        this.nameChanged = nameChanged;
        this.descriptionChanged = descriptionChanged;
    }

    /**
     * Returns the instance of the interface that changed.
     * 
     * @return the instance of the interface that changed. Guaranteed not to be
     *         <code>null</code>.
     */
    public ICategory getCategory() {
        return category;
    }

    /**
     * Returns whether or not the defined property changed.
     * 
     * @return <code>true</code>, iff the defined property changed.
     */
    public boolean hasDefinedChanged() {
        return definedChanged;
    }

    /**
     * Returns whether or not the name property changed.
     * 
     * @return <code>true</code>, iff the name property changed.
     */
    public boolean hasNameChanged() {
        return nameChanged;
    }

    /**
     * Returns whether or not the description property changed.
     * 
     * @return <code>true</code>, iff the description property changed.
     */
    public boolean hasDescriptionChanged() {
        return descriptionChanged;
    }

    /**
     * Returns whether or not the categoryActivityBindings property changed.
     * 
     * @return <code>true</code>, iff the categoryActivityBindings property changed.
     */
    public boolean haveCategoryActivityBindingsChanged() {
        return categoryActivityBindingsChanged;
    }
}
