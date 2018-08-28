/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stefan Winkler <stefan@winklerweb.net> - Bug 434189
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * A message line displaying a status.
 */
public class MessageLine extends CLabel {

    /**
     * Creates a new message line as a child of the given parent.
     */
    public MessageLine(Composite parent) {
        this(parent, SWT.LEFT);
    }

    /**
     * Creates a new message line as a child of the parent and with the given SWT stylebits.
     */
    public MessageLine(Composite parent, int style) {
        super(parent, style);
    }

    private Image findImage(IStatus status) {
        if (status.isOK()) {
            return null;
        } else if (status.matches(IStatus.ERROR)) {
            return PlatformUI.getWorkbench().getSharedImages().getImage(
                    ISharedImages.IMG_OBJS_ERROR_TSK);
        } else if (status.matches(IStatus.WARNING)) {
            return PlatformUI.getWorkbench().getSharedImages().getImage(
                    ISharedImages.IMG_OBJS_WARN_TSK);
        } else if (status.matches(IStatus.INFO)) {
            return PlatformUI.getWorkbench().getSharedImages().getImage(
                    ISharedImages.IMG_OBJS_INFO_TSK);
        }
        return null;
    }

    /**
     * Sets the message and image to the given status.
     * <code>null</code> is a valid argument and will set the empty text and no image
     */
    public void setErrorStatus(IStatus status) {
        if (status != null) {
            String message = status.getMessage();
            if (message != null && message.length() > 0) {
                setText(message);
                setImage(findImage(status));
                return;
            }
        }
		setText(""); //$NON-NLS-1$
        setImage(null);
	}
}

