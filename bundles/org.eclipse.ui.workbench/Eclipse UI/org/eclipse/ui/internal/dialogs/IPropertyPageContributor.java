/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import org.eclipse.ui.internal.*;
import org.eclipse.core.runtime.IAdaptable;
/**
 * Implement this interface in order to register property
 * pages for a given object. During property dialog building
 * sequence, all property page contributors for a given object
 * are given a chance to add their pages.
 */
public interface IPropertyPageContributor extends IObjectContributor {
/**
 * Implement this method to add instances of PropertyPage class to the
 * property page manager.
 * @return true if pages were added, false if not.
 */	

public boolean contributePropertyPages(PropertyPageManager manager, IAdaptable object);
}
