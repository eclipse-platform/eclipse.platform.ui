/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.part;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchPart2;

/**
 * @since 3.1
 */
public abstract class NewPartToWorkbenchPartAdapter implements IWorkbenchPart2 {
    
    private IPartPropertyProvider propertyProvider;
    
    public NewPartToWorkbenchPartAdapter(IPartPropertyProvider provider) {
        this.propertyProvider = provider;
    }
    
    /**
     * @since 3.1 
     *
     * @return
     */
    protected IPartPropertyProvider getPropertyProvider() {
        return propertyProvider;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPart#addPropertyListener(org.eclipse.ui.IPropertyListener)
     */
    public void addPropertyListener(IPropertyListener listener) {
        propertyProvider.addPropertyListener(this, listener);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    public void createPartControl(Composite parent) {
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPart#dispose()
     */
    public void dispose() {

    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPart#setFocus()
     */
    public void setFocus() {

    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPart2#getContentDescription()
     */
    public String getContentDescription() {
        return propertyProvider.getContentDescription();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPart2#getPartName()
     */
    public String getPartName() {
        return propertyProvider.getPartName();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPart#getTitle()
     */
    public String getTitle() {
        return propertyProvider.getTitle();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPart#getTitleImage()
     */
    public Image getTitleImage() {
        return propertyProvider.getTitleImage();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPart#getTitleToolTip()
     */
    public String getTitleToolTip() {
        return propertyProvider.getTitleToolTip();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPart#removePropertyListener(org.eclipse.ui.IPropertyListener)
     */
    public void removePropertyListener(IPropertyListener listener) {
        propertyProvider.removePropertyListener(this, listener);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IEditorPart#getEditorInput()
     */
    public IEditorInput getEditorInput() {
        return propertyProvider.getEditorInput();
    }
    
    public boolean isDirty() {
        return propertyProvider.isDirty();
    }
    
}
