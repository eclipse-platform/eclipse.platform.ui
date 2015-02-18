/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.boot;

/**
 * Bootstrap type for the platform. Platform runnables represent executable 
 * entry points into plug-ins.  Runnables can be configured into the Platform's
 * <code>org.eclipse.core.runtime.applications</code> extension-point 
 * or be made available through code or extensions on other plug-in's extension-points.
 * <p>
 * Clients may implement this interface.
 * </p>
 * @deprecated In Eclipse 3.0 the boot plug-in and packages were deprecated.
 * This class has been replaced by an equivalent class in the org.eclipse.core.runtime package.
 * This API will be deleted in a future release. See bug 370248 for details.
 * 
 * @see org.eclipse.core.runtime.IPlatformRunnable
 */

public interface IPlatformRunnable extends org.eclipse.core.runtime.IPlatformRunnable {

}
