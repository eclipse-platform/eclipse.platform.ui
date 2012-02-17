/*******************************************************************************
 * Copyright (c) 2008, 2012 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Wind River Systems - adapted to use with IToggleBreakpiontsTargetFactory extension
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionConverter;
import org.eclipse.core.expressions.ExpressionTagNames;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.internal.core.IConfigurationElementConstants;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTargetFactory;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTargetManager;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTargetManagerListener;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.SimpleMarkerAnnotation;

/**
 * The concrete implementation of the toggle breakpoints target manager 
 * interface. 
 * 
 * @since 3.5
 */
public class ToggleBreakpointsTargetManager implements IToggleBreakpointsTargetManager {

    /**
     * Toggle breakpoints target ID which refers to a target contributed
     * through the legacy adapter mechanism.
     */
    public static String DEFAULT_TOGGLE_TARGET_ID = "default"; //$NON-NLS-1$
    
    private static Set DEFAULT_TOGGLE_TARGET_ID_SET = new TreeSet();
    static {
        DEFAULT_TOGGLE_TARGET_ID_SET.add(DEFAULT_TOGGLE_TARGET_ID);
    }
    
    /**
     * Acts as a proxy between the toggle breakpoints target manager and the factories 
     * contributed to the extension point.  Only loads information from the plug-in XML 
     * and only instantiates the specified factory if required (lazy loading).
     */
    private static class ToggleTargetFactory implements IToggleBreakpointsTargetFactory {
        
        private IConfigurationElement fConfigElement;
        private IToggleBreakpointsTargetFactory fFactory;
        private Expression fEnablementExpression;
        
        public ToggleTargetFactory(IConfigurationElement configElement){
            fConfigElement = configElement;         
        }

        /**
         * @return Returns the instantiated factory specified by the class property. 
         */
        private IToggleBreakpointsTargetFactory getFactory() {
            if (fFactory != null) return fFactory;
            try{
                Object obj = fConfigElement.createExecutableExtension(IConfigurationElementConstants.CLASS);
                if(obj instanceof IToggleBreakpointsTargetFactory) {
                    fFactory = (IToggleBreakpointsTargetFactory)obj;
                } else {
                    throw new CoreException(new Status(IStatus.ERROR, DebugUIPlugin.getUniqueIdentifier(), IDebugUIConstants.INTERNAL_ERROR, "org.eclipse.debug.ui.toggleBreakpointsTargetFactories extension failed to load breakpoint toggle target because the specified class does not implement org.eclipse.debug.ui.actions.IToggleBreakpointsTargetFactory.  Class specified was: " + obj, null)); //$NON-NLS-1$
                }   
            } catch (CoreException e){
                DebugUIPlugin.log(e.getStatus());
                fFactory = null;
            }
            return fFactory;
        }
        
        /**
         * Checks if the enablement expression for the factory evaluates to true for the
         * given part and selection.
         * @param part The active part.
         * @param selection The current selection
         * @return whether the delegated target factory is enabled for given 
         * part and selection.
         */
        public boolean isEnabled(IWorkbenchPart part, ISelection selection) {
            boolean enabled = false;
            Expression expression = getEnablementExpression();
            if (expression != null) {
                enabled = evalEnablementExpression(part, selection, expression);
            } else {
                enabled = true;
            }
            return enabled;
        }
        
        /**
         * Returns the active debug context given the active part.  It is used
         * in creating the evaluation context for the factories' enablement expression. 
         * @param part active part
         * @return current active debug context
         */
        private IStructuredSelection getDebugContext(IWorkbenchPart part) {
            ISelection selection = DebugUITools.getDebugContextManager().
                getContextService(part.getSite().getWorkbenchWindow()).getActiveContext();
            if (selection instanceof IStructuredSelection) {
                return (IStructuredSelection)selection;
            } 
            return StructuredSelection.EMPTY;
        }

        /**
         * Evaluate the given expression within the given context and return
         * the result. Returns <code>true</code> iff result is either TRUE.
         * 
         * @param part the {@link IWorkbenchPart} context
         * @param selection the current selection in the part
         * @param exp the current expression
         * @return the result of evaluating the expression
         */
        private boolean evalEnablementExpression(IWorkbenchPart part, ISelection selection, Expression exp) {
            if (exp != null){
                IEvaluationContext context = DebugUIPlugin.createEvaluationContext(part);
                List debugContextList = getDebugContext(part).toList();
                context.addVariable(IConfigurationElementConstants.DEBUG_CONTEXT, debugContextList); 

                if (selection instanceof IStructuredSelection) {
                    List selectionList = ((IStructuredSelection)selection).toList();
                    context.addVariable(IConfigurationElementConstants.SELECTION, selectionList); 
                }

                if (part instanceof IEditorPart) {
                    context.addVariable(IConfigurationElementConstants.EDITOR_INPUT, ((IEditorPart)part).getEditorInput());
                }
                
                try{
                    EvaluationResult result = exp.evaluate(context);
                    if (result == EvaluationResult.TRUE){
                        return true;
                    }
                } catch (CoreException e){
                    // Evaluation failed
                }
            }
            return false;
        }
        
        /**
         * @return Returns an expression that represents the enablement logic for the
         * breakpiont toggle target.
         */
        private Expression getEnablementExpression(){
            if (fEnablementExpression == null) {
                try{
                    IConfigurationElement[] elements = fConfigElement.getChildren(ExpressionTagNames.ENABLEMENT);
                    IConfigurationElement enablement = elements.length > 0 ? elements[0] : null; 
                    if (enablement != null) {
                        fEnablementExpression = ExpressionConverter.getDefault().perform(enablement);
                    }
                } catch (CoreException e){
                    DebugUIPlugin.log(e.getStatus());
                    fEnablementExpression = null;
                }
            }
            return fEnablementExpression;
        }

        /** 
         * Instantiates the factory and asks it to produce the IToggleBreakpointsTarget
         * for the given ID
         * @param targetID ID to create toggle target for 
         * @return The created toggle target, or null.
         */
        public IToggleBreakpointsTarget createToggleTarget(String targetID) {
            IToggleBreakpointsTargetFactory factory = getFactory();
            if (factory != null) {
                return factory.createToggleTarget(targetID);
            } 
            return null;
        }
        
        /** 
         * Instantiates the factory and asks it for the set of toggle target
         * IDs that the factory can produce for the given part and selection.
         * @param part The active part.
         * @param selection The current selection
         * @return Set of <code>String</code> IDs for possible toggle breakpoint 
         * targets, possibly empty
         */
        public Set getToggleTargets(IWorkbenchPart part, ISelection selection) {
            IToggleBreakpointsTargetFactory factory = getFactory();
            if (factory != null) {
                return factory.getToggleTargets(part, selection);
            } 
            return Collections.EMPTY_SET;
        }

        /** 
         * Instantiates the factory and asks it to produce the name of the toggle target
         * for the given ID.
         * @param targetID toggle breakpoints target identifier
         * @return toggle target name
         */
        public String getToggleTargetName(String targetID) {
            IToggleBreakpointsTargetFactory factory = getFactory();
            if (factory != null) {
                return factory.getToggleTargetName(targetID);
            } 
            return null;
        }

        /** 
         * Instantiates the factory and asks it to produce the description of the toggle 
         * target for the given ID.
         * @param targetID toggle breakpoints target identifier
         * @return toggle target name or <code>null</code> if none
         */
        public String getToggleTargetDescription(String targetID) {
            IToggleBreakpointsTargetFactory factory = getFactory();
            if (factory != null) {
                return factory.getToggleTargetDescription(targetID);
            } 
            return null;
        }
        
        /** 
         * Instantiates the factory and asks it for the toggle tareget ID that
         * the factory considers the default for the given part and selection.
         * @param part The active part.
         * @param selection The current selection
         * @return a breakpoint toggle target identifier or <code>null</code>
         */
        public String getDefaultToggleTarget(IWorkbenchPart part, ISelection selection) { 
            IToggleBreakpointsTargetFactory factory = getFactory();
            if (factory != null) {
                return factory.getDefaultToggleTarget(part, selection);
            } 
            return null;
        }
    }

    
    /**
     * Factory for toggle breakpoints targets contributed through the
     * adapter mechanism.  
     */
    private static class ToggleBreakpointsTargetAdapterFactory implements IToggleBreakpointsTargetFactory {

        private IAdaptable getAdaptable(IWorkbenchPart part, ISelection selection) {
            IAdaptable adaptable = null;
            if (selection instanceof IStructuredSelection) {
                IStructuredSelection ss = (IStructuredSelection)selection; 
                if (ss.getFirstElement() instanceof IAdaptable) {
                    adaptable = (IAdaptable) ss.getFirstElement();
                }
            } else {
                adaptable = part;
            }
            return adaptable;
        }
        
        private boolean canGetToggleBreakpointsTarget(IAdaptable adaptable) {
            if (adaptable != null) {
                IToggleBreakpointsTarget adapter = (IToggleBreakpointsTarget) 
                    adaptable.getAdapter(IToggleBreakpointsTarget.class);
                if (adapter == null) {
                    IAdapterManager adapterManager = Platform.getAdapterManager();
                    if (adapterManager.hasAdapter(adaptable, IToggleBreakpointsTarget.class.getName())) {
                        return true;
                    }
                } else {
                    return true;
                }
            }
            return false;
        }
        
        /**
         * Finds the toggle breakpoints target for the active part and selection.  
         * It first looks for the target using the factories registered using an 
         * extension point.  If not found it uses the <code>IAdaptable</code>
         * mechanism.
         * @param adaptable The adaptable object to retrieve the toggle adapter from
         * @return The toggle breakpoints target, or <code>null</code> if not found.
         */
        private IToggleBreakpointsTarget getToggleBreakpointsTarget(IAdaptable adaptable) {
            if (adaptable != null) {
                IToggleBreakpointsTarget adapter = (IToggleBreakpointsTarget) 
                    adaptable.getAdapter(IToggleBreakpointsTarget.class);
                if (adapter == null) {
                    // attempt to force load adapter
                    IAdapterManager adapterManager = Platform.getAdapterManager();
                    if (adapterManager.hasAdapter(adaptable, IToggleBreakpointsTarget.class.getName())) {
                        adapter = (IToggleBreakpointsTarget) 
                            adapterManager.loadAdapter(adaptable, IToggleBreakpointsTarget.class.getName());
                    }
                }
                return adapter;
            }
            return null;
        }

        /**
         * Checks if there is an adaptable object for the given part and 
         * selection, and if there is, it checks whether an 
         * <code>IToggleBreakpointsTarget</code> can be obtained as an adapter.
         * @param part The workbench part in which toggle target is to be used
         * @param selection The active selection to use with toggle target
         * @return Whether the adapter (default) toggle target is available. 
         */
        public boolean isEnabled(IWorkbenchPart part, ISelection selection) {
            IAdaptable adaptable = getAdaptable(part, selection);
            return adaptable != null && canGetToggleBreakpointsTarget(adaptable);
        }
        
        /**
         * Not implemented use {@link #createDefaultToggleTarget(IWorkbenchPart, ISelection)}
         * instead.
         * @param targetID not used
         * @return always returns null
         */
        public IToggleBreakpointsTarget createToggleTarget(String targetID) {
            return null;
        }
        
        /**
         * @param part The workbench part in which toggle target is to be used
         * @param selection The active selection to use with toggle target
         * @return Returns a toggle target for the given part and selection, obtained 
         * through the adapter mechanism. 
         */
        public IToggleBreakpointsTarget createDefaultToggleTarget(IWorkbenchPart part, ISelection selection) {
            IAdaptable adaptable = getAdaptable(part, selection);
            return getToggleBreakpointsTarget(adaptable);
        }
        
        public Set getToggleTargets(IWorkbenchPart part, ISelection selection) {
            IAdaptable adaptable = getAdaptable(part, selection);
            if (canGetToggleBreakpointsTarget(adaptable)) {
                return DEFAULT_TOGGLE_TARGET_ID_SET;
            } 
            return Collections.EMPTY_SET;
        }

        public String getToggleTargetName(String targetID) {
            return ActionMessages.ToggleBreakpointsTargetManager_defaultToggleTarget_name;
        }

        public String getToggleTargetDescription(String targetID) {
            return ActionMessages.ToggleBreakpointsTargetManager_defaultToggleTarget_description;
        }
        
        public String getDefaultToggleTarget(IWorkbenchPart part, ISelection selection) {
            return DEFAULT_TOGGLE_TARGET_ID;
        }
    }

    
    /**
     * Preference key for storing the preferred targets map.
     * @see #storePreferredTargets()
     * @see #loadPreferredTargets()
     */
    public static final String PREF_TARGETS = "preferredTargets"; //$NON-NLS-1$

    
    /**
     * There should only ever be once instance of this manager for the workbench.
     */
    private static ToggleBreakpointsTargetManager fgSingleton;

    public static ToggleBreakpointsTargetManager getDefault(){
        if (fgSingleton == null) fgSingleton = new ToggleBreakpointsTargetManager();
        return fgSingleton;
    }
    
    /**
     * Maps the IDs of toggle breakpoint targets to their instances.  The target
     * IDs must be unique.
     */
    private Map fKnownFactories;

    /**
     * Maps a Set of target id's to the one target id that is preferred.
     */
    private Map fPreferredTargets;
    
    /**
     * Maps the IDs of toggle targets to the factory that can create them.
     * There can currently only be one factory for a given toggle target.
     */
    private Map fFactoriesByTargetID = new HashMap();
    
    /**
     * List of listeners to changes in the preferred toggle targets list.
     */
    private ListenerList fChangedListners = new ListenerList();

    /**
     * Initializes the collection of known factories from extension point contributions.
     */
    private void initializeFactories() {
        fKnownFactories = new LinkedHashMap();
        fKnownFactories.put(DEFAULT_TOGGLE_TARGET_ID, new ToggleBreakpointsTargetAdapterFactory());
        IExtensionPoint ep = Platform.getExtensionRegistry().getExtensionPoint(DebugUIPlugin.getUniqueIdentifier(), IDebugUIConstants.EXTENSION_POINT_TOGGLE_BREAKPOINTS_TARGET_FACTORIES);
        IConfigurationElement[] elements = ep.getConfigurationElements();
        for (int i= 0; i < elements.length; i++) {
            String id = elements[i].getAttribute(IConfigurationElementConstants.ID); 
            if (id != null && id.length() != 0) {
                if (fKnownFactories.containsKey(id)) {
                    DebugUIPlugin.log(new Status(IStatus.ERROR, DebugUIPlugin.getUniqueIdentifier(), IDebugUIConstants.INTERNAL_ERROR, "org.eclipse.debug.ui.toggleBreakpointsTargetFactory extension failed to load breakpoint toggle target because the specified id is already registered.  Specified ID is: " + id, null)); //$NON-NLS-1$
                } else {
                    fKnownFactories.put(id, new ToggleTargetFactory(elements[i]));
                }
            } else {
                DebugUIPlugin.log(new Status(IStatus.ERROR, DebugUIPlugin.getUniqueIdentifier(), IDebugUIConstants.INTERNAL_ERROR, "org.eclipse.debug.ui.toggleBreakpointsTargetFactory extension failed to load breakpoint toggle target because the specified id is empty.", null)); //$NON-NLS-1$
            }
        }   
        
        // If there are any factories contributed through the extension point, 
        // set a system property for use in enabling actions.
        System.setProperty(IDebugUIConstants.SYS_PROP_BREAKPOINT_TOGGLE_FACTORIES_USED, 
                fKnownFactories.size() > 1 ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Returns the set of IToggleBreakpointsTargetFactory objects (they will be
     * ToggleTargetFactory) that were contributed to the extension point and 
     * are enabled for the given part and selection (enabled if the factory 
     * does not have an enablement expression or if the enablement expression 
     * evaluates to true).
     * 
     * @param part active part
     * @param selection active selection in part
     * @return The factories enabled for the part and selection or an empty 
     * collection.
     */
    private Set getEnabledFactories(IWorkbenchPart part, ISelection selection) {
        if (fKnownFactories == null) initializeFactories();

        Set set = new HashSet();
        for (Iterator itr = fKnownFactories.keySet().iterator(); itr.hasNext(); ) {
            String id = (String)itr.next();
            IToggleBreakpointsTargetFactory factory = (IToggleBreakpointsTargetFactory)fKnownFactories.get(id);
            if (factory instanceof ToggleTargetFactory && 
                ((ToggleTargetFactory)factory).isEnabled(part, selection)) {
                set.add(factory);
            } else if (factory instanceof ToggleBreakpointsTargetAdapterFactory && 
                ((ToggleBreakpointsTargetAdapterFactory)factory).isEnabled(part, selection)) {
                set.add(factory);
            } 
        }
        return set;
    }

    /**
     * Produces the set of IDs for all possible toggle targets that can be used for
     * the given part and selection.
     *  
     * @param factoriesToQuery The collection of factories to check
     * @param part active part
     * @param selection active selection in part
     * @return Set of toggle target IDs or an empty set
     */
    private Set getEnabledTargetIDs(Collection factoriesToQuery, IWorkbenchPart part, ISelection selection){
        Set idsForSelection = new TreeSet();
        Iterator factoriesItr = factoriesToQuery.iterator();
        while (factoriesItr.hasNext()) {
            IToggleBreakpointsTargetFactory factory = (IToggleBreakpointsTargetFactory) factoriesItr.next();
            Iterator targetIDsItr = factory.getToggleTargets(part, selection).iterator();
            while (targetIDsItr.hasNext()) {
                String targetID = (String) targetIDsItr.next();
                fFactoriesByTargetID.put(targetID, factory);
                idsForSelection.add(targetID);             
            }           
        }
        return idsForSelection;
    }

    public Set getEnabledToggleBreakpointsTargetIDs(IWorkbenchPart part, ISelection selection) {
        return getEnabledTargetIDs(getEnabledFactories(part, selection), part, selection);
    }

    public String getPreferredToggleBreakpointsTargetID(IWorkbenchPart part, ISelection selection) {
        Set factories = getEnabledFactories(part, selection);
        Set possibleIDs = getEnabledTargetIDs(factories, part, selection);
        return chooseToggleTargetIDInSet(possibleIDs, part, selection);
    }

    public IToggleBreakpointsTarget getToggleBreakpointsTarget(IWorkbenchPart part, ISelection selection) {
        String id = getPreferredToggleBreakpointsTargetID(part, selection);
        IToggleBreakpointsTargetFactory factory = (IToggleBreakpointsTargetFactory)fFactoriesByTargetID.get(id);
        if (factory != null) {
            if (DEFAULT_TOGGLE_TARGET_ID.equals(id)) {
                return ((ToggleBreakpointsTargetAdapterFactory)factory).createDefaultToggleTarget(part, selection);
            } else {
                return factory.createToggleTarget(id);
            }
        }
        return null;
    }
    
    public String getToggleBreakpointsTargetName(String id) {
        IToggleBreakpointsTargetFactory factory = (IToggleBreakpointsTargetFactory)fFactoriesByTargetID.get(id);
        if (factory != null) {
            return factory.getToggleTargetName(id);
        }
        return null;
    }
    
    public String getToggleBreakpointsTargetDescription(String id) {
        IToggleBreakpointsTargetFactory factory = (IToggleBreakpointsTargetFactory)fFactoriesByTargetID.get(id);
        if (factory != null) {
            return factory.getToggleTargetDescription(id);
        }
        return null;
    }

    public void addChangedListener(IToggleBreakpointsTargetManagerListener listener) {
        fChangedListners.add(listener);
    }

    public void removeChangedListener(IToggleBreakpointsTargetManagerListener listener) {
        fChangedListners.remove(listener);
    }

    /**
     * Stores the map of preferred target IDs to the preference store in the format:
     * 
     * Key1A,Key1B:Value1|Key2A,Key2B,Key2C:Value2| 
     * 
     * Where the sub keys (Key1A, Key1B, etc.) are the elements of the set used at the 
     * key in the mapping and the values are the associated String value in the mapping.
     */
    private void storePreferredTargets() {
        StringBuffer buffer= new StringBuffer();
        Iterator iter = fPreferredTargets.entrySet().iterator();
        while (iter.hasNext()) {
            Entry entry = (Entry) iter.next();
            Iterator setIter = ((Set)entry.getKey()).iterator();
            while (setIter.hasNext()) {
                String currentID = (String) setIter.next();
                buffer.append(currentID);
                buffer.append(',');
            }
            buffer.deleteCharAt(buffer.length()-1);
            buffer.append(':');
            buffer.append(entry.getValue());
            buffer.append('|');
        }
        IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(DebugUIPlugin.getUniqueIdentifier());
        if(prefs != null) {
        	prefs.put(PREF_TARGETS, buffer.toString());
        }
    }

    /**
     * Loads the map of preferred target IDs from the preference store.
     * 
     * @see #storePreferredTargets()
     */
    private void loadPreferredTargets() {
        fPreferredTargets = new HashMap();
        String preferenceValue = Platform.getPreferencesService().getString(
        		DebugUIPlugin.getUniqueIdentifier(), 
        		PREF_TARGETS, 
        		null, 
        		null);
        if(preferenceValue == null) {
        	return;
        }
        StringTokenizer entryTokenizer = new StringTokenizer(preferenceValue,"|"); //$NON-NLS-1$
        while (entryTokenizer.hasMoreTokens()){
            String token = entryTokenizer.nextToken();
            int valueStart = token.indexOf(':');
            StringTokenizer keyTokenizer = new StringTokenizer(token.substring(0,valueStart),","); //$NON-NLS-1$
            Set keys = new TreeSet();
            while (keyTokenizer.hasMoreTokens()){
                keys.add(keyTokenizer.nextToken());
            }
            fPreferredTargets.put(keys, token.substring(valueStart+1));
        }
    }

    /**
     * Adds or updates the mapping to set which target ID is preferred for a certain
     * set of possible IDs.
     * 
     * @param possibleIDs The set of possible IDs
     * @param preferredID The preferred ID in the set.
     */
    public void setPreferredTarget(Set possibleIDs, String preferredID) {
        if (possibleIDs == null) return;

        if (fKnownFactories == null) initializeFactories();

        if (fPreferredTargets == null){
            loadPreferredTargets();
        }
        String currentKey = (String)fPreferredTargets.get(possibleIDs);
        if (currentKey == null || !currentKey.equals(preferredID)){
            fPreferredTargets.put(possibleIDs, preferredID);
            storePreferredTargets();
            firePreferredTargetsChanged();
        }        
    }
    
    /**
     * Returns the preferred toggle target ID from the given set if the mapping has been set.
     * 
     * @param possibleTargetIDs The set of possible toggle target IDs
     * @return The preferred ID or null
     */
    private String getUserPreferredTarget(Set possibleTargetIDs){
        if (fPreferredTargets == null){
            loadPreferredTargets();
        }
        return (String)fPreferredTargets.get(possibleTargetIDs);
    }

    /**
     * Given a set of possible toggle target IDs, this method will determine which target is
     * preferred and should be used to toggle breakpoints.  This method chooses a toggle target
     * by storing previous choices and can be set using a context menu.
     * 
     * @param possibleTargetIDs The set of possible toggle target IDs
     * @param part The workbench part in which toggle target is to be used
     * @param selection The active selection to use with toggle target 
     * @return The preferred toggle target ID or null
     */
    private String chooseToggleTargetIDInSet(Set possibleTargetIDs, IWorkbenchPart part, ISelection selection){
        if (possibleTargetIDs == null || possibleTargetIDs.isEmpty()){
            return null;
        }
        
        String preferredID = getUserPreferredTarget(possibleTargetIDs);
        
        if (preferredID == null){
            // If there is no preferred pane already set, check the factories to see there is a default target
            Iterator possibleIDsIterator = possibleTargetIDs.iterator();
            while (preferredID == null && possibleIDsIterator.hasNext()) {
                IToggleBreakpointsTargetFactory factory = (IToggleBreakpointsTargetFactory)
                        fFactoriesByTargetID.get(possibleIDsIterator.next());
                if (factory != null) {
                    preferredID = factory.getDefaultToggleTarget(part, selection);
                }
            }
            // If the factories don't have a default, just pick the first one.
            // Also make sure that the default is among the available toggle target
            // IDs (bug 352502).
            if (preferredID == null || !possibleTargetIDs.contains(preferredID)) {
                preferredID= (String)possibleTargetIDs.iterator().next();
            }
            setPreferredTarget(possibleTargetIDs, preferredID);
        }

        return preferredID;
    }

    /**
     * Notifies the change listeners that the preferred targets changed.
     */
    private void firePreferredTargetsChanged() {
        Object[] listeners = fChangedListners.getListeners();
        for (int i = 0; i < listeners.length; i++) {
            ((IToggleBreakpointsTargetManagerListener)listeners[i]).preferredTargetsChanged();
        }
    }
    
    public IBreakpoint getBeakpointFromEditor(ITextEditor editor, IVerticalRulerInfo info) {
    	IDocumentProvider provider = editor.getDocumentProvider();
    	if(provider == null) {
    		return null;
    	}
    	IEditorInput input = editor.getEditorInput();
    	IAnnotationModel annotationModel = provider.getAnnotationModel(input);
		if (annotationModel != null) {
			IDocument document = provider.getDocument(input);
			Iterator iterator = annotationModel.getAnnotationIterator();
			while (iterator.hasNext()) {
				Object object = iterator.next();
				if (object instanceof SimpleMarkerAnnotation) {
					SimpleMarkerAnnotation markerAnnotation = (SimpleMarkerAnnotation) object;
					IMarker marker = markerAnnotation.getMarker();
					try {
						if (marker.isSubtypeOf(IBreakpoint.BREAKPOINT_MARKER)) {
							Position position = annotationModel.getPosition(markerAnnotation);
							int line = document.getLineOfOffset(position.getOffset());
							if (line == info.getLineOfLastMouseButtonActivity()) {
								IBreakpoint breakpoint = DebugPlugin.getDefault().getBreakpointManager().getBreakpoint(marker);
								if (breakpoint != null) {
									return breakpoint;
								}
							}
						}
					} catch (CoreException e) {
					} catch (BadLocationException e) {
					}
				}
			}
		}
    	return null;
    }
}
