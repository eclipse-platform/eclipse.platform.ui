/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.variables;



/**
 * Default deferred content provider for a variable
 */
public class DeferredLogicalStructure extends DeferredVariable {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.variables.DeferredVariable#isShowLogicalStructure()
	 */
	protected boolean isShowLogicalStructure() {
		return true;
	}
}
