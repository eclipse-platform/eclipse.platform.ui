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
package org.eclipse.ui.tests.components;

import org.eclipse.core.components.ServiceFactory;
import org.eclipse.core.components.ComponentException;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.Part;

/**
 * @since 3.1
 */
public class ViewBuilder implements IPartBuilder {

    private String viewId;
    private Shell testShell;
    private IWorkbenchPage page; 
    
    public ViewBuilder(String viewId) {
        this.viewId = viewId;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.tests.components.IPartBuilder#createPart()
     */
    public Part createPart(Composite parent, ServiceFactory context, IMemento savedState) throws ComponentException {
        page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        
        return page.getPartFactory().createView(viewId, parent, savedState, context);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "View " + viewId;
    }
}
