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
package org.eclipse.help.ui.internal.views;

import java.util.Hashtable;

import org.eclipse.help.IContextProvider;
import org.eclipse.help.ui.internal.*;
import org.eclipse.help.ui.internal.IHelpUIConstants;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.ViewPart;

public class HelpView extends ViewPart implements IPartListener2, ISelectionChangedListener {
	protected FormToolkit toolkit;
	protected IMemento memento;
	protected ReusableHelpPart reusableHelpPart;
	private Hashtable pageRecs;
	private IWorkbenchPart monitoredPart;
	private boolean visible;

	/**
	 * 
	 */
	public HelpView() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		toolkit = new FormToolkit(parent.getDisplay());
    	//toolkit.setBackground(toolkit.getColors().createColor("bg", 245, 250, 255));
		reusableHelpPart.createControl(parent, toolkit);
		//reusableHelpPart.setShowDocumentsInPlace(false);
		reusableHelpPart
				.setDefaultContextHelpText(HelpUIResources.getString("HelpView.defaultText")); //$NON-NLS-1$
		reusableHelpPart.showPage(getFirstPage());
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window==null) return;
		IWorkbenchPage page = window.getActivePage();
		if (page==null) return;
		IWorkbenchPartReference aref = page.getActivePartReference();
		if (aref!=null)
			handlePartActivation(aref);
	}

	public void dispose() {
		IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		IPartService service = window.getPartService();
		if (monitoredPart!=null)
			uninstallSelectionListener(monitoredPart);
		service.removePartListener(this);
		if (reusableHelpPart != null) {
			reusableHelpPart.dispose();
			reusableHelpPart = null;
		}
		if (toolkit != null) {
			toolkit.dispose();
			toolkit = null;
		}
		super.dispose();
	}

	public void init(IViewSite site, IMemento memento) throws PartInitException {
		this.memento = memento;
		init(site);
		reusableHelpPart = new ReusableHelpPart(site.getWorkbenchWindow(), getHelpPartStyle());
		IActionBars actionBars = site.getActionBars();
		reusableHelpPart.init(actionBars.getToolBarManager(), actionBars.getStatusLineManager());
		IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		IPartService service = window.getPartService();
		service.addPartListener(this);
	}

	private void handlePartActivation(IWorkbenchPartReference ref) {
		if (reusableHelpPart == null)
			return;
		if (!reusableHelpPart.isMonitoringContextHelp())
			return;
		if (isThisPart(ref))
			return;
		IWorkbenchPart part = ref.getPart(false);
		Display display = part.getSite().getShell().getDisplay();
		Control c = display.getFocusControl();
		if (c != null && c.isVisible() && !c.isDisposed()) {
			IContextProvider provider = (IContextProvider)part.getAdapter(IContextProvider.class);
			if (provider!=null) {
				if (visible)
					reusableHelpPart.update(provider, part, c);
				if ((provider.getContextChangeMask() & IContextProvider.SELECTION)!=0) {
					// context help changes with selections
					installSelectionListener(part);
				}
			}
			else
				if (visible)
					reusableHelpPart.update(part, c);
		}
	}
	
	private void installSelectionListener(IWorkbenchPart part) {
		ISelectionProvider provider = part.getSite().getSelectionProvider();
		if (provider instanceof IPostSelectionProvider)
			((IPostSelectionProvider)provider).addPostSelectionChangedListener(this);
		else
			provider.addSelectionChangedListener(this);
		monitoredPart = part;
		//System.out.println("Installing "+part.getSite().getRegisteredName());
	}
	private void uninstallSelectionListener(IWorkbenchPart part) {
		ISelectionProvider provider = part.getSite().getSelectionProvider();
		if (provider instanceof IPostSelectionProvider)
			((IPostSelectionProvider)provider).removePostSelectionChangedListener(this);
		else
			provider.removeSelectionChangedListener(this);
		monitoredPart = null;
		//System.out.println("Uninstalling "+part.getSite().getRegisteredName());
	}
	
	private boolean isThisPart(IWorkbenchPartReference ref) {
		IWorkbenchPart part = ref.getPart(false);
		return part!=null && part.equals(this);
	}
	
	private void updateActivePart() {
		if (reusableHelpPart == null)
			return;
		if (!reusableHelpPart.isMonitoringContextHelp())
			return;
		if (monitoredPart==null)
			return;
		IContextProvider provider = (IContextProvider)monitoredPart.getAdapter(IContextProvider.class);
		Control c = monitoredPart.getSite().getShell().getDisplay().getFocusControl();
		if (c!=null && c.isDisposed()==false && provider!=null && visible) {
			reusableHelpPart.update(provider, monitoredPart, c);
		}
	}

	private void handlePartDeactivation(IWorkbenchPartReference ref) {
		IWorkbenchPart part = ref.getPart(false);
		if (monitoredPart!=null && part!=null && part.equals(monitoredPart)) {
			uninstallSelectionListener(part);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPartListener2#partActivated(org.eclipse.ui.IWorkbenchPartReference)
	 */
	public void partActivated(final IWorkbenchPartReference partRef) {
		if (isThisPart(partRef)) {
			visible = true;
			hook(true);
			selectionChanged(null);
		}
		else {
		//getSite().getShell().getDisplay().asyncExec(new Runnable() {
			//public void run() {
				handlePartActivation(partRef);
			//}
		//});
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPartListener2#partBroughtToTop(org.eclipse.ui.IWorkbenchPartReference)
	 */
	public void partBroughtToTop(IWorkbenchPartReference partRef) {
		if (isThisPart(partRef)) {
			visible= true;
			hook(true);
			selectionChanged(null);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPartListener2#partClosed(org.eclipse.ui.IWorkbenchPartReference)
	 */
	public void partClosed(IWorkbenchPartReference partRef) {
		handlePartDeactivation(partRef);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPartListener2#partDeactivated(org.eclipse.ui.IWorkbenchPartReference)
	 */
	public void partDeactivated(IWorkbenchPartReference partRef) {
		handlePartDeactivation(partRef);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPartListener2#partHidden(org.eclipse.ui.IWorkbenchPartReference)
	 */
	public void partHidden(IWorkbenchPartReference partRef) {
		if (isThisPart(partRef)) {
			visible = false;
			hook(false);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPartListener2#partInputChanged(org.eclipse.ui.IWorkbenchPartReference)
	 */
	public void partInputChanged(IWorkbenchPartReference partRef) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPartListener2#partOpened(org.eclipse.ui.IWorkbenchPartReference)
	 */
	public void partOpened(IWorkbenchPartReference partRef) {
		if (isThisPart(partRef)) {
			visible = true;
			hook(true);
			selectionChanged(null);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPartListener2#partVisible(org.eclipse.ui.IWorkbenchPartReference)
	 */
	public void partVisible(IWorkbenchPartReference partRef) {
		if (isThisPart(partRef)) {
			visible=true;
			hook(true);
			selectionChanged(null);
		}
	}
	
	private void hook(boolean doHook) {
		if (doHook) {
			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			IPartService service = window.getPartService();
			handlePartActivation(service.getActivePartReference());
		}
		else {
			if (monitoredPart!=null) 
				uninstallSelectionListener(monitoredPart);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	public void selectionChanged(SelectionChangedEvent event) {
		if (!visible) return;
		getSite().getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				updateActivePart();
			}
		});
	}
	/* (non-Javadoc)
	 * @see org.eclipse.help.ui.internal.views.BaseHelpView#getFirstPage()
	 */
	protected String getFirstPage() {
		return IHelpUIConstants.HV_CONTEXT_HELP_PAGE;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.help.ui.internal.views.BaseHelpView#getHelpPartStyle()
	 */
	protected int getHelpPartStyle() {
		return ReusableHelpPart.ALL_TOPICS|ReusableHelpPart.CONTEXT_HELP|ReusableHelpPart.SEARCH;
	}
	public void setFocus() {
		if (reusableHelpPart != null)
			reusableHelpPart.setFocus();
	}
}