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
import org.eclipse.ui.commands.ActionHandler;
import org.eclipse.ui.commands.HandlerSubmission;
import org.eclipse.ui.commands.IHandler;
import org.eclipse.ui.contexts.EnabledSubmission;

final class KeyBindingService implements IKeyBindingService {

    private Set enabledContextIds = Collections.EMPTY_SET;

    private List enabledSubmissions = new ArrayList();

    private Map handlerSubmissionsByAction = new HashMap();

    private String partId;

    KeyBindingService(String partId) {
        super();
        this.partId = partId;
    }

    public String[] getScopes() {
        return (String[]) enabledContextIds
                .toArray(new String[enabledContextIds.size()]);
    }

    public void registerAction(IAction action) {
        if (!handlerSubmissionsByAction.containsKey(action)) {
            String commandId = action.getActionDefinitionId();

            if (commandId != null) {
                IHandler handler = new ActionHandler(action);
                HandlerSubmission handlerSubmission = new HandlerSubmission(
                        partId, null, commandId, handler, 4);
                handlerSubmissionsByAction.put(action, handlerSubmission);
                Workbench.getInstance().getCommandSupport()
                        .addHandlerSubmissions(
                                Collections.singletonList(handlerSubmission));
            }
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
            enabledSubmissions.add(new EnabledSubmission(partId, null,
                    contextId));
        }

        Workbench.getInstance().getContextSupport().addEnabledSubmissions(
                enabledSubmissions);
    }

    public void unregisterAction(IAction action) {
        HandlerSubmission handlerSubmission = (HandlerSubmission) handlerSubmissionsByAction
                .get(action);

        if (handlerSubmission != null)
                Workbench.getInstance().getCommandSupport()
                        .removeHandlerSubmissions(
                                Collections.singletonList(handlerSubmission));
    }
}
