/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.core.databinding;

import java.util.Collections;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ListChangeEvent;
import org.eclipse.core.databinding.observable.list.ListDiff;
import org.eclipse.core.databinding.observable.list.ListDiffEntry;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * @since 3.3
 * 
 */
public class ListBinding extends Binding {

	private UpdateListStrategy targetToModel;
	private UpdateListStrategy modelToTarget;
	private IObservableValue validationStatusObservable;
	private boolean updatingTarget;
	private boolean updatingModel;

	private IListChangeListener targetChangeListener = new IListChangeListener() {
		public void handleListChange(ListChangeEvent event) {
			if (!updatingTarget) {
				doUpdate((IObservableList) getTarget(),
						(IObservableList) getModel(), event.diff,
						targetToModel, false, false);
			}
		}
	};
	private IListChangeListener modelChangeListener = new IListChangeListener() {
		public void handleListChange(ListChangeEvent event) {
			if (!updatingModel) {
				doUpdate((IObservableList) getModel(),
						(IObservableList) getTarget(), event.diff,
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
	public ListBinding(IObservableList target, IObservableList model,
			UpdateListStrategy targetToModelStrategy,
			UpdateListStrategy modelToTargetStrategy) {
		super(target, model);
		this.targetToModel = targetToModelStrategy;
		this.modelToTarget = modelToTargetStrategy;
		if ((targetToModel.getUpdatePolicy() & UpdateValueStrategy.POLICY_UPDATE) != 0) {
			target.addListChangeListener(targetChangeListener);
		} else {
			targetChangeListener = null;
		}
		if ((modelToTarget.getUpdatePolicy() & UpdateValueStrategy.POLICY_UPDATE) != 0) {
			model.addListChangeListener(modelChangeListener);
		} else {
			modelChangeListener = null;
		}
	}

	public IObservableValue getValidationStatus() {
		return validationStatusObservable;
	}

	protected void preInit() {
		validationStatusObservable = new WritableValue(context
				.getValidationRealm(), Status.OK_STATUS, IStatus.class);
	}

	protected void postInit() {
		if (modelToTarget.getUpdatePolicy() == UpdateListStrategy.POLICY_UPDATE) {
			updateModelToTarget();
		}
		if (targetToModel.getUpdatePolicy() != UpdateListStrategy.POLICY_NEVER) {
			validateTargetToModel();
		}
	}

	public void updateModelToTarget() {
		final IObservableList modelList = (IObservableList) getModel();
		modelList.getRealm().exec(new Runnable() {
			public void run() {
				ListDiff diff = Diffs.computeListDiff(Collections.EMPTY_LIST,
						modelList);
				doUpdate(modelList, (IObservableList) getTarget(), diff,
						modelToTarget, true, true);
			}
		});
	}

	public void updateTargetToModel() {
		final IObservableList targetList = (IObservableList) getTarget();
		targetList.getRealm().exec(new Runnable() {
			public void run() {
				ListDiff diff = Diffs.computeListDiff(Collections.EMPTY_LIST,
						targetList);
				doUpdate(targetList, (IObservableList) getModel(), diff,
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
	 * This method may be moved to UpdateListStrategy in the future if clients
	 * need more control over how the two lists are kept in sync.
	 */
	private void doUpdate(final IObservableList source,
			final IObservableList destination, final ListDiff diff,
			final UpdateListStrategy updateListStrategy,
			final boolean explicit, final boolean clearDestination) {
		final int policy = updateListStrategy.getUpdatePolicy();
		if (policy != UpdateListStrategy.POLICY_NEVER) {
			if (policy != UpdateListStrategy.POLICY_ON_REQUEST || explicit) {
				destination.getRealm().exec(new Runnable() {
					public void run() {
						if (destination == getTarget()) {
							updatingTarget = true;
						} else {
							updatingModel = true;
						}
						try {
							if (clearDestination) {
								destination.clear();
							}
							ListDiffEntry[] diffEntries = diff.getDifferences();
							for (int i = 0; i < diffEntries.length; i++) {
								ListDiffEntry listDiffEntry = diffEntries[i];
								if (listDiffEntry.isAddition()) {
									IStatus setterStatus = updateListStrategy
											.doAdd(
													destination,
													updateListStrategy
															.convert(listDiffEntry
																	.getElement()),
													listDiffEntry.getPosition());
									if (!setterStatus.isOK()) {
										validationStatusObservable
												.setValue(setterStatus);
										// TODO - at this point, the two lists
										// will be out of sync...
									}
								} else {
									IStatus setterStatus = updateListStrategy
											.doRemove(destination,
													listDiffEntry.getPosition());
									if (!setterStatus.isOK()) {
										validationStatusObservable
												.setValue(setterStatus);
										// TODO - at this point, the two lists
										// will be out of sync...
									}
								}
							}
						} finally {
							if (destination == getTarget()) {
								updatingTarget = false;
							} else {
								updatingModel = false;
							}
						}
					}
				});
			}
		}
	}

	public void dispose() {
		if (targetChangeListener != null) {
			((IObservableList)getTarget()).removeListChangeListener(targetChangeListener);
			targetChangeListener = null;
		}
		if (modelChangeListener != null) {
			((IObservableList)getModel()).removeListChangeListener(modelChangeListener);
			modelChangeListener = null;
		}
		super.dispose();
	}
}
