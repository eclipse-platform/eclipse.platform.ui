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
package org.eclipse.ui.internal.genericeditor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;

public class CompositeReconcilerStrategy implements IReconcilingStrategy, IReconcilingStrategyExtension{
	private List<IReconcilingStrategy> fReconcilingStrategies;

	public CompositeReconcilerStrategy(List<IReconciler> reconcilers, String contentType) {
		this.fReconcilingStrategies = new ArrayList<>();
		for (IReconciler iReconciler : reconcilers) {
			IReconcilingStrategy strategy = iReconciler.getReconcilingStrategy(contentType);
			if(strategy != null) {
				fReconcilingStrategies.add(strategy);
			}
		}
	}
	@Override
	public void setProgressMonitor(IProgressMonitor monitor) {
		for (IReconcilingStrategy iReconcilingStrategy : fReconcilingStrategies) {
			if (iReconcilingStrategy instanceof IReconcilingStrategyExtension) {
				((IReconcilingStrategyExtension) iReconcilingStrategy).setProgressMonitor(monitor);
			}
		}
	}

	@Override
	public void initialReconcile() {
		for (IReconcilingStrategy iReconcilingStrategy : fReconcilingStrategies) {
			if (iReconcilingStrategy instanceof IReconcilingStrategyExtension) {
				((IReconcilingStrategyExtension) iReconcilingStrategy).initialReconcile();
			}
		}
	}

	@Override
	public void setDocument(IDocument document) {
		for (IReconcilingStrategy iReconcilingStrategy : fReconcilingStrategies) {
			iReconcilingStrategy.setDocument(document);
		}
	}

	@Override
	public void reconcile(DirtyRegion dirtyRegion, IRegion subRegion) {
		for (IReconcilingStrategy iReconcilingStrategy : fReconcilingStrategies) {
			iReconcilingStrategy.reconcile(dirtyRegion, subRegion);
		}
	}

	@Override
	public void reconcile(IRegion partition) {
		for (IReconcilingStrategy iReconcilingStrategy : fReconcilingStrategies) {
			iReconcilingStrategy.reconcile(partition);
		}
	}

}
