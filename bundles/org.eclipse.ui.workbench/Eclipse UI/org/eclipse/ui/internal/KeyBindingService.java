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
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.commands.ActionHandler;
import org.eclipse.ui.commands.HandlerSubmission;
import org.eclipse.ui.commands.IHandler;
import org.eclipse.ui.contexts.EnabledSubmission;

final class KeyBindingService implements IKeyBindingService {

    private Set enabledContextIds = Collections.EMPTY_SET;

    private List enabledSubmissions = new ArrayList();

    private Map handlerSubmissionsByCommandId = new HashMap();

    private IWorkbenchSite workbenchSite;

    KeyBindingService(IWorkbenchSite workbenchSite) {
        super();
        this.workbenchSite = workbenchSite;
    }

    public String[] getScopes() {
        return (String[]) enabledContextIds
                .toArray(new String[enabledContextIds.size()]);
    }

    public void registerAction(IAction action) {
        unregisterAction(action);
        String commandId = action.getActionDefinitionId();
        if (commandId != null) {
            IHandler handler = new ActionHandler(action);
            HandlerSubmission handlerSubmission = new HandlerSubmission(null,
                    workbenchSite, commandId, handler, 4);
            handlerSubmissionsByCommandId.put(commandId, handlerSubmission);
            Workbench.getInstance().getCommandSupport().addHandlerSubmissions(
                    Collections.singletonList(handlerSubmission));
        }
    }

    public void setScopes(String[] scopes) {
        enabledContextIds = new HashSet(Arrays.asList(scopes));
        Workbench.getInstance().getContextSupport().removeEnabledSubmissions(
                enabledSubmissions);
        enabledSubmissions.clear();
        for (Iterator iterator = enabledContextIds.iterator(); iterator
                .hasNext();) {
            String contextId = (String) iterator.next();
            enabledSubmissions.add(new EnabledSubmission(null, workbenchSite,
                    contextId));
        }
        Workbench.getInstance().getContextSupport().addEnabledSubmissions(
                enabledSubmissions);
    }

    public void unregisterAction(IAction action) {
        String commandId = action.getActionDefinitionId();

        if (commandId != null) {
            HandlerSubmission handlerSubmission = (HandlerSubmission) handlerSubmissionsByCommandId
                    .remove(commandId);

            if (handlerSubmission != null)
                    Workbench.getInstance().getCommandSupport()
                            .removeHandlerSubmissions(
                                    Collections
                                            .singletonList(handlerSubmission));
        }
    }
}
