/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.presentations.IPresentablePart;
import org.eclipse.ui.presentations.IStackPresentationSite;
import org.eclipse.ui.presentations.StackPresentation;

/**
 * @since 3.0
 */
public abstract class DefaultStackPresentationSite implements
        IStackPresentationSite {

    private StackPresentation presentation;

    private int state = IStackPresentationSite.STATE_RESTORED;

    private int activeState = StackPresentation.AS_INACTIVE;

    public DefaultStackPresentationSite() {

    }

    public void setPresentation(StackPresentation newPresentation) {
        presentation = newPresentation;
        if (presentation != null) {
            presentation.setState(state);
            presentation.setActive(activeState);
        }
    }

    public StackPresentation getPresentation() {
        return presentation;
    }

    public int getState() {
        return state;
    }

    public void setActive(int activeState) {
        if (activeState != this.activeState) {
            this.activeState = activeState;
            if (presentation != null) {
                presentation.setActive(activeState);
            }
        }
    }

    public int getActive() {
        return activeState;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.skins.IStackPresentationSite#selectPart(org.eclipse.ui.internal.skins.IPresentablePart)
     */
    public void selectPart(IPresentablePart toSelect) {

        if (presentation != null) {
            presentation.selectPart(toSelect);
        }
    }

    public void dispose() {
        if (presentation != null) {
            presentation.dispose();
        }
        setPresentation(null);
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
    public boolean isCloseable(IPresentablePart part) {
        return part.isCloseable();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.skins.IPresentationSite#dragStart(org.eclipse.ui.internal.skins.IPresentablePart, boolean)
     */
    public void dragStart(IPresentablePart beingDragged, Point initialPosition,
            boolean keyboard) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.skins.IPresentationSite#close(org.eclipse.ui.internal.skins.IPresentablePart)
     */
    public void close(IPresentablePart toClose) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.skins.IPresentationSite#dragStart(boolean)
     */
    public void dragStart(Point initialPosition, boolean keyboard) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.presentations.IStackPresentationSite#supportsState(int)
     */
    public boolean supportsState(int state) {
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.presentations.IStackPresentationSite#getSelectedPart()
     */
    public abstract IPresentablePart getSelectedPart();

    public void addSystemActions(IMenuManager menuManager) {

    }

    public abstract boolean isPartMoveable(IPresentablePart toMove);

    public abstract boolean isStackMoveable();

}
