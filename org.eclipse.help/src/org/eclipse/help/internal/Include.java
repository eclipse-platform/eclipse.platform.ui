/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal;

import org.eclipse.help.IInclude;

public class Include extends Node implements IInclude {

	private String target;
	
	public Include(String target) {
		this.target = target;
	}
	
	public String getTarget() {
		return target;
	}
}
