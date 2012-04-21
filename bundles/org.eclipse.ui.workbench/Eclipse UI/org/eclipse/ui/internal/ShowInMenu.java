/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.ContributionManager;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISourceProvider;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.services.IWorkbenchLocationService;
import org.eclipse.ui.internal.services.WorkbenchSourceProvider;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.menus.IMenuService;
import org.eclipse.ui.menus.IWorkbenchContribution;
import org.eclipse.ui.menus.MenuUtil;
import org.eclipse.ui.part.IShowInSource;
import org.eclipse.ui.part.IShowInTargetList;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.services.IServiceLocator;
import org.eclipse.ui.services.ISourceProviderService;
import org.eclipse.ui.views.IViewDescriptor;
import org.eclipse.ui.views.IViewRegistry;

/**
 * A <code>ShowInMenu</code> is used to populate a menu manager with Show In
 * actions. The items to show are determined from the active perspective and
 * active part.
 */
public class ShowInMenu extends ContributionItem implements
		IWorkbenchContribution {

	private static final String NO_TARGETS_MSG = WorkbenchMessages.Workbench_showInNoTargets;

	private IWorkbenchWindow window;

	private Map actions = new HashMap(21);

	private boolean dirty = true;

	private IMenuListener menuListener = new IMenuListener() {
		public void menuAboutToShow(IMenuManager manager) {
			manager.markDirty();
			dirty = true;
		}
	};

	private IServiceLocator locator;

	private MenuManager currentManager;

	public ShowInMenu() {

	}

	/**
	 * Creates a Show In menu.
	 * 
	 * @param window
	 *            the window containing the menu
	 * @param id
	 *            The ID for this contribution
	 */
	public ShowInMenu(IWorkbenchWindow window, String id) {
		super(id);
		this.window = window;
	}

	public boolean isDirty() {
		return dirty;
	}

	/**
	 * Overridden to always return true and force dynamic menu building.
	 */
	public boolean isDynamic() {
		return true;
	}

	public void fill(Menu menu, int index) {
		if (getParent() instanceof MenuManager) {
			((MenuManager) getParent()).addMenuListener(menuListener);
		}

		if (!dirty) {
			return;
		}

		if (currentManager!=null && currentManager.getSize() > 0) {
			IMenuService service = (IMenuService) locator
					.getService(IMenuService.class);
			service.releaseContributions(currentManager);
			currentManager.removeAll();
		}

		currentManager = new MenuManager();
		fillMenu(currentManager);
		IContributionItem[] items = currentManager.getItems();
		if (items.length == 0) {
			MenuItem item = new MenuItem(menu, SWT.NONE, index++);
			item.setText(NO_TARGETS_MSG);
			item.setEnabled(false);
		} else {
			for (int i = 0; i < items.length; i++) {
				if (items[i].isVisible()) {
					items[i].fill(menu, index++);
				}
			}
		}
		dirty = false;
	}

	/**
	 * Fills the menu with Show In actions.
	 */
	private void fillMenu(IMenuManager innerMgr) {
		// Remove all.
		innerMgr.removeAll();

		IWorkbenchPart sourcePart = getSourcePart();
		ShowInContext context = getContext(sourcePart);
		if (context == null) {
			return;
		}
		if (context.getInput() == null
				&& (context.getSelection() == null || context.getSelection()
						.isEmpty())) {
			return;
		}

		IViewDescriptor[] viewDescs = getViewDescriptors(sourcePart);
		for (int i = 0; i < viewDescs.length; ++i) {
			IContributionItem cci = getContributionItem(viewDescs[i]);
			if (cci != null) {
				innerMgr.add(cci);
			}
		}
		if (sourcePart != null && innerMgr instanceof MenuManager) {
			ISourceProviderService sps = (ISourceProviderService) locator
					.getService(ISourceProviderService.class);
			ISourceProvider sp = sps
					.getSourceProvider(ISources.SHOW_IN_SELECTION);
			if (sp instanceof WorkbenchSourceProvider) {
				((WorkbenchSourceProvider) sp).checkActivePart(true);
			}
			IMenuService service = (IMenuService) locator
					.getService(IMenuService.class);
			service.populateContributionManager((ContributionManager) innerMgr,
					MenuUtil.SHOW_IN_MENU_ID);
		}
	}

	/**
	 * Return the appropriate command contribution item for the parameter.
	 * @param viewDescriptor
	 * @return the show in command contribution item
	 */
	protected IContributionItem getContributionItem(IViewDescriptor viewDescriptor) {
		CommandContributionItemParameter parm = new CommandContributionItemParameter(
				locator, viewDescriptor.getId(), IWorkbenchCommandConstants.NAVIGATE_SHOW_IN,
				CommandContributionItem.STYLE_PUSH);
		HashMap targetId = new HashMap();
		targetId.put(IWorkbenchCommandConstants.NAVIGATE_SHOW_IN_PARM_TARGET,
				viewDescriptor.getId());
		parm.parameters = targetId;
		parm.label = viewDescriptor.getLabel();
		if (parm.label.length() > 0) {
			parm.mnemonic = parm.label.substring(0, 1);
		}
		parm.icon = viewDescriptor.getImageDescriptor();
		return new CommandContributionItem(parm);
	}

	/**
	 * Returns the Show In... target part ids for the given source part. Merges
	 * the contributions from the current perspective and the source part.
	 */
	private ArrayList getShowInPartIds(IWorkbenchPart sourcePart) {
		ArrayList targetIds = new ArrayList();
		WorkbenchPage page = (WorkbenchPage) getWindow().getActivePage();
		if (page != null) {
			targetIds.addAll(page.getShowInPartIds());
		}
		IShowInTargetList targetList = getShowInTargetList(sourcePart);
		if (targetList != null) {
			String[] partIds = targetList.getShowInTargetIds();
			if (partIds != null) {
				for (int i = 0; i < partIds.length; ++i) {
					if (!targetIds.contains(partIds[i])) {
						targetIds.add(partIds[i]);
					}
				}
			}
		}
		page.sortShowInPartIds(targetIds);
		return targetIds;
	}

	/**
	 * Returns the source part, or <code>null</code> if there is no applicable
	 * source part
	 * <p>
	 * This implementation returns the current part in the window. Subclasses
	 * may extend or reimplement.
	 * 
	 * @return the source part or <code>null</code>
	 */
	private IWorkbenchPart getSourcePart() {
		IWorkbenchWindow window = getWindow();
		
		if (window == null)
			return null;
		Shell shell = window.getShell();
		if (shell == null || shell != shell.getDisplay().getActiveShell())
			return null;
		
		IWorkbenchPage page = window.getActivePage();
		if (page != null) {
			return page.getActivePart();
		}
		return null;
	}

	/**
	 * Returns the <code>IShowInSource</code> provided by the source part, or
	 * <code>null</code> if it does not provide one.
	 * 
	 * @param sourcePart
	 *            the source part
	 * @return an <code>IShowInSource</code> or <code>null</code>
	 */
	private IShowInSource getShowInSource(IWorkbenchPart sourcePart) {
		return (IShowInSource) Util.getAdapter(sourcePart, IShowInSource.class);
	}

	/**
	 * Returns the <code>IShowInTargetList</code> for the given source part,
	 * or <code>null</code> if it does not provide one.
	 * 
	 * @param sourcePart
	 *            the source part or <code>null</code>
	 * @return the <code>IShowInTargetList</code> or <code>null</code>
	 */
	private IShowInTargetList getShowInTargetList(IWorkbenchPart sourcePart) {
		return (IShowInTargetList) Util.getAdapter(sourcePart,
				IShowInTargetList.class);
	}

	/**
	 * Returns the <code>ShowInContext</code> to show in the selected target,
	 * or <code>null</code> if there is no valid context to show.
	 * <p>
	 * This implementation obtains the context from the
	 * <code>IShowInSource</code> of the source part (if provided), or, if the
	 * source part is an editor, it creates the context from the editor's input
	 * and selection.
	 * <p>
	 * Subclasses may extend or reimplement.
	 * 
	 * @return the <code>ShowInContext</code> to show or <code>null</code>
	 */
	protected ShowInContext getContext(IWorkbenchPart sourcePart) {
		if (sourcePart != null) {
			IShowInSource source = getShowInSource(sourcePart);
			if (source != null) {
				ShowInContext context = source.getShowInContext();
				if (context != null) {
					return context;
				}
			} else if (sourcePart instanceof IEditorPart) {
				Object input = ((IEditorPart) sourcePart).getEditorInput();
				ISelectionProvider sp = sourcePart.getSite().getSelectionProvider();
				ISelection sel = sp == null ? null : sp.getSelection();
				return new ShowInContext(input, sel);
			}
		}
		return null;
	}

	/**
	 * Returns the view descriptors to show in the dialog.
	 */
	private IViewDescriptor[] getViewDescriptors(IWorkbenchPart sourcePart) {
		String srcId = sourcePart == null ? null : sourcePart.getSite().getId();
		ArrayList ids = getShowInPartIds(sourcePart);
		ArrayList descs = new ArrayList();
		IViewRegistry reg = WorkbenchPlugin.getDefault().getViewRegistry();
		for (Iterator i = ids.iterator(); i.hasNext();) {
			String id = (String) i.next();
			if (!id.equals(srcId)) {
				IViewDescriptor desc = reg.find(id);
				if (desc != null) {
					descs.add(desc);
				}
			}
		}
		return (IViewDescriptor[]) descs.toArray(new IViewDescriptor[descs
				.size()]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.menus.IWorkbenchContribution#initialize(org.eclipse.ui.services.IServiceLocator)
	 */
	public void initialize(IServiceLocator serviceLocator) {
		locator = serviceLocator;
	}

	protected IWorkbenchWindow getWindow() {
		if(locator == null) return null;
		
		IWorkbenchLocationService wls = (IWorkbenchLocationService) locator
				.getService(IWorkbenchLocationService.class);

		if (window == null) {
			window = wls.getWorkbenchWindow();
		}
		if (window == null) {
			IWorkbench wb = wls.getWorkbench();
			if (wb != null) {
				window = wb.getActiveWorkbenchWindow();
			}
		}
		return window;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.ContributionItem#dispose()
	 */
	public void dispose() {
		if (currentManager != null && currentManager.getSize() > 0) {
			IMenuService service = (IMenuService) locator
					.getService(IMenuService.class);
			if (service != null) {
				service.releaseContributions(currentManager);
			}
			currentManager.removeAll();
			currentManager = null;
		}
		if (getParent() instanceof MenuManager) {
			((MenuManager) getParent()).removeMenuListener(menuListener);
		}
		actions.clear();
		window=null;
		locator=null;
	}
}
