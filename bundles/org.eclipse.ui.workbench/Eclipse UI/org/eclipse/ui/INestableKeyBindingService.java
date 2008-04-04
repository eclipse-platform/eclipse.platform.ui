/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui;

import org.eclipse.ui.services.IServiceLocator;

/**
 * <p>
 * A service that is capable of nesting other services within itself.  This 
 * allows lower level components to query for a service provider in a 
 * hierarchical fashion, and for information to be resolved in a hierarchical
 * manner
 * </p>
 * <p>
 * This interface is not intended to be implemented or extended by clients.
 * </p>
 * 
 * @since 2.1.3
 * @deprecated This is now handled by {@link IServiceLocator} which can
 * be nested.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface INestableKeyBindingService extends IKeyBindingService {

    /**
     * Marks the service associated with <code>nestedSite</code> as active if
     * one exists.  If there is no service associated, then nothing changes.
     * Calling this method with <code>null</code> forces deactivation of the
     * current service.
     * 
     * @param nestedSite The site whose service should be activated; 
     * <code>null</code> if the current service should be deactivated.
     * @return <code>true</code> if a service is activated (or deactivated, in
     * the case of a <code>null</code> parameter); <code>false</code> if
     * nothing changed.
     */
    public boolean activateKeyBindingService(IWorkbenchSite nestedSite);

    /**
     * An accessor for the nested key binding service associated with a
     * particular site.  If the key binding service does not exist for this
     * <code>nestedSite</code> already, then a new one should be constructed.
     * 
     * @param nestedSite The site for which the service should be found; 
     * should not be <code>null</code>.
     * @return The associated service, if any; or a new associated service, if
     * none existed previously.
     */
    public IKeyBindingService getKeyBindingService(IWorkbenchSite nestedSite);

    /**
     * Removes a nested key binding service from this key binding service.  The
     * service to remove is determined by the <code>nestedSite</code> with
     * which it is associated.
     * 
     * @param nestedSite The site from which to remove the nested service.
     * This site must not be <code>null</code>.
     * @return <code>true</code> if the service existed and could be removed; 
     * <code>false</code> otherwise.
     */
    public boolean removeKeyBindingService(IWorkbenchSite nestedSite);

}
