/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.adaptable;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.internal.views.navigator.ResourceNavigatorMessages;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.views.navigator.ResourceSorter;

/**
 * Implements the Resource Navigator view.
 */
public class AdaptedResourceNavigator extends ViewPart {
    private TreeViewer viewer;

    private IDialogSettings settings;

    private IMemento memento;

    protected TestNavigatorActionGroup actionGroup;

    /**
     * Preference name constant for linking editor switching to navigator selection.
     * 
     * [Issue: We're cheating here, by referencing a preference which is actually defined
     * on the Workbench's preference page.  The Navigator should eventually have its own
     * preference page with this preference on it, instead of on the Workbench's.
     * The value must be the same as IWorkbenchPreferenceConstants.LINK_NAVIGATOR_TO_EDITOR.]
     */
    private static final String LINK_NAVIGATOR_TO_EDITOR = "LINK_NAVIGATOR_TO_EDITOR"; //$NON-NLS-1$

    private IPartListener partListener = new IPartListener() {
        public void partActivated(IWorkbenchPart part) {
            if (part instanceof IEditorPart)
                editorActivated((IEditorPart) part);
        }

        public void partBroughtToTop(IWorkbenchPart part) {
        }

        public void partClosed(IWorkbenchPart part) {
        }

        public void partDeactivated(IWorkbenchPart part) {
        }

        public void partOpened(IWorkbenchPart part) {
        }
    };

    /**
     * Creates a new AdaptedResourceNavigator.
     */
    public AdaptedResourceNavigator() {
        IDialogSettings workbenchSettings = getPlugin().getDialogSettings();
        settings = workbenchSettings.getSection("ResourceNavigator"); //$NON-NLS-1$
        if (settings == null)
            settings = workbenchSettings.addNewSection("ResourceNavigator"); //$NON-NLS-1$
    }

    /**
     * Converts the given selection into a form usable by the viewer,
     * where the elements are resources.
     */
    StructuredSelection convertSelection(ISelection selection) {
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
        viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        //	initDrillDownAdapter(viewer);
        viewer.setUseHashlookup(true);
        viewer.setContentProvider(new TestAdaptableContentProvider());
        IDecoratorManager manager = getSite().getWorkbenchWindow()
                .getWorkbench().getDecoratorManager();
        viewer.setLabelProvider(new DecoratingLabelProvider(
                new TestAdaptableWorkbenchAdapter(), manager
                        .getLabelDecorator()));

        viewer.setInput(getInitialInput());
        updateTitle();

        MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager manager) {
                AdaptedResourceNavigator.this.fillContextMenu(manager);
            }
        });
        Menu menu = menuMgr.createContextMenu(viewer.getTree());
        viewer.getTree().setMenu(menu);
        getSite().registerContextMenu(menuMgr, viewer);

        makeActions();

        // Update the global action enable state to match
        // the current selection.
        IStructuredSelection selection = (IStructuredSelection) viewer
                .getSelection();
        actionGroup.updateGlobalActions(selection);

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
        viewer.getControl().addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent event) {
                handleKeyPressed(event);
            }

            public void keyReleased(KeyEvent event) {
                handleKeyReleased(event);
            }
        });

        actionGroup.fillActionBars(selection);

        getSite().setSelectionProvider(viewer);

        getSite().getPage().addPartListener(partListener);

        if (memento != null)
            restoreState(memento);
        memento = null;
    }

    /* (non-Javadoc)
     * Method declared on IWorkbenchPart.
     */
    public void dispose() {
        getSite().getPage().removePartListener(partListener);
        super.dispose();
    }

    /**
     * An editor has been activated.  Set the selection in this navigator
     * to be the editor's input, if linking is enabled.
     */
    void editorActivated(IEditorPart editor) {
        if (!isLinkingEnabled())
            return;

        IEditorInput input = editor.getEditorInput();
        if (input instanceof IFileEditorInput) {
            IFileEditorInput fileInput = (IFileEditorInput) input;
            IFile file = fileInput.getFile();
            ISelection newSelection = new StructuredSelection(file);
            if (!viewer.getSelection().equals(newSelection)) {
                viewer.setSelection(newSelection);
            }
        }

    }

    /**
     * Called when the context menu is about to open.
     */
    void fillContextMenu(IMenuManager menu) {
        actionGroup.setContext(new ActionContext(getViewer().getSelection()));
        actionGroup.fillContextMenu(menu);
    }

    /** 
     * Returns the initial input for the viewer.
     * Tries to convert the input to a resource, either directly or via IAdaptable.
     * If the resource is a container, it uses that.
     * If the resource is a file, it uses its parent folder.
     * If a resource could not be obtained, it uses the workspace root.
     */
    IContainer getInitialInput() {
        IAdaptable input = getSite().getPage().getInput();
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
                return (IContainer) resource;
            default:
                // Unknown resource type.  Fall through.
                break;
            }
        }
        return ResourcesPlugin.getWorkspace().getRoot();
    }

    /**
     * Returns the navigator's plugin.
     */
    public AbstractUIPlugin getPlugin() {
        return (AbstractUIPlugin) Platform.getPlugin(PlatformUI.PLUGIN_ID);
    }

    /**
     * Returns the current sorter.
     * @since 2.0
     */
    public ResourceSorter getSorter() {
        return (ResourceSorter) getViewer().getSorter();
    }

    /**
     * Returns the tree viewer which shows the resource hierarchy.
     * @since 2.0
     */
    public TreeViewer getViewer() {
        return viewer;
    }

    /**
     * Returns the shell to use for opening dialogs.
     * Used in this class, and in the actions.
     */
    public Shell getShell() {
        return getViewSite().getShell();
    }

    /**
     * Returns the message to show in the status line.
     *
     * @param selection the current selection
     * @return the status line message
     */
    String getStatusLineMessage(IStructuredSelection selection) {
        if (selection.size() == 1) {
            Object o = selection.getFirstElement();
            if (o instanceof IResource) {
                return ((IResource) o).getFullPath().makeRelative().toString();
            } else {
                return ResourceNavigatorMessages.ResourceNavigator_oneItemSelected;
            }
        }
        if (selection.size() > 1) {
            return NLS.bind(ResourceNavigatorMessages.ResourceNavigator_statusLine, new Integer(selection.size()));
        }
        return ""; //$NON-NLS-1$
    }

    /**
     * Returns the tool tip text for the given element.
     */
    String getToolTipText(Object element) {
        if (element instanceof IResource) {
            IPath path = ((IResource) element).getFullPath();
            if (path.isRoot()) {
                return ResourceNavigatorMessages.ResourceManager_toolTip;
            } else {
                return path.makeRelative().toString();
            }
        } else {
            return ((ILabelProvider) getViewer().getLabelProvider())
                    .getText(element);
        }
    }

    /**
     * Handles double clicks in viewer.
     * Opens editor if file double-clicked.
     * @since 2.0
     */
    protected void handleDoubleClick(DoubleClickEvent event) {
        IStructuredSelection selection = (IStructuredSelection) event
                .getSelection();
        Object element = selection.getFirstElement();

        // 1GBZIA0: ITPUI:WIN2000 - Double-clicking in navigator should expand/collapse containers
        if (viewer.isExpandable(element)) {
            viewer.setExpandedState(element, !viewer.getExpandedState(element));
        }

    }

    /**
     * Handles selection changed in viewer.
     * Updates global actions.
     * Links to editor (if option enabled)
     * @since 2.0
     */
    protected void handleSelectionChanged(SelectionChangedEvent event) {
        IStructuredSelection sel = (IStructuredSelection) event.getSelection();
        updateStatusLine(sel);
        actionGroup.updateGlobalActions(sel);
        actionGroup.selectionChanged(sel);
        linkToEditor(sel);
    }

    /**
     * Handles a key press in viewer. By default do nothing.
     */
    protected void handleKeyPressed(KeyEvent event) {

    }

    /**
     * Handles a key release in viewer.
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
     * Initializes a drill down adapter on the viewer.
     */
    void initDrillDownAdapter(TreeViewer viewer) {
        DrillDownAdapter drillDownAdapter = new DrillDownAdapter(viewer) {
            // need to update title whenever input changes;
            // updateNavigationButtons is called whenever any of the drill down buttons are used
            protected void updateNavigationButtons() {
                super.updateNavigationButtons();
                updateTitle();
            }
        };
        drillDownAdapter.addNavigationActions(getViewSite().getActionBars()
                .getToolBarManager());
    }

    /**
     * Returns whether the preference to link navigator selection to active editor is enabled.
     * @since 2.0
     */
    protected boolean isLinkingEnabled() {
        IPreferenceStore store = getPlugin().getPreferenceStore();
        return store.getBoolean(LINK_NAVIGATOR_TO_EDITOR);
    }

    /**
     * Links to editor (if option enabled)
     * @since 2.0
     */
    protected void linkToEditor(IStructuredSelection selection) {
        if (!isLinkingEnabled())
            return;

        Object obj = selection.getFirstElement();
        if (obj instanceof IFile && selection.size() == 1) {
            IFile file = (IFile) obj;
            IWorkbenchPage page = getSite().getPage();
            IEditorReference editorArray[] = page.getEditorReferences();
            for (int i = 0; i < editorArray.length; ++i) {
                IEditorPart editor = editorArray[i].getEditor(true);
                IEditorInput input = editor.getEditorInput();
                if (input instanceof IFileEditorInput
                        && file.equals(((IFileEditorInput) input).getFile())) {
                    page.bringToTop(editor);
                    return;
                }
            }
        }
    }

    /**
     *	Create self's action objects
     */
    protected void makeActions() {
        actionGroup = new TestNavigatorActionGroup(this);
    }

    /**
     * Restore the state of the receiver to the state described in
     * momento.
     * @since 2.0
     */

    protected void restoreState(IMemento memento) {
    }

    public void saveState(IMemento memento) {
    }

    /**
     *	Reveal and select the passed element selection in self's visual component
     */
    public void selectReveal(ISelection selection) {
        StructuredSelection ssel = convertSelection(selection);
        if (!ssel.isEmpty()) {
            getViewer().setSelection(ssel, true);
        }
    }

    /**
     * @see IWorkbenchPart#setFocus()
     */
    public void setFocus() {
        getViewer().getTree().setFocus();
    }

    /**
     * Note: For experimental use only.
     * Sets the decorator for the navigator.
     *
     * @param decorator a label decorator or <code>null</code> for no decorations.
     */
    public void setLabelDecorator(ILabelDecorator decorator) {
        DecoratingLabelProvider provider = (DecoratingLabelProvider) getViewer()
                .getLabelProvider();
        if (decorator == null) {
            IDecoratorManager manager = getSite().getWorkbenchWindow()
                    .getWorkbench().getDecoratorManager();
            provider.setLabelDecorator(manager.getLabelDecorator());
        } else {
            provider.setLabelDecorator(decorator);
        }
    }

    /**
     * Updates the message shown in the status line.
     *
     * @param selection the current selection
     */
    void updateStatusLine(IStructuredSelection selection) {
        String msg = getStatusLineMessage(selection);
        getViewSite().getActionBars().getStatusLineManager().setMessage(msg);
    }

    /**
     * Updates the title text and title tool tip.
     * Called whenever the input of the viewer changes.
     */
    void updateTitle() {
        Object input = getViewer().getInput();
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        if (input == null || input.equals(workspace)
                || input.equals(workspace.getRoot())) {
            setContentDescription(""); //$NON-NLS-1$
            setTitleToolTip(""); //$NON-NLS-1$
        } else {
            ILabelProvider labelProvider = (ILabelProvider) getViewer()
                    .getLabelProvider();
            setContentDescription(labelProvider.getText(input));
            //$NON-NLS-1$
            setTitleToolTip(getToolTipText(input));
        }
    }

}
