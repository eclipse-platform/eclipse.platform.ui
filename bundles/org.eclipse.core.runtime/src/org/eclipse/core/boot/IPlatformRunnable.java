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
package org.eclipse.core.boot;

/**
 * Bootstrap type for the platform. Platform runnables represent executable 
 * entry points into plug-ins.  Runnables can be configured into the Platform's
 * <code>org.eclipse.core.runtime.applications</code> extension-point 
 * or be made available through code or extensions on other plug-in's extension-points.
 *
 * <p>
 * Clients may implement this interface.
 * </p>
 * <p>
 * <b>Note</b>: This is obsolete API that will be replaced in time with
 * the OSGI-based Eclipse Platform Runtime introduced with Eclipse 3.0.
 * This API will be deprecated once the APIs for the new Eclipse Platform
 * Runtime achieve their final and stable form (post-3.0). </p>
 */

public interface IPlatformRunnable extends org.eclipse.core.runtime.IPlatformRunnable {

}