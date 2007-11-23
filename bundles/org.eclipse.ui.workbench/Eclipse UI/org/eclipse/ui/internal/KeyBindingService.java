/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.commands.ActionHandler;
import org.eclipse.ui.commands.HandlerSubmission;
import org.eclipse.ui.commands.IHandler;
import org.eclipse.ui.commands.Priority;
import org.eclipse.ui.contexts.EnabledSubmission;
import org.eclipse.ui.internal.actions.CommandAction;
import org.eclipse.ui.internal.handlers.CommandLegacyActionWrapper;

/**
 * This service provides a nestable implementation of a key binding service.
 * This class is provided for backwards compatibility only, and might be removed
 * in the future. All of the functionality is the class can be duplicated by
 * using the commands and contexts API.
 * 
 * @since 2.0
 */
public final class KeyBindingService implements INestableKeyBindingService {

    /**
     * The currently active nested service, if any. If there are no nested
     * services or none of them are active, then this value is <code>null</code>.
     */
    private IKeyBindingService activeService = null;

    /**
     * Whether this key binding service has been disposed.  A disposed key
     * binding service should not be used again.
     */
    private boolean disposed;

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
     * The context submissions from the currently active nested service. This
     * value is <code>null</code> if there is no currently active nested
     * service.
     */
    private List nestedEnabledSubmissions = null;

    /**
     * The handler submissions from the currently active nested service. This
     * value is <code>null</code> if there is no currently active handler
     * service.
     */
    private List nestedHandlerSubmissions = null;

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
    private IWorkbenchPartSite workbenchPartSite;

    /**
     * Constructs a new instance of <code>KeyBindingService</code> on a given
     * workbench site. This instance is not nested.
     * 
     * @param workbenchPartSite
     *            The site for which this service will be responsible; should
     *            not be <code>null</code>.
     */
    public KeyBindingService(IWorkbenchPartSite workbenchPartSite) {
        this(workbenchPartSite, null);
    }

    /**
     * Constructs a new instance of <code>KeyBindingService</code> on a given
     * workbench site.
     * 
     * @param workbenchPartSite
     *            The site for which this service will be responsible; should
     *            not be <code>null</code>.
     * @param parent
     *            The parent key binding service, if any; <code>null</code> if
     *            none.
     */
    KeyBindingService(IWorkbenchPartSite workbenchPartSite,
            KeyBindingService parent) {
        this.workbenchPartSite = workbenchPartSite;
        this.parent = parent;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.INestableKeyBindingService#activateKeyBindingService(org.eclipse.ui.IWorkbenchSite)
     */
    public boolean activateKeyBindingService(IWorkbenchSite nestedSite) {
        if (disposed) {
			return false;
		}

        // Check if we should do a deactivation.
        if (nestedSite == null) {
            // We should do a deactivation, if there is one active.
            if (activeService == null) {
                // There is no active service. Do no work.
                return false;
            }
            // Deactivate the currently active nested service.
            deactivateNestedService();
            return true;
        }

        // Attempt to activate a service.
        final IKeyBindingService service = (IKeyBindingService) nestedServices
                .get(nestedSite);
        
        if (service == activeService) {
            // The service is already active, or already null
            return false;
        }

        deactivateNestedService();
        if (service!=null) {
        	activateNestedService(service);
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
     *            the reference to the active service is set to
     *            <code>null</code> but nothing else happens.
     */
    private final void activateNestedService(final IKeyBindingService service) {
        if (disposed) {
			return;
		}

        /*
         * If I have a parent, and I'm the active service, then deactivate so
         * that I can make changes.
         */
        boolean active = false;
        boolean haveParent = (parent != null);
        if (haveParent) {
            active = (parent.activeService == this);
            if (active) {
                parent.deactivateNestedService();
            }
        }

        // Update the active service.
        activeService = service;

        // Check to see that the service isn't null.
        if (service == null) {
            return;
        }

        if (haveParent) {
            if (active) {
                parent.activateNestedService(this);
            }

        } else if (activeService instanceof KeyBindingService) {
            // I have no parent, so I can make the changes myself.
            final KeyBindingService nestedService = (KeyBindingService) activeService;

            // Update the contexts.
            nestedEnabledSubmissions = nestedService.getEnabledSubmissions();
            normalizeSites(nestedEnabledSubmissions);
            Workbench.getInstance().getContextSupport().addEnabledSubmissions(
                    nestedEnabledSubmissions);

            // Update the handlers.
            nestedHandlerSubmissions = nestedService.getHandlerSubmissions();
            normalizeSites(nestedHandlerSubmissions);
            Workbench.getInstance().getCommandSupport().addHandlerSubmissions(
                    nestedHandlerSubmissions);
        }
    }

    /**
     * Deactives the currently active service. This nulls out the reference, and
     * removes all the enabled submissions for the nested service.
     */
    private final void deactivateNestedService() {
        if (disposed) {
			return;
		}

        // Don't do anything if there is no active service.
        if (activeService == null) {
            return;
        }

        // Check to see if there is a parent.
        boolean active = false;
        if (parent != null) {
            // Check if I'm the active service.
            if (parent.activeService == this) {
                active = true;
                // Deactivate myself so I can make changes.
                parent.deactivateNestedService();
            }

        } else if (activeService instanceof KeyBindingService) {
            // Remove all the nested context ids.
            Workbench.getInstance().getContextSupport()
                    .removeEnabledSubmissions(nestedEnabledSubmissions);

            /*
             * Remove all of the nested handler submissions. The handlers here
             * weren't created by this instance (but by the nest instance), and
             * hence can't be disposed here.
             */
            Workbench.getInstance().getCommandSupport()
                    .removeHandlerSubmissions(nestedHandlerSubmissions);

        }

        // Clear our reference to the active service.
        activeService = null;

        // If necessary, let my parent know that changes have occurred.
        if (active) {
            parent.activateNestedService(this);
        }
    }

    /**
     * Disposes this key binding service. This clears out all of the submissions
     * held by this service, and its nested services.
     */
    public void dispose() {
        if (!disposed) {
            deactivateNestedService();
            disposed = true;

            Workbench
                    .getInstance()
                    .getContextSupport()
                    .removeEnabledSubmissions(new ArrayList(enabledSubmissions));
            enabledSubmissions.clear();

            /*
             * Each removed handler submission, must dispose its corresponding
             * handler -- as these handlers only exist inside of this class.
             */
            final List submissions = new ArrayList(
                    handlerSubmissionsByCommandId.values());
            final Iterator submissionItr = submissions.iterator();
            while (submissionItr.hasNext()) {
                ((HandlerSubmission) submissionItr.next()).getHandler()
                        .dispose();
            }
            Workbench.getInstance().getCommandSupport()
                    .removeHandlerSubmissions(submissions);
            handlerSubmissionsByCommandId.clear();

            for (Iterator iterator = nestedServices.values().iterator(); iterator
                    .hasNext();) {
                KeyBindingService keyBindingService = (KeyBindingService) iterator
                        .next();
                keyBindingService.dispose();
            }

            nestedEnabledSubmissions = null;
            nestedHandlerSubmissions = null;
            nestedServices.clear();
        }
    }

    /**
     * Gets a copy of all the enabled submissions in the nesting chain.
     * 
     * @return All of the nested enabled submissions -- including the ones from
     *         this service. This list may be empty, but is never
     *         <code>null</code>.
     */
    private final List getEnabledSubmissions() {
        if (disposed) {
			return null;
		}

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
     *         this service. This list may be empty, but is never
     *         <code>null</code>.
     */
    private final List getHandlerSubmissions() {
        if (disposed) {
			return null;
		}

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
        if (disposed) {
			return null;
		}

        if (nestedSite == null) {
            return null;
        }

        IKeyBindingService service = (IKeyBindingService) nestedServices
                .get(nestedSite);
        if (service == null) {
            // TODO the INestedKeyBindingService API should be based on
            // IWorkbenchPartSite..
            if (nestedSite instanceof IWorkbenchPartSite) {
				service = new KeyBindingService(
                        (IWorkbenchPartSite) nestedSite, this);
			} else {
				service = new KeyBindingService(null, this);
			}

            nestedServices.put(nestedSite, service);
        }

        return service;
    }

    public String[] getScopes() {
        if (disposed) {
			return null;
		}

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
     * site. This ensures that the context manager will recognize the context as
     * active. Note: this method modifies the list in place; it is
     * <em>destructive</em>.
     * 
     * @param submissionsToModify
     *            The submissions list to modify; must not be <code>null</code>,
     *            but may be empty.
     */
    private final void normalizeSites(final List submissionsToModify) {
        if (disposed) {
			return;
		}

        final int size = submissionsToModify.size();
        for (int i = 0; i < size; i++) {
            final Object submission = submissionsToModify.get(i);
            final Object replacementSubmission;

            if (submission instanceof EnabledSubmission) {
                final EnabledSubmission enabledSubmission = (EnabledSubmission) submission;
                if (!workbenchPartSite.equals(enabledSubmission
                        .getActiveWorkbenchPartSite())) {
                    replacementSubmission = new EnabledSubmission(null,
                            enabledSubmission.getActiveShell(),
                            workbenchPartSite, enabledSubmission.getContextId());
                } else {
                    replacementSubmission = enabledSubmission;
                }

            } else if (submission instanceof HandlerSubmission) {
                final HandlerSubmission handlerSubmission = (HandlerSubmission) submission;
                if (!workbenchPartSite.equals(handlerSubmission
                        .getActiveWorkbenchPartSite())) {
                    replacementSubmission = new HandlerSubmission(null,
                            handlerSubmission.getActiveShell(),
                            workbenchPartSite,
                            handlerSubmission.getCommandId(), handlerSubmission
                                    .getHandler(), handlerSubmission
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
        if (disposed) {
			return;
		}
        
        if (action instanceof CommandLegacyActionWrapper) {
        	// this is a registration of a fake action for an already
			// registered handler
			WorkbenchPlugin
					.log("Cannot register a CommandLegacyActionWrapper back into the system"); //$NON-NLS-1$
			return;
        }
        
        if (action instanceof CommandAction) {
			// we unfortunately had to allow these out into the wild, but they
			// still must not feed back into the system
			return;
        }

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
                    workbenchPartSite.getShell(), workbenchPartSite, commandId,
                    handler, Priority.MEDIUM);
            handlerSubmissionsByCommandId.put(commandId, handlerSubmission);

            // Either submit the new handler myself, or simply re-activate.
            if (parent != null) {
                if (active) {
                    parent.activateNestedService(this);
                }
            } else {
                Workbench.getInstance().getCommandSupport()
                        .addHandlerSubmission(handlerSubmission);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.INestableKeyBindingService#removeKeyBindingService(org.eclipse.ui.IWorkbenchSite)
     */
    public boolean removeKeyBindingService(IWorkbenchSite nestedSite) {
        if (disposed) {
			return false;
		}

        final IKeyBindingService service = (IKeyBindingService) nestedServices
                .remove(nestedSite);
        if (service == null) {
            return false;
        }

        if (service.equals(activeService)) {
            deactivateNestedService();
        }

        return true;
    }

    public void setScopes(String[] scopes) {
        if (disposed) {
			return;
		}

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
            enabledSubmissions.add(new EnabledSubmission(null, null,
                    workbenchPartSite, contextId));
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
        if (disposed) {
			return;
		}
        
        if (action instanceof CommandLegacyActionWrapper) {
        	// this is a registration of a fake action for an already
			// registered handler
			WorkbenchPlugin
					.log("Cannot unregister a CommandLegacyActionWrapper out of the system"); //$NON-NLS-1$
			return;
        }

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

            /*
             * Either activate this service again, or remove the submission
             * myself.
             */
            if (parent != null) {
                if (active) {
                    parent.activateNestedService(this);
                }
            } else {
            	if (handlerSubmission != null) {
                    Workbench.getInstance().getCommandSupport()
                            .removeHandlerSubmission(handlerSubmission);
                    handlerSubmission.getHandler().dispose();
                }
            }
        }
    }
}
