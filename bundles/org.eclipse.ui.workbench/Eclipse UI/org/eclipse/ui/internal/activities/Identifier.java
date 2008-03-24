/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.activities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.ui.activities.IIdentifier;
import org.eclipse.ui.activities.IIdentifierListener;
import org.eclipse.ui.activities.IdentifierEvent;
import org.eclipse.ui.internal.util.Util;

final class Identifier implements IIdentifier {
    private final static int HASH_FACTOR = 89;

    private final static int HASH_INITIAL = Identifier.class.getName()
            .hashCode();

    private final static Set strongReferences = new HashSet();

    private Set activityIds = Collections.EMPTY_SET;

    private transient String[] activityIdsAsArray = {};

    private boolean enabled;

    private transient int hashCode = HASH_INITIAL;

    private String id;

    private List identifierListeners;

    private transient String string;

    Identifier(String id) {
        if (id == null) {
			throw new NullPointerException();
		}

        this.id = id;
    }

    public void addIdentifierListener(IIdentifierListener identifierListener) {
        if (identifierListener == null) {
			throw new NullPointerException();
		}

        if (identifierListeners == null) {
			identifierListeners = new ArrayList();
		}

        if (!identifierListeners.contains(identifierListener)) {
			identifierListeners.add(identifierListener);
		}

        strongReferences.add(this);
    }

    public int compareTo(Object object) {
        Identifier castedObject = (Identifier) object;
        int compareTo = Util.compare(activityIdsAsArray,
                castedObject.activityIdsAsArray);

        if (compareTo == 0) {
            compareTo = Util.compare(enabled, castedObject.enabled);

            if (compareTo == 0) {
				compareTo = Util.compare(id, castedObject.id);
			}
        }

        return compareTo;
    }

    public boolean equals(Object object) {
        if (!(object instanceof Identifier)) {
			return false;
		}

        final Identifier castedObject = (Identifier) object;
        if (!Util.equals(activityIds, castedObject.activityIds)) {
            return false;
        }
        
        if (!Util.equals(enabled, castedObject.enabled)) {
            return false;
        }
        
        return Util.equals(id, castedObject.id);
    }

    void fireIdentifierChanged(IdentifierEvent identifierEvent) {
        if (identifierEvent == null) {
			throw new NullPointerException();
		}

        if (identifierListeners != null) {
			for (int i = 0; i < identifierListeners.size(); i++) {
				((IIdentifierListener) identifierListeners.get(i))
                        .identifierChanged(identifierEvent);
			}
		}
    }

    public Set getActivityIds() {
        return activityIds;
    }

    public String getId() {
        return id;
    }

    public int hashCode() {
        if (hashCode == HASH_INITIAL) {
            hashCode = hashCode * HASH_FACTOR + Util.hashCode(activityIds);
            hashCode = hashCode * HASH_FACTOR + Util.hashCode(enabled);
            hashCode = hashCode * HASH_FACTOR + Util.hashCode(id);
            if (hashCode == HASH_INITIAL) {
				hashCode++;
			}
        }

        return hashCode;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void removeIdentifierListener(IIdentifierListener identifierListener) {
        if (identifierListener == null) {
			throw new NullPointerException();
		}

        if (identifierListeners != null) {
			identifierListeners.remove(identifierListener);
		}

        if (identifierListeners.isEmpty()) {
			strongReferences.remove(this);
		}
    }

    boolean setActivityIds(Set activityIds) {
        activityIds = Util.safeCopy(activityIds, String.class);

        if (!Util.equals(activityIds, this.activityIds)) {
            this.activityIds = activityIds;
            this.activityIdsAsArray = (String[]) this.activityIds
                    .toArray(new String[this.activityIds.size()]);
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

    public String toString() {
        if (string == null) {
            final StringBuffer stringBuffer = new StringBuffer();
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
