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

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.util.Util;

/**
 * Submits a context to the workbench for activation. The submission indicates
 * under what conditions the context should become active. Given these
 * conditions, the workbench will decide whether the context should be active or
 * not.
 * 
 * @since 3.0
 */
public final class EnabledSubmission implements Comparable {

    private final static int HASH_FACTOR = 89;

    private final static int HASH_INITIAL = EnabledSubmission.class.getName()
            .hashCode();

    private final Shell activeShell;

    private IWorkbenchSite activeWorkbenchSite;

    private String contextId;

    private transient int hashCode;

    private transient boolean hashCodeComputed;

    private transient String string;

    /**
     * Constructs a new instance of <code>EnabledSubmission</code>.
     * 
     * @param activeWorkbenchSite
     *            The workbench site which must be active for this submission to
     *            be active. If this value is <code>null</code>, then any
     *            workbench site can be active.
     * @param activeWorkbenchWindow
     *            The workbench window which must be active for this submission
     *            to be active. If this value is <code>null</code>, then any
     *            workbench window can be active.
     * @param contextId
     *            The context which should be activated when this submission is
     *            enabled. This value must not be <code>null</code>.
     * @deprecated The workbench window has been replaced with a reference to a
     *             shell. This is a required level of generality for key
     *             bindings in dialogs (since dialogs are not workbench
     *             windows). Please use the other constructor.
     */
    public EnabledSubmission(IWorkbenchSite activeWorkbenchSite,
            IWorkbenchWindow activeWorkbenchWindow, String contextId) {
        this(activeWorkbenchWindow.getShell(), activeWorkbenchSite, contextId);
    }

    /**
     * Constructs a new instance of <code>EnabledSubmission</code>.
     * 
     * @param activeShell
     *            The shell which must be active for this submission to be
     *            active. If this value is <code>null</code>, then any shell
     *            can be active.
     * @param workbenchSite
     *            The workbench site which must be active for this submission to
     *            be active. If this value is <code>null</code>, then any
     *            workbench site can be active. workbench window can be active.
     * @param contextId
     *            The context which should be activated when this submission is
     *            enabled. This value must not be <code>null</code>.
     */
    public EnabledSubmission(final Shell activeShell,
            final IWorkbenchSite workbenchSite, final String contextId) {
        if (contextId == null) throw new NullPointerException();
        this.activeShell = activeShell;
        this.activeWorkbenchSite = workbenchSite;
        this.contextId = contextId;
    }

    public int compareTo(Object object) {
        EnabledSubmission castedObject = (EnabledSubmission) object;
        int compareTo = Util.compare(activeWorkbenchSite,
                castedObject.activeWorkbenchSite);

        if (compareTo == 0) {
            compareTo = Util.compare(activeShell, castedObject.activeShell);

            if (compareTo == 0)
                    compareTo = Util.compare(contextId, castedObject.contextId);
        }

        return compareTo;
    }

    /**
     * An accessor for the shell in which this submission should be active.
     * 
     * @return The shell in which this submission is active; <code>null</code>
     *         if it is active in any shell.
     */
    public final Shell getActiveShell() {
        return activeShell;
    }

    /**
     * An accessor the workbench site for which this submission should apply.
     * 
     * @return The workbench site for which this submission should apply;
     *         <code>null</code> if this submission applies to any workbench
     *         site.
     */
    public IWorkbenchSite getActiveWorkbenchSite() {
        return activeWorkbenchSite;
    }

    /**
     * An accessor for the identifier of the context that this submission wishes
     * to activate.
     * 
     * @return The context identifier; this should never be <code>null</code>.
     */
    public String getContextId() {
        return contextId;
    }

    public int hashCode() {
        if (!hashCodeComputed) {
            hashCode = HASH_INITIAL;
            hashCode = hashCode * HASH_FACTOR + Util.hashCode(activeShell);
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
            stringBuffer.append("[activeShell="); //$NON-NLS-1$
            stringBuffer.append(activeShell);
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