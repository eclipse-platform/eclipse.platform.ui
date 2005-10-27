/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.navigator.internal.deferred;
 


/**
 * @author Michael D. Elder <mdelder@us.ibm.com>
 */
public interface IPendingElementCollector {

	public void collectChildren(Object parent, Object[] children);

	public void done(PendingUpdateAdapter placeHolder);

}