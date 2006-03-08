/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.tests.performance.parts;

import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.views.markers.internal.ProblemView;
/**
 * A problems view with API for the performance tests.
 * @since 3.1
 *
 */
public class PerformanceProblemsView extends ProblemView {
	
	/**
	 * Return the table widget
	 * @return
	 */
	public Table getTableWidget(){
		return getTable();
	}

}
