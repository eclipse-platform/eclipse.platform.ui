/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * - Lucas Bullen (Red Hat Inc.)
 *******************************************************************************/
package org.eclipse.ui.genericeditor.tests.contributions;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.reconciler.Reconciler;

public class HighlightReconciler extends Reconciler {

	private HighlightStrategy fStrategy;

	public HighlightReconciler() {
		fStrategy = new HighlightStrategy();
		this.setReconcilingStrategy(fStrategy, IDocument.DEFAULT_CONTENT_TYPE);
	}

	@Override
	public void install(ITextViewer textViewer) {
		super.install(textViewer);
		fStrategy.install(textViewer);
	}

	@Override
	public void uninstall() {
		super.uninstall();
		fStrategy.uninstall();
	}
}
