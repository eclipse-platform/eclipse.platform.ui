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

package org.eclipse.ui.internal;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionDelta;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.runtime.IRegistryChangeListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.internal.decorators.DecoratorDefinition;
import org.eclipse.ui.internal.decorators.DecoratorManager;
import org.eclipse.ui.internal.decorators.DecoratorRegistryReader;
import org.eclipse.ui.internal.dialogs.PropertyPageContributorManager;
import org.eclipse.ui.internal.dialogs.WorkbenchPreferenceNode;
import org.eclipse.ui.internal.registry.ActionSetPartAssociationsReader;
import org.eclipse.ui.internal.registry.ActionSetRegistry;
import org.eclipse.ui.internal.registry.EditorRegistry;
import org.eclipse.ui.internal.registry.EditorRegistryReader;
import org.eclipse.ui.internal.registry.IActionSet;
import org.eclipse.ui.internal.registry.IActionSetDescriptor;
import org.eclipse.ui.internal.registry.IViewRegistry;
import org.eclipse.ui.internal.registry.NewWizardsRegistryReader;
import org.eclipse.ui.internal.registry.PerspectiveRegistry;
import org.eclipse.ui.internal.registry.PreferencePageRegistryReader;
import org.eclipse.ui.internal.registry.PropertyPagesRegistryReader;
import org.eclipse.ui.internal.registry.ViewRegistry;
import org.eclipse.ui.internal.registry.WorkingSetRegistry;
import org.eclipse.ui.internal.registry.WorkingSetRegistryReader;
import org.eclipse.ui.internal.themes.ColorDefinition;
import org.eclipse.ui.internal.themes.FontDefinition;
import org.eclipse.ui.internal.themes.ThemeElementHelper;
import org.eclipse.ui.internal.themes.ThemeRegistry;
import org.eclipse.ui.internal.themes.ThemeRegistryReader;
import org.eclipse.ui.themes.ITheme;
import org.eclipse.ui.themes.IThemeManager;

class ExtensionEventHandler implements IRegistryChangeListener {

    private static final String TAG_CATEGORY = "category";//$NON-NLS-1$

    private static final String ATT_TARGET_ID = "targetID";//$NON-NLS-1$

    private static final String TAG_PART = "part";//$NON-NLS-1$

    private static final String ATT_ID = "id";//$NON-NLS-1$

    private static final String TAG_PROVIDER = "imageprovider";//$NON-NLS-1$

    private static final String TAG_ACTION_SET_PART_ASSOCIATION = "actionSetPartAssociation"; //$NON-NLS-1$	

    private Workbench workbench;

    private List changeList = new ArrayList(10);

    public ExtensionEventHandler(Workbench workbench) {
        this.workbench = workbench;
    }

    public void registryChanged(IRegistryChangeEvent event) {
        try {
            IExtensionDelta delta[] = event
                    .getExtensionDeltas(WorkbenchPlugin.PI_WORKBENCH);
            IExtension ext;
            IExtensionPoint extPt;
            IWorkbenchWindow[] win = PlatformUI.getWorkbench()
                    .getWorkbenchWindows();
            if (win.length == 0)
                return;
            Display display = win[0].getShell().getDisplay();
            if (display == null)
                return;
            ArrayList appearList = new ArrayList(5);
            ArrayList revokeList = new ArrayList(5);
            String id = null;
            int numPerspectives = 0;
            int numActionSetPartAssoc = 0;

            // push action sets and perspectives to the top because incoming 
            // actionSetPartAssociations and perspectiveExtensions may depend upon 
            // them for their bindings.		
            for (int i = 0; i < delta.length; i++) {
                id = delta[i].getExtensionPoint().getSimpleIdentifier();
                if (delta[i].getKind() == IExtensionDelta.ADDED) {
                    if (id.equals(IWorkbenchConstants.PL_ACTION_SETS))
                        appearList.add(0, delta[i]);
                    else if (!id.equals(IWorkbenchConstants.PL_PERSPECTIVES)
                            && !id.equals(IWorkbenchConstants.PL_VIEWS)
                            && !id.equals(IWorkbenchConstants.PL_ACTION_SETS))
                        appearList.add(appearList.size() - numPerspectives,
                                delta[i]);
                } else {
                    if (delta[i].getKind() == IExtensionDelta.REMOVED) {
                        if (id
                                .equals(IWorkbenchConstants.PL_ACTION_SET_PART_ASSOCIATIONS)) {
                            revokeList.add(0, delta[i]);
                            numActionSetPartAssoc++;
                        } else if (id
                                .equals(IWorkbenchConstants.PL_PERSPECTIVES))
                            revokeList.add(numActionSetPartAssoc, delta[i]);
                        else
                            revokeList.add(delta[i]);
                    }
                }
            }
            Iterator iter = appearList.iterator();
            IExtensionDelta extDelta = null;
            while (iter.hasNext()) {
                extDelta = (IExtensionDelta) iter.next();
                extPt = extDelta.getExtensionPoint();
                ext = extDelta.getExtension();
                asyncAppear(display, extPt, ext);
            }
            // Suspend support for removing a plug-in until this is more stable
            //		iter = revokeList.iterator();
            //		while(iter.hasNext()) {
            //			extDelta = (IExtensionDelta) iter.next();
            //			extPt = extDelta.getExtensionPoint();
            //			ext = extDelta.getExtension();
            //			asyncRevoke(display, extPt, ext);
            //		}

            resetCurrentPerspective(display);
        } finally {
            //ensure the list is cleared for the next pass through
            changeList.clear();
        }

    }

    private void asyncAppear(Display display, final IExtensionPoint extpt,
            final IExtension ext) {
        Runnable run = new Runnable() {
            public void run() {
                appear(extpt, ext);
            }
        };
        display.syncExec(run);
    }

    private void asyncRevoke(Display display, final IExtensionPoint extpt,
            final IExtension ext) {
        Runnable run = new Runnable() {
            public void run() {
                revoke(extpt, ext);
            }
        };
        display.syncExec(run);
    }

    private void appear(IExtensionPoint extPt, IExtension ext) {
        String name = extPt.getSimpleIdentifier();
        if (name.equalsIgnoreCase(IWorkbenchConstants.PL_EDITOR)) {
            loadEditor(ext);
            return;
        }
        if (name
                .equalsIgnoreCase(IWorkbenchConstants.PL_PERSPECTIVE_EXTENSIONS)) {
            loadPerspectiveExtensions(ext);
            return;
        }
        if (name
                .equalsIgnoreCase(IWorkbenchConstants.PL_ACTION_SET_PART_ASSOCIATIONS)) {
            loadActionSetPartAssociation(ext);
            return;
        }
        if (name.equalsIgnoreCase(IWorkbenchConstants.PL_WORKINGSETS)) {
            loadWorkingSets(ext);
            return;
        }
        if (name.equalsIgnoreCase(IWorkbenchConstants.PL_POPUP_MENU)) {
            loadPopupMenu(ext);
            return;
        }
        if (name.equalsIgnoreCase(IWorkbenchConstants.PL_PREFERENCES)) {
            loadPreferencePages(ext);
            return;
        }
        if (name.equalsIgnoreCase(IWorkbenchConstants.PL_PROPERTY_PAGES)) {
            loadPropertyPages(ext);
            return;
        }
        if (name.equalsIgnoreCase(IWorkbenchConstants.PL_FONT_DEFINITIONS)) {
            loadFontDefinitions(ext);
            return;
        }
        if (name.equalsIgnoreCase(IWorkbenchConstants.PL_DECORATORS)) {
            loadDecorators(ext);
            return;
        }
        if (name.equalsIgnoreCase(IWorkbenchConstants.PL_THEMES)) {
            loadThemes(ext);
            return;
        }
    }

    /**
     * @param ext
     */
    private void loadFontDefinitions(IExtension ext) {
        ThemeRegistryReader reader = new ThemeRegistryReader();
        reader.setRegistry((ThemeRegistry) WorkbenchPlugin.getDefault()
                .getThemeRegistry());
        IConfigurationElement[] elements = ext.getConfigurationElements();
        for (int i = 0; i < elements.length; i++)
            reader.readElement(elements[i]);

        Collection fonts = reader.getFontDefinitions();
        FontDefinition[] fontDefs = (FontDefinition[]) fonts
                .toArray(new FontDefinition[fonts.size()]);
        ThemeElementHelper.populateRegistry(workbench.getThemeManager()
                .getTheme(IThemeManager.DEFAULT_THEME), fontDefs, workbench
                .getPreferenceStore());
    }

    //TODO: confirm
    private void loadThemes(IExtension ext) {
        ThemeRegistryReader reader = new ThemeRegistryReader();
        ThemeRegistry registry = (ThemeRegistry) WorkbenchPlugin.getDefault()
                .getThemeRegistry();
        reader.setRegistry(registry);
        IConfigurationElement[] elements = ext.getConfigurationElements();
        for (int i = 0; i < elements.length; i++)
            reader.readElement(elements[i]);

        Collection colors = reader.getColorDefinitions();
        ColorDefinition[] colorDefs = (ColorDefinition[]) colors
                .toArray(new ColorDefinition[colors.size()]);

        ITheme theme = workbench.getThemeManager().getTheme(
                IThemeManager.DEFAULT_THEME);
        ThemeElementHelper.populateRegistry(theme, colorDefs, workbench
                .getPreferenceStore());

        Collection fonts = reader.getFontDefinitions();
        FontDefinition[] fontDefs = (FontDefinition[]) fonts
                .toArray(new FontDefinition[fonts.size()]);
        ThemeElementHelper.populateRegistry(theme, fontDefs, workbench
                .getPreferenceStore());

        Map data = reader.getData();
        registry.addData(data);
    }

    private void loadDecorators(IExtension ext) {
        DecoratorRegistryReader reader = new DecoratorRegistryReader();
        IConfigurationElement[] elements = ext.getConfigurationElements();
        for (int i = 0; i < elements.length; i++) {
            reader.readElement(elements[i]);
        }

        Collection decorators = reader.getValues();
        DecoratorManager manager = (DecoratorManager) workbench
                .getDecoratorManager();
        for (Iterator i = decorators.iterator(); i.hasNext();) {
            manager.addDecorator((DecoratorDefinition) i.next());
        }
    }

    private void loadPropertyPages(IExtension ext) {
        PropertyPageContributorManager manager = PropertyPageContributorManager
                .getManager();
        PropertyPagesRegistryReader reader = new PropertyPagesRegistryReader(
                manager);
        IConfigurationElement[] elements = ext.getConfigurationElements();
        for (int i = 0; i < elements.length; i++) {
            reader.readElement(elements[i]);
        }
    }

    private void loadPreferencePages(IExtension ext) {
        PreferenceManager manager = workbench.getPreferenceManager();
        List nodes = manager.getElements(PreferenceManager.POST_ORDER);
        IConfigurationElement[] elements = ext.getConfigurationElements();
        for (int i = 0; i < elements.length; i++) {
            WorkbenchPreferenceNode node = PreferencePageRegistryReader
                    .createNode(workbench, elements[i]);
            if (node == null)
                continue;
            String category = node.getCategory();
            if (category == null) {
                manager.addToRoot(node);
            } else {
                WorkbenchPreferenceNode parent = null;
                for (Iterator j = nodes.iterator(); j.hasNext();) {
                    WorkbenchPreferenceNode element = (WorkbenchPreferenceNode) j
                            .next();
                    if (category.equals(element.getId())) {
                        parent = element;
                        break;
                    }
                }
                if (parent == null) {
                    //Could not find the parent - log
                    WorkbenchPlugin
                            .log("Invalid preference page path: " + category); //$NON-NLS-1$
                    manager.addToRoot(node);
                } else {
                    parent.add(node);
                }
            }
        }
    }

    /**
     * TODO: object contributions are easy to update, but viewer contributions are not because they're 
     * statically cached in anonymous PopupMenuExtenders.  Currently you will be prompted to restart in 
     * the case of a viewer contribtion. 
     * 
     * We can implement this refresh by keeping a weak set of references to PopupMenuExtenders and 
     * iterating over them on a delta.  We add a method to PopupMenuExtender that will supply an extension
     * to the underlying staticActionBuilder for processing. 
     */
    private void loadPopupMenu(IExtension ext) {
        ObjectActionContributorManager oMan = ObjectActionContributorManager
                .getManager();
        ObjectActionContributorReader oReader = new ObjectActionContributorReader();
        oReader.setManager(oMan);
        IConfigurationElement[] elements = ext.getConfigurationElements();
        boolean clearPopups = false;
        // takes care of object contributions
        for (int i = 0; i < elements.length; i++) {
            oReader.readElement(elements[i]);
            if (elements[i].getName().equals(
                    ViewerActionBuilder.TAG_CONTRIBUTION_TYPE))
                clearPopups = true;
        }

        if (clearPopups)
            PopupMenuExtender.getManager().clearCaches();
    }

    private void revoke(IExtensionPoint extPt, IExtension ext) {
        String name = extPt.getSimpleIdentifier();
        if (name.equalsIgnoreCase(IWorkbenchConstants.PL_NEW)) {
            //NewWizardsRegistryReader.removeExtension(ext);
            return;
        }
        if (name.equalsIgnoreCase(IWorkbenchConstants.PL_VIEWS)) {
            unloadView(ext);
            return;
        }
        
        if (name.equalsIgnoreCase(IWorkbenchConstants.PL_PERSPECTIVES)) {
            unloadPerspective(ext);
            return;
        }

        if (name.equalsIgnoreCase(IWorkbenchConstants.PL_ACTION_SETS)) {
            unloadActionSets(ext);
            return;
        }

        if (name
                .equalsIgnoreCase(IWorkbenchConstants.PL_ACTION_SET_PART_ASSOCIATIONS)) {
            unloadActionSetPartAssociation(ext);
            return;
        }
        if (name.equalsIgnoreCase(IWorkbenchConstants.PL_WORKINGSETS)) {
            unloadWorkingSets(ext);
            return;
        }

    }

    private void restoreViewState(MultiStatus result, String id) {
        IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
        IMemento memento;
        for (int i = 0; i < windows.length; i++) {
            WorkbenchWindow window = (WorkbenchWindow) windows[i];
            IWorkbenchPage[] pages = window.getPages();
            for (int j = 0; j < pages.length; j++) {
                memento = (IMemento) ((WorkbenchPage) pages[j]).getStateMap()
                        .remove(id);
                if (memento == null)
                    continue;
                IMemento[] viewMems = memento
                        .getChildren(IWorkbenchConstants.TAG_VIEW);
                ViewFactory viewFactory = ((WorkbenchPage) pages[j])
                        .getViewFactory();
                for (int k = 0; k < viewMems.length; k++) {
                    viewFactory.restoreViewState(viewMems[k]);
                    createOpenPerspectiveView(pages[j], viewFactory,
                            viewMems[k]);
                }
            }
        }
    }

    private void createOpenPerspectiveView(IWorkbenchPage page,
            ViewFactory viewFactory, IMemento memento) {
        String id = memento.getString(IWorkbenchConstants.TAG_ID);
        String perspId = memento.getString(IWorkbenchConstants.TAG_PERSPECTIVE);
        Perspective persp = ((WorkbenchPage) page).getActivePerspective();
        if (persp.getDesc().getId().equals(perspId)) {
            try {
                viewFactory.createView(id);
                page.showView(id);
            } catch (PartInitException e) {
            }
        }
    }

    private void unloadView(IExtension ext) {
//        final MultiStatus result = new MultiStatus(PlatformUI.PLUGIN_ID,
//                IStatus.OK, WorkbenchMessages
//                        .getString("ViewFactory.problemsSavingViews"), null); //$NON-NLS-1$
//        IViewRegistry vReg = WorkbenchPlugin.getDefault().getViewRegistry();
//        IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
//        XMLMemento memento = null;
//        for (int i = 0; i < windows.length; i++) {
//            WorkbenchWindow window = (WorkbenchWindow) windows[i];
//            IWorkbenchPage[] pages = window.getPages();
//            for (int j = 0; j < pages.length; j++) {
//                ArrayList viewsRemoved = new ArrayList();
//                IConfigurationElement[] elements = ext
//                        .getConfigurationElements();
//                for (int k = 0; k < elements.length; k++) {
//                    if (!elements[k].getName().equals(
//                            IWorkbenchConstants.TAG_VIEW))
//                        continue;
//                    String id = elements[k]
//                            .getAttribute(IWorkbenchConstants.TAG_ID);
//                    if (id != null) {
//                        ViewFactory viewFactory = ((WorkbenchPage) pages[j])
//                                .getViewFactory();
//                        IViewReference viewRef = viewFactory.getView(id);
//                        if (viewRef != null) {
//                            // don't save view state
//                            //							if (isViewOpen(viewRef, viewFactory)){
//                            //								memento = XMLMemento.createWriteRoot(IWorkbenchConstants.TAG_VIEWS);
//                            //								saveViewState(pages[j], id, viewFactory.saveViewState(memento, viewRef, result));
//                            //								//((WorkbenchPage)pages[j]).getStateMap().put(id, memento);
//                            //							}
//                            ((WorkbenchPage) pages[j]).hideView(viewRef);
//                            ((WorkbenchPage) pages[j]).getViewFactory()
//                                    .releaseView(viewRef);
//                        }
//                        viewsRemoved.add(id);
//                        ((ViewRegistry) vReg).remove(id);
//                    }
//                }
//                Object[] showViewIdsRemoved = findShowViewIdsRemoved(
//                        ((WorkbenchPage) pages[j]).getShowViewActionIds(),
//                        viewsRemoved);
//                if (showViewIdsRemoved.length > 0)
//                    removeViewIdsFromShowViewMenu(window, showViewIdsRemoved);
//            }
//        }
//        if (result.getSeverity() != IStatus.OK) {
//            ErrorDialog.openError((Shell) null, WorkbenchMessages
//                    .getString("Workbench.problemsSaving"), //$NON-NLS-1$
//                    WorkbenchMessages.getString("Workbench.problemsSavingMsg"), //$NON-NLS-1$
//                    result);
//        }
    }

    private void saveViewState(IWorkbenchPage page, String id, IMemento memento) {
        Perspective persp = ((WorkbenchPage) page).getActivePerspective();
        if (persp.findView(id) != null)
            memento.putString(IWorkbenchConstants.TAG_PERSPECTIVE, persp
                    .getDesc().getId());
    }

    private Object[] findShowViewIdsRemoved(ArrayList showViewIds,
            ArrayList viewsRemoved) {
        ArrayList list = new ArrayList();
        Object[] showViewIdList = showViewIds.toArray();
        Object[] viewsRemovedList = viewsRemoved.toArray();
        for (int i = 0; i < showViewIdList.length; i++)
            for (int j = 0; j < viewsRemovedList.length; j++)
                if (((String) viewsRemovedList[j])
                        .equals((String) showViewIdList[i]))
                    list.add(viewsRemovedList[j]);
        return list.toArray();
    }

    private void removeViewIdsFromShowViewMenu(IWorkbenchWindow window,
            Object[] viewsRemoved) {
        MenuManager menuManager = ((WorkbenchWindow) window).getMenuManager();
        IContributionItem[] items = menuManager.getItems();
        menuManager = null;
        for (int i = 0; i < items.length; i++)
            if (items[i] instanceof MenuManager
                    && ((MenuManager) items[i]).getMenuText().equals("&Window")) { //$NON-NLS-1$
                menuManager = (MenuManager) items[i];
                break;
            }
        if (menuManager == null)
            return;
        items = menuManager.getItems();
        menuManager = null;
        for (int i = 0; i < items.length; i++)
            if (items[i] instanceof MenuManager
                    && ((MenuManager) items[i]).getMenuText().equals(
                            "Show &View")) { //$NON-NLS-1$
                menuManager = (MenuManager) items[i];
                break;
            }
        if (menuManager == null)
            return;
        items = menuManager.getItems();
        if (items.length < 1 || !(items[0] instanceof ShowViewMenu))
            return;

        for (int i = 0; i < viewsRemoved.length; i++)
            ((ShowViewMenu) items[0]).removeAction((String) viewsRemoved[i]);
    }

    private void loadEditor(IExtension ext) {
        MultiStatus result = new MultiStatus(
                PlatformUI.PLUGIN_ID,
                IStatus.OK,
                WorkbenchMessages.getString("Workbench.problemsRestoring"), null); //$NON-NLS-1$
        IEditorRegistry eReg = WorkbenchPlugin.getDefault().getEditorRegistry();
        EditorRegistryReader eReader = new EditorRegistryReader();
        IConfigurationElement[] elements = ext.getConfigurationElements();
        for (int i = 0; i < elements.length; i++) {
            String id = elements[i].getAttribute(IWorkbenchConstants.TAG_ID);
            if (id != null && eReg.findEditor(id) != null)
                continue;
            eReader.readElement((EditorRegistry) eReg, elements[i]);
            //restoreEditorState(elements[i], result);
        }
        if (result.getSeverity() == IStatus.ERROR) {
            ErrorDialog.openError(null, WorkbenchMessages
                    .getString("Workspace.problemsTitle"), //$NON-NLS-1$
                    WorkbenchMessages
                            .getString("Workbench.problemsRestoringMsg"), //$NON-NLS-1$
                    result);
        }
    }

    private void restoreEditorState(IConfigurationElement element,
            MultiStatus result) {
        String id = element.getAttribute(IWorkbenchConstants.TAG_ID);
        IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
        IMemento memento;
        for (int i = 0; i < windows.length; i++) {
            WorkbenchWindow window = (WorkbenchWindow) windows[i];
            IWorkbenchPage[] pages = window.getPages();
            for (int j = 0; j < pages.length; j++) {
                IEditorReference existingVisible = ((WorkbenchPage) pages[j])
                        .getEditorPresentation().getVisibleEditor();
                EditorManager editorManager = ((WorkbenchPage) pages[j])
                        .getEditorManager();
                ArrayList visibleEditors = new ArrayList(5);
                IEditorPart activeEditor[] = new IEditorPart[1];
                ArrayList errorWorkbooks = new ArrayList(1);
                memento = (IMemento) ((WorkbenchPage) pages[j]).getStateMap()
                        .remove(id);
                if (memento == null)
                    continue;
                IMemento[] editorMems = memento
                        .getChildren(IWorkbenchConstants.TAG_EDITOR);
                for (int k = 0; k < editorMems.length; k++) {
                    if (!checkOpenable(editorMems[k], pages[j]))
                        continue;
                    editorManager.restoreEditorState(editorMems[k],
                            visibleEditors, activeEditor, errorWorkbooks,
                            result);
                }
                if (existingVisible == null)
                    for (int k = 0; k < visibleEditors.size(); k++)
                        editorManager
                                .setVisibleEditor(
                                        (IEditorReference) visibleEditors
                                                .get(k), false);
                else
                    editorManager.setVisibleEditor(existingVisible, true);

                if (visibleEditors.size() == 1)
                    for (Iterator iter = errorWorkbooks.iterator(); iter
                            .hasNext();) {
                        iter.next();
                        ((WorkbenchPage) pages[j]).getEditorPresentation()
                                .fixVisibleEditor();
                    }
            }
        }
    }

    private boolean checkOpenable(IMemento memento, IWorkbenchPage page) {
        IMemento inputMem = memento.getChild(IWorkbenchConstants.TAG_INPUT);
        String factoryID = null;
        if (inputMem != null)
            factoryID = inputMem.getString(IWorkbenchConstants.TAG_FACTORY_ID);
        if (factoryID == null)
            return false;
        IElementFactory factory = WorkbenchPlugin.getDefault()
                .getElementFactory(factoryID);
        if (factory == null)
            return false;
        IAdaptable input = factory.createElement(inputMem);
        if (input == null || !(input instanceof IEditorInput))
            return false;
        IEditorInput editorInput = (IEditorInput) input;
        IEditorReference edRefs[] = page.getEditorReferences();

        for (int i = 0; i < edRefs.length; i++) {
            IEditorPart editor = edRefs[i].getEditor(false);
            if (editor == null)
                continue;
            IEditorInput edInput = editor.getEditorInput();
            if (edInput.equals(editorInput))
                return false;
        }
        return true;
    }


    private void closeEditors(IWorkbenchPage page, String id, MultiStatus result) {
        XMLMemento memento = XMLMemento
                .createWriteRoot(IWorkbenchConstants.TAG_EDITORS);
        IEditorReference[] editorRefs = page.getEditorReferences();
        boolean changed = false;
        for (int i = 0; i < editorRefs.length; i++) {
            if (editorRefs[i].getId().equals(id)) {
                IEditorReference e = editorRefs[i];
                IEditorPart editor = e.getEditor(true);
                EditorManager editorManager = ((WorkbenchPage) page)
                        .getEditorManager();
                if (editor == null) {
                    IMemento mem = editorManager.getMemento(editorRefs[i]);
                    if (mem != null) {
                        IMemento editorMem = memento
                                .createChild(IWorkbenchConstants.TAG_EDITOR);
                        editorMem.putMemento(mem);
                    }
                } else
                    editorManager.saveEditorState(memento, e, result);
                //((WorkbenchPage)page).getStateMap().put(id, memento);
                page.closeEditor(editor, true);
                changed = true;
            }
        }
    }

    private void unloadPerspective(IExtension ext) {
        final MultiStatus result = new MultiStatus(PlatformUI.PLUGIN_ID,
                IStatus.OK, WorkbenchMessages
                        .getString("ViewFactory.problemsSavingViews"), null); //$NON-NLS-1$
        IPerspectiveRegistry pReg = WorkbenchPlugin.getDefault()
                .getPerspectiveRegistry();
        IConfigurationElement[] elements = ext.getConfigurationElements();
        for (int i = 0; i < elements.length; i++) {
            if (!elements[i].getName().equals(
                    IWorkbenchConstants.TAG_PERSPECTIVE))
                continue;
            String id = elements[i].getAttribute(IWorkbenchConstants.TAG_ID);
            if (id == null)
                continue;
            IPerspectiveDescriptor desc = pReg.findPerspectiveWithId(id);
            if (desc == null)
                continue;
            ((PerspectiveRegistry) pReg).deletePerspective(desc);
            IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
            for (int j = 0; j < windows.length; j++) {
                WorkbenchWindow window = (WorkbenchWindow) windows[j];
                IWorkbenchPage[] pages = window.getPages();
                for (int k = 0; k < pages.length; k++) {
                    //					Perspective persp = ((WorkbenchPage)pages[k]).findPerspective(desc);
                    //					if (persp == null)
                    //						return;
                    //					XMLMemento memento = XMLMemento.createWriteRoot(IWorkbenchConstants.TAG_PERSPECTIVE);
                    //					result.merge(persp.saveState(memento));
                    ((WorkbenchPage) pages[k]).closePerspective(desc, true);
                    //((WorkbenchPage)pages[k]).getStateMap().put(id, memento);				
                }
            }
            //((Workbench)workbench).getPerspectiveHistory().removeItem(desc);
        }
        if (result.getSeverity() != IStatus.OK) {
            ErrorDialog.openError((Shell) null, WorkbenchMessages
                    .getString("Workbench.problemsSaving"), //$NON-NLS-1$
                    WorkbenchMessages.getString("Workbench.problemsSavingMsg"), //$NON-NLS-1$
                    result);
        }
    }

    private void loadPerspectiveExtensions(IExtension ext) {
        IWorkbenchWindow window = PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow();
        if (window == null)
            return;
        IWorkbenchPage page = window.getActivePage();
        if (page == null)
            return;

        // Get the current perspective.
        IPerspectiveDescriptor persp = page.getPerspective();
        if (persp == null)
            return;
        String currentId = persp.getId();
        IConfigurationElement[] elements = ext.getConfigurationElements();
        for (int i = 0; i < elements.length; i++) {
            // If any of these refer to the current perspective, output
            // a message saying this perspective will need to be reset
            // in order to see the changes.  For any other case, the
            // perspective extension registry will be rebuilt anyway so
            // just ignore it.
            String id = elements[i].getAttribute(ATT_TARGET_ID);
            if (id == null)
                continue;
            if (id.equals(currentId)) {
                // Display message
                changeList
                        .add(MessageFormat
                                .format(
                                        ExtensionEventHandlerMessages
                                                .getString("ExtensionEventHandler.change_format"), //$NON-NLS-1$ 
                                        new Object[] {
                                                ext.getNamespace(),
                                                ExtensionEventHandlerMessages
                                                        .getString("ExtensionEventHandler.newPerspectiveExtension") })); //$NON-NLS-1$ 				
                break;
            }
        }
    }

    private void restorePerspectiveState(MultiStatus result, String id) {
        IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
        IMemento memento;
        for (int i = 0; i < windows.length; i++) {
            WorkbenchWindow window = (WorkbenchWindow) windows[i];
            IWorkbenchPage[] pages = window.getPages();
            // count in reverse order since we insert perspectives at the beginning
            for (int j = pages.length - 1; j >= 0; j--) {
                memento = (IMemento) ((WorkbenchPage) pages[j]).getStateMap()
                        .remove(id);
                if (memento == null)
                    continue;
                try {
                    Perspective persp = new Perspective(null,
                            ((WorkbenchPage) pages[j]));
                    result.merge(persp.restoreState(memento));
                    ((WorkbenchPage) pages[j]).addPerspective(persp);
                    ((WorkbenchWindow) windows[i]).addPerspectiveShortcut(persp
                            .getDesc(), (WorkbenchPage) pages[j]);
                } catch (WorkbenchException e) {
                }
            }
        }
    }

    private void resetCurrentPerspective(Display display) {
        if (changeList.isEmpty())
            return;

        final StringBuffer message = new StringBuffer(
                ExtensionEventHandlerMessages
                        .getString("ExtensionEventHandler.following_changes")); //$NON-NLS-1$

        for (Iterator i = changeList.iterator(); i.hasNext();) {
            message.append(i.next());
        }

        message.append(ExtensionEventHandlerMessages
                .getString("ExtensionEventHandler.need_to_reset")); //$NON-NLS-1$

        display.asyncExec(new Runnable() {
            public void run() {
                Shell parentShell = null;
                IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
                if (window == null) {
                    if (workbench.getWorkbenchWindowCount() == 0)
                        return;
                    window = workbench.getWorkbenchWindows()[0];
                }

                parentShell = window.getShell();

                if (MessageDialog
                        .openQuestion(
                                parentShell,
                                ExtensionEventHandlerMessages
                                        .getString("ExtensionEventHandler.reset_perspective"), message.toString())) { //$NON-NLS-1$
                    IWorkbenchPage page = window.getActivePage();
                    if (page == null)
                        return;
                    page.resetPerspective();
                }
            }
        });

    }

    private void unloadActionSets(IExtension ext) {
        ActionSetRegistry aReg = WorkbenchPlugin.getDefault()
                .getActionSetRegistry();
        IConfigurationElement[] elements = ext.getConfigurationElements();
        IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
        for (int i = 0; i < windows.length; i++) {
            WorkbenchWindow window = (WorkbenchWindow) windows[i];
            IWorkbenchPage[] pages = window.getPages();
            for (int j = 0; j < pages.length; j++) {
                for (int k = 0; k < elements.length; k++) {
                    if (!elements[k].getName().equals(
                            IWorkbenchConstants.TAG_ACTION_SET))
                        continue;
                    String id = elements[k]
                            .getAttribute(IWorkbenchConstants.TAG_ID);
                    if (id != null) {
                        aReg.remove(id);
                        removeActionSet((WorkbenchPage) pages[j], id);
                    }
                }
            }
        }
    }

    private void removeActionSet(WorkbenchPage page, String id) {
        Perspective persp = page.getActivePerspective();
        ActionPresentation actionPresentation = ((WorkbenchWindow) page
                .getWorkbenchWindow()).getActionPresentation();
        IActionSet[] actionSets = actionPresentation.getActionSets();
        for (int i = 0; i < actionSets.length; i++) {
            IActionSetDescriptor desc = ((PluginActionSet) actionSets[i])
                    .getDesc();
            if (id.equals(desc.getId())) {
                PluginActionSetBuilder builder = new PluginActionSetBuilder();
                builder.removeActionExtensions((PluginActionSet) actionSets[i],
                        page.getWorkbenchWindow());
                actionPresentation.removeActionSet(desc);
            }
        }
        if (persp != null)
            persp.removeActionSet(id);
    }

    private void loadActionSetPartAssociation(IExtension ext) {
        ActionSetRegistry aReg = WorkbenchPlugin.getDefault()
                .getActionSetRegistry();
        ActionSetPartAssociationsReader reader = new ActionSetPartAssociationsReader(
                aReg);
        IConfigurationElement[] elements = ext.getConfigurationElements();
        for (int i = 0; i < elements.length; i++)
            reader.readElement(elements[i]);
    }

    private void unloadActionSetPartAssociation(IExtension ext) {
        ActionSetRegistry aReg = WorkbenchPlugin.getDefault()
                .getActionSetRegistry();
        IConfigurationElement[] elements = ext.getConfigurationElements();
        for (int i = 0; i < elements.length; i++) {
            String type = elements[i].getName();
            if (!type.equals(TAG_ACTION_SET_PART_ASSOCIATION))
                continue;
            String actionSetId = elements[i].getAttribute(ATT_TARGET_ID);
            IConfigurationElement[] children = elements[i].getChildren();
            for (int j = 0; j < children.length; j++) {
                IConfigurationElement child = children[j];
                type = child.getName();
                if (type.equals(TAG_PART)) {
                    String partId = child.getAttribute(ATT_ID);
                    if (partId != null)
                        aReg.removeAssociation(actionSetId, partId);
                }
            }
        }
    }

    private void loadWorkingSets(IExtension ext) {
        WorkingSetRegistry wReg = (WorkingSetRegistry) WorkbenchPlugin
                .getDefault().getWorkingSetRegistry();
        WorkingSetRegistryReader reader = new WorkingSetRegistryReader(wReg);
        IConfigurationElement[] elements = ext.getConfigurationElements();
        for (int i = 0; i < elements.length; i++)
            reader.readElement(elements[i]);
    }

    private void unloadWorkingSets(IExtension ext) {
        WorkingSetRegistry wReg = (WorkingSetRegistry) WorkbenchPlugin
                .getDefault().getWorkingSetRegistry();
        IConfigurationElement[] elements = ext.getConfigurationElements();
        for (int i = 0; i < elements.length; i++)
            wReg.removeWorkingSetDescriptor(elements[i]
                    .getAttribute(IWorkbenchConstants.TAG_ID));
    }

    private void stopView(IExtension ext) {
        IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
        for (int i = 0; i < windows.length; i++) {
            WorkbenchWindow window = (WorkbenchWindow) windows[i];
            IWorkbenchPage[] pages = window.getPages();
            for (int j = 0; j < pages.length; j++) {
                IConfigurationElement[] elements = ext
                        .getConfigurationElements();
                for (int k = 0; k < elements.length; k++) {
                    if (!elements[k].getName().equals(
                            IWorkbenchConstants.TAG_VIEW))
                        continue;
                    String id = elements[k]
                            .getAttribute(IWorkbenchConstants.TAG_ID);
                    if (id != null) {
                        ViewFactory viewFactory = ((WorkbenchPage) pages[j])
                                .getViewFactory();
                        IViewReference viewRef = viewFactory.getView(id);
                        if (viewRef != null) {
                            ((WorkbenchPage) pages[j]).hideView(viewRef);
                            ((WorkbenchPage) pages[j]).getViewFactory()
                                    .releaseView(viewRef);
                        }
                    }
                }
            }
        }
    }

    private void stopEditor(IExtension ext) {
        IConfigurationElement[] elements = ext.getConfigurationElements();
        IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
        for (int i = 0; i < elements.length; i++) {
            String id = elements[i].getAttribute(IWorkbenchConstants.TAG_ID);
            for (int j = 0; j < windows.length; j++) {
                WorkbenchWindow window = (WorkbenchWindow) windows[j];
                IWorkbenchPage[] pages = window.getPages();
                for (int k = 0; k < pages.length; k++) {
                    IEditorReference[] editorRefs = pages[k]
                            .getEditorReferences();
                    for (int l = 0; l < editorRefs.length; l++)
                        if (editorRefs[l].getId().equals(id)) {
                            IEditorPart editor = editorRefs[l].getEditor(true);
                            if (editor != null)
                                pages[k].closeEditor(editor, true);
                        }
                }
            }
        }
    }

    private void stopPerspective(IExtension ext) {
        IPerspectiveRegistry pReg = WorkbenchPlugin.getDefault()
                .getPerspectiveRegistry();
        IConfigurationElement[] elements = ext.getConfigurationElements();
        for (int i = 0; i < elements.length; i++) {
            if (!elements[i].getName().equals(
                    IWorkbenchConstants.TAG_PERSPECTIVE))
                continue;
            String id = elements[i].getAttribute(IWorkbenchConstants.TAG_ID);
            if (id == null)
                continue;
            IPerspectiveDescriptor desc = pReg.findPerspectiveWithId(id);
            if (desc == null)
                continue;
            ((PerspectiveRegistry) pReg).deletePerspective(desc);
            IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
            for (int j = 0; j < windows.length; j++) {
                WorkbenchWindow window = (WorkbenchWindow) windows[j];
                IWorkbenchPage[] pages = window.getPages();
                for (int k = 0; k < pages.length; k++) {
                    Perspective persp = ((WorkbenchPage) pages[k])
                            .findPerspective(desc);
                    if (persp == null)
                        return;
                    ((WorkbenchPage) pages[k]).closePerspective(desc, true);
                }
            }
        }
    }

    private void stopActionSets(IExtension ext) {
        ActionSetRegistry aReg = (ActionSetRegistry) WorkbenchPlugin
                .getDefault().getActionSetRegistry();
        IConfigurationElement[] elements = ext.getConfigurationElements();
        IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
        for (int i = 0; i < windows.length; i++) {
            WorkbenchWindow window = (WorkbenchWindow) windows[i];
            IWorkbenchPage[] pages = window.getPages();
            for (int j = 0; j < pages.length; j++) {
                for (int k = 0; k < elements.length; k++) {
                    if (!elements[k].getName().equals(
                            IWorkbenchConstants.TAG_ACTION_SET))
                        continue;
                    String id = elements[k]
                            .getAttribute(IWorkbenchConstants.TAG_ID);
                    if (id != null)
                        ((WorkbenchPage) pages[j]).hideActionSet(id);
                }
            }
        }
    }
}