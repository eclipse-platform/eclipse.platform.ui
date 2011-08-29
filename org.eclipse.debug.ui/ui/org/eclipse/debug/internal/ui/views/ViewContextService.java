/*******************************************************************************
 *  Copyright (c) 2006, 2011 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.commands.contexts.Context;
import org.eclipse.core.commands.contexts.ContextManagerEvent;
import org.eclipse.core.commands.contexts.IContextManagerListener;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.internal.core.IInternalDebugCoreConstants;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.contexts.DebugModelContextBindingManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.debug.ui.contexts.IDebugContextService;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener4;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.contexts.IContextService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Performs view management for a window.
 * 
 * @since 3.2
 */
public class ViewContextService implements IDebugContextListener, IPerspectiveListener4, IPropertyChangeListener, IContextManagerListener {
	
	/**
	 * Maps the perspectives in this window to its last activated workbench context
	 */
	private Map fPerspectiveToActiveContext = new HashMap();
	
	/**
	 * Map of the perspectives to all workbench contexts activated in that perspective 
	 */
	private Map fPerspectiveToActivatedContexts = new HashMap();
	
	/**
	 * Map of context id's to context view bindings
	 */
	private Map fContextIdsToBindings;
    	
	/**
	 * List of perspectives that debugging is allowed in
	 */
	private Set fEnabledPerspectives = new HashSet();	
    
    /**
     * Whether to ignore perspective change call backs (set to 
     * true when this class is modifying views). 
     */
    private boolean fIgnoreChanges = false;
	
	/**
	 * The window this service is working for
	 */
	private IWorkbenchWindow fWindow;
	
	private IContextService fContextService;
	
	private IDebugContextService fDebugContextService;
	
	/**
	 * Perspective that is currently being de-activated.  Used to determine
	 * when to ignore active context changes.  
	 */
	private IPerspectiveDescriptor fActivePerspective;
	
	// base debug context
	public static final String DEBUG_CONTEXT= "org.eclipse.debug.ui.debugging"; //$NON-NLS-1$
	
	// extension points
	private static final String ID_CONTEXT_VIEW_BINDINGS= "contextViewBindings"; //$NON-NLS-1$

	// extension elements
    private static final String ELEM_CONTEXT_VIEW_BINDING= "contextViewBinding"; //$NON-NLS-1$
    private static final String ELEM_PERSPECTIVE= "perspective"; //$NON-NLS-1$
    
	// extension attributes
	private static final String ATTR_CONTEXT_ID= "contextId"; //$NON-NLS-1$
	private static final String ATTR_VIEW_ID= "viewId"; //$NON-NLS-1$
	private static final String ATTR_AUTO_OPEN= "autoOpen"; //$NON-NLS-1$
	private static final String ATTR_AUTO_CLOSE= "autoClose"; //$NON-NLS-1$	
    private static final String ATTR_PERSPECTIVE_ID= "perspectiveId"; //$NON-NLS-1$
    
    // XML tags
    private static final String XML_ELEMENT_VIEW_BINDINGS ="viewBindings"; //$NON-NLS-1$
    private static final String XML_ELEMENT_PERSPECTIVE ="perspective"; //$NON-NLS-1$
    private static final String XML_ELEMENT_VIEW = "view"; //$NON-NLS-1$
    private static final String XML_ATTR_ID = "id"; //$NON-NLS-1$
    private static final String XML_ATTR_USER_ACTION = "userAction"; //$NON-NLS-1$
    private static final String XML_VALUE_OPENED = "opened"; //$NON-NLS-1$
    private static final String XML_VALUE_CLOSED = "closed"; //$NON-NLS-1$
    
    // ids of base debug views in debug perspective that should not be auto-closed
    private static Set fgBaseDebugViewIds = null;
    
    static {
        fgBaseDebugViewIds = new HashSet();
        fgBaseDebugViewIds.add(IDebugUIConstants.ID_DEBUG_VIEW);
        fgBaseDebugViewIds.add(IDebugUIConstants.ID_VARIABLE_VIEW);
        fgBaseDebugViewIds.add(IDebugUIConstants.ID_BREAKPOINT_VIEW);
        fgBaseDebugViewIds.add(IConsoleConstants.ID_CONSOLE_VIEW);
    }
    
    private static String[] EMPTY_IDS = new String[0];
    
    /**
     * View bindings for a debug context
     */
    private class DebugContextViewBindings {
    	
    	// context id
    	private String fId;
    	
    	// list of view bindings id's specific to this context
    	private String[] fViewBindingIds = EMPTY_IDS;
    	
    	// all bindings including inherited bindings, top down in activation order
    	private String[] fAllViewBindingIds = null;
    	// associated binding to activate
    	private Map fAllViewIdToBindings = new HashMap();
    	// all context id's in this context hierarchy (top down order)
    	private String[] fAllConetxtIds = null;

    	// id of parent context
    	private String fParentId;
    	
    	/**
    	 * Constructs an empty view binding for the given context.
    	 * 
    	 * @param id context id
    	 */
    	public DebugContextViewBindings(String id) {
    		fId = id;
    	}
    	
    	/**
    	 * Returns the context id for these view bindings
    	 * 
    	 * @return context id
    	 */
    	public String getId() {
    		return fId;
    	}
    	
    	/**
    	 * Adds the given view binding to this context
    	 * 
    	 * @param binding view binding to add
    	 */
    	public void addBinding(ViewBinding binding) {
    		String[] newBindings = new String[fViewBindingIds.length + 1];
    		System.arraycopy(fViewBindingIds, 0, newBindings, 0, fViewBindingIds.length);
    		newBindings[fViewBindingIds.length] = binding.getViewId();
    		fAllViewIdToBindings.put(binding.getViewId(), binding);
    		fViewBindingIds = newBindings;
    	}
    	
    	/**
    	 * Sets the parent id of this view bindings
    	 * 
    	 * @param id parent context id
    	 */
    	protected void setParentId(String id) {
    		fParentId = id;
    	}
    	
    	/**
    	 * Returns the id of parent context
    	 * 
    	 * @return parent context id
    	 */
    	public DebugContextViewBindings getParentContext() {
    		if (fParentId == null) {
    			return null;
    		}
    		return (DebugContextViewBindings) fContextIdsToBindings.get(fParentId);
    	}
    	
    	/**
    	 * Activates the views in this context hierarchy. Views are activated top down, allowing
    	 * sub-contexts to override settings in a parent context.
    	 * @param page the page context
    	 * @param perspective the perspective description
    	 * @param allViewIds that are relevant to the chain activation.
    	 */
    	public void activateChain(IWorkbenchPage page, IPerspectiveDescriptor perspective, Set allViewIds) {
    		initializeChain();
    		doActivation(page, perspective, allViewIds, fAllConetxtIds);
    	}	
    	
    	public String[] getAllViewBindingsIds() {
    	    initializeChain();
    	    return fAllViewBindingIds;
    	}
    	
    	/**
    	 * Activates the view bindings for the specified views and the 
    	 * specified contexts in the given page.
    	 *  
    	 * @param page page to activate views in
    	 * @param perspective the perspective description
    	 * @param allViewIds id's of all the views that are relevant in this context activation
    	 * @param contextIds associated contexts that are activated
    	 */
    	private void doActivation(IWorkbenchPage page, IPerspectiveDescriptor perspective, Set allViewIds, String[] contextIds) {
    		// note activation of all the relevant contexts
    		for (int i = 0; i < contextIds.length; i++) {
				addActivated(contextIds[i]);
			}
    		// set the active context to be this
    		setActive(perspective, getId());
    		// activate the view bindings and bring most relevant views to top
    		for (int i = 0; i < fAllViewBindingIds.length; i++) {
				String viewId = fAllViewBindingIds[i];
				ViewBinding binding = (ViewBinding) fAllViewIdToBindings.get(viewId);
				binding.activated(page, perspective);
                binding.checkZOrder(page, allViewIds);
			}
    	}
    	
    	/**
    	 * Builds the top down ordered list of bindings for this context allowing sub-contexts
    	 * to override parent settings.
    	 */
    	private synchronized void initializeChain() {
    		if (fAllViewBindingIds == null) {
    			List orderedIds = new ArrayList();
    			List contexts = new ArrayList();
    			DebugContextViewBindings context = this;
    			while (context != null) {
    				contexts.add(0, context);
    				context = context.getParentContext();
    			}
    			Iterator iterator = contexts.iterator();
    			fAllConetxtIds = new String[contexts.size()];
    			int pos = 0;
    			while (iterator.hasNext()) {
    				DebugContextViewBindings bindings = (DebugContextViewBindings) iterator.next();
    				fAllConetxtIds[pos] = bindings.getId();
    				pos++;
    				for (int i = 0; i < bindings.fViewBindingIds.length; i++) {
						String viewId = bindings.fViewBindingIds[i];
    					if (bindings == this) {
    						orderedIds.add(viewId);
    					}
    					if (!fAllViewIdToBindings.containsKey(viewId)) {
    						orderedIds.add(viewId);
    						fAllViewIdToBindings.put(viewId, bindings.fAllViewIdToBindings.get(viewId));
    					}
					}
    			}
    			fAllViewBindingIds = (String[]) orderedIds.toArray(new String[orderedIds.size()]);
    		}
    	}
    	
    	/**
    	 * Deactivates this context only (not parents)
    	 * 
    	 * @param page workbench page
    	 * @param perspective the perspective description
    	 */
    	public void deactivate(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
			removeActivated(getId());
			if (isActiveContext(getId())) {
				setActive(page.getPerspective(), null);
			}
			for (int i = 0; i < fViewBindingIds.length; i++) {
				String viewId = fViewBindingIds[i];
				ViewBinding binding = (ViewBinding) fAllViewIdToBindings.get(viewId);
				binding.deactivated(page, perspective);
			}
    	}
    	
    	/**
    	 * Notes when a view is opened/closed manually.
    	 * 
    	 * @param opened opened or closed
    	 * @param viewId the view identifier
    	 */
    	public void setViewOpened(boolean opened, String viewId) {
    		initializeChain();
    		ViewBinding binding = (ViewBinding)fAllViewIdToBindings.get(viewId);
    		if (binding != null) {
    			if (opened) {
    				binding.userOpened();
    			} else {
    				binding.userClosed();
    			}
    		}
    	}

        public void applyUserSettings(String viewId, Element viewElement) {
        	initializeChain();
        	ViewBinding binding = (ViewBinding) fAllViewIdToBindings.get(viewId);
        	if (binding != null) {
        		binding.applyUserSettings(viewElement);
        	}
        }    	
    	
        /**
         * Save view binding settings into XML document.
         * 
         * @param document the document to save to
         * @param root the root XML element
         * @param alreadyDone views already done
         */
        public void saveBindings(Document document, Element root, Set alreadyDone) {
        	for (int i = 0; i < fViewBindingIds.length; i++) {
				String viewId = fViewBindingIds[i];
        		if (!alreadyDone.contains(viewId)) {
        			alreadyDone.add(viewId);
        			ViewBinding binding = (ViewBinding) fAllViewIdToBindings.get(viewId);
        			binding.saveBindings(document, root);
        		}
			}
        }    	
    }
    
    /**
     * Information for a view
     */
    private class ViewBinding {
        private IConfigurationElement fElement;
        /**
         * Set of perspectives this view was opened in by the user
         */
        private Set fUserOpened = new HashSet();
        /**
         * Set of perspectives this view was closed in by the user
         */
        private Set fUserClosed = new HashSet();
        /**
         * Set of perspectives this view was auto-opened by view management.
         */
        private Set fAutoOpened = new HashSet();
        
        public ViewBinding(IConfigurationElement element) {
            fElement = element;
        }
        
        /**
         * Returns the id of the view this binding pertains to.
         * 
         * @return the id of the view
         */
        public String getViewId() {
            return fElement.getAttribute(ATTR_VIEW_ID);
        }
        
        /**
         * Returns whether this view binding is set for auto-open.
         * 
         * @return if the view is set to auto-open
         */
        public boolean isAutoOpen() {
            String autoopen = fElement.getAttribute(ATTR_AUTO_OPEN);
            return autoopen == null || "true".equals(autoopen); //$NON-NLS-1$
        }
        
        /**
         * Returns whether this view binding is set for auto-close.
         * 
         * @return if the view is set to auto-close
         */
        public boolean isAutoClose() {
            String autoclose = fElement.getAttribute(ATTR_AUTO_CLOSE);
            return autoclose == null || "true".equals(autoclose); //$NON-NLS-1$
        }
        
        /**
         * Returns whether this view was opened by the user in the active perspective.
         * @param perspective the perspective description
         * @return if this view was opened by the user
         */
        public boolean isUserOpened(IPerspectiveDescriptor perspective) {
            return fUserOpened.contains(perspective.getId());
        }
        
        /**
         * Returns whether this view was closed by the user in the active perspective
         * @param perspective the description of the perspective
         * @return if this view was closed by the user in the active perspective 
         */
        public boolean isUserClosed(IPerspectiveDescriptor perspective) {
            return fUserClosed.contains(getActivePerspective().getId());
        }
        
        /**
         * Returns whether this view is part of the active perspective by default
         * 
         * TODO: we really need an API to determine which views are
         * in a perspective by default, but it does not seem to exist.
         * @param perspective  the description of the perspective
         * @return if this view is part of the active perspective by default
         */
        public boolean isDefault(IPerspectiveDescriptor perspective) {
            String id = perspective.getId();
            if (IDebugUIConstants.ID_DEBUG_PERSPECTIVE.equals(id)) {
                return fgBaseDebugViewIds.contains(getViewId());
            }
            return false;
        }
        
        protected void userOpened() {
            if (isTrackingViews()) {
                String id = getActivePerspective().getId();
                fAutoOpened.remove(id);
                fUserOpened.add(id);
                fUserClosed.remove(id);
                saveViewBindings();
            }
        }
        
        protected void userClosed() {
            if (isTrackingViews()) {
                String id = getActivePerspective().getId();
                fAutoOpened.remove(id);
                fUserClosed.add(id);
                fUserOpened.remove(id);
                saveViewBindings();
            }
        }
        
        /**
         * Returns whether the preference is set to track user view open/close.
         * 
         * @return if the service is set to track user view open/close
         */
        protected boolean isTrackingViews() {
            return DebugUITools.getPreferenceStore().getBoolean(IInternalDebugUIConstants.PREF_TRACK_VIEWS);
        }
        
        /**
         * Context has been activated, open/show as required.
         * 
         * @param page the workbench page
         * @param perspective the perspective description
         */
        public void activated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
            if (!isUserClosed(perspective)) {
                if (isAutoOpen()) {
                    try {
                        fIgnoreChanges = true;
                        // Remember whether the view was opened by view management.
                        // (Bug 128065)
                        if (page.findViewReference(getViewId()) == null) {
                            fAutoOpened.add(perspective.getId());
                        }
                        page.showView(getViewId(), null, IWorkbenchPage.VIEW_CREATE);
                    } catch (PartInitException e) {
                        DebugUIPlugin.log(e);
                    } finally {
                        fIgnoreChanges = false;
                    }
                }
            }
        }
        
        /**
         * Context has been activated. Check the view stack to see if this view
         * should be made visible.
         * 
         * @param page the page to check
         * @param relevantViews the array of view identifiers
         */
        public void checkZOrder(IWorkbenchPage page, Set relevantViews) {
        	// see if view is open already
        	IViewPart part = page.findView(getViewId());
        	if (part != null) {
        		IViewPart[] viewStack = page.getViewStack(part);
        		if (viewStack != null && viewStack.length > 0) {
        			String top = viewStack[0].getSite().getId();
        			if (relevantViews.contains(top)) {
        			    return;
        			}

        			// an irrelevant view is visible
                    try {
                        fIgnoreChanges = true;
                        page.bringToTop(part);
                    } finally {
                        fIgnoreChanges = false;
                    }
        		}
        	}
        }        
        
        /**
         * Context has been deactivated, close as required.
         * 
         * @param page the workbench page
         * @param perspective the perspective description
         */
        public void deactivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
            if (!isUserOpened(perspective)) {
                if (fAutoOpened.remove(perspective.getId()) && isAutoClose() && !isDefault(perspective)) {
                    IViewReference reference = page.findViewReference(getViewId());
                    if (reference != null) {
                        try {
                            fIgnoreChanges = true;
                            page.hideView(reference);
                        } finally {
                            fIgnoreChanges = false;
                        }
                    }
                }
            }
        }

        /**
         * Save view binding settings into XML document.
         * 
         * @param document the document to save to
         * @param root the root XML element
         */
        public void saveBindings(Document document, Element root) {
            Element viewElement = document.createElement(XML_ELEMENT_VIEW);
            viewElement.setAttribute(XML_ATTR_ID, getViewId());
            appendPerspectives(document, viewElement, fUserOpened, XML_VALUE_OPENED);
            appendPerspectives(document, viewElement, fUserClosed, XML_VALUE_CLOSED);
            if (viewElement.hasChildNodes()) {
                root.appendChild(viewElement);
            }
        }
        
        private void appendPerspectives(Document document, Element parent, Set perpectives, String xmlValue) {
            String[] ids = (String[]) perpectives.toArray(new String[perpectives.size()]);
            for (int i = 0; i < ids.length; i++) {
                String id = ids[i];
                Element element = document.createElement(XML_ELEMENT_PERSPECTIVE);
                element.setAttribute(XML_ATTR_ID, id);
                element.setAttribute(XML_ATTR_USER_ACTION, xmlValue);
                parent.appendChild(element);
            }
        }
        
        public void applyUserSettings(Element viewElement) {
            NodeList list = viewElement.getChildNodes();
            int length = list.getLength();
            for (int i = 0; i < length; ++i) {
                Node node = list.item(i);
                short type = node.getNodeType();
                if (type == Node.ELEMENT_NODE) {
                    Element entry = (Element) node;
                    if(entry.getNodeName().equalsIgnoreCase(XML_ELEMENT_PERSPECTIVE)){
                        String id = entry.getAttribute(XML_ATTR_ID);
                        String setting = entry.getAttribute(XML_ATTR_USER_ACTION);
                        if (id != null) {
                            if (XML_VALUE_CLOSED.equals(setting)) {
                                fUserClosed.add(id);
                            } else if (XML_VALUE_OPENED.equals(setting)) {
                                fUserOpened.add(id);
                            }
                        }
                    }
                }
            }       
        }
    }
    
    private IDebugContextService getDebugContextService() {
    	return fDebugContextService;
    }
	
	/**
	 * Creates a service for the given window
	 * 
	 * @param window the window to attach this service to
	 */
	ViewContextService(IWorkbenchWindow window) {
		fWindow = window;
		fContextService = (IContextService) PlatformUI.getWorkbench().getAdapter(IContextService.class);
        fDebugContextService = DebugUITools.getDebugContextManager().getContextService(fWindow);
		loadContextToViewExtensions();
        applyUserViewBindings();
		loadPerspectives();
		window.addPerspectiveListener(this);
		getDebugContextService().addDebugContextListener(this);
		DebugUIPlugin.getDefault().getPluginPreferences().addPropertyChangeListener(this);
		fContextService.addContextManagerListener(this);
		if (fWindow != null) {
		    IWorkbenchPage page = fWindow.getActivePage();
		    if (page != null) {
		        fActivePerspective = page.getPerspective();
		    }
		}
	}
	
	public void dispose() {
		fWindow.removePerspectiveListener(this);
		fWindow = null;  // avoid leaking a window reference (bug 321658).
		getDebugContextService().removeDebugContextListener(this);
		DebugUIPlugin.getDefault().getPluginPreferences().removePropertyChangeListener(this);
		fContextService.removeContextManagerListener(this);
        fActivePerspective = null;
	}
	
	/**
	 * Loads extensions which map context id's to view bindings.
	 */
	private void loadContextToViewExtensions() {
        fContextIdsToBindings = new HashMap();
		IExtensionPoint extensionPoint= Platform.getExtensionRegistry().getExtensionPoint(DebugUIPlugin.getUniqueIdentifier(), ID_CONTEXT_VIEW_BINDINGS);
		IConfigurationElement[] configurationElements = extensionPoint.getConfigurationElements();
		for (int i = 0; i < configurationElements.length; i++) {
			IConfigurationElement element = configurationElements[i];
			if ( ELEM_CONTEXT_VIEW_BINDING.equals(element.getName()) ) {
    			String viewId = element.getAttribute(ATTR_VIEW_ID);
    			String contextId = element.getAttribute(ATTR_CONTEXT_ID);
    			if (contextId == null || viewId == null) {
    				continue;
    			}
                ViewBinding info = new ViewBinding(element);
    			DebugContextViewBindings bindings = (DebugContextViewBindings) fContextIdsToBindings.get(contextId);
    			if (bindings == null) {
    				bindings = new DebugContextViewBindings(contextId);
    				fContextIdsToBindings.put(contextId, bindings);
    			}
    			bindings.addBinding(info);
			}
		}
		linkParentContexts();
	}
    
    /**
     * Applies user settings that modify view binding extensions.
     */
    private void applyUserViewBindings() {
        String xml = DebugUITools.getPreferenceStore().getString(IInternalDebugUIConstants.PREF_USER_VIEW_BINDINGS);
        if (xml.length() > 0) {
            try {
                Element root = DebugPlugin.parseDocument(xml);
                NodeList list = root.getChildNodes();
                int length = list.getLength();
                for (int i = 0; i < length; ++i) {
                    Node node = list.item(i);
                    short type = node.getNodeType();
                    if (type == Node.ELEMENT_NODE) {
                        Element entry = (Element) node;
                        if(entry.getNodeName().equalsIgnoreCase(XML_ELEMENT_VIEW)){
                            String id = entry.getAttribute(XML_ATTR_ID);
                            Iterator bindings = fContextIdsToBindings.values().iterator();
                            while (bindings.hasNext()) {
                            	DebugContextViewBindings binding = (DebugContextViewBindings) bindings.next();
                            	binding.applyUserSettings(id, entry);
                            }
                        }
                    }
                }                
            } catch (CoreException e) {
                DebugUIPlugin.log(e);
            }
        }
    }
	
	/**
	 * Load the collection of perspectives in which view management will occur from the preference store.
	 */
	private void loadPerspectives() {
	    String preference = DebugUIPlugin.getDefault().getPreferenceStore().getString(
	        IDebugUIConstants.PREF_MANAGE_VIEW_PERSPECTIVES);
	    if (IDebugUIConstants.PREF_MANAGE_VIEW_PERSPECTIVES_DEFAULT.equals(preference)) {
            fEnabledPerspectives = getDefaultEnabledPerspectives();
	    } else {
            fEnabledPerspectives = parseList(preference);
	    }
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.Preferences.IPropertyChangeListener#propertyChange(org.eclipse.core.runtime.Preferences.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
        if (!fIgnoreChanges) {
    		if (IDebugUIConstants.PREF_MANAGE_VIEW_PERSPECTIVES.equals(event.getProperty())) { 
    			loadPerspectives();
    		} else if (IInternalDebugUIConstants.PREF_USER_VIEW_BINDINGS.equals(event.getProperty())) {
    		    loadContextToViewExtensions();
                applyUserViewBindings();
                // clear activations to re-enable activation based on new settings
                fPerspectiveToActivatedContexts.clear();
                ISelection selection = getDebugContextService().getActiveContext();
                contextActivated(selection);
            }
        }
	}	
	
	/**
	 * Returns whether this service's window's active perspective supports view management.
	 * 
	 * @return whether this service's window's active perspective supports view management
	 */
	private boolean isEnabledPerspective() {
		IPerspectiveDescriptor perspective = getActivePerspective();
		if (perspective != null) {
			return fEnabledPerspectives.contains(perspective.getId());
		}
		return false;
	}	
	
	/**
	 * Returns the active perspective in this service's window, or <code>null</code>
	 * 
	 * @return active perspective or <code>null</code>
	 */
	private IPerspectiveDescriptor getActivePerspective() {
        if (fWindow == null) {
            return null;
        }

        return fActivePerspective;
	}
	
	/**
	 * Parses the comma separated string into a list of strings
	 * @param listString the comma separated string to parse into a list object
	 * 
	 * @return list
	 */
	public static Set parseList(String listString) {
		Set list = new HashSet(10);
		StringTokenizer tokenizer = new StringTokenizer(listString, ","); //$NON-NLS-1$
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			list.add(token);
		}
		return list;
	}
	
	/**
     * Calculates the default set of perspectives enabled for view management 
     * based on the contextViewBindings extension point.
     * 
     * @return set of enabled perspectives.
     * 
     * @since 3.5
     */
    public static Set getDefaultEnabledPerspectives() {
        Set perspectives = new HashSet(4);
        
        IExtensionPoint extensionPoint= Platform.getExtensionRegistry().getExtensionPoint(DebugUIPlugin.getUniqueIdentifier(), ID_CONTEXT_VIEW_BINDINGS);
        IConfigurationElement[] configurationElements = extensionPoint.getConfigurationElements();
        for (int i = 0; i < configurationElements.length; i++) {
            IConfigurationElement element = configurationElements[i];
            if ( ELEM_PERSPECTIVE.equals(element.getName()) ) {
                String perspectiveId = element.getAttribute(ATTR_PERSPECTIVE_ID);
                if (perspectiveId != null) {
                    perspectives.add(perspectiveId);
                }
            }
        }
        
        return perspectives;
    }

	public void contextActivated(ISelection selection) {
		if (isEnabledPerspective()) {
			if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
				IStructuredSelection ss = (IStructuredSelection) selection;
				Iterator iterator = ss.iterator();
				while (iterator.hasNext()) {
					Object target = iterator.next();
					ILaunch launch = DebugModelContextBindingManager.getLaunch(target);
					if (launch != null && !launch.isTerminated()) {
						ILaunchConfiguration launchConfiguration = launch.getLaunchConfiguration();
						if (launchConfiguration != null) {
							try {
								ILaunchConfigurationType type = launchConfiguration.getType();
								// check if this perspective is enabled for the launch type
								// Include the word '.internal.' so the context is filtered from the key binding pref page (Bug 144019) also see PerspectiveManager.handleBreakpointHit()
								if (fContextService.getActiveContextIds().contains(type.getIdentifier() + ".internal." + getActivePerspective().getId())) { //$NON-NLS-1$
									// get the leaf contexts to be activated
									List workbenchContexts = DebugModelContextBindingManager.getDefault().getWorkbenchContextsForDebugContext(target);
									// TODO: do we need to check if contexts are actually enabled in workbench first?
									if (!workbenchContexts.isEmpty()) {
									    // Quickly check if any contexts need activating
									    boolean needToActivate = false;
									    for (int i = 0; i < workbenchContexts.size(); i++) {
                                            if (!isActivated((String)workbenchContexts.get(i))) {
                                                needToActivate = true;
                                                break;
                                            }
									    }
									    
									    if (needToActivate) {
									        Set allViewIds = getAllContextsViewIDs(workbenchContexts);
									        
    										// if all contexts already activate and last context is already active context == done
                                            for (int i = 0; i < workbenchContexts.size(); i++) {
    											String contextId = (String)workbenchContexts.get(i);
    											if (!isActivated(contextId)) {
    												activateChain(contextId, getActivePerspective(), allViewIds);
    											}
    										}
    									}
									}									
								}
							} catch (CoreException e) {
								DebugUIPlugin.log(e);
							}
						}												
					}						
				}
			}
		}
	}
	
	/**
	 * Returns whether the given context is the active context in the active perspective.
	 * 
	 * @param contextId the id of the context
	 * @return if the given id is the id for the currently active context
	 */
	private boolean isActiveContext(String contextId) {
		IPerspectiveDescriptor activePerspective = getActivePerspective();
		if (activePerspective != null) {
			String activeId = (String) fPerspectiveToActiveContext.get(activePerspective);
			return contextId.equals(activeId);
		}
		return false;
	}
	
	/**
	 * Returns whether the given context is activated in the active perspective. 
	 * 
	 * @param contextId the context id
	 * @return if the given context is activated in the active perspective 
	 */
	private boolean isActivated(String contextId) {
		IPerspectiveDescriptor activePerspective = getActivePerspective();
		if (activePerspective != null) {
			Set contexts = (Set) fPerspectiveToActivatedContexts.get(activePerspective); 
			if (contexts != null) {
				return contexts.contains(contextId);
			}
		}
		return false;
	}
	
	private void addActivated(String contextId) {
		IPerspectiveDescriptor activePerspective = getActivePerspective();
		if (activePerspective != null) {
			Set contexts = (Set) fPerspectiveToActivatedContexts.get(activePerspective); 
			if (contexts == null) {
				contexts = new HashSet();
				fPerspectiveToActivatedContexts.put(activePerspective, contexts);
			}
			contexts.add(contextId);
		}
	}
	
	private void removeActivated(String contextId) {
		IPerspectiveDescriptor activePerspective = getActivePerspective();
		if (activePerspective != null) {
			Set contexts = (Set) fPerspectiveToActivatedContexts.get(activePerspective); 
			if (contexts != null) {
				contexts.remove(contextId);
			}
		}		
	}

	public void debugContextChanged(DebugContextEvent event) {
		if ((event.getFlags() & DebugContextEvent.ACTIVATED) > 0) {
			contextActivated(event.getContext());
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPerspectiveListener3#perspectiveOpened(org.eclipse.ui.IWorkbenchPage, org.eclipse.ui.IPerspectiveDescriptor)
	 */
	public void perspectiveOpened(IWorkbenchPage page, IPerspectiveDescriptor perspective) {        
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPerspectiveListener3#perspectiveClosed(org.eclipse.ui.IWorkbenchPage, org.eclipse.ui.IPerspectiveDescriptor)
	 */
	public void perspectiveClosed(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPerspectiveListener3#perspectiveDeactivated(org.eclipse.ui.IWorkbenchPage, org.eclipse.ui.IPerspectiveDescriptor)
	 */
	public void perspectiveDeactivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
	}
	
	/**
	 * Closes all auto-opened views.
	 * 
	 * @param perspective the perspective descriptor
	 */
	private void clean(IPerspectiveDescriptor perspective) {
		Set contexts = (Set) fPerspectiveToActivatedContexts.remove(perspective);
		fPerspectiveToActiveContext.remove(perspective);
		if (contexts != null) {
			Iterator iterator = contexts.iterator();
			while (iterator.hasNext()) {
				String id = (String) iterator.next();
				deactivate(id, perspective);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPerspectiveListener3#perspectiveSavedAs(org.eclipse.ui.IWorkbenchPage, org.eclipse.ui.IPerspectiveDescriptor, org.eclipse.ui.IPerspectiveDescriptor)
	 */
	public void perspectiveSavedAs(IWorkbenchPage page, IPerspectiveDescriptor oldPerspective, IPerspectiveDescriptor newPerspective) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPerspectiveListener2#perspectiveChanged(org.eclipse.ui.IWorkbenchPage, org.eclipse.ui.IPerspectiveDescriptor, org.eclipse.ui.IWorkbenchPartReference, java.lang.String)
	 */
	public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, IWorkbenchPartReference partRef, String changeId) {
		if (!fIgnoreChanges && page.getWorkbenchWindow().equals(fWindow)) {
			if(partRef != null) {
                if (IWorkbenchPage.CHANGE_VIEW_SHOW == changeId || IWorkbenchPage.CHANGE_VIEW_HIDE == changeId) {
                    // Update only the contexts which are currently active (Bug 128065)
                    Set activatedContexts = (Set)fPerspectiveToActivatedContexts.get(perspective);
                    if (activatedContexts != null) {
                    	Iterator iterator = activatedContexts.iterator();
                    	while (iterator.hasNext()) {
                    	    DebugContextViewBindings bindings = 
                    	        (DebugContextViewBindings)fContextIdsToBindings.get(iterator.next());
                    	    if (bindings != null) {
                    	        bindings.setViewOpened(IWorkbenchPage.CHANGE_VIEW_SHOW == changeId, partRef.getId());
                    	    }
                    	}
                    }
                }
			}
        }	
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPerspectiveListener#perspectiveActivated(org.eclipse.ui.IWorkbenchPage, org.eclipse.ui.IPerspectiveDescriptor)
	 */
	public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
		if (page.getWorkbenchWindow().equals(fWindow)) {
		    fActivePerspective = perspective;
			ISelection activeContext = getDebugContextService().getActiveContext();
			if (activeContext != null) {
				contextActivated(activeContext);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPerspectiveListener#perspectiveChanged(org.eclipse.ui.IWorkbenchPage, org.eclipse.ui.IPerspectiveDescriptor, java.lang.String)
	 */
	public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, String changeId) {
	}
	
	/**
	 * Activates all parent contexts of the given context, top down.
	 * 
	 * @param contextId the identifier of the {@link DebugContextViewBindings} to activate
	 * @param perspective the perspective description
	 */
	private void activateChain(String contextId, IPerspectiveDescriptor perspective, Set allViewIds) {
	    if (fWindow == null) return; // disposed
	    
		IWorkbenchPage page = fWindow.getActivePage();
		if (page != null) {
			DebugContextViewBindings bindings= (DebugContextViewBindings) fContextIdsToBindings.get(contextId);
			if (bindings != null) {
				bindings.activateChain(page, perspective, allViewIds);
			}
		}
	}
	
	private Set getAllContextsViewIDs(List contextsIds) {
        if (fWindow == null) return Collections.EMPTY_SET; // disposed
        
        TreeSet viewIds = new TreeSet();
        for (int i = 0; i < contextsIds.size(); i++) {
            DebugContextViewBindings bindings= (DebugContextViewBindings) fContextIdsToBindings.get(contextsIds.get(i));
            if (bindings != null) {
                String[] bindingViewIds = bindings.getAllViewBindingsIds();
                for (int j = 0; j < bindingViewIds.length; j++) {
                    viewIds.add(bindingViewIds[j]);
                }
            }
        }
        return viewIds;
	}
	
	/**
	 * Links each debug context view bindings with its parent context bindings 
	 */
	private void linkParentContexts() {
		Iterator iterator = fContextIdsToBindings.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry entry = (Entry)iterator.next();
			String contextId = (String) entry.getKey();
			DebugContextViewBindings bindings = (DebugContextViewBindings) entry.getValue();
			if (!bindings.getId().equals(DEBUG_CONTEXT)) {
				Context context = fContextService.getContext(contextId);
				try {
					bindings.setParentId(context.getParentId());
				} catch (NotDefinedException e) {
					DebugUIPlugin.log(e);
				}
			}
		}
	}	
	
	/**
	 * Sets the active context in the given perspective, or removes
	 * when <code>null</code>.
	 * 
	 * @param perspective the perspective descriptor
	 * @param contextId the context identifier
	 */
	private void setActive(IPerspectiveDescriptor perspective, String contextId) {
		if (contextId == null) {
			fPerspectiveToActiveContext.remove(perspective);
		} else {
			fPerspectiveToActiveContext.put(perspective, contextId);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.contexts.IContextManagerListener#contextManagerChanged(org.eclipse.core.commands.contexts.ContextManagerEvent)
	 */
	public void contextManagerChanged(ContextManagerEvent event) {
		if (event.isActiveContextsChanged() && getActivePerspective() != null) {
			Set disabledContexts = getDisabledContexts(event);
			if (!disabledContexts.isEmpty()) {
				Iterator contexts = disabledContexts.iterator();
				while (contexts.hasNext()) {
					String contextId = (String)contexts.next();
					if (isViewContext(contextId)) {
						if (isActivated(contextId)) {
							deactivate(contextId, getActivePerspective());
						}
					}
				}
			}
			// Ensure that the views are activated for the new contexts if needed.
			contextActivated(DebugUITools.getDebugContextManager().getContextService(fWindow).getActiveContext());
		}
	}
	
	private void deactivate(String contextId, IPerspectiveDescriptor perspective) {
	    if (fWindow == null) return;  // disposed
	    
		IWorkbenchPage page = fWindow.getActivePage();
		if (page != null) {
			DebugContextViewBindings bindings = (DebugContextViewBindings) fContextIdsToBindings.get(contextId);
			if (bindings != null) {
				bindings.deactivate(page, perspective);
			}
		}		
	}
	
	/**
	 * Returns a set of contexts disabled in the given event, possibly empty.
	 * 
	 * @param event the event
	 * @return disabled context id's
	 */
	private Set getDisabledContexts(ContextManagerEvent event) {
		Set prev = new HashSet(event.getPreviouslyActiveContextIds());
		Set activeContextIds = event.getContextManager().getActiveContextIds();
		if (activeContextIds != null) {
			prev.removeAll(activeContextIds);
		}
		return prev;
	}	

	/**
	 * Returns whether the given context has view bindings.
	 * 
	 * @param id the context id
	 * @return whether the given context has view bindings
	 */
	private boolean isViewContext(String id) {
		return fContextIdsToBindings.containsKey(id);
	}
    
    /**
     * Save view binding settings that differ from extension settings
     */
    private void saveViewBindings() {
        try {
            Document document = DebugPlugin.newDocument();
            Element root = document.createElement(XML_ELEMENT_VIEW_BINDINGS);
            document.appendChild(root);
            Set done = new HashSet();
            Iterator bindings = fContextIdsToBindings.values().iterator();
            while (bindings.hasNext()) {
                DebugContextViewBindings binding = (DebugContextViewBindings) bindings.next();
                binding.saveBindings(document, root, done);
            }
            String prefValue = IInternalDebugCoreConstants.EMPTY_STRING;
            if (root.hasChildNodes()) {
            	prefValue = DebugPlugin.serializeDocument(document);
            }
            fIgnoreChanges = true;
            DebugUITools.getPreferenceStore().setValue(IInternalDebugUIConstants.PREF_USER_VIEW_BINDINGS, prefValue);
        } catch (CoreException e) {
            DebugUIPlugin.log(e);
        } finally {
            fIgnoreChanges = false;
        }

    }    
    
    /**
     * Returns the perspectives in which debugging is enabled.
     * 
     * @return the array of perspective identifiers in which debugging is enabled 
     */
    public String[] getEnabledPerspectives() {
    	return (String[]) fEnabledPerspectives.toArray(new String[fEnabledPerspectives.size()]);
    }
    
    /**
     * Show the view without effecting user preferences
     * 
     * @param viewId the id of the view to show
     */
    public void showViewQuiet(String viewId) {
        if (fWindow == null) return;  // disposed;
        
		IWorkbenchPage page = fWindow.getActivePage();
		if (page != null) {
			try {
				fIgnoreChanges = true;
				IViewPart part = page.showView(viewId, null, IWorkbenchPage.VIEW_VISIBLE);
				if (!page.isPartVisible(part)) {
					page.bringToTop(part);
				}
			} catch (PartInitException e) {
				DebugUIPlugin.log(e);
			} finally {
				fIgnoreChanges = false;
			}
		}    	
    }

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPerspectiveListener4#perspectivePreDeactivate(org.eclipse.ui.IWorkbenchPage, org.eclipse.ui.IPerspectiveDescriptor)
	 */
	public void perspectivePreDeactivate(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
		if (page.getWorkbenchWindow().equals(fWindow)) {
            fActivePerspective = null;
			clean(perspective);
		}
	}
}
