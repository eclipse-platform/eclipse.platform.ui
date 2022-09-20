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

package org.eclipse.ui.tests.markers;

import org.eclipse.ui.views.markers.IFilterHelpHandler;

public class FilterHelpHandler implements IFilterHelpHandler {

	@Override
	public void handleHelpClick() {
		System.out.println("its a help");
	}

}
