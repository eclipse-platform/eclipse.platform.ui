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
package org.eclipse.ui.internal;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.internal.skins.IPresentablePart;

/**
 * @since 3.0
 */
public class PresentableViewPart implements IPresentablePart {
	
	private ViewPane pane;
	
	public PresentableViewPart(ViewPane pane) {
		this.pane = pane;
	}
	
	private Perspective getPerspective() {
		return pane.getPage().getActivePerspective();
	}
	
	private IViewReference getViewReference() {
		return pane.getViewReference();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.skins.IPresentablePart#setBounds(org.eclipse.swt.graphics.Rectangle)
	 */
	public void setBounds(Rectangle bounds) {
		pane.setBounds(bounds);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.skins.IPresentablePart#setVisible(boolean)
	 */
	public void setVisible(boolean isVisible) {
		pane.setVisible(isVisible);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.skins.IPresentablePart#setFocus()
	 */
	public void setFocus() {
		pane.setFocus();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.skins.IPresentablePart#addPropertyListener(org.eclipse.ui.IPropertyListener)
	 */
	public void addPropertyListener(IPropertyListener listener) {
		getViewReference().addPropertyListener(listener);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.skins.IPresentablePart#getName()
	 */
	public String getName() {
		WorkbenchPartReference ref = (WorkbenchPartReference)pane.getPartReference();

		return ref.getRegisteredName();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.skins.IPresentablePart#getTitle()
	 */
	public String getTitle() {
		return getViewReference().getTitle();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.skins.IPresentablePart#getTitleImage()
	 */
	public Image getTitleImage() {
		return getViewReference().getTitleImage();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.skins.IPresentablePart#getTitleToolTip()
	 */
	public String getTitleToolTip() {
		return getViewReference().getTitleToolTip();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.skins.IPresentablePart#isDirty()
	 */
	public boolean isDirty() {
		return false;
	}
}
