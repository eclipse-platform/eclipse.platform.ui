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
package org.eclipse.ui.part;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IActionBars;

/**
 * This interface has been replaced by <code>IPageBookViewPage</code>
 * but is preserved for backward compatibility.
 * <p>
 * This class is not intended to be directly implemented by clients; clients
 * should instead subclass <code>Page</code>.
 * </p>
 *
 * @see PageBookView
 * @see Page
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IPage {
    /**
     * Creates the SWT control for this page under the given parent 
     * control.
     * <p>
     * Clients should not call this method (the workbench calls this method when
     * it needs to, which may be never).
     * </p>
     *
     * @param parent the parent control
     */
    public void createControl(Composite parent);

    /**
     * Disposes of this page.
     * <p>
     * This is the last method called on the <code>IPage</code>. Implementors should 
     * clean up any resources associated with the page.
     * </p>
     * Callers of this method should ensure that the page's control (if it exists)
     * has been disposed before calling this method. However, for backward compatibilty,
     * implementors must also ensure that the page's control has been disposed before
     * this method returns.  
     * </p>
     * <p>
     * Note that there is no guarantee that createControl() has been called, 
     * so the control may never have been created.
     * </p>
     */
    public void dispose();

    /**
     * Returns the SWT control for this page.
     *
     * @return the SWT control for this page, or <code>null</code> if this
     *   page does not have a control
     */
    public Control getControl();

    /**
     * Allows the page to make contributions to the given action bars.
     * The contributions will be visible when the page is visible.
     * <p>
     * This method is automatically called shortly after 
     * <code>createControl</code> is called
     * </p>
     *
     * @param actionBars the action bars for this page
     */
    public void setActionBars(IActionBars actionBars);

    /**
     * Asks this page to take focus within its pagebook view.
     */
    public void setFocus();
}
