/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.contexts;

import org.eclipse.ui.internal.util.Util;

public final class EnabledSubmission implements Comparable {

    private final static int HASH_FACTOR = 89;

    private final static int HASH_INITIAL = EnabledSubmission.class.getName()
            .hashCode();

    private String activePartId;

    private String activePerspectiveId;

    private String contextId;

    private transient int hashCode;

    private transient boolean hashCodeComputed;

    private transient String string;

    public EnabledSubmission(String activePartId, String activePerspectiveId,
            String contextId) {
        if (contextId == null) throw new NullPointerException();
        this.activePartId = activePartId;
        this.activePerspectiveId = activePerspectiveId;
        this.contextId = contextId;
    }

    public int compareTo(Object object) {
        EnabledSubmission castedObject = (EnabledSubmission) object;
        int compareTo = Util.compare(activePartId, castedObject.activePartId);

        if (compareTo == 0) {
            compareTo = Util.compare(activePerspectiveId,
                    castedObject.activePerspectiveId);

            if (compareTo == 0)
                    compareTo = Util.compare(contextId, castedObject.contextId);
        }

        return compareTo;
    }

    public boolean equals(Object object) {
        if (!(object instanceof EnabledSubmission)) return false;

        EnabledSubmission castedObject = (EnabledSubmission) object;
        boolean equals = true;
        equals &= Util.equals(activePartId, castedObject.activePartId);
        equals &= Util.equals(activePerspectiveId,
                castedObject.activePerspectiveId);
        equals &= Util.equals(contextId, castedObject.contextId);
        return equals;
    }

    public String getActivePartId() {
        return activePartId;
    }

    public String getActivePerspectiveId() {
        return activePerspectiveId;
    }

    public String getContextId() {
        return contextId;
    }

    public int hashCode() {
        if (!hashCodeComputed) {
            hashCode = HASH_INITIAL;
            hashCode = hashCode * HASH_FACTOR + Util.hashCode(activePartId);
            hashCode = hashCode * HASH_FACTOR
                    + Util.hashCode(activePerspectiveId);
            hashCode = hashCode * HASH_FACTOR + Util.hashCode(contextId);
            hashCodeComputed = true;
        }

        return hashCode;
    }

    public String toString() {
        if (string == null) {
            final StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("[activePartId="); //$NON-NLS-1$
            stringBuffer.append(activePartId);
            stringBuffer.append(",activePerspectiveId="); //$NON-NLS-1$
            stringBuffer.append(activePerspectiveId);
            stringBuffer.append(",contextId="); //$NON-NLS-1$
            stringBuffer.append(contextId);
            stringBuffer.append(']');
            string = stringBuffer.toString();
        }

        return string;
    }
}
