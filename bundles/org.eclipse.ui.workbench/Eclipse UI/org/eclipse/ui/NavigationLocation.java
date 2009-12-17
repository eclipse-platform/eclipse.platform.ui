/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui;

/**
 * Default implementation of INavigationLocation.
 * 
 * @since 2.1
 */
public abstract class NavigationLocation implements INavigationLocation {

    private IWorkbenchPage page;

    private IEditorInput input;

    /**
     * Constructs a NavigationLocation with its editor part.
     * 
     * @param editorPart
     */
    protected NavigationLocation(IEditorPart editorPart) {
        this.page = editorPart.getSite().getPage();
        this.input = editorPart.getEditorInput();
    }

    /** 
     * Returns the part that the receiver holds the location for.
     * 
     * @return IEditorPart
     */
    protected IEditorPart getEditorPart() {
        if (input == null) {
			return null;
		}
        return page.findEditor(input);
    }

    /*
     * (non-Javadoc)
     * Method declared on INavigationLocation.
     */
    public Object getInput() {
        return input;
    }

    /*
     * (non-Javadoc)
     * Method declared on INavigationLocation.
     */
    public String getText() {
        IEditorPart part = getEditorPart();
        if (part == null) {
			return new String();
		}
        return part.getTitle();
    }

    /*
     * (non-Javadoc)
     * Method declared on INavigationLocation.
     */
    public void setInput(Object input) {
        this.input = (IEditorInput) input;
    }

    /**
     * May be extended by clients.
     *
     * @see org.eclipse.ui.INavigationLocation#dispose()
     */
    public void dispose() {
        releaseState();
    }

    /**
     * May be extended by clients.
     * 
     * @see org.eclipse.ui.INavigationLocation#releaseState()
     */
    public void releaseState() {
        input = null;
    }
}
