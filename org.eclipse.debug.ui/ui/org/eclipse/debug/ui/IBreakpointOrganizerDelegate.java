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
package org.eclipse.debug.ui;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.jface.util.IPropertyChangeListener;

/**
 * A breakpoint organizer is used to categorize breakpoints and provides
 * change notification when categorization has changed. Categories are represented
 * as arbitrary adaptable objects. For example, projects could be used to
 * categorize breakpoints. Images and labels for categories are generated
 * via workbench adapaters.
 * <p>
 * Organizers may optionally support breakpoint recategorization. 
 * </p>
 * <p>
 * TODO: plug-in XML
 * </p>
 * <p>
 * Clients contributing a breakpoint organizer are intended to implement
 * this interface.
 * </p>
 * @since 3.1
 */
public interface IBreakpointOrganizerDelegate {
    
    /**
     * Change event id when a category's breakpoints have changed.
     * The <code>oldValue</code> of the <code>PropertyChangeEvent</code> will be the
     * category that has changed, and the source of the event will the the
     * breakpoint organizer. Breakpoints in the category will be
     * recategorized when this event is fired.
     *
     * @see IPropertyChangeListener
     */
    public static final String P_CATEGORY_CHANGED = DebugUIPlugin.getUniqueIdentifier() + ".P_CATEGORY_CHANGED"; //$NON-NLS-1$

    /**
     * Returns objects representing the categories of the specified
     * breakpoint or <code>null</code> if this organizer cannot classify
     * the breakpoint. Categories must return <code>true</code> when sent
     * the message <code>equals(Object)</code> with an equivalent category
     * as an argument.
     * 
     * @param breakpoint breakpoint to classify
     * @return categories of the given breakpoint or <code>null</code>
     */
    public IAdaptable[] getCategories(IBreakpoint breakpoint);
    
    /**
     * Adds the specified listener. Has no effect if an identical listener is
     * already registered.
     * 
     * @param listener listener to add
     */
    public void addPropertyChangeListener(IPropertyChangeListener listener);
    
    /**
     * Removes the specified listener. Has no effect if an identical listener
     * is not already registered.
     * 
     * @param listener listener to remove
     */
    public void removePropertyChangeListener(IPropertyChangeListener listener);
    
    /**
     * Adds the specified breakpoint to the given category. Only called
     * if <code>canAdd(...)</code> returns <code>true</code> for the given
     * breakpoint and category.
     * 
     * @param breakpoint breakpoint to recategorize
     * @param category the breakpoint's new category
     */
    public void addBreakpoint(IBreakpoint breakpoint, IAdaptable category);
    
    /**
     * Removes the specified breakpoint from the given category. Only
     * called if <code>canRemove(...)</code> returns <code>true</code> for
     * the givem breakpoint and category.
     * 
     * @param breakpoint breakpoint to recategorize
     * @param category the category the breakpoint is remove from
     */
    public void removeBreakpoint(IBreakpoint breakpoint, IAdaptable category);
    
    /**
     * Returns whether the given breakpoint can be categorized in the
     * specified category.
     *  
     * @param breakpoint breakpoint to recatogorize
     * @param category the category to add the breakpoint to
     * @return whether the given breakpoint can be categorized in the
     * specified category
     */
    public boolean canAdd(IBreakpoint breakpoint, IAdaptable category);
    
    /**
     * Returns whether the given breakpoint can be removed from the given
     * category.
     * 
     * @param breakpoint berakpoint to recategorize
     * @param category the category to remove the breakpoint from
     * @return whether the given breakpoint can be removed from the given
     * category
     */
    public boolean canRemove(IBreakpoint breakpoint, IAdaptable category);
    
    /**
     * Returns any empty categories owned by this organizer, or <code>null</code>
     * if none. This allows the breakpoints view to display empty categories
     * that breakpoints can be recategorized into.
     *  
     * @return empty categories owned by this organizer, or <code>null</code>
     */
    public IAdaptable[] getEmptyCategories();
    
    /**
     * Disposes this breakpoint organizer.
     */
    public void dispose();
    
}
