/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.internal.registry.IViewDescriptor;

/**
 * A view container manages the services for a view.
 */
public class ViewSite extends PartSite
	implements IViewSite
{
/**
 * Creates a new ViewSite.
 */
public ViewSite(IViewReference ref, IViewPart view, WorkbenchPage page, IViewDescriptor desc) {
	super(ref, view, page);
	setConfigurationElement(desc.getConfigurationElement());
}
/**
 * Returns the secondary id or <code>null</code>.
 */
public String getSecondaryId() {
    return ((IViewReference) getPartReference()).getSecondaryId();
}
/**
 * Returns the view.
 */
public IViewPart getViewPart() {
	return (IViewPart)getPart();
}

}
