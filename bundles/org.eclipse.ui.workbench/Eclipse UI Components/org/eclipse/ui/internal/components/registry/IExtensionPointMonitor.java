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
package org.eclipse.ui.internal.components.registry;

import org.eclipse.core.runtime.IExtension;

/**
 * Implementers of this interface will be notified about all extensions
 * that extend some extension point.
 * 
 * @since 3.1
 */
public interface IExtensionPointMonitor {
    public void added(IExtension newExtension);
    public void removed(IExtension oldExtension);
}
