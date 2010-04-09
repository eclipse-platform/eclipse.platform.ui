/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.commands.contexts.Context;
import org.eclipse.e4.ui.services.EContextService;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IKeyBindingService;
import org.eclipse.ui.INestableKeyBindingService;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.internal.actions.CommandAction;
import org.eclipse.ui.internal.e4.compatibility.E4Util;
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
     * Whether this key binding service has been disposed.  A disposed key
     * binding service should not be used again.
     */
    private boolean disposed;

    /**
     * The set of context identifiers enabled in this key binding service (not
     * counting any nested services). This set may be empty, but it is never
     * <code>null</code>.
     */
	private Set<String> enabledContextIds = Collections.EMPTY_SET;


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
		E4Util.unsupported("KeyBindingService: " + parent); //$NON-NLS-1$
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

		E4Util.unsupported("activateKeyBindingService"); //$NON-NLS-1$
        return true;
    }

    /**
     * Disposes this key binding service. This clears out all of the submissions
     * held by this service, and its nested services.
     */
    public void dispose() {
        if (!disposed) {
            disposed = true;
        }
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

		E4Util.unsupported("getKeyBindingService"); //$NON-NLS-1$

        return null;
    }

    public String[] getScopes() {
        if (disposed) {
			return null;
		}

        // Build the list of active scopes
        final Set activeScopes = new HashSet();
        activeScopes.addAll(enabledContextIds);

        return (String[]) activeScopes.toArray(new String[activeScopes.size()]);
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

        return true;
    }

    public void setScopes(String[] scopes) {
        if (disposed) {
			return;
		}
		Set<String> oldContextIds = enabledContextIds;
		enabledContextIds = new HashSet<String>(Arrays.asList(scopes));
		EContextService cs = (EContextService) workbenchPartSite.getService(EContextService.class);
		addParents(cs, scopes);
		
		for (String id : oldContextIds) {
			if (!enabledContextIds.contains(id)) {
				cs.deactivateContext(id);
			}
        }
		for (String id : enabledContextIds) {
			if (!oldContextIds.contains(id)) {
				cs.activateContext(id);
			}
		}
    }

    /**
	 * @param cs
	 * @param scopes
	 */
	private void addParents(EContextService cs, String[] scopes) {
		for (String id : scopes) {
			try {
				Context current = cs.getContext(id);
				String parentId = current.getParentId();
				while (parentId != null && !enabledContextIds.contains(parentId)) {
					enabledContextIds.add(parentId);
					current = cs.getContext(parentId);
					parentId = current.getParentId();
				}
			} catch (NotDefinedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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

    }
}
