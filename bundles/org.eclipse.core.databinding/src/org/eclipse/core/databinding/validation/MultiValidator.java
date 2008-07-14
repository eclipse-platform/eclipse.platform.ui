/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 218269)
 *     Boris Bokowski - bug 218269
 *     Matthew Hall - bug 237884, 240590
 ******************************************************************************/

package org.eclipse.core.databinding.validation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.databinding.ValidationStatusProvider;
import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.Observables;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ListChangeEvent;
import org.eclipse.core.databinding.observable.list.ListDiffVisitor;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.internal.databinding.observable.ValidatedObservableList;
import org.eclipse.core.internal.databinding.observable.ValidatedObservableMap;
import org.eclipse.core.internal.databinding.observable.ValidatedObservableSet;
import org.eclipse.core.internal.databinding.observable.ValidatedObservableValue;
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
	private IObservableValue validationStatus;
	private IObservableValue unmodifiableValidationStatus;
	private WritableList targets;
	private IObservableList unmodifiableTargets;
	private IObservableList models;

	IListChangeListener targetsListener = new IListChangeListener() {
		public void handleListChange(ListChangeEvent event) {
			event.diff.accept(new ListDiffVisitor() {
				public void handleAdd(int index, Object element) {
					((IObservable) element)
							.addChangeListener(dependencyListener);
				}

				public void handleRemove(int index, Object element) {
					((IObservable) element)
							.removeChangeListener(dependencyListener);
				}
			});
		}
	};

	private IChangeListener dependencyListener = new IChangeListener() {
		public void handleChange(ChangeEvent event) {
			revalidate();
		}
	};

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

		validationStatus = new WritableValue(realm, ValidationStatus.ok(),
				IStatus.class);

		targets = new WritableList(realm, new ArrayList(), IObservable.class);
		targets.addListChangeListener(targetsListener);
		unmodifiableTargets = Observables.unmodifiableObservableList(targets);

		models = Observables.emptyObservableList(realm);
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
			revalidate();
			unmodifiableValidationStatus = Observables
					.unmodifiableObservableValue(validationStatus);
		}
		return unmodifiableValidationStatus;
	}

	private void revalidate() {
		final IObservable[] dependencies = ObservableTracker.runAndMonitor(
				new Runnable() {
					public void run() {
						try {
							IStatus status = validate();
							if (status == null)
								status = ValidationStatus.ok();
							setStatus(status);
						} catch (RuntimeException e) {
							// Usually an NPE as dependencies are
							// init'ed
							setStatus(ValidationStatus.error(e.getMessage(), e));
						}
					}
				}, null, null);
		ObservableTracker.runAndIgnore(new Runnable() {
			public void run() {
				List newTargets = new ArrayList(Arrays.asList(dependencies));
				targets.retainAll(newTargets);
				newTargets.removeAll(targets);

				// Prevent dependency loop
				newTargets.remove(validationStatus);
				newTargets.remove(unmodifiableValidationStatus);
				newTargets.remove(targets);
				newTargets.remove(unmodifiableTargets);
				newTargets.remove(models);

				targets.addAll(newTargets);
			}
		});
	}

	private void setStatus(final IStatus status) {
		ObservableTracker.runAndIgnore(new Runnable() {
			public void run() {
				validationStatus.setValue(status);
			}
		});
	}

	/**
	 * Return the current validation status.
	 * <p>
	 * Note: To ensure that the validation status is kept current, all
	 * dependencies used to calculate status should be accessed through
	 * {@link IObservable} instances. Each dependency observable must be in the
	 * same realm as the MultiValidator.
	 * 
	 * @return the current validation status.
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
	 * <li>While invalid, the wrapper's entries are the target observable's
	 * last valid entries. If the target changes entries, a stale event is fired
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

}
