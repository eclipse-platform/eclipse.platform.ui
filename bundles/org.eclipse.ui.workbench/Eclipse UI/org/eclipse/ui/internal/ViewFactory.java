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
package org.eclipse.ui.internal;

import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.dynamicHelpers.IExtensionRemovalHandler;
import org.eclipse.core.runtime.dynamicHelpers.IExtensionTracker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPart2;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.misc.StatusUtil;
import org.eclipse.ui.internal.misc.UIStats;
import org.eclipse.ui.internal.registry.ViewDescriptor;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.part.WorkbenchPart;
import org.eclipse.ui.views.IViewDescriptor;
import org.eclipse.ui.views.IViewRegistry;

/**
 * The ViewFactory is used to control the creation and disposal of views.  
 * It implements a reference counting strategy so that one view can be shared
 * by more than one client.
 */
/*package*/class ViewFactory implements IExtensionRemovalHandler {

    private class ViewReference extends WorkbenchPartReference implements
            IViewReference {

        private String secondaryId;

        private boolean create = true;
        
        private boolean creationInProgress = false;
        
        public ViewReference(String id, IMemento memento) {
            this(id, null, memento);
        }

        public ViewReference(String id, String secondaryId, IMemento memento) {
            super();
            ViewDescriptor desc = (ViewDescriptor) viewReg.find(id);
            ImageDescriptor iDesc = null;
            String title = null;
            if (desc != null) {
                iDesc = desc.getImageDescriptor();
                title = desc.getLabel();
            }

            String name = null;

            if (memento != null) {
                name = memento.getString(IWorkbenchConstants.TAG_PART_NAME);
            }
            if (name == null) {
                name = title;
            }

            init(id, title, null, iDesc, name, null);
            this.secondaryId = secondaryId;
        }
        
        protected PartPane createPane() {
            return new ViewPane(this, page);
        }

        /* (non-Javadoc)
         * @see org.eclipse.ui.internal.WorkbenchPartReference#dispose()
         */
        public void dispose() {
            super.dispose();
            create = false;
        }
        
        /* (non-Javadoc)
         * @see org.eclipse.ui.IWorkbenchPartReference#getPage()
         */
        public IWorkbenchPage getPage() {
            return page;
        }

        /* (non-Javadoc)
         * @see org.eclipse.ui.IWorkbenchPartReference#getPart(boolean)
         */
        public IWorkbenchPart getPart(boolean restore) {
            if (part != null)
                return part;
            if (!create)
                return null;
            if (restore) {
                IStatus status = restoreView(this);
            }
            return part;
        }

        /* (non-Javadoc)
         * @see org.eclipse.ui.internal.WorkbenchPartReference#getRegisteredName()
         */
        public String getRegisteredName() {
            if (part != null && part.getSite() != null) {
                return part.getSite().getRegisteredName();
            }

            IViewRegistry reg = viewReg;
            IViewDescriptor desc = reg.find(getId());
            if (desc != null)
                return desc.getLabel();
            return getTitle();
        }

        protected String computePartName() {
            if (part instanceof IWorkbenchPart2) {
                return super.computePartName();
            } else {
                return getRegisteredName();
            }
        }

        protected String computeContentDescription() {
            if (part instanceof IWorkbenchPart2) {
                return super.computeContentDescription();
            } else {
                String rawTitle = getRawTitle();

                if (!Util.equals(rawTitle, getRegisteredName())) {
                    return rawTitle;
                }

                return ""; //$NON-NLS-1$
            }
        }

        /* (non-Javadoc)
         * @see org.eclipse.ui.IViewReference
         */
        public String getSecondaryId() {
            return secondaryId;
        }

        /* (non-Javadoc)
         * @see org.eclipse.ui.IViewReference#getView(boolean)
         */
        public IViewPart getView(boolean restore) {
            return (IViewPart) getPart(restore);
        }

        /* (non-Javadoc)
         * @see org.eclipse.ui.IViewReference#isFastView()
         */
        public boolean isFastView() {
            return page.isFastView(this);
        }

    }

    private ReferenceCounter counter;

    private HashMap mementoTable = new HashMap();

    private WorkbenchPage page;

    private IViewRegistry viewReg;

    /**
     * Separates a view's primary id from its secondary id in view key strings.
     */
    static final String ID_SEP = ":"; //$NON-NLS-1$

    /**
     * Returns a string representing a view with the given id and (optional) secondary id,
     * suitable for use as a key in a map.  
     * 
     * @param id primary id of the view
     * @param secondaryId secondary id of the view or <code>null</code>
     * @return the key
     */
    static String getKey(String id, String secondaryId) {
        return secondaryId == null ? id : id + ID_SEP + secondaryId;
    }

    /**
     * Returns a string representing the given view reference, suitable for use as a key in a map.  
     * 
     * @param viewRef the view reference
     * @return the key
     */
    static String getKey(IViewReference viewRef) {
        return getKey(viewRef.getId(), viewRef.getSecondaryId());
    }

    /**
     * Extracts ths primary id portion of a compound id.
     * @param compoundId a compound id of the form: primaryId [':' secondaryId]
     * @return the primary id
     */
    static String extractPrimaryId(String compoundId) {
        int i = compoundId.lastIndexOf(ID_SEP);
        if (i == -1)
            return compoundId;
        return compoundId.substring(0, i);
    }

    /**
     * Extracts ths secondary id portion of a compound id.
     * @param compoundId a compound id of the form: primaryId [':' secondaryId]
     * @return the secondary id, or <code>null</code> if none
     */
    static String extractSecondaryId(String compoundId) {
        int i = compoundId.lastIndexOf(ID_SEP);
        if (i == -1)
            return null;
        return compoundId.substring(i + 1);
    }

    /**
     * Returns whether the given view id contains a wildcard. Wildcards cannot
     * be used in regular view ids, only placeholders.
     * 
     * @param viewId the view id
     * @return <code>true</code> if the given view id contains a wildcard,
     *         <code>false</code> otherwise
     * 
     * @since 3.1
     */
    static boolean hasWildcard(String viewId) {
        return viewId.indexOf(PartPlaceholder.WILD_CARD) >= 0;
    }
    
    /**
     * Constructs a new view factory.
     */
    public ViewFactory(WorkbenchPage page, IViewRegistry reg) {
        super();
        this.page = page;
        this.viewReg = reg;
        counter = new ReferenceCounter();
        page.getExtensionTracker().registerRemovalHandler(this);
    }
    
    /**
     * Wrapper for restoring the view. First, this delegates to busyRestoreViewHelper
     * to do the real work of restoring the view. If unable to restore the view, this
     * method tries to substitute an error part and return success.
     *
     * @param ref_
     * @return
     */
    public IStatus busyRestoreView(IViewReference ref_) {
        ViewReference ref = (ViewReference) ref_;
        
        // Check the status of this part
        IStatus partStatus = Status.OK_STATUS;
        
        // If the part has already been restored, exit
        if (ref.getPart(false) != null)
            return Status.OK_STATUS;
        
        if (ref.creationInProgress) {
            IStatus result = WorkbenchPlugin.getStatus(
                    new PartInitException(NLS.bind("Warning: Detected attempt by view {0} to create itself recursively (this is probably, but not necessarily, a bug)",  //$NON-NLS-1$
                            ref.getId())));
            WorkbenchPlugin.log(result);
            return result;
        }

        try {
            ref.creationInProgress = true;    
            
            // Try to restore the view -- this does the real work of restoring the view
            //
            partStatus = busyRestoreViewHelper(ref);
            
            // If unable to create the part, create an error part instead
            if (ref.getPart(false) == null) {
                IStatus displayStatus = StatusUtil.newStatus(partStatus,
                        NLS.bind(WorkbenchMessages.ViewFactory_initException, partStatus.getMessage()));
                
                IStatus logStatus = StatusUtil.newStatus(partStatus,
                        NLS.bind("Unable to create view ID {0}: {1}", ref.getId(), partStatus.getMessage()));  //$NON-NLS-1$
                WorkbenchPlugin.log(logStatus);
                
                ErrorViewPart part = new ErrorViewPart(displayStatus);
                String label = ref_.getId();
                IViewDescriptor desc = viewReg.find(ref.getId());
                if (desc != null) {
                    label = desc.getLabel();
                }
                PartPane pane = ref.getPane();
                ViewSite site = new ViewSite(ref, part, page, ref_.getId(), PlatformUI.PLUGIN_ID, label);
                site.setActionBars(new ViewActionBars(page.getActionBars(),
                        (ViewPane) pane));
                try {
                    part.init(site);
                } catch (PartInitException e) {
                    return e.getStatus();
                }
    
                Composite parent = (Composite)pane.getControl();
                Composite content = new Composite(parent, SWT.NONE);
                content.setLayout(new FillLayout());
                
                try {
                    part.createPartControl(content);
                } catch (Exception e) {
                    content.dispose();
                    return partStatus;
                }
                
                ref.setPart(part);
            }
        } finally {
            ref.creationInProgress = false;
        }
            
        return Status.OK_STATUS;
    }
    
    public IStatus busyRestoreViewHelper(ViewReference ref) {
        
        IStatus partStatus = Status.OK_STATUS;
        
        // If there was a previous failed attempt to restore the part, exit
        if (partStatus.getSeverity() != IStatus.OK) {
            return partStatus;
        }
        
        String key = getKey(ref);
        IMemento stateMem = getViewState(key);
        
        IViewDescriptor desc = viewReg.find(ref.getId());
        if (desc == null) {
            // If this view descriptor is unknown...
            return new Status(
                    IStatus.ERROR,
                    PlatformUI.PLUGIN_ID,
                    0,
                    WorkbenchMessages.ViewFactory_couldNotCreate,
                    null);
        }
        
        // Create the part pane
        PartPane pane = ref.getPane();
        
        // Create the pane's top-level control
        pane.createControl(page.getClientComposite());
        
        String label = desc.getLabel(); // debugging only

        // Things that will need to be disposed if an exception occurs (they are listed here
        // in the order they should be disposed)
        Composite content = null;
        IViewPart initializedView = null;
        ViewSite site = null;
        ViewActionBars actionBars = null;
        // End of things that need to be explicitly disposed from the try block
        
        try {
            IViewPart view;
            try { 
                UIStats.start(UIStats.CREATE_PART, label);
                
                view = desc.createView();
            } finally {
                UIStats.end(UIStats.CREATE_PART, ref, label);    
            }

            // Create site
            site = new ViewSite(ref, view, page, desc);
            actionBars = new ViewActionBars(page.getActionBars(),
                    (ViewPane) pane);
            site.setActionBars(actionBars);

            try {
                UIStats.start(UIStats.INIT_PART, label);
                view.init(site, stateMem);
                // Once we've called init, we MUST dispose the view. Remember the fact that
                // we've initialized the view in case an exception is thrown.
                initializedView = view;
                
            } finally {
                UIStats.end(UIStats.INIT_PART, view, label);
            }

            if (view.getSite() != site) {
                partStatus = WorkbenchPlugin.getStatus(WorkbenchMessages.ViewFactory_siteException,
                        null);
            } else {
                
                int style = SWT.NONE;
                if(view instanceof WorkbenchPart) {
                    style = ((WorkbenchPart) view).getOrientation();
                }

                // Create the top-level composite
                {
                    Composite parent = (Composite)pane.getControl();
                    content = new Composite(parent, style);
                    content.setLayout(new FillLayout());
    
                    try {
                        UIStats.start(UIStats.CREATE_PART_CONTROL, label);
                        view.createPartControl(content);
    
                        parent.layout(true);
                    } finally {
                        UIStats.end(UIStats.CREATE_PART_CONTROL, view, label);
                    }
                }
                
                // Install the part's tools and menu
                {
                    ViewActionBuilder builder = new ViewActionBuilder();
                    builder.readActionExtensions(view);
                    ActionDescriptor[] actionDescriptors = builder
                            .getExtendedActions();
                    KeyBindingService keyBindingService = (KeyBindingService) view
                            .getSite().getKeyBindingService();
    
                    if (actionDescriptors != null) {
                        for (int i = 0; i < actionDescriptors.length; i++) {
                            ActionDescriptor actionDescriptor = actionDescriptors[i];
    
                            if (actionDescriptor != null) {
                                IAction action = actionDescriptors[i]
                                        .getAction();
    
                                if (action != null
                                        && action.getActionDefinitionId() != null)
                                    keyBindingService.registerAction(action);
                            }
                        }
                    }
                    site.getActionBars().updateActionBars();
                }
                
                ref.setPart(view);
                ref.refreshFromPart();
                ref.releaseReferences();
                
                IConfigurationElement element = (IConfigurationElement) desc
                        .getAdapter(IConfigurationElement.class);
                if (element != null)
                    page.getExtensionTracker().registerObject(
                            element.getDeclaringExtension(), view,
                            IExtensionTracker.REF_WEAK);
                
                page.addPart(ref);
                page.firePartOpened(view);
            }
        } catch (Exception e) {
            // An exception occurred. First deallocate anything we've allocated in the try block (see the top
            // of the try block for a list of objects that need to be explicitly disposed)
            if (content != null) {
                try {
                    content.dispose();
                } catch (RuntimeException re) {
                    WorkbenchPlugin.log(re);
                }
            }
            
            if (initializedView != null) {
                try {
                    initializedView.dispose();
                } catch (RuntimeException re) {
                    WorkbenchPlugin.log(re);
                }
            }

            if (site != null) {
                try {
                    site.dispose();
                } catch (RuntimeException re) {
                    WorkbenchPlugin.log(re);
                }
            }
            
            if (actionBars != null) {
                try {
                    actionBars.dispose();
                } catch (RuntimeException re) {
                    WorkbenchPlugin.log(re);
                }
            }
            
            partStatus = WorkbenchPlugin.getStatus(e);
        }
        
        return partStatus;
    }

    /**
     * Creates an instance of a view defined by id.
     * 
     * This factory implements reference counting.  The first call to this
     * method will return a new view.  Subsequent calls will return the
     * first view with an additional reference count.  The view is
     * disposed when releaseView is called an equal number of times
     * to getView.
     */
    public IViewReference createView(final String id) throws PartInitException {
        return createView(id, null);
    }

    /**
     * Creates an instance of a view defined by id and secondary id.
     * 
     * This factory implements reference counting.  The first call to this
     * method will return a new view.  Subsequent calls will return the
     * first view with an additional reference count.  The view is
     * disposed when releaseView is called an equal number of times
     * to createView.
     */
    public IViewReference createView(String id, String secondaryId)
            throws PartInitException {
        IViewDescriptor desc = viewReg.find(id);
        // ensure that the view id is valid
        if (desc == null)
            throw new PartInitException(NLS.bind(WorkbenchMessages.ViewFactory_couldNotCreate,  id ));
        // ensure that multiple instances are allowed if a secondary id is given
        if (secondaryId != null) {
            if (!desc.getAllowMultiple()) {
                throw new PartInitException(NLS.bind(WorkbenchMessages.ViewFactory_noMultiple, id)); 
            }
        }
        String key = getKey(id, secondaryId);
        IViewReference ref = (IViewReference) counter.get(key);
        if (ref == null) {
            IMemento memento = (IMemento) mementoTable.get(key);
            ref = new ViewReference(id, secondaryId, memento);
            counter.put(key, ref);
        } else {
            counter.addRef(key);
        }
        return ref;
    }

    /**
     * Remove a view rec from the manager.
     *
     * The IViewPart.dispose method must be called at a higher level.
     */
    private void destroyView(IViewPart view) {
        // Free action bars, pane, etc.
        PartSite site = (PartSite) view.getSite();
        ViewActionBars actionBars = (ViewActionBars) site.getActionBars();
        actionBars.dispose();
        PartPane pane = site.getPane();
        pane.dispose();

        // Free the site.
        site.dispose();
    }

    /**
     * Returns the view with the given id, or <code>null</code> if not found.
     */
    public IViewReference getView(String id) {
        return getView(id, null);
    }

    /**
     * Returns the view with the given id and secondary id, or <code>null</code> if not found.
     */
    public IViewReference getView(String id, String secondaryId) {
        String key = getKey(id, secondaryId);
        return (IViewReference) counter.get(key);
    }

    /**
     * @return the <code>IViewRegistry</code> used by this factory.
     * @since 3.0
     */
    public IViewRegistry getViewRegistry() {
        return viewReg;
    }

    /**
     * Returns a list of views which are open.
     */
    public IViewReference[] getViews() {
        List list = counter.values();
        IViewReference[] array = new IViewReference[list.size()];
        list.toArray(array);
        return array;
    }

    /**
     * @return the <code>WorkbenchPage</code> used by this factory.
     * @since 3.0
     */
    public WorkbenchPage getWorkbenchPage() {
        return page;
    }

    /**
     * Returns whether a view with the same id(s) as the
     * given view reference exists.
     */
    public boolean hasView(IViewReference viewRef) {
        return hasView(viewRef.getId(), viewRef.getSecondaryId());
    }

    /**
     * Returns whether a view with the given id exists.
     */
    public boolean hasView(String id) {
        return hasView(id, null);
    }

    /**
     * Returns whether a view with the given ids exists.
     */
    public boolean hasView(String id, String secondaryId) {
        return getView(id, secondaryId) != null;
    }

    /**
     * Releases an instance of a view.
     *
     * This factory does reference counting.  For more info see
     * getView.
     */
    public void releaseView(IViewReference viewRef) {
        String key = getKey(viewRef);
        IViewReference ref = (IViewReference) counter.get(key);
        if (ref == null)
            return;
        int count = counter.removeRef(key);
        if (count <= 0) {
            IViewPart view = (IViewPart) ref.getPart(false);
            if (view != null)
                destroyView(view);
        }
    }

    /**
     * Restore view states.
     *  
     * @param memento the <code>IMemento</code> to restore from.
     * @return <code>IStatus</code>
     */
    public IStatus restoreState(IMemento memento) {
        IMemento mem[] = memento.getChildren(IWorkbenchConstants.TAG_VIEW);
        for (int i = 0; i < mem.length; i++) {
            //for dynamic UI - add the next line to replace subsequent code that is commented out
            restoreViewState(mem[i]);
        }
        return new Status(IStatus.OK, PlatformUI.PLUGIN_ID, 0, "", null); //$NON-NLS-1$
    }

    /**
     * Creates an instance of a view defined by id.
     * 
     * This factory implements reference counting.  The first call to this
     * method will return a new view.  Subsequent calls will return the
     * first view with an additional reference count.  The view is
     * disposed when releaseView is called an equal number of times
     * to getView.
     */
    public IStatus restoreView(final IViewReference ref) {
        final IStatus result[] = new IStatus[1];
        BusyIndicator.showWhile(page.getWorkbenchWindow().getShell()
                .getDisplay(), new Runnable() {
            public void run() {
                result[0] = busyRestoreView(ref);
            }
        });
        return result[0];
    }

    /**
     * Save view states.
     * 
     * @param memento the <code>IMemento</code> to save to.
     * @return <code>IStatus</code>
     */
    public IStatus saveState(IMemento memento) {
        final MultiStatus result = new MultiStatus(PlatformUI.PLUGIN_ID,
                IStatus.OK, WorkbenchMessages.ViewFactory_problemsSavingViews, null); 

        final IViewReference refs[] = getViews();
        for (int i = 0; i < refs.length; i++) {
            //for dynamic UI - add the following line to replace subsequent code which is commented out
            saveViewState(memento, refs[i], result);
        }
        return result;
    }

    //	for dynamic UI
    public IMemento saveViewState(IMemento memento, IViewReference ref,
            MultiStatus res) {
        final MultiStatus result = res;
        final IMemento viewMemento = memento
                .createChild(IWorkbenchConstants.TAG_VIEW);
        viewMemento.putString(IWorkbenchConstants.TAG_ID, ViewFactory
                .getKey(ref));
        if (ref instanceof ViewReference) {
            viewMemento.putString(IWorkbenchConstants.TAG_PART_NAME,
                    ((ViewReference) ref).getPartName());
        }
        final IViewReference viewRef = ref;
        final IViewPart view = (IViewPart) ref.getPart(false);
        if (view != null) {
            Platform.run(new SafeRunnable() {
                public void run() {
                    view.saveState(viewMemento
                            .createChild(IWorkbenchConstants.TAG_VIEW_STATE));
                }

                public void handleException(Throwable e) {
                    result
                            .add(new Status(
                                    IStatus.ERROR,
                                    PlatformUI.PLUGIN_ID,
                                    0,
                                    NLS.bind(WorkbenchMessages.ViewFactory_couldNotSave, viewRef.getTitle() ),
                                    e));
                }
            });
        } else {
            IMemento mem = getViewState(ViewFactory.getKey(ref));
            if (mem != null) {
                IMemento child = viewMemento
                        .createChild(IWorkbenchConstants.TAG_VIEW_STATE);
                child.putMemento(mem);
            }
        }
        return viewMemento;
    }

    //	for dynamic UI
    public void restoreViewState(IMemento memento) {
        String compoundId = memento.getString(IWorkbenchConstants.TAG_ID);
        mementoTable.put(compoundId, memento);
    }

    private IMemento getViewState(String key) {
        IMemento memento = (IMemento) mementoTable.get(key);

        if (memento == null) {
            return null;
        }

        return memento.getChild(IWorkbenchConstants.TAG_VIEW_STATE);
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.dynamicHelpers.IExtensionRemovalHandler#removeInstance(org.eclipse.core.runtime.IExtension, java.lang.Object[])
     */
    public void removeInstance(IExtension source, Object[] objects) {
        for (int i = 0; i < objects.length; i++) {
            if (objects[i] instanceof IViewPart) {
                IViewPart part = (IViewPart) objects[i];
                // String primaryViewId = part.getViewSite().getId();
                // String secondaryViewId = part.getViewSite().getSecondaryId();
                // IViewReference viewRef = page.findViewReference(
                // primaryViewId, secondaryViewId);
                // IPerspectiveDescriptor[] descs =
                // page.getOpenedPerspectives();
                // Perspective active = page.getActivePerspective();
                // for (int i = 0; i < descs.length; i++) {
                // Perspective nextPerspective = page.findPerspective(descs[i]);
                //
                // if (nextPerspective == null || active == nextPerspective)
                // continue;
                //
                // page.hideView(nextPerspective, viewRef);
                // }
                page.hideView(part);
            }

        }
    }

}

