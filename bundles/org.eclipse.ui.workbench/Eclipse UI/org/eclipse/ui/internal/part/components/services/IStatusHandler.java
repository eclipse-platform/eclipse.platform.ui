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
package org.eclipse.ui.internal.part.components.services;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Allows a part to report its current status. Status messages remain until they are
 * explicitly cleared or overwritten.
 * 
 * @since 3.1
 */
public interface IStatusHandler {
    /**
     * Sets or clears the current status message, replacing any existing status message.
     *
     * @param message new status message, or null to clear
     * @param image custom image to be displayed next to the status message, or null
     * to clear. The receiver may ignore the image if it is not capable of associating
     * images with status messages.
     */
    public void set(IStatus message, ImageDescriptor image);
}
