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
 *   Lucas Bullen (Red Hat Inc.) - initial implementation
 *******************************************************************************/
package org.eclipse.ui.internal.genericeditor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.IReconcilerExtension;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;

public class CompositeReconciler implements IReconciler, IReconcilerExtension {
	private final List<IReconciler> fReconcilers;

	public CompositeReconciler(List<IReconciler> reconcilers) {
		fReconcilers = reconcilers.stream().filter(Objects::nonNull).collect(Collectors.toList());
	}

	@Override
	public String getDocumentPartitioning() {
		boolean defaultFound = false;
		String[] types = (String[]) fReconcilers.stream()
			.filter(IReconcilerExtension.class::isInstance)
			.map(IReconcilerExtension.class::cast)
			.map(IReconcilerExtension::getDocumentPartitioning)
			.filter(Objects::nonNull)
			.toArray();
		for (String type : types) {
			if (type.equals(IDocumentExtension3.DEFAULT_PARTITIONING)) {
				defaultFound = true;
			} else {
				return type;
			}
		}
		return defaultFound ? IDocumentExtension3.DEFAULT_PARTITIONING : null;
	}

	@Override
	public void install(ITextViewer textViewer) {
		for (IReconciler iReconciler : fReconcilers) {
			iReconciler.install(textViewer);
		}
	}

	@Override
	public void uninstall() {
		for (IReconciler iReconciler : fReconcilers) {
			iReconciler.uninstall();
		}
	}

	@Override
	public IReconcilingStrategy getReconcilingStrategy(String contentType) {
		List<IReconcilingStrategy> strategies = new ArrayList<>();
		for (IReconciler iReconciler : fReconcilers) {
			IReconcilingStrategy strategy = iReconciler.getReconcilingStrategy(contentType);
			if(strategy != null) {
				strategies.add(strategy);
			}
		}

		if(strategies.size() == 1) {
			return strategies.get(0);
		}

		return new CompositeReconcilerStrategy(strategies);

	}
}
