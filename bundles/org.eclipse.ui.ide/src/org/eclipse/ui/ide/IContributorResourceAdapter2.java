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
package org.eclipse.ui.ide;

import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IContributorResourceAdapter;

/**
 * An extension to the <code>IContributorResourceAdapter</code> that adapts 
 * a model object to a <code>ResourceMapping</code>.
 * 
 * @since 3.2
 */
public interface IContributorResourceAdapter2 extends IContributorResourceAdapter {

    /**
     * Return the resource mapping that the supplied adaptable 
     * adapts to. An <code>IContributorResourceAdapter2</code> assumes
     * that any object passed to it adapts to one equivalent
     * resource mapping.
     * 
     * @param adaptable the adaptable being queried
     * @return a resource mapping, or <code>null</code> if there
     *  is no adapted resource mapping for this type
     */
    public ResourceMapping getAdaptedResourceMapping(IAdaptable adaptable);
}
