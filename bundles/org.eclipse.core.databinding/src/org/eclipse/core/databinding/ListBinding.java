/*******************************************************************************
 * Copyright (c) 2007, 2017 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matthew Hall - bugs 262221, 271148, 280341, 278550
 ******************************************************************************/

package org.eclipse.core.databinding;

import java.util.Collections;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ListDiff;
import org.eclipse.core.databinding.observable.list.ListDiffVisitor;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.internal.databinding.BindingStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;

/**
 * @param <T>
 *            the type of the elements in the list on the target side
 * @param <M>
 *            the type of the elements in the list on the model side
 * @since 1.0
 *
 */
public class ListBinding<M, T> extends Binding {

	private UpdateListStrategy<? super T, ? extends M> targetToModel;
	private UpdateListStrategy<? super M, ? extends T> modelToTarget;
	private IObservableValue<IStatus> validationStatusObservable;
	private IObservableList<T> target;
	private IObservableList<M> model;
	private boolean updatingTarget;
	private boolean updatingModel;

	private IListChangeListener<T> targetChangeListener = event -> {
		if (!updatingTarget) {
			doUpdate(target, model, event.diff, targetToModel, false, false);
		}
	};
	private IListChangeListener<M> modelChangeListener = event -> {
		if (!updatingModel) {
			doUpdate(model, target, event.diff, modelToTarget, false, false);
		}
	};

	/**
	 * @param target
	 * @param model
	 * @param modelToTargetStrategy
	 * @param targetToModelStrategy
	 */
	public ListBinding(IObservableList<T> target, IObservableList<M> model,
			UpdateListStrategy<? super T, ? extends M> targetToModelStrategy,
			UpdateListStrategy<? super M, ? extends T> modelToTargetStrategy) {
		super(target, model);
		this.target = target;
		this.model = model;
		this.targetToModel = targetToModelStrategy;
		this.modelToTarget = modelToTargetStrategy;
	}

	@Override
	public IObservableValue<IStatus> getValidationStatus() {
		return validationStatusObservable;
	}

	@Override
	protected void preInit() {
		ObservableTracker.setIgnore(true);
		try {
			validationStatusObservable = new WritableValue<>(
					context.getValidationRealm(), Status.OK_STATUS,
					IStatus.class);
		} finally {
			ObservableTracker.setIgnore(false);
		}
	}

	@Override
	protected void postInit() {
		if (modelToTarget.getUpdatePolicy() == UpdateListStrategy.POLICY_UPDATE) {
			model.getRealm().exec(() -> {
				model.addListChangeListener(modelChangeListener);
				updateModelToTarget();
			});
		} else {
			modelChangeListener = null;
		}

		if (targetToModel.getUpdatePolicy() == UpdateListStrategy.POLICY_UPDATE) {
			target.getRealm().exec(() -> {
				target.addListChangeListener(targetChangeListener);
				if (modelToTarget.getUpdatePolicy() == UpdateListStrategy.POLICY_NEVER) {
					// we have to sync from target to model, if the other
					// way round (model to target) is forbidden
					// (POLICY_NEVER)
					updateTargetToModel();
				} else {
					validateTargetToModel();
				}
			});
		} else {
			targetChangeListener = null;
		}
	}

	@Override
	public void updateModelToTarget() {
		model.getRealm().exec(() -> {
			ListDiff<M> diff = Diffs.computeListDiff(Collections.emptyList(), model);
			doUpdate(model, target, diff, modelToTarget, true, true);
		});
	}

	@Override
	public void updateTargetToModel() {
		target.getRealm().exec(() -> {
			ListDiff<T> diff = Diffs.computeListDiff(Collections.emptyList(), target);
			doUpdate(target, model, diff, targetToModel, true, true);
		});
	}

	@Override
	public void validateModelToTarget() {
		// nothing for now
	}

	@Override
	public void validateTargetToModel() {
		// nothing for now
	}

	/*
	 * This method may be moved to UpdateListStrategy in the future if clients
	 * need more control over how the two lists are kept in sync.
	 */
	private <S, D1, D2 extends D1> void doUpdate(final IObservableList<S> source,
			final IObservableList<D1> destination, final ListDiff<? extends S> diff,
			final UpdateListStrategy<? super S, D2> updateListStrategy,
			final boolean explicit, final boolean clearDestination) {
		final int policy = updateListStrategy.getUpdatePolicy();
		if (policy != UpdateListStrategy.POLICY_NEVER) {
			if (policy != UpdateListStrategy.POLICY_ON_REQUEST || explicit) {
				if (!destination.getRealm().isCurrent()) {
					/*
					 * If the destination is different from the source realm, we have to avoid lazy
					 * diff calculation.
					 */
					diff.getDifferences();
				}
				destination.getRealm().exec(() -> {
					if (destination == target) {
						updatingTarget = true;
					} else {
						updatingModel = true;
					}
					final MultiStatus multiStatus = BindingStatus.ok();

					try {
						if (clearDestination) {
							destination.clear();
						}
						diff.accept(new ListDiffVisitor<S>() {
							boolean useMoveAndReplace = updateListStrategy.useMoveAndReplace();

							@Override
							public void handleAdd(int index, S element) {
								IStatus setterStatus = updateListStrategy.doAdd(destination,
										updateListStrategy.convert(element), index);

								mergeStatus(multiStatus, setterStatus);
							}

							@Override
							public void handleRemove(int index, S element) {
								IStatus setterStatus = updateListStrategy.doRemove(destination, index);
								mergeStatus(multiStatus, setterStatus);
							}

							@Override
							public void handleMove(int oldIndex, int newIndex, S element) {
								if (useMoveAndReplace) {
									IStatus setterStatus = updateListStrategy
											.doMove(destination, oldIndex, newIndex);

									mergeStatus(multiStatus, setterStatus);
								} else {
									super.handleMove(oldIndex, newIndex, element);
								}
							}

							@Override
							public void handleReplace(int index, S oldElement, S newElement) {
								if (useMoveAndReplace) {
									IStatus setterStatus = updateListStrategy.doReplace(
										destination, index, updateListStrategy.convert(newElement));
									mergeStatus(multiStatus, setterStatus);
								} else {
									super.handleReplace(index, oldElement, newElement);
								}
							}
						});
						// TODO - at this point, the two lists will be out
						// of sync if an error occurred...
					} finally {
						setValidationStatus(multiStatus);

						if (destination == target) {
							updatingTarget = false;
						} else {
							updatingModel = false;
						}
					}
				});
			}
		}
	}

	private void setValidationStatus(final IStatus status) {
		validationStatusObservable.getRealm().exec(() -> validationStatusObservable.setValue(status));
	}

	/**
	 * Merges the provided <code>newStatus</code> into the
	 * <code>multiStatus</code>.
	 *
	 * @param multiStatus
	 * @param newStatus
	 */
	/* package */void mergeStatus(MultiStatus multiStatus, IStatus newStatus) {
		if (!newStatus.isOK()) {
			multiStatus.add(newStatus);
		}
	}

	@Override
	public void dispose() {
		if (targetChangeListener != null) {
			target.removeListChangeListener(targetChangeListener);
			targetChangeListener = null;
		}
		if (modelChangeListener != null) {
			model.removeListChangeListener(modelChangeListener);
			modelChangeListener = null;
		}
		super.dispose();
	}
}
