/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.components;

/**
 * Component used for testing dependency errors. This component can never
 * be created because it contains a dependency cycle. CycleTest1 refers back
 * to this object.
 * 
 * @since 3.1
 */
public class CycleTest2 {
    public CycleTest2(CycleTest1 otherObject) {
        
    }
}
