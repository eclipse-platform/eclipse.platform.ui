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

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.util.Util;

public final class HandlerSubmission implements Comparable {

    private final static int HASH_FACTOR = 89;

    private final static int HASH_INITIAL = HandlerSubmission.class.getName()
            .hashCode();

    private String activePartId;

    private Shell activeShell;

    private IWorkbenchPartSite activeWorkbenchPartSite;

    // TODO remove before 3.0
    private IWorkbenchWindow activeWorkbenchWindow;

    private String commandId;

    private IHandler handler;

    private transient int hashCode;

    private transient boolean hashCodeComputed;

    private Priority priority;

    private transient String string;

    /**
     * @deprecated to be removed for 3.0
     * 
     * @param activeWorkbenchSite
     * @param activeWorkbenchWindow
     * @param commandId
     * @param handler
     * @param priority
     */
    public HandlerSubmission(IWorkbenchSite activeWorkbenchSite,
            IWorkbenchWindow activeWorkbenchWindow, String commandId,
            IHandler handler, int priority) {
        if (commandId == null || handler == null)
                throw new NullPointerException();

        if (activeWorkbenchSite instanceof IWorkbenchPartSite)
                this.activeWorkbenchPartSite = (IWorkbenchPartSite) activeWorkbenchSite;

        this.activeWorkbenchWindow = activeWorkbenchWindow;
        this.commandId = commandId;
        this.handler = handler;

        switch (priority) {
        case 0:
        case 1:
            this.priority = Priority.MEDIUM;
            break;
        case 2:
        case 3:
        case 4:
            this.priority = Priority.LOW;
            break;
        case 5:
        default:
            this.priority = Priority.LEGACY;
            break;
        }
    }

    /**
     * @deprecated to be removed for 3.0
     * 
     * @param activeWorkbenchSite
     * @param commandId
     * @param handler
     * @param priority
     * @param activeShell
     */
    public HandlerSubmission(IWorkbenchSite activeWorkbenchSite,
            String commandId, IHandler handler, int priority, Shell activeShell) {
        if (commandId == null || handler == null)
                throw new NullPointerException();

        this.activeShell = activeShell;

        if (activeWorkbenchSite instanceof IWorkbenchPartSite)
                this.activeWorkbenchPartSite = (IWorkbenchPartSite) activeWorkbenchSite;

        this.commandId = commandId;
        this.handler = handler;

        switch (priority) {
        case 0:
        case 1:
            this.priority = Priority.MEDIUM;
            break;
        case 2:
        case 3:
        case 4:
            this.priority = Priority.LOW;
            break;
        case 5:
        default:
            this.priority = Priority.LEGACY;
            break;
        }
    }

    public HandlerSubmission(String activePartId, Shell activeShell,
            IWorkbenchPartSite activeWorkbenchPartSite, String commandId,
            IHandler handler, Priority priority) {
        if (commandId == null || handler == null)
                throw new NullPointerException();

        this.activePartId = activePartId;
        this.activeShell = activeShell;
        this.activeWorkbenchPartSite = activeWorkbenchPartSite;
        this.commandId = commandId;
        this.handler = handler;
        this.priority = priority;
    }

    public int compareTo(Object object) {
        HandlerSubmission castedObject = (HandlerSubmission) object;
        int compareTo = Util.compare(activeWorkbenchPartSite,
                castedObject.activeWorkbenchPartSite);

        if (compareTo == 0) {
            compareTo = Util.compare(activeWorkbenchWindow,
                    castedObject.activeWorkbenchWindow);

            if (compareTo == 0) {
                compareTo = Util.compare(activeShell, castedObject.activeShell);

                if (compareTo == 0) {
                    compareTo = Util.compare(priority, castedObject.priority);

                    if (compareTo == 0) {
                        compareTo = Util.compare(commandId,
                                castedObject.commandId);

                        if (compareTo == 0)
                                compareTo = Util.compare(handler,
                                        castedObject.handler);
                    }
                }
            }
        }

        return compareTo;
    }

    public Shell getActiveShell() {
        return activeShell;
    }

    public IWorkbenchPartSite getActiveWorkbenchPartSite() {
        return activeWorkbenchPartSite;
    }

    public IWorkbenchWindow getActiveWorkbenchWindow() {
        return activeWorkbenchWindow;
    }

    public String getCommandId() {
        return commandId;
    }

    public IHandler getHandler() {
        return handler;
    }

    public Priority getPriority() {
        return priority;
    }

    public int hashCode() {
        if (!hashCodeComputed) {
            hashCode = HASH_INITIAL;
            hashCode = hashCode * HASH_FACTOR + Util.hashCode(activeShell);
            hashCode = hashCode * HASH_FACTOR
                    + Util.hashCode(activeWorkbenchPartSite);
            hashCode = hashCode * HASH_FACTOR
                    + Util.hashCode(activeWorkbenchWindow);
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
            stringBuffer.append("[activeShell="); //$NON-NLS-1$
            stringBuffer.append(activeShell);
            stringBuffer.append(",activeWorkbenchSite="); //$NON-NLS-1$
            stringBuffer.append(activeWorkbenchPartSite);
            stringBuffer.append(",activeWorkbenchWindow="); //$NON-NLS-1$
            stringBuffer.append(activeWorkbenchWindow);
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