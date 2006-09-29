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
package org.eclipse.compare.internal;

import org.eclipse.compare.CompareViewerSwitchingPane;

/**
 * The precursor to {@link org.eclipse.compare.CompareNavigator}. This class is being keep around
 * as it was needed by clients in some situations.
 * @deprecated Use {@link org.eclipse.compare.CompareNavigator}
 */
public class CompareNavigator extends org.eclipse.compare.CompareNavigator {

	public CompareNavigator(CompareViewerSwitchingPane[] panes) {
		super(panes);
	}
	
}
