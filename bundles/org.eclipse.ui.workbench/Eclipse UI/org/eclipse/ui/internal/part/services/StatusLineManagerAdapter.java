/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.part.services;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.internal.part.components.services.IStatusFactory;
import org.eclipse.ui.internal.part.components.services.IStatusHandler;

/**
 * TODO: fix progress reporting and actual contribution items
 * 
 * @since 3.1
 */
public class StatusLineManagerAdapter extends NullContributionManager implements
    IStatusLineManager {

    private IStatusHandler handler;
    private IStatusFactory factory;
    
    private String currentError;
    private Image currentErrorImage;
    private String currentMessage;
    private Image currentMessageImage;
    
    public StatusLineManagerAdapter(IStatusHandler handler, IStatusFactory factory) {
        this.handler = handler;
        this.factory = factory;
    }
    
    public IProgressMonitor getProgressMonitor() {
        return new NullProgressMonitor();
    }

    public boolean isCancelEnabled() {
        return false;
    }

    public void setCancelEnabled(boolean enabled) {
    }

    public void setErrorMessage(String message) {
        setErrorMessage(null, message);
    }

    public void setErrorMessage(Image image, String message) {
        currentErrorImage = image;
        currentError = message;
        updateHandler();
    }

    public void setMessage(String message) {
        setMessage(null, message);
    }

    public void setMessage(Image image, String message) {
        currentMessageImage = image;
        currentMessage = message;
        updateHandler();
    }
    
    private void updateHandler() {
        if (currentError != null) {
            handler.set(factory.newError(currentError, null), currentErrorImage == null ? null : 
                    ImageDescriptor.createFromImage(currentErrorImage));
            return;
        }
        
        if (currentMessage != null) {
            handler.set(factory.newMessage(currentMessage), currentMessageImage == null ? null : 
                ImageDescriptor.createFromImage(currentMessageImage));
            return;
        }
        
        handler.set(null, null);
    }

}
