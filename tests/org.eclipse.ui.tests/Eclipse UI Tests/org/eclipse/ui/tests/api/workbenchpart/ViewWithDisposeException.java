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
package org.eclipse.ui.tests.api.workbenchpart;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;

public class ViewWithDisposeException extends ViewPart {

    public void createPartControl(Composite parent) {
        parent.setLayout(new FillLayout());
        
        Label testLabel = new Label(parent, SWT.NONE);
        
        testLabel.setText("This view is supposed to throw an exception when closed");
    }

    public void setFocus() {

    }

    public void dispose() {
        throw new RuntimeException("This exception was thrown intentionally as part of an error handling test");
    }
}
