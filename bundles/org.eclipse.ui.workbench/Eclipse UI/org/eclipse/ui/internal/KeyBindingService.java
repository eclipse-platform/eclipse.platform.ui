/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.action.IAction;

import org.eclipse.ui.IKeyBindingService;
import org.eclipse.ui.INestableKeyBindingService;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.commands.ActionHandler;
import org.eclipse.ui.commands.HandlerSubmission;
import org.eclipse.ui.commands.IHandler;
import org.eclipse.ui.contexts.EnabledSubmission;

/**
 * This service provides a nestable implementation of a key binding service.
 * This class is provided for backwards compatibility only, and might be
 * removed in the future. All of the functionality is the class can be
 * duplicated by using the commands and contexts API.
 * 
 * @since 2.0
 */
final class KeyBindingService implements INestableKeyBindingService {

    /**
     * The currently active nested service, if any. If there are no nested
     * services or none of them are active, then this value is <code>null</code>.
     */
    private IKeyBindingService activeService = null;

    /**
     * The set of context identifiers enabled in this key binding service (not
     * counting any nested services). This set may be empty, but it is never
     * <code>null</code>.
     */
    private Set enabledContextIds = Collections.EMPTY_SET;

    /**
     * The list of context submissions indicating the enabled state of the
     * context. This does not include those from nested services. This list may
     * be empty, but it is never <code>null</code>.
     */
    private List enabledSubmissions = new ArrayList();

    /**
     * The map of handler submissions, sorted by command identifiers. This does
     * not include those from nested services. This map may be empty, but it is
     * never <code>null</code>.
     */
    private Map handlerSubmissionsByCommandId = new HashMap();

    /**
     * The map of workbench part sites to nested key binding services. This map
     * may be empty, but is never <code>null</code>.
     */
    private final Map nestedServices = new HashMap();

    /**
     * The parent for this key binding service; <code>null</code> if there is
     * no parent. If there is a parent, then this means that it should not do a
     * "live" update of its contexts or handlers, but should make a call to the
     * parent instead.
     */
    private final KeyBindingService parent;

    /**
     * The site within the workbench at which this service is provided. This
     * value should not be <code>null</code>.
     */
    private IWorkbenchSite workbenchSite;

    /**
     * Constructs a new instance of <code>KeyBindingService</code> on a given
     * workbench site. This instance is not nested.
     * 
     * @param workbenchSite
     *            The site for which this service will be responsible; should
     *            not be <code>null</code>.
     */
    KeyBindingService(IWorkbenchSite workbenchSite) {
        this(workbenchSite, null);
    }

    /**
     * Constructs a new instance of <code>KeyBindingService</code> on a given
     * workbench site.
     * 
     * @param workbenchSite
     *            The site for which this service will be responsible; should
     *            not be <code>null</code>.
     * @param parent
     *            The parent key binding service, if any; <code>null</code>
     *            if none.
     */
    KeyBindingService(IWorkbenchSite workbenchSite, KeyBindingService parent) {
        this.workbenchSite = workbenchSite;
        this.parent = parent;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.INestableKeyBindingService#activateKeyBindingService(org.eclipse.ui.IWorkbenchSite)
     */
    public boolean activateKeyBindingService(IWorkbenchSite nestedSite) {
        // Check if we should do a deactivation.
        if (nestedSite == null) {
            if (activeService == null) {
                return false;
            } else {
                deactivateNestedService();
                return true;
            }
        }

        // Attempt to activate a service.
        final IKeyBindingService service = (IKeyBindingService) nestedServices
                .get(nestedSite);
        if (service == null) { return false; }

        if (parent != null) {
            parent.activateNestedService(service);
        }
        activateNestedService(service);
        if (parent != null) {
            parent.deactivateNestedService();
        }

        return true;
    }

    /**
     * Activates the given service without worrying about the currently active
     * service. This goes through the work of adding all of the nested context
     * ids as enabled submissions.
     * 
     * @param service
     *            The service to become active; if <code>null</code>, then
     *            the reference to the active service is set to <code>null</code>
     *            but nothing else happens.
     */
    private final void activateNestedService(final IKeyBindingService service) {
        // Remove myself from the parent before continuing.
        boolean active = false;
        if ((parent != null) && (parent.activeService == this)) {
            active = true;
            parent.deactivateNestedService();
        }

        // Update the active service.
        activeService = service;

        // Check to see that the service isn't null.
        if (service == null) { return; }

        if (active) {
            parent.activateNestedService(this);

        } else if (activeService instanceof KeyBindingService) {
            final KeyBindingService nestedService = (KeyBindingService) activeService;

            // Update the contexts.
            final List nestedEnabledSubmissions = nestedService
                    .getEnabledSubmissions();
            normalizeSites(nestedEnabledSubmissions);
            Workbench.getInstance().getContextSupport().addEnabledSubmissions(
                    nestedEnabledSubmissions);

            // Update the handlers.
            final List nestedHandlerSubmissions = nestedService
                    .getHandlerSubmissions();
            normalizeSites(nestedHandlerSubmissions);
            Workbench.getInstance().getCommandSupport().addHandlerSubmissions(
                    nestedHandlerSubmissions);
        }
    }

    /**
     * Deactives the currently active service. This nulls out the reference,
     * and removes all the enabled submissions for the nested service.
     */
    private final void deactivateNestedService() {
        // Don't do anything if there is no active service.
        if (activeService == null) { return; }

        // Remove myself from the parent before continuing.
        boolean active = false;
        if ((parent != null) && (parent.activeService == this)) {
            active = true;
            parent.deactivateNestedService();

        } else if (activeService instanceof KeyBindingService) {
            final KeyBindingService nestedService = (KeyBindingService) activeService;

            // Remove all the nested context ids.
            final List nestedEnabledSubmissions = nestedService
                    .getEnabledSubmissions();
            normalizeSites(nestedEnabledSubmissions);
            Workbench.getInstance().getContextSupport()
                    .removeEnabledSubmissions(nestedEnabledSubmissions);

            // Remove all of the nested handler submissions.
            final List nestedHandlerSubmissions = nestedService
                    .getHandlerSubmissions();
            normalizeSites(nestedHandlerSubmissions);
            Workbench.getInstance().getCommandSupport()
                    .removeHandlerSubmissions(nestedHandlerSubmissions);

        }

        // Clear our reference to the active service.
        activeService = null;

        // If necessary, reactivate this nested service.
        if (active) {
            parent.activateNestedService(this);
        }
    }

    /**
     * Gets a copy of all the enabled submissions in the nesting chain.
     * 
     * @return All of the nested enabled submissions -- including the ones from
     *         this service. This list may be empty, but is never <code>null</code>.
     */
    private final List getEnabledSubmissions() {
        final List submissions = new ArrayList(enabledSubmissions);
        if (activeService instanceof KeyBindingService) {
            final KeyBindingService nestedService = (KeyBindingService) activeService;
            submissions.addAll(nestedService.getEnabledSubmissions());
        }
        return submissions;
    }

    /**
     * Gets a copy of all the handler submissions in the nesting chain.
     * 
     * @return All of the nested handler submissions -- including the ones from
     *         this service. This list may be empty, but is never <code>null</code>.
     */
    private final List getHandlerSubmissions() {
        final List submissions = new ArrayList(handlerSubmissionsByCommandId
                .values());
        if (activeService instanceof KeyBindingService) {
            final KeyBindingService nestedService = (KeyBindingService) activeService;
            submissions.addAll(nestedService.getHandlerSubmissions());
        }
        return submissions;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.INestableKeyBindingService#getKeyBindingService(org.eclipse.ui.IWorkbenchSite)
     */
    public IKeyBindingService getKeyBindingService(IWorkbenchSite nestedSite) {
        if (nestedSite == null) { return null; }

        IKeyBindingService service = (IKeyBindingService) nestedServices
                .get(nestedSite);
        if (service == null) {
            service = new KeyBindingService(nestedSite, this);
            nestedServices.put(nestedSite, service);
        }

        return service;
    }

    public String[] getScopes() {
        // Get the nested scopes, if any.
        final String[] nestedScopes;
        if (activeService == null) {
            nestedScopes = null;
        } else {
            nestedScopes = activeService.getScopes();
        }

        // Build the list of active scopes
        final Set activeScopes = new HashSet();
        activeScopes.addAll(enabledContextIds);
        if (nestedScopes != null) {
            for (int i = 0; i < nestedScopes.length; i++) {
                activeScopes.add(nestedScopes[i]);
            }
        }

        return (String[]) activeScopes.toArray(new String[activeScopes.size()]);
    }

    /**
     * Replaces the active workbench site with this service's active workbench
     * site. This ensures that the context manager will recognize the context
     * as active. Note: this method modifies the list in place; it is <em>destructive</em>.
     * 
     * @param submissionsToModify
     *            The submissions list to modify; must not be <code>null</code>,
     *            but may be empty.
     */
    private final void normalizeSites(final List submissionsToModify) {
        final int size = submissionsToModify.size();
        for (int i = 0; i < size; i++) {
            final Object submission = submissionsToModify.get(i);
            final Object replacementSubmission;

            if (submission instanceof EnabledSubmission) {
                final EnabledSubmission enabledSubmission = (EnabledSubmission) submission;
                if (!workbenchSite.equals(enabledSubmission
                        .getActiveWorkbenchSite())) {
                    replacementSubmission = new EnabledSubmission(
                            enabledSubmission.getActivePerspectiveDescriptor(),
                            workbenchSite, enabledSubmission.getContextId());
                } else {
                    replacementSubmission = enabledSubmission;
                }

            } else if (submission instanceof HandlerSubmission) {
                final HandlerSubmission handlerSubmission = (HandlerSubmission) submission;
                if (!workbenchSite.equals(handlerSubmission
                        .getActiveWorkbenchSite())) {
                    replacementSubmission = new HandlerSubmission(
                            handlerSubmission.getActivePerspectiveDescriptor(),
                            workbenchSite, handlerSubmission.getCommandId(),
                            handlerSubmission.getHandler(), handlerSubmission
                                    .getPriority());
                } else {
                    replacementSubmission = handlerSubmission;
                }

            } else {
                replacementSubmission = submission;
            }

            submissionsToModify.set(i, replacementSubmission);
        }

    }

    public void registerAction(IAction action) {
        unregisterAction(action);
        String commandId = action.getActionDefinitionId();
        if (commandId != null) {
            /*
             * If I have a parent and I'm active, de-activate myself while
             * making changes.
             */
            boolean active = false;
            if ((parent != null) && (parent.activeService == this)) {
                active = true;
                parent.deactivateNestedService();
            }

            // Create the new submission
            IHandler handler = new ActionHandler(action);
            HandlerSubmission handlerSubmission = new HandlerSubmission(null,
                    workbenchSite, commandId, handler, 4);
            handlerSubmissionsByCommandId.put(commandId, handlerSubmission);

            // Either submit the new handler myself, or simply re-activate.
            if (parent != null) {
                if (active) {
                    parent.activateNestedService(this);
                }
            } else {
                Workbench.getInstance().getCommandSupport()
                        .addHandlerSubmissions(
                                Collections.singletonList(handlerSubmission));
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.INestableKeyBindingService#removeKeyBindingService(org.eclipse.ui.IWorkbenchSite)
     */
    public boolean removeKeyBindingService(IWorkbenchSite nestedSite) {
        final IKeyBindingService service = (IKeyBindingService) nestedServices
                .remove(nestedSite);
        if (service == null) { return false; }

        if (service.equals(activeService)) {
            deactivateNestedService();
        }

        return true;
    }

    public void setScopes(String[] scopes) {
        // Either deactivate myself, or remove the previous submissions myself.
        boolean active = false;
        if ((parent != null) && (parent.activeService == this)) {
            active = true;
            parent.deactivateNestedService();
        } else {
            Workbench.getInstance().getContextSupport()
                    .removeEnabledSubmissions(enabledSubmissions);
        }
        enabledSubmissions.clear();

        // Determine the new list of submissions.
        enabledContextIds = new HashSet(Arrays.asList(scopes));
        for (Iterator iterator = enabledContextIds.iterator(); iterator
                .hasNext();) {
            String contextId = (String) iterator.next();
            enabledSubmissions.add(new EnabledSubmission(null, workbenchSite,
                    contextId));
        }

        // Submit the new contexts myself, or simply re-active myself.
        if (parent != null) {
            if (active) {
                parent.activateNestedService(this);
            }
        } else {
            Workbench.getInstance().getContextSupport().addEnabledSubmissions(
                    enabledSubmissions);
        }
    }

    public void unregisterAction(IAction action) {
        String commandId = action.getActionDefinitionId();

        if (commandId != null) {
            // Deactivate this service while making changes.
            boolean active = false;
            if ((parent != null) && (parent.activeService == this)) {
                active = true;
                parent.deactivateNestedService();
            }

            // Remove the current submission, if any.
            HandlerSubmission handlerSubmission = (HandlerSubmission) handlerSubmissionsByCommandId
                    .remove(commandId);

            // Either activate this service again, or make the submission
            // myself
            if (handlerSubmission != null) {
                if (parent != null) {
                    if (active) {
                        parent.activateNestedService(this);
                    }
                } else {
                    Workbench.getInstance().getCommandSupport()
                            .removeHandlerSubmissions(
                                    Collections
                                            .singletonList(handlerSubmission));
                }
            }
        }
    }
}
