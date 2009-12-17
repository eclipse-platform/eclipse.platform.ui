/*******************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.contexts;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.internal.util.Util;

/**
 * </p>
 * An instance of this class represents a request to enabled a context. An
 * enabled submission specifies a list of conditions under which it would be
 * appropriate for a particular context to be enabled. These conditions include
 * things like the active part or the active shell. So, it is possible to say
 * things like: "when the java editor is active, please consider enabling the
 * 'editing java' context".
 * </p>
 * <p>
 * The workbench considers all of the submissions it has received and choses the
 * ones it views as the best possible match.
 * </p>
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 * <p>
 * Note: this class has a natural ordering that is inconsistent with equals.
 * </p>
 * 
 * @since 3.0
 * @see IWorkbenchContextSupport
 * @deprecated Please use <code>IContextService.activateContext</code>
 *             instead.
 * @see org.eclipse.ui.contexts.IContextService
 */
public final class EnabledSubmission implements Comparable {

    /**
     * The identifier of the part in which this context should be enabled. If
     * this value is <code>null</code>, this means it should be active in any
     * part.
     */
    private final String activePartId;

    /**
     * The shell in which this context should be enabled. If this value is
     * <code>null</code>, this means it should be active in any shell.
     */
    private final Shell activeShell;

    /**
     * The part site in which this context should be enabled. If this value is
     * <code>null</code>, this means it should be active in any part site.
     */
    private final IWorkbenchPartSite activeWorkbenchPartSite;

    /**
     * The identifier for the context that should be enabled by this
     * submissions. This value should never be <code>null</code>.
     */
    private final String contextId;

    /**
     * The cached string representation of this instance. This value is computed
     * lazily on the first call to retrieve the string representation, and the
     * cache is used for all future calls. If this value is <code>null</code>,
     * then the value has not yet been computed.
     */
    private transient String string = null;

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
        if (contextId == null) {
			throw new NullPointerException();
		}

        this.activePartId = activePartId;
        this.activeShell = activeShell;
        this.activeWorkbenchPartSite = activeWorkbenchPartSite;
        this.contextId = contextId;
    }

    /**
     * @see Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object object) {
        EnabledSubmission castedObject = (EnabledSubmission) object;
        int compareTo = Util.compare(activeWorkbenchPartSite,
                castedObject.activeWorkbenchPartSite);

        if (compareTo == 0) {
            compareTo = Util.compare(activePartId, castedObject.activePartId);

            if (compareTo == 0) {
                compareTo = Util.compare(activeShell, castedObject.activeShell);

                if (compareTo == 0) {
					compareTo = Util.compare(contextId, castedObject.contextId);
				}
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

    /**
     * @see Object#toString()
     */
    public String toString() {
        if (string == null) {
            final StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("[activePartId="); //$NON-NLS-1$
            stringBuffer.append(activePartId);
            stringBuffer.append(",activeShell="); //$NON-NLS-1$
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
