/*
 * Copyright (c) 2002 IBM Corp.  All rights reserved.
 * This file is made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package org.eclipse.update.ui.forms.internal.engine;

import org.eclipse.jface.action.Action;
import org.eclipse.update.ui.forms.internal.IHyperlinkListener;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;

/**
 * @version 	1.0
 * @author
 */
public class HyperlinkAction {
	IStatusLineManager manager;
	private String description;
	
	public HyperlinkAction() {
	}
	
	public void linkActivated(final IHyperlinkSegment link) {
	}
	
	public void linkEntered(IHyperlinkSegment link) {
		if (manager!=null && description!=null) {
			manager.setMessage(description);
		}
	}

	public void linkExited(IHyperlinkSegment link) {
		if (manager!=null && description!=null) {
			manager.setMessage(null);
		}
	}
	
	public void setStatusLineManager(IStatusLineManager manager) {
		this.manager = manager;
	}
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
}