/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.dnd;

import org.eclipse.ui.IWorkbenchWindow;

public class ExistingWindowProvider implements IWorkbenchWindowProvider {

    private IWorkbenchWindow window;
    
    public ExistingWindowProvider(IWorkbenchWindow window) {
        this.window = window;
    }
    
    public IWorkbenchWindow getWorkbenchWindow() {
        return window;
    }

}
