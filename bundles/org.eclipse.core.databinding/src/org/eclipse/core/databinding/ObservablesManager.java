/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Bob Smith - bug 198880, 249526
 *******************************************************************************/

package org.eclipse.core.databinding;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.internal.databinding.Pair;

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

	private Set managedObservables = new HashSet();
	private Set excludedObservables = new HashSet();
	private Map contexts = new HashMap();

	/**
	 * Create a new observables manager.
	 */
	public ObservablesManager() {
	}

	/**
	 * Adds the given observable to this manager.
	 *
	 * @param observable
	 *            the observable
	 */
	public void addObservable(IObservable observable) {
		managedObservables.add(observable);
	}

	/**
	 * Adds the given observable to this manager's exclusion list. The given
	 * observable will not be disposed of by this manager.
	 *
	 * @param observable
	 *            the observable
	 */
	public void excludeObservable(IObservable observable) {
		excludedObservables.add(observable);
	}

	/**
	 * Adds the given data binding context's target and/or model observables to
	 * this manager.
	 *
	 * @param context
	 *            the data binding context
	 * @param trackTargets
	 *            <code>true</code> if the target observables of the context
	 *            should be managed
	 * @param trackModels
	 *            <code>true</code> if the model observables of the context
	 *            should be managed
	 */
	public void addObservablesFromContext(DataBindingContext context,
			boolean trackTargets, boolean trackModels) {
		if (trackTargets || trackModels) {
			contexts.put(context, new Pair(new Boolean(trackTargets),
					new Boolean(trackModels)));
		}
	}

	/**
	 * Executes the specified runnable and adds to this manager all observables
	 * created while executing the runnable.
	 * 
	 * @param runnable
	 *            the runnable to execute
	 */
	public void runAndCollect(Runnable runnable) {
		IObservable[] collected = ObservableTracker.runAndCollect(runnable);
		for (int i = 0; i < collected.length; i++)
			addObservable(collected[i]);
	}

	/**
	 * Disposes of this manager and all observables that it manages.
	 */
	public void dispose() {
		Set observables = new HashSet();
		observables.addAll(managedObservables);
		for (Iterator it = contexts.keySet().iterator(); it.hasNext();) {
			DataBindingContext context = (DataBindingContext) it.next();
			Pair trackModelsOrTargets = (Pair) contexts.get(context);
			boolean disposeTargets = ((Boolean) trackModelsOrTargets.a)
					.booleanValue();
			boolean disposeModels = ((Boolean) trackModelsOrTargets.b)
					.booleanValue();
			for (Iterator it2 = context.getBindings().iterator(); it2.hasNext();) {
				Binding binding = (Binding) it2.next();
				if (disposeTargets) {
					observables.add(binding.getTarget());
				}
				if (disposeModels) {
					observables.add(binding.getModel());
				}
			}
		}
		observables.removeAll(excludedObservables);
		for (Iterator it = observables.iterator(); it.hasNext();) {
			IObservable observable = (IObservable) it.next();
			observable.dispose();
		}
	}
}
