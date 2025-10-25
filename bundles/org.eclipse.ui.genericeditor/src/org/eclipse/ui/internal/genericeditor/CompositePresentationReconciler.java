/*******************************************************************************
 * Copyright (c) 2023 Christoph Läubrich and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Christoph Läubrich - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.genericeditor;

import java.util.List;
import java.util.Objects;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.presentation.IPresentationDamager;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.IPresentationRepairer;

class CompositePresentationReconciler implements IPresentationReconciler {

	private List<IPresentationReconciler> reconciliers;

	public CompositePresentationReconciler(List<IPresentationReconciler> reconciliers) {
		this.reconciliers = reconciliers;
	}

	@Override
	public void uninstall() {
		for (int i = reconciliers.size() - 1; i >= 0; i--) {
			IPresentationReconciler reconciler = reconciliers.get(i);
			reconciler.uninstall();
		}

	}

	@Override
	public void install(ITextViewer viewer) {
		for (IPresentationReconciler reconciler : reconciliers) {
			reconciler.install(viewer);
		}
	}

	@Override
	public IPresentationRepairer getRepairer(String contentType) {
		return reconciliers.stream().map(reconciler -> reconciler.getRepairer(contentType)).filter(Objects::nonNull)
				.findFirst().orElse(null);
	}

	@Override
	public IPresentationDamager getDamager(String contentType) {
		return reconciliers.stream().map(reconciler -> reconciler.getDamager(contentType)).filter(Objects::nonNull)
				.findFirst().orElse(null);
	}

}
