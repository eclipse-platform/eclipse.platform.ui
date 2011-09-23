/*******************************************************************************
 * Copyright (c) 2008, 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 218269)
 *     Boris Bokowski - bug 218269
 *     Matthew Hall - bug 237884, 240590, 251003, 251424, 278550, 332504
 *     Ovidio Mallo - bug 238909, 235859
 *     Stefan R�ck - bug 332504 
 ******************************************************************************/

package org.eclipse.core.databinding.validation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.databinding.ValidationStatusProvider;
import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.IStaleListener;
import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.Observables;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.StaleEvent;
import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ListChangeEvent;
import org.eclipse.core.databinding.observable.list.ListDiffVisitor;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.value.AbstractObservableValue;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.internal.databinding.Util;
import org.eclipse.core.internal.databinding.validation.ValidatedObservableList;
import org.eclipse.core.internal.databinding.validation.ValidatedObservableMap;
import org.eclipse.core.internal.databinding.validation.ValidatedObservableSet;
import org.eclipse.core.internal.databinding.validation.ValidatedObservableValue;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;

/**
 * A validator for cross-constraints between observables.
 * 
 * <p>
 * Some practical examples of cross-constraints:
 * <ul>
 * <li>A start date cannot be later than an end date
 * <li>A list of percentages should add up to 100%
 * </ul>
 * <p>
 * Example: require two integer fields to contain either both even or both odd
 * numbers.
 * 
 * <pre>
 * DataBindingContext dbc = new DataBindingContext();
 * 
 * IObservableValue target0 = SWTObservables.observeText(text0, SWT.Modify);
 * IObservableValue target1 = SWTObservables.observeText(text1, SWT.Modify);
 * 
 * // Binding in two stages (from target to middle, then from middle to model)
 * // simplifies the validation logic.  Using the middle observables saves
 * // the trouble of converting the target values (Strings) to the model type
 * // (integers) manually during validation.
 * final IObservableValue middle0 = new WritableValue(null, Integer.TYPE);
 * final IObservableValue middle1 = new WritableValue(null, Integer.TYPE);
 * dbc.bind(target0, middle0, null, null);
 * dbc.bind(target1, middle1, null, null);
 * 
 * // Create the multi-validator
 * MultiValidator validator = new MultiValidator() {
 * 	protected IStatus validate() {
 * 		// Calculate the validation status
 * 		Integer value0 = (Integer) middle0.getValue();
 * 		Integer value1 = (Integer) middle1.getValue();
 * 		if (Math.abs(value0.intValue()) % 2 != Math.abs(value1.intValue()) % 2)
 * 			return ValidationStatus
 * 					.error(&quot;Values must be both even or both odd&quot;);
 * 		return ValidationStatus.ok();
 * 	}
 * };
 * dbc.addValidationStatusProvider(validator);
 * 
 * // Bind the middle observables to the model observables. 
 * IObservableValue model0 = new WritableValue(new Integer(2), Integer.TYPE);
 * IObservableValue model1 = new WritableValue(new Integer(4), Integer.TYPE);
 * dbc.bind(middle0, model0, null, null);
 * dbc.bind(middle1, model1, null, null);
 * </pre>
 * 
 * <p>
 * MultiValidator can also prevent invalid data from being copied to model. This
 * is done by wrapping each target observable in a validated observable, and
 * then binding the validated observable to the model.
 * 
 * <pre>
 * 
 * ...
 * 
 * // Validated observables do not change value until the validator passes. 
 * IObservableValue validated0 = validator.observeValidatedValue(middle0);
 * IObservableValue validated1 = validator.observeValidatedValue(middle1);
 * IObservableValue model0 = new WritableValue(new Integer(2), Integer.TYPE);
 * IObservableValue model1 = new WritableValue(new Integer(4), Integer.TYPE);
 * // Bind to the validated value, not the middle/target
 * dbc.bind(validated0, model0, null, null);
 * dbc.bind(validated1, model1, null, null);
 * </pre>
 * 
 * Note: No guarantee is made as to the order of updates when multiple validated
 * observables change value at once (i.e. multiple updates pending when the
 * status becomes valid). Therefore the model may be in an invalid state after
 * the first but before the last pending update.
 * 
 * @since 1.1
 */
public abstract class MultiValidator extends ValidationStatusProvider {
	private Realm realm;
	private ValidationStatusObservableValue validationStatus;
	private IObservableValue unmodifiableValidationStatus;
	private WritableList targets;
	private IObservableList unmodifiableTargets;
	private IObservableList models;

	IListChangeListener targetsListener = new IListChangeListener() {
		public void handleListChange(ListChangeEvent event) {
			event.diff.accept(new ListDiffVisitor() {
				public void handleAdd(int index, Object element) {
					IObservable dependency = (IObservable) element;
					dependency.addChangeListener(dependencyListener);
					dependency.addStaleListener(dependencyListener);
				}

				public void handleRemove(int index, Object element) {
					IObservable dependency = (IObservable) element;
					dependency.removeChangeListener(dependencyListener);
					dependency.removeStaleListener(dependencyListener);
				}
			});
		}
	};

	private class DependencyListener implements IChangeListener, IStaleListener {
		public void handleChange(ChangeEvent event) {
			revalidate();
		}

		public void handleStale(StaleEvent staleEvent) {
			validationStatus.makeStale();
		}
	}

	private DependencyListener dependencyListener = new DependencyListener();

	/**
	 * Constructs a MultiValidator on the default realm.
	 */
	public MultiValidator() {
		this(Realm.getDefault());
	}

	/**
	 * Constructs a MultiValidator on the given realm.
	 * 
	 * @param realm
	 *            the realm on which validation takes place.
	 */
	public MultiValidator(Realm realm) {
		Assert.isNotNull(realm, "Realm cannot be null"); //$NON-NLS-1$
		this.realm = realm;

		ObservableTracker.setIgnore(true);
		try {
			validationStatus = new ValidationStatusObservableValue(realm);

			targets = new WritableList(realm, new ArrayList(),
					IObservable.class);
			targets.addListChangeListener(targetsListener);
			unmodifiableTargets = Observables
					.unmodifiableObservableList(targets);

			models = Observables.emptyObservableList(realm);
		} finally {
			ObservableTracker.setIgnore(false);
		}
	}

	private void checkObservable(IObservable target) {
		Assert.isNotNull(target, "Target observable cannot be null"); //$NON-NLS-1$
		Assert
				.isTrue(realm.equals(target.getRealm()),
						"Target observable must be in the same realm as MultiValidator"); //$NON-NLS-1$
	}

	/**
	 * Returns an {@link IObservableValue} whose value is always the current
	 * validation status of this MultiValidator. The returned observable is in
	 * the same realm as this MultiValidator.
	 * 
	 * @return an {@link IObservableValue} whose value is always the current
	 *         validation status of this MultiValidator.
	 */
	public IObservableValue getValidationStatus() {
		if (unmodifiableValidationStatus == null) {
			ObservableTracker.setIgnore(true);
			try {
				unmodifiableValidationStatus = Observables
						.unmodifiableObservableValue(validationStatus);
			} finally {
				ObservableTracker.setIgnore(false);
			}
			revalidate();
		}
		return unmodifiableValidationStatus;
	}

	/**
	 * Signals that a re-evaluation of the current validation status is
	 * necessary.
	 * <p>
	 * Clients may invoke this method whenever the validation status needs to be
	 * updated due to some state change which cannot be automatically tracked by
	 * the MultiValidator as it is not captured by any {@link IObservable}
	 * instance.
	 * <p>
	 * Note: There is no guarantee as of whether the MultiValidator will
	 * immediately re-evaluate the validation status by calling
	 * {@link #validate} when becoming dirty. Instead, it may decide to perform
	 * the re-evaluation lazily.
	 * 
	 * @see #validate()
	 * @since 1.2
	 */
	protected final void revalidate() {
		class ValidationRunnable implements Runnable {
			IStatus validationResult;

			public void run() {
				try {
					validationResult = validate();
					if (validationResult == null)
						validationResult = ValidationStatus.ok();
				} catch (RuntimeException e) {
					// Usually an NPE as dependencies are init'ed
					validationResult = ValidationStatus
							.error(e.getMessage(), e);
				}
			}
		}

		ValidationRunnable validationRunnable = new ValidationRunnable();
		final IObservable[] dependencies = ObservableTracker.runAndMonitor(
				validationRunnable, null, null);

		ObservableTracker.setIgnore(true);
		try {
			List newTargets = new ArrayList(Arrays.asList(dependencies));

			// Internal observables should not be dependencies
			// (prevent dependency loop)
			for (Iterator itNew = newTargets.iterator(); itNew.hasNext();) {
				Object newDependency = itNew.next();
				if (newDependency == validationStatus
						|| newDependency == unmodifiableValidationStatus
						|| newDependency == targets
						|| newDependency == unmodifiableTargets
						|| newDependency == models) {
					itNew.remove();
				}
			}

			// This loop is roughly equivalent to:
			// targets.retainAll(newTargets);
			// newTargets.removeAll(targets);
			// Except that dependencies are compared by identity instead of
			// equality
			outer: for (int i = targets.size() - 1; i >= 0; i--) {
				Object oldDependency = targets.get(i);
				for (Iterator itNew = newTargets.iterator(); itNew.hasNext();) {
					Object newDependency = itNew.next();
					if (oldDependency == newDependency) {
						// Dependency is already known--remove from list of
						// new dependencies
						itNew.remove();
						continue outer;
					}
				}
				// Old dependency is no longer a dependency--remove from
				// targets
				targets.remove(i);
			}

			targets.addAll(newTargets);
		} finally {
			ObservableTracker.setIgnore(false);
		}

		// Once the dependencies are up-to-date, we set the new status.
		validationStatus.setValue(validationRunnable.validationResult);
	}

	/**
	 * Returns the current validation status.
	 * <p>
	 * Note: To ensure that the validation status is kept current automatically,
	 * all dependencies used to calculate status should be accessed through
	 * {@link IObservable} instances. Each dependency observable must be in the
	 * same realm as the MultiValidator. Other dependencies not captured by the
	 * state of those observables may be accounted for by having clients
	 * <i>explicitly</i> call {@link #revalidate()} whenever the validation
	 * status needs to be re-evaluated due to some arbitrary change in the
	 * application state.
	 * 
	 * @return the current validation status.
	 * 
	 * @see #revalidate()
	 */
	protected abstract IStatus validate();

	/**
	 * Returns a wrapper {@link IObservableValue} which stays in sync with the
	 * given target observable only when the validation status is valid.
	 * Statuses of {@link IStatus#OK OK}, {@link IStatus#INFO INFO} or
	 * {@link IStatus#WARNING WARNING} severity are considered valid.
	 * <p>
	 * The wrapper behaves as follows with respect to the validation status:
	 * <ul>
	 * <li>While valid, the wrapper stays in sync with its target observable.
	 * <li>While invalid, the wrapper's value is the target observable's last
	 * valid value. If the target changes value, a stale event is fired
	 * signaling that a change is pending.
	 * <li>When status changes from invalid to valid, the wrapper takes the
	 * value of the target observable, and synchronization resumes.
	 * </ul>
	 * 
	 * @param target
	 *            the target observable being wrapped. Must be in the same realm
	 *            as the MultiValidator.
	 * @return an IObservableValue which stays in sync with the given target
	 *         observable only with the validation status is valid.
	 */
	public IObservableValue observeValidatedValue(IObservableValue target) {
		checkObservable(target);
		return new ValidatedObservableValue(target, getValidationStatus());
	}

	/**
	 * Returns a wrapper {@link IObservableList} which stays in sync with the
	 * given target observable only when the validation status is valid.
	 * Statuses of {@link IStatus#OK OK}, {@link IStatus#INFO INFO} or
	 * {@link IStatus#WARNING WARNING} severity are considered valid.
	 * <p>
	 * The wrapper behaves as follows with respect to the validation status:
	 * <ul>
	 * <li>While valid, the wrapper stays in sync with its target observable.
	 * <li>While invalid, the wrapper's elements are the target observable's
	 * last valid elements. If the target changes elements, a stale event is
	 * fired signaling that a change is pending.
	 * <li>When status changes from invalid to valid, the wrapper takes the
	 * elements of the target observable, and synchronization resumes.
	 * </ul>
	 * 
	 * @param target
	 *            the target observable being wrapped. Must be in the same realm
	 *            as the MultiValidator.
	 * @return an IObservableValue which stays in sync with the given target
	 *         observable only with the validation status is valid.
	 */
	public IObservableList observeValidatedList(IObservableList target) {
		checkObservable(target);
		return new ValidatedObservableList(target, getValidationStatus());
	}

	/**
	 * Returns a wrapper {@link IObservableSet} which stays in sync with the
	 * given target observable only when the validation status is valid.
	 * Statuses of {@link IStatus#OK OK}, {@link IStatus#INFO INFO} or
	 * {@link IStatus#WARNING WARNING} severity are considered valid.
	 * <p>
	 * The wrapper behaves as follows with respect to the validation status:
	 * <ul>
	 * <li>While valid, the wrapper stays in sync with its target observable.
	 * <li>While invalid, the wrapper's elements are the target observable's
	 * last valid elements. If the target changes elements, a stale event is
	 * fired signaling that a change is pending.
	 * <li>When status changes from invalid to valid, the wrapper takes the
	 * elements of the target observable, and synchronization resumes.
	 * </ul>
	 * 
	 * @param target
	 *            the target observable being wrapped. Must be in the same realm
	 *            as the MultiValidator.
	 * @return an IObservableValue which stays in sync with the given target
	 *         observable only with the validation status is valid.
	 */
	public IObservableSet observeValidatedSet(IObservableSet target) {
		checkObservable(target);
		return new ValidatedObservableSet(target, getValidationStatus());
	}

	/**
	 * Returns a wrapper {@link IObservableMap} which stays in sync with the
	 * given target observable only when the validation status is valid.
	 * Statuses of {@link IStatus#OK OK}, {@link IStatus#INFO INFO} or
	 * {@link IStatus#WARNING WARNING} severity are considered valid.
	 * <p>
	 * The wrapper behaves as follows with respect to the validation status:
	 * <ul>
	 * <li>While valid, the wrapper stays in sync with its target observable.
	 * <li>While invalid, the wrapper's entries are the target observable's last
	 * valid entries. If the target changes entries, a stale event is fired
	 * signaling that a change is pending.
	 * <li>When status changes from invalid to valid, the wrapper takes the
	 * entries of the target observable, and synchronization resumes.
	 * </ul>
	 * 
	 * @param target
	 *            the target observable being wrapped. Must be in the same realm
	 *            as the MultiValidator.
	 * @return an IObservableValue which stays in sync with the given target
	 *         observable only with the validation status is valid.
	 */
	public IObservableMap observeValidatedMap(IObservableMap target) {
		checkObservable(target);
		return new ValidatedObservableMap(target, getValidationStatus());
	}

	public IObservableList getTargets() {
		return unmodifiableTargets;
	}

	public IObservableList getModels() {
		return models;
	}

	public void dispose() {
		if (targets != null) {
			targets.clear(); // Remove listeners from dependencies
		}

		if (unmodifiableValidationStatus != null) {
			unmodifiableValidationStatus.dispose();
			unmodifiableValidationStatus = null;
		}

		if (validationStatus != null) {
			validationStatus.dispose();
			validationStatus = null;
		}

		if (unmodifiableTargets != null) {
			unmodifiableTargets.dispose();
			unmodifiableTargets = null;
		}

		if (targets != null) {
			targets.dispose();
			targets = null;
		}

		if (models != null) {
			models.dispose();
			models = null;
		}

		realm = null;

		super.dispose();
	}

	private class ValidationStatusObservableValue extends
			AbstractObservableValue {
		private Object value = ValidationStatus.ok();

		private boolean stale = false;

		public ValidationStatusObservableValue(Realm realm) {
			super(realm);
		}

		protected Object doGetValue() {
			return value;
		}

		protected void doSetValue(Object value) {
			boolean oldStale = stale;

			// Update the staleness state by checking whether any of the current
			// dependencies is stale.
			stale = false;
			for (Iterator iter = targets.iterator(); iter.hasNext();) {
				IObservable dependency = (IObservable) iter.next();
				if (dependency.isStale()) {
					stale = true;
					break;
				}
			}

			Object oldValue = this.value;
			this.value = value;

			// If either becoming non-stale or setting a new value, we must fire
			// a value change event.
			if ((oldStale && !stale) || !Util.equals(oldValue, value)) {
				fireValueChange(Diffs.createValueDiff(oldValue, value));
			} else if (!oldStale && stale) {
				fireStale();
			}
		}

		void makeStale() {
			if (!stale) {
				stale = true;
				fireStale();
			}
		}

		public boolean isStale() {
			ObservableTracker.getterCalled(this);
			return stale;
		}

		public Object getValueType() {
			return IStatus.class;
		}
	}
}
