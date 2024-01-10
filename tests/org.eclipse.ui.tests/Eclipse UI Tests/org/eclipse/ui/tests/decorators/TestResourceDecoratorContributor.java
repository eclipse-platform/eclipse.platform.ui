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

import org.eclipse.core.resources.IResource;

public class TestResourceDecoratorContributor extends TestAdaptableDecoratorContributor {
	@SuppressWarnings("hiding")
	public static final String SUFFIX = "IResource.1";
	@SuppressWarnings("hiding")
	public static final String ID = "org.eclipse.ui.tests.decorators.resourceDescorator";
	public TestResourceDecoratorContributor() {
		setExpectedElementType(IResource.class);
		setSuffix(SUFFIX);
	}
}
