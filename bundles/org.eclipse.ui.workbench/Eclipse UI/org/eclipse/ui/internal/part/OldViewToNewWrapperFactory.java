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
package org.eclipse.ui.internal.part;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.components.ComponentException;
import org.eclipse.ui.components.ComponentFactory;
import org.eclipse.ui.components.ComponentHandle;
import org.eclipse.ui.components.Components;
import org.eclipse.ui.components.IServiceProvider;

/**
 * @since 3.1
 */
public class OldViewToNewWrapperFactory extends ComponentFactory {

    public ComponentHandle createHandle(IServiceProvider availableServices) throws ComponentException {
        IViewPart part = (IViewPart) Components.queryInterface(availableServices,
                IViewPart.class);
        
        StandardWorkbenchServices services = new StandardWorkbenchServices(availableServices);
        
        return new ComponentHandle(new OldViewToNewWrapper(part, services));
    }

}
