/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.progress;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.StatusLineLayoutData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;

public class ProgressContributionItem extends ContributionItem {

	private ProgressControl control;
	
	

	public ProgressContributionItem(String id) {
		super(id);
		
	}

	public void fill(Composite parent) {
		if (control == null) {
			
			control = new ProgressControl();
			control.createCanvas(parent);

			AnimatedCanvas canvas = control.getCanvas();
			StatusLineLayoutData data = new StatusLineLayoutData();
			Rectangle bounds = canvas.getImage().getBounds();
			data.widthHint = bounds.width;
			data.heightHint = bounds.height;
			canvas.getControl().setLayoutData(data);
		}
	}

}