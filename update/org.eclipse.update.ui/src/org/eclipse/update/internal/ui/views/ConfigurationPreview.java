/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.ui.views;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.*;

public class ConfigurationPreview {
	private ConfigurationPreviewForm form;

	public ConfigurationPreview(ConfigurationView configView) {
		form = new ConfigurationPreviewForm(configView);
	}
	public void createControl(Composite parent) {
		form.createControl(parent);
		form.initialize(null);
	}
	
	public Control getControl() {
		return form.getControl();
	}
	
	public Control getScrollingControl() {
		return form.getScrollingControl();
	}
	
	public void setSelection(IStructuredSelection selection) {
		Object obj = selection.getFirstElement();
		form.expandTo(obj);
	}
	
	public void dispose() {
		if (form!=null)
			form.dispose();
	}
}
