/*******************************************************************************
 * Copyright (c) 2008, 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 124684)
 *     IBM Corporation - through ListBinding.java
 *     Matthew Hall - bugs 271148, 278550
 ******************************************************************************/

package org.eclipse.core.databinding;

import java.util.Collections;
import java.util.Iterator;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.ISetChangeListener;
import org.eclipse.core.databinding.observable.set.SetChangeEvent;
import org.eclipse.core.databinding.observable.set.SetDiff;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.internal.databinding.BindingStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;

/**
 * @since 1.1
 * 
 */
public class SetBinding extends Binding {

	private UpdateSetStrategy targetToModel;
	private UpdateSetStrategy modelToTarget;
	private IObservableValue validationStatusObservable;
	private boolean updatingTarget;
	private boolean updatingModel;

	private ISetChangeListener targetChangeListener = new ISetChangeListener() {
		public void handleSetChange(SetChangeEvent event) {
			if (!updatingTarget) {
				doUpdate((IObservableSet) getTarget(),
						(IObservableSet) getModel(), event.diff, targetToModel,
						false, false);
			}
		}
	};

	private ISetChangeListener modelChangeListener = new ISetChangeListener() {
		public void handleSetChange(SetChangeEvent event) {
			if (!updatingModel) {
				doUpdate((IObservableSet) getModel(),
						(IObservableSet) getTarget(), event.diff,
						modelToTarget, false, false);
			}
		}
	};

	/**
	 * @param target
	 * @param model
	 * @param modelToTargetStrategy
	 * @param targetToModelStrategy
	 */
	public SetBinding(IObservableSet target, IObservableSet model,
			UpdateSetStrategy targetToModelStrategy,
			UpdateSetStrategy modelToTargetStrategy) {
		super(target, model);
		this.targetToModel = targetToModelStrategy;
		this.modelToTarget = modelToTargetStrategy;
		if ((targetToModel.getUpdatePolicy() & UpdateSetStrategy.POLICY_UPDATE) != 0) {
			target.addSetChangeListener(targetChangeListener);
		} else {
			targetChangeListener = null;
		}
		if ((modelToTarget.getUpdatePolicy() & UpdateSetStrategy.POLICY_UPDATE) != 0) {
			model.addSetChangeListener(modelChangeListener);
		} else {
			modelChangeListener = null;
		}
	}

	public IObservableValue getValidationStatus() {
		return validationStatusObservable;
	}

	protected void preInit() {
		ObservableTracker.setIgnore(true);
		try {
			validationStatusObservable = new WritableValue(context
					.getValidationRealm(), Status.OK_STATUS, IStatus.class);
		} finally {
			ObservableTracker.setIgnore(false);
		}
	}

	protected void postInit() {
		if (modelToTarget.getUpdatePolicy() == UpdateSetStrategy.POLICY_UPDATE) {
			updateModelToTarget();
		}
		if (targetToModel.getUpdatePolicy() == UpdateSetStrategy.POLICY_UPDATE) {
			validateTargetToModel();
		}
	}

	public void updateModelToTarget() {
		final IObservableSet modelSet = (IObservableSet) getModel();
		modelSet.getRealm().exec(new Runnable() {
			public void run() {
				SetDiff diff = Diffs.computeSetDiff(Collections.EMPTY_SET,
						modelSet);
				doUpdate(modelSet, (IObservableSet) getTarget(), diff,
						modelToTarget, true, true);
			}
		});
	}

	public void updateTargetToModel() {
		final IObservableSet targetSet = (IObservableSet) getTarget();
		targetSet.getRealm().exec(new Runnable() {
			public void run() {
				SetDiff diff = Diffs.computeSetDiff(Collections.EMPTY_SET,
						targetSet);
				doUpdate(targetSet, (IObservableSet) getModel(), diff,
						targetToModel, true, true);
			}
		});
	}

	public void validateModelToTarget() {
		// nothing for now
	}

	public void validateTargetToModel() {
		// nothing for now
	}

	/*
	 * This method may be moved to UpdateSetStrategy in the future if clients
	 * need more control over how the two sets are kept in sync.
	 */
	private void doUpdate(final IObservableSet source,
			final IObservableSet destination, final SetDiff diff,
			final UpdateSetStrategy updateSetStrategy, final boolean explicit,
			final boolean clearDestination) {
		final int policy = updateSetStrategy.getUpdatePolicy();
		if (policy == UpdateSetStrategy.POLICY_NEVER)
			return;
		if (policy == UpdateSetStrategy.POLICY_ON_REQUEST && !explicit)
			return;
		destination.getRealm().exec(new Runnable() {
			public void run() {
				if (destination == getTarget()) {
					updatingTarget = true;
				} else {
					updatingModel = true;
				}
				MultiStatus multiStatus = BindingStatus.ok();

				try {
					if (clearDestination) {
						destination.clear();
					}

					for (Iterator iterator = diff.getRemovals().iterator(); iterator
							.hasNext();) {
						IStatus setterStatus = updateSetStrategy.doRemove(
								destination, updateSetStrategy.convert(iterator
										.next()));

						mergeStatus(multiStatus, setterStatus);
						// TODO - at this point, the two sets
						// will be out of sync if an error
						// occurred...
					}

					for (Iterator iterator = diff.getAdditions().iterator(); iterator
							.hasNext();) {
						IStatus setterStatus = updateSetStrategy.doAdd(
								destination, updateSetStrategy.convert(iterator
										.next()));

						mergeStatus(multiStatus, setterStatus);
						// TODO - at this point, the two sets
						// will be out of sync if an error
						// occurred...
					}
				} finally {
					validationStatusObservable.setValue(multiStatus);

					if (destination == getTarget()) {
						updatingTarget = false;
					} else {
						updatingModel = false;
					}
				}
			}
		});
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

	public void dispose() {
		if (targetChangeListener != null) {
			((IObservableSet) getTarget())
					.removeSetChangeListener(targetChangeListener);
			targetChangeListener = null;
		}
		if (modelChangeListener != null) {
			((IObservableSet) getModel())
					.removeSetChangeListener(modelChangeListener);
			modelChangeListener = null;
		}
		super.dispose();
	}
}
