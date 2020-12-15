/*******************************************************************************
 * Copyright (c) 2020 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.ui.internal.editors.text.folding;

import org.eclipse.jface.text.source.projection.ProjectionViewer;

import org.eclipse.ui.internal.editors.text.TextOperationActionHandler;

public class CollapseHandler extends TextOperationActionHandler {

	public CollapseHandler() {
		super(ProjectionViewer.COLLAPSE);
	}
}
