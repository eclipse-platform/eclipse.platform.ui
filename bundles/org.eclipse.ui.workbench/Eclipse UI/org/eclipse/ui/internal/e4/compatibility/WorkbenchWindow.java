/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.e4.compatibility;

import java.lang.reflect.InvocationTargetException;
import javax.inject.Inject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.ContextInjectionFactory;
import org.eclipse.e4.ui.model.application.MElementContainer;
import org.eclipse.e4.ui.model.application.MUIElement;
import org.eclipse.e4.ui.model.application.MWindow;
import org.eclipse.e4.workbench.ui.IPresentationEngine;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.internal.registry.UIExtensionTracker;
import org.eclipse.ui.internal.services.IWorkbenchLocationService;
import org.eclipse.ui.internal.services.WorkbenchLocationService;
import org.eclipse.ui.services.IServiceScopes;

/**
 * @since 3.5
 *
 */
public class WorkbenchWindow implements IWorkbenchWindow {

	@Inject
	private IWorkbench workbench;
	@Inject
	private MWindow model;
	@Inject
	private IPresentationEngine engine;
	private WorkbenchPage page;
	private UIExtensionTracker tracker;

	private IAdaptable input;
	private IPerspectiveDescriptor perspective;

	private ListenerList pageListeners = new ListenerList();
	private ListenerList perspectiveListeners = new ListenerList();

	WorkbenchWindow(IAdaptable input, IPerspectiveDescriptor perspective) {
		this.input = input;
		this.perspective = perspective;
	}

	void contextSet() {
		IEclipseContext windowContext = model.getContext();
		page = new WorkbenchPage(this, input);
		ContextInjectionFactory.inject(page, windowContext);
		page.setPerspective(perspective);

		windowContext.set(IWorkbenchWindow.class.getName(), this);
		windowContext.set(IWorkbenchPage.class.getName(), page);

		windowContext.set(IWorkbenchLocationService.class.getName(), new WorkbenchLocationService(
				IServiceScopes.PARTSITE_SCOPE, getWorkbench(), this, null, null, null, 1));
	}

	public MWindow getModel() {
		return model;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindow#close()
	 */
	public boolean close() {
		// FIXME: the rendering engine should destroy in response to a remove,
		// right?
		MElementContainer<MUIElement> parent = model.getParent();
		model.getParent().getChildren().remove(model);
		if (parent.getSelectedElement() == model) {
			if (!parent.getChildren().isEmpty()) {
				parent.setSelectedElement(parent.getChildren().get(0));
			}
		}
		engine.removeGui(model);
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindow#getActivePage()
	 */
	public IWorkbenchPage getActivePage() {
		return page;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindow#getPages()
	 */
	public IWorkbenchPage[] getPages() {
		return new IWorkbenchPage[] { page };
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindow#getPartService()
	 */
	public IPartService getPartService() {
		return page;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindow#getSelectionService()
	 */
	public ISelectionService getSelectionService() {
		// FIXME compat window selection service
		E4Util.unsupported("getSelectionService"); //$NON-NLS-1$
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindow#getShell()
	 */
	public Shell getShell() {
		return (Shell) model.getWidget();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindow#getWorkbench()
	 */
	public IWorkbench getWorkbench() {
		return workbench;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindow#isApplicationMenu(java.lang.String)
	 */
	public boolean isApplicationMenu(String menuId) {
		// FIXME compat isApplicationMenu
		E4Util.unsupported("isApplicationMenu"); //$NON-NLS-1$
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindow#openPage(java.lang.String, org.eclipse.core.runtime.IAdaptable)
	 */
	public IWorkbenchPage openPage(String perspectiveId, IAdaptable input)
			throws WorkbenchException {
		return workbench.openWorkbenchWindow(perspectiveId, input).getActivePage();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindow#openPage(org.eclipse.core.runtime.IAdaptable)
	 */
	public IWorkbenchPage openPage(IAdaptable input) throws WorkbenchException {
		return openPage(null, input);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindow#run(boolean, boolean, org.eclipse.jface.operation.IRunnableWithProgress)
	 */
	public void run(boolean fork, boolean cancelable,
			IRunnableWithProgress runnable) throws InvocationTargetException,
			InterruptedException {
		// TODO Auto-generated method stub
		runnable.run(new NullProgressMonitor());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindow#setActivePage(org.eclipse.ui.IWorkbenchPage)
	 */
	public void setActivePage(IWorkbenchPage page) {
		// TODO Auto-generated method stub
		this.page = (WorkbenchPage) page;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindow#getExtensionTracker()
	 */
	public IExtensionTracker getExtensionTracker() {
		if (tracker == null) {
			tracker = new UIExtensionTracker(getWorkbench().getDisplay());
		}
		return tracker;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPageService#addPageListener(org.eclipse.ui.IPageListener)
	 */
	public void addPageListener(IPageListener listener) {
		pageListeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPageService#addPerspectiveListener(org.eclipse.ui.IPerspectiveListener)
	 */
	public void addPerspectiveListener(IPerspectiveListener listener) {
		perspectiveListeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPageService#removePageListener(org.eclipse.ui.IPageListener)
	 */
	public void removePageListener(IPageListener listener) {
		pageListeners.remove(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPageService#removePerspectiveListener(org.eclipse.ui.IPerspectiveListener)
	 */
	public void removePerspectiveListener(IPerspectiveListener listener) {
		perspectiveListeners.remove(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.services.IServiceLocator#getService(java.lang.Class)
	 */
	public Object getService(Class api) {
		return model.getContext().get(api.getName());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.services.IServiceLocator#hasService(java.lang.Class)
	 */
	public boolean hasService(Class api) {
		return model.getContext().containsKey(api.getName());
	}

}
