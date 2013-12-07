/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.ui.css.swt.resources;

public class ResourceByDefinitionKey {
	private Object key;

	public ResourceByDefinitionKey(Object key) {
		this.key = key;
	}

	@Override
	public int hashCode() {
		return key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof ResourceByDefinitionKey
				&& key.equals(((ResourceByDefinitionKey) obj).key);
	}

	@Override
	public String toString() {
		return key.toString();
	}
}
