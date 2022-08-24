package org.eclipse.ui.tests;

import org.eclipse.ui.views.markers.MarkerSupportView;

/*******************************************************************************
 * Copyright (c) 2005, 2022 IBM Corporation and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which accompanies this distribution,
 * and is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors: IBM Corporation - initial API and implementation
 *******************************************************************************/

public class SubTypeOnlyTestView extends MarkerSupportView {
	public static final String ID = "org.eclipse.ui.tests.subTypeOnlyTestView";

	static final String CONTENT_GEN_ID = "org.eclipse.ui.tests.subTypeOnlyTestViewContentGenerator";

	public SubTypeOnlyTestView() {
		super(CONTENT_GEN_ID);
	}

}
