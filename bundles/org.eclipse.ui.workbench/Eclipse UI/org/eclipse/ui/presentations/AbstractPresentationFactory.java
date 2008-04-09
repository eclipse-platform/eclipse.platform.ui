/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matthew Hatem Matthew_Hatem@notesdev.ibm.com Bug 189953
 *******************************************************************************/
package org.eclipse.ui.presentations;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Sash;

/**
 * This is a factory for presentation objects that control the appearance of
 * editors, views and other components in the workbench.
 * 
 * @since 3.0
 */
public abstract class AbstractPresentationFactory {
	
	/**
	 * Bit value for the createSash method's 'style' parameter.
	 * @since 3.4
	 */
	public static int SASHTYPE_NORMAL = 0;
	/**
	 * Bit value for the createSash method's 'style' parameter.
	 * @since 3.4
	 */
	public static int SASHTYPE_FLOATING = 1<<1;
	/**
	 * Bit value for the createSash method's 'style' parameter.
	 * @since 3.4
	 */
	public static int SASHORIENTATION_HORIZONTAL = SWT.HORIZONTAL; // 1<<8
	/**
	 * Bit value for the createSash method's 'style' parameter.
	 * @since 3.4
	 */
	public static int SASHORIENTATION_VERTICAL = SWT.VERTICAL; // 1<<9
	
	private static final int SASH_SIZE = 3;

    /**
     * Creates an editor presentation for presenting editors.
     * <p>
     * The presentation creates its controls under the given parent composite.
     * </p>
     * 
     * @param parent
     *            the parent composite to use for the presentation's controls
     * @param site
     *            the site used for communication between the presentation and
     *            the workbench
     * @return a newly created part presentation
     */
    public abstract StackPresentation createEditorPresentation(
            Composite parent, IStackPresentationSite site);

    /**
     * Creates a stack presentation for presenting regular docked views.
     * <p>
     * The presentation creates its controls under the given parent composite.
     * </p>
     * 
     * @param parent
     *            the parent composite to use for the presentation's controls
     * @param site
     *            the site used for communication between the presentation and
     *            the workbench
     * @return a newly created part presentation
     */
    public abstract StackPresentation createViewPresentation(Composite parent,
            IStackPresentationSite site);

    /**
     * Creates a standalone stack presentation for presenting a standalone view.
     * A standalone view cannot be docked together with other views. The title
     * of a standalone view may be hidden.
     * <p>
     * The presentation creates its controls under the given parent composite.
     * </p>
     * 
     * @param parent
     *            the parent composite to use for the presentation's controls
     * @param site
     *            the site used for communication between the presentation and
     *            the workbench
     * @param showTitle
     *            <code>true</code> to show the title for the view,
     *            <code>false</code> to hide it
     * @return a newly created part presentation
     */
    public abstract StackPresentation createStandaloneViewPresentation(
            Composite parent, IStackPresentationSite site, boolean showTitle);

    /**
     * Creates the status line manager for the window.
     * Subclasses may override.
     * 
     * @return the window's status line manager
     */
    public IStatusLineManager createStatusLineManager() {
        return new StatusLineManager();
    }

    /**
     * Creates the control for the window's status line.
     * Subclasses may override.
     * 
     * @param statusLine the window's status line manager
     * @param parent the parent composite
     * @return the window's status line control
     */
    public Control createStatusLineControl(IStatusLineManager statusLine,
            Composite parent) {
        return ((StatusLineManager) statusLine).createControl(parent, SWT.NONE);
    }
    
    /**
     * Returns a globally unique identifier for this type of presentation factory. This is used
     * to ensure that one presentation is not restored from mementos saved by a different
     * presentation.
     * 
     * @return a globally unique identifier for this type of presentation factory.
     */
    public String getId() {
        return this.getClass().getName();
    }
    
    /**
     * Creates the Sash control that is used to separate view and editor parts.
     * 
     * @param parent the parent composite
     * @param style A bit set giving both the 'type' of the desired sash and
     * its orientation (i.e. one 'SASHTYPE' value and one "SASHORIENTATION" value). 
     * @return the sash control
     * @since 3.4
     */
    public Sash createSash(Composite parent, int style) {
    	int swtOrientation = style & (SASHORIENTATION_HORIZONTAL|SASHORIENTATION_VERTICAL);
    	Sash sash = new Sash(parent, swtOrientation | SWT.SMOOTH);
        return sash;
    }
    
    /**
     * Returns the size of the Sash control that is used to separate view and editor parts.
     * 
     * @param style A bit set giving both the 'type' of the desired sash and
     * its orientation.
     * @return the sash size
     * @since 3.4
     */
    public int getSashSize(int style) {
    	return SASH_SIZE;
    }
}
