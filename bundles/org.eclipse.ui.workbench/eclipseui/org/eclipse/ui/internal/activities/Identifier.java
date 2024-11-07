/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
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
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 472654
 *******************************************************************************/

package org.eclipse.ui.internal.activities;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.ui.activities.IIdentifier;
import org.eclipse.ui.activities.IIdentifierListener;
import org.eclipse.ui.activities.IdentifierEvent;
import org.eclipse.ui.internal.util.Util;

final class Identifier implements IIdentifier {
	private static final int HASH_FACTOR = 89;

	private static final int HASH_INITIAL = Identifier.class.getName().hashCode();

	private static final Set<Identifier> strongReferences = new HashSet<>();

	private Set<String> activityIds = Collections.emptySet();

	private transient String[] activityIdsAsArray = {};

	private boolean enabled;

	private transient int hashCode = HASH_INITIAL;

	private String id;

	private ListenerList<IIdentifierListener> identifierListeners;

	private transient String string;

	Identifier(String id) {
		if (id == null) {
			throw new NullPointerException();
		}

		this.id = id;
	}

	@Override
	public void addIdentifierListener(IIdentifierListener identifierListener) {
		if (identifierListener == null) {
			throw new NullPointerException();
		}

		if (identifierListeners == null) {
			identifierListeners = new ListenerList<>(ListenerList.IDENTITY);
		}

		identifierListeners.add(identifierListener);
		strongReferences.add(this);
	}

	@Override
	public int compareTo(IIdentifier object) {
		Identifier castedObject = (Identifier) object;
		int compareTo = Util.compare(activityIdsAsArray, castedObject.activityIdsAsArray);

		if (compareTo == 0) {
			compareTo = Util.compare(enabled, castedObject.enabled);

			if (compareTo == 0) {
				compareTo = Util.compare(id, castedObject.id);
			}
		}

		return compareTo;
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof Identifier)) {
			return false;
		}

		final Identifier castedObject = (Identifier) object;
		return Objects.equals(activityIds, castedObject.activityIds) && enabled == castedObject.enabled
				&& Objects.equals(id, castedObject.id);
	}

	void fireIdentifierChanged(IdentifierEvent identifierEvent) {
		if (identifierEvent == null) {
			throw new NullPointerException();
		}

		if (identifierListeners != null) {
			for (IIdentifierListener listener : identifierListeners) {
				listener.identifierChanged(identifierEvent);
			}
		}
	}

	@Override
	public Set<String> getActivityIds() {
		return activityIds;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public int hashCode() {
		if (hashCode == HASH_INITIAL) {
			hashCode = hashCode * HASH_FACTOR + Objects.hashCode(activityIds);
			hashCode = hashCode * HASH_FACTOR + Boolean.hashCode(enabled);
			hashCode = hashCode * HASH_FACTOR + Objects.hashCode(id);
			if (hashCode == HASH_INITIAL) {
				hashCode++;
			}
		}
		return hashCode;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void removeIdentifierListener(IIdentifierListener identifierListener) {
		if (identifierListener == null) {
			throw new NullPointerException();
		}

		if (identifierListeners != null) {
			identifierListeners.remove(identifierListener);
			if (identifierListeners.isEmpty()) {
				strongReferences.remove(this);
			}
		}
	}

	boolean setActivityIds(Set<String> activityIds) {
		activityIds = Util.safeCopy(activityIds, String.class);

		if (!Objects.equals(activityIds, this.activityIds)) {
			this.activityIds = activityIds;
			this.activityIdsAsArray = this.activityIds.toArray(new String[this.activityIds.size()]);
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

	@Override
	public String toString() {
		if (string == null) {
			final StringBuilder stringBuffer = new StringBuilder();
			stringBuffer.append('[');
			stringBuffer.append(activityIds);
			stringBuffer.append(',');
			stringBuffer.append(enabled);
			stringBuffer.append(',');
			stringBuffer.append(id);
			stringBuffer.append(']');
			string = stringBuffer.toString();
		}

		return string;
	}
}
