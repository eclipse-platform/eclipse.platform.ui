package org.eclipse.jface.action;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 1999, 2000
 */

/**
 * A <code>SubToolBarManager</code> monitors the additional and removal of 
 * items from a parent manager so that visibility of the entire set can be changed as a
 * unit.
 */
public class SubToolBarManager extends SubContributionManager 
	implements IToolBarManager
{
	/**
	 * The parent tool bar manager.
	 */
	private IToolBarManager parentMgr;
/**
 * Constructs a new manager.
 *
 * @param mgr the parent manager.  All contributions made to the 
 *      <code>SubToolBarManager</code> are forwarded and appear in the
 *      parent manager.
 */
public SubToolBarManager(IToolBarManager mgr) {
	super(mgr);
	parentMgr = mgr;
}
/* (non-Javadoc)
 * Method declared on IToolBarManager.
 */
public void update(boolean force) {
	// This method is not governed by visibility.  The client may
	// call <code>setVisible</code> and then force an update.  At that
	// point we need to update the parent.
	parentMgr.update(force);
}
}
