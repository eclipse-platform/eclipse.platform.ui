/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.decorators;

import org.eclipse.core.resources.mapping.ResourceMapping;

public class TestResourceMappingDecoratorContributor extends
		TestAdaptableDecoratorContributor {
	@SuppressWarnings("hiding")
	public static final String SUFFIX = "ResourceMapping.1";
	@SuppressWarnings("hiding")
	public static final String ID = "org.eclipse.ui.tests.decorators.resourceMappingDescorator";
	public TestResourceMappingDecoratorContributor() {
		setExpectedElementType(ResourceMapping.class);
		setSuffix(SUFFIX);
	}
}
