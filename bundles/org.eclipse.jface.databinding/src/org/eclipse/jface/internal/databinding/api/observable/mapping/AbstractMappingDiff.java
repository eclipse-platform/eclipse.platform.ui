/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.api.observable.mapping;

import java.util.Set;

/**
 * @since 1.0
 *
 */
abstract public class AbstractMappingDiff implements IMappingDiff {

	private Set elements;

	/**
	 * @param elements
	 */
	public AbstractMappingDiff(Set elements) {
		this.elements = elements;
	}

	public Set getElements() {
		return elements;
	}

}
