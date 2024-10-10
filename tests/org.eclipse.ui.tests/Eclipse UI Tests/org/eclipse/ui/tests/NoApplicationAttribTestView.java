/*******************************************************************************
 * Copyright (c) 2024 Enda O'Brien and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which accompanies this distribution,
 * and is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors: Enda O'Brien, Pilz Ireland - PR #144
 *******************************************************************************/
package org.eclipse.ui.tests;

import org.eclipse.ui.views.markers.MarkerSupportView;

/**
 * A test view that does not define the markerTypeReference application
 * attribute in its content generator (CONTENT_GEN_ID).
 *
 */
public class NoApplicationAttribTestView extends MarkerSupportView {
	public static final String ID = "org.eclipse.ui.tests.noApplicationAttribTestView";

	static final String CONTENT_GEN_ID = "org.eclipse.ui.tests.noApplicationAttribTestViewContentGenerator";

	public NoApplicationAttribTestView() {
		super(CONTENT_GEN_ID);
	}

}
