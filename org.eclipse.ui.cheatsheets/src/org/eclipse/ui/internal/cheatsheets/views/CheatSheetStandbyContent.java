/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.cheatsheets.views;


import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.internal.cheatsheets.registry.*;
import org.eclipse.ui.intro.IIntroPart;
import org.eclipse.ui.intro.internal.parts.IStandbyContentPart;



public final class CheatSheetStandbyContent implements IStandbyContentPart {

    private IIntroPart introPart;
    private CheatSheetView cheatSheet;
    private Composite container;

    class ViewSiteAdapter implements IViewSite {

        public IActionBars getActionBars() {
            return introPart.getIntroSite().getActionBars();
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.ui.IWorkbenchPartSite#getId()
         */
        public String getId() {
            return introPart.getIntroSite().getId();
        }

        public String getSecondaryId() {
            return null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.ui.IWorkbenchPartSite#getKeyBindingService()
         */
        public IKeyBindingService getKeyBindingService() {
            return introPart.getIntroSite().getKeyBindingService();
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.ui.IWorkbenchPartSite#getPluginId()
         */
        public String getPluginId() {
            return introPart.getIntroSite().getPluginId();
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.ui.IWorkbenchPartSite#getRegisteredName()
         */
        public String getRegisteredName() {
            return introPart.getIntroSite().getRegisteredName();
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.ui.IWorkbenchPartSite#registerContextMenu(org.eclipse.jface.action.MenuManager,
         *      org.eclipse.jface.viewers.ISelectionProvider)
         */
        public void registerContextMenu(MenuManager menuManager,
                ISelectionProvider selectionProvider) {
            introPart.getIntroSite().registerContextMenu(menuManager,
                    selectionProvider);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.ui.IWorkbenchPartSite#registerContextMenu(java.lang.String,
         *      org.eclipse.jface.action.MenuManager,
         *      org.eclipse.jface.viewers.ISelectionProvider)
         */
        public void registerContextMenu(String menuId, MenuManager menuManager,
                ISelectionProvider selectionProvider) {
            registerContextMenu(menuId, menuManager, selectionProvider);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.ui.IWorkbenchSite#getPage()
         */
        public IWorkbenchPage getPage() {
            return introPart.getIntroSite().getPage();
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.ui.IWorkbenchSite#getSelectionProvider()
         */
        public ISelectionProvider getSelectionProvider() {
            return getSelectionProvider();
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.ui.IWorkbenchSite#getShell()
         */
        public Shell getShell() {
            return introPart.getIntroSite().getShell();
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.ui.IWorkbenchSite#getWorkbenchWindow()
         */
        public IWorkbenchWindow getWorkbenchWindow() {
            return introPart.getIntroSite().getWorkbenchWindow();
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.ui.IWorkbenchSite#setSelectionProvider(org.eclipse.jface.viewers.ISelectionProvider)
         */
        public void setSelectionProvider(ISelectionProvider provider) {
            introPart.getIntroSite().setSelectionProvider(provider);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
         */
        public Object getAdapter(Class adapter) {
            return introPart.getIntroSite().getAdapter(adapter);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.intro.internal.parts.IStandbyContentPart#createControl(org.eclipse.swt.widgets.Composite,
     *      org.eclipse.ui.forms.widgets.FormToolkit)
     */
    public void createPartControl(Composite parent, FormToolkit toolkit) {
        cheatSheet = new CheatSheetView();
        try {
            cheatSheet.init(new ViewSiteAdapter());
            container = toolkit.createComposite(parent);
            FillLayout layout = new FillLayout();
            layout.marginWidth = layout.marginHeight = 0;
            container.setLayout(layout);
            cheatSheet.createPartControl(container);
        } catch (PartInitException e) {
            return;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.intro.internal.parts.IStandbyContentPart#getControl()
     */
    public Control getControl() {
        return container;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.intro.internal.parts.IStandbyContentPart#init(org.eclipse.ui.intro.IIntroPart)
     */
    public void init(IIntroPart introPart) {
        this.introPart = introPart;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.intro.internal.parts.IStandbyContentPart#setInput(java.lang.Object)
     */
    public void setInput(Object input) {
        CheatSheetElement element = findCheatSheet((String) input);
        cheatSheet.setContent(element);
    }

    private CheatSheetElement findCheatSheet(String id) {
        CheatSheetRegistryReader reader = CheatSheetRegistryReader
                .getInstance();
        return reader.findCheatSheet(id);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.intro.internal.parts.IStandbyContentPart#setFocus()
     */
    public void setFocus() {
    	cheatSheet.setFocus();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.intro.internal.parts.IStandbyContentPart#dispose()
     */
    public void dispose() {
    	cheatSheet.dispose();
    }

}
