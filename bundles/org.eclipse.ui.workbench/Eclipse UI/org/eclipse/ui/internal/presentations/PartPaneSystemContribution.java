/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.presentations;

import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.internal.PartPane;
import org.eclipse.ui.presentations.IStackPresentationSite;

/**
 * @since 3.0
 */
public class PartPaneSystemContribution extends StandardSystemContribution {

	private PartPane pane;
	
	/**
	 * @param site
	 */
	public PartPaneSystemContribution(IStackPresentationSite site) {
		super(site);
	}
	
	public void setCurrentPane(PartPane pane) {
		this.pane = pane;
		setPart(pane.getPresentablePart());
	}
	
	protected PartPane getPane() {
		return pane;
	}
	
	protected void addSizeMenuItem (Menu menu) {
		if (pane != null) {
			pane.addSizeMenuItem(menu);			
		}
	}	
}
