package org.eclipse.jface.action;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

/**
 * Interface for something that creates and disposes of SWT menus.
 */
public interface IMenuCreator {
/**
 * Disposes the menu returned by <code>getMenu</code>. Does nothing
 * if there is no menu.
 */
public void dispose();
/**
 * Returns the SWT menu, created as a pop up menu parented by the
 * given control.
 *
 * @param parent the parent control
 * @return the menu, or <code>null</code> if the menu could not
 *  be created
 */
public Menu getMenu(Control parent);
/**
 * Returns an SWT menu created as a drop down menu parented by the
 * given menu.
 *
 * @param parent the parent menu
 * @return the menu, or <code>null</code> if the menu could not
 *  be created
 */
public Menu getMenu(Menu parent);
}
