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

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.internal.components.framework.ComponentException;
import org.eclipse.ui.internal.components.framework.ComponentFactory;
import org.eclipse.ui.internal.components.framework.ComponentHandle;
import org.eclipse.ui.internal.components.framework.Components;
import org.eclipse.ui.internal.components.framework.IServiceProvider;
import org.eclipse.ui.internal.part.components.services.IPartActionBars;

/**
 * @since 3.1
 */
public class OldViewToNewWrapperFactory extends ComponentFactory {

    public ComponentHandle createHandle(IServiceProvider availableServices) throws ComponentException {
        IViewPart part = (IViewPart) Components.queryInterface(availableServices,
                IViewPart.class);

        IPartActionBars actionBars = (IPartActionBars) Components.queryInterface(availableServices,
                IPartActionBars.class);
        
        StandardWorkbenchServices services = new StandardWorkbenchServices(availableServices);
        
        return new ComponentHandle(new OldViewToNewWrapper(part, actionBars, services));
    }

    
}
