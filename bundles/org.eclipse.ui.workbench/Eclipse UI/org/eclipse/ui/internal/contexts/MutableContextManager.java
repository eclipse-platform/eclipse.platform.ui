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

package org.eclipse.ui.internal.contexts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.WeakHashMap;

import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.contexts.ContextEvent;
import org.eclipse.ui.contexts.ContextManagerEvent;
import org.eclipse.ui.contexts.IContext;
import org.eclipse.ui.contexts.IContextBinding;
import org.eclipse.ui.contexts.IMutableContextManager;
import org.eclipse.ui.internal.util.Util;

public final class MutableContextManager extends AbstractContextManager
        implements IMutableContextManager {

    static boolean isContextDefinitionChildOf(String ancestor, String id,
            Map contextDefinitionsById) {
        Collection visited = new HashSet();

        while (id != null && !visited.contains(id)) {
            ContextDefinition contextDefinition = (ContextDefinition) contextDefinitionsById
                    .get(id);
            visited.add(id);

            if (contextDefinition != null
                    && Util.equals(id = contextDefinition.getParentId(),
                            ancestor)) return true;
        }

        return false;
    }

    private Map contextContextBindingsByParentContextId = new HashMap();

    private Map contextDefinitionsById = new HashMap();

    private IContextRegistry contextRegistry;

    private Map contextsById = new WeakHashMap();

    private Set definedContextIds = new HashSet();

    private Set enabledContextIds = new HashSet();

    public MutableContextManager() {
        this(new ExtensionContextRegistry(Platform.getExtensionRegistry()));
    }

    public MutableContextManager(IContextRegistry contextRegistry) {
        if (contextRegistry == null) throw new NullPointerException();

        this.contextRegistry = contextRegistry;

        this.contextRegistry
                .addContextRegistryListener(new IContextRegistryListener() {

                    public void contextRegistryChanged(
                            ContextRegistryEvent contextRegistryEvent) {
                        readRegistry();
                    }
                });

        readRegistry();
    }

    public IContext getContext(String contextId) {
        if (contextId == null) throw new NullPointerException();

        Context context = (Context) contextsById.get(contextId);

        if (context == null) {
            context = new Context(contextId);
            updateContext(context);
            contextsById.put(contextId, context);
        }

        return context;
    }

    public Set getDefinedContextIds() {
        return Collections.unmodifiableSet(definedContextIds);
    }

    public Set getEnabledContextIds() {
        return Collections.unmodifiableSet(enabledContextIds);
    }

    private void getRequiredContextIds(Set contextIds, Set requiredContextIds) {
        for (Iterator iterator = contextIds.iterator(); iterator.hasNext();) {
            String contextId = (String) iterator.next();
            IContext context = getContext(contextId);
            Set childContextIds = new HashSet();
            Set contextContextBindings = context.getContextContextBindings();

            for (Iterator iterator2 = contextContextBindings.iterator(); iterator2
                    .hasNext();) {
                IContextBinding contextContextBinding = (IContextBinding) iterator2
                        .next();
                childContextIds.add(contextContextBinding.getContextId());
            }

            childContextIds.removeAll(requiredContextIds);
            requiredContextIds.addAll(childContextIds);
            getRequiredContextIds(childContextIds, requiredContextIds);
        }
    }

    private void notifyContexts(Map contextEventsByContextId) {
        for (Iterator iterator = contextEventsByContextId.entrySet().iterator(); iterator
                .hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String contextId = (String) entry.getKey();
            ContextEvent contextEvent = (ContextEvent) entry.getValue();
            Context context = (Context) contextsById.get(contextId);

            if (context != null) context.fireContextChanged(contextEvent);
        }
    }

    private void readRegistry() {
        Collection contextDefinitions = new ArrayList();
        contextDefinitions.addAll(contextRegistry.getContextDefinitions());
        Map contextDefinitionsById = new HashMap(ContextDefinition
                .contextDefinitionsById(contextDefinitions, false));

        for (Iterator iterator = contextDefinitionsById.values().iterator(); iterator
                .hasNext();) {
            ContextDefinition contextDefinition = (ContextDefinition) iterator
                    .next();
            String name = contextDefinition.getName();

            if (name == null || name.length() == 0) iterator.remove();
        }

        for (Iterator iterator = contextDefinitionsById.keySet().iterator(); iterator
                .hasNext();)
            if (!isContextDefinitionChildOf(null, (String) iterator.next(),
                    contextDefinitionsById)) iterator.remove();

        Map contextContextBindingDefinitionsByParentContextId = ContextContextBindingDefinition
                .contextContextBindingDefinitionsByParentContextId(contextRegistry
                        .getContextContextBindingDefinitions());
        Map contextContextBindingsByParentContextId = new HashMap();

        for (Iterator iterator = contextContextBindingDefinitionsByParentContextId
                .entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String parentContextId = (String) entry.getKey();

            if (contextDefinitionsById.containsKey(parentContextId)) {
                Collection contextContextBindingDefinitions = (Collection) entry
                        .getValue();

                if (contextContextBindingDefinitions != null)
                        for (Iterator iterator2 = contextContextBindingDefinitions
                                .iterator(); iterator2.hasNext();) {
                            ContextContextBindingDefinition contextContextBindingDefinition = (ContextContextBindingDefinition) iterator2
                                    .next();
                            String childContextId = contextContextBindingDefinition
                                    .getChildContextId();

                            if (contextDefinitionsById
                                    .containsKey(childContextId)) {
                                IContextBinding contextContextBinding = new ContextBinding(
                                        childContextId, parentContextId);
                                Set contextContextBindings = (Set) contextContextBindingsByParentContextId
                                        .get(parentContextId);

                                if (contextContextBindings == null) {
                                    contextContextBindings = new HashSet();
                                    contextContextBindingsByParentContextId
                                            .put(parentContextId,
                                                    contextContextBindings);
                                }

                                contextContextBindings
                                        .add(contextContextBinding);
                            }
                        }
            }
        }

        this.contextContextBindingsByParentContextId = contextContextBindingsByParentContextId;
        this.contextDefinitionsById = contextDefinitionsById;
        boolean definedContextIdsChanged = false;
        Set definedContextIds = new HashSet(contextDefinitionsById.keySet());
        Set previouslyDefinedContextIds = null;

        if (!definedContextIds.equals(this.definedContextIds)) {
            previouslyDefinedContextIds = this.definedContextIds;
            this.definedContextIds = definedContextIds;
            definedContextIdsChanged = true;
        }

        Set enabledContextIds = new HashSet(this.enabledContextIds);
        getRequiredContextIds(this.enabledContextIds, enabledContextIds);
        boolean enabledContextIdsChanged = false;
        Set previouslyEnabledContextIds = null;

        if (!this.enabledContextIds.equals(enabledContextIds)) {
            previouslyEnabledContextIds = this.enabledContextIds;
            this.enabledContextIds = enabledContextIds;
            enabledContextIdsChanged = true;
        }

        Map contextEventsByContextId = updateContexts(contextsById.keySet());

        if (definedContextIdsChanged || enabledContextIdsChanged)
                fireContextManagerChanged(new ContextManagerEvent(this,
                        definedContextIdsChanged, enabledContextIdsChanged,
                        previouslyDefinedContextIds,
                        previouslyEnabledContextIds));

        if (contextEventsByContextId != null)
                notifyContexts(contextEventsByContextId);
    }

    public void setEnabledContextIds(Set enabledContextIds) {
        enabledContextIds = Util.safeCopy(enabledContextIds, String.class);
        Set requiredContextIds = new HashSet(enabledContextIds);
        getRequiredContextIds(enabledContextIds, requiredContextIds);
        enabledContextIds = requiredContextIds;
        boolean contextManagerChanged = false;
        Map contextEventsByContextId = null;
        Set previouslyEnabledContextIds = null;

        if (!this.enabledContextIds.equals(enabledContextIds)) {
            previouslyEnabledContextIds = this.enabledContextIds;
            this.enabledContextIds = enabledContextIds;
            contextManagerChanged = true;
            contextEventsByContextId = updateContexts(contextsById.keySet());
        }

        if (contextEventsByContextId != null)
                notifyContexts(contextEventsByContextId);

        if (contextManagerChanged)
                fireContextManagerChanged(new ContextManagerEvent(this, false,
                        true, null, previouslyEnabledContextIds));
    }

    private ContextEvent updateContext(Context context) {
        Set contextContextBindings = (Set) contextContextBindingsByParentContextId
                .get(context.getId());
        boolean contextContextBindingsChanged = context
                .setContextContextBindings(contextContextBindings != null ? contextContextBindings
                        : Collections.EMPTY_SET);
        ContextDefinition contextDefinition = (ContextDefinition) contextDefinitionsById
                .get(context.getId());
        boolean definedChanged = context.setDefined(contextDefinition != null);
        boolean enabledChanged = context.setEnabled(enabledContextIds
                .contains(context.getId()));
        boolean nameChanged = context
                .setName(contextDefinition != null ? contextDefinition
                        .getName() : null);
        boolean parentIdChanged = context
                .setParentId(contextDefinition != null ? contextDefinition
                        .getParentId() : null);

        if (contextContextBindingsChanged || definedChanged || enabledChanged
                || nameChanged || parentIdChanged)
            return new ContextEvent(context, contextContextBindingsChanged,
                    definedChanged, enabledChanged, nameChanged,
                    parentIdChanged);
        else
            return null;
    }

    private Map updateContexts(Collection contextIds) {
        Map contextEventsByContextId = new TreeMap();

        for (Iterator iterator = contextIds.iterator(); iterator.hasNext();) {
            String contextId = (String) iterator.next();
            Context context = (Context) contextsById.get(contextId);

            if (context != null) {
                ContextEvent contextEvent = updateContext(context);

                if (contextEvent != null)
                        contextEventsByContextId.put(contextId, contextEvent);
            }
        }

        return contextEventsByContextId;
    }
}