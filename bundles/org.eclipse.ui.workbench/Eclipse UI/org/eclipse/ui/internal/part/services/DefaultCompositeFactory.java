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
package org.eclipse.ui.internal.part.services;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.components.framework.ComponentFactory;
import org.eclipse.ui.internal.components.framework.ComponentHandle;
import org.eclipse.ui.internal.components.framework.IDisposable;
import org.eclipse.ui.internal.components.framework.IServiceProvider;

/**
 * @since 3.1
 */
public class DefaultCompositeFactory extends ComponentFactory {

    private final static class CompositeHandle extends ComponentHandle implements IDisposable {
        
        public CompositeHandle(Composite toWrap) {
            super(toWrap);
        }
        
        public IDisposable getDisposable() {
            return this;
        }
        
        public void dispose() {
            ((Composite)getInstance()).dispose();
        }

    }
    
    /* (non-Javadoc)
     * @see org.eclipse.core.component.ComponentAdapter#createInstance(org.eclipse.core.component.IContainer)
     */
    public ComponentHandle createHandle(IServiceProvider availableServices) {
        Shell result = new Shell(Display.getCurrent(), SWT.NONE);
        result.setLayout(new FillLayout());
        result.open();
        return new CompositeHandle(result);
    }

}
