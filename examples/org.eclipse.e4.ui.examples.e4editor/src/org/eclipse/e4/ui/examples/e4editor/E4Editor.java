/*******************************************************************************
* Copyright (c) 2025 Feilim Breatnach and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License 2.0 which accompanies this distribution,
* and is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors: Feilim Breatnach, Pilz Ireland
*******************************************************************************/

package org.eclipse.e4.ui.examples.e4editor;

import jakarta.annotation.PostConstruct;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class E4Editor {

	@PostConstruct
	public void postConstruct(Composite parent) {
		Text label = new Text(parent, SWT.ITALIC);
		label.setText("E4 Editor Content...");

	}
}
