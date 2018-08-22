/*******************************************************************************
 * Copyright (c) 2008, 2017 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 124684)
 *     IBM Corporation - through ListBinding.java
 *     Matthew Hall - bugs 271148, 278550
 ******************************************************************************/

package org.eclipse.core.databinding;

import java.util.Collections;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.ISetChangeListener;
import org.eclipse.core.databinding.observable.set.SetDiff;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.internal.databinding.BindingStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;

/**
 * @param <T>
 *            the type of the elements in the set on the target side
 * @param <M>
 *            the type of the elements in the set on the model side
 * @since 1.1
 *
 */
public class SetBinding<M, T> extends Binding {

	private UpdateSetStrategy<? super T, ? extends M> targetToModel;
	private UpdateSetStrategy<? super M, ? extends T> modelToTarget;
	private IObservableValue<IStatus> validationStatusObservable;
	private IObservableSet<T> target;
	private IObservableSet<M> model;
	private boolean updatingTarget;
	private boolean updatingModel;

	private ISetChangeListener<T> targetChangeListener = event -> {
		if (!updatingTarget) {
			doUpdate(target, model, event.diff, targetToModel, false, false);
		}
	};

	private ISetChangeListener<M> modelChangeListener = event -> {
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
	public SetBinding(IObservableSet<T> target, IObservableSet<M> model,
			UpdateSetStrategy<? super T, ? extends M> targetToModelStrategy,
			UpdateSetStrategy<? super M, ? extends T> modelToTargetStrategy) {
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
					context.getValidationRealm(), Status.OK_STATUS, IStatus.class);
		} finally {
			ObservableTracker.setIgnore(false);
		}
	}

	@Override
	protected void postInit() {
		if (modelToTarget.getUpdatePolicy() == UpdateSetStrategy.POLICY_UPDATE) {
			model.getRealm().exec(() -> {
				model.addSetChangeListener(modelChangeListener);
				updateModelToTarget();
			});
		} else {
			modelChangeListener = null;
		}

		if (targetToModel.getUpdatePolicy() == UpdateSetStrategy.POLICY_UPDATE) {
			target.getRealm().exec(() -> {
				target.addSetChangeListener(targetChangeListener);
				if (modelToTarget.getUpdatePolicy() == UpdateSetStrategy.POLICY_NEVER) {
					// we have to sync from target to model, if the other
					// way round (model to target) is forbidden (POLICY_NEVER)
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
			SetDiff<M> diff = Diffs.computeSetDiff(Collections.emptySet(), model);
			doUpdate(model, target, diff, modelToTarget, true, true);
		});
	}

	@Override
	public void updateTargetToModel() {
		target.getRealm().exec(() -> {
			SetDiff<T> diff = Diffs.computeSetDiff(Collections.emptySet(), target);
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
	 * This method may be moved to UpdateSetStrategy in the future if clients
	 * need more control over how the two sets are kept in sync.
	 */
	private <S, D1, D2 extends D1> void doUpdate(final IObservableSet<S> source, final IObservableSet<D1> destination,
			final SetDiff<? extends S> diff, final UpdateSetStrategy<? super S, D2> updateSetStrategy,
			final boolean explicit, final boolean clearDestination) {
		final int policy = updateSetStrategy.getUpdatePolicy();
		if (policy == UpdateSetStrategy.POLICY_NEVER)
			return;
		if (policy == UpdateSetStrategy.POLICY_ON_REQUEST && !explicit)
			return;
		if (!destination.getRealm().isCurrent()) {
			/*
			 * If the destination is different from the source realm, we have to avoid lazy
			 * diff calculation.
			 */
			diff.getAdditions();
			diff.getRemovals();
		}
		destination.getRealm().exec(() -> {
			if (destination == target) {
				updatingTarget = true;
			} else {
				updatingModel = true;
			}
			MultiStatus multiStatus = BindingStatus.ok();

			try {
				if (clearDestination) {
					destination.clear();
				}

				for (S element : diff.getRemovals()) {
					IStatus setterStatus1 = updateSetStrategy.doRemove(destination, updateSetStrategy.convert(element));

					mergeStatus(multiStatus, setterStatus1);
					// TODO - at this point, the two sets
					// will be out of sync if an error
					// occurred...
				}

				for (S element : diff.getAdditions()) {
					IStatus setterStatus2 = updateSetStrategy.doAdd(destination, updateSetStrategy.convert(element));

					mergeStatus(multiStatus, setterStatus2);
					// TODO - at this point, the two sets
					// will be out of sync if an error
					// occurred...
				}
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
			target.removeSetChangeListener(targetChangeListener);
			targetChangeListener = null;
		}
		if (modelChangeListener != null) {
			model.removeSetChangeListener(modelChangeListener);
			modelChangeListener = null;
		}
		super.dispose();
	}
}
