/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Benjamin Muskalla - bug 105041
 *     Remy Chi Jian Suen - bug 144102
 *******************************************************************************/

package org.eclipse.ui.views.navigator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.osgi.util.NLS;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.commands.ActionHandler;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.util.Util;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerSorter;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.OpenAndLinkWithEditorHelper;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ResourceWorkingSetFilter;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.actions.OpenResourceAction;
import org.eclipse.ui.handlers.CollapseAllHandler;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.views.navigator.ResourceNavigatorMessages;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.ISetSelectionTarget;
import org.eclipse.ui.part.IShowInSource;
import org.eclipse.ui.part.IShowInTarget;
import org.eclipse.ui.part.PluginTransfer;
import org.eclipse.ui.part.ResourceTransfer;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.views.framelist.FrameList;
import org.eclipse.ui.views.framelist.TreeFrame;

/**
 * Implements the Resource Navigator view.
 * @deprecated as of 3.5, use the Common Navigator Framework classes instead
 */
public class ResourceNavigator extends ViewPart implements ISetSelectionTarget,
        IResourceNavigator {

    private TreeViewer viewer;

    private IDialogSettings settings;

    private IMemento memento;

    private FrameList frameList;

    private ResourceNavigatorActionGroup actionGroup;

    private ResourcePatternFilter patternFilter = new ResourcePatternFilter();

    private ResourceWorkingSetFilter workingSetFilter = new ResourceWorkingSetFilter();

    private boolean linkingEnabled;

    private boolean dragDetected;

    private Listener dragDetectListener;
    /**
     * Remembered working set.
     */
    private IWorkingSet workingSet;

    /**
	 * Marks whether the working set we're using is currently empty. In this
	 * event we're effectively not using a working set.
	 */
    private boolean emptyWorkingSet = false;
    
    /**
	 * Settings constant for section name (value <code>ResourceNavigator</code>).
	 */
    private static final String STORE_SECTION = "ResourceNavigator"; //$NON-NLS-1$

    /**
     * Settings constant for sort order (value <code>ResourceViewer.STORE_SORT_TYPE</code>).
     */
    private static final String STORE_SORT_TYPE = "ResourceViewer.STORE_SORT_TYPE"; //$NON-NLS-1$

    /**
     * Settings constant for working set (value <code>ResourceWorkingSetFilter.STORE_WORKING_SET</code>).
     */
    private static final String STORE_WORKING_SET = "ResourceWorkingSetFilter.STORE_WORKING_SET"; //$NON-NLS-1$

    /**
     * @deprecated No longer used but preserved to avoid an api change.
     */
    public static final String NAVIGATOR_VIEW_HELP_ID = INavigatorHelpContextIds.RESOURCE_VIEW;

    /**
     * True iff we've already scheduled an asynchronous call to linkToEditor
     */
    private boolean linkScheduled = false;
    
    // Persistance tags.
    private static final String TAG_SORTER = "sorter"; //$NON-NLS-1$

    private static final String TAG_FILTERS = "filters"; //$NON-NLS-1$

    private static final String TAG_FILTER = "filter"; //$NON-NLS-1$

    private static final String TAG_SELECTION = "selection"; //$NON-NLS-1$

    private static final String TAG_EXPANDED = "expanded"; //$NON-NLS-1$

    private static final String TAG_ELEMENT = "element"; //$NON-NLS-1$

    private static final String TAG_IS_ENABLED = "isEnabled"; //$NON-NLS-1$

    private static final String TAG_PATH = "path"; //$NON-NLS-1$

    private static final String TAG_CURRENT_FRAME = "currentFrame"; //$NON-NLS-1$

    private IPartListener partListener = new IPartListener() {
        public void partActivated(IWorkbenchPart part) {
            if (part instanceof IEditorPart) {
				editorActivated((IEditorPart) part);
			}
        }

        public void partBroughtToTop(IWorkbenchPart part) {
            if (part instanceof IEditorPart) {
				editorActivated((IEditorPart) part);
			}
        }

        public void partClosed(IWorkbenchPart part) {
        }

        public void partDeactivated(IWorkbenchPart part) {
        }

        public void partOpened(IWorkbenchPart part) {
        }
    };

    private IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent event) {
            String property = event.getProperty();
            Object newValue = event.getNewValue();
            Object oldValue = event.getOldValue();
           
            if (IWorkingSetManager.CHANGE_WORKING_SET_REMOVE.equals(property)
                    && oldValue == workingSet) {
                setWorkingSet(null);
            } else if (IWorkingSetManager.CHANGE_WORKING_SET_NAME_CHANGE
                    .equals(property)
                    && newValue == workingSet) {
                updateTitle();
            } else if (IWorkingSetManager.CHANGE_WORKING_SET_CONTENT_CHANGE
                    .equals(property)
                    && newValue == workingSet) {
				if (workingSet.isAggregateWorkingSet() && workingSet.isEmpty()) {
					// act as if the working set has been made null
					if (!emptyWorkingSet) {
						emptyWorkingSet = true;
						workingSetFilter.setWorkingSet(null);
					}
				} else {
					// we've gone from empty to non-empty on our set.
					// Restore it.
					if (emptyWorkingSet) {
					    emptyWorkingSet = false;
						workingSetFilter.setWorkingSet(workingSet);
					}
				}
				getViewer().refresh();
            }
        }
    };

	private CollapseAllHandler collapseAllHandler;
	
	/**
	 * Helper to open and activate editors.
	 * 
	 * @since 3.5
	 */
	private OpenAndLinkWithEditorHelper openAndLinkWithEditorHelper;


    /**
     * Constructs a new resource navigator view.
     */
    public ResourceNavigator() {
        IDialogSettings viewsSettings = getPlugin().getDialogSettings();

        settings = viewsSettings.getSection(STORE_SECTION);
        if (settings == null) {
            settings = viewsSettings.addNewSection(STORE_SECTION);
        }

        initLinkingEnabled();
    }

    /**
     * Converts the given selection into a form usable by the viewer,
     * where the elements are resources.
     */
    private StructuredSelection convertSelection(ISelection selection) {
        ArrayList list = new ArrayList();
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection ssel = (IStructuredSelection) selection;
            for (Iterator i = ssel.iterator(); i.hasNext();) {
                Object o = i.next();
                IResource resource = null;
                if (o instanceof IResource) {
                    resource = (IResource) o;
                } else {
                    if (o instanceof IAdaptable) {
                        resource = (IResource) ((IAdaptable) o)
                                .getAdapter(IResource.class);
                    }
                }
                if (resource != null) {
                    list.add(resource);
                }
            }
        }
        return new StructuredSelection(list);
    }

    /* (non-Javadoc)
     * Method declared on IWorkbenchPart.
     */
    public void createPartControl(Composite parent) {
        TreeViewer viewer = createViewer(parent);
        this.viewer = viewer;

        if (memento != null) {
            restoreFilters();
            restoreLinkingEnabled();
        }
        frameList = createFrameList();
        initDragAndDrop();
        updateTitle();

        initContextMenu();

        initResourceComparator();
        initWorkingSetFilter();

        // make sure input is set after sorters and filters,
        // to avoid unnecessary refreshes
        viewer.setInput(getInitialInput());

        // make actions after setting input, because some actions
        // look at the viewer for enablement (e.g. the Up action)
        makeActions();

        // Fill the action bars and update the global action handlers'
        // enabled state to match the current selection.
        getActionGroup().fillActionBars(getViewSite().getActionBars());
        updateActionBars((IStructuredSelection) viewer.getSelection());

        getSite().setSelectionProvider(viewer);
        getSite().getPage().addPartListener(partListener);
        IWorkingSetManager workingSetManager = getPlugin().getWorkbench()
                .getWorkingSetManager();
        workingSetManager.addPropertyChangeListener(propertyChangeListener);

        if (memento != null) {
			restoreState(memento);
		}
        memento = null;

        // Set help for the view
        getSite().getWorkbenchWindow().getWorkbench().getHelpSystem().setHelp(
				viewer.getControl(), getHelpContextId());
    }

    /**
     * Returns the help context id to use for this view.
     * 
     * @since 2.0
     */
    protected String getHelpContextId() {
        return INavigatorHelpContextIds.RESOURCE_VIEW;
    }

    /**
     * Initializes and registers the context menu.
     * 
     * @since 2.0
     */
    protected void initContextMenu() {
        MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager manager) {
                ResourceNavigator.this.fillContextMenu(manager);
            }
        });
        TreeViewer viewer = getTreeViewer();
        Menu menu = menuMgr.createContextMenu(viewer.getTree());
        viewer.getTree().setMenu(menu);
        getSite().registerContextMenu(menuMgr, viewer);
    }

    /**
     * Creates the viewer.
     * 
     * @param parent the parent composite
     * @since 2.0
     */
    protected TreeViewer createViewer(Composite parent) {
        TreeViewer viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL
                | SWT.V_SCROLL);
        viewer.setUseHashlookup(true);
        initContentProvider(viewer);
        initLabelProvider(viewer);
        initFilters(viewer);
        initListeners(viewer);

        return viewer;
    }

    /**
     * Sets the content provider for the viewer.
     * 
     * @param viewer the viewer
     * @since 2.0
     */
    protected void initContentProvider(TreeViewer viewer) {
        viewer.setContentProvider(new WorkbenchContentProvider());
    }

    /**
     * Sets the label provider for the viewer.
     * 
     * @param viewer the viewer
     * @since 2.0
     */
    protected void initLabelProvider(TreeViewer viewer) {
        viewer.setLabelProvider(new DecoratingLabelProvider(
                new WorkbenchLabelProvider(), getPlugin().getWorkbench()
                        .getDecoratorManager().getLabelDecorator()));
    }

    /**
     * Adds the filters to the viewer.
     * 
     * @param viewer the viewer
     * @since 2.0
     */
    protected void initFilters(TreeViewer viewer) {
        viewer.addFilter(patternFilter);
        viewer.addFilter(workingSetFilter);
    }

    /**
     * Initializes the linking enabled setting from the preference store.
     */
    private void initLinkingEnabled() {
        // Try the dialog settings first, which remember the last choice.
        String setting = settings
                .get(IWorkbenchPreferenceConstants.LINK_NAVIGATOR_TO_EDITOR);
        if (setting != null) {
            linkingEnabled = setting.equals("true"); //$NON-NLS-1$
            return;
        }
        // If not in the dialog settings, check the preference store for the default setting.
        // Use the UI plugin's preference store since this is a public preference.
        linkingEnabled = PlatformUI.getPreferenceStore().getBoolean(
                IWorkbenchPreferenceConstants.LINK_NAVIGATOR_TO_EDITOR);
    }

    /**
     * Adds the listeners to the viewer.
     * 
     * @param viewer the viewer
     * @since 2.0
     */
    protected void initListeners(final TreeViewer viewer) {
        viewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                handleSelectionChanged(event);
            }
        });
        viewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event) {
                handleDoubleClick(event);
            }
        });
        
		openAndLinkWithEditorHelper = new OpenAndLinkWithEditorHelper(viewer) {
			protected void activate(ISelection selection) {
				Object selectedElement = getSingleElement(selection);
				if (selectedElement instanceof IFile) {
					IEditorInput input = new FileEditorInput((IFile)selectedElement);
					final IWorkbenchPage page = getSite().getPage();
					IEditorPart editor = page.findEditor(input);
					if (editor != null) {
						page.activate(editor);
					}
				}
				
			}

			protected void linkToEditor(ISelection selection) {
		        if (!linkScheduled) {
					// Ensure that if another selection change arrives while we're waiting for the *syncExec,
					// we only do this work once.
					linkScheduled = true;
					getSite().getShell().getDisplay().asyncExec(new Runnable() {
						public void run() {
							// There's no telling what might have changed since the syncExec was scheduled.
							// Check to make sure that the widgets haven't been disposed.
							linkScheduled = false;

							if (viewer == null || viewer.getControl() == null || viewer.getControl().isDisposed()) {
								return;
							}

							if (dragDetected == false) {
								// only synchronize with editor when the selection is not the result
								// of a drag. Fixes bug 22274.
								ResourceNavigator.this.linkToEditor(viewer.getSelection());
							}
						}
					});
				}
			}

			protected void open(ISelection selection, boolean activate) {
				handleOpen(selection);
			}

		};
        

        viewer.getControl().addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent event) {
                handleKeyPressed(event);
            }

            public void keyReleased(KeyEvent event) {
                handleKeyReleased(event);
            }
        });
        
        openAndLinkWithEditorHelper.setLinkWithEditor(linkingEnabled);
    }

    /* (non-Javadoc)
     * Method declared on IWorkbenchPart.
     */
    public void dispose() {
        getSite().getPage().removePartListener(partListener);

        IWorkingSetManager workingSetManager = getPlugin().getWorkbench()
                .getWorkingSetManager();
        workingSetManager.removePropertyChangeListener(propertyChangeListener);

        if (collapseAllHandler != null) {
			collapseAllHandler.dispose();
		}
        
        if (getActionGroup() != null) {
            getActionGroup().dispose();
        }
        Control control = viewer.getControl();
        if (dragDetectListener != null && control != null
                && control.isDisposed() == false) {
            control.removeListener(SWT.DragDetect, dragDetectListener);
        }
        
        super.dispose();
    }

    /**
     * An editor has been activated.  Sets the selection in this navigator
     * to be the editor's input, if linking is enabled.
     * 
     * @param editor the active editor
     * @since 2.0
     */
    protected void editorActivated(IEditorPart editor) {
        if (!isLinkingEnabled()) {
            return;
        }

        IFile file = ResourceUtil.getFile(editor.getEditorInput());
        if (file != null) {
            ISelection newSelection = new StructuredSelection(file);
            if (getTreeViewer().getSelection().equals(newSelection)) {
                getTreeViewer().getTree().showSelection();
            } else {
                getTreeViewer().setSelection(newSelection, true);
            }
        }
    }

    /**
     * Called when the context menu is about to open.
     * Delegates to the action group using the viewer's selection as the action context.
     * @since 2.0
     */
    protected void fillContextMenu(IMenuManager menu) {
        IStructuredSelection selection = (IStructuredSelection) getViewer()
                .getSelection();
        getActionGroup().setContext(new ActionContext(selection));
        getActionGroup().fillContextMenu(menu);
    }

    /*
     * @see IResourceNavigatorPart
     * @since 2.0
     */
    public FrameList getFrameList() {
        return frameList;
    }

    /**
     * Returns the initial input for the viewer.
     * Tries to convert the page input to a resource, either directly or via IAdaptable.
     * If the resource is a container, it uses that.
     * If the resource is a file, it uses its parent folder.
     * If a resource could not be obtained, it uses the workspace root.
     * 
     * @since 2.0
     */
    protected IAdaptable getInitialInput() {
        IAdaptable input = getSite().getPage().getInput();
        if (input != null) {
            IResource resource = null;
            if (input instanceof IResource) {
                resource = (IResource) input;
            } else {
                resource = (IResource) input.getAdapter(IResource.class);
            }
            if (resource != null) {
                switch (resource.getType()) {
                case IResource.FILE:
                    return resource.getParent();
                case IResource.FOLDER:
                case IResource.PROJECT:
                case IResource.ROOT:
                    return resource;
                default:
                    // Unknown resource type.  Fall through.
                    break;
                }
            }
        }
        return ResourcesPlugin.getWorkspace().getRoot();
    }

    /**
     * Returns the pattern filter for this view.
     *
     * @return the pattern filter
     * @since 2.0
     */
    public ResourcePatternFilter getPatternFilter() {
        return this.patternFilter;
    }

    /**
     * Returns the working set for this view.
     *
     * @return the working set
     * @since 2.0
     */
    public IWorkingSet getWorkingSet() {
        return workingSetFilter.getWorkingSet();
    }

    /**
     * Returns the navigator's plugin.
     * @return the UI plugin for this bundle
     */
    public AbstractUIPlugin getPlugin() {
        return IDEWorkbenchPlugin.getDefault();
    }

    /**
	 * Return the sorter. If a comparator was set using
	 * {@link #setComparator(ResourceComparator)}, this method will return
	 * <code>null</code>.
	 * 
	 * @since 2.0
	 * @deprecated as of 3.3, use {@link ResourceNavigator#getComparator()}
	 */
    public ResourceSorter getSorter() {
        ViewerSorter sorter = getTreeViewer().getSorter();
        if (sorter instanceof ResourceSorter) {
        	return (ResourceSorter) sorter;
        }
        return null;
    }

    /**
     * Returns the comparator.  If a sorter was set using
	 * {@link #setSorter(ResourceSorter)}, this method will return
	 * <code>null</code>.
     * 
     * @return the <code>ResourceComparator</code>
     * @since 3.3
     */

    public ResourceComparator getComparator(){
    	ViewerComparator comparator = getTreeViewer().getComparator();
    	if (comparator instanceof ResourceComparator) {
    		return (ResourceComparator) comparator;
    	}
    	return null;
    }
    /**
     * Returns the resource viewer which shows the resource hierarchy.
     * @since 2.0
     */
    public TreeViewer getViewer() {
        return viewer;
    }

    /**
     * Returns the tree viewer which shows the resource hierarchy.
     * @return the tree viewer
     * @since 2.0
     */
    public TreeViewer getTreeViewer() {
        return viewer;
    }

    /**
     * Returns the shell to use for opening dialogs.
     * Used in this class, and in the actions.
     * 
     * @return the shell
     * @deprecated use getViewSite().getShell()
     */
    public Shell getShell() {
        return getViewSite().getShell();
    }

    /**
     * Returns the message to show in the status line.
     *
     * @param selection the current selection
     * @return the status line message
     * @since 2.0
     */
    protected String getStatusLineMessage(IStructuredSelection selection) {
        if (selection.size() == 1) {
            Object o = selection.getFirstElement();
            if (o instanceof IResource) {
                return ((IResource) o).getFullPath().makeRelative().toString();
            }
            return ResourceNavigatorMessages.ResourceNavigator_oneItemSelected;
        }
        if (selection.size() > 1) {
            return NLS.bind(ResourceNavigatorMessages.ResourceNavigator_statusLine, String.valueOf(selection.size()));
        }
        return ""; //$NON-NLS-1$
    }

    /**
     * Returns the name for the given element.
     * Used as the name for the current frame.
     */
    String getFrameName(Object element) {
        if (element instanceof IResource) {
			return ((IResource) element).getName();
		}
        String text = ((ILabelProvider) getTreeViewer().getLabelProvider())
        .getText(element);
        if(text == null) {
			return "";//$NON-NLS-1$
		}
        return text;
    }

    /**
     * Returns the tool tip text for the given element.
     * Used as the tool tip text for the current frame, and for the view title tooltip.
     */
    String getFrameToolTipText(Object element) {
        if (element instanceof IResource) {
            IPath path = ((IResource) element).getFullPath();
            if (path.isRoot()) {
				return ResourceNavigatorMessages.ResourceManager_toolTip;
			}
            return path.makeRelative().toString();
        }
        
        String text = ((ILabelProvider) getTreeViewer().getLabelProvider())
        	.getText(element);
        if(text == null) {
			return "";//$NON-NLS-1$
		}
        return text;
    }

	/**
	 * Handles an open event from the viewer. Opens an editor on the selected file.
	 * 
	 * @param event the open event
	 * @since 2.0
	 * @deprecated As of 3.5, replaced by {@link #handleOpen(ISelection)}
	 */
    protected void handleOpen(OpenEvent event) {
        handleOpen(event.getSelection());
	}

	/**
	 * Handles an open event from the viewer. Opens an editor on the selected file.
	 * 
	 * @param selection the selection
	 * @since 3.5
	 */
	protected void handleOpen(ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			getActionGroup().runDefaultAction((IStructuredSelection)selection);
		}
    }

    /**
     * Handles a double-click event from the viewer.
     * Expands or collapses a folder when double-clicked.
     * 
     * @param event the double-click event
     * @since 2.0
     */
    protected void handleDoubleClick(DoubleClickEvent event) {
        IStructuredSelection selection = (IStructuredSelection) event
                .getSelection();
        Object element = selection.getFirstElement();

        // 1GBZIA0: ITPUI:WIN2000 - Double-clicking in navigator should expand/collapse containers
        TreeViewer viewer = getTreeViewer();
        if (viewer.isExpandable(element)) {
            viewer.setExpandedState(element, !viewer.getExpandedState(element));
		} else if (selection.size() == 1 && (element instanceof IResource)
				&& ((IResource) element).getType() == IResource.PROJECT) {
			OpenResourceAction ora = new OpenResourceAction(getSite());
			ora.selectionChanged((IStructuredSelection) viewer.getSelection());
			if (ora.isEnabled()) {
				ora.run();
			}
		}

    }

    /**
     * Handles a selection changed event from the viewer.
     * Updates the status line and the action bars, and links to editor (if option enabled).
     * 
     * @param event the selection event
     * @since 2.0
     */
    protected void handleSelectionChanged(SelectionChangedEvent event) {
        final IStructuredSelection sel = (IStructuredSelection) event
                .getSelection();
        updateStatusLine(sel);
        updateActionBars(sel);
        dragDetected = false;
    }

    /**
     * Handles a key press event from the viewer.
     * Delegates to the action group.
     * 
     * @param event the key event
     * @since 2.0
     */
    protected void handleKeyPressed(KeyEvent event) {
        getActionGroup().handleKeyPressed(event);
    }

    /**
     * Handles a key release in the viewer.  Does nothing by default.
     * 
     * @param event the key event
     * @since 2.0
     */
    protected void handleKeyReleased(KeyEvent event) {
    }

    /* (non-Javadoc)
     * Method declared on IViewPart.
     */
    public void init(IViewSite site, IMemento memento) throws PartInitException {
        super.init(site, memento);
        this.memento = memento;
    }

    /**
     * Adds drag and drop support to the navigator.
     * 
     * @since 2.0
     */
    protected void initDragAndDrop() {
        int ops = DND.DROP_COPY | DND.DROP_MOVE  | DND.DROP_LINK;
        Transfer[] transfers = new Transfer[] {
                LocalSelectionTransfer.getInstance(),
                ResourceTransfer.getInstance(), FileTransfer.getInstance(),
                PluginTransfer.getInstance() };
        TreeViewer viewer = getTreeViewer();
        viewer.addDragSupport(ops, transfers, new NavigatorDragAdapter(viewer));
        NavigatorDropAdapter adapter = new NavigatorDropAdapter(viewer);
        adapter.setFeedbackEnabled(false);
        viewer.addDropSupport(ops | DND.DROP_DEFAULT, transfers, adapter);
        dragDetectListener = new Listener() {
            public void handleEvent(Event event) {
                dragDetected = true;
            }
        };
        viewer.getControl().addListener(SWT.DragDetect, dragDetectListener);
    }

    /**
     * Creates the frame source and frame list, and connects them.
     * 
     * @since 2.0
     */
    protected FrameList createFrameList() {
        NavigatorFrameSource frameSource = new NavigatorFrameSource(this);
        FrameList frameList = new FrameList(frameSource);
        frameSource.connectTo(frameList);
        return frameList;
    }

    /**
     * Initializes the sorter.
     * 
     * @deprecated as of 3.3, use {@link ResourceNavigator#initResourceComparator()} instead
     */
    protected void initResourceSorter() {
        int sortType = ResourceSorter.NAME;
        try {
            int sortInt = 0;
            if (memento != null) {
                String sortStr = memento.getString(TAG_SORTER);
                if (sortStr != null) {
					sortInt = new Integer(sortStr).intValue();
				}
            } else {
                sortInt = settings.getInt(STORE_SORT_TYPE);
            }
            if (sortInt == ResourceSorter.NAME
                    || sortInt == ResourceSorter.TYPE) {
				sortType = sortInt;
			}
        } catch (NumberFormatException e) {
        }
        setSorter(new ResourceSorter(sortType));
    }
    
    /**
     * Initializes the comparator.
	 * @since 3.3
     */
    protected void initResourceComparator(){
        int sortType = ResourceComparator.NAME;
        try {
            int sortInt = 0;
            if (memento != null) {
                String sortStr = memento.getString(TAG_SORTER);
                if (sortStr != null) {
					sortInt = new Integer(sortStr).intValue();
				}
            } else {
                sortInt = settings.getInt(STORE_SORT_TYPE);
            }
            if (sortInt == ResourceComparator.NAME
                    || sortInt == ResourceComparator.TYPE) {
				sortType = sortInt;
			}
        } catch (NumberFormatException e) {
        }
        setComparator(new ResourceComparator(sortType));
    }

    /**
     * Restores the working set filter from the persistence store.
     */
    protected void initWorkingSetFilter() {
        String workingSetName = settings.get(STORE_WORKING_SET);

        IWorkingSet workingSet = null;
        
        if (workingSetName != null && workingSetName.equals("") == false) { //$NON-NLS-1$
			IWorkingSetManager workingSetManager = getPlugin().getWorkbench()
					.getWorkingSetManager();
			workingSet = workingSetManager.getWorkingSet(workingSetName);
		} else if (PlatformUI
				.getPreferenceStore()
				.getBoolean(
						IWorkbenchPreferenceConstants.USE_WINDOW_WORKING_SET_BY_DEFAULT)) {
			// use the window set by default if the global preference is set
			workingSet = getSite().getPage().getAggregateWorkingSet();
		}

		if (workingSet != null) {
			// Only initialize filter. Don't set working set into viewer.
			// Working set is set via WorkingSetFilterActionGroup
			// during action creation.
			workingSetFilter.setWorkingSet(workingSet);
			internalSetWorkingSet(workingSet);
		}
    }

    /**
	 * Returns whether the navigator selection automatically tracks the active
	 * editor.
	 * 
	 * @return <code>true</code> if linking is enabled, <code>false</code>
	 *         if not
	 * @since 2.0 (this was protected in 2.0, but was made public in 2.1)
	 */
    public boolean isLinkingEnabled() {
        return linkingEnabled;
    }

	/**
	 * Brings the corresponding editor to top if the selected resource is open.
	 * 
	 * @since 2.0
	 * @deprecated As of 3.5, replaced by {@link #linkToEditor(ISelection)}
	 */
    protected void linkToEditor(IStructuredSelection selection) {
    	linkToEditor((ISelection)selection);
	}

	/**
	 * Brings the corresponding editor to top if the selected resource is open.
	 * 
	 * @since 3.5
	 */
	protected void linkToEditor(ISelection selection) {

    	if (this != this.getSite().getPage().getActivePart())
    		return;
    	
        Object obj = getSingleElement(selection);
		if (obj instanceof IFile) {
            IFile file = (IFile) obj;
            IWorkbenchPage page = getSite().getPage();
            IEditorPart editor = ResourceUtil.findEditor(page, file);
            if (editor != null) {
                page.bringToTop(editor);
                return;
            }
        }
    }

    /**
     * Creates the action group, which encapsulates all actions for the view.
     */
    protected void makeActions() {
    	MainActionGroup group = new MainActionGroup(this);
        setActionGroup(group);
        
        IHandlerService service = (IHandlerService) getSite().getService(IHandlerService.class);
		service.activateHandler(IWorkbenchCommandConstants.NAVIGATE_TOGGLE_LINK_WITH_EDITOR,
    			new ActionHandler(group.toggleLinkingAction));
    	collapseAllHandler = new CollapseAllHandler(viewer);
    	service.activateHandler(CollapseAllHandler.COMMAND_ID,
				collapseAllHandler);
    }

    /**
     * Restores the saved filter settings.
     */
    private void restoreFilters() {
        IMemento filtersMem = memento.getChild(TAG_FILTERS);

        if (filtersMem != null) { //filters have been defined
            IMemento children[] = filtersMem.getChildren(TAG_FILTER);

            // check if first element has new tag defined, indicates new version
            if (children.length > 0
                    && children[0].getString(TAG_IS_ENABLED) != null) {
                ArrayList selectedFilters = new ArrayList();
                ArrayList unSelectedFilters = new ArrayList();
                for (int i = 0; i < children.length; i++) {
                    if (children[i].getString(TAG_IS_ENABLED).equals(
                            String.valueOf(true))) {
						selectedFilters.add(children[i].getString(TAG_ELEMENT));
					} else {
						//enabled == false
                        unSelectedFilters.add(children[i]
                                .getString(TAG_ELEMENT));
					}
                }

                /* merge filters from Memento with selected = true filters from plugins
                 * ensure there are no duplicates & don't override user preferences	 */
                List pluginFilters = FiltersContentProvider.getDefaultFilters();
                for (Iterator iter = pluginFilters.iterator(); iter.hasNext();) {
                    String element = (String) iter.next();
                    if (!selectedFilters.contains(element)
                            && !unSelectedFilters.contains(element)) {
						selectedFilters.add(element);
					}
                }

                //Convert to an array of Strings
                String[] patternArray = new String[selectedFilters.size()];
                selectedFilters.toArray(patternArray);
                getPatternFilter().setPatterns(patternArray);

            } else { //filters defined, old version: ignore filters from plugins
                String filters[] = new String[children.length];
                for (int i = 0; i < children.length; i++) {
                    filters[i] = children[i].getString(TAG_ELEMENT);
                }
                getPatternFilter().setPatterns(filters);
            }
        } else { //no filters defined, old version: ignore filters from plugins
            getPatternFilter().setPatterns(new String[0]);
        }
    }

    /**
     * Restores the state of the receiver to the state described in the specified memento.
     *
     * @param memento the memento
     * @since 2.0
     */
    protected void restoreState(IMemento memento) {
        TreeViewer viewer = getTreeViewer();
        IMemento frameMemento = memento.getChild(TAG_CURRENT_FRAME);

        if (frameMemento != null) {
            TreeFrame frame = new TreeFrame(viewer);
            frame.restoreState(frameMemento);
            frame.setName(getFrameName(frame.getInput()));
            frame.setToolTipText(getFrameToolTipText(frame.getInput()));
            viewer.setSelection(new StructuredSelection(frame.getInput()));
            frameList.gotoFrame(frame);
        } else {
            IContainer container = ResourcesPlugin.getWorkspace().getRoot();
            IMemento childMem = memento.getChild(TAG_EXPANDED);
            if (childMem != null) {
                ArrayList elements = new ArrayList();
                IMemento[] elementMem = childMem.getChildren(TAG_ELEMENT);
                for (int i = 0; i < elementMem.length; i++) {
                    Object element = container.findMember(elementMem[i]
                            .getString(TAG_PATH));
                    if (element != null) {
                        elements.add(element);
                    }
                }
                viewer.setExpandedElements(elements.toArray());
            }
            childMem = memento.getChild(TAG_SELECTION);
            if (childMem != null) {
                ArrayList list = new ArrayList();
                IMemento[] elementMem = childMem.getChildren(TAG_ELEMENT);
                for (int i = 0; i < elementMem.length; i++) {
                    Object element = container.findMember(elementMem[i]
                            .getString(TAG_PATH));
                    if (element != null) {
                        list.add(element);
                    }
                }
                viewer.setSelection(new StructuredSelection(list));
            }
        }
    }

    /**
     * Restores the linking enabled state.
     */
    private void restoreLinkingEnabled() {
        Integer val = memento
                .getInteger(IWorkbenchPreferenceConstants.LINK_NAVIGATOR_TO_EDITOR);
        if (val != null) {
            linkingEnabled = val.intValue() != 0;
        }
    }

    /**
     * @see ViewPart#saveState
     */
    public void saveState(IMemento memento) {
        TreeViewer viewer = getTreeViewer();
        if (viewer == null) {
            if (this.memento != null) {
				memento.putMemento(this.memento);
			}
            return;
        }

        //save sorter
        if (getComparator() != null) {
        	memento.putInteger(TAG_SORTER, getComparator().getCriteria());
        } else if (getSorter() != null) {
        	memento.putInteger(TAG_SORTER, getSorter().getCriteria());
        }

        //save filters
        String filters[] = getPatternFilter().getPatterns();
        List selectedFilters = Arrays.asList(filters);
        List allFilters = FiltersContentProvider.getDefinedFilters();
        IMemento filtersMem = memento.createChild(TAG_FILTERS);
        for (Iterator iter = allFilters.iterator(); iter.hasNext();) {
            String element = (String) iter.next();
            IMemento child = filtersMem.createChild(TAG_FILTER);
            child.putString(TAG_ELEMENT, element);
            child.putString(TAG_IS_ENABLED, String.valueOf(selectedFilters
                    .contains(element)));
        }

        if (frameList.getCurrentIndex() > 0) {
            //save frame, it's not the "home"/workspace frame
            TreeFrame currentFrame = (TreeFrame) frameList.getCurrentFrame();
            IMemento frameMemento = memento.createChild(TAG_CURRENT_FRAME);
            currentFrame.saveState(frameMemento);
        } else {
            //save visible expanded elements
            Object expandedElements[] = viewer.getVisibleExpandedElements();
            if (expandedElements.length > 0) {
                IMemento expandedMem = memento.createChild(TAG_EXPANDED);
                for (int i = 0; i < expandedElements.length; i++) {
                    if (expandedElements[i] instanceof IResource) {
                        IMemento elementMem = expandedMem
                                .createChild(TAG_ELEMENT);
                        elementMem.putString(TAG_PATH,
                                ((IResource) expandedElements[i]).getFullPath()
                                        .toString());
                    }
                }
            }
            //save selection
            Object elements[] = ((IStructuredSelection) viewer.getSelection())
                    .toArray();
            if (elements.length > 0) {
                IMemento selectionMem = memento.createChild(TAG_SELECTION);
                for (int i = 0; i < elements.length; i++) {
                    if (elements[i] instanceof IResource) {
                        IMemento elementMem = selectionMem
                                .createChild(TAG_ELEMENT);
                        elementMem.putString(TAG_PATH,
                                ((IResource) elements[i]).getFullPath()
                                        .toString());
                    }
                }
            }
        }

        saveLinkingEnabled(memento);
    }

    /**
     * Saves the linking enabled state.
     */
    private void saveLinkingEnabled(IMemento memento) {
        memento.putInteger(
                IWorkbenchPreferenceConstants.LINK_NAVIGATOR_TO_EDITOR,
                linkingEnabled ? 1 : 0);
    }

    /**
     * Selects and reveals the specified elements.
     */
    public void selectReveal(ISelection selection) {
        StructuredSelection ssel = convertSelection(selection);
        if (!ssel.isEmpty()) {
            getViewer().getControl().setRedraw(false);
            getViewer().setSelection(ssel, true);
            getViewer().getControl().setRedraw(true);
        }
    }

    /**
     * Saves the filters defined as strings in <code>patterns</code>
     * in the preference store.
     */
    public void setFiltersPreference(String[] patterns) {

        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < patterns.length; i++) {
            if (i != 0) {
				sb.append(ResourcePatternFilter.COMMA_SEPARATOR);
			}
            sb.append(patterns[i]);
        }

        getPlugin().getPreferenceStore().setValue(
                ResourcePatternFilter.FILTERS_TAG, sb.toString());

        // remove value in old workbench preference store location
        IPreferenceStore preferenceStore = IDEWorkbenchPlugin.getDefault()
                .getPreferenceStore();
        String storedPatterns = preferenceStore
                .getString(ResourcePatternFilter.FILTERS_TAG);
        if (storedPatterns.length() > 0) {
			preferenceStore.setValue(ResourcePatternFilter.FILTERS_TAG, ""); //$NON-NLS-1$
		}
    }

    /**
     * @see IWorkbenchPart#setFocus()
     */
    public void setFocus() {
        getTreeViewer().getTree().setFocus();
    }

    /**
     * Note: For experimental use only.
     * Sets the decorator for the navigator.
     * <p>
     * As of 2.0, this method no longer has any effect.
     * </p>
     *
     * @param decorator a label decorator or <code>null</code> for no decorations.
     * @deprecated use the decorators extension point instead; see IWorkbench.getDecoratorManager()
     */
    public void setLabelDecorator(ILabelDecorator decorator) {
        // do nothing
    }

    /**
     * @see IResourceNavigator#setLinkingEnabled(boolean)
     * @since 2.1
     */
    public void setLinkingEnabled(boolean enabled) {
        this.linkingEnabled = enabled;

        // remember the last setting in the dialog settings
        settings.put(IWorkbenchPreferenceConstants.LINK_NAVIGATOR_TO_EDITOR,
                enabled);

        // if turning linking on, update the selection to correspond to the active editor
        if (enabled) {
            IEditorPart editor = getSite().getPage().getActiveEditor();
            if (editor != null) {
                editorActivated(editor);
            }
        }
        openAndLinkWithEditorHelper.setLinkWithEditor(enabled);
    }

    /**
     * Sets the resource sorter.
     * 
     * @param sorter the resource sorter
     * @since 2.0
     * @deprecated as of 3.3, use {@link ResourceNavigator#setComparator(ResourceComparator)}
     */
    public void setSorter(ResourceSorter sorter) {
        TreeViewer viewer = getTreeViewer();
        ViewerSorter viewerSorter = viewer.getSorter();

        viewer.getControl().setRedraw(false);
        if (viewerSorter == sorter) {
            viewer.refresh();
        } else {
            viewer.setSorter(sorter);
        }
        viewer.getControl().setRedraw(true);
        settings.put(STORE_SORT_TYPE, sorter.getCriteria());

        // update the sort actions' checked state
        updateActionBars((IStructuredSelection) viewer.getSelection());
    }
    
    /**
     * Sets the resource comparator
     * 
     * @param comparator the resource comparator
     * @since 3.3
     */
    public void setComparator(ResourceComparator comparator){
        TreeViewer viewer = getTreeViewer();
        ViewerComparator viewerComparator = viewer.getComparator();

        viewer.getControl().setRedraw(false);
        if (viewerComparator == comparator) {
            viewer.refresh();
        } else {
            viewer.setComparator(comparator);
        }
        viewer.getControl().setRedraw(true);
        settings.put(STORE_SORT_TYPE, comparator.getCriteria());

        // update the sort actions' checked state
        updateActionBars((IStructuredSelection) viewer.getSelection());
    }

    /*
     * @see org.eclipse.ui.views.navigator.IResourceNavigatorPart#setWorkingSet(IWorkingSet)
     * @since 2.0
     */
    public void setWorkingSet(IWorkingSet workingSet) {
        TreeViewer treeViewer = getTreeViewer();
        Object[] expanded = treeViewer.getExpandedElements();
        ISelection selection = treeViewer.getSelection();
        
        boolean refreshNeeded = internalSetWorkingSet(workingSet);
        
        workingSetFilter.setWorkingSet(emptyWorkingSet ? null : workingSet);
        if (workingSet != null) {
            settings.put(STORE_WORKING_SET, workingSet.getName());
        } else {
            settings.put(STORE_WORKING_SET, ""); //$NON-NLS-1$
        }
        updateTitle();
        if(refreshNeeded) {
        	treeViewer.refresh();
        }
        treeViewer.setExpandedElements(expanded);
        if (selection.isEmpty() == false
                && selection instanceof IStructuredSelection) {
            IStructuredSelection structuredSelection = (IStructuredSelection) selection;
            treeViewer.reveal(structuredSelection.getFirstElement());
        }
    }

	/**
	 * Set the internal working set fields specific to the navigator.
	 * 
	 * @param workingSet
	 *            the new working set
	 * @since 3.2
	 */
	private boolean internalSetWorkingSet(IWorkingSet workingSet) {
		boolean refreshNeeded = !Util.equals(this.workingSet, workingSet);
		this.workingSet = workingSet;
		emptyWorkingSet = workingSet != null && workingSet.isAggregateWorkingSet()
				&& workingSet.isEmpty();
		return refreshNeeded;
	}

    /**
     * Updates the action bar actions.
     * 
     * @param selection the current selection
     * @since 2.0
     */
    protected void updateActionBars(IStructuredSelection selection) {
        ResourceNavigatorActionGroup group = getActionGroup();
        if (group != null) {
            group.setContext(new ActionContext(selection));
            group.updateActionBars();
        }
    }

    /**
     * Updates the message shown in the status line.
     *
     * @param selection the current selection
     */
    protected void updateStatusLine(IStructuredSelection selection) {
        String msg = getStatusLineMessage(selection);
        getViewSite().getActionBars().getStatusLineManager().setMessage(msg);
    }

    /**
     * Updates the title text and title tool tip.
     * Called whenever the input of the viewer changes.
     * Called whenever the input of the viewer changes.
     * 
     * @since 2.0
     */
    public void updateTitle() {
        Object input = getViewer().getInput();
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IWorkingSet workingSet = workingSetFilter.getWorkingSet();

        if (input == null || input.equals(workspace)
                || input.equals(workspace.getRoot())) {
            setContentDescription(""); //$NON-NLS-1$
            if (workingSet != null) {
                setTitleToolTip(NLS.bind(ResourceNavigatorMessages.ResourceNavigator_workingSetToolTip, workingSet.getLabel()));
            } else {
                setTitleToolTip(""); //$NON-NLS-1$
            }
        } else {
            ILabelProvider labelProvider = (ILabelProvider) getTreeViewer()
                    .getLabelProvider();
            String inputToolTip = getFrameToolTipText(input);
            String text = labelProvider.getText(input);
            if(text != null) {
				setContentDescription(text);
			}
            if (workingSet != null) {
                setTitleToolTip(NLS.bind(ResourceNavigatorMessages.ResourceNavigator_workingSetInputToolTip, inputToolTip, workingSet.getLabel()));
            } else {
                setTitleToolTip(inputToolTip);
            }
        }
    }

    /**
     * Returns the action group.
     * 
     * @return the action group
     */
    protected ResourceNavigatorActionGroup getActionGroup() {
        return actionGroup;
    }

    /**
     * Sets the action group.
     * 
     * @param actionGroup the action group
     */
    protected void setActionGroup(ResourceNavigatorActionGroup actionGroup) {
        this.actionGroup = actionGroup;
    }

    /*
     * @see IWorkbenchPart#getAdapter(Class)
     */
    public Object getAdapter(Class adapter) {
        if (adapter == IShowInSource.class) {
            return getShowInSource();
        }
        if (adapter == IShowInTarget.class) {
            return getShowInTarget();
        }
        return null;
    }

    /**
     * Returns the <code>IShowInSource</code> for this view.
     */
    protected IShowInSource getShowInSource() {
        return new IShowInSource() {
            public ShowInContext getShowInContext() {
                return new ShowInContext(getViewer().getInput(), getViewer()
                        .getSelection());
            }
        };
    }

    /**
     * Returns the <code>IShowInTarget</code> for this view.
     */
    protected IShowInTarget getShowInTarget() {
        return new IShowInTarget() {
            public boolean show(ShowInContext context) {
                ArrayList toSelect = new ArrayList();
                ISelection sel = context.getSelection();
                if (sel instanceof IStructuredSelection) {
                    IStructuredSelection ssel = (IStructuredSelection) sel;
                    for (Iterator i = ssel.iterator(); i.hasNext();) {
                        Object o = i.next();
                        if (o instanceof IResource) {
                            toSelect.add(o);
                        } else if (o instanceof IMarker) {
                            IResource r = ((IMarker) o).getResource();
                            if (r.getType() != IResource.ROOT) {
                                toSelect.add(r);
                            }
                        } else if (o instanceof IAdaptable) {
                            IAdaptable adaptable = (IAdaptable) o;
                            o = adaptable.getAdapter(IResource.class);
                            if (o instanceof IResource) {
                                toSelect.add(o);
                            } else {
                                o = adaptable.getAdapter(IMarker.class);
                                if (o instanceof IMarker) {
                                    IResource r = ((IMarker) o).getResource();
                                    if (r.getType() != IResource.ROOT) {
                                        toSelect.add(r);
                                    }
                                }
                            }
                        }
                    }
                }
                if (toSelect.isEmpty()) {
                    Object input = context.getInput();
                    if (input instanceof IAdaptable) {
                        IAdaptable adaptable = (IAdaptable) input;
                        Object o = adaptable.getAdapter(IResource.class);
                        if (o instanceof IResource) {
                            toSelect.add(o);
                        }
                    }
                }
                if (!toSelect.isEmpty()) {
                    selectReveal(new StructuredSelection(toSelect));
                    return true;
                }
                return false;
            }
        };
    }
    
	/**
	 * Returns the selected element if the selection consists of a single element only.
	 * 
	 * @param s the selection
	 * @return the selected first element or null
	 * @since 3.5
	 */
	protected static final Object getSingleElement(ISelection s) {
		if (!(s instanceof IStructuredSelection))
			return null;

		IStructuredSelection selection = (IStructuredSelection)s;
		if (selection.size() != 1)
			return null;

		return selection.getFirstElement();
	}

}
