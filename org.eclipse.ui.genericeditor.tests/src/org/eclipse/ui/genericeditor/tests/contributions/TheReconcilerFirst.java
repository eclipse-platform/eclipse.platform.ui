/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Lucas Bullen (Red Hat Inc.) - initial implementation
 *******************************************************************************/
package org.eclipse.ui.genericeditor.tests.contributions;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.reconciler.Reconciler;

public class TheReconcilerFirst extends Reconciler{
	public TheReconcilerFirst() {
        this.setReconcilingStrategy(new ReconcilerStrategyFirst(), IDocument.DEFAULT_CONTENT_TYPE);
	}
}
