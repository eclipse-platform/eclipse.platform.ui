package org.eclipse.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * Interface for an action that is contributed into a view's local tool bar,
 * pulldown menu, or popup menu. It extends <code>IActionDelegate</code>
 * and adds an initialization method for connecting the delegate to the view it
 * should work with.
 */
public interface IViewActionDelegate extends IActionDelegate {
/**
 * Initializes this action delegate with the view it will work in.
 *
 * @param view the view that provides the context for this delegate
 */
public void init(IViewPart view);
}
