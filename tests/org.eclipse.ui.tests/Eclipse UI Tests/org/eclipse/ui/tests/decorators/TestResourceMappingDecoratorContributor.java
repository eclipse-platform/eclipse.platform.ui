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

import org.eclipse.core.resources.mapping.ResourceMapping;

public class TestResourceMappingDecoratorContributor extends
        TestAdaptableDecoratorContributor {
    public static final String SUFFIX = "ResourceMapping.1";
    public static final String ID = "org.eclipse.ui.tests.decorators.resourceMappingDescorator";
    public TestResourceMappingDecoratorContributor() {
        setExpectedElementType(ResourceMapping.class);
        setSuffix(SUFFIX);
    }
}
