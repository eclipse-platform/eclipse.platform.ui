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
package org.eclipse.core.internal.dependencies;

import org.eclipse.core.internal.runtime.Assert;

class Element implements IElement {
	private Object id;
	private Object versionId;
	private IDependency[] dependencies;
	private boolean singleton;
	public Element(Object id, Object versionId, IDependency[] dependencies, boolean singleton) {
		Assert.isNotNull(id);
		Assert.isNotNull(versionId);
		Assert.isNotNull(dependencies);
		this.id = id;
		this.versionId = versionId;
		this.dependencies = dependencies;		
		this.singleton = singleton;
	}
	public Object getId() {
		return id;
	}
	public Object getVersionId() {
		return versionId;
	}
	public IDependency[] getDependencies() {
		return dependencies;
	}
	public IDependency getDependency(Object id) {
		for (int i = 0; i < dependencies.length; i++)
			if (dependencies[i].getRequiredObjectId().equals(id))
				return dependencies[i];
		return null;
	}
	public boolean isSingleton() {
		return singleton;
	}
	public String toString() {
		return this.id + "_" + this.versionId; //$NON-NLS-1$
	}

}
