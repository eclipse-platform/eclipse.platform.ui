/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.bindings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.eclipse.commands.contexts.Context;
import org.eclipse.commands.contexts.ContextManager;
import org.eclipse.commands.contexts.ContextManagerEvent;
import org.eclipse.commands.contexts.IContextManagerListener;
import org.eclipse.commands.misc.NotDefinedException;
import org.eclipse.jface.contexts.IContextIds;
import org.eclipse.jface.util.Util;
import org.eclipse.swt.SWT;

/**
 * <p>
 * A central repository for bindings -- both in the defined and undefined
 * states. Schemes and bindings can be created and retrieved using this manager.
 * It is possible to listen to changes in the collection of schemes and bindings
 * by attaching a listener to the manager.
 * </p>
 * 
 * @since 3.1
 */
public final class BindingManager implements IContextManagerListener,
        ISchemeListener {

    /**
     * This flag can be set to <code>true</code> if the binding manager should
     * print information to <code>System.out</code> when certain boundary
     * conditions occur.
     */
    public static boolean DEBUG = false;

    /**
     * The separator character used in locales.
     */
    private static final String LOCALE_SEPARATOR = "_"; //$NON-NLS-1$

    /**
     * Takes a fully-specified string, and converts it into an array of
     * increasingly less-specific strings. So, for example, "en_GB" would become
     * ["en_GB", "en", "", null].
     * 
     * @param string
     *            The string to break apart into its less specific components;
     *            should not be <code>null</code>.
     * @param separator
     *            The separator that indicates a separation between a degrees of
     *            specificity; should not be <code>null</code>.
     * @return An array of strings from the most specific (i.e.,
     *         <code>string</code>) to the least specific (i.e.,
     *         <code>null</code>).
     */
    private static final String[] expand(String string, final String separator) {
        // Test for boundary conditions.
        if (string == null || separator == null) {
            return new String[0];
        }

        final List strings = new ArrayList();
        final StringBuffer stringBuffer = new StringBuffer();
        string = string.trim(); // remove whitespace
        if (string.length() > 0) {
            final StringTokenizer stringTokenizer = new StringTokenizer(string,
                    separator);
            while (stringTokenizer.hasMoreElements()) {
                if (stringBuffer.length() > 0)
                    stringBuffer.append(separator);
                stringBuffer.append(((String) stringTokenizer.nextElement())
                        .trim());
                strings.add(stringBuffer.toString());
            }
        }
        Collections.reverse(strings);
        strings.add(Util.ZERO_LENGTH_STRING);
        strings.add(null);
        return (String[]) strings.toArray(new String[strings.size()]);
    }

    /**
     * The active bindings. This is a map of tirggers (
     * <code>TriggerSequence</code>) to command ids (<code>String</code>).
     */
    private Map activeBindings = null;

    /**
     * The scheme that is currently active. An active scheme is the one that is
     * currently dictating which bindings will actually work. This value may be
     * <code>null</code> if there is no active scheme.
     */
    private Scheme activeScheme = null;

    /**
     * The set of all bindings currently handled by this manager. This value may
     * be <code>null</code> if there are no bindings.
     */
    private Set bindings = null;

    /**
     * A cache of the bindings previously computed by this manager. This value
     * may be empty, but it is never <code>null</code>. This is a map of
     * <code>CachedBindingSet</code> to <code>CachedBindingSet</code>.
     */
    private Map cachedBindings = new HashMap();

    /**
     * The context manager for this binding manager. For a binding manager to
     * function, it needs to listen for changes to the contexts. This value is
     * guaranteed to never be <code>null</code>.
     */
    private final ContextManager contextManager;

    /**
     * The set of all identifiers for schemes that are defined. This value may
     * be empty, but is never <code>null</code>.
     */
    private final Set definedSchemeIds = new HashSet();

    /**
     * The collection of listener to this binding manager. This collection is
     * <code>null</code> if there are no listeners.
     */
    private Collection listeners = null;

    /**
     * The locale for this manager. This defaults to the current locale. The
     * value will never be <code>null</code>.
     */
    private String locale = Locale.getDefault().toString();

    /**
     * The array of locales, starting with the active locale and moving up
     * through less specific representations of the locale. For example,
     * ["en_US", "en", "", null].
     */
    private String[] locales = expand(locale, LOCALE_SEPARATOR);

    /**
     * The platform for this manager. This defaults to the current platform. The
     * value will never be <code>null</code>.
     */
    private String platform = SWT.getPlatform();

    /**
     * The array of platforms, starting with the active platform and moving up
     * through less specific representations of the platform. For example,
     * ["gtk", "", null].
     */
    private String[] platforms = expand(platform, Util.ZERO_LENGTH_STRING);

    /**
     * The array of scheme identifiers, starting with the active scheme and
     * moving up through its parents.
     */
    private String[] schemeIds = null;

    /**
     * The map of scheme identifiers (<code>String</code>) to scheme (
     * <code>Scheme</code>). This value may be empty, but is never
     * <code>null</code>.
     */
    private final Map schemesById = new HashMap();

    /**
     * Constructs a new instance of <code>BindingManager</code>.
     * 
     * @param contextManager
     *            The context manager that will support this binding manager.
     *            This value must not be <code>null</code>.
     */
    public BindingManager(final ContextManager contextManager) {
        if (contextManager == null) {
            throw new NullPointerException(
                    "A binding manager requires a context manager"); //$NON-NLS-1$
        }

        this.contextManager = contextManager;
        contextManager.addContextManagerListener(this);
    }

    /**
     * Adds a listener to this binding manager. The listener will be notified
     * when the set of defined schemes or bindings changes. This can be used to
     * track the global appearance and disappearance of bindings.
     * 
     * @param listener
     *            The listener to attach; must not be <code>null</code>.
     */
    public final void addBindingManagerListener(
            final IBindingManagerListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        }

        if (listeners == null) {
            listeners = new HashSet();
        }

        listeners.add(listener);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.commands.contexts.IContextManagerListener#contextManagerChanged(org.eclipse.commands.contexts.ContextManagerEvent)
     */
    public final void contextManagerChanged(
            final ContextManagerEvent contextManagerEvent) {
        if (contextManagerEvent.haveActiveContextsChanged()) {
            recomputeBindings();
        }
    }

    /**
     * Creates a tree of context identifiers, representing the hierarchical
     * structure of the given contexts. The tree is structured as a mapping from
     * child to parent.
     * 
     * @param contextIds
     *            The set of context identifiers to be converted into a tree;
     *            must not be <code>null</code>.
     * @return The tree of contexts to use; may be empty, but never
     *         <code>null</code>. The keys and values are both strings.
     */
    private final Map createContextTreeFor(final Set contextIds) {
        final Map contextTree = new HashMap();

        final Iterator contextIdItr = contextIds.iterator();
        while (contextIdItr.hasNext()) {
            String childContextId = (String) contextIdItr.next();
            while (childContextId != null) {
                final Context childContext = contextManager
                        .getContext(childContextId);

                try {
                    final String parentContextId = childContext.getParentId();
                    contextTree.put(childContextId, parentContextId);
                    childContextId = parentContextId;
                } catch (final NotDefinedException e) {
                    break; // stop ascending
                }
            }
        }

        return contextTree;
    }

    /**
     * <p>
     * Creates a tree of context identifiers, representing the hierarchical
     * structure of the given contexts. The tree is structured as a mapping from
     * child to parent. In this tree, the key binding specific filtering of
     * contexts will have taken place.
     * </p>
     * <p>
     * This method is intended for internal use only.
     * </p>
     * 
     * @param contextIds
     *            The set of context identifiers to be converted into a tree;
     *            must not be <code>null</code>.
     * @return The tree of contexts to use; may be empty, but never
     *         <code>null</code>. The keys and values are both strings.
     */
    private final Map createFilteredContextTreeFor(final Set contextIds) {
        // Check to see whether a dialog or window is active.
        boolean dialog = false;
        boolean window = false;
        Iterator contextIdItr = contextIds.iterator();
        while (contextIdItr.hasNext()) {
            final String contextId = (String) contextIdItr.next();
            if (IContextIds.CONTEXT_ID_DIALOG.equals(contextId)) {
                dialog = true;
                continue;
            }
            if (IContextIds.CONTEXT_ID_WINDOW.equals(contextId)) {
                window = true;
                continue;
            }
        }

        /*
         * Remove all context identifiers for contexts whose parents are dialog
         * or window, and the corresponding dialog or window context is not
         * active.
         */
        try {
            contextIdItr = contextIds.iterator();
            while (contextIdItr.hasNext()) {
                String contextId = (String) contextIdItr.next();
                Context context = contextManager.getContext(contextId);
                String parentId = context.getParentId();
                while (parentId != null) {
                    if (IContextIds.CONTEXT_ID_DIALOG.equals(parentId)) {
                        if (!dialog) {
                            contextIdItr.remove();
                        }
                        break;
                    }
                    if (IContextIds.CONTEXT_ID_WINDOW.equals(parentId)) {
                        if (!window) {
                            contextIdItr.remove();
                        }
                        break;
                    }
                    if (IContextIds.CONTEXT_ID_DIALOG_AND_WINDOW
                            .equals(parentId)) {
                        if ((!window) && (!dialog)) {
                            contextIdItr.remove();
                        }
                        break;
                    }

                    context = contextManager.getContext(parentId);
                    parentId = context.getParentId();
                }
            }
        } catch (NotDefinedException e) {
            if (DEBUG) {
                System.out.println("CONTEXTS >>> NotDefinedException('" //$NON-NLS-1$
                        + e.getMessage()
                        + "') while filtering dialog/window contexts"); //$NON-NLS-1$
            }
        }

        return createContextTreeFor(contextIds);
    }

    /**
     * Notifies all of the listeners to this manager that the defined or active
     * schemes of bindings have changed.
     * 
     * @param event
     *            The event to send to all of the listeners; must not be
     *            <code>null</code>.
     */
    private final void fireBindingManagerChanged(final BindingManagerEvent event) {
        if (event == null)
            throw new NullPointerException();

        if (listeners != null) {
            final Iterator listenerItr = listeners.iterator();
            while (listenerItr.hasNext()) {
                final IBindingManagerListener listener = (IBindingManagerListener) listenerItr
                        .next();
                listener.bindingManagerChanged(event);
            }
        }
    }

    /**
     * Returns the active bindings.
     * 
     * @return The map of triggers (<code>TriggerSequence</code>) to command
     *         ids (<code>String</code>) which are currently active. This
     *         value will not be <code>null</code>, but may be empty.
     */
    public final Map getActiveBindings() {
        return activeBindings;
    }

    /**
     * Gets the currently active scheme.
     * 
     * @return The active scheme; may be <code>null</code> if there is no
     *         active scheme.
     */
    public final Scheme getActiveScheme() {
        return activeScheme;
    }

    /**
     * Returns the set of identifiers for those schemes that are defined.
     * 
     * @return The set of defined scheme identifiers; this value may be empty,
     *         but it is never <code>null</code>.
     */
    public final Set getDefinedSchemeIds() {
        return Collections.unmodifiableSet(definedSchemeIds);
    }

    /**
     * Returns the active locale for this binding manager. The locale is in the
     * same format as <code>Locale.getDefault().toString()</code>.
     * 
     * @return The active locale; never <code>null</code>.
     */
    public final String getLocale() {
        return locale;
    }

    /**
     * Returns all of the possible bindings that start with the given trigger
     * (but are not equal to the given trigger).
     * 
     * @param trigger
     *            The prefix to look for; must not be <code>null</code>.
     * @return A map of triggers (<code>TriggerSequence</code>) to command
     *         identifier (<code>String</code>). This map may be empty, but
     *         it is never <code>null</code>.
     */
    public final Map getPartialMatches(final TriggerSequence trigger) {
        if (activeBindings == null) {
            recomputeBindings();
        }

        final Map partialMatches = new HashMap();
        final Iterator bindingItr = activeBindings.entrySet().iterator();
        while (bindingItr.hasNext()) {
            final Map.Entry entry = (Map.Entry) bindingItr.next();
            final TriggerSequence triggerSequence = (TriggerSequence) entry
                    .getKey();
            if (triggerSequence.startsWith(trigger, false)) {
                partialMatches.put(triggerSequence, entry.getValue());
            }
        }

        return partialMatches;
    }

    /**
     * Returns the command identifier for the active binding matching this
     * trigger, if any.
     * 
     * @param trigger
     *            The trigger to match; may be <code>null</code>.
     * @return The command identifier that matches, if any; <code>null</code>
     *         otherwise.
     */
    public final String getPerfectMatch(final TriggerSequence trigger) {
        if (activeBindings == null) {
            recomputeBindings();
        }

        return (String) activeBindings.get(trigger);
    }

    /**
     * Returns the active platform for this binding manager. The platform is in
     * the same format as <code>SWT.getPlatform()</code>.
     * 
     * @return The active platform; never <code>null</code>.
     */
    public final String getPlatform() {
        return platform;
    }

    /**
     * Gets the scheme with the given identifier. If the scheme does not already
     * exist, then a new (undefined) scheme is created with that identifier.
     * This guarantees that schemes will remain unique.
     * 
     * @param identifier
     *            The identifier for the scheme to retrieve; must not be
     *            <code>null</code>.
     * @return A scheme with the given identifier.
     */
    public final Scheme getScheme(final String identifier) {
        if (identifier == null) {
            throw new NullPointerException(
                    "Cannot get a scheme with a null identifier"); //$NON-NLS-1$
        }

        Scheme scheme = (Scheme) schemesById.get(identifier);
        if (scheme == null) {
            scheme = new Scheme(identifier);
            schemesById.put(identifier, scheme);
            scheme.addSchemeListener(this);
        }

        return scheme;
    }

    /**
     * Ascends all of the parents of the scheme until no more parents are found.
     * 
     * @param schemeId
     *            The id of the scheme for which the parents should be found;
     *            may be <code>null</code>.
     * @return The array of scheme ids (<code>String</code>) starting with
     *         <code>schemeId</code> and then ascending through its ancestors.
     */
    private final String[] getSchemeIds(String schemeId) {
        final List strings = new ArrayList();
        while (schemeId != null) {
            strings.add(schemeId);
            try {
                schemeId = getScheme(schemeId).getParentId();
            } catch (final NotDefinedException e) {
                return new String[0];
            }
        }

        return (String[]) strings.toArray(new String[strings.size()]);
    }

    /**
     * Returns whether the given trigger sequence is a partial match for the
     * given sequence.
     * 
     * @param trigger
     *            The sequence which should be the prefix for some binding;
     *            should not be <code>null</code>.
     * @return <code>true</code> if the trigger can be found in the active
     *         bindings; <code>false</code> otherwise.
     */
    public final boolean isPartialMatch(final TriggerSequence trigger) {
        if (activeBindings == null) {
            recomputeBindings();
        }

        final Iterator bindingItr = activeBindings.entrySet().iterator();
        while (bindingItr.hasNext()) {
            final Map.Entry entry = (Map.Entry) bindingItr.next();
            final TriggerSequence triggerSequence = (TriggerSequence) entry
                    .getKey();
            if (triggerSequence.startsWith(trigger, false)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns whether the given trigger sequence is a perfect match for the
     * given sequence.
     * 
     * @param trigger
     *            The sequence which should match exactly; should not be
     *            <code>null</code>.
     * @return <code>true</code> if the trigger can be found in the active
     *         bindings; <code>false</code> otherwise.
     */
    public final boolean isPerfectMatch(final TriggerSequence trigger) {
        if (activeBindings == null) {
            recomputeBindings();
        }

        return activeBindings.containsKey(trigger);
    }

    /**
     * This recomputes the bindings based on changes to the state of the world.
     * This computation can be triggered by changes to contexts, the active
     * scheme, the locale, or the platform. This method tries to use the cache
     * of pre-computed bindings, if possible. When this method completes,
     * <code>activeBindings</code> will be set to the current set of bindings
     * and <code>cachedBindings</code> will contain an instance of
     * <code>CachedBindingSet</code> representing these bindings.
     */
    private final void recomputeBindings() {
        if (bindings == null) {
            // Not yet initialized. This is happening too early. Do nothing.
            return;
        }

        // Figure out the current state.
        final Set activeContextIds = contextManager.getActiveContextIds();
        final Map activeContextTree = createFilteredContextTreeFor(activeContextIds);

        // Build a cached binding set for that state.
        final CachedBindingSet bindingCache = new CachedBindingSet(
                activeContextTree, locales, platforms, schemeIds);

        /*
         * Check if the cached binding set already exists. If so, simply set the
         * active bindings and return.
         */
        CachedBindingSet existingCache = (CachedBindingSet) cachedBindings
                .get(bindingCache);
        if (existingCache == null) {
            existingCache = bindingCache;
            cachedBindings.put(existingCache, existingCache);
        }
        Map commandIdsByTrigger = existingCache.getCommandIdsByTrigger();
        if (commandIdsByTrigger != null) {
            activeBindings = commandIdsByTrigger;
            return;
        }

        // Compute the active bindings.
        commandIdsByTrigger = new HashMap();
        recomputeBindings(activeContextTree, commandIdsByTrigger);
        existingCache.setCommandIdsByTrigger(commandIdsByTrigger);
        activeBindings = commandIdsByTrigger;
    }

    /**
     * Computes the bindings given the context tree, and inserts them into the
     * <code>commandIdsByTrigger</code>. It is assumed that
     * <code>locales</code>,<code>platforsm</code> and
     * <code>schemeIds</code> correctly reflect the state of the application.
     * This method does not deal with caching.
     * 
     * @param activeContextTree
     *            The map representing the tree of active contexts. The map is
     *            one of child to parent, each being a context id (
     *            <code>String</code>). The keys are never <code>null</code>,
     *            but the values may be (i.e., no parent). This map may be
     *            empty, but it can never be <code>null</code>.
     * @param commandIdsByTrigger
     *            The empty of map that is intended to be filled with triggers (
     *            <code>TriggerSequence</code>) to command identifiers (
     *            <code>String</code>). This value must not be
     *            <code>null</code> and must be empty.
     */
    private final void recomputeBindings(final Map activeContextTree,
            final Map commandIdsByTrigger) {
        /*
         * FIRST PASS: Just throw in bindings that match the current state. If
         * there is more than one match for a binding, then create a list.
         */
        final Map possibleBindings = new HashMap();
        final Iterator bindingItr = bindings.iterator();
        while (bindingItr.hasNext()) {
            final Binding binding = (Binding) bindingItr.next();
            boolean found;

            // Check the context.
            final String contextId = binding.getContextId();
            if (!activeContextTree.containsKey(contextId)) {
                continue;
            }

            // Check the locale.
            final String locale = binding.getLocale();
            found = false;
            for (int i = 0; i < locales.length; i++) {
                if (Util.equals(locale, locales[i])) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                continue;
            }

            // Check the platform.
            final String platform = binding.getPlatform();
            found = false;
            for (int i = 0; i < platforms.length; i++) {
                if (Util.equals(platform, platforms[i])) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                continue;
            }

            // Check the scheme ids.
            final String schemeId = binding.getSchemeId();
            found = false;
            for (int i = 0; i < schemeIds.length; i++) {
                if (Util.equals(schemeId, schemeIds[i])) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                continue;
            }

            // Insert the match into the list of possible matches.
            final TriggerSequence trigger = binding.getTriggerSequence();
            final String commandId = binding.getCommandId();
            final Object existingMatch = possibleBindings.get(trigger);
            if (existingMatch instanceof String) {
                possibleBindings.remove(trigger);
                final SortedSet matches = new TreeSet(new BindingComparator());
                matches.add(existingMatch);
                matches.add(commandId);
                possibleBindings.put(trigger, matches);

            } else if (existingMatch instanceof SortedSet) {
                final SortedSet matches = (SortedSet) existingMatch;
                matches.add(commandId);

            } else {
                possibleBindings.put(trigger, commandId);
            }
        }

        /*
         * SECOND PASS: We now know that all bindings in possibleBindings match
         * the current state.
         * 
         * There is an ordering in which the search is applied. This ordering is
         * the priority certain properties are given within the binding. The
         * order for bindings is scheme, context, type, platform, and locale.
         * 
         * There are some some special cases that need mentioning. First of all,
         * there is a linear ordering to schemes, types, platforms and locales.
         * This is not the case for contexts. It is possible for two active
         * contexts to be siblings or even completely even completely unrelated.
         * So, if an inheritance relationship is defined, then a conflict can be
         * resolved. If two bindings belong to contexts who are not
         * ancestors/descendents of each other, then a conflict arises.
         * 
         * The second thing to consider is that it is possible to unbind
         * something. An unbinding is identified by a null command identifier.
         * An unbinding has to match on almost other property -- including the
         * trigger, but excluding the type. The trigger needs to be included so
         * that we know how to match (otherwise all bindings in a particular
         * context, a particular platform and a particular locale would be
         * removed). The type needs to be excluded so that the user can override
         * system bindings.
         */
        final Iterator possibleBindingItr = possibleBindings.entrySet()
                .iterator();
        int singleMatchCount = 0;
        int multipleMatchCount = 0;
        while (possibleBindingItr.hasNext()) {
            final Map.Entry entry = (Map.Entry) possibleBindingItr.next();
            final TriggerSequence trigger = (TriggerSequence) entry.getKey();
            final Object match = entry.getValue();
            if (match instanceof String) {
                commandIdsByTrigger.put(trigger, match);
                singleMatchCount++;

            } else if (match instanceof SortedSet) {
                final SortedSet matches = (SortedSet) match;
                commandIdsByTrigger.put(trigger, matches.first());
            }
        }

        if (DEBUG) {
            System.out.println("Single matches = " + singleMatchCount); //$NON-NLS-1$
            System.out.println("Multiple matches = " + multipleMatchCount); //$NON-NLS-1$
        }
    }

    /**
     * Removes a listener from this binding manager.
     * 
     * @param listener
     *            The listener to be removed; must not be <code>null</code>.
     */
    public final void removeBindingManagerListener(
            final IBindingManagerListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        }

        if (listeners == null) {
            return;
        }

        listeners.remove(listener);

        if (listeners.isEmpty()) {
            listeners = null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.bindings.ISchemeListener#schemeChanged(org.eclipse.jface.bindings.SchemeEvent)
     */
    public final void schemeChanged(final SchemeEvent schemeEvent) {
        if (schemeEvent.hasDefinedChanged()) {
            final Scheme scheme = schemeEvent.getScheme();

            final String schemeId = scheme.getId();
            final boolean schemeIdAdded = scheme.isDefined();
            boolean activeSchemeChanged = false;
            if (schemeIdAdded) {
                definedSchemeIds.add(schemeId);
            } else {
                definedSchemeIds.remove(schemeId);
                if (activeScheme == scheme) {
                    activeScheme = null;
                    activeSchemeChanged = true;
                    recomputeBindings();
                }
            }

            fireBindingManagerChanged(new BindingManagerEvent(this,
                    activeSchemeChanged, schemeId, schemeIdAdded,
                    !schemeIdAdded));
        }
    }

    /**
     * Selects one of the schemes as the active scheme. This scheme must be
     * defined.
     * 
     * @param schemeId
     *            The scheme to become active; must not be <code>null</code>.
     * @throws NotDefinedException
     *             If the given scheme is currently undefined.
     */
    public final void setActiveScheme(final String schemeId)
            throws NotDefinedException {
        if (schemeId == null) {
            throw new NullPointerException("Cannot activate a null scheme"); //$NON-NLS-1$
        }

        final Scheme scheme = (Scheme) schemesById.get(schemeId);
        if ((scheme == null) || (!scheme.isDefined())) {
            throw new NotDefinedException("Cannot activate an undefined scheme"); //$NON-NLS-1$
        }

        if (Util.equals(activeScheme, scheme)) {
            return;
        }

        activeScheme = scheme;
        schemeIds = getSchemeIds(activeScheme.getId());
        recomputeBindings();
        fireBindingManagerChanged(new BindingManagerEvent(this, true, null,
                false, false));
    }

    /**
     * Changes the set of bindings for this binding manager. The whole set is
     * required so that internal consistency can be maintained and so that
     * excessive recomputations do nothing occur.
     * 
     * @param bindings
     *            The new set of bindings; may be <code>null</code>.
     */
    public final void setBindings(final Set bindings) {
        if (Util.equals(this.bindings, bindings)) {
            return;
        }

        if (bindings != null) {
            this.bindings = Collections.unmodifiableSet(bindings);
        } else {
            this.bindings = null;
        }

        activeBindings = null;
        cachedBindings.clear();
        recomputeBindings();

        fireBindingManagerChanged(new BindingManagerEvent(this, false, null,
                false, false));
    }

    /**
     * Changes the locale for this binding manager. The locale can be used to
     * provide locale-specific bindings. If the locale is different than the
     * current locale, then this will force a recomputation of the bindings. The
     * locale is in the same format as
     * <code>Locale.getDefault().toString()</code>.
     * 
     * @param locale
     *            The new locale; must not be <code>null</code>.
     * @see Locale#getDefault()
     */
    public final void setLocale(final String locale) {
        if (locale == null) {
            throw new NullPointerException("The locale cannot be null"); //$NON-NLS-1$
        }

        if (!Util.equals(this.locale, locale)) {
            this.locale = locale;
            this.locales = expand(locale, LOCALE_SEPARATOR);
            recomputeBindings();
        }
    }

    /**
     * Changes the platform for this binding manager. The platform can be used
     * to provide platform-specific bindings. If the platform is different than
     * the current platform, then this will force a recomputation of the
     * bindings. The locale is in the same format as
     * <code>SWT.getPlatform()</code>.
     * 
     * @param platform
     *            The new platform; must not be <code>null</code>.
     * @see org.eclipse.swt.SWT#getPlatform()
     */
    public final void setPlatform(final String platform) {
        if (platform == null) {
            throw new NullPointerException("The platform cannot be null"); //$NON-NLS-1$
        }

        if (!Util.equals(this.platform, platform)) {
            this.platform = platform;
            this.platforms = expand(platform, Util.ZERO_LENGTH_STRING);
            recomputeBindings();
        }
    }
}
