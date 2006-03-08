/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.performance.parts;

import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.views.markers.internal.ProblemView;

/**
 * PerformanceProblemsView is a problems view created
 * for the performance tests.
 * @since 3.2
 *
 */
public class PerformanceProblemsView extends ProblemView {

	public Tree getTreeWidget(){
		return getTree();
	}
}
