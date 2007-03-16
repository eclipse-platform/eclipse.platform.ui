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
package org.eclipse.ui.tests.decorators;

/**
 * Decorator used to test an unadaptaed contribution
 */
public class TestUnadaptableDecoratorContributor extends TestAdaptableDecoratorContributor {
    public static final String SUFFIX = "ICommon.2";
    public static final String ID = "org.eclipse.ui.tests.decorators.generalAdaptabilityOff";
    public TestUnadaptableDecoratorContributor() {
        setSuffix(SUFFIX);
    }
}
