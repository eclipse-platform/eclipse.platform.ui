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

import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.util.Util;

public final class EnabledSubmission implements Comparable {

    private final static int HASH_FACTOR = 89;

    private final static int HASH_INITIAL = EnabledSubmission.class.getName()
            .hashCode();

    private IWorkbenchSite activeWorkbenchSite;

    private IWorkbenchWindow activeWorkbenchWindow;

    private String contextId;

    private transient int hashCode;

    private transient boolean hashCodeComputed;

    private transient String string;

    public EnabledSubmission(IWorkbenchSite activeWorkbenchSite,
            IWorkbenchWindow activeWorkbenchWindow, String contextId) {
        if (contextId == null) throw new NullPointerException();
        this.activeWorkbenchSite = activeWorkbenchSite;
        this.activeWorkbenchWindow = activeWorkbenchWindow;
        this.contextId = contextId;
    }

    public int compareTo(Object object) {
        EnabledSubmission castedObject = (EnabledSubmission) object;
        int compareTo = Util.compare(activeWorkbenchSite,
                castedObject.activeWorkbenchSite);

        if (compareTo == 0) {
            compareTo = Util.compare(activeWorkbenchWindow,
                    castedObject.activeWorkbenchWindow);

            if (compareTo == 0)
                    compareTo = Util.compare(contextId, castedObject.contextId);
        }

        return compareTo;
    }

    public IWorkbenchSite getActiveWorkbenchSite() {
        return activeWorkbenchSite;
    }

    public IWorkbenchWindow getActiveWorkbenchWindow() {
        return activeWorkbenchWindow;
    }

    public String getContextId() {
        return contextId;
    }

    public int hashCode() {
        if (!hashCodeComputed) {
            hashCode = HASH_INITIAL;
            hashCode = hashCode * HASH_FACTOR
                    + Util.hashCode(activeWorkbenchSite);
            hashCode = hashCode * HASH_FACTOR
                    + Util.hashCode(activeWorkbenchWindow);
            hashCode = hashCode * HASH_FACTOR + Util.hashCode(contextId);
            hashCodeComputed = true;
        }

        return hashCode;
    }

    public String toString() {
        if (string == null) {
            final StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("[activeWorkbenchSite="); //$NON-NLS-1$
            stringBuffer.append(activeWorkbenchSite);
            stringBuffer.append(",activeWorkbenchWindow="); //$NON-NLS-1$
            stringBuffer.append(activeWorkbenchWindow);
            stringBuffer.append(",contextId="); //$NON-NLS-1$
            stringBuffer.append(contextId);
            stringBuffer.append(']');
            string = stringBuffer.toString();
        }

        return string;
    }
}
