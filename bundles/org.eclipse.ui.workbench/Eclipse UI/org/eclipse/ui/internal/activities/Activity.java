/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.activities;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ui.activities.ActivityEvent;
import org.eclipse.ui.activities.IActivity;
import org.eclipse.ui.activities.IActivityListener;
import org.eclipse.ui.activities.IPatternBinding;
import org.eclipse.ui.activities.NotDefinedException;
import org.eclipse.ui.internal.util.Util;

final class Activity implements IActivity {

	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL = Activity.class.getName().hashCode();

	private List activityListeners;
	private MutableActivityManager activityManager;
	private boolean defined;
	private String description;
	private boolean enabled;

	private transient int hashCode;
	private transient boolean hashCodeComputed;
	private String id;
	private String name;
	private String parentId;
	private List patternBindings;
	private transient IPatternBinding[] patternBindingsAsArray;
	private transient String string;

	Activity(MutableActivityManager activityManager, String id) {
		if (activityManager == null || id == null)
			throw new NullPointerException();

		this.activityManager = activityManager;
		this.id = id;
	}

	public void addActivityListener(IActivityListener activityListener) {
		if (activityListener == null)
			throw new NullPointerException();

		if (activityListeners == null)
			activityListeners = new ArrayList();

		if (!activityListeners.contains(activityListener))
			activityListeners.add(activityListener);

		activityManager.getActivitiesWithListeners().add(this);
	}

	public int compareTo(Object object) {
		Activity castedObject = (Activity) object;
		int compareTo = Util.compare(defined, castedObject.defined);

		if (compareTo == 0) {
			compareTo = Util.compare(description, castedObject.description);

			if (compareTo == 0) {
				compareTo = Util.compare(enabled, castedObject.enabled);

				if (compareTo == 0) {
					compareTo = Util.compare(id, castedObject.id);

					if (compareTo == 0) {
						compareTo = Util.compare(name, castedObject.name);

						if (compareTo == 0) {
							compareTo =
								Util.compare(parentId, castedObject.parentId);

							if (compareTo == 0)
								compareTo =
									Util.compare(
										(Comparable[]) patternBindingsAsArray,
										(
											Comparable[]) castedObject
												.patternBindingsAsArray);
						}
					}
				}
			}
		}

		return compareTo;
	}

	public boolean equals(Object object) {
		if (!(object instanceof Activity))
			return false;

		Activity castedObject = (Activity) object;
		boolean equals = true;
		equals &= Util.equals(defined, castedObject.defined);
		equals &= Util.equals(description, castedObject.description);
		equals &= Util.equals(enabled, castedObject.enabled);
		equals &= Util.equals(id, castedObject.id);
		equals &= Util.equals(name, castedObject.name);
		equals &= Util.equals(parentId, castedObject.parentId);
		equals &= Util.equals(patternBindings, castedObject.patternBindings);
		return equals;
	}

	void fireActivityChanged(ActivityEvent activityEvent) {
		if (activityEvent == null)
			throw new NullPointerException();

		if (activityListeners != null)
			for (int i = 0; i < activityListeners.size(); i++)
				((IActivityListener) activityListeners.get(i)).activityChanged(
					activityEvent);
	}

	public String getDescription() throws NotDefinedException {
		if (!defined)
			throw new NotDefinedException();

		return description;
	}

	public String getId() {
		return id;
	}

	public String getName() throws NotDefinedException {
		if (!defined)
			throw new NotDefinedException();

		return name;
	}

	public String getParentId() throws NotDefinedException {
		if (!defined)
			throw new NotDefinedException();

		return parentId;
	}

	public List getPatternBindings() {
		return patternBindings;
	}

	public int hashCode() {
		if (!hashCodeComputed) {
			hashCode = HASH_INITIAL;
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(defined);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(description);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(enabled);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(id);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(name);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(parentId);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(patternBindings);
			hashCodeComputed = true;
		}

		return hashCode;
	}

	public boolean isDefined() {
		return defined;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public boolean match(String string) {
		boolean match = false;

		if (isDefined())
			for (Iterator iterator = patternBindings.iterator();
				iterator.hasNext();
				) {
				IPatternBinding patternBinding =
					(IPatternBinding) iterator.next();

				if (patternBinding.isInclusive() && !match)
					match =
						patternBinding.getPattern().matcher(string).matches();
				else if (!patternBinding.isInclusive() && match)
					match =
						!patternBinding.getPattern().matcher(string).matches();
			}

		return match;
	}

	public void removeActivityListener(IActivityListener activityListener) {
		if (activityListener == null)
			throw new NullPointerException();

		if (activityListeners != null)
			activityListeners.remove(activityListener);

		if (activityListeners.isEmpty())
			activityManager.getActivitiesWithListeners().remove(this);
	}

	boolean setDefined(boolean defined) {
		if (defined != this.defined) {
			this.defined = defined;
			hashCodeComputed = false;
			hashCode = 0;
			string = null;
			return true;
		}

		return false;
	}

	boolean setDescription(String description) {
		if (!Util.equals(description, this.description)) {
			this.description = description;
			hashCodeComputed = false;
			hashCode = 0;
			string = null;
			return true;
		}

		return false;
	}

	boolean setEnabled(boolean enabled) {
		if (enabled != this.enabled) {
			this.enabled = enabled;
			hashCodeComputed = false;
			hashCode = 0;
			string = null;
			return true;
		}

		return false;
	}

	boolean setName(String name) {
		if (!Util.equals(name, this.name)) {
			this.name = name;
			hashCodeComputed = false;
			hashCode = 0;
			string = null;
			return true;
		}

		return false;
	}

	boolean setParentId(String parentId) {
		if (!Util.equals(parentId, this.parentId)) {
			this.parentId = parentId;
			hashCodeComputed = false;
			hashCode = 0;
			string = null;
			return true;
		}

		return false;
	}

	boolean setPatternBindings(List patternBindings) {
		patternBindings = Util.safeCopy(patternBindings, IPatternBinding.class);

		if (!Util.equals(patternBindings, this.patternBindings)) {
			this.patternBindings = patternBindings;
			this.patternBindingsAsArray =
				(IPatternBinding[]) this.patternBindings.toArray(
					new IPatternBinding[this.patternBindings.size()]);
			hashCodeComputed = false;
			hashCode = 0;
			string = null;
			return true;
		}

		return false;
	}

	public String toString() {
		if (string == null) {
			final StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append('[');
			stringBuffer.append(defined);
			stringBuffer.append(',');
			stringBuffer.append(description);
			stringBuffer.append(',');
			stringBuffer.append(enabled);
			stringBuffer.append(',');
			stringBuffer.append(id);
			stringBuffer.append(',');
			stringBuffer.append(name);
			stringBuffer.append(',');
			stringBuffer.append(parentId);
			stringBuffer.append(',');
			stringBuffer.append(patternBindings);
			stringBuffer.append(']');
			string = stringBuffer.toString();
		}

		return string;
	}
}
