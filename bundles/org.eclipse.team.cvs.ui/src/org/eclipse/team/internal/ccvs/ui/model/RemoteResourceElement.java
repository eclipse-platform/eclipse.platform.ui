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
package org.eclipse.team.internal.ccvs.ui.model;


import org.eclipse.team.internal.ccvs.core.ICVSRemoteResource;

public abstract class RemoteResourceElement extends CVSModelElement {
	/**
	 * Initial implementation: return the resource's name
	 */
	public String getLabel(Object o) {
		if (!(o instanceof ICVSRemoteResource)) return null;
		return ((ICVSRemoteResource)o).getName();
	}
	/**
	 * Return null.
	 */
	public Object getParent(Object o) {
		if (!(o instanceof ICVSRemoteResource)) return null;
		ICVSRemoteResource rr = (ICVSRemoteResource)o;
		return rr.getParent();
	}
}
