/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
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
 *******************************************************************************/

package org.eclipse.ui.internal.activities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.eclipse.core.expressions.Expression;
import org.eclipse.ui.activities.ActivityEvent;
import org.eclipse.ui.activities.IActivity;
import org.eclipse.ui.activities.IActivityListener;
import org.eclipse.ui.activities.IActivityPatternBinding;
import org.eclipse.ui.activities.IActivityRequirementBinding;
import org.eclipse.ui.activities.NotDefinedException;
import org.eclipse.ui.internal.util.Util;

final class Activity implements IActivity {
	private static final int HASH_FACTOR = 89;

	private static final int HASH_INITIAL = Activity.class.getName().hashCode();

	private static final Set<Activity> strongReferences = new HashSet<>();

	private Set<IActivityRequirementBinding> activityRequirementBindings;

	private transient IActivityRequirementBinding[] activityRequirementBindingsAsArray;

	private List<IActivityListener> activityListeners;

	private Set<IActivityPatternBinding> activityPatternBindings;

	private transient IActivityPatternBinding[] activityPatternBindingsAsArray;

	private boolean defined;

	private boolean enabled;

	private transient int hashCode = HASH_INITIAL;

	private String id;

	private String name;

	private transient String string;

	private String description;

	private boolean defaultEnabled;

	private Expression expression;

	Activity(String id) {
		if (id == null) {
			throw new NullPointerException();
		}

		this.id = id;
	}

	@Override
	public void addActivityListener(IActivityListener activityListener) {
		if (activityListener == null) {
			throw new NullPointerException();
		}

		if (activityListeners == null) {
			activityListeners = new ArrayList<>();
		}

		if (!activityListeners.contains(activityListener)) {
			activityListeners.add(activityListener);
		}

		strongReferences.add(this);
	}

	@Override
	public int compareTo(IActivity object) {
		Activity castedObject = (Activity) object;

		int compareTo = Util.compare(activityRequirementBindingsAsArray,
				castedObject.activityRequirementBindingsAsArray);

		if (compareTo == 0) {
			compareTo = Util.compare(activityPatternBindingsAsArray, castedObject.activityPatternBindingsAsArray);

			if (compareTo == 0) {
				compareTo = Util.compare(defined, castedObject.defined);

				if (compareTo == 0) {
					compareTo = Util.compare(enabled, castedObject.enabled);

					if (compareTo == 0) {
						compareTo = Util.compare(id, castedObject.id);

						if (compareTo == 0) {
							compareTo = Util.compare(name, castedObject.name);
						}
					}
				}
			}
		}

		return compareTo;
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof Activity)) {
			return false;
		}

		final Activity castedObject = (Activity) object;
		return Objects.equals(activityRequirementBindings, castedObject.activityRequirementBindings)
				&& Objects.equals(activityPatternBindings, castedObject.activityPatternBindings)
				&& defined == castedObject.defined && enabled == castedObject.enabled
				&& Objects.equals(id, castedObject.id) && Objects.equals(name, castedObject.name);
	}

	void fireActivityChanged(ActivityEvent activityEvent) {
		if (activityEvent == null) {
			throw new NullPointerException();
		}

		if (activityListeners != null) {
			for (int i = 0; i < activityListeners.size(); i++) {
				activityListeners.get(i).activityChanged(activityEvent);
			}
		}
	}

	@Override
	public Set<IActivityRequirementBinding> getActivityRequirementBindings() {
		return activityRequirementBindings;
	}

	@Override
	public Set<IActivityPatternBinding> getActivityPatternBindings() {
		return activityPatternBindings;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getName() throws NotDefinedException {
		if (!defined) {
			throw new NotDefinedException();
		}

		return name;
	}

	@Override
	public Expression getExpression() {
		return expression;
	}

	@Override
	public int hashCode() {
		if (hashCode == HASH_INITIAL) {
			hashCode = hashCode * HASH_FACTOR + Objects.hashCode(activityRequirementBindings);
			hashCode = hashCode * HASH_FACTOR + Objects.hashCode(activityPatternBindings);
			hashCode = hashCode * HASH_FACTOR + Boolean.hashCode(defined);
			hashCode = hashCode * HASH_FACTOR + Boolean.hashCode(enabled);
			hashCode = hashCode * HASH_FACTOR + Objects.hashCode(id);
			hashCode = hashCode * HASH_FACTOR + Objects.hashCode(name);
			if (hashCode == HASH_INITIAL) {
				hashCode++;
			}
		}

		return hashCode;
	}

	@Override
	public boolean isDefined() {
		return defined;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	public boolean isMatch(String string) {
		if (isDefined()) {
			for (Iterator<IActivityPatternBinding> iterator = activityPatternBindings.iterator(); iterator.hasNext();) {
				ActivityPatternBinding activityPatternBinding = (ActivityPatternBinding) iterator.next();

				if (activityPatternBinding.isMatch(string)) {
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public void removeActivityListener(IActivityListener activityListener) {
		if (activityListener == null) {
			throw new NullPointerException();
		}

		if (activityListeners != null) {
			activityListeners.remove(activityListener);
		}

		if (activityListeners.isEmpty()) {
			strongReferences.remove(this);
		}
	}

	boolean setActivityRequirementBindings(Set<IActivityRequirementBinding> activityRequirementBindings) {
		activityRequirementBindings = Util.safeCopy(activityRequirementBindings, IActivityRequirementBinding.class);

		if (!Objects.equals(activityRequirementBindings, this.activityRequirementBindings)) {
			this.activityRequirementBindings = activityRequirementBindings;
			this.activityRequirementBindingsAsArray = this.activityRequirementBindings
					.toArray(new IActivityRequirementBinding[this.activityRequirementBindings.size()]);
			hashCode = HASH_INITIAL;
			string = null;
			return true;
		}

		return false;
	}

	boolean setActivityPatternBindings(Set<IActivityPatternBinding> activityPatternBindings) {
		activityPatternBindings = Util.safeCopy(activityPatternBindings, IActivityPatternBinding.class);

		if (!Objects.equals(activityPatternBindings, this.activityPatternBindings)) {
			this.activityPatternBindings = activityPatternBindings;
			this.activityPatternBindingsAsArray = this.activityPatternBindings
					.toArray(new IActivityPatternBinding[this.activityPatternBindings.size()]);
			hashCode = HASH_INITIAL;
			string = null;
			return true;
		}

		return false;
	}

	boolean setDefined(boolean defined) {
		if (defined != this.defined) {
			this.defined = defined;
			hashCode = HASH_INITIAL;
			string = null;
			return true;
		}

		return false;
	}

	boolean setEnabled(boolean enabled) {
		if (enabled != this.enabled) {
			this.enabled = enabled;
			hashCode = HASH_INITIAL;
			string = null;
			return true;
		}

		return false;
	}

	boolean setName(String name) {
		if (!Objects.equals(name, this.name)) {
			this.name = name;
			hashCode = HASH_INITIAL;
			string = null;
			return true;
		}

		return false;
	}

	void setExpression(Expression exp) {
		expression = exp;
	}

	boolean setDescription(String description) {
		if (!Objects.equals(description, this.description)) {
			this.description = description;
			hashCode = HASH_INITIAL;
			string = null;
			return true;
		}

		return false;
	}

	@Override
	public String toString() {
		if (string == null) {
			final StringBuilder stringBuffer = new StringBuilder();
			stringBuffer.append('[');
			stringBuffer.append(activityRequirementBindings);
			stringBuffer.append(',');
			stringBuffer.append(activityPatternBindings);
			stringBuffer.append(',');
			stringBuffer.append(defined);
			stringBuffer.append(',');
			stringBuffer.append(enabled);
			stringBuffer.append(',');
			stringBuffer.append(id);
			stringBuffer.append(',');
			stringBuffer.append(name);
			stringBuffer.append(']');
			string = stringBuffer.toString();
		}

		return string;
	}

	@Override
	public String getDescription() throws NotDefinedException {
		if (!defined) {
			throw new NotDefinedException();
		}

		return description;
	}

	@Override
	public boolean isDefaultEnabled() {
		return defaultEnabled;
	}

	boolean setDefaultEnabled(boolean defaultEnabled) {
		if (defaultEnabled != this.defaultEnabled) {
			this.defaultEnabled = defaultEnabled;
			hashCode = HASH_INITIAL;
			string = null;
			return true;
		}

		return false;
	}
}
