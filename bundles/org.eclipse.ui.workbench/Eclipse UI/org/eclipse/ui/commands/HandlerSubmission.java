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
package org.eclipse.ui.commands;

import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.internal.util.Util;

public final class HandlerSubmission implements Comparable {

    private final static int HASH_FACTOR = 89;

    private final static int HASH_INITIAL = HandlerSubmission.class.getName()
            .hashCode();

    private IPerspectiveDescriptor activePerspectiveDescriptor;

    private IWorkbenchSite activeWorkbenchSite;

    private String commandId;

    private IHandler handler;

    private transient int hashCode;

    private transient boolean hashCodeComputed;

    private int priority;

    private transient String string;

    public HandlerSubmission(
            IPerspectiveDescriptor activePerspectiveDescriptor,
            IWorkbenchSite activeWorkbenchSite, String commandId,
            IHandler handler, int priority) {
        if (commandId == null || handler == null)
                throw new NullPointerException();
        this.activePerspectiveDescriptor = activePerspectiveDescriptor;
        this.activeWorkbenchSite = activeWorkbenchSite;
        this.commandId = commandId;
        this.handler = handler;
        this.priority = priority;
    }

    public int compareTo(Object object) {
        HandlerSubmission castedObject = (HandlerSubmission) object;
        int compareTo = Util.compare(activePerspectiveDescriptor,
                castedObject.activePerspectiveDescriptor);

        if (compareTo == 0) {
            compareTo = Util.compare(activeWorkbenchSite,
                    castedObject.activeWorkbenchSite);

            if (compareTo == 0) {
                compareTo = Util.compare(-priority, -castedObject.priority);

                if (compareTo == 0) {
                    compareTo = Util.compare(commandId, castedObject.commandId);

                    if (compareTo == 0)
                            compareTo = Util.compare(handler,
                                    castedObject.handler);
                }
            }
        }

        return compareTo;
    }

    //    public boolean equals(Object object) {
    //        if (!(object instanceof HandlerSubmission)) return false;
    //
    //        HandlerSubmission castedObject = (HandlerSubmission) object;
    //        boolean equals = true;
    //        equals &= Util.equals(activePerspectiveDescriptor,
    //                castedObject.activePerspectiveDescriptor);
    //        equals &= Util.equals(activeWorkbenchSite,
    //                castedObject.activeWorkbenchSite);
    //        equals &= Util.equals(commandId, castedObject.commandId);
    //        equals &= Util.equals(handler, castedObject.handler);
    //        equals &= Util.equals(priority, castedObject.priority);
    //        return equals;
    //    }

    public IPerspectiveDescriptor getActivePerspectiveDescriptor() {
        return activePerspectiveDescriptor;
    }

    public IWorkbenchSite getActiveWorkbenchSite() {
        return activeWorkbenchSite;
    }

    public String getCommandId() {
        return commandId;
    }

    public IHandler getHandler() {
        return handler;
    }

    public int getPriority() {
        return priority;
    }

    public int hashCode() {
        if (!hashCodeComputed) {
            hashCode = HASH_INITIAL;
            hashCode = hashCode * HASH_FACTOR
                    + Util.hashCode(activePerspectiveDescriptor);
            hashCode = hashCode * HASH_FACTOR
                    + Util.hashCode(activeWorkbenchSite);
            hashCode = hashCode * HASH_FACTOR + Util.hashCode(commandId);
            hashCode = hashCode * HASH_FACTOR + Util.hashCode(handler);
            hashCode = hashCode * HASH_FACTOR + Util.hashCode(priority);
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
            stringBuffer.append(",commandId="); //$NON-NLS-1$
            stringBuffer.append(commandId);
            stringBuffer.append(",handler="); //$NON-NLS-1$
            stringBuffer.append(handler);
            stringBuffer.append(",priority="); //$NON-NLS-1$
            stringBuffer.append(priority);
            stringBuffer.append(']');
            string = stringBuffer.toString();
        }

        return string;
    }
}
