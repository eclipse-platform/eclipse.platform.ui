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
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.util.Util;

/**
 * An instance of this class represents a request to enable a context, under
 * particular conditions.
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 * 
 * @since 3.0
 * @see IWorkbenchContextSupport
 */
public final class EnabledSubmission implements Comparable {

    private final static int HASH_FACTOR = 89;

    private final static int HASH_INITIAL = EnabledSubmission.class.getName()
            .hashCode();

    private String activePartId;

    private Shell activeShell;

    private IWorkbenchPartSite activeWorkbenchPartSite;

    private String contextId;

    private transient int hashCode;

    private transient boolean hashCodeComputed;

    private transient String string;

    /**
     * @deprecated to be removed for 3.0
     * 
     * @param activeWorkbenchSite
     * @param activeWorkbenchWindow
     * @param contextId
     */
    public EnabledSubmission(IWorkbenchSite activeWorkbenchSite,
            IWorkbenchWindow activeWorkbenchWindow, String contextId) {
        if (contextId == null) throw new NullPointerException();

        if (activeWorkbenchSite instanceof IWorkbenchPartSite)
                this.activeWorkbenchPartSite = (IWorkbenchPartSite) activeWorkbenchSite;

        this.contextId = contextId;
    }

    /**
     * @deprecated to be removed for 3.0
     * 
     * @param activeWorkbenchSite
     * @param contextId
     * @param activeShell
     */
    public EnabledSubmission(Shell activeShell,
            IWorkbenchSite activeWorkbenchSite, String contextId) {
        if (contextId == null) throw new NullPointerException();

        this.activeShell = activeShell;

        if (activeWorkbenchSite instanceof IWorkbenchPartSite)
                this.activeWorkbenchPartSite = (IWorkbenchPartSite) activeWorkbenchSite;

        this.contextId = contextId;
    }

    /**
     * Creates a new instance of this class.
     * 
     * @param activePartId
     *            the identifier of the part that must be active for this
     *            request to be considered. May be <code>null</code>.
     * @param activeShell
     *            the shell that must be active for this request to be
     *            considered. May be <code>null</code>.
     * @param activeWorkbenchPartSite
     *            the workbench part site of the part that must be active for
     *            this request to be considered. May be <code>null</code>.
     * @param contextId
     *            the identifier of the context to be enabled. Must not be
     *            <code>null</code>.
     */
    public EnabledSubmission(String activePartId, Shell activeShell,
            IWorkbenchPartSite activeWorkbenchPartSite, String contextId) {
        if (contextId == null) throw new NullPointerException();

        this.activePartId = activePartId;
        this.activeShell = activeShell;
        this.activeWorkbenchPartSite = activeWorkbenchPartSite;
        this.contextId = contextId;
    }

    public int compareTo(Object object) {
        EnabledSubmission castedObject = (EnabledSubmission) object;
        int compareTo = Util.compare(activeWorkbenchPartSite,
                castedObject.activeWorkbenchPartSite);

        if (compareTo == 0) {
            compareTo = Util.compare(activePartId, castedObject.activePartId);

            if (compareTo == 0) {
                compareTo = Util.compare(activeShell, castedObject.activeShell);

                if (compareTo == 0)
                        compareTo = Util.compare(contextId,
                                castedObject.contextId);
            }
        }

        return compareTo;
    }

    /**
     * Returns the identifier of the part that must be active for this request
     * to be considered.
     * 
     * @return the identifier of the part that must be active for this request
     *         to be considered. May be <code>null</code>.
     */
    public String getActivePartId() {
        return activePartId;
    }

    /**
     * Returns the shell that must be active for this request to be considered.
     * 
     * @return the shell that must be active for this request to be considered.
     *         May be <code>null</code>.
     */
    public Shell getActiveShell() {
        return activeShell;
    }

    /**
     * Returns the workbench part site of the part that must be active for this
     * request to be considered.
     * 
     * @return the workbench part site of the part that must be active for this
     *         request to be considered. May be <code>null</code>.
     */
    public IWorkbenchPartSite getActiveWorkbenchPartSite() {
        return activeWorkbenchPartSite;
    }

    /**
     * Returns the identifier of the context to be enabled.
     * 
     * @return the identifier of the context to be enabled. Guaranteed not to be
     *         <code>null</code>.
     */
    public String getContextId() {
        return contextId;
    }

    public int hashCode() {
        if (!hashCodeComputed) {
            hashCode = HASH_INITIAL;
            hashCode = hashCode * HASH_FACTOR + Util.hashCode(activePartId);
            hashCode = hashCode * HASH_FACTOR + Util.hashCode(activeShell);
            hashCode = hashCode * HASH_FACTOR
                    + Util.hashCode(activeWorkbenchPartSite);
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
            stringBuffer.append("activeShell="); //$NON-NLS-1$
            stringBuffer.append(activeShell);
            stringBuffer.append(",activeWorkbenchSite="); //$NON-NLS-1$
            stringBuffer.append(activeWorkbenchPartSite);
            stringBuffer.append(",contextId="); //$NON-NLS-1$
            stringBuffer.append(contextId);
            stringBuffer.append(']');
            string = stringBuffer.toString();
        }

        return string;
    }
}