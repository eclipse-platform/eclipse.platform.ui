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
package org.eclipse.ui.internal.skins.newlook;

import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.internal.skins.IPresentablePart;
import org.eclipse.ui.internal.skins.IPresentationSite;
import org.eclipse.ui.internal.skins.IStackPresentationSite;
import org.eclipse.ui.internal.skins.StackPresentation;

/**
 * @since 3.0
 */
public class DefaultStackPresentationSite implements IStackPresentationSite {
	
	private StackPresentation presentation;
	private int state = IPresentationSite.STATE_RESTORED;
	private IPresentablePart current;
	
	public DefaultStackPresentationSite() {
		
	}
	
	public void setPresentation(StackPresentation newPresentation) {
		presentation = newPresentation;
	}
	
	public StackPresentation getPresentation() {
		return presentation;
	}
	
	public int getState() {
		return state;
	}
	
	public IPresentablePart getCurrent() {
		return current;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.skins.IStackPresentationSite#selectPart(org.eclipse.ui.internal.skins.IPresentablePart)
	 */
	public void selectPart(IPresentablePart toSelect) {
		current = toSelect;
		
		if (presentation != null) {
			presentation.selectPart(toSelect);
		}
	}
	
	public void dispose() {
		presentation.dispose();
		setPresentation(null);
		current = null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.skins.IPresentationSite#setState(int)
	 */
	public void setState(int newState) {
		setPresentationState(newState);
	}
	
	public void setPresentationState(int newState) {
		state = newState;
		if (presentation != null) {
			presentation.setState(newState);
		}		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.skins.IPresentablePart#isClosable()
	 */
	public boolean isClosable(IPresentablePart part) {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.skins.IPresentablePart#isMovable()
	 */
	public boolean isMovable(IPresentablePart part) {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.skins.IPresentationSite#dragStart(org.eclipse.ui.internal.skins.IPresentablePart, boolean)
	 */
	public void dragStart(IPresentablePart beingDragged, Point initialPosition, boolean keyboard) {

	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.skins.IPresentationSite#close(org.eclipse.ui.internal.skins.IPresentablePart)
	 */
	public void close(IPresentablePart toClose) {
		// TODO Auto-generated method stub
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.skins.IPresentationSite#dragStart(boolean)
	 */
	public void dragStart(boolean keyboard) {
		// TODO Auto-generated method stub
	}

}
