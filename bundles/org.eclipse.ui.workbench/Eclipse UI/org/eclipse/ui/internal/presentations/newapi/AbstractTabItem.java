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
package org.eclipse.ui.internal.presentations.newapi;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Widget;

/**
 * @since 3.0
 */
public abstract class AbstractTabItem {
	public abstract Rectangle getBounds();
	public abstract Widget getControl();
	public abstract String getTitleToolTip();
	public abstract String getTitle();
	public abstract String getContentDescription();
	public abstract String getPartName();
	public abstract Image getImage();
	public abstract void setTitle(String title);
	public abstract void setContentDescription(String contentDescription);
	public abstract void setPartName(String partName);
	public abstract void setImage(Image titleImage);
	public abstract void setTitleToolTip(String toolTip);
	public void setBusyState(boolean isBusy, boolean isBold) {
	}
	public abstract boolean isShowing();
	public abstract void setCloseable(boolean isCloseable);
}
