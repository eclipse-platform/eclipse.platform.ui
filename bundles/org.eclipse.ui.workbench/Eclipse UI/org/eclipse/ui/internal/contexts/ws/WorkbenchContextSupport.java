/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.contexts.ws;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.contexts.ContextActivationServiceFactory;
import org.eclipse.ui.contexts.ContextManagerFactory;
import org.eclipse.ui.contexts.EnabledSubmission;
import org.eclipse.ui.contexts.ICompoundContextActivationService;
import org.eclipse.ui.contexts.IContextManager;
import org.eclipse.ui.contexts.IMutableContextManager;
import org.eclipse.ui.contexts.IWorkbenchContextSupport;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.contexts.ProxyContextManager;
import org.eclipse.ui.internal.util.Util;

public class WorkbenchContextSupport implements IWorkbenchContextSupport {

    private IWorkbenchWindow activeWorkbenchWindow;

    private String activePartId;

    private String activePerspectiveId;

    private Map enabledSubmissionsByContextId = new HashMap();

    private IPageListener pageListener = new IPageListener() {

        public void pageActivated(IWorkbenchPage workbenchPage) {
            processEnabledSubmissionsByContextId(false);
        }

        public void pageClosed(IWorkbenchPage workbenchPage) {
            processEnabledSubmissionsByContextId(false);
        }

        public void pageOpened(IWorkbenchPage workbenchPage) {
            processEnabledSubmissionsByContextId(false);
        }
    };

    private IPartListener partListener = new IPartListener() {

        public void partActivated(IWorkbenchPart workbenchPart) {
            processEnabledSubmissionsByContextId(false);
        }

        public void partBroughtToTop(IWorkbenchPart workbenchPart) {
            processEnabledSubmissionsByContextId(false);
        }

        public void partClosed(IWorkbenchPart workbenchPart) {
            processEnabledSubmissionsByContextId(false);
        }

        public void partDeactivated(IWorkbenchPart workbenchPart) {
            processEnabledSubmissionsByContextId(false);
        }

        public void partOpened(IWorkbenchPart workbenchPart) {
            processEnabledSubmissionsByContextId(false);
        }
    };

    private IPerspectiveListener perspectiveListener = new IPerspectiveListener() {

        public void perspectiveActivated(IWorkbenchPage workbenchPage,
                IPerspectiveDescriptor perspectiveDescriptor) {
            processEnabledSubmissionsByContextId(false);
        }

        public void perspectiveChanged(IWorkbenchPage workbenchPage,
                IPerspectiveDescriptor perspectiveDescriptor, String changeId) {
            processEnabledSubmissionsByContextId(false);
        }
    };

    private IWindowListener windowListener = new IWindowListener() {

        public void windowActivated(IWorkbenchWindow window) {
            processEnabledSubmissionsByContextId(false);
        }

        public void windowClosed(IWorkbenchWindow window) {
            processEnabledSubmissionsByContextId(false);
        }

        public void windowDeactivated(IWorkbenchWindow window) {
            processEnabledSubmissionsByContextId(false);
        }

        public void windowOpened(IWorkbenchWindow window) {
            processEnabledSubmissionsByContextId(false);
        }
    };

    private Workbench workbench;

    private ICompoundContextActivationService compoundContextActivationService;

    private IMutableContextManager mutableContextManager;

    private ProxyContextManager proxyContextManager;

    public WorkbenchContextSupport(Workbench workbench) {
        this.workbench = workbench;
        mutableContextManager = ContextManagerFactory
                .getMutableContextManager();
        proxyContextManager = new ProxyContextManager(mutableContextManager);
        compoundContextActivationService = ContextActivationServiceFactory
                .getCompoundContextActivationService();
        workbench.addWindowListener(windowListener);
    }

    public void addEnabledSubmissions(List enabledSubmissions) {
        enabledSubmissions = Util.safeCopy(enabledSubmissions,
                EnabledSubmission.class);

        for (Iterator iterator = enabledSubmissions.iterator(); iterator
                .hasNext();) {
            EnabledSubmission enabledSubmission = (EnabledSubmission) iterator
                    .next();
            String contextId = enabledSubmission.getContextId();
            List enabledSubmissions2 = (List) enabledSubmissionsByContextId
                    .get(contextId);

            if (enabledSubmissions2 == null) {
                enabledSubmissions2 = new ArrayList();
                enabledSubmissionsByContextId.put(contextId,
                        enabledSubmissions2);
            }

            enabledSubmissions2.add(enabledSubmission);
        }

        processEnabledSubmissionsByContextId(true);
    }

    public ICompoundContextActivationService getCompoundContextActivationService() {
        return compoundContextActivationService;
    }

    public IContextManager getContextManager() {
        return proxyContextManager;
    }

    private void processEnabledSubmissionsByContextId(boolean force) {
        Set enabledContextIds = new HashSet();
        String activePartId = null;
        String activePerspectiveId = null;
        IWorkbenchWindow activeWorkbenchWindow = workbench
                .getActiveWorkbenchWindow();

        if (this.activeWorkbenchWindow != activeWorkbenchWindow) {
            if (this.activeWorkbenchWindow != null) {
                this.activeWorkbenchWindow.removePageListener(pageListener);
                this.activeWorkbenchWindow
                        .removePerspectiveListener(perspectiveListener);
                this.activeWorkbenchWindow.getPartService().removePartListener(
                        partListener);
            }

            this.activeWorkbenchWindow = activeWorkbenchWindow;

            if (this.activeWorkbenchWindow != null) {
                this.activeWorkbenchWindow.addPageListener(pageListener);
                this.activeWorkbenchWindow
                        .addPerspectiveListener(perspectiveListener);
                this.activeWorkbenchWindow.getPartService().addPartListener(
                        partListener);
            }
        }

        if (activeWorkbenchWindow != null) {
            IWorkbenchPage activeWorkbenchPage = activeWorkbenchWindow
                    .getActivePage();

            if (activeWorkbenchPage != null) {
                IWorkbenchPart activeWorkbenchPart = activeWorkbenchPage
                        .getActivePart();

                if (activeWorkbenchPart != null) {
                    IWorkbenchPartSite activeWorkbenchPartSite = activeWorkbenchPart
                            .getSite();

                    if (activeWorkbenchPartSite != null)
                            activePartId = activeWorkbenchPartSite.getId();
                }

                IPerspectiveDescriptor perspectiveDescriptor = activeWorkbenchPage
                        .getPerspective();

                if (perspectiveDescriptor != null)
                        activePerspectiveId = perspectiveDescriptor.getId();
            }
        }

        if (force || !Util.equals(this.activePartId, activePartId)
                || !Util.equals(this.activePerspectiveId, activePerspectiveId)) {
            this.activePartId = activePartId;
            this.activePerspectiveId = activePerspectiveId;

            for (Iterator iterator = enabledSubmissionsByContextId.entrySet()
                    .iterator(); iterator.hasNext();) {
                Map.Entry entry = (Map.Entry) iterator.next();
                String contextId = (String) entry.getKey();
                List enabledSubmissions = (List) entry.getValue();
                //SortedSet matchingEnabledSubmissions = null;

                for (Iterator iterator2 = enabledSubmissions.iterator(); iterator2
                        .hasNext();) {
                    EnabledSubmission enabledSubmission = (EnabledSubmission) iterator2
                            .next();
                    String activePartId2 = enabledSubmission.getActivePartId();
                    String activePerspectiveId2 = enabledSubmission
                            .getActivePerspectiveId();

                    if (activePartId2 != null && activePartId2 != activePartId)
                            continue;

                    if (activePerspectiveId2 != null
                            && activePerspectiveId2 != activePerspectiveId)
                            continue;

                    enabledContextIds.add(contextId);
                    /*
                     * if (matchingEnabledSubmissions == null)
                     * matchingEnabledSubmissions = new TreeSet();
                     * 
                     * matchingEnabledSubmissions.add(enabledSubmission);
                     */
                }

                /*
                 * if (matchingEnabledSubmissions != null) { EnabledSubmission
                 * bestEnabledSubmission = (EnabledSubmission)
                 * matchingEnabledSubmissions .last();
                 * enabledContextIds.add(contextId); }
                 */
            }

            /*
             * TODO switch to this from the old mechanism in
             * WorkbenchContextsAndContexts ((ContextManager)
             * mutableContextManager)
             * .setEnabledsByContextId(enabledsByContextId);
             */
        }
    }

    public void removeEnabledSubmissions(List enabledSubmissions) {
        enabledSubmissions = Util.safeCopy(enabledSubmissions,
                EnabledSubmission.class);

        for (Iterator iterator = enabledSubmissions.iterator(); iterator
                .hasNext();) {
            EnabledSubmission enabledSubmission = (EnabledSubmission) iterator
                    .next();
            String contextId = enabledSubmission.getContextId();
            List enabledSubmissions2 = (List) enabledSubmissionsByContextId
                    .get(contextId);

            if (enabledSubmissions2 != null) {
                enabledSubmissions2.remove(enabledSubmission);

                if (enabledSubmissions2.isEmpty())
                        enabledSubmissionsByContextId.remove(contextId);
            }
        }

        processEnabledSubmissionsByContextId(true);
    }
}
