/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.internal.part.StatusPart;
import org.eclipse.ui.part.ViewPart;

public class ErrorViewPart extends ViewPart {
    
    private IStatus msg;
    
    public ErrorViewPart(IStatus errorMessage) {
        msg = errorMessage;
    }
    
    public void createPartControl(Composite parent) {
        new StatusPart(parent, msg);
    }

    public void setPartName(String newName) {
        super.setPartName(newName);
    }
    
    public void setFocus() {

    }

}
