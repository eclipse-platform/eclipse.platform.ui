/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.dynamichelpers.ExtensionTracker;
import org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.internal.provisional.action.ICoolBarManager2;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.INavigationHistory;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.ISaveablesLifecycleListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IShowEditorInput;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.SubActionBars;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.internal.StartupThreading.StartupRunnable;
import org.eclipse.ui.internal.contexts.ContextAuthority;
import org.eclipse.ui.internal.dialogs.CustomizePerspectiveDialog;
import org.eclipse.ui.internal.dnd.SwtUtil;
import org.eclipse.ui.internal.intro.IIntroConstants;
import org.eclipse.ui.internal.misc.UIListenerLogging;
import org.eclipse.ui.internal.misc.UIStats;
import org.eclipse.ui.internal.registry.ActionSetRegistry;
import org.eclipse.ui.internal.registry.EditorDescriptor;
import org.eclipse.ui.internal.registry.EditorRegistry;
import org.eclipse.ui.internal.registry.IActionSetDescriptor;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.internal.registry.PerspectiveDescriptor;
import org.eclipse.ui.internal.registry.UIExtensionTracker;
import org.eclipse.ui.internal.tweaklets.WorkbenchImplementation;
import org.eclipse.ui.internal.tweaklets.GrabFocus;
import org.eclipse.ui.internal.tweaklets.TabBehaviour;
import org.eclipse.ui.internal.tweaklets.Tweaklets;
import org.eclipse.ui.internal.util.PrefUtil;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.part.MultiEditor;
import org.eclipse.ui.presentations.IStackPresentationSite;

/**
 * A collection of views and editors in a workbench.
 */
public class WorkbenchPage extends CompatibleWorkbenchPage implements
        IWorkbenchPage {
	
	private static final String ATT_AGGREGATE_WORKING_SET_ID = "aggregateWorkingSetId"; //$NON-NLS-1$
	
    protected WorkbenchWindow window;

    private IAdaptable input;

    private IWorkingSet workingSet;
    
    private AggregateWorkingSet aggregateWorkingSet;

    private Composite composite;
    
    //Could be delete. This information is in the active part list;
    private ActivationList activationList = new ActivationList();

    private EditorManager editorMgr;

    private EditorAreaHelper editorPresentation;

    private ListenerList propertyChangeListeners = new ListenerList();

    private PageSelectionService selectionService = new PageSelectionService(
            this);

    private WorkbenchPagePartList partList = new WorkbenchPagePartList(selectionService);

    private IActionBars actionBars;
    
    private ActionSetManager actionSets;
    
    private ViewFactory viewFactory;

    private PerspectiveList perspList = new PerspectiveList();

    private PerspectiveDescriptor deferredActivePersp;

    private NavigationHistory navigationHistory = new NavigationHistory(this);
    
    private IStickyViewManager stickyViewMan = StickyViewManager.getInstance(this);

    /**
     * If we're in the process of activating a part, this points to the new part.
     * Otherwise, this is null.
     */
    private IWorkbenchPartReference partBeingActivated = null;
    
    /**
     * Contains a list of perspectives that may be dirty due to plugin 
     * installation and removal. 
     */
    private Set dirtyPerspectives = new HashSet();
    
    private IPropertyChangeListener workingSetPropertyChangeListener = new IPropertyChangeListener() {
        /*
         * Remove the working set from the page if the working set is deleted.
         */
        public void propertyChange(PropertyChangeEvent event) {
            String property = event.getProperty();
            if (IWorkingSetManager.CHANGE_WORKING_SET_REMOVE.equals(property)) {
            		if(event.getOldValue().equals(workingSet)) {
						setWorkingSet(null);
					}
            		
            		// room for optimization here
            		List newList = new ArrayList(Arrays.asList(workingSets));
            		if (newList.remove(event.getOldValue())) {
						setWorkingSets((IWorkingSet []) newList
								.toArray(new IWorkingSet [newList.size()]));
					}
            }
        }
    };

    private ActionSwitcher actionSwitcher = new ActionSwitcher();

	private IExtensionTracker tracker;
    
    // Deferral count... delays disposing parts and sending certain events if nonzero
    private int deferCount = 0;
    // Parts waiting to be disposed
    private List pendingDisposals = new ArrayList();
    
	private IExtensionChangeHandler perspectiveChangeHandler = new IExtensionChangeHandler() {

		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.dynamicHelpers.IExtensionChangeHandler#removeExtension(org.eclipse.core.runtime.IExtension, java.lang.Object[])
		 */
		public void removeExtension(IExtension extension, Object[] objects) {
			boolean suggestReset = false;
			for (int i = 0; i < objects.length; i++) {
				if (objects[i] instanceof DirtyPerspectiveMarker) {
					String id = ((DirtyPerspectiveMarker)objects[i]).perspectiveId;
					if (!dirtyPerspectives.remove(id)) {
						dirtyPerspectives.add(id); // otherwise we will be dirty
					}
					PerspectiveDescriptor persp = (PerspectiveDescriptor) getPerspective();
					if (persp == null || persp.hasCustomDefinition()) {
						continue;
					}
					if (persp.getId().equals(id)) {
						suggestReset = true;
					}
				}
			}
			if (suggestReset) {
				suggestReset();
			}
		}
        
        /* (non-Javadoc)
         * @see org.eclipse.core.runtime.dynamicHelpers.IExtensionChangeHandler#addExtension(org.eclipse.core.runtime.dynamicHelpers.IExtensionTracker, org.eclipse.core.runtime.IExtension)
         */
        public void addExtension(IExtensionTracker tracker, IExtension extension) {
            if (WorkbenchPage.this != getWorkbenchWindow().getActivePage()) {
				return;
			}
            
            // Get the current perspective.
            PerspectiveDescriptor persp = (PerspectiveDescriptor) getPerspective();
            if (persp == null) {
				return;
			}
            String currentId = persp.getId();
            IConfigurationElement[] elements = extension.getConfigurationElements();
            boolean suggestReset = false;
            for (int i = 0; i < elements.length; i++) {
                // If any of these refer to the current perspective, output
                // a message saying this perspective will need to be reset
                // in order to see the changes.  For any other case, the
                // perspective extension registry will be rebuilt anyway so
                // just ignore it.
                String id = elements[i].getAttribute(IWorkbenchRegistryConstants.ATT_TARGET_ID);
                if (id == null) {
					continue;
				}
                if (id.equals(currentId) && !persp.hasCustomDefinition()) {
                    suggestReset = true;
                }
                else {
                    dirtyPerspectives.add(id);
                }
                DirtyPerspectiveMarker marker = new DirtyPerspectiveMarker(id);
                tracker.registerObject(extension, marker, IExtensionTracker.REF_STRONG);
            }
            if (suggestReset) {
				suggestReset();
			}

        }
	};
	private IWorkingSet[] workingSets = new IWorkingSet[0];
	private String aggregateWorkingSetId;

	private IExtensionPoint getPerspectiveExtensionPoint() {
		return Platform.getExtensionRegistry().getExtensionPoint(PlatformUI.PLUGIN_ID, IWorkbenchRegistryConstants.PL_PERSPECTIVE_EXTENSIONS);
	}

    /**
     * Manages editor contributions and action set part associations.
     */
    private class ActionSwitcher {
        private IWorkbenchPart activePart;

        private IEditorPart topEditor;

        private ArrayList oldActionSets = new ArrayList();

        /**
         * Updates the contributions given the new part as the active part.
         * 
         * @param newPart
         *            the new active part, may be <code>null</code>
         */
        public void updateActivePart(IWorkbenchPart newPart) {
            if (activePart == newPart) {
				return;
			}

            boolean isNewPartAnEditor = newPart instanceof IEditorPart;
            if (isNewPartAnEditor) {
                String oldId = null;
                if (topEditor != null) {
					oldId = topEditor.getSite().getId();
				}
                String newId = newPart.getSite().getId();

                // if the active part is an editor and the new editor
                // is the same kind of editor, then we don't have to do
                // anything
                if (activePart == topEditor && newId.equals(oldId)) {
                	activePart = newPart;
                	topEditor = (IEditorPart) newPart;
                    return;
                }

                // remove the contributions of the old editor
                // if it is a different kind of editor
                if (oldId != null && !oldId.equals(newId)) {
					deactivateContributions(topEditor, true);
				}

                // if a view was the active part, disable its contributions
                if (activePart != null && activePart != topEditor) {
					deactivateContributions(activePart, true);
				}

                // show (and enable) the contributions of the new editor
                // if it is a different kind of editor or if the
                // old active part was a view
                if (!newId.equals(oldId) || activePart != topEditor) {
					activateContributions(newPart, true);
				}

            } else if (newPart == null) {
                if (activePart != null) {
					// remove all contributions
                    deactivateContributions(activePart, true);
				}
            } else {
                // new part is a view

                // if old active part is a view, remove all contributions,
                // but if old part is an editor only disable
                if (activePart != null) {
					deactivateContributions(activePart,
                            activePart instanceof IViewPart);
				}

                activateContributions(newPart, true);
            }

            ArrayList newActionSets = null;
            if (isNewPartAnEditor
                    || (activePart == topEditor && newPart == null)) {
				newActionSets = calculateActionSets(newPart, null);
			} else {
				newActionSets = calculateActionSets(newPart, topEditor);
			}

            if (!updateActionSets(newActionSets)) {
				updateActionBars();
			}

            if (isNewPartAnEditor) {
                topEditor = (IEditorPart) newPart;
            } else if (activePart == topEditor && newPart == null) {
                // since we removed all the contributions, we clear the top
                // editor
                topEditor = null;
            }

            activePart = newPart;
        }

        /**
         * Updates the contributions given the new part as the topEditor.
         * 
         * @param newEditor
         *            the new top editor, may be <code>null</code>
         */
        public void updateTopEditor(IEditorPart newEditor) {
            if (topEditor == newEditor) {
				return;
			}

            if (activePart == topEditor) {
            	updateActivePart(newEditor);
            	return;
            }
            
            String oldId = null;
            if (topEditor != null) {
				oldId = topEditor.getSite().getId();
			}
            String newId = null;
            if (newEditor != null) {
				newId = newEditor.getSite().getId();
			}
            if (oldId == null ? newId == null : oldId.equals(newId)) {
                // we don't have to change anything
                topEditor = newEditor;
                return;
            }

            // Remove the contributions of the old editor
            if (topEditor != null) {
				deactivateContributions(topEditor, true);
			}

            // Show (disabled) the contributions of the new editor
            if (newEditor != null) {
				activateContributions(newEditor, false);
			}

            ArrayList newActionSets = calculateActionSets(activePart, newEditor);
            if (!updateActionSets(newActionSets)) {
				updateActionBars();
			}

            topEditor = newEditor;
        }

        /**
         * Activates the contributions of the given part. If <code>enable</code>
         * is <code>true</code> the contributions are visible and enabled,
         * otherwise they are disabled.
         * 
         * @param part
         *            the part whose contributions are to be activated
         * @param enable
         *            <code>true</code> the contributions are to be enabled,
         *            not just visible.
         */
        private void activateContributions(IWorkbenchPart part, boolean enable) {
            PartSite site = (PartSite) part.getSite();
            site.activateActionBars(enable);
        }

        /**
         * Deactivates the contributions of the given part. If <code>remove</code>
         * is <code>true</code> the contributions are removed, otherwise they
         * are disabled.
         * 
         * @param part
         *            the part whose contributions are to be deactivated
         * @param remove
         *            <code>true</code> the contributions are to be removed,
         *            not just disabled.
         */
        private void deactivateContributions(IWorkbenchPart part, boolean remove) {
            PartSite site = (PartSite) part.getSite();
            site.deactivateActionBars(remove);
        }

        /**
         * Calculates the action sets to show for the given part and editor
         * 
         * @param part
         *            the active part, may be <code>null</code>
         * @param editor
         *            the current editor, may be <code>null</code>, may be
         *            the active part
         * @return the new action sets
         */
        private ArrayList calculateActionSets(IWorkbenchPart part,
                IEditorPart editor) {
            ArrayList newActionSets = new ArrayList();
            if (part != null) {
                IActionSetDescriptor[] partActionSets = WorkbenchPlugin
                        .getDefault().getActionSetRegistry().getActionSetsFor(
                                part.getSite().getId());
                for (int i = 0; i < partActionSets.length; i++) {
                    newActionSets.add(partActionSets[i]);
                }
            }
            if (editor != null && editor != part) {
                IActionSetDescriptor[] editorActionSets = WorkbenchPlugin
                        .getDefault().getActionSetRegistry().getActionSetsFor(
                                editor.getSite().getId());
                for (int i = 0; i < editorActionSets.length; i++) {
                    newActionSets.add(editorActionSets[i]);
                }
            }
            return newActionSets;
        }

        /**
         * Updates the actions we are showing for the active part and current
         * editor.
         * 
         * @param newActionSets
         *            the action sets to show
         * @return <code>true</code> if the action sets changed
         */
        private boolean updateActionSets(ArrayList newActionSets) {
			if (oldActionSets.equals(newActionSets)) {
				return false;
			}

			IContextService service = (IContextService) window
					.getService(IContextService.class);
			try {
				service.activateContext(ContextAuthority.DEFER_EVENTS);

				// show the new
				for (int i = 0; i < newActionSets.size(); i++) {
					actionSets.showAction((IActionSetDescriptor) newActionSets
							.get(i));
				}

				// hide the old
				for (int i = 0; i < oldActionSets.size(); i++) {
					actionSets.hideAction((IActionSetDescriptor) oldActionSets
							.get(i));
				}

				oldActionSets = newActionSets;

			} finally {
				service.activateContext(ContextAuthority.SEND_EVENTS);
			}
			Perspective persp = getActivePerspective();
			if (persp == null) {
				return false;
			}

			window.updateActionSets(); // this calls updateActionBars
			window.firePerspectiveChanged(WorkbenchPage.this, getPerspective(),
					CHANGE_ACTION_SET_SHOW);
			return true;
		}

    }

    /**
	 * Constructs a new page with a given perspective and input.
	 * 
	 * @param w
	 *            the parent window
	 * @param layoutID
	 *            must not be <code>null</code>
	 * @param input
	 *            the page input
	 * @throws WorkbenchException
	 *             on null layout id
	 */
    public WorkbenchPage(WorkbenchWindow w, String layoutID, IAdaptable input)
            throws WorkbenchException {
        super();
        if (layoutID == null) {
			throw new WorkbenchException(WorkbenchMessages.WorkbenchPage_UndefinedPerspective);
		}
        init(w, layoutID, input, true);
    }

    /**
     * Constructs a page. <code>restoreState(IMemento)</code> should be
     * called to restore this page from data stored in a persistance file.
     * 
     * @param w
     *            the parent window
     * @param input
     *            the page input
     * @throws WorkbenchException 
     */
    public WorkbenchPage(WorkbenchWindow w, IAdaptable input)
            throws WorkbenchException {
        super();
        init(w, null, input, false);
    }

    /**
     * Activates a part. The part will be brought to the front and given focus.
     * 
     * @param part
     *            the part to activate
     */
    public void activate(IWorkbenchPart part) {
        // Sanity check.
        if (!certifyPart(part)) {
			return;
		}

        if (window.isClosing()) {
			return;
		}

        if (composite!=null && composite.isVisible()
        		&& !((GrabFocus)Tweaklets.get(GrabFocus.KEY)).grabFocusAllowed(part)) {
        	return;
        }
        
        // If zoomed, unzoom.
        zoomOutIfNecessary(part);

        if (part instanceof MultiEditor) {
            part = ((MultiEditor) part).getActiveEditor();
        }
        // Activate part.
        //if (window.getActivePage() == this) {
        IWorkbenchPartReference ref = getReference(part);
        internalBringToTop(ref);
        setActivePart(part);
    }

    /**
     * Activates a part. The part is given focus, the pane is hilighted.
     */
    private void activatePart(final IWorkbenchPart part) {
        Platform.run(new SafeRunnable(WorkbenchMessages.WorkbenchPage_ErrorActivatingView) { 
                    public void run() {
                        if (part != null) {
                            //part.setFocus();
                            PartPane pane = getPane(part);
                            pane.setFocus();
                            PartSite site = (PartSite) part.getSite();
                            pane.showFocus(true);
                            updateTabList(part);
                            SubActionBars bars = (SubActionBars) site
                                    .getActionBars();
                            bars.partChanged(part);
                        }
                    }
                });
    }

    /**
     * Add a fast view.
     */
    public void addFastView(IViewReference ref) {
        Perspective persp = getActivePerspective();
        if (persp == null) {
			return;
		}
        
        persp.getFastViewManager().addViewReference(FastViewBar.FASTVIEWBAR_ID, -1, ref, true);
    }
    
    /**
     * Add a fast view.
     */
    public void makeFastView(IViewReference ref) {
        Perspective persp = getActivePerspective();
        if (persp == null) {
			return;
		}

        FastViewManager fvm = persp.getFastViewManager();
        if (fvm.isFastView(ref)) {
            return;
        }
        
        // Do real work.
        persp.makeFastView(ref);

        updateActivePart();
        
        // The view is now invisible.
        // If it is active then deactivate it.

        // Notify listeners.
        window.firePerspectiveChanged(this, getPerspective(), ref,
                CHANGE_FAST_VIEW_ADD);
        window.firePerspectiveChanged(this, getPerspective(),
                CHANGE_FAST_VIEW_ADD);
    }

    /**
     * Adds an IPartListener to the part service.
     */
    public void addPartListener(IPartListener l) {
        partList.getPartService().addPartListener(l);
    }

    /**
     * Adds an IPartListener to the part service.
     */
    public void addPartListener(IPartListener2 l) {
        partList.getPartService().addPartListener(l);
    }

    /**
     * Implements IWorkbenchPage
     * 
     * @see org.eclipse.ui.IWorkbenchPage#addPropertyChangeListener(IPropertyChangeListener)
     * @since 2.0
     * @deprecated individual views should store a working set if needed and
     *             register a property change listener directly with the
     *             working set manager to receive notification when the view
     *             working set is removed.
     */
    public void addPropertyChangeListener(IPropertyChangeListener listener) {
        propertyChangeListeners.add(listener);
    }

    /*
     * (non-Javadoc) Method declared on ISelectionListener.
     */
    public void addSelectionListener(ISelectionListener listener) {
        selectionService.addSelectionListener(listener);
    }

    /*
     * (non-Javadoc) Method declared on ISelectionListener.
     */
    public void addSelectionListener(String partId, ISelectionListener listener) {
        selectionService.addSelectionListener(partId, listener);
    }

    /*
     * (non-Javadoc) Method declared on ISelectionListener.
     */
    public void addPostSelectionListener(ISelectionListener listener) {
        selectionService.addPostSelectionListener(listener);
    }

    /*
     * (non-Javadoc) Method declared on ISelectionListener.
     */
    public void addPostSelectionListener(String partId,
            ISelectionListener listener) {
        selectionService.addPostSelectionListener(partId, listener);
    }
    
    private ILayoutContainer getContainer(IWorkbenchPart part) {
        PartPane pane = getPane(part);
        if (pane == null) {
            return null;
        }
        
        return pane.getContainer();
    }

    private ILayoutContainer getContainer(IWorkbenchPartReference part) {
        PartPane pane = getPane(part);
        if (pane == null) {
            return null;
        }
        
        return pane.getContainer();
    }
    
    private PartPane getPane(IWorkbenchPart part) {
        if (part == null) {
            return null;
        }
        return getPane(getReference(part));
    }
    
    private PartPane getPane(IWorkbenchPartReference part) {
        if (part == null) {
            return null;
        }
        
        return ((WorkbenchPartReference)part).getPane();
    }

    
    /**
     * Brings a part to the front of its stack. Does not update the active part or
     * active editor. This should only be called if the caller knows that the part
     * is not in the same stack as the active part or active editor, or if the caller
     * is prepared to update activation after the call.
     *
     * @param part
     */
    private boolean internalBringToTop(IWorkbenchPartReference part) {

        boolean broughtToTop = false;
        
        // Move part.
        if (part instanceof IEditorReference) {
            ILayoutContainer container = getContainer(part);
            if (container instanceof PartStack) {
                PartStack stack = (PartStack)container;
                PartPane newPart = getPane(part);
                if (stack.getSelection() != newPart) {
                    stack.setSelection(newPart);
                }
                broughtToTop = true;
            }
        } else if (part instanceof IViewReference) {
            Perspective persp = getActivePerspective();
            if (persp != null) {
                broughtToTop = persp.bringToTop((IViewReference)part);
            }
        }
        
        // Ensure that this part is considered the most recently activated part
        // in this stack
        activationList.bringToTop(part);
        
        return broughtToTop;
    }

    
    /**
     * Moves a part forward in the Z order of a perspective so it is visible.
     * If the part is in the same stack as the active part, the new part is
     * activated.
     * 
     * @param part
     *            the part to bring to move forward
     */
    public void bringToTop(IWorkbenchPart part) {
        // Sanity check.
        Perspective persp = getActivePerspective();
        if (persp == null || !certifyPart(part)) {
			return;
		}
        
        if (!((GrabFocus)Tweaklets.get(GrabFocus.KEY)).grabFocusAllowed(part)) {
        	return;
        }

        String label = null; // debugging only
        if (UIStats.isDebugging(UIStats.BRING_PART_TO_TOP)) {
            label = part != null ? part.getTitle() : "none"; //$NON-NLS-1$
        }
        
        try {
            UIStats.start(UIStats.BRING_PART_TO_TOP, label);
            
            IWorkbenchPartReference ref = getReference(part);
            ILayoutContainer activeEditorContainer = getContainer(getActiveEditor());
            ILayoutContainer activePartContainer = getContainer(getActivePart());
            ILayoutContainer newPartContainer = getContainer(part);
            
            if (newPartContainer == activePartContainer) {
                makeActive(ref);
            } else if (newPartContainer == activeEditorContainer) {
                if (ref instanceof IEditorReference) {
                	if (part!=null) {
                    	IWorkbenchPartSite site = part.getSite();
						if (site instanceof PartSite) {
							ref = ((PartSite) site).getPane()
									.getPartReference();
						}
                	}
                    makeActiveEditor((IEditorReference)ref);
                } else {
                    makeActiveEditor(null);
                }
            } else {
                internalBringToTop(ref);
                if (ref != null) {
                    partList.firePartBroughtToTop(ref);
                }
            }
        } finally {
            UIStats.end(UIStats.BRING_PART_TO_TOP, part, label);
        }
    }

    /**
     * Resets the layout for the perspective. The active part in the old layout
     * is activated in the new layout for consistent user context.
     * 
     * Assumes the busy cursor is active.
     */
    private void busyResetPerspective() {

        ViewIntroAdapterPart introViewAdapter = ((WorkbenchIntroManager) getWorkbenchWindow()
                .getWorkbench().getIntroManager()).getViewIntroAdapterPart();
        PartPane introPane = null;
        boolean introFullScreen = false;
        if (introViewAdapter != null) {
            introPane = ((PartSite) introViewAdapter.getSite()).getPane();
            introViewAdapter.setHandleZoomEvents(false);
            introFullScreen = introPane.isZoomed();
        }

        //try to prevent intro flicker.
        if (introFullScreen) {
			window.getShell().setRedraw(false);
		}

        try {

            // Always unzoom
            if (isZoomed()) {
				zoomOut();
			}

            // Get the current perspective.
            // This describes the working layout of the page and differs from
            // the original template.
            Perspective oldPersp = getActivePerspective();

            // Map the current perspective to the original template.
            // If the original template cannot be found then it has been deleted.
            // In that case just return. (PR#1GDSABU).
            IPerspectiveRegistry reg = WorkbenchPlugin.getDefault()
                    .getPerspectiveRegistry();
            PerspectiveDescriptor desc = (PerspectiveDescriptor) reg
                    .findPerspectiveWithId(oldPersp.getDesc().getId());
            if (desc == null) {
				desc = (PerspectiveDescriptor) reg
                        .findPerspectiveWithId(((PerspectiveDescriptor) oldPersp
                                .getDesc()).getOriginalId());
			}
            if (desc == null) {
				return;
			}

            // Notify listeners that we are doing a reset.
            window.firePerspectiveChanged(this, desc, CHANGE_RESET);

            // Create new persp from original template.
            // Suppress the perspectiveOpened and perspectiveClosed events otherwise it looks like two
            // instances of the same perspective are open temporarily (see bug 127470).
            Perspective newPersp = createPerspective(desc, false);
            if (newPersp == null) {
                // We're not going through with the reset, so it is complete.
                window
                        .firePerspectiveChanged(this, desc,
                                CHANGE_RESET_COMPLETE);
                return;
            }

            // Update the perspective list and shortcut
            perspList.swap(oldPersp, newPersp);

            // Install new persp.
            setPerspective(newPersp);

            // Destroy old persp.
            disposePerspective(oldPersp, false);

            // Update the Coolbar layout.
            resetToolBarLayout();

            // restore the maximized intro
            if (introViewAdapter != null) {
                try {
                    // ensure that the intro is visible in the new perspective
                    showView(IIntroConstants.INTRO_VIEW_ID);
                    if (introFullScreen) {
						toggleZoom(introPane.getPartReference());
					}
                } catch (PartInitException e) {
                    WorkbenchPlugin.log("Could not restore intro", //$NON-NLS-1$
                            WorkbenchPlugin.getStatus(e));
                } finally {
                    // we want the intro back to a normal state before we fire the event
                    introViewAdapter.setHandleZoomEvents(true);
                }
            }
            // Notify listeners that we have completed our reset.
            window.firePerspectiveChanged(this, desc, CHANGE_RESET_COMPLETE);
        } finally {
            // reset the handling of zoom events (possibly for the second time) in case there was 
            // an exception thrown
            if (introViewAdapter != null) {
				introViewAdapter.setHandleZoomEvents(true);
			}

            if (introFullScreen) {
				window.getShell().setRedraw(true);
			}
        }

    }

    /**
     * Implements <code>setPerspective</code>.
     * 
     * Assumes that busy cursor is active.
     * 
     * @param desc
     *            identifies the new perspective.
     */
    private void busySetPerspective(IPerspectiveDescriptor desc) {
        // Create new layout.
        String label = desc.getId(); // debugging only
        Perspective newPersp = null;
        try {
            UIStats.start(UIStats.SWITCH_PERSPECTIVE, label);
            PerspectiveDescriptor realDesc = (PerspectiveDescriptor) desc;
            newPersp = findPerspective(realDesc);
            if (newPersp == null) {
                newPersp = createPerspective(realDesc, true);
                if (newPersp == null) {
					return;
				}
            }

            // Change layout.
            setPerspective(newPersp);
        } finally {
            UIStats.end(UIStats.SWITCH_PERSPECTIVE, desc.getId(), label);
        }
    }

    /**
     * Shows a view.
     * 
     * Assumes that a busy cursor is active.
     */
    protected IViewPart busyShowView(String viewID, String secondaryID, int mode)
            throws PartInitException {
        Perspective persp = getActivePerspective();
        if (persp == null) {
			return null;
		}

        // If this view is already visible just return.
        IViewReference ref = persp.findView(viewID, secondaryID);
        IViewPart view = null;
        if (ref != null) {
			view = ref.getView(true);
		}
        if (view != null) {
            busyShowView(view, mode);
            return view;
        }

        // Show the view.
        view = persp.showView(viewID, secondaryID);
        if (view != null) {
            busyShowView(view, mode);
            
            IWorkbenchPartReference partReference = getReference(view);
            PartPane partPane = getPane(partReference);
            partPane.setInLayout(true);

            window.firePerspectiveChanged(this, getPerspective(),
                    partReference, CHANGE_VIEW_SHOW);
            window.firePerspectiveChanged(this, getPerspective(),
                    CHANGE_VIEW_SHOW);
        }
        return view;
    }

    /*
     * Performs showing of the view in the given mode.
     */
    private void busyShowView(IViewPart part, int mode) {
        if (!((GrabFocus)Tweaklets.get(GrabFocus.KEY)).grabFocusAllowed(part)) {
        	return;
        }
        if (mode == VIEW_ACTIVATE) {
			activate(part);
		} else if (mode == VIEW_VISIBLE) {
            IWorkbenchPartReference ref = getActivePartReference();
            // if there is no active part or it's not a view, bring to top
            if (ref == null || !(ref instanceof IViewReference)) {
				bringToTop(part);
			} else {
                // otherwise check to see if the we're in the same stack as the active view
                IViewReference activeView = (IViewReference) ref;
                IViewReference[] viewStack = getViewReferenceStack(part);
                for (int i = 0; i < viewStack.length; i++) {
                    if (viewStack[i].equals(activeView)) {
						return;
					}
                }
                bringToTop(part);
            }
        }
    }

    /**
     * Returns whether a part exists in the current page.
     */
    private boolean certifyPart(IWorkbenchPart part) {
        //Workaround for bug 22325
        if (part != null && !(part.getSite() instanceof PartSite)) {
			return false;
		}

        if (part instanceof IEditorPart) {
            IEditorReference ref = (IEditorReference) getReference(part);
            return ref != null && getEditorManager().containsEditor(ref);
        }
        if (part instanceof IViewPart) {
            Perspective persp = getActivePerspective();
            return persp != null && persp.containsView((IViewPart) part);
        }
        return false;
    }

    /**
     * Closes the perspective.
     */
    public boolean close() {
        final boolean[] ret = new boolean[1];
        BusyIndicator.showWhile(null, new Runnable() {
            public void run() {
                ret[0] = window.closePage(WorkbenchPage.this, true);
            }
        });
        return ret[0];
    }

    /**
     * See IWorkbenchPage
     */
    public boolean closeAllSavedEditors() {
        // get the Saved editors
        IEditorReference editors[] = getEditorReferences();
        IEditorReference savedEditors[] = new IEditorReference[editors.length];
        int j = 0;
        for (int i = 0; i < editors.length; i++) {
            IEditorReference editor = editors[i];
            if (!editor.isDirty()) {
                savedEditors[j++] = editor;
            }
        }
        //there are no unsaved editors
        if (j == 0) {
			return true;
		}
        IEditorReference[] newSaved = new IEditorReference[j];
        System.arraycopy(savedEditors, 0, newSaved, 0, j);
        return closeEditors(newSaved, false);
    }

    /**
     * See IWorkbenchPage
     */
    public boolean closeAllEditors(boolean save) {
        return closeEditors(getEditorReferences(), save);
    }

    private void updateActivePart() {
        
        if (isDeferred()) {
            return;
        }
        
        IWorkbenchPartReference oldActivePart = partList.getActivePartReference();
        IWorkbenchPartReference oldActiveEditor = partList.getActiveEditorReference();
        IWorkbenchPartReference newActivePart = null;
        IEditorReference newActiveEditor = null;
        
        if (!window.isClosing()) {
            // If an editor is active, try to keep an editor active
            if (oldActivePart == oldActiveEditor) {
                newActiveEditor = (IEditorReference)activationList.getActiveReference(true);
                newActivePart = newActiveEditor;
                if (newActivePart == null) {
                    // Only activate a non-editor if there's no editors left
                    newActivePart = activationList.getActiveReference(false);
                }
            } else {
                // If a non-editor is active, activate whatever was activated most recently
                newActivePart = activationList.getActiveReference(false);
                
                if (newActivePart instanceof IEditorReference) {
                    // If that happens to be an editor, make it the active editor as well
                    newActiveEditor = (IEditorReference)newActivePart;
                } else {
                    // Otherwise, select whatever editor was most recently active
                    newActiveEditor = (IEditorReference)activationList.getActiveReference(true);
                }   
            }
        }

        if (newActiveEditor != oldActiveEditor) {
            makeActiveEditor(newActiveEditor);
        }
        
        if (newActivePart != oldActivePart) {
            makeActive(newActivePart);
        }
    }
    
    /**
     * Makes the given part active. Brings it in front if necessary. Permits null 
     * (indicating that no part should be active).
     * 
     * @since 3.1 
     *
     * @param ref new active part (or null)
     */
    private void makeActive(IWorkbenchPartReference ref) {
        if (ref == null) {
            setActivePart(null);
        } else {
            IWorkbenchPart newActive = ref.getPart(true);
            if (newActive == null) {
                setActivePart(null);
            } else {
                activate(newActive);
            }
        }
    }
    
    /**
     * Makes the given editor active. Brings it to front if necessary. Permits <code>null</code> 
     * (indicating that no editor is active).
     * 
     * @since 3.1 
     *
     * @param ref the editor to make active, or <code>null</code> for no active editor
     */
    private void makeActiveEditor(IEditorReference ref) {
        if (ref == getActiveEditorReference()) {
            return;
        }
        
        IEditorPart part = (ref == null) ? null : ref.getEditor(true);
        
        if (part != null) {
            editorMgr.setVisibleEditor(ref, false);
            navigationHistory.markEditor(part);
        }
        
        actionSwitcher.updateTopEditor(part);

        if (ref != null) {
            activationList.bringToTop(getReference(part));
        }
        
        partList.setActiveEditor(ref);
    }
    
    /**
     * See IWorkbenchPage
     */
    public boolean closeEditors(IEditorReference[] refArray, boolean save) {
        if (refArray.length == 0) {
            return true;
        }
        
        // Check if we're being asked to close any parts that are already closed or cannot
        // be closed at this time
        ArrayList toClose = new ArrayList();
        for (int i = 0; i < refArray.length; i++) {
            IEditorReference reference = refArray[i];
            
            // If we're in the middle of creating this part, this is a programming error. Abort the entire
            // close operation. This usually occurs if someone tries to open a dialog in a method that
            // isn't allowed to do so, and a *syncExec tries to close the part. If this shows up in a log
            // file with a dialog's event loop on the stack, then the code that opened the dialog is usually
            // at fault.
            if (reference == partBeingActivated) {
                WorkbenchPlugin.log(new RuntimeException("WARNING: Blocked recursive attempt to close part "  //$NON-NLS-1$
                        + partBeingActivated.getId() + " while still in the middle of activating it")); //$NON-NLS-1$
                return false;
            }
            
            if(reference instanceof WorkbenchPartReference) {
                WorkbenchPartReference ref = (WorkbenchPartReference) reference;
                
                // If we're being asked to close a part that is disposed (ie: already closed),
                // skip it and proceed with closing the remaining parts.
                if (ref.isDisposed()) {
                    continue;
                }
            }
            
            toClose.add(reference);
        }
        
        IEditorReference[] editorRefs = (IEditorReference[]) toClose.toArray(new IEditorReference[toClose.size()]);
        
        // notify the model manager before the close
        List partsToClose = new ArrayList();
        for (int i = 0; i < editorRefs.length; i++) {
            IEditorPart refPart = editorRefs[i].getEditor(false);
            if (refPart != null) {
            	partsToClose.add(refPart);
            }
        }
        SaveablesList modelManager = null;
        Object postCloseInfo = null;
        if(partsToClose.size()>0) {
        	modelManager = (SaveablesList) getWorkbenchWindow().getService(ISaveablesLifecycleListener.class);
        	// this may prompt for saving and return null if the user canceled:
        	postCloseInfo = modelManager.preCloseParts(partsToClose, save, getWorkbenchWindow());
        	if (postCloseInfo==null) {
        		return false;
        	}
        }

        // Fire pre-removal changes 
        for (int i = 0; i < editorRefs.length; i++) {
            IEditorReference ref = editorRefs[i];
            
            // Notify interested listeners before the close
            window.firePerspectiveChanged(this, getPerspective(), ref,
                    CHANGE_EDITOR_CLOSE);
            
        }        
        
        deferUpdates(true);
        try {        
        	if(modelManager!=null) {
            	modelManager.postClose(postCloseInfo);
            }
        	
	        // Close all editors.
	        for (int i = 0; i < editorRefs.length; i++) {
	            IEditorReference ref = editorRefs[i];
	            
	            // Remove editor from the presentation
                editorPresentation.closeEditor(ref);
	            
                partRemoved((WorkbenchPartReference)ref);                
	        }
        } finally {
            deferUpdates(false);
        }
                        
        // Notify interested listeners after the close
        window.firePerspectiveChanged(this, getPerspective(),
                CHANGE_EDITOR_CLOSE);
        
        // Return true on success.
        return true;
    }
    
    /**
     * Enables or disables listener notifications. This is used to delay listener notifications until the
     * end of a public method.
     * 
     * @param shouldDefer
     */
    private void deferUpdates(boolean shouldDefer) {
        if (shouldDefer) {
            if (deferCount == 0) {
                startDeferring();
            }
            deferCount++;
        } else {
            deferCount--;
            if (deferCount == 0) {
                handleDeferredEvents();
            }
        }
    }
    
    private void startDeferring() {
        editorPresentation.getLayoutPart().deferUpdates(true);
    }

    private void handleDeferredEvents() {
        editorPresentation.getLayoutPart().deferUpdates(false);
        updateActivePart();
        WorkbenchPartReference[] disposals = (WorkbenchPartReference[]) pendingDisposals.toArray(new WorkbenchPartReference[pendingDisposals.size()]);
        pendingDisposals.clear();
        for (int i = 0; i < disposals.length; i++) {
            WorkbenchPartReference reference = disposals[i];
            disposePart(reference);
        }
        
    }
    
    private boolean isDeferred() {
        return deferCount > 0;
    }

    /**
     * See IWorkbenchPage#closeEditor
     */
    public boolean closeEditor(IEditorReference editorRef, boolean save) {
        return closeEditors(new IEditorReference[] {editorRef}, save);
    }

    /**
     * See IWorkbenchPage#closeEditor
     */
    public boolean closeEditor(IEditorPart editor, boolean save) {
        IWorkbenchPartReference ref = getReference(editor);
        if (ref instanceof IEditorReference) {
        	return closeEditors(new IEditorReference[] {(IEditorReference) ref}, save);
        }
        return false;
    }

    /**
     * @see IWorkbenchPage#closePerspective(IPerspectiveDescriptor, boolean, boolean)
     */
    public void closePerspective(IPerspectiveDescriptor desc, boolean saveParts, boolean closePage) {
        Perspective persp = findPerspective(desc);
        if (persp != null) {
			closePerspective(persp, saveParts, closePage);
		}
    }

    /**
	 * Closes the specified perspective. If last perspective, then entire page
	 * is closed.
	 * 
	 * @param persp
	 *            the perspective to be closed
	 * @param saveParts
	 *            whether the parts that are being closed should be saved
	 *            (editors if last perspective, views if not shown in other
	 *            parspectives)
	 */
    /* package */
    void closePerspective(Perspective persp, boolean saveParts, boolean closePage) {

        // Always unzoom
        if (isZoomed()) {
			zoomOut();
		}

        List partsToSave = new ArrayList();
        List viewsToClose = new ArrayList();
        // collect views that will go away and views that are dirty
        IViewReference[] viewReferences = persp.getViewReferences();
        for (int i = 0; i < viewReferences.length; i++) {
			IViewReference reference = viewReferences[i];
	        if (getViewFactory().getReferenceCount(reference) == 1) {
	        	IViewPart viewPart = reference.getView(false);
	        	if (viewPart != null) {
	        		viewsToClose.add(viewPart);
	        		if (saveParts && reference.isDirty()) {
	        			partsToSave.add(viewPart);
	        		}
	        	}
	        }
		}
        if (saveParts && perspList.size() == 1) {
        	// collect editors that are dirty
        	IEditorReference[] editorReferences = getEditorReferences();
        	for (int i = 0; i < editorReferences.length; i++) {
				IEditorReference reference = editorReferences[i];
					if (reference.isDirty()) {
						IEditorPart editorPart = reference.getEditor(false);
						if (editorPart != null) {
							partsToSave.add(editorPart);
						}
					}
			}
        }
        if (saveParts && !partsToSave.isEmpty()) {
        	if (!EditorManager.saveAll(partsToSave, true, true, false, window)) {
        		// user canceled
        		return;
        	}
	    }
        // Close all editors on last perspective close
        if (perspList.size() == 1 && getEditorManager().getEditorCount() > 0) {
            // Close all editors
            if (!closeAllEditors(false)) {
				return;
			}
        }
        
        // closeAllEditors already notified the saveables list about the editors.
        SaveablesList saveablesList = (SaveablesList) getWorkbenchWindow().getWorkbench().getService(ISaveablesLifecycleListener.class);
        // we took care of the saving already, so pass in false (postCloseInfo will be non-null)
        Object postCloseInfo = saveablesList.preCloseParts(viewsToClose, false, getWorkbenchWindow());
        saveablesList.postClose(postCloseInfo);

        // Dispose of the perspective
        boolean isActive = (perspList.getActive() == persp);
        if (isActive) {
			setPerspective(perspList.getNextActive());
		}
        disposePerspective(persp, true);
        if (closePage && perspList.size() == 0) {
			close();
		}
    }

    /**
     * Forces all perspectives on the page to zoom out.
     */
    public void unzoomAllPerspectives() {
    	for (Iterator perspIter = perspList.iterator(); perspIter.hasNext();) {
			Perspective persp = (Perspective) perspIter.next();
			persp.getPresentation().forceNoZoom();
		}
    }
    
    /**
     * @see IWorkbenchPage#closeAllPerspectives(boolean, boolean)
     */
    public void closeAllPerspectives(boolean saveEditors, boolean closePage) {

        if (perspList.isEmpty()) {
			return;
		}

        // Always unzoom
        if (isZoomed()) {
			zoomOut();
		}

        if(saveEditors) {
        	if (!saveAllEditors(true)) {
        		return;
        	}
        }
        // Close all editors
        if (!closeAllEditors(false)) {
			return;
		}

        // Deactivate the active perspective and part
        setPerspective((Perspective) null);

        // Close each perspective in turn
        PerspectiveList oldList = perspList;
        perspList = new PerspectiveList();
        Iterator itr = oldList.iterator();
        while (itr.hasNext()) {
			closePerspective((Perspective) itr.next(), false, false);
		}
        if (closePage) {
            close();
        }
    }

    /**
     * Creates the client composite.
     */
    private void createClientComposite() {
        final Composite parent = window.getPageComposite();
        StartupThreading.runWithoutExceptions(new StartupRunnable() {

			public void runWithException() {
				composite = new Composite(parent, SWT.NONE);
				composite.setVisible(false); // Make visible on activate.
				// force the client composite to be layed out
				parent.layout();
			}
		});
       
    }

    /**
     * Creates a new view set. Return null on failure.
     * 
     * @param desc the perspective descriptor
     * @param notify whether to fire a perspective opened event
     */
    private Perspective createPerspective(PerspectiveDescriptor desc, boolean notify) {
        String label = desc.getId(); // debugging only
        try {
            UIStats.start(UIStats.CREATE_PERSPECTIVE, label);
            Perspective persp = ((WorkbenchImplementation) Tweaklets
    				.get(WorkbenchImplementation.KEY)).createPerspective(desc, this);
            perspList.add(persp);
            if (notify) {
            	window.firePerspectiveOpened(this, desc);
            }
            //if the perspective is fresh and uncustomzied then it is not dirty
            //no reset will be prompted for
            if (!desc.hasCustomDefinition()) {
				dirtyPerspectives.remove(desc.getId());
			}
            return persp;
        } catch (WorkbenchException e) {
            if (!((Workbench) window.getWorkbench()).isStarting()) {
                MessageDialog
                        .openError(
                                window.getShell(),
                                WorkbenchMessages.Error, 
                                NLS.bind(WorkbenchMessages.Workbench_showPerspectiveError,desc.getId() )); 
            }
            return null;
        } finally {
            UIStats.end(UIStats.CREATE_PERSPECTIVE, desc.getId(), label);
        }
    }

    /**
     * This is called by child objects after a part has been added to the page.
     * The page will in turn notify its listeners. 
     */
    /* package */ void partAdded(WorkbenchPartReference ref) {
        activationList.add(ref);
        partList.addPart(ref);
        updateActivePart();
    }
    
    /**
     * This is called by child objects after a part has been added to the page.
     * The part will be queued for disposal after all listeners have been notified
     */
    /* package */ void partRemoved(WorkbenchPartReference ref) {
        activationList.remove(ref);
        disposePart(ref);
    }
    
    private void disposePart(WorkbenchPartReference ref) {
        if (isDeferred()) {
            pendingDisposals.add(ref);
        } else {
            partList.removePart(ref);
            ref.dispose();
        }
    }
    
    /**
     * Deactivates a part. The pane is unhilighted.
     */
    private void deactivatePart(IWorkbenchPart part) {
        if (part != null) {
            PartSite site = (PartSite) part.getSite();
            site.getPane().showFocus(false);
        }
    }
    
	/**
	 * Detaches a view from the WorkbenchWindow.
	 */
	public void detachView(IViewReference ref){
		Perspective persp = getActivePerspective();
		if(persp == null) {
			return;
		}
		
		PerspectiveHelper presentation = persp.getPresentation();		
		presentation.detachPart(ref);
	}
	
	/**
	 * Removes a detachedwindow. 
	 */
	public void attachView(IViewReference ref){
  		PerspectiveHelper presentation = getPerspectivePresentation();
   		presentation.attachPart(ref);
	}

    /**
     * Cleanup.
     */
    public void dispose() {

        // Always unzoom
        if (isZoomed()) {
			zoomOut();
		}

        makeActiveEditor(null);
        makeActive(null);
        
        // Close and dispose the editors.
        closeAllEditors(false);
        
        // Need to make sure model data is cleaned up when the page is
		// disposed. Collect all the views on the page and notify the
		// saveable list of a pre/post close. This will free model data.
		IWorkbenchPartReference[] partsToClose = getOpenParts();
		List dirtyParts = new ArrayList(partsToClose.length);
		for (int i = 0; i < partsToClose.length; i++) {
			IWorkbenchPart part = partsToClose[i].getPart(false);
			if (part != null && part instanceof IViewPart) {
				dirtyParts.add(part);
			}
		}
		SaveablesList saveablesList = (SaveablesList) getWorkbenchWindow().getWorkbench().getService(ISaveablesLifecycleListener.class);
		Object postCloseInfo = saveablesList.preCloseParts(dirtyParts, false,getWorkbenchWindow());
		saveablesList.postClose(postCloseInfo);

        // Get rid of perspectives. This will close the views.
        Iterator itr = perspList.iterator();
        while (itr.hasNext()) {
            Perspective perspective = (Perspective) itr.next();
            window.firePerspectiveClosed(this, perspective.getDesc());
            perspective.dispose();
        }
        perspList = new PerspectiveList();

        // Capture views.
        IViewReference refs[] = viewFactory.getViews();

        if (refs.length > 0) {
            // Dispose views.
            for (int i = 0; i < refs.length; i++) {
                final WorkbenchPartReference ref = (WorkbenchPartReference) refs[i];
                //partList.removePart(ref);
                //firePartClosed(refs[i]);
                Platform.run(new SafeRunnable() {
                    public void run() {
//                        WorkbenchPlugin.log(new Status(IStatus.WARNING, WorkbenchPlugin.PI_WORKBENCH, 
//                                Status.OK, "WorkbenchPage leaked a refcount for view " + ref.getId(), null));  //$NON-NLS-1$//$NON-NLS-2$
                        
                        ref.dispose();
                    }
    
                    public void handleException(Throwable e) {
                    }
                });
            }
        }
        
        activationList = new ActivationList();

        // Get rid of editor presentation.
        editorPresentation.dispose();

        // Get rid of composite.
        composite.dispose();

        navigationHistory.dispose();

        stickyViewMan.clear();
        
        if (tracker != null) {
			tracker.close();
		}
        
        // if we're destroying a window in a non-shutdown situation then we should
        // clean up the working set we made.
        if (!window.getWorkbench().isClosing()) {
        		if (aggregateWorkingSet != null) {
        			PlatformUI.getWorkbench().getWorkingSetManager().removeWorkingSet(aggregateWorkingSet);
        		}
        }
    }

    /**
     * Dispose a perspective.
     * 
     * @param persp the perspective descriptor
     * @param notify whether to fire a perspective closed event
     */
    private void disposePerspective(Perspective persp, boolean notify) {
        // Get rid of perspective.
        perspList.remove(persp);
        if (notify) {
        	window.firePerspectiveClosed(this, persp.getDesc());
        }
        persp.dispose();

        stickyViewMan.remove(persp.getDesc().getId());
    }

    /**
     * @return NavigationHistory
     */
    public INavigationHistory getNavigationHistory() {
        return navigationHistory;
    }

    /**
     * Edits the action sets.
     */
    public boolean editActionSets() {
        Perspective persp = getActivePerspective();
        if (persp == null) {
			return false;
		}

        // Create list dialog.
        CustomizePerspectiveDialog dlg = window.createCustomizePerspectiveDialog(persp);
        
        // Open.
        boolean ret = (dlg.open() == Window.OK);
        if (ret) {
            window.updateActionSets();
            window.firePerspectiveChanged(this, getPerspective(), CHANGE_RESET);
            window.firePerspectiveChanged(this, getPerspective(),
                    CHANGE_RESET_COMPLETE);
        }
        return ret;
    }

    /**
     * Returns the first view manager with given ID.
     */
    public Perspective findPerspective(IPerspectiveDescriptor desc) {
        Iterator itr = perspList.iterator();
        while (itr.hasNext()) {
            Perspective mgr = (Perspective) itr.next();
            if (desc.getId().equals(mgr.getDesc().getId())) {
				return mgr;
			}
        }
        return null;
    }

    /**
     * See IWorkbenchPage@findView.
     */
    public IViewPart findView(String id) {
        IViewReference ref = findViewReference(id);
        if (ref == null) {
			return null;
		}
        return ref.getView(true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWorkbenchPage
     */
    public IViewReference findViewReference(String viewId) {
        return findViewReference(viewId, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWorkbenchPage
     */
    public IViewReference findViewReference(String viewId, String secondaryId) {
        Perspective persp = getActivePerspective();
        if (persp == null) {
			return null;
		}
        return persp.findView(viewId, secondaryId);
    }


    /**
     * Notify property change listeners about a property change.
     * 
     * @param changeId
     *            the change id
     * @param oldValue
     *            old property value
     * @param newValue
     *            new property value
     */
    private void firePropertyChange(String changeId, Object oldValue,
            Object newValue) {
        
        UIListenerLogging.logPagePropertyChanged(this, changeId, oldValue, newValue);
        
        Object[] listeners = propertyChangeListeners.getListeners();
        PropertyChangeEvent event = new PropertyChangeEvent(this, changeId,
                oldValue, newValue);

        for (int i = 0; i < listeners.length; i++) {
            ((IPropertyChangeListener) listeners[i]).propertyChange(event);
        }
    }

    /*
     * Returns the action bars.
     */
    public IActionBars getActionBars() {
        if (actionBars == null) {
			actionBars = new WWinActionBars(window);
		}
        return actionBars;
    }

    /**
     * Returns an array of the visible action sets.
     */
    public IActionSetDescriptor[] getActionSets() {
        Collection collection = actionSets.getVisibleItems();
        
        return (IActionSetDescriptor[]) collection.toArray(new IActionSetDescriptor[collection.size()]);
    }

    /**
     * @see IWorkbenchPage
     */
    public IEditorPart getActiveEditor() {
        return partList.getActiveEditor();
    }

    /**
     * Returns the reference for the active editor, or <code>null</code> 
     * if there is no active editor.
     * 
     * @return the active editor reference or <code>null</code>
     */
    public IEditorReference getActiveEditorReference() {
        return partList.getActiveEditorReference();
    }
    
    /*
     * (non-Javadoc) Method declared on IPartService
     */
    public IWorkbenchPart getActivePart() {
        return partList.getActivePart();
    }

    /*
     * (non-Javadoc) Method declared on IPartService
     */
    public IWorkbenchPartReference getActivePartReference() {
        return partList.getActivePartReference();
    }

    /**
     * Returns the active perspective for the page, <code>null</code> if
     * none.
     */
    public Perspective getActivePerspective() {
        return perspList.getActive();
    }

    /**
     * Returns the client composite.
     */
    public Composite getClientComposite() {
        return composite;
    }

    //  for dynamic UI - change access from private to protected
    // for testing purposes only, changed from protected to public
    /**
     * Answer the editor manager for this window.
     */
    public EditorManager getEditorManager() {
        return editorMgr;
    }

    /**
     * Answer the perspective presentation.
     */
    public PerspectiveHelper getPerspectivePresentation() {
        if (getActivePerspective() != null) {
			return getActivePerspective().getPresentation();
		}
        return null;
    }

    /**
     * Answer the editor presentation.
     */
    public EditorAreaHelper getEditorPresentation() {
        return editorPresentation;
    }

    /**
     * See IWorkbenchPage.
     */
    public IEditorPart[] getEditors() {
        final IEditorReference refs[] = getEditorReferences();
        final ArrayList result = new ArrayList(refs.length);
        Display d = getWorkbenchWindow().getShell().getDisplay();
        //Must be backward compatible.
        d.syncExec(new Runnable() {
            public void run() {
                for (int i = 0; i < refs.length; i++) {
                    IWorkbenchPart part = refs[i].getPart(true);
                    if (part != null) {
						result.add(part);
					}
                }
            }
        });
        final IEditorPart editors[] = new IEditorPart[result.size()];
        return (IEditorPart[]) result.toArray(editors);
    }

    public IEditorPart[] getDirtyEditors() {
        return getEditorManager().getDirtyEditors();
    }
	
    public ISaveablePart[] getDirtyParts() {
        List result = new ArrayList(3);
        IWorkbenchPartReference[] allParts = getAllParts();
        for (int i = 0; i < allParts.length; i++) {
            IWorkbenchPartReference reference = allParts[i];
            
            IWorkbenchPart part = reference.getPart(false);
            if (part != null && part instanceof ISaveablePart) {
                ISaveablePart saveable = (ISaveablePart)part;
                if (saveable.isDirty()) {
                    result.add(saveable);
                }
            }
        }
        
        return (ISaveablePart[]) result.toArray(new ISaveablePart[result.size()]);
    }
  
    /**
     * See IWorkbenchPage.
     */
    public IEditorPart findEditor(IEditorInput input) {
        return getEditorManager().findEditor(input);
    }

    /**
     * See IWorkbenchPage.
     */
    public IEditorReference[] findEditors(IEditorInput input, String editorId, int matchFlags) {
    	return getEditorManager().findEditors(input, editorId, matchFlags);
    }
    
    /**
     * See IWorkbenchPage.
     */
    public IEditorReference[] getEditorReferences() {
        return editorPresentation.getEditors();
    }

    /**
     * Returns the docked views.
     */
    public IViewReference[] getFastViews() {
        Perspective persp = getActivePerspective();
        if (persp != null) {
			return persp.getFastViews();
		} else {
			return new IViewReference[0];
		}
    }

    /**
     * @see IWorkbenchPage
     */
    public IAdaptable getInput() {
        return input;
    }

    /**
     * Returns the page label. This is a combination of the page input and
     * active perspective.
     */
    public String getLabel() {
        String label = WorkbenchMessages.WorkbenchPage_UnknownLabel;
        IWorkbenchAdapter adapter = (IWorkbenchAdapter) Util.getAdapter(input, 
                IWorkbenchAdapter.class);
        if (adapter != null) {
			label = adapter.getLabel(input);
		}
        Perspective persp = getActivePerspective();
        if (persp != null) {
			label = NLS.bind(WorkbenchMessages.WorkbenchPage_PerspectiveFormat,  label, persp.getDesc().getLabel());
		} else if (deferredActivePersp != null) {
			label = NLS.bind(WorkbenchMessages.WorkbenchPage_PerspectiveFormat,label, deferredActivePersp.getLabel());
		} 
        return label;
    }

    /**
     * Returns the perspective.
     */
    public IPerspectiveDescriptor getPerspective() {
        if (deferredActivePersp != null) {
			return deferredActivePersp;
		}
        Perspective persp = getActivePerspective();
        if (persp != null) {
			return persp.getDesc();
		} else {
			return null;
		}
    }

    /*
     * (non-Javadoc) Method declared on ISelectionService
     */
    public ISelection getSelection() {
        return selectionService.getSelection();
    }

    /*
     * (non-Javadoc) Method declared on ISelectionService
     */
    public ISelection getSelection(String partId) {
        return selectionService.getSelection(partId);
    }

    /**
     * Returns the ids of the parts to list in the Show In... prompter. This is
     * a List of Strings.
     */
    public ArrayList getShowInPartIds() {
        Perspective persp = getActivePerspective();
        if (persp != null) {
			return persp.getShowInPartIds();
		} else {
			return new ArrayList();
		}
    }

    /**
     * The user successfully performed a Show In... action on the specified
     * part. Update the list of Show In items accordingly.
     */
    public void performedShowIn(String partId) {
        Perspective persp = getActivePerspective();
        if (persp != null) {
            persp.performedShowIn(partId);
        }
    }

    /**
     * Sorts the given collection of show in target part ids in MRU order.
     */
    public void sortShowInPartIds(ArrayList partIds) {
        final Perspective persp = getActivePerspective();
        if (persp != null) {
            Collections.sort(partIds, new Comparator() {
                public int compare(Object a, Object b) {
                    long ta = persp.getShowInTime((String) a);
                    long tb = persp.getShowInTime((String) b);
                    return (ta == tb) ? 0 : ((ta > tb) ? -1 : 1);
                }
            });
        }
    }

    /*
     * Returns the view factory.
     */
    public ViewFactory getViewFactory() {
        if (viewFactory == null) {
            viewFactory = new ViewFactory(this, WorkbenchPlugin.getDefault()
                    .getViewRegistry());
        }
        return viewFactory;
    }

    /**
     * See IWorkbenchPage.
     */
    public IViewReference[] getViewReferences() {
        Perspective persp = getActivePerspective();
        if (persp != null) {
			return persp.getViewReferences();
		} else {
			return new IViewReference[0];
		}
    }

    /**
     * See IWorkbenchPage.
     */
    public IViewPart[] getViews() {
		return getViews(null, true);
    }
	
	/**
	 * Returns all view parts in the specified perspective
	 * 
	 * @param persp the perspective
	 * @return an array of view parts
	 * @since 3.1
	 */
	/*package*/IViewPart[] getViews(Perspective persp, boolean restore) {			
        if (persp == null) {
			persp = getActivePerspective();
		}
		
        if (persp != null) {
            IViewReference refs[] = persp.getViewReferences();
            ArrayList parts = new ArrayList(refs.length);
            for (int i = 0; i < refs.length; i++) {
                IWorkbenchPart part = refs[i].getPart(restore);
                if (part != null) {
					parts.add(part);
				}
            }
            IViewPart[] result = new IViewPart[parts.size()];
            return (IViewPart[]) parts.toArray(result);
        }
        return new IViewPart[0];
    }

    /**
     * See IWorkbenchPage.
     */
    public IWorkbenchWindow getWorkbenchWindow() {
        return window;
    }

    /**
     * Implements IWorkbenchPage
     * 
     * @see org.eclipse.ui.IWorkbenchPage#getWorkingSet()
     * @since 2.0
     * @deprecated individual views should store a working set if needed
     */
    public IWorkingSet getWorkingSet() {
        return workingSet;
    }

    /**
     * @see IWorkbenchPage
     */
    public void hideActionSet(String actionSetID) {
        Perspective persp = getActivePerspective();
        if (persp != null) {
            persp.removeActionSet(actionSetID);
            window.updateActionSets();
            window.firePerspectiveChanged(this, getPerspective(),
                    CHANGE_ACTION_SET_HIDE);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWorkbenchPage#hideView(org.eclipse.ui.IViewReference)
     */
    public void hideView(IViewReference ref) {
        
        // Sanity check.
        if (ref == null) {
			return;
		}

        Perspective persp = getActivePerspective();
        if (persp == null) {
			return;
		}

        boolean promptedForSave = false;
        IViewPart view = ref.getView(false);
        if (view != null) {

            if (!certifyPart(view)) {
                return;
            }
            
            // Confirm.
    		if (view instanceof ISaveablePart) {
    			ISaveablePart saveable = (ISaveablePart)view;
    			if (saveable.isSaveOnCloseNeeded()) {
    				IWorkbenchWindow window = view.getSite().getWorkbenchWindow();
    				boolean success = EditorManager.saveAll(Collections.singletonList(view), true, true, false, window);
    				if (!success) {
    					// the user cancelled.
    					return;
    				}
    				promptedForSave = true;
    			}
    		}
        }
        
        int refCount = getViewFactory().getReferenceCount(ref);
        SaveablesList saveablesList = null;
        Object postCloseInfo = null;
        if (refCount == 1) {
        	IWorkbenchPart actualPart = ref.getPart(false);
        	if (actualPart != null) {
				saveablesList = (SaveablesList) actualPart
						.getSite().getService(ISaveablesLifecycleListener.class);
				postCloseInfo = saveablesList.preCloseParts(Collections
						.singletonList(actualPart), !promptedForSave, this
						.getWorkbenchWindow());
				if (postCloseInfo==null) {
					// cancel
					return;
				}
			}
        }
        
        // Notify interested listeners before the hide
        window.firePerspectiveChanged(this, persp.getDesc(), ref,
                CHANGE_VIEW_HIDE);

        PartPane pane = getPane(ref);
        pane.setInLayout(false);
        
        updateActivePart();
        
        if (saveablesList != null) {
        	saveablesList.postClose(postCloseInfo);
        }

        // Hide the part.
        persp.hideView(ref);

        // Notify interested listeners after the hide
        window.firePerspectiveChanged(this, getPerspective(), CHANGE_VIEW_HIDE);
    }

    /* package */void refreshActiveView() {
        updateActivePart();
    }

    /**
     * See IPerspective
     */
    public void hideView(IViewPart view) {
        hideView((IViewReference)getReference(view));
    }

    /**
     * Initialize the page.
     * 
     * @param w
     *            the parent window
     * @param layoutID
     *            may be <code>null</code> if restoring from file
     * @param input
     *            the page input
     * @param openExtras
     *            whether to process the perspective extras preference
     */
    private void init(WorkbenchWindow w, String layoutID, IAdaptable input, boolean openExtras)
            throws WorkbenchException {
        // Save args.
        this.window = w;
        this.input = input;
        actionSets = new ActionSetManager(w);

        // Create presentation.
        createClientComposite();
        editorPresentation = new EditorAreaHelper(this);
        editorMgr = new EditorManager(window, this, editorPresentation);

		// add this page as a client to be notified when the UI has re-orded perspectives 
		// so that the order can be properly maintained in the receiver.
		// E.g. a UI might support drag-and-drop and will need to make this known to ensure
		// #saveState and #restoreState do not lose this re-ordering
		w.addPerspectiveReorderListener(new IReorderListener() {
			public void reorder(Object perspective, int newLoc) {
				perspList.reorder((IPerspectiveDescriptor)perspective, newLoc);				
			}
		});
		
		if (openExtras) {
			openPerspectiveExtras();
		}
		
        // Get perspective descriptor.
        if (layoutID != null) {
            PerspectiveDescriptor desc = (PerspectiveDescriptor) WorkbenchPlugin
                    .getDefault().getPerspectiveRegistry()
                    .findPerspectiveWithId(layoutID);
            if (desc == null) {
				throw new WorkbenchException(
                        NLS.bind(WorkbenchMessages.WorkbenchPage_ErrorCreatingPerspective,layoutID ));
			}
            Perspective persp = findPerspective(desc);
            if (persp == null) {
	            persp = createPerspective(desc, true);
            }
            perspList.setActive(persp);
            window.firePerspectiveActivated(this, desc);
        }
        
        getExtensionTracker()
                .registerHandler(
                        perspectiveChangeHandler,
                        ExtensionTracker
                                .createExtensionPointFilter(getPerspectiveExtensionPoint()));
    }
    
    /**
	 * Opens the perspectives specified in the PERSPECTIVE_BAR_EXTRAS preference (see bug 84226).
	 */
	public void openPerspectiveExtras() {
        String extras = PrefUtil.getAPIPreferenceStore().getString(
				IWorkbenchPreferenceConstants.PERSPECTIVE_BAR_EXTRAS);
		StringTokenizer tok = new StringTokenizer(extras, ", "); //$NON-NLS-1$
		ArrayList descs = new ArrayList();
		while (tok.hasMoreTokens()) {
			String id = tok.nextToken();
            IPerspectiveDescriptor desc = WorkbenchPlugin.getDefault().getPerspectiveRegistry().findPerspectiveWithId(id);
            if (desc != null) {
            	descs.add(desc);
            }
		}
		// HACK: The perspective switcher currently adds the button for a new perspective to the beginning of the list.
		// So, we process the extra perspectives in reverse order here to have their buttons appear in the order declared. 
		for (int i = descs.size(); --i >= 0;) {
			PerspectiveDescriptor desc = (PerspectiveDescriptor) descs.get(i);
            if (findPerspective(desc) == null) {
            	createPerspective(desc, true);
            }
		}
	}

	/**
     * See IWorkbenchPage.
     */
    public boolean isPartVisible(IWorkbenchPart part) {
    	PartPane pane = getPane(part);
    	return pane != null && pane.getVisible();
    }
    
    /**
     * See IWorkbenchPage.
     */
    public boolean isEditorAreaVisible() {
        Perspective persp = getActivePerspective();
        if (persp == null) {
			return false;
		}
        return persp.isEditorAreaVisible();
    }

    /**
     * Returns whether the view is fast.
     */
    public boolean isFastView(IViewReference ref) {
        Perspective persp = getActivePerspective();
        if (persp != null) {
			return persp.isFastView(ref);
		} else {
			return false;
		}
    }
    
    /**
     * Return whether the view is closeable or not.
     * 
     * @param ref the view reference to check.  Must not be <code>null</code>.
     * @return true if the part is closeable.
     * @since 3.1.1
     */
    public boolean isCloseable(IViewReference ref) {
		Perspective persp = getActivePerspective();
		if (persp != null) {
			return persp.isCloseable(ref);
		}
		return false;
	}

    /**
     * Return whether the view is moveable or not.
     * 
     * @param ref the view reference to check.  Must not be <code>null</code>.
     * @return true if the part is moveable.
     * @since 3.1.1
     */
    public boolean isMoveable(IViewReference ref) {
		Perspective persp = getActivePerspective();
		if (persp != null) {
			return persp.isMoveable(ref);
		}
		return false;
	}

    /**
     * Returns whether the layout of the active
     * perspective is fixed.
     */
    public boolean isFixedLayout() {
        Perspective persp = getActivePerspective();
        if (persp != null) {
			return persp.isFixedLayout();
		} else {
			return false;
		}
    }

    /**
     * Return the active fast view or null if there are no fast views or if
     * there are all minimized.
     */
    public IViewReference getActiveFastView() {
        Perspective persp = getActivePerspective();
        if (persp != null) {
			return persp.getActiveFastView();
		} else {
			return null;
		}
    }

    /**
     * Return true if the perspective has a dirty editor.
     */
    protected boolean isSaveNeeded() {
        return getEditorManager().isSaveAllNeeded();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPage#isPageZoomed()
     */
    public boolean isPageZoomed() {
        Perspective persp = getActivePerspective();
        if (persp == null) {
			return false;
		}
        if (persp.getPresentation() == null) {
			return false;
		}
        
        if (Perspective.useNewMinMax(persp))
        	return persp.getPresentation().getMaximizedStack() != null;
        
        // Default to the legacy code
    	return isZoomed();
    }
    
    /**
     * Returns whether the page is zoomed.
     * @return <code>true</code> if the page is zoomed.
     * 
     * <strong>NOTE:</strong> As of 3.3 this method should always return 'false'
     * when using the new min/max behavior. It is only used for
     * legacy 'zoom' handling.
     * 
     */
   public boolean isZoomed() {
        Perspective persp = getActivePerspective();
        if (persp == null) {
			return false;
		}
        if (persp.getPresentation() == null) {
			return false;
		}
        return persp.getPresentation().isZoomed();
    }

    /**
     * This method is called when the page is activated.
     */
    protected void onActivate() {
        composite.setVisible(true);
        Perspective persp = getActivePerspective();

        if (persp != null) {
            persp.onActivate();
            updateVisibility(null, persp);
        }
    }

    /**
     * This method is called when the page is deactivated.
     */
    protected void onDeactivate() {
    	makeActiveEditor(null);
        makeActive(null);
        if (getActivePerspective() != null) {
			getActivePerspective().onDeactivate();
		}
        composite.setVisible(false);
    }

    /**
     * See IWorkbenchPage.
     */
    public void reuseEditor(IReusableEditor editor, IEditorInput input) {
        
        // Rather than calling editor.setInput on the editor directly, we do it through the part reference.
        // This case lets us detect badly behaved editors that are not firing a PROP_INPUT event in response
        // to the input change... but if all editors obeyed their API contract, the "else" branch would be
        // sufficient.
        IWorkbenchPartReference ref = getReference(editor);
        if (ref instanceof EditorReference) {
            EditorReference editorRef = (EditorReference) ref;
            
            editorRef.setInput(input);
        } else {
            editor.setInput(input);
        }
        navigationHistory.markEditor(editor);
    }

    /**
     * See IWorkbenchPage.
     */
    public IEditorPart openEditor(IEditorInput input, String editorID)
            throws PartInitException {
        return openEditor(input, editorID, true, MATCH_INPUT);
    }

    /**
     * See IWorkbenchPage.
     */
    public IEditorPart openEditor(IEditorInput input, String editorID,
			boolean activate) throws PartInitException {
		return openEditor(input, editorID, activate, MATCH_INPUT);
    }
	
    /**
     * See IWorkbenchPage.
     */
    public IEditorPart openEditor(final IEditorInput input,
            final String editorID, final boolean activate, final int matchFlags)
            throws PartInitException {
    	return openEditor(input, editorID, activate, matchFlags, null);
    }
	
    /**
     * This is not public API but for use internally.  editorState can be <code>null</code>.
     */
    public IEditorPart openEditor(final IEditorInput input,
            final String editorID, final boolean activate, final int matchFlags,
            final IMemento editorState)
            throws PartInitException {
        if (input == null || editorID == null) {
            throw new IllegalArgumentException();
        }

        final IEditorPart result[] = new IEditorPart[1];
        final PartInitException ex[] = new PartInitException[1];
        BusyIndicator.showWhile(window.getWorkbench().getDisplay(),
                new Runnable() {
                    public void run() {
                        try {
                            result[0] = busyOpenEditor(input, editorID,
                                    activate, matchFlags, editorState);
                        } catch (PartInitException e) {
                            ex[0] = e;
                        }
                    }
                });
        if (ex[0] != null) {
			throw ex[0];
		}
        return result[0];
    }

    
    /*
     * Added to fix Bug 178235 [EditorMgmt] DBCS 3.3 - Cannot open file with external program.
     * Opens a new editor using the given input and descriptor. (Normally, editors are opened using
     * an editor ID and an input.)
     */
    public IEditorPart openEditorFromDescriptor(final IEditorInput input,
    		final IEditorDescriptor editorDescriptor, final boolean activate,
    		final IMemento editorState)
    throws PartInitException {
    	if (input == null || !(editorDescriptor instanceof EditorDescriptor)) {
    		throw new IllegalArgumentException();
    	}
    	
    	final IEditorPart result[] = new IEditorPart[1];
    	final PartInitException ex[] = new PartInitException[1];
    	BusyIndicator.showWhile(window.getWorkbench().getDisplay(),
    			new Runnable() {
    		public void run() {
    			try {
    				result[0] = busyOpenEditorFromDescriptor(input, (EditorDescriptor)editorDescriptor,
    						activate, editorState);
    			} catch (PartInitException e) {
    				ex[0] = e;
    			}
    		}
    	});
    	if (ex[0] != null) {
    		throw ex[0];
    	}
    	return result[0];
    }
    
    /**
     * @see #openEditor(IEditorInput, String, boolean, int)
	 */
    private IEditorPart busyOpenEditor(IEditorInput input, String editorID,
            boolean activate, int matchFlags, IMemento editorState) throws PartInitException {

        final Workbench workbench = (Workbench) getWorkbenchWindow()
                .getWorkbench();
        workbench.largeUpdateStart();

        try {
            return busyOpenEditorBatched(input, editorID, activate, matchFlags, editorState);

        } finally {
            workbench.largeUpdateEnd();
        }
    }

    /*
     * Added to fix Bug 178235 [EditorMgmt] DBCS 3.3 - Cannot open file with external program.
     * See openEditorFromDescriptor().
     */
    private IEditorPart busyOpenEditorFromDescriptor(IEditorInput input, EditorDescriptor editorDescriptor,
    		boolean activate, IMemento editorState) throws PartInitException {
    	
    	final Workbench workbench = (Workbench) getWorkbenchWindow()
    	.getWorkbench();
    	workbench.largeUpdateStart();
    	
    	try {
    		return busyOpenEditorFromDescriptorBatched(input, editorDescriptor, activate, editorState);
    		
    	} finally {
    		workbench.largeUpdateEnd();
    	}
    }
    
    /**
     * Do not call this method.  Use <code>busyOpenEditor</code>.
     * 
     * @see IWorkbenchPage#openEditor(IEditorInput, String, boolean)
     */
    protected IEditorPart busyOpenEditorBatched(IEditorInput input,
            String editorID, boolean activate,  int matchFlags, IMemento editorState) throws PartInitException {

        // If an editor already exists for the input, use it.
		IEditorPart editor = null;
		// Reuse an existing open editor, unless we are in "new editor tab management" mode
		editor = getEditorManager().findEditor(editorID, input, ((TabBehaviour)Tweaklets.get(TabBehaviour.KEY)).getReuseEditorMatchFlags(matchFlags));
        if (editor != null) {
            if (IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID.equals(editorID)) {
                if (editor.isDirty()) {
                    MessageDialog dialog = new MessageDialog(
                            getWorkbenchWindow().getShell(),
                            WorkbenchMessages.Save, 
                            null, // accept the default window icon
                            NLS.bind(WorkbenchMessages.WorkbenchPage_editorAlreadyOpenedMsg,  input.getName()), 
                            MessageDialog.QUESTION, new String[] {
                                    IDialogConstants.YES_LABEL,
                                    IDialogConstants.NO_LABEL,
                                    IDialogConstants.CANCEL_LABEL }, 0);
                    int saveFile = dialog.open();
                    if (saveFile == 0) {
                        try {
                            final IEditorPart editorToSave = editor;
                            getWorkbenchWindow().run(false, false,
                                    new IRunnableWithProgress() {
                                        public void run(IProgressMonitor monitor)
                                                throws InvocationTargetException,
                                                InterruptedException {
                                            editorToSave.doSave(monitor);
                                        }
                                    });
                        } catch (InvocationTargetException e) {
                            throw (RuntimeException) e.getTargetException();
                        } catch (InterruptedException e) {
                            return null;
                        }
                    } else if (saveFile == 2) {
                        return null;
                    }
                }
            } else {
                // do the IShowEditorInput notification before showing the editor
                // to reduce flicker
                if (editor instanceof IShowEditorInput) {
                    ((IShowEditorInput) editor).showEditorInput(input);
                }
                showEditor(activate, editor);
                return editor;
            }
        }


        // Otherwise, create a new one. This may cause the new editor to
        // become the visible (i.e top) editor.
        IEditorReference ref = null;
        ref = getEditorManager().openEditor(editorID, input, true, editorState);
        if (ref != null) {
            editor = ref.getEditor(true);
        }

        if (editor != null) {
            setEditorAreaVisible(true);
            if (activate) {
                if (editor instanceof MultiEditor) {
					activate(((MultiEditor) editor).getActiveEditor());
				} else {
					activate(editor);
				}
            } else {
                bringToTop(editor);
            }
            window.firePerspectiveChanged(this, getPerspective(), ref,
                    CHANGE_EDITOR_OPEN);
            window.firePerspectiveChanged(this, getPerspective(),
                    CHANGE_EDITOR_OPEN);
        }

        return editor;
    }
    
    /*
     * Added to fix Bug 178235 [EditorMgmt] DBCS 3.3 - Cannot open file with external program.
     * See openEditorFromDescriptor().
     */
    private IEditorPart busyOpenEditorFromDescriptorBatched(IEditorInput input,
            EditorDescriptor editorDescriptor, boolean activate, IMemento editorState) throws PartInitException {

    	IEditorPart editor = null;
        // Create a new one. This may cause the new editor to
        // become the visible (i.e top) editor.
        IEditorReference ref = null;
        ref = getEditorManager().openEditorFromDescriptor(editorDescriptor, input, editorState);
		if (ref != null) {
            editor = ref.getEditor(true);
        }

        if (editor != null) {
            setEditorAreaVisible(true);
            if (activate) {
                if (editor instanceof MultiEditor) {
					activate(((MultiEditor) editor).getActiveEditor());
				} else {
					activate(editor);
				}
            } else {
                bringToTop(editor);
            }
            window.firePerspectiveChanged(this, getPerspective(), ref,
                    CHANGE_EDITOR_OPEN);
            window.firePerspectiveChanged(this, getPerspective(),
                    CHANGE_EDITOR_OPEN);
        }

        return editor;
    }
    
    public void openEmptyTab() {
    	IEditorPart editor = null;
        EditorReference ref = null;
        ref = (EditorReference) getEditorManager().openEmptyTab();
        if (ref != null) {
            editor = ref.getEmptyEditor((EditorDescriptor) ((EditorRegistry) WorkbenchPlugin
					.getDefault().getEditorRegistry())
					.findEditor(EditorRegistry.EMPTY_EDITOR_ID));
        }

        if (editor != null) {
            setEditorAreaVisible(true);
			activate(editor);
            window.firePerspectiveChanged(this, getPerspective(), ref,
                    CHANGE_EDITOR_OPEN);
            window.firePerspectiveChanged(this, getPerspective(),
                    CHANGE_EDITOR_OPEN);
        }
    }

    protected void showEditor(boolean activate, IEditorPart editor) {
        setEditorAreaVisible(true);
        if (activate) {
            zoomOutIfNecessary(editor);
            activate(editor);
        } else {
            bringToTop(editor);
        }
    }

    /**
     * See IWorkbenchPage.
     */
    public boolean isEditorPinned(IEditorPart editor) {
    	WorkbenchPartReference ref = (WorkbenchPartReference)getReference(editor); 
        return ref != null && ref.isPinned();
    }
    
    /**
     * Returns whether changes to a part will affect zoom. There are a few
     * conditions for this .. - we are zoomed. - the part is contained in the
     * main window. - the part is not the zoom part - the part is not a fast
     * view - the part and the zoom part are not in the same editor workbook
     */
    private boolean partChangeAffectsZoom(IWorkbenchPartReference ref) {
        PartPane pane = ((WorkbenchPartReference) ref).getPane();
        if (pane instanceof MultiEditorInnerPane) {
			pane = ((MultiEditorInnerPane) pane).getParentPane();
		}
        return getActivePerspective().getPresentation().partChangeAffectsZoom(
                pane);
    }

    /**
     * Removes a fast view.
     */
    public void removeFastView(IViewReference ref) {
        Perspective persp = getActivePerspective();
        if (persp == null) {
			return;
		}

        // Do real work.
        persp.removeFastView(ref);

        // Notify listeners.
        window.firePerspectiveChanged(this, getPerspective(), ref,
                CHANGE_FAST_VIEW_REMOVE);
        window.firePerspectiveChanged(this, getPerspective(),
                CHANGE_FAST_VIEW_REMOVE);
    }

    /**
     * Removes an IPartListener from the part service.
     */
    public void removePartListener(IPartListener l) {
        partList.getPartService().removePartListener(l);
    }

    /**
     * Removes an IPartListener from the part service.
     */
    public void removePartListener(IPartListener2 l) {
        partList.getPartService().removePartListener(l);
    }

    /**
     * Implements IWorkbenchPage
     * 
     * @see org.eclipse.ui.IWorkbenchPage#removePropertyChangeListener(IPropertyChangeListener)
     * @since 2.0
     * @deprecated individual views should store a working set if needed and
     *             register a property change listener directly with the
     *             working set manager to receive notification when the view
     *             working set is removed.
     */
    public void removePropertyChangeListener(IPropertyChangeListener listener) {
        propertyChangeListeners.remove(listener);
    }

    /*
     * (non-Javadoc) Method declared on ISelectionListener.
     */
    public void removeSelectionListener(ISelectionListener listener) {
        selectionService.removeSelectionListener(listener);
    }

    /*
     * (non-Javadoc) Method declared on ISelectionListener.
     */
    public void removeSelectionListener(String partId,
            ISelectionListener listener) {
        selectionService.removeSelectionListener(partId, listener);
    }

    /*
     * (non-Javadoc) Method declared on ISelectionListener.
     */
    public void removePostSelectionListener(ISelectionListener listener) {
        selectionService.removePostSelectionListener(listener);
    }

    /*
     * (non-Javadoc) Method declared on ISelectionListener.
     */
    public void removePostSelectionListener(String partId,
            ISelectionListener listener) {
        selectionService.removePostSelectionListener(partId, listener);
    }

    /**
     * This method is called when a part is activated by clicking within it. In
     * response, the part, the pane, and all of its actions will be activated.
     * 
     * In the current design this method is invoked by the part pane when the
     * pane, the part, or any children gain focus.
     */
    public void requestActivation(IWorkbenchPart part) {        
        // Sanity check.
        if (!certifyPart(part)) {
			return;
		}

        if (part instanceof MultiEditor) {
            part = ((MultiEditor) part).getActiveEditor();
        }

        // Real work.
        setActivePart(part);
    }

    /**
     * Resets the layout for the perspective. The active part in the old layout
     * is activated in the new layout for consistent user context.
     */
    public void resetPerspective() {
        // Run op in busy cursor.
        // Use set redraw to eliminate the "flash" that can occur in the
        // coolbar as the perspective is reset.
        ICoolBarManager2 mgr = (ICoolBarManager2) window.getCoolBarManager2();
        try {
            mgr.getControl2().setRedraw(false);
            BusyIndicator.showWhile(null, new Runnable() {
                public void run() {
                    busyResetPerspective();
                }
            });
        } finally {
            mgr.getControl2().setRedraw(true);
        }
    }

    /**
     * Restore this page from the memento and ensure that the active
     * perspective is equals the active descriptor otherwise create a new
     * perspective for that descriptor. If activeDescriptor is null active the
     * old perspective.
     */
    public IStatus restoreState(IMemento memento,
            final IPerspectiveDescriptor activeDescriptor) {
        StartupThreading.runWithoutExceptions(new StartupRunnable() {

			public void runWithException() throws Throwable {
				deferUpdates(true);
			}});
        
        try {
            // Restore working set
            String pageName = memento.getString(IWorkbenchConstants.TAG_LABEL);
            
            String label = null; // debugging only
            if (UIStats.isDebugging(UIStats.RESTORE_WORKBENCH)) {
                label = pageName == null ? "" : "::" + pageName; //$NON-NLS-1$ //$NON-NLS-2$
            }
    
            try {
                UIStats.start(UIStats.RESTORE_WORKBENCH, "WorkbenchPage" + label); //$NON-NLS-1$
                if (pageName == null) {
					pageName = ""; //$NON-NLS-1$
				}
                final MultiStatus result = new MultiStatus(
                        PlatformUI.PLUGIN_ID,
                        IStatus.OK,
                        NLS.bind(WorkbenchMessages.WorkbenchPage_unableToRestorePerspective, pageName ), 
                        null);
    
                String workingSetName = memento
                        .getString(IWorkbenchConstants.TAG_WORKING_SET);
                if (workingSetName != null) {
                    AbstractWorkingSetManager workingSetManager = (AbstractWorkingSetManager) getWorkbenchWindow()
                            .getWorkbench().getWorkingSetManager();
                    setWorkingSet(workingSetManager.getWorkingSet(workingSetName));
                }
                
	            IMemento workingSetMem = memento
						.getChild(IWorkbenchConstants.TAG_WORKING_SETS);
	            if (workingSetMem != null) {
					IMemento[] workingSetChildren = workingSetMem
							.getChildren(IWorkbenchConstants.TAG_WORKING_SET);
					List workingSetList = new ArrayList(
							workingSetChildren.length);
					for (int i = 0; i < workingSetChildren.length; i++) {
						IWorkingSet set = getWorkbenchWindow().getWorkbench()
								.getWorkingSetManager().getWorkingSet(
										workingSetChildren[i].getID());
						if (set != null) {
							workingSetList.add(set);
						}
					}

					workingSets = (IWorkingSet[]) workingSetList
							.toArray(new IWorkingSet[workingSetList.size()]);
				}
	            
	            aggregateWorkingSetId = memento.getString(ATT_AGGREGATE_WORKING_SET_ID);
	            
	            IWorkingSet setWithId = window.getWorkbench().getWorkingSetManager().getWorkingSet(aggregateWorkingSetId);
	            
	            // check to see if the set has already been made and assign it if it has
	            if (setWithId instanceof AggregateWorkingSet) {
					aggregateWorkingSet = (AggregateWorkingSet) setWithId;
				}
                // Restore editor manager.
                IMemento childMem = memento
                        .getChild(IWorkbenchConstants.TAG_EDITORS);
                result.merge(getEditorManager().restoreState(childMem));
    
                childMem = memento.getChild(IWorkbenchConstants.TAG_VIEWS);
                if (childMem != null) {
					result.merge(getViewFactory().restoreState(childMem));
				}
    
                // Get persp block.
                childMem = memento.getChild(IWorkbenchConstants.TAG_PERSPECTIVES);
                String activePartID = childMem
                        .getString(IWorkbenchConstants.TAG_ACTIVE_PART);
                String activePartSecondaryID = null;
                if (activePartID != null) {
                    activePartSecondaryID = ViewFactory
                            .extractSecondaryId(activePartID);
                    if (activePartSecondaryID != null) {
                        activePartID = ViewFactory.extractPrimaryId(activePartID);
                    }
                }
                final String activePerspectiveID = childMem
                        .getString(IWorkbenchConstants.TAG_ACTIVE_PERSPECTIVE);
    
                // Restore perspectives.
                final IMemento perspMems[] = childMem
                        .getChildren(IWorkbenchConstants.TAG_PERSPECTIVE);
                final Perspective activePerspectiveArray [] = new Perspective[1];
                
                for (int i = 0; i < perspMems.length; i++) {
                    
                        final IMemento current = perspMems[i];
					StartupThreading
							.runWithoutExceptions(new StartupRunnable() {

								public void runWithException() throws Throwable {
						            Perspective persp = ((WorkbenchImplementation) Tweaklets
						    				.get(WorkbenchImplementation.KEY)).createPerspective(null,
													WorkbenchPage.this);
									result.merge(persp.restoreState(current));
									final IPerspectiveDescriptor desc = persp
											.getDesc();
									if (desc.equals(activeDescriptor)) {
										activePerspectiveArray[0] = persp;
									} else if ((activePerspectiveArray[0] == null)
											&& desc.getId().equals(
													activePerspectiveID)) {
										activePerspectiveArray[0] = persp;
									}
									perspList.add(persp);
									window.firePerspectiveOpened(
											WorkbenchPage.this, desc);
								}
							});
                }
                Perspective activePerspective = activePerspectiveArray[0];
                boolean restoreActivePerspective = false;
                if (activeDescriptor == null) {
					restoreActivePerspective = true;

                } else if (activePerspective != null
                        && activePerspective.getDesc().equals(activeDescriptor)) {
                    restoreActivePerspective = true;
                } else {
                    restoreActivePerspective = false;
                    activePerspective = createPerspective((PerspectiveDescriptor) activeDescriptor, true);
                    if (activePerspective == null) {
                        result
                                .merge(new Status(
                                        IStatus.ERROR,
                                        PlatformUI.PLUGIN_ID,
                                        0,
                                        NLS.bind(WorkbenchMessages.Workbench_showPerspectiveError, activeDescriptor.getId() ), 
                                        null));
                    }
                }
    
                perspList.setActive(activePerspective);
    
                // Make sure we have a valid perspective to work with,
                // otherwise return.
                activePerspective = perspList.getActive();
                if (activePerspective == null) {
                    activePerspective = perspList.getNextActive();
                    perspList.setActive(activePerspective);
                }
                if (activePerspective != null && restoreActivePerspective) {
					result.merge(activePerspective.restoreState());
				}
    
                if (activePerspective != null) {
                	final Perspective myPerspective = activePerspective;
                	final String myActivePartId = activePartID, mySecondaryId = activePartSecondaryID;
                	StartupThreading.runWithoutExceptions(new StartupRunnable() {

						public void runWithException() throws Throwable {
							window.firePerspectiveActivated(WorkbenchPage.this, myPerspective
		                            .getDesc());
		    
		                    // Restore active part.
		                    if (myActivePartId != null) {
		                        IViewReference ref = myPerspective.findView(
		                        		myActivePartId, mySecondaryId);
		                        
		                        if (ref != null) {
		                            activationList.setActive(ref);
		                        }
		                    }
						}});
                    
                }
    
                childMem = memento
                        .getChild(IWorkbenchConstants.TAG_NAVIGATION_HISTORY);
                if (childMem != null) {
					navigationHistory.restoreState(childMem);
				} else if (getActiveEditor() != null) {
					navigationHistory.markEditor(getActiveEditor());
				}
                
                // restore sticky view state
                stickyViewMan.restore(memento);
                    
                return result;
            } finally {
            	String blame = activeDescriptor == null ? pageName : activeDescriptor.getId();
                UIStats.end(UIStats.RESTORE_WORKBENCH, blame, "WorkbenchPage" + label); //$NON-NLS-1$
            }
        } finally {
        	StartupThreading.runWithoutExceptions(new StartupRunnable() {

				public void runWithException() throws Throwable {
					deferUpdates(false);
				}
			});
        }
    }

    /**
     * See IWorkbenchPage
     */
    public boolean saveAllEditors(boolean confirm) {
        return saveAllEditors(confirm, false);
    }

    /**
     * @param confirm 
     * @param addNonPartSources true if saveables from non-part sources should be saved too
     * @return false if the user cancelled 
     * 
     */
    public boolean saveAllEditors(boolean confirm, boolean addNonPartSources) {
        return getEditorManager().saveAll(confirm, false, addNonPartSources);
    }

    /*
     * Saves the workbench part.
     */
    protected boolean savePart(ISaveablePart saveable, IWorkbenchPart part,
            boolean confirm) {
        // Do not certify part do allow editors inside a multipageeditor to
        // call this.
        return getEditorManager().savePart(saveable, part, confirm);
    }

    /**
     * Saves an editors in the workbench. If <code>confirm</code> is <code>true</code>
     * the user is prompted to confirm the command.
     * 
     * @param confirm
     *            if user confirmation should be sought
     * @return <code>true</code> if the command succeeded, or <code>false</code>
     *         if the user cancels the command
     */
    public boolean saveEditor(IEditorPart editor, boolean confirm) {
        return savePart(editor, editor, confirm);
    }

    /**
     * Saves the current perspective.
     */
    public void savePerspective() {
        Perspective persp = getActivePerspective();
        if (persp == null) {
			return;
		}

        // Always unzoom.
        if (isZoomed()) {
			zoomOut();
		}

        persp.saveDesc();
    }

    /**
     * Saves the perspective.
     */
    public void savePerspectiveAs(IPerspectiveDescriptor newDesc) {
        Perspective persp = getActivePerspective();
        if (persp == null) {
			return;
		}
        IPerspectiveDescriptor oldDesc = persp.getDesc();

        // Always unzoom.
        if (isZoomed()) {
			zoomOut();
		}

        persp.saveDescAs(newDesc);
        window.firePerspectiveSavedAs(this, oldDesc, newDesc);
    }

    /**
     * Save the state of the page.
     */
    public IStatus saveState(IMemento memento) {
        // We must unzoom to get correct layout.
        if (isZoomed()) {
			zoomOut();
		}

        MultiStatus result = new MultiStatus(
                PlatformUI.PLUGIN_ID,
                IStatus.OK,
                NLS.bind(WorkbenchMessages.WorkbenchPage_unableToSavePerspective, getLabel()), 
                null);

        // Save editor manager.
        IMemento childMem = memento
                .createChild(IWorkbenchConstants.TAG_EDITORS);
        result.merge(editorMgr.saveState(childMem));

        childMem = memento.createChild(IWorkbenchConstants.TAG_VIEWS);
        result.merge(getViewFactory().saveState(childMem));

        // Create persp block.
        childMem = memento.createChild(IWorkbenchConstants.TAG_PERSPECTIVES);
        if (getPerspective() != null) {
			childMem.putString(IWorkbenchConstants.TAG_ACTIVE_PERSPECTIVE,
                    getPerspective().getId());
		}
        if (getActivePart() != null) {
            if (getActivePart() instanceof IViewPart) {
                IViewReference ref = (IViewReference) getReference(getActivePart());
                if (ref != null) {
                    childMem.putString(IWorkbenchConstants.TAG_ACTIVE_PART,
                            ViewFactory.getKey(ref));
                }
            } else {
                childMem.putString(IWorkbenchConstants.TAG_ACTIVE_PART,
                        getActivePart().getSite().getId());
            }
        }

        // Save each perspective in opened order
        Iterator itr = perspList.iterator();
        while (itr.hasNext()) {
            Perspective persp = (Perspective) itr.next();
            IMemento gChildMem = childMem
                    .createChild(IWorkbenchConstants.TAG_PERSPECTIVE);
            result.merge(persp.saveState(gChildMem));
        }
        // Save working set if set
        if (workingSet != null) {
            memento.putString(IWorkbenchConstants.TAG_WORKING_SET, workingSet
                    .getName());
        }
        
        IMemento workingSetMem = memento
				.createChild(IWorkbenchConstants.TAG_WORKING_SETS);
		for (int i = 0; i < workingSets.length; i++) {
			workingSetMem.createChild(IWorkbenchConstants.TAG_WORKING_SET,
					workingSets[i].getName());
		}
		
		if (aggregateWorkingSetId != null) {
			memento.putString(ATT_AGGREGATE_WORKING_SET_ID, aggregateWorkingSetId);
		}

        navigationHistory.saveState(memento
                .createChild(IWorkbenchConstants.TAG_NAVIGATION_HISTORY));
        
        
        // save the sticky activation state
        stickyViewMan.save(memento);
        
		return result;
    }
    
    private String getId(IWorkbenchPart part) {
        return getId(getReference(part));
    }
    
    private String getId(IWorkbenchPartReference ref) {
        if (ref == null) {
            return "null"; //$NON-NLS-1$
        } return ref.getId();
    }

    /**
     * Sets the active part.
     */
    private void setActivePart(IWorkbenchPart newPart) {
        // Optimize it.
        if (getActivePart() == newPart) {
            return;
        }
        
        if (partBeingActivated != null) {
            if (partBeingActivated.getPart(false) != newPart) {
                WorkbenchPlugin.log(new RuntimeException(NLS.bind(
                        "WARNING: Prevented recursive attempt to activate part {0} while still in the middle of activating part {1}", //$NON-NLS-1$
                        getId(newPart), getId(partBeingActivated))));
            }
            return;
        }

        //No need to change the history if the active editor is becoming the
        // active part
        String label = null; // debugging only
        if (UIStats.isDebugging(UIStats.ACTIVATE_PART)) {
            label = newPart != null ? newPart.getTitle() : "none"; //$NON-NLS-1$
        }
        try {
            IWorkbenchPartReference partref = getReference(newPart);
            IWorkbenchPartReference realPartRef = null;
			if (newPart != null) {
				IWorkbenchPartSite site = newPart.getSite();
				if (site instanceof PartSite) {
					realPartRef = ((PartSite) site).getPane()
							.getPartReference();
				}
			}

            partBeingActivated = realPartRef;
            
            UIStats.start(UIStats.ACTIVATE_PART, label);
            // Notify perspective. It may deactivate fast view.
            Perspective persp = getActivePerspective();
            if (persp != null) {
				persp.partActivated(newPart);
			}

            // Deactivate old part
            IWorkbenchPart oldPart = getActivePart();
            if (oldPart != null) {
                deactivatePart(oldPart);
            }
            
            // Set active part.
            if (newPart != null) {
                activationList.setActive(newPart);
                if (newPart instanceof IEditorPart) {
					makeActiveEditor((IEditorReference)realPartRef);
				}
            }
            activatePart(newPart);
            
            actionSwitcher.updateActivePart(newPart);
            
            partList.setActivePart(partref);
        } finally {
            partBeingActivated = null;
        	Object blame = newPart == null ? (Object)this : newPart;
            UIStats.end(UIStats.ACTIVATE_PART, blame, label);
        }
    }

    /**
     * See IWorkbenchPage.
     */
    public void setEditorAreaVisible(boolean showEditorArea) {
        Perspective persp = getActivePerspective();
        if (persp == null) {
			return;
		}
        if (showEditorArea == persp.isEditorAreaVisible()) {
			return;
		}
        // If parts change always update zoom.
        if (isZoomed()) {
			zoomOut();
		}
        // Update editor area visibility.
        if (showEditorArea) {
            persp.showEditorArea();
            window.firePerspectiveChanged(this, getPerspective(),
                    CHANGE_EDITOR_AREA_SHOW);
        } else {
            persp.hideEditorArea();
            updateActivePart();
            window.firePerspectiveChanged(this, getPerspective(),
                    CHANGE_EDITOR_AREA_HIDE);
        }
    }

    /**
     * Sets the layout of the page. Assumes the new perspective is not null.
     * Keeps the active part if possible. Updates the window menubar and
     * toolbar if necessary.
     */
    private void setPerspective(Perspective newPersp) {
        // Don't do anything if already active layout
        Perspective oldPersp = getActivePerspective();
        if (oldPersp == newPersp) {
			return;
		}

        window.largeUpdateStart();
        try {
	        if (oldPersp != null) {
	        	// fire the pre-deactivate
				window.firePerspectivePreDeactivate(this, oldPersp.getDesc());
	        }
	        
	        if (newPersp != null) {
	            IStatus status = newPersp.restoreState();
	            if (status.getSeverity() != IStatus.OK) {
	                String title = WorkbenchMessages.WorkbenchPage_problemRestoringTitle; 
	                String msg = WorkbenchMessages.WorkbenchPage_errorReadingState;
	                ErrorDialog.openError(getWorkbenchWindow().getShell(), title,
	                        msg, status);
	            }
	        }
	
	
	        // Deactivate the old layout
	        if (oldPersp != null) {
				oldPersp.onDeactivate();

				// Notify listeners of deactivation
				window.firePerspectiveDeactivated(this, oldPersp.getDesc());
	        }
	
	        // Activate the new layout
	        perspList.setActive(newPersp);
	        if (newPersp != null) {
	            newPersp.onActivate();
	
	            // Notify listeners of activation
	            window.firePerspectiveActivated(this, newPersp.getDesc());
	        }
	
	        updateVisibility(oldPersp, newPersp);
	
            // Update the window
            window.updateActionSets();

            // Update sticky views
            stickyViewMan.update(oldPersp, newPersp);
            
        } finally {
            window.largeUpdateEnd();
            if (newPersp == null) {
				return;
			}
            IPerspectiveDescriptor desc = newPersp.getDesc();
            if (desc == null) {
				return;
			}
            if (dirtyPerspectives.remove(desc.getId())) {
            	suggestReset();
            }
        }
    }

    void perspectiveActionSetChanged(Perspective perspective, IActionSetDescriptor descriptor, int changeType) {
        if (perspective == getActivePerspective()) {
            actionSets.change(descriptor, changeType);
        }
    }
    
	/*
     * Update visibility state of all views.
     */
    private void updateVisibility(Perspective oldPersp, Perspective newPersp) {
        
        // Flag all parts in the old perspective
        IWorkbenchPartReference[] oldRefs = new IWorkbenchPartReference[0];
        if (oldPersp != null) {
            oldRefs = oldPersp.getViewReferences();
            for (int i = 0; i < oldRefs.length; i++) {
                PartPane pane = ((WorkbenchPartReference) oldRefs[i]).getPane();
                pane.setInLayout(false);
            }
        }
        
        PerspectiveHelper pres = null;
        // Make parts in the new perspective visible
        if (newPersp != null) {
            pres = newPersp.getPresentation();
            IWorkbenchPartReference[] newRefs = newPersp.getViewReferences();
            for (int i = 0; i < newRefs.length; i++) {
                WorkbenchPartReference ref = (WorkbenchPartReference)newRefs[i];
                PartPane pane = ref.getPane();
                if (pres.isPartVisible(ref)) {
                    activationList.bringToTop(ref);
                }

                pane.setInLayout(true);
            }
        }

        updateActivePart();

        // Hide any parts in the old perspective that are no longer visible
        for (int i = 0; i < oldRefs.length; i++) {
            WorkbenchPartReference ref = (WorkbenchPartReference)oldRefs[i];
               
            PartPane pane = ref.getPane();
            if (pres == null || !pres.isPartVisible(ref)) {
                pane.setVisible(false);
            }
        }
    }

    /**
     * Sets the perspective.
     * 
     * @param desc
     *            identifies the new perspective.
     */
    public void setPerspective(final IPerspectiveDescriptor desc) {
    	if (Util.equals(getPerspective(), desc)) {
    		return;
    	}
        // Going from multiple to single rows can make the coolbar
        // and its adjacent views appear jumpy as perspectives are
        // switched. Turn off redraw to help with this.
        ICoolBarManager2 mgr = (ICoolBarManager2) window.getCoolBarManager2();
        try {
            mgr.getControl2().setRedraw(false);
            getClientComposite().setRedraw(false);
            // Run op in busy cursor.
            BusyIndicator.showWhile(null, new Runnable() {
                public void run() {
                    busySetPerspective(desc);
                }
            });
        } finally {
            getClientComposite().setRedraw(true);
            mgr.getControl2().setRedraw(true);
            IWorkbenchPart part = getActivePart();
            if (part != null) {
				part.setFocus();
			}
        }
    }
    
    /**
     * Allow access to the part service for this page ... used internally to
     * propogate certain types of events to the page part listeners.
     * @return the part service for this page.
     */
    public PartService getPartService() {
    	return (PartService)partList.getPartService();
    }

    /**
     * Restore the toolbar layout for the active perspective.
     */
    protected void resetToolBarLayout() {
    	ICoolBarManager2 mgr = (ICoolBarManager2) window.getCoolBarManager2();
    	mgr.resetItemOrder();
    }

    /**
     * Sets the active working set for the workbench page. Notifies property
     * change listener about the change.
     * 
     * @param newWorkingSet
     *            the active working set for the page. May be null.
     * @since 2.0
     * @deprecated individual views should store a working set if needed
     */
    public void setWorkingSet(IWorkingSet newWorkingSet) {
        IWorkingSet oldWorkingSet = workingSet;

        workingSet = newWorkingSet;
        if (oldWorkingSet != newWorkingSet) {
            firePropertyChange(CHANGE_WORKING_SET_REPLACE, oldWorkingSet,
                    newWorkingSet);
        }
        if (newWorkingSet != null) {
            WorkbenchPlugin.getDefault().getWorkingSetManager()
                    .addPropertyChangeListener(workingSetPropertyChangeListener);
        } else {
            WorkbenchPlugin.getDefault().getWorkingSetManager()
                    .removePropertyChangeListener(workingSetPropertyChangeListener);
        }
    }

    /**
     * @see IWorkbenchPage
     */
    public void showActionSet(String actionSetID) {
        Perspective persp = getActivePerspective();
        if (persp != null) {
            ActionSetRegistry reg = WorkbenchPlugin.getDefault()
                 .getActionSetRegistry();
            
            IActionSetDescriptor desc = reg.findActionSet(actionSetID);
            if (desc != null) {
                persp.addActionSet(desc);
                window.updateActionSets();
                window.firePerspectiveChanged(this, getPerspective(),
                        CHANGE_ACTION_SET_SHOW);
            }
        }
    }

    /**
     * See IWorkbenchPage.
     */
    public IViewPart showView(String viewID) throws PartInitException {
        return showView(viewID, null, VIEW_ACTIVATE);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWorkbenchPage#showView(java.lang.String,
     *      java.lang.String, int)
     */
    public IViewPart showView(final String viewID, final String secondaryID,
            final int mode) throws PartInitException {

        if (secondaryID != null) {
            if (secondaryID.length() == 0
                    || secondaryID.indexOf(ViewFactory.ID_SEP) != -1) {
				throw new IllegalArgumentException(WorkbenchMessages.WorkbenchPage_IllegalSecondaryId);
			} 
        }
        if (!certifyMode(mode)) {
			throw new IllegalArgumentException(WorkbenchMessages.WorkbenchPage_IllegalViewMode);
		}

        // Run op in busy cursor.
        final Object[] result = new Object[1];
        BusyIndicator.showWhile(null, new Runnable() {
            public void run() {
                try {
                    result[0] = busyShowView(viewID, secondaryID, mode);
                } catch (PartInitException e) {
                    result[0] = e;
                }
            }
        });
        if (result[0] instanceof IViewPart) {
			return (IViewPart) result[0];
		} else if (result[0] instanceof PartInitException) {
			throw (PartInitException) result[0];
		} else {
			throw new PartInitException(WorkbenchMessages.WorkbenchPage_AbnormalWorkbenchCondition);
		} 
    }

    /**
     * @param mode the mode to test
     * @return whether the mode is recognized
     * @since 3.0
     */
    private boolean certifyMode(int mode) {
        switch (mode) {
        case VIEW_ACTIVATE:
        case VIEW_VISIBLE:
        case VIEW_CREATE:
            return true;
        default:
            return false;
        }
    }

    /**
     * Hides the active fast view. Has no effect if there is no fast view active.
     */
    public void hideFastView() {
        Perspective persp = getActivePerspective();
        if (persp != null) {
            IViewReference ref = persp.getActiveFastView();
            if (ref != null) {
                toggleFastView(ref);
            }
        }
    }

    /**
     * Toggles the visibility of a fast view. If the view is active it is
     * deactivated. Otherwise, it is activated.
     */
    public void toggleFastView(IViewReference ref) {
        Perspective persp = getActivePerspective();
        if (persp != null) {
            persp.toggleFastView(ref);
            // if the fast view has been deactivated
            if (ref != persp.getActiveFastView()) {
                IWorkbenchPart previouslyActive = activationList
                        .getPreviouslyActive();
                IEditorPart activeEditor = getActiveEditor();
                if (activeEditor != null
                        && previouslyActive instanceof IEditorPart) {
					setActivePart(activeEditor);
				} else {
					setActivePart(previouslyActive);
				}
            }
        }
    }

    /**
     * Sets the state of the given part.
     * 
     * @param ref part whose state should be modified (not null)
     * @param newState one of the IStackPresentationSite.STATE_* constants
     */
    public void setState(IWorkbenchPartReference ref, int newState) {
        Perspective persp = getActivePerspective();
        if (persp == null) {
			return;
		}

        PartPane pane = ((WorkbenchPartReference) ref).getPane();

        // If target part is detached fire the zoom event.  Note this doesn't 
        // actually cause any changes in size and is required to support 
        // intro state changes.  We may want to introduce the notion of a zoomed
        // (fullscreen) detached view at a later time.
        if (!pane.isDocked()) {
            pane.setZoomed(newState == IStackPresentationSite.STATE_MAXIMIZED);
            return;
        }

        if (ref instanceof IViewReference
                && persp.isFastView((IViewReference) ref)) {
            persp.setFastViewState(newState);
            return;
        }

    	if (Perspective.useNewMinMax(persp)) {
	        // set the container's state to the new one
	        PartStack parent = ((PartStack)pane.getContainer());
	        parent.setState(newState);
	        return;
    	}
    	
        boolean wasZoomed = isZoomed();
        boolean isZoomed = newState == IStackPresentationSite.STATE_MAXIMIZED;
		
        // Update zoom status.
        if (wasZoomed && !isZoomed) {
            zoomOut();
        } else if (!wasZoomed && isZoomed) {
            persp.getPresentation().zoomIn(ref);
            activate(ref.getPart(true));
        }
    	
        PartStack parent = ((PartStack)pane.getContainer());
        
        if (parent != null) {
            parent.setMinimized(newState == IStackPresentationSite.STATE_MINIMIZED);
        }
}
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPage#setPartState(org.eclipse.ui.IWorkbenchPartReference, int)
     */
    public void setPartState(IWorkbenchPartReference ref, int state) {
    	setState(ref, state);
    }
    
    /**
     * Returns the maximized/minimized/restored state of the given part reference
     * 
     * @param ref part to query (not null)
     * @return one of the IStackPresentationSite.STATE_* constants
     */
    int getState(IWorkbenchPartReference ref) {
        Perspective persp = getActivePerspective();
        if (persp == null) {
			return IStackPresentationSite.STATE_RESTORED;
		}

        PartPane pane = ((WorkbenchPartReference) ref).getPane();
    	
        if (ref instanceof IViewReference
                && persp.isFastView((IViewReference) ref)) {
            return persp.getFastViewState();
        }    	
        
        PartStack parent = ((PartStack)pane.getContainer());
        
        if (parent != null) {
        	return parent.getState();
        }
        
        return IStackPresentationSite.STATE_RESTORED;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPage#getPartState(org.eclipse.ui.IWorkbenchPartReference)
     */
    public int getPartState(IWorkbenchPartReference ref) {
    	return getState(ref);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPage#toggleZoom(org.eclipse.ui.IWorkbenchPartReference)
     */
    public void toggleZoom(IWorkbenchPartReference ref) {
    	int oldState = getState(ref);
    	boolean shouldZoom = oldState != IStackPresentationSite.STATE_MAXIMIZED;
    	int newState = shouldZoom ? IStackPresentationSite.STATE_MAXIMIZED : IStackPresentationSite.STATE_RESTORED;
    	
    	setState(ref, newState);
    }

    /**
     * updateActionBars method comment.
     */
    public void updateActionBars() {
        window.updateActionBars();
    }

    /**
     * Sets the tab list of this page's composite appropriately when a part is
     * activated.
     */
    private void updateTabList(IWorkbenchPart part) {
        PartSite site = (PartSite) part.getSite();
        PartPane pane = site.getPane();
        if (pane instanceof ViewPane) {
            ViewPane viewPane = (ViewPane) pane;
            Control[] tabList = viewPane.getTabList();
            if (!pane.isDocked()) {
                viewPane.getControl().getShell().setTabList(tabList);
            } else {
                getClientComposite().setTabList(tabList);
            }
        } else if (pane instanceof EditorPane) {
            EditorSashContainer ea = ((EditorPane) pane).getWorkbook()
                    .getEditorArea();
            ea.updateTabList();
            getClientComposite().setTabList(new Control[] { ea.getParent() });
        }
    }

    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPage#zoomOut()
     */
    public void zoomOut() {
        Perspective persp = getActivePerspective();
        if (persp != null) {
			persp.getPresentation().zoomOut();
		}
    }

    /**
     * Zooms out a zoomed in part if it is necessary to do so for the user to
     * view the IWorkbenchPart that is the argument. Otherwise, does nothing.
     * 
     * @param part
     *            the part to be made viewable
     */
    private void zoomOutIfNecessary(IWorkbenchPart part) {
        if (isZoomed() && partChangeAffectsZoom(((PartSite)part.getSite()).getPartReference())) {
			zoomOut();
		}
    }

    /**
     * 
     */
    public int getEditorReuseThreshold() {
    	return ((TabBehaviour)Tweaklets.get(TabBehaviour.KEY)).getEditorReuseThreshold();
    }

    /**
     * 
     */
    public void setEditorReuseThreshold(int openEditors) {
    }

    /*
     * Returns the editors in activation order (oldest first).
     */
    public IEditorReference[] getSortedEditors() {
        return activationList.getEditors();
    }

    /**
     * @see IWorkbenchPage#getOpenPerspectives()
     */
    public IPerspectiveDescriptor[] getOpenPerspectives() {
        Perspective opened[] = perspList.getOpenedPerspectives();
        IPerspectiveDescriptor[] result = new IPerspectiveDescriptor[opened.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = opened[i].getDesc();
        }
        return result;
    }
    
    /**
     * Return all open Perspective objects.
     * 
     * @return all open Perspective objects
     * @since 3.1
     */
    /*package*/Perspective [] getOpenInternalPerspectives() {
        return perspList.getOpenedPerspectives();
    }
	
	/**
	 * Checks perspectives in the order they were activiated
	 * for the specfied part.  The first sorted perspective 
	 * that contains the specified part is returned.
	 * 
	 * @param part specified part to search for
	 * @return the first sorted perspespective containing the part
	 * @since 3.1
	 */
	/*package*/Perspective getFirstPerspectiveWithView(IViewPart part) {
		Perspective [] perspectives = perspList.getSortedPerspectives();
		for (int i=perspectives.length - 1; i >= 0; i--) {
			if (perspectives[i].containsView(part)) {
				return perspectives[i];
			}
		}
		// we should never get here
		return null;
	}

    /**
     * Returns the perspectives in activation order (oldest first).
     */
    public IPerspectiveDescriptor[] getSortedPerspectives() {
        Perspective sortedArray[] = perspList.getSortedPerspectives();
        IPerspectiveDescriptor[] result = new IPerspectiveDescriptor[sortedArray.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = sortedArray[i].getDesc();
        }
        return result;
    }

    /*
     * Returns the parts in activation order (oldest first).
     */
    public IWorkbenchPartReference[] getSortedParts() {
        return activationList.getParts();
    }

    /**
     * Returns the reference to the given part, or <code>null</code> if it has no reference 
     * (i.e. it is not a top-level part in this workbench page).
     * 
     * @param part the part
     * @return the part's reference or <code>null</code> if the given part does not belong 
     * to this workbench page
     */
    public IWorkbenchPartReference getReference(IWorkbenchPart part) {
        if (part == null) {
            return null;
        }
        IWorkbenchPartSite site = part.getSite();
        if (!(site instanceof PartSite)) {
        	return null;
        }
        PartSite partSite = ((PartSite) site);
        PartPane pane = partSite.getPane();
        if (pane instanceof MultiEditorInnerPane) {
            MultiEditorInnerPane innerPane = (MultiEditorInnerPane) pane;
            return innerPane.getParentPane().getPartReference();
        }
        return partSite.getPartReference();
    }

    private class ActivationList {
        //List of parts in the activation order (oldest first)
        List parts = new ArrayList();

        /*
         * Add/Move the active part to end of the list;
         */
        void setActive(IWorkbenchPart part) {
            if (parts.size() <= 0) {
				return;
			}
			IWorkbenchPartReference ref = getReference(part);
			if (ref != null) {
				if (ref == parts.get(parts.size() - 1)) {
					return;
				}
				parts.remove(ref);
				parts.add(ref);
			}
        }
        
        /*
		 * Ensures that the given part appears AFTER any other part in the same
		 * container.
		 */
        void bringToTop(IWorkbenchPartReference ref) {
            ILayoutContainer targetContainer = getContainer(ref);
            
            int newIndex = lastIndexOfContainer(targetContainer);
            
            //New index can be -1 if there is no last index
            if (newIndex >= 0 && ref == parts.get(newIndex)) 
				return;
			
            parts.remove(ref);
            if(newIndex >= 0)
            	parts.add(newIndex, ref);
            else
            	parts.add(ref);
        }
        
        /*
         * Returns the last (most recent) index of the given container in the activation list, or returns
         * -1 if the given container does not appear in the activation list.
         */
        int lastIndexOfContainer(ILayoutContainer container) {
            for (int i = parts.size() - 1; i >= 0; i--) {
                IWorkbenchPartReference ref = (IWorkbenchPartReference)parts.get(i);

                ILayoutContainer cnt = getContainer(ref);
                if (cnt == container) {
                    return i; 
                }
            }
            
            return -1;
        }

        /*
         * Add/Move the active part to end of the list;
         */
        void setActive(IWorkbenchPartReference ref) {
            setActive(ref.getPart(true));
        }

        /*
         * Add the active part to the beginning of the list.
         */
        void add(IWorkbenchPartReference ref) {
            if (parts.indexOf(ref) >= 0) {
				return;
			}

            IWorkbenchPart part = ref.getPart(false);
            if (part != null) {
                PartPane pane = ((PartSite) part.getSite()).getPane();
                if (pane instanceof MultiEditorInnerPane) {
                    MultiEditorInnerPane innerPane = (MultiEditorInnerPane) pane;
                    add(innerPane.getParentPane().getPartReference());
                    return;
                }
            }
            parts.add(0, ref);
        }

        /*
         * Return the active part. Filter fast views.
         */
        IWorkbenchPart getActive() {
            if (parts.isEmpty()) {
				return null;
			}
            return getActive(parts.size() - 1);
        }

        /*
         * Return the previously active part. Filter fast views.
         */
        IWorkbenchPart getPreviouslyActive() {
            if (parts.size() < 2) {
				return null;
			}
            return getActive(parts.size() - 2);
        }

        private IWorkbenchPart getActive(int start) {
            IWorkbenchPartReference ref = getActiveReference(start, false);
            
            if (ref == null) {
                return null;
            }
            
            return ref.getPart(true);
        }
        
        public IWorkbenchPartReference getActiveReference(boolean editorsOnly) {
            return getActiveReference(parts.size() - 1, editorsOnly);
        }
        
        private IWorkbenchPartReference getActiveReference(int start, boolean editorsOnly) {
            // First look for parts that aren't obscured by the current zoom state
            IWorkbenchPartReference nonObscured = getActiveReference(start, editorsOnly, true);
            
            if (nonObscured != null) {
                return nonObscured;
            }
            
            // Now try all the rest of the parts
            return getActiveReference(start, editorsOnly, false);
        }
        
        /*
         * Find a part in the list starting from the end and filter
         * and views from other perspectives. Will filter fast views
         * unless 'includeActiveFastViews' is true;
         */
        private IWorkbenchPartReference getActiveReference(int start, boolean editorsOnly, boolean skipPartsObscuredByZoom) {
            IWorkbenchPartReference[] views = getViewReferences();
            for (int i = start; i >= 0; i--) {
                WorkbenchPartReference ref = (WorkbenchPartReference) parts
                        .get(i);

                if (editorsOnly && !(ref instanceof IEditorReference)) {
                    continue;
                }
                
                // Skip parts whose containers have disabled auto-focus
                PartPane pane = ref.getPane();

                if (pane != null) {
                    if (!pane.allowsAutoFocus()) {
                        continue;
                    }
                    
                    if (skipPartsObscuredByZoom) {
                        if (pane.isObscuredByZoom()) {
                            continue;
                        }
                    }
                }

                // Skip fastviews (unless overridden)
                if (ref instanceof IViewReference) {
                    if (ref == getActiveFastView() || !((IViewReference) ref).isFastView()) {
                        for (int j = 0; j < views.length; j++) {
                            if (views[j] == ref) {
                                return ref;
                            }
                        }
                    }
                } else {
                    return ref;
                }
            }
            return null;
        }

        /*
         * Retuns the index of the part within the activation list. The higher
         * the index, the more recently it was used.
         */
        int indexOf(IWorkbenchPart part) {
        	IWorkbenchPartReference ref = getReference(part);
        	if (ref == null) {
        		return -1;
        	}
            return parts.indexOf(ref);
        }

        /*
         * Returns the index of the part reference within the activation list.  
         * The higher the index, the more recent it was used.
         */
        int indexOf(IWorkbenchPartReference ref) {
            return parts.indexOf(ref);
        }

        /*
         * Remove a part from the list
         */
        boolean remove(IWorkbenchPartReference ref) {
            return parts.remove(ref);
        }

        /*
         * Returns the editors in activation order (oldest first).
         */
        private IEditorReference[] getEditors() {
            ArrayList editors = new ArrayList(parts.size());
            for (Iterator i = parts.iterator(); i.hasNext();) {
                IWorkbenchPartReference part = (IWorkbenchPartReference) i
                        .next();
                if (part instanceof IEditorReference) {
                    editors.add(part);
                }
            }
            return (IEditorReference[]) editors
                    .toArray(new IEditorReference[editors.size()]);
        }

        /*
         * Return a list with all parts (editors and views).
         */
        private IWorkbenchPartReference[] getParts() {
            IWorkbenchPartReference[] views = getViewReferences();
            ArrayList resultList = new ArrayList(parts.size());
            for (Iterator iterator = parts.iterator(); iterator.hasNext();) {
                IWorkbenchPartReference ref = (IWorkbenchPartReference) iterator
                        .next();
                if (ref instanceof IViewReference) {
                    //Filter views from other perspectives
                    for (int i = 0; i < views.length; i++) {
                        if (views[i] == ref) {
                            resultList.add(ref);
                            break;
                        }
                    }
                } else {
                    resultList.add(ref);
                }
            }
            IWorkbenchPartReference[] result = new IWorkbenchPartReference[resultList
                    .size()];
            return (IWorkbenchPartReference[]) resultList.toArray(result);
        }

        /*
         * Returns the topmost editor on the stack, or null if none.
         */
        IEditorPart getTopEditor() {
            IEditorReference editor = (IEditorReference)getActiveReference(parts.size() - 1, true);
            
            if (editor == null) {
                return null;
            }
            
            return editor.getEditor(true);
        }
    }

    /**
     * Helper class to keep track of all opened perspective. Both the opened
     * and used order is kept.
     */
    private class PerspectiveList {
        /**
         * List of perspectives in the order they were opened;
         */
        private List openedList;

        /**
         * List of perspectives in the order they were used. Last element is
         * the most recently used, and first element is the least recently
         * used.
         */
        private List usedList;

        /**
         * The perspective explicitly set as being the active one
         */
        private Perspective active;

        /**
         * Creates an empty instance of the perspective list
         */
        public PerspectiveList() {
            openedList = new ArrayList();
            usedList = new ArrayList();
        }

        /**
         * Update the order of the perspectives in the opened list
         *
         * @param perspective
         * @param newLoc
         */
        public void reorder(IPerspectiveDescriptor perspective, int newLoc) {
			int oldLocation = 0;
			Perspective movedPerspective = null;
			for (Iterator iterator = openedList.iterator(); iterator.hasNext();) {
				Perspective openPerspective = (Perspective) iterator.next();
				if (openPerspective.getDesc().equals(perspective)) {
					oldLocation = openedList.indexOf(openPerspective);
					movedPerspective = openPerspective;
				}
			}
			
			if (oldLocation == newLoc) {
				return;
			}
			
			openedList.remove(oldLocation);
			openedList.add(newLoc, movedPerspective);
			
		}

		/**
		 * Return all perspectives in the order they were activated.
		 * 
		 * @return an array of perspectives sorted by activation order, least
		 *         recently activated perspective last.
		 */
        public Perspective[] getSortedPerspectives() {
            Perspective[] result = new Perspective[usedList.size()];
            return (Perspective[]) usedList.toArray(result);
        }

        /**
         * Adds a perspective to the list. No check is done for a duplicate when
         * adding.
         * @param perspective the perspective to add
         * @return boolean <code>true</code> if the perspective was added
         */
        public boolean add(Perspective perspective) {
            openedList.add(perspective);
            usedList.add(0, perspective);
            //It will be moved to top only when activated.
            return true;
        }

        /**
         * Returns an iterator on the perspective list in the order they were
         * opened.
         */
        public Iterator iterator() {
            return openedList.iterator();
        }

        /**
         * Returns an array with all opened perspectives
         */
        public Perspective[] getOpenedPerspectives() {
            Perspective[] result = new Perspective[openedList.size()];
            return (Perspective[]) openedList.toArray(result);
        }

        /**
         * Removes a perspective from the list.
         */
        public boolean remove(Perspective perspective) {
            if (active == perspective) {
                updateActionSets(active, null);
                active = null;
            }
            usedList.remove(perspective);
            return openedList.remove(perspective);
        }

        /**
         * Swap the opened order of old perspective with the new perspective.
         */
        public void swap(Perspective oldPerspective, Perspective newPerspective) {
            int oldIndex = openedList.indexOf(oldPerspective);
            int newIndex = openedList.indexOf(newPerspective);

            if (oldIndex < 0 || newIndex < 0) {
				return;
			}

            openedList.set(oldIndex, newPerspective);
            openedList.set(newIndex, oldPerspective);
        }

        /**
         * Returns whether the list contains any perspectives
         */
        public boolean isEmpty() {
            return openedList.isEmpty();
        }

        /**
         * Returns the most recently used perspective in the list.
         */
        public Perspective getActive() {
            return active;
        }

        /**
         * Returns the next most recently used perspective in the list.
         */
        public Perspective getNextActive() {
            if (active == null) {
                if (usedList.isEmpty()) {
					return null;
				} else {
					return (Perspective) usedList.get(usedList.size() - 1);
				}
            } else {
                if (usedList.size() < 2) {
					return null;
				} else {
					return (Perspective) usedList.get(usedList.size() - 2);
				}
            }
        }

        /**
         * Returns the number of perspectives opened
         */
        public int size() {
            return openedList.size();
        }

        /**
         * Marks the specified perspective as the most recently used one in the
         * list.
         */
        public void setActive(Perspective perspective) {
            if (perspective == active) {
				return;
			}

            updateActionSets(active, perspective);
            active = perspective;

            if (perspective != null) {
                usedList.remove(perspective);
                usedList.add(perspective);
            }
        }
        
        private void updateActionSets(Perspective oldPersp, Perspective newPersp) {
			// Update action sets

			IContextService service = (IContextService) window
					.getService(IContextService.class);
			try {
				service.activateContext(ContextAuthority.DEFER_EVENTS);
				if (newPersp != null) {
					IActionSetDescriptor[] newAlwaysOn = newPersp
							.getAlwaysOnActionSets();
					for (int i = 0; i < newAlwaysOn.length; i++) {
						IActionSetDescriptor descriptor = newAlwaysOn[i];

						actionSets.showAction(descriptor);
					}

					IActionSetDescriptor[] newAlwaysOff = newPersp
							.getAlwaysOffActionSets();
					for (int i = 0; i < newAlwaysOff.length; i++) {
						IActionSetDescriptor descriptor = newAlwaysOff[i];

						actionSets.maskAction(descriptor);
					}
				}

				if (oldPersp != null) {
					IActionSetDescriptor[] newAlwaysOn = oldPersp
							.getAlwaysOnActionSets();
					for (int i = 0; i < newAlwaysOn.length; i++) {
						IActionSetDescriptor descriptor = newAlwaysOn[i];

						actionSets.hideAction(descriptor);
					}

					IActionSetDescriptor[] newAlwaysOff = oldPersp
							.getAlwaysOffActionSets();
					for (int i = 0; i < newAlwaysOff.length; i++) {
						IActionSetDescriptor descriptor = newAlwaysOff[i];

						actionSets.unmaskAction(descriptor);
					}
				}
			} finally {
				service.activateContext(ContextAuthority.SEND_EVENTS);
			}
		}
    }

    // for dynamic UI
    protected void addPerspective(Perspective persp) {
        perspList.add(persp);
        window.firePerspectiveOpened(this, persp.getDesc());
    }

    /**
	 * Find the stack of view references stacked with this view part.
	 * 
	 * @param part
	 *            the part
	 * @return the stack of references
	 * @since 3.0
	 */
    private IViewReference[] getViewReferenceStack(IViewPart part) {
        // Sanity check.
        Perspective persp = getActivePerspective();
        if (persp == null || !certifyPart(part)) {
			return null;
		}

        ILayoutContainer container = ((PartSite) part.getSite()).getPane()
                .getContainer();
        if (container instanceof ViewStack) {
            ViewStack folder = (ViewStack) container;
            final ArrayList list = new ArrayList(folder.getChildren().length);
            for (int i = 0; i < folder.getChildren().length; i++) {
                LayoutPart layoutPart = folder.getChildren()[i];
                if (layoutPart instanceof ViewPane) {
                    IViewReference view = ((ViewPane) layoutPart)
                            .getViewReference();
                    if (view != null) {
						list.add(view);
					}
                }
            }

            // sort the list by activation order (most recently activated first)
            Collections.sort(list, new Comparator() {
                public int compare(Object o1, Object o2) {
                    int pos1 = (-1)
                            * activationList
                                    .indexOf((IWorkbenchPartReference) o1);
                    int pos2 = (-1)
                            * activationList
                                    .indexOf((IWorkbenchPartReference) o2);
                    return pos1 - pos2;
                }
            });

            return (IViewReference[]) list.toArray(new IViewReference[list
                    .size()]);
        }

        return new IViewReference[] { (IViewReference) getReference(part) };
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWorkbenchPage#getViewStack(org.eclipse.ui.IViewPart)
     */
    public IViewPart[] getViewStack(IViewPart part) {
        IViewReference[] refStack = getViewReferenceStack(part);
        if (refStack == null) {
			return null;
		}

        List result = new ArrayList();
       
        for (int i = 0; i < refStack.length; i++) {
            IViewPart next = refStack[i].getView(false);
            if (next != null) {
                result.add(next);
            }
        }

        return (IViewPart[]) result.toArray(new IViewPart[result.size()]);
    }

    /**
     * Allow for programmatically resizing a part.
     * <p>
     * <em>EXPERIMENTAL</em>
     * </p>
     * <p>
     * Known limitations:
     * <ul>
     * <li>currently applies only to views</li>
     * <li>has no effect when view is zoomed</li>
     * </ul> 
     */
    public void resizeView(IViewPart part, int width, int height) {
        SashInfo sashInfo = new SashInfo();
        PartPane pane = ((PartSite) part.getSite()).getPane();
        ILayoutContainer container = pane.getContainer();
        LayoutTree tree = getPerspectivePresentation().getLayout().root
                .find(((ViewStack) container));

        // retrieve our layout sashes from the layout tree
        findSashParts(tree, pane.findSashes(), sashInfo);

        // first set the width
        float deltaWidth = width - pane.getBounds().width;
        if (sashInfo.right != null) {
            Rectangle rightBounds = sashInfo.rightNode.getBounds();
            // set the new ratio 
            sashInfo.right.setRatio(((deltaWidth + sashInfo.right
                    .getBounds().x) - rightBounds.x)
                    / rightBounds.width);
            // complete the resize
            sashInfo.rightNode.setBounds(rightBounds);
        } else if (sashInfo.left != null) {
            Rectangle leftBounds = sashInfo.leftNode.getBounds();
            // set the ratio
            sashInfo.left
                    .setRatio(((sashInfo.left.getBounds().x - deltaWidth) - leftBounds.x)
                            / leftBounds.width);
            // complete the resize
            sashInfo.leftNode.setBounds(sashInfo.leftNode.getBounds());
        }

        // next set the height
        float deltaHeight = height - pane.getBounds().height;
        if (sashInfo.bottom != null) {
            Rectangle bottomBounds = sashInfo.bottomNode.getBounds();
            // set the new ratio 
            sashInfo.bottom.setRatio(((deltaHeight + sashInfo.bottom
                    .getBounds().y) - bottomBounds.y)
                    / bottomBounds.height);
            // complete the resize
            sashInfo.bottomNode.setBounds(bottomBounds);
        } else if (sashInfo.top != null) {
            Rectangle topBounds = sashInfo.topNode.getBounds();
            // set the ratio
            sashInfo.top
                    .setRatio(((sashInfo.top.getBounds().y - deltaHeight) - topBounds.y)
                            / topBounds.height);
            // complete the resize
            sashInfo.topNode.setBounds(topBounds);
        }

    }

    // provides sash information for the given pane
    private class SashInfo {
        private LayoutPartSash right;

        private LayoutPartSash left;

        private LayoutPartSash top;

        private LayoutPartSash bottom;

        private LayoutTreeNode rightNode;

        private LayoutTreeNode leftNode;

        private LayoutTreeNode topNode;

        private LayoutTreeNode bottomNode;
    }

    private void findSashParts(LayoutTree tree, PartPane.Sashes sashes,
            SashInfo info) {
        LayoutTree parent = tree.getParent();
        if (parent == null) {
			return;
		}

        if (parent.part instanceof LayoutPartSash) {
            // get the layout part sash from this tree node
            LayoutPartSash sash = (LayoutPartSash) parent.part;
            // make sure it has a sash control
            Control control = sash.getControl();
            if (control != null) {
                // check for a vertical sash
                if (sash.isVertical()) {
                    if (sashes.left == control) {
                        info.left = sash;
                        info.leftNode = parent.findSash(sash);
                    } else if (sashes.right == control) {
                        info.right = sash;
                        info.rightNode = parent.findSash(sash);
                    }
                }
                // check for a horizontal sash
                else {
                    if (sashes.top == control) {
                        info.top = sash;
                        info.topNode = parent.findSash(sash);
                    } else if (sashes.bottom == control) {
                        info.bottom = sash;
                        info.bottomNode = parent.findSash(sash);
                    }
                }
            }
        }
        // recursive call to continue up the tree
        findSashParts(parent, sashes, info);
    }
    
	/**
	 * Returns all parts that are owned by this page
	 * 
	 * @return
	 */
	IWorkbenchPartReference[] getAllParts() {
		IViewReference[] views = viewFactory.getViews();
		IEditorReference[] editors = getEditorReferences();
		
		IWorkbenchPartReference[] result = new IWorkbenchPartReference[views.length + editors.length];
		int resultIdx = 0;
		
		for (int i = 0; i < views.length; i++) {
			result[resultIdx++] = views[i];
		}
		
		for (int i = 0; i < editors.length; i++) {
			result[resultIdx++] = editors[i];
		}
		
		return result;
	}
	
	/**
	 * Returns all open parts that are owned by this page (that is, all parts
	 * for which a part opened event would have been sent -- these would be
	 * activated parts whose controls have already been created.
	 */
	IWorkbenchPartReference[] getOpenParts() {
		IWorkbenchPartReference[] refs = getAllParts();
		List result = new ArrayList();
		
		for (int i = 0; i < refs.length; i++) {
			IWorkbenchPartReference reference = refs[i];
			
			IWorkbenchPart part = reference.getPart(false);
			if (part != null) {
				result.add(reference);
			}
		}
		
		return (IWorkbenchPartReference[]) result.toArray(new IWorkbenchPartReference[result.size()]);
	}    	
    
    /**
     * Sanity-checks the objects in this page. Throws an Assertation exception
     * if an object's internal state is invalid. ONLY INTENDED FOR USE IN THE 
     * UI TEST SUITES. 
     */
    public void testInvariants() {
        Perspective persp = getActivePerspective();
        
        if (persp != null) {

            persp.testInvariants();
            
            // When we have widgets, ensure that there is no situation where the editor area is visible
            // and the perspective doesn't want an editor area. 
            if (!SwtUtil.isDisposed(getClientComposite()) && editorPresentation.getLayoutPart().isVisible()) {
                Assert.isTrue(persp.isEditorAreaVisible());
            }
        }
        
    }

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#getExtensionTracker()
	 */
	public IExtensionTracker getExtensionTracker() {
		if (tracker == null) {
			tracker = new UIExtensionTracker(getWorkbenchWindow().getWorkbench().getDisplay());
		}
		return tracker;		
	}

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWorkbenchPage#getNewWizardShortcuts()
     */
    public String[] getNewWizardShortcuts() {
        Perspective persp = getActivePerspective();
        if (persp == null) {
            return new String[0];
        }
        return persp.getNewWizardShortcuts();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWorkbenchPage#getPerspectiveShortcuts()
     */
    public String[] getPerspectiveShortcuts() {
        Perspective persp = getActivePerspective();
        if (persp == null) {
            return new String[0];
        }
        return persp.getPerspectiveShortcuts();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWorkbenchPage#getShowViewShortcuts()
     */
    public String[] getShowViewShortcuts() {
        Perspective persp = getActivePerspective();
        if (persp == null) {
            return new String[0];
        }
        return persp.getShowViewShortcuts();
    }
    
    /**
	 * @since 3.1
	 */
	private void suggestReset() {
		final IWorkbench workbench = getWorkbenchWindow().getWorkbench();
        workbench.getDisplay().asyncExec(new Runnable() {
            public void run() {
                Shell parentShell = null;
                
				IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
                if (window == null) {
                    if (workbench.getWorkbenchWindowCount() == 0) {
						return;
					}
                    window = workbench.getWorkbenchWindows()[0];
                }

                parentShell = window.getShell();

                if (MessageDialog
                        .openQuestion(
                                parentShell,
                                WorkbenchMessages.Dynamic_resetPerspectiveTitle, 
                                WorkbenchMessages.Dynamic_resetPerspectiveMessage)) { 
                    IWorkbenchPage page = window.getActivePage();
                    if (page == null) {
						return;
					}
                    page.resetPerspective();
                }
            }
        });

		
	} 
    
    public boolean isPartVisible(IWorkbenchPartReference reference) {        
        IWorkbenchPart part = reference.getPart(false);
        // Can't be visible if it isn't created yet
        if (part == null) {
            return false;
        }
        
        return isPartVisible(part);
    }

	public IWorkingSet[] getWorkingSets() {
		return workingSets;
	}

	public void setWorkingSets(IWorkingSet[] newWorkingSets) {
		if (newWorkingSets == null) {
			newWorkingSets = new IWorkingSet[0];
		}

		IWorkingSet[] oldWorkingSets = workingSets;
		
		// filter out any duplicates if necessary
		if (newWorkingSets.length > 1) {	
			Set setOfSets = new HashSet();
			for (int i = 0; i < newWorkingSets.length; i++) {
				if (newWorkingSets[i] == null) {
					throw new IllegalArgumentException();
				}
				setOfSets.add(newWorkingSets[i]);
			}
			newWorkingSets = (IWorkingSet[]) setOfSets
					.toArray(new IWorkingSet[setOfSets.size()]);
		}

		workingSets = newWorkingSets;
		if (!Arrays.equals(oldWorkingSets, newWorkingSets)) {
			firePropertyChange(CHANGE_WORKING_SETS_REPLACE, oldWorkingSets,
					newWorkingSets);
			if (aggregateWorkingSet != null) {
				aggregateWorkingSet.setComponents(workingSets);
			}
		}
		if (newWorkingSets != null) {
			WorkbenchPlugin
					.getDefault()
					.getWorkingSetManager()
					.addPropertyChangeListener(workingSetPropertyChangeListener);
		} else {
			WorkbenchPlugin.getDefault().getWorkingSetManager()
					.removePropertyChangeListener(
							workingSetPropertyChangeListener);
		}
	}
	
	public IWorkingSet getAggregateWorkingSet() {
		if (aggregateWorkingSet == null) {
			IWorkingSetManager workingSetManager = PlatformUI.getWorkbench()
					.getWorkingSetManager();
			aggregateWorkingSet = (AggregateWorkingSet) workingSetManager.getWorkingSet(
							getAggregateWorkingSetId());
			if (aggregateWorkingSet == null) {
				aggregateWorkingSet = (AggregateWorkingSet) workingSetManager
						.createAggregateWorkingSet(
								getAggregateWorkingSetId(),
								WorkbenchMessages.WorkbenchPage_workingSet_default_label,
								getWorkingSets());
				workingSetManager.addWorkingSet(aggregateWorkingSet);
			}
		}
		return aggregateWorkingSet;
	}

	private String getAggregateWorkingSetId() {	
		if (aggregateWorkingSetId == null) {
			aggregateWorkingSetId = "Aggregate for window " + System.currentTimeMillis(); //$NON-NLS-1$
		}
		return aggregateWorkingSetId;
	}
}
