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
import org.eclipse.ui.commands.IMutableCommandHandlerService;
import org.eclipse.ui.contexts.EnabledSubmission;
import org.eclipse.ui.contexts.IMutableContextActivationService;

final class KeyBindingService implements IKeyBindingService {

    private Map handlersByCommandId = new HashMap();

    private String partId;

    private IMutableCommandHandlerService mutableCommandHandlerService;

    private IMutableContextActivationService mutableContextActivationService;

    KeyBindingService(String partId,
            IMutableCommandHandlerService mutableCommandHandlerService,
            IMutableContextActivationService mutableContextActivationService) {
        super();
        this.partId = partId;
        this.mutableCommandHandlerService = mutableCommandHandlerService;
        this.mutableContextActivationService = mutableContextActivationService;
    }

    public String[] getScopes() {
        return (String[]) enabledContextIds
                .toArray(new String[enabledContextIds.size()]);
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
        // TODO remove begin
        mutableContextActivationService.setActiveContextIds(enabledContextIds);
        // TODO remove end
    }

    private Set enabledContextIds = Collections.EMPTY_SET;

    private List enabledSubmissions = new ArrayList();

    private Map handlerSubmissionsByAction = new HashMap();

    public void registerAction(IAction action) {
        // TODO just call unregisterAction first or test and no-op begin
        HandlerSubmission handlerSubmission = (HandlerSubmission) handlerSubmissionsByAction
                .get(action);

        if (handlerSubmission != null)
                Workbench.getInstance().getCommandSupport()
                        .removeHandlerSubmissions(
                                Collections.singletonList(handlerSubmission));
        // TODO just call unregisterAction first or test and no-op end

        String commandId = action.getActionDefinitionId();

        if (commandId != null) {
            IHandler handler = new ActionHandler(action);
            handlerSubmission = new HandlerSubmission(partId, null, commandId,
                    handler, 4);
            handlerSubmissionsByAction.put(action, handlerSubmission);
            Workbench.getInstance().getCommandSupport().addHandlerSubmissions(
                    Collections.singletonList(handlerSubmission));

            // TODO remove begin
            handlersByCommandId.put(commandId, new ActionHandler(action));
            mutableCommandHandlerService
                    .setHandlersByCommandId(handlersByCommandId);
            // TODO remove end
        }
    }

    public void unregisterAction(IAction action) {
        HandlerSubmission handlerSubmission = (HandlerSubmission) handlerSubmissionsByAction
                .get(action);

        if (handlerSubmission != null)
                Workbench.getInstance().getCommandSupport()
                        .removeHandlerSubmissions(
                                Collections.singletonList(handlerSubmission));

        // TODO remove begin
        String commandId = action.getActionDefinitionId();

        if (commandId != null) {
            handlersByCommandId.remove(commandId);
            mutableCommandHandlerService
                    .setHandlersByCommandId(handlersByCommandId);
        }
        // TODO remove end
    }
}
