package org.eclipse.ui;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

/**
 * The primary interface between a view part and the outside world.
 * <p>
 * The workbench exposes its implemention of view part sites via this interface,
 * which is not intended to be implemented or extended by clients.
 * </p>
 */
public interface IViewSite extends IWorkbenchPartSite {
/**
 * Returns the action bars for this part site.
 * Views have exclusive use of their site's action bars.
 *
 * @return the action bars
 */
public IActionBars getActionBars();
}
