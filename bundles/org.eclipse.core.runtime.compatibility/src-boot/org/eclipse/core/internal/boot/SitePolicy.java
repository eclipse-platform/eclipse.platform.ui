/*******************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.boot;

import org.eclipse.core.boot.IPlatformConfiguration.ISitePolicy;
import org.eclipse.update.configurator.IPlatformConfiguration;

public class SitePolicy implements ISitePolicy {
	private IPlatformConfiguration.ISitePolicy newPolicy;

	public SitePolicy(IPlatformConfiguration.ISitePolicy policy) {
		newPolicy = policy;
	}

	public int getType() {
		return newPolicy.getType();
	}

	public String[] getList() {
		return newPolicy.getList();
	}

	public void setList(String[] list) {
		newPolicy.setList(list);
	}

	public IPlatformConfiguration.ISitePolicy getNewPolicy() {
		return newPolicy;
	}

	public boolean equals(Object o) {
		if (o instanceof SitePolicy)
			return newPolicy.equals(((SitePolicy) o).newPolicy);
		return false;
	}

	public int hashCode() {
		return newPolicy.hashCode();
	}
}
