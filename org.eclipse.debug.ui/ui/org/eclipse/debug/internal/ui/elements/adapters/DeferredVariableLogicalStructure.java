/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.elements.adapters;


/**
 * Workbench adapter for a variable when showing logical structures.
 */
public class DeferredVariableLogicalStructure extends DeferredVariable {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.variables.DeferredVariable#isShowLogicalStructure()
	 */
	protected boolean isShowLogicalStructure() {
		return true;
	}
}
