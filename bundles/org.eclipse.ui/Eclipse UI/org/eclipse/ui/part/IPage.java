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
 * This is the last method called on the <code>IPage</code>.  It is the page's
 * responsibility to ensure that all of its controls have been disposed by the
 * time this method returns, but they may have already been disposed if the parent
 * composite has been disposed.  Also, there is no guarantee that createControl() 
 * has been called, so the controls may never have been created.
 * </p>
 * <p>
 * Clients should not call this method (the workbench calls this method at
 * appropriate times).
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
