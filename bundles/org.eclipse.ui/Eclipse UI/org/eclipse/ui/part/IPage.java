package org.eclipse.ui.part;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.action.*;
import org.eclipse.ui.*;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Composite;

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
 */
public interface IPage {
/**
 * Creates the SWT control for this page under the given parent 
 * control.
 *
 * @param parent the parent composite
 */
public void createControl(Composite parent);
/**
 * Disposes all resources associated with this page.  From this point on,
 * the page will not be referenced within the workbench.
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
