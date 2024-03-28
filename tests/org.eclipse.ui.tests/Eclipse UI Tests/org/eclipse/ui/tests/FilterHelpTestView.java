/*******************************************************************************
 * Copyright (c) 2022 Enda O'Brien and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which accompanies this distribution,
 * and is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors: IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.tests;

import org.eclipse.ui.views.markers.MarkerSupportView;

public class FilterHelpTestView extends MarkerSupportView {

	public static final String ID = "org.eclipse.ui.tests.filterHelpTestView";

	static final String CONTENT_GEN_ID = "org.eclipse.ui.tests.filterHelpContentGenerator";

	public FilterHelpTestView() {
		super(CONTENT_GEN_ID);
	}

}
