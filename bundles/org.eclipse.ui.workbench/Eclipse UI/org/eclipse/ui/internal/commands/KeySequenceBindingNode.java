/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.keys.KeySequence;
import org.eclipse.ui.keys.KeyStroke;

final class KeySequenceBindingNode {

    final static class Assignment implements Comparable {
        boolean hasPluginCommandIdInFirstKeyConfiguration;

        boolean hasPluginCommandIdInInheritedKeyConfiguration;

        boolean hasPreferenceCommandIdInFirstKeyConfiguration;

        boolean hasPreferenceCommandIdInInheritedKeyConfiguration;

        String pluginCommandIdInFirstKeyConfiguration;

        String pluginCommandIdInInheritedKeyConfiguration;

        String preferenceCommandIdInFirstKeyConfiguration;

        String preferenceCommandIdInInheritedKeyConfiguration;

        public int compareTo(Object object) {
            Assignment castedObject = (Assignment) object;
            int compareTo = hasPreferenceCommandIdInFirstKeyConfiguration == false ? (castedObject.hasPreferenceCommandIdInFirstKeyConfiguration == true ? -1
                    : 0)
                    : 1;

            if (compareTo == 0) {
                compareTo = hasPreferenceCommandIdInInheritedKeyConfiguration == false ? (castedObject.hasPreferenceCommandIdInInheritedKeyConfiguration == true ? -1
                        : 0)
                        : 1;

                if (compareTo == 0) {
                    compareTo = hasPluginCommandIdInFirstKeyConfiguration == false ? (castedObject.hasPluginCommandIdInFirstKeyConfiguration == true ? -1
                            : 0)
                            : 1;

                    if (compareTo == 0) {
                        compareTo = hasPluginCommandIdInInheritedKeyConfiguration == false ? (castedObject.hasPluginCommandIdInInheritedKeyConfiguration == true ? -1
                                : 0)
                                : 1;

                        if (compareTo == 0) {
                            compareTo = Util
                                    .compare(
                                            preferenceCommandIdInFirstKeyConfiguration,
                                            castedObject.preferenceCommandIdInFirstKeyConfiguration);

                            if (compareTo == 0) {
                                compareTo = Util
                                        .compare(
                                                preferenceCommandIdInInheritedKeyConfiguration,
                                                castedObject.preferenceCommandIdInInheritedKeyConfiguration);

                                if (compareTo == 0) {
                                    compareTo = Util
                                            .compare(
                                                    pluginCommandIdInFirstKeyConfiguration,
                                                    castedObject.pluginCommandIdInFirstKeyConfiguration);

                                    if (compareTo == 0)
                                        compareTo = Util
                                                .compare(
                                                        pluginCommandIdInInheritedKeyConfiguration,
                                                        castedObject.pluginCommandIdInInheritedKeyConfiguration);
                                }
                            }
                        }
                    }
                }
            }

            return compareTo;
        }

        boolean contains(String commandId) {
            return Util.equals(commandId,
                    preferenceCommandIdInFirstKeyConfiguration)
                    || Util.equals(commandId,
                            preferenceCommandIdInInheritedKeyConfiguration)
                    || Util.equals(commandId,
                            pluginCommandIdInFirstKeyConfiguration)
                    || Util.equals(commandId,
                            pluginCommandIdInInheritedKeyConfiguration);
        }

        public boolean equals(Object object) {
            if (!(object instanceof Assignment))
                return false;

            Assignment castedObject = (Assignment) object;
            boolean equals = true;
            equals &= hasPreferenceCommandIdInFirstKeyConfiguration == castedObject.hasPreferenceCommandIdInFirstKeyConfiguration;
            equals &= hasPreferenceCommandIdInInheritedKeyConfiguration == castedObject.hasPreferenceCommandIdInInheritedKeyConfiguration;
            equals &= hasPluginCommandIdInFirstKeyConfiguration == castedObject.hasPluginCommandIdInFirstKeyConfiguration;
            equals &= hasPluginCommandIdInInheritedKeyConfiguration == castedObject.hasPluginCommandIdInInheritedKeyConfiguration;
            equals &= preferenceCommandIdInFirstKeyConfiguration == castedObject.preferenceCommandIdInFirstKeyConfiguration;
            equals &= preferenceCommandIdInInheritedKeyConfiguration == castedObject.preferenceCommandIdInInheritedKeyConfiguration;
            equals &= pluginCommandIdInFirstKeyConfiguration == castedObject.pluginCommandIdInFirstKeyConfiguration;
            equals &= pluginCommandIdInInheritedKeyConfiguration == castedObject.pluginCommandIdInInheritedKeyConfiguration;
            return equals;
        }
    }

    static void add(Map keyStrokeNodeByKeyStrokeMap, KeySequence keySequence,
            String contextId, String keyConfigurationId, int rank,
            String platform, String locale, String commandId) {
        List keyStrokes = keySequence.getKeyStrokes();
        Map root = keyStrokeNodeByKeyStrokeMap;
        KeySequenceBindingNode keySequenceBindingNode = null;

        for (int i = 0; i < keyStrokes.size(); i++) {
            KeyStroke keyStroke = (KeyStroke) keyStrokes.get(i);
            keySequenceBindingNode = (KeySequenceBindingNode) root
                    .get(keyStroke);

            if (keySequenceBindingNode == null) {
                keySequenceBindingNode = new KeySequenceBindingNode();
                root.put(keyStroke, keySequenceBindingNode);
            }

            root = keySequenceBindingNode.childKeyStrokeNodeByKeyStrokeMap;
        }

        if (keySequenceBindingNode != null)
            keySequenceBindingNode.add(contextId, keyConfigurationId, rank,
                    platform, locale, commandId);
    }

    static Map find(Map keyStrokeNodeByKeyStrokeMap, KeySequence keySequence) {
        Iterator iterator = keySequence.getKeyStrokes().iterator();
        KeySequenceBindingNode keySequenceBindingNode = null;

        while (iterator.hasNext()) {
            keySequenceBindingNode = (KeySequenceBindingNode) keyStrokeNodeByKeyStrokeMap
                    .get(iterator.next());

            if (keySequenceBindingNode == null)
                return null;

            keyStrokeNodeByKeyStrokeMap = keySequenceBindingNode.childKeyStrokeNodeByKeyStrokeMap;
        }

        return keyStrokeNodeByKeyStrokeMap;
    }

    static Map getAssignmentsByContextIdKeySequence(
            Map keyStrokeNodeByKeyStrokeMap, KeySequence prefix) {
        Map assignmentsByContextIdByKeySequence = new HashMap();
        Iterator iterator = keyStrokeNodeByKeyStrokeMap.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            KeyStroke keyStroke = (KeyStroke) entry.getKey();
            KeySequenceBindingNode keySequenceBindingNode = (KeySequenceBindingNode) entry
                    .getValue();
            List keyStrokes = new ArrayList(prefix.getKeyStrokes());
            keyStrokes.add(keyStroke);
            KeySequence keySequence = KeySequence.getInstance(keyStrokes);
            Map childAssignmentsByContextIdByKeySequence = getAssignmentsByContextIdKeySequence(
                    keySequenceBindingNode.childKeyStrokeNodeByKeyStrokeMap,
                    keySequence);

            if (childAssignmentsByContextIdByKeySequence.size() >= 1)
                assignmentsByContextIdByKeySequence
                        .putAll(childAssignmentsByContextIdByKeySequence);

            assignmentsByContextIdByKeySequence.put(keySequence,
                    keySequenceBindingNode.assignmentsByContextId);
        }

        return assignmentsByContextIdByKeySequence;
    }

    static void getKeySequenceBindingDefinitions(
            Map keyStrokeNodeByKeyStrokeMap, KeySequence prefix, int rank,
            List keySequenceBindingDefinitions) {
        Iterator iterator = keyStrokeNodeByKeyStrokeMap.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            KeyStroke keyStroke = (KeyStroke) entry.getKey();
            KeySequenceBindingNode keySequenceBindingNode = (KeySequenceBindingNode) entry
                    .getValue();
            List keyStrokes = new ArrayList(prefix.getKeyStrokes());
            keyStrokes.add(keyStroke);
            KeySequence keySequence = KeySequence.getInstance(keyStrokes);
            Map contextMap = keySequenceBindingNode.contextMap;
            Iterator iterator2 = contextMap.entrySet().iterator();

            while (iterator2.hasNext()) {
                Map.Entry entry2 = (Map.Entry) iterator2.next();
                String contextId = (String) entry2.getKey();
                Map keyConfigurationMap = (Map) entry2.getValue();
                Iterator iterator3 = keyConfigurationMap.entrySet().iterator();

                while (iterator3.hasNext()) {
                    Map.Entry entry3 = (Map.Entry) iterator3.next();
                    String keyConfigurationId = (String) entry3.getKey();
                    Map rankMap = (Map) entry3.getValue();
                    Map platformMap = (Map) rankMap.get(new Integer(rank));

                    if (platformMap != null) {
                        Iterator iterator4 = platformMap.entrySet().iterator();

                        while (iterator4.hasNext()) {
                            Map.Entry entry4 = (Map.Entry) iterator4.next();
                            String platform = (String) entry4.getKey();
                            Map localeMap = (Map) entry4.getValue();
                            Iterator iterator5 = localeMap.entrySet()
                                    .iterator();

                            while (iterator5.hasNext()) {
                                Map.Entry entry5 = (Map.Entry) iterator5.next();
                                String locale = (String) entry5.getKey();
                                Set commandIds = (Set) entry5.getValue();
                                Iterator iterator6 = commandIds.iterator();

                                while (iterator6.hasNext()) {
                                    String commandId = (String) iterator6
                                            .next();
                                    keySequenceBindingDefinitions
                                            .add(new KeySequenceBindingDefinition(
                                                    contextId, commandId,
                                                    keyConfigurationId,
                                                    keySequence, locale,
                                                    platform, null));
                                }
                            }
                        }
                    }
                }
            }

            getKeySequenceBindingDefinitions(
                    keySequenceBindingNode.childKeyStrokeNodeByKeyStrokeMap,
                    keySequence, rank, keySequenceBindingDefinitions);
        }
    }

    static Map getKeySequenceBindingsByCommandId(Map keySequenceMap) {
        Map commandMap = new HashMap();
        Iterator iterator = keySequenceMap.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            KeySequence keySequence = (KeySequence) entry.getKey();
            Match match = (Match) entry.getValue();
            String commandId = match.getCommandId();
            int value = match.getValue();
            SortedSet keySequenceBindings = (SortedSet) commandMap
                    .get(commandId);

            if (keySequenceBindings == null) {
                keySequenceBindings = new TreeSet();
                commandMap.put(commandId, keySequenceBindings);
            }

            keySequenceBindings.add(new KeySequenceBinding(keySequence, value));
        }

        return commandMap;
    }

    static Map getMatchesByKeySequence(Map keyStrokeNodeByKeyStrokeMap,
            KeySequence prefix) {
        Map keySequenceMap = new HashMap();
        Iterator iterator = keyStrokeNodeByKeyStrokeMap.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            KeyStroke keyStroke = (KeyStroke) entry.getKey();
            KeySequenceBindingNode keySequenceBindingNode = (KeySequenceBindingNode) entry
                    .getValue();
            List keyStrokes = new ArrayList(prefix.getKeyStrokes());
            keyStrokes.add(keyStroke);
            KeySequence keySequence = KeySequence.getInstance(keyStrokes);
            Map childMatchesByKeySequence = getMatchesByKeySequence(
                    keySequenceBindingNode.childKeyStrokeNodeByKeyStrokeMap,
                    keySequence);

            if (childMatchesByKeySequence.size() >= 1)
                keySequenceMap.putAll(childMatchesByKeySequence);
            else if (keySequenceBindingNode.match != null
                    && keySequenceBindingNode.match.getCommandId() != null)
                keySequenceMap.put(keySequence, keySequenceBindingNode.match);
        }

        return keySequenceMap;
    }

    static void remove(Map keyStrokeNodeByKeyStrokeMap,
            KeySequence keySequence, String contextId,
            String keyConfigurationId, int rank, String platform, String locale) {
        Iterator iterator = keySequence.getKeyStrokes().iterator();
        KeySequenceBindingNode keySequenceBindingNode = null;

        while (iterator.hasNext()) {
            keySequenceBindingNode = (KeySequenceBindingNode) keyStrokeNodeByKeyStrokeMap
                    .get(iterator.next());

            if (keySequenceBindingNode == null)
                return;

            keyStrokeNodeByKeyStrokeMap = keySequenceBindingNode.childKeyStrokeNodeByKeyStrokeMap;
        }

        keySequenceBindingNode.remove(contextId, keyConfigurationId, rank,
                platform, locale);
    }

    static void remove(Map keyStrokeNodeByKeyStrokeMap,
            KeySequence keySequence, String contextId,
            String keyConfigurationId, int rank, String platform,
            String locale, String commandId) {
        Iterator iterator = keySequence.getKeyStrokes().iterator();
        KeySequenceBindingNode keySequenceBindingNode = null;

        while (iterator.hasNext()) {
            keySequenceBindingNode = (KeySequenceBindingNode) keyStrokeNodeByKeyStrokeMap
                    .get(iterator.next());

            if (keySequenceBindingNode == null)
                return;

            keyStrokeNodeByKeyStrokeMap = keySequenceBindingNode.childKeyStrokeNodeByKeyStrokeMap;
        }

        keySequenceBindingNode.remove(contextId, keyConfigurationId, rank,
                platform, locale, commandId);
    }

    static void solve(Map keyStrokeNodeByKeyStrokeMap,
            String[] keyConfigurationIds, String[] platforms, String[] locales) {
        for (Iterator iterator = keyStrokeNodeByKeyStrokeMap.values()
                .iterator(); iterator.hasNext();) {
            KeySequenceBindingNode keySequenceBindingNode = (KeySequenceBindingNode) iterator
                    .next();
            keySequenceBindingNode.solveAssignmentsByContextId(
                    keyConfigurationIds, platforms, locales);
            solve(keySequenceBindingNode.childKeyStrokeNodeByKeyStrokeMap,
                    keyConfigurationIds, platforms, locales);
        }
    }

    static void solve(Map keyStrokeNodeByKeyStrokeMap, Map contextIds,
            String[] keyConfigurationIds, String[] platforms, String[] locales) {
        for (Iterator iterator = keyStrokeNodeByKeyStrokeMap.values()
                .iterator(); iterator.hasNext();) {
            KeySequenceBindingNode keySequenceBindingNode = (KeySequenceBindingNode) iterator
                    .next();
            keySequenceBindingNode.solveMatch(contextIds, keyConfigurationIds,
                    platforms, locales);
            solve(keySequenceBindingNode.childKeyStrokeNodeByKeyStrokeMap,
                    contextIds, keyConfigurationIds, platforms, locales);
        }
    }

    private Map contextMap = new HashMap();

    private Map assignmentsByContextId = new HashMap();

    private Map childKeyStrokeNodeByKeyStrokeMap = new HashMap();

    private Match match = null;

    private KeySequenceBindingNode() {
    }

    private void add(String contextId, String keyConfigurationId, int rank,
            String platform, String locale, String commandId) {
        Map keyConfigurationMap = (Map) contextMap.get(contextId);

        if (keyConfigurationMap == null) {
            keyConfigurationMap = new HashMap();
            contextMap.put(contextId, keyConfigurationMap);
        }

        Map rankMap = (Map) keyConfigurationMap.get(keyConfigurationId);

        if (rankMap == null) {
            rankMap = new HashMap();
            keyConfigurationMap.put(keyConfigurationId, rankMap);
        }

        Map platformMap = (Map) rankMap.get(new Integer(rank));

        if (platformMap == null) {
            platformMap = new HashMap();
            rankMap.put(new Integer(rank), platformMap);
        }

        Map localeMap = (Map) platformMap.get(platform);

        if (localeMap == null) {
            localeMap = new HashMap();
            platformMap.put(platform, localeMap);
        }

        Set commandIds = (Set) localeMap.get(locale);

        if (commandIds == null) {
            commandIds = new HashSet();
            localeMap.put(locale, commandIds);
        }

        commandIds.add(commandId);
    }

    private void remove(String contextId, String keyConfigurationId, int rank,
            String platform, String locale) {
        Map keyConfigurationMap = (Map) contextMap.get(contextId);

        if (keyConfigurationMap != null) {
            Map rankMap = (Map) keyConfigurationMap.get(keyConfigurationId);

            if (rankMap != null) {
                Map platformMap = (Map) rankMap.get(new Integer(rank));

                if (platformMap != null) {
                    Map localeMap = (Map) platformMap.get(platform);

                    if (localeMap != null) {
                        localeMap.remove(locale);

                        if (localeMap.isEmpty()) {
                            platformMap.remove(platform);

                            if (platformMap.isEmpty()) {
                                rankMap.remove(new Integer(rank));

                                if (rankMap.isEmpty()) {
                                    keyConfigurationMap
                                            .remove(keyConfigurationId);

                                    if (keyConfigurationMap.isEmpty())
                                        contextMap.remove(contextId);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void remove(String contextId, String keyConfigurationId, int rank,
            String platform, String locale, String commandId) {
        Map keyConfigurationMap = (Map) contextMap.get(contextId);

        if (keyConfigurationMap != null) {
            Map rankMap = (Map) keyConfigurationMap.get(keyConfigurationId);

            if (rankMap != null) {
                Map platformMap = (Map) rankMap.get(new Integer(rank));

                if (platformMap != null) {
                    Map localeMap = (Map) platformMap.get(platform);

                    if (localeMap != null) {
                        Set commandIds = (Set) localeMap.get(locale);

                        if (commandIds != null) {
                            commandIds.remove(commandId);

                            if (commandIds.isEmpty()) {
                                localeMap.remove(locale);

                                if (localeMap.isEmpty()) {
                                    platformMap.remove(platform);

                                    if (platformMap.isEmpty()) {
                                        rankMap.remove(new Integer(rank));

                                        if (rankMap.isEmpty()) {
                                            keyConfigurationMap
                                                    .remove(keyConfigurationId);

                                            if (keyConfigurationMap.isEmpty())
                                                contextMap.remove(contextId);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void solveAssignmentsByContextId(String[] keyConfigurationIds,
            String[] platforms, String[] locales) {
        assignmentsByContextId.clear();

        for (Iterator iterator = contextMap.entrySet().iterator(); iterator
                .hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String contextId = (String) entry.getKey();
            Map keyConfigurationMap = (Map) entry.getValue();
            KeySequenceBindingNode.Assignment assignment = null;

            if (keyConfigurationMap != null)
                for (int keyConfiguration = 0; keyConfiguration < keyConfigurationIds.length
                        && keyConfiguration < 0xFF; keyConfiguration++) {
                    Map rankMap = (Map) keyConfigurationMap
                            .get(keyConfigurationIds[keyConfiguration]);

                    if (rankMap != null)
                        for (int rank = 0; rank <= 1; rank++) {
                            Map platformMap = (Map) rankMap.get(new Integer(
                                    rank));

                            if (platformMap != null)
                                for (int platform = 0; platform < platforms.length
                                        && platform < 0xFF; platform++) {
                                    Map localeMap = (Map) platformMap
                                            .get(platforms[platform]);

                                    if (localeMap != null)
                                        for (int locale = 0; locale < locales.length
                                                && locale < 0xFF; locale++) {
                                            Set commandIds = (Set) localeMap
                                                    .get(locales[locale]);

                                            if (commandIds != null) {
                                                String commandId = commandIds
                                                        .size() == 1 ? (String) commandIds
                                                        .iterator().next()
                                                        : null;

                                                if (assignment == null)
                                                    assignment = new Assignment();

                                                switch (rank) {
                                                case 0:
                                                    if (keyConfiguration == 0
                                                            && !assignment.hasPreferenceCommandIdInFirstKeyConfiguration) {
                                                        assignment.hasPreferenceCommandIdInFirstKeyConfiguration = true;
                                                        assignment.preferenceCommandIdInFirstKeyConfiguration = commandId;
                                                    } else if (!assignment.hasPreferenceCommandIdInInheritedKeyConfiguration) {
                                                        assignment.hasPreferenceCommandIdInInheritedKeyConfiguration = true;
                                                        assignment.preferenceCommandIdInInheritedKeyConfiguration = commandId;
                                                    }

                                                    break;

                                                case 1:
                                                    if (keyConfiguration == 0
                                                            && !assignment.hasPluginCommandIdInFirstKeyConfiguration) {
                                                        assignment.hasPluginCommandIdInFirstKeyConfiguration = true;
                                                        assignment.pluginCommandIdInFirstKeyConfiguration = commandId;
                                                    } else if (!assignment.hasPluginCommandIdInInheritedKeyConfiguration) {
                                                        assignment.hasPluginCommandIdInInheritedKeyConfiguration = true;
                                                        assignment.pluginCommandIdInInheritedKeyConfiguration = commandId;
                                                    }

                                                    break;
                                                }
                                            }
                                        }
                                }

                        }

                }

            if (assignment != null)
                assignmentsByContextId.put(contextId, assignment);
        }
    }

    /**
     * <p>
     * Finds a single match for this key binding node, given the state passed in
     * as parameters. The match will contain a single command identifier, and a
     * match value (a rough approximation of how accurate the match may be --
     * lower is better).
     * </p>
     * <p>
     * The matching algorithm checks for a match in each context given -- in the
     * order provided. Similar, it checks the key configuration, the rank (see
     * below), the platform, and the locale -- in the order provided. If a
     * single command identifier (note: this could be <code>null</code>--
     * indicating a removed key binding) matches the given characteristics, then
     * it is added to the list. For each context identifier, there can be at
     * most one match. If there is no match,
     * <em>or the match has a <code>null</code> command identifier</em>,
     * then we will move on to consider the next context.
     * </p>
     * <p>
     * The rank is a special value. It is either <code>0</code> or
     * <code>1</code>. It is used for marking removed key bindings. The
     * removed command identifier is moved to the first rank, and a
     * <code>null</code> command identifier is placed in the zeroth rank. The
     * ranking mechanism is controlled externally, but only the zeroth and first
     * ranks are considered internally.
     * </p>
     * <p>
     * When this method completes, <code>match</code> will contain the best
     * possible match, if any. If no match can be found, then <code>match</code>
     * will be <code>null</code>.
     * </p>
     * <p>
     * TODO Doug's Note (May 29, 2004): This mechanism is insanely complex for
     * what it is doing, and doesn't seem to cover all of the use cases. This
     * would be a good candidate for refactoring in the future. Most of this
     * code was written by Chris McLaren, but I've had to hack in some
     * behavioural changes to accomodate bugs for 3.0. If you're looking for
     * insight as to how it currently works, you might try talking to one of us.
     * Most notably, the fall through mechanism for <code>null</code> command
     * identifiers. My main concerns are: interactions between key
     * configurations, and unbinding a key in a child context so as to bind it
     * in a parent context.
     * </p>
     * 
     * @param contextTree
     *            The tree of contexts to consider. The tree is represented as a
     *            map of child context identifiers to parent context
     *            identifiers. This value must never be <code>null</code>,
     *            though it may contain <code>null</code> parents. It should
     *            never contain <code>null</code> children. It should only
     *            contain strings.
     * @param keyConfigurationIds
     *            The key configuration identifiers in the order they should be
     *            considered. This value must never be <code>null</code>,
     *            though it may contain <code>null</code> values.
     *            <code>null</code> values typically indicate "any".
     * @param platforms
     *            The platform identifiers in the order they should be
     *            considered. This value must never be <code>null</code>,
     *            though it may contain <code>null</code> values.
     *            <code>null</code> values typically indicate "any".
     * @param locales
     *            The locale identifiers in the order they should be considered.
     *            This value must never be <code>null</code>, though it may
     *            contain <code>null</code> values. <code>null</code> values
     *            typically indicate "any".
     */
    private void solveMatch(Map contextTree, String[] keyConfigurationIds,
            String[] platforms, String[] locales) {
        // Clear out the current match.
        match = null;

        // Get an array of context identifiers.
        final String[] contextIds = (String[]) contextTree.keySet().toArray(
                new String[contextTree.size()]);

        // Get the maximum indices to consider.
        final int maxContext = (contextIds.length > 0xFF) ? 0xFF
                : contextIds.length;
        int maxKeyConfiguration = (keyConfigurationIds.length > 0xFF) ? 0xFF
                : keyConfigurationIds.length;
        final int maxPlatform = (platforms.length > 0xFF) ? 0xFF
                : platforms.length;
        final int maxLocale = (locales.length > 0xFF) ? 0xFF : locales.length;

        // Peel apart the nested map looking for matches.
        final Collection contextIdsNotToConsider = new HashSet();
        for (int context = 0; context < maxContext; context++) {
            boolean matchFoundForThisContext = false;

            /*
             * Check to see if the context identifier has been nixed by a child
             * context. That is, has a non-null match been found in a child
             * context?
             */
            final String contextId = contextIds[context];
            if (contextIdsNotToConsider.contains(contextId)) {
                continue;
            }

            final Map keyConfigurationMap = (Map) contextMap.get(contextId);
            if (keyConfigurationMap != null)
                for (int keyConfiguration = 0; keyConfiguration < maxKeyConfiguration
                        && !matchFoundForThisContext; keyConfiguration++) {
                    final Map rankMap = (Map) keyConfigurationMap
                            .get(keyConfigurationIds[keyConfiguration]);

                    if (rankMap != null) {
                        for (int rank = 0; rank <= 1; rank++) {
                            final Map platformMap = (Map) rankMap
                                    .get(new Integer(rank));

                            if (platformMap != null)
                                for (int platform = 0; platform < maxPlatform
                                        && !matchFoundForThisContext; platform++) {
                                    final Map localeMap = (Map) platformMap
                                            .get(platforms[platform]);

                                    if (localeMap != null)
                                        for (int locale = 0; locale < maxLocale
                                                && !matchFoundForThisContext; locale++) {
                                            final Set commandIds = (Set) localeMap
                                                    .get(locales[locale]);

                                            if (commandIds != null) {
                                                /*
                                                 * Jump to the next
                                                 * context.
                                                 */
                                                matchFoundForThisContext = true;

                                                /*
                                                 * Get the command
                                                 * identifier.
                                                 */
                                                final String commandId = commandIds
                                                        .size() == 1 ? (String) commandIds
                                                        .iterator().next()
                                                        : null;

                                                /*
                                                 * Make sure we
                                                 * don't consider
                                                 * any higher key
                                                 * configurations
                                                 * again.
                                                 */
                                                maxKeyConfiguration = keyConfiguration + 1;

                                                /*
                                                 * Make sure we
                                                 * don't consider
                                                 * any parents of
                                                 * this context, if
                                                 * the command
                                                 * identifier is not
                                                 * null.
                                                 */
                                                if (commandId != null) {
                                                    String parentContext = (String) contextTree
                                                            .get(contextId);
                                                    while (parentContext != null) {
                                                        contextIdsNotToConsider
                                                                .add(parentContext);
                                                        parentContext = (String) contextTree
                                                                .get(parentContext);
                                                    }

                                                    /*
                                                     * Check for a
                                                     * conflict.
                                                     */
                                                    if ((match != null)
                                                            && (!contextIdsNotToConsider
                                                                    .contains(contextIds[match
                                                                            .getValue() >> 24]))) {
                                                        WorkbenchPlugin
                                                                .log("Conflicting key binding for '" //$NON-NLS-1$
                                                                        + commandId
                                                                        + "' and '" //$NON-NLS-1$
                                                                        + match
                                                                                .getCommandId()
                                                                        + "'"); //$NON-NLS-1$
                                                        match = null;

                                                    } else {
                                                        match = new Match(
                                                                commandId,
                                                                (context << 24)
                                                                        + (keyConfiguration << 16)
                                                                        + (platform << 8)
                                                                        + locale);
                                                    }
                                                }
                                            }
                                        }
                                }
                        }
                    }
                }
        }
    }
}