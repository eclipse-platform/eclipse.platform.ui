/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
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


import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.ui.tests.menus.ObjectContributionClasses;
import org.junit.Assert;

/**
 * Decorator for testing adaptability. This class is used to test
 * the adaptable contribution. It can also be subclassed
 * and the expected class and suffix can be customized
 */
public class TestAdaptableDecoratorContributor extends TestLightweightDecoratorContributor {

	public static final String SUFFIX = "ICommon.1";
	public static final String ID = "org.eclipse.ui.tests.decorators.generalAdaptabilityOn";

	private Class<?> clazz;
	private String suffix;

	public TestAdaptableDecoratorContributor() {
		setExpectedElementType(ObjectContributionClasses.ICommon.class);
		setSuffix(SUFFIX);
	}

	protected void setSuffix(String suffix) {
		this.suffix = suffix;
	}

	protected void setExpectedElementType(Class<?> clazz) {
		this.clazz = clazz;
	}

	@Override
	public void decorate(Object element, IDecoration decoration) {
		Assert.assertTrue(clazz.isInstance(element));
		decoration.addSuffix(suffix);
	}
}
