package org.eclipse.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.swt.widgets.*;

/**
 * Interface for a pulldown action that is contributed into the workbench window 
 * tool bar.  It extends <code>IWorkbenchWindowActionDelegate</code> and adds an
 * initialization method to define the menu creator for the action.
 */
public interface IWorkbenchWindowPulldownDelegate extends IWorkbenchWindowActionDelegate {
/**
 * Returns the menu for this pull down action.  This method will only be
 * called if the user opens the pull down menu for the action.   Note that it
 * is the responsibility of the implementor to properly dispose of any SWT menus
 * created by this method.
 * 
 * @return the menu
 */
public Menu getMenu(Control parent);
}
