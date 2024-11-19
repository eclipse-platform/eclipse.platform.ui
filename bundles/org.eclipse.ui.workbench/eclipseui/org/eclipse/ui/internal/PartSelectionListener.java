/*******************************************************************************
 * Copyright (c) 2019 Remain BV, Netherlands
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wim Jongman - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import java.util.function.Predicate;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.INullSelectionListener;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.SelectionListenerFactory;
import org.eclipse.ui.SelectionListenerFactory.ISelectionModel;

/**
 * An intermediate selection listener that filters selection change events based
 * on one or more predicates and forwards it to the functional selection
 * listener.
 *
 * Instances of this class can be obtained via the
 * {@link SelectionListenerFactory} class.
 */
public class PartSelectionListener implements ISelectionListener, INullSelectionListener {

	private final ISelectionListener fCallbackListener;
	private final IWorkbenchPart fTargetPart;
	private Predicate<ISelectionModel> fPredicate;

	private IWorkbenchPart fCurrentSelectionPart;
	private IWorkbenchPart fLastDeliveredPart;
	private ISelection fCurrentSelection;
	private ISelection fLastDeliveredSelection;

	/**
	 * Constructs the intermediate selection listener to filter selections before
	 * they are passed on the the callback listener.
	 *
	 * @param part             the part which may not be null.
	 * @param callbackListener the callback listener which may not be null.
	 * @param predicate        the predicate which may not be null.
	 */
	public PartSelectionListener(IWorkbenchPart part, ISelectionListener callbackListener,
			Predicate<ISelectionModel> predicate) {
		Assert.isNotNull(part);
		Assert.isNotNull(callbackListener);
		Assert.isNotNull(predicate);
		fTargetPart = part;
		fCallbackListener = callbackListener;
		fPredicate = predicate;
		addPartListener(part);
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		saveCurrentSelection(part, selection);
		if (fPredicate.test(getModel())) {
			doCallback(part, selection);
		}
	}

	private void doCallback(IWorkbenchPart part, ISelection selection) {
		if (selection == null && (fCallbackListener instanceof INullSelectionListener)) {
			fCallbackListener.selectionChanged(part, selection);
			saveLastDelivered(part, selection);
		} else if (selection != null) {
			fCallbackListener.selectionChanged(part, selection);
			saveLastDelivered(part, selection);
		}
	}

	private ISelectionModel getModel() {
		return new ISelectionModel() {

			@Override
			public IWorkbenchPart getTargetPart() {
				return fTargetPart;
			}

			@Override
			public IWorkbenchPart getLastDeliveredSelectionPart() {
				return fLastDeliveredPart;
			}

			@Override
			public ISelection getLastDeliveredSelection() {
				return fLastDeliveredSelection;
			}

			@Override
			public IWorkbenchPart getCurrentSelectionPart() {
				return fCurrentSelectionPart;
			}

			@Override
			public ISelection getCurrentSelection() {
				return fCurrentSelection;
			}

			@Override
			public boolean isTargetPartVisible() {
				return fTargetPart.getSite().getPage().isPartVisible(fTargetPart);
			}

			@Override
			public boolean isSelectionPartVisible() {
				return getCurrentSelectionPart() != null
						&& getCurrentSelectionPart().getSite().getPage().isPartVisible(getCurrentSelectionPart());
			}
		};
	}

	private void addPartListener(IWorkbenchPart part) {
		IPartService partService = part.getSite().getService(IPartService.class);
		partService.addPartListener(new IPartListener2() {
			@Override
			public void partVisible(IWorkbenchPartReference partRef) {
				if (partRef.getPart(false) == fTargetPart) {
					selectionChanged(fCurrentSelectionPart, fCurrentSelection);
				}
			}

			@Override
			public void partClosed(IWorkbenchPartReference partRef) {
				if (partRef.getPart(false) == fTargetPart) {
					fTargetPart.getSite().getPage().removeSelectionListener(PartSelectionListener.this);
				}
			}
		});
	}

	private void saveCurrentSelection(IWorkbenchPart part, ISelection selection) {
		fCurrentSelectionPart = part;
		fCurrentSelection = selection;
	}

	private void saveLastDelivered(IWorkbenchPart part, ISelection selection) {
		fLastDeliveredPart = part;
		fLastDeliveredSelection = selection;
	}

	/**
	 * And-chains this predicate to the already existing predicate. Nothing happens
	 * if the passed predicate is null.
	 *
	 * @param predicate the non-null predicate to and-chain to the existing
	 *                  predicate
	 * @return this
	 */
	public PartSelectionListener addPredicate(Predicate<ISelectionModel> predicate) {
		if (predicate != null) {
			fPredicate = fPredicate.and(predicate);
		}
		return this;
	}
}