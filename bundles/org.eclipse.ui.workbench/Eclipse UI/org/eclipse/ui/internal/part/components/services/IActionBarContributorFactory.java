/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.part.components.services;

import org.eclipse.ui.internal.part.Part;

public interface IActionBarContributorFactory {
    public IActionBarContributor getContributor(IPartDescriptor descriptor);
    public void activateBars(IActionBarContributor toActivate, Part actualPart);
    public void deactivateBars(IActionBarContributor toActivate);
}
