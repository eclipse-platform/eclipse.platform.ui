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

import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.internal.util.Util;

public final class EnabledSubmission implements Comparable {

    private final static int HASH_FACTOR = 89;

    private final static int HASH_INITIAL = EnabledSubmission.class.getName()
            .hashCode();

    private IPerspectiveDescriptor activePerspectiveDescriptor;

    private IWorkbenchSite activeWorkbenchSite;

    private String contextId;

    private transient int hashCode;

    private transient boolean hashCodeComputed;

    private transient String string;

    public EnabledSubmission(
            IPerspectiveDescriptor activePerspectiveDescriptor,
            IWorkbenchSite activeWorkbenchSite, String contextId) {
        if (contextId == null) throw new NullPointerException();
        this.activePerspectiveDescriptor = activePerspectiveDescriptor;
        this.activeWorkbenchSite = activeWorkbenchSite;
        this.contextId = contextId;
    }

    public int compareTo(Object object) {
        EnabledSubmission castedObject = (EnabledSubmission) object;
        int compareTo = Util.compare(activePerspectiveDescriptor,
                castedObject.activePerspectiveDescriptor);

        if (compareTo == 0) {
            compareTo = Util.compare(activeWorkbenchSite,
                    castedObject.activeWorkbenchSite);

            if (compareTo == 0)
                    compareTo = Util.compare(contextId, castedObject.contextId);
        }

        return compareTo;
    }

    //    public boolean equals(Object object) {
    //        if (!(object instanceof EnabledSubmission)) return false;
    //
    //        EnabledSubmission castedObject = (EnabledSubmission) object;
    //        boolean equals = true;
    //        equals &= Util.equals(activePerspectiveDescriptor,
    //                castedObject.activePerspectiveDescriptor);
    //        equals &= Util.equals(activeWorkbenchSite,
    //                castedObject.activeWorkbenchSite);
    //        equals &= Util.equals(contextId, castedObject.contextId);
    //        return equals;
    //    }

    public IPerspectiveDescriptor getActivePerspectiveDescriptor() {
        return activePerspectiveDescriptor;
    }

    public IWorkbenchSite getActiveWorkbenchSite() {
        return activeWorkbenchSite;
    }

    public String getContextId() {
        return contextId;
    }

    public int hashCode() {
        if (!hashCodeComputed) {
            hashCode = HASH_INITIAL;
            hashCode = hashCode * HASH_FACTOR
                    + Util.hashCode(activePerspectiveDescriptor);
            hashCode = hashCode * HASH_FACTOR
                    + Util.hashCode(activeWorkbenchSite);
            hashCode = hashCode * HASH_FACTOR + Util.hashCode(contextId);
            hashCodeComputed = true;
        }

        return hashCode;
    }

    public String toString() {
        if (string == null) {
            final StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("[activePerspectiveDescriptor="); //$NON-NLS-1$
            stringBuffer.append(activePerspectiveDescriptor);
            stringBuffer.append(",activeWorkbenchSite="); //$NON-NLS-1$
            stringBuffer.append(activeWorkbenchSite);
            stringBuffer.append(",contextId="); //$NON-NLS-1$
            stringBuffer.append(contextId);
            stringBuffer.append(']');
            string = stringBuffer.toString();
        }

        return string;
    }
}
