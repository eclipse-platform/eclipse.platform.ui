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
package org.eclipse.core.internal.watson;
import java.lang.String;
import org.eclipse.core.runtime.IPath;
/**
 * Callback interface so visitors can request the path of the object they
 * are visiting. This avoids creating paths when they are not needed.
 */
public interface IPathRequestor {
	public IPath requestPath();
	public String requestName();
}
