package org.eclipse.ui;

/************************************************************************
Copyright (c) 2000, 2003 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/

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
