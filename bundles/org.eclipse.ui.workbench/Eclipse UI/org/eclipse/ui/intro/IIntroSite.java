/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.intro;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IKeyBindingService;
import org.eclipse.ui.IWorkbenchSite;

/** 
 * The primary interface between an intro part and the workbench.
 * <p>
 * The workbench exposes its implemention of intro part sites via this 
 * interface, which is not intended to be implemented or extended by clients.
 * </p>
 *  
 * @since 3.0
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IIntroSite extends IWorkbenchSite {

    /**
     * Returns the part registry extension id for this intro site's part.
     * <p>
     * The name comes from the <code>id</code> attribute in the configuration
     * element.
     * </p>
     *
     * @return the registry extension id
     */
    public String getId();

    /**
     * Returns the unique identifier of the plug-in that defines this intro
     * site's part.
     *
     * @return the unique identifier of the declaring plug-in
     * @see org.eclipse.core.runtime.IPluginDescriptor#getUniqueIdentifier()
     */
    public String getPluginId();

    /**
     * Returns the key binding service in use.
     * <p>
     * The part will access this service to register
     * all of its actions, to set the active scope.
     * </p>
     * 
     * @return the key binding service in use
     * @deprecated Use IServiceLocator#getService(*) to retrieve
     * IContextService and IHandlerService instead.
     */
    public IKeyBindingService getKeyBindingService();

    /**
     * Returns the action bars for this part site.
     * The intro part has exclusive use of its site's action bars.
     * 
     * @return the action bars
     */
    public IActionBars getActionBars();
}
