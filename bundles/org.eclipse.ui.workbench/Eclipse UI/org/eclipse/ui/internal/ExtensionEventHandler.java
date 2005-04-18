/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionDelta;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.runtime.IRegistryChangeListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.internal.registry.ActionSetRegistry;
import org.eclipse.ui.internal.registry.IActionSet;
import org.eclipse.ui.internal.registry.IActionSetDescriptor;
import org.eclipse.ui.internal.registry.PerspectiveRegistry;
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
    
    private void appear(IExtensionPoint extPt, IExtension ext) {
        String name = extPt.getSimpleIdentifier();
        if (name.equalsIgnoreCase(IWorkbenchConstants.PL_WORKINGSETS)) {
            loadWorkingSets(ext);
            return;
        }
        if (name.equalsIgnoreCase(IWorkbenchConstants.PL_FONT_DEFINITIONS)) {
            loadFontDefinitions(ext);
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

    private void revoke(IExtensionPoint extPt, IExtension ext) {
        String name = extPt.getSimpleIdentifier();
        
        if (name.equalsIgnoreCase(IWorkbenchConstants.PL_PERSPECTIVES)) {
            unloadPerspective(ext);
            return;
        }

        if (name.equalsIgnoreCase(IWorkbenchConstants.PL_WORKINGSETS)) {
            unloadWorkingSets(ext);
            return;
        }

    }


    private void unloadPerspective(IExtension ext) {
        final MultiStatus result = new MultiStatus(PlatformUI.PLUGIN_ID,
                IStatus.OK, WorkbenchMessages.ViewFactory_problemsSavingViews, null);
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
                    pages[k].closePerspective(desc, true, true);
                    //((WorkbenchPage)pages[k]).getStateMap().put(id, memento);				
                }
            }
            //((Workbench)workbench).getPerspectiveHistory().removeItem(desc);
        }
        if (result.getSeverity() != IStatus.OK) {
            ErrorDialog.openError((Shell) null, WorkbenchMessages.Workbench_problemsSaving,
                    WorkbenchMessages.Workbench_problemsSavingMsg,
                    result);
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
                } catch (WorkbenchException e) {
                }
            }
        }
    }

    private void resetCurrentPerspective(Display display) {
        if (changeList.isEmpty())
            return;

        final StringBuffer message = new StringBuffer(
                ExtensionEventHandlerMessages.ExtensionEventHandler_following_changes);

        for (Iterator i = changeList.iterator(); i.hasNext();) {
            message.append(i.next());
        }

        message.append(ExtensionEventHandlerMessages.ExtensionEventHandler_need_to_reset);

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
                                ExtensionEventHandlerMessages.ExtensionEventHandler_reset_perspective, message.toString())) {
                    IWorkbenchPage page = window.getActivePage();
                    if (page == null)
                        return;
                    page.resetPerspective();
                }
            }
        });

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
