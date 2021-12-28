/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
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
 *     Bob Smith - bug 198880, 249526
 *     Matthew Hall - bug 249526, 261513
 *******************************************************************************/

package org.eclipse.core.databinding;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.internal.databinding.IdentitySet;

/**
 * An observables manager can be used for lifecycle management of
 * {@link IObservable} objects.
 *
 * @noextend This class is not intended to be subclassed by clients.
 *
 * @since 1.0
 *
 */
public class ObservablesManager {

	private Set<IObservable> managedObservables = new IdentitySet<>();
	private Set<IObservable> excludedObservables = new IdentitySet<>();
	private Map<DataBindingContext, ManagerEntry> contexts = new HashMap<>();

	/**
	 * Create a new observables manager.
	 */
	public ObservablesManager() {
	}

	/**
	 * Adds the given observable to this manager.
	 *
	 * @param observable the observable
	 */
	public void addObservable(IObservable observable) {
		managedObservables.add(observable);
	}

	/**
	 * Adds the given observable to this manager's exclusion list. The given
	 * observable will not be disposed of by this manager.
	 *
	 * @param observable the observable
	 */
	public void excludeObservable(IObservable observable) {
		excludedObservables.add(observable);
	}

	/**
	 * Adds the given data binding context's target and/or model observables to be
	 * disposed by this manager.
	 * <p>
	 * Prefer the {@link #addContext} method if the {@code context} argument should
	 * also be disposed.
	 * </p>
	 * <p>
	 * Note: The {@code context} argument must NOT be disposed before this manager
	 * itself is disposed. If it is then its contents will not be disposed by this
	 * manager.
	 * </p>
	 *
	 * @param context      the data binding context
	 * @param trackTargets <code>true</code> if the target observables of the
	 *                     context should be managed
	 * @param trackModels  <code>true</code> if the model observables of the context
	 *                     should be managed
	 */
	public void addObservablesFromContext(DataBindingContext context, boolean trackTargets, boolean trackModels) {
		if (trackTargets || trackModels) {
			contexts.put(context, new ManagerEntry(false, trackTargets, trackModels));
		}
	}

	/**
	 * Adds the databinding context to be disposed by this manager. Identical to
	 * {@link #addObservablesFromContext} except that the binding context itself is
	 * also disposed.
	 * <p>
	 * Note: The {@code context} argument must NOT be disposed before this manager
	 * itself is disposed. (There is no reason to dispose {@code context} manually
	 * when using this method.)
	 * </p>
	 *
	 * @param context      the data binding context to add
	 * @param trackTargets <code>true</code> if the target observables of the
	 *                     context should be managed
	 * @param trackModels  <code>true</code> if the model observables of the context
	 *                     should be managed
	 * @since 1.10
	 */
	public void addContext(DataBindingContext context, boolean trackTargets, boolean trackModels) {
		contexts.put(context, new ManagerEntry(true, trackTargets, trackModels));
	}

	/**
	 * Executes the specified runnable and adds to this manager all observables
	 * created while executing the runnable.
	 * <p>
	 *
	 * @deprecated This method is very error prone. If any class creates internal
	 *             observables without hiding them with
	 *             {@link ObservableTracker#setIgnore} the result can be that
	 *             objects are disposed involuntary or in the wrong order. Prefer
	 *             instead to use {@link #addContext}.
	 *
	 * @param runnable the runnable to execute
	 * @since 1.2
	 */
	@Deprecated(since = "1.11.0")
	public void runAndCollect(Runnable runnable) {
		IObservable[] collected = ObservableTracker.runAndCollect(runnable);
		for (IObservable observable : collected) {
			addObservable(observable);
		}
	}

	/**
	 * Disposes of this manager and all observables that it manages.
	 * <p>
	 * Note: If {@link #addObservablesFromContext} is used then its {@code context}
	 * argument must NOT be disposed before this object itself is disposed. If it is
	 * then its contents will not be disposed by this object.
	 * </p>
	 */
	public void dispose() {
		Set<IObservable> observables = new IdentitySet<>();
		observables.addAll(managedObservables);

		for (Entry<DataBindingContext, ManagerEntry> e : contexts.entrySet()) {
			DataBindingContext context = e.getKey();
			ManagerEntry entry = e.getValue();

			for (Binding binding : context.getBindings()) {
				if (entry.disposeTargets) {
					observables.addAll(binding.getTargets());
				}
				if (entry.disposeModels) {
					observables.addAll(binding.getModels());
				}
			}

			if (entry.disposeContext) {
				context.dispose();
			}
		}
		observables.removeAll(excludedObservables);
		for (IObservable observable : observables) {
			observable.dispose();
		}
	}

	private static class ManagerEntry {
		public final boolean disposeContext;
		public final boolean disposeTargets;
		public final boolean disposeModels;

		public ManagerEntry(boolean disposeContext, boolean disposeTargets, boolean disposeModels) {
			this.disposeContext = disposeContext;
			this.disposeTargets = disposeTargets;
			this.disposeModels = disposeModels;
		}
	}
}
