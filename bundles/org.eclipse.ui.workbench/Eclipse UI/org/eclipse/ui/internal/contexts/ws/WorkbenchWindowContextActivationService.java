package org.eclipse.ui.internal.contexts.ws;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.contexts.ContextActivationServiceEvent;
import org.eclipse.ui.contexts.IContextActivationService;
import org.eclipse.ui.contexts.IContextActivationServiceListener;
import org.eclipse.ui.contexts.IWorkbenchContextSupport;
import org.eclipse.ui.contexts.IWorkbenchPageContextSupport;
import org.eclipse.ui.contexts.IWorkbenchPartSiteContextSupport;
import org.eclipse.ui.internal.contexts.AbstractContextActivationService;
import org.eclipse.ui.internal.util.Util;

final class WorkbenchWindowContextActivationService
	extends AbstractContextActivationService {
	private Set activeContextIds = new HashSet();
	private final IContextActivationServiceListener contextActivationServiceListener =
		new IContextActivationServiceListener() {
		public void contextActivationServiceChanged(ContextActivationServiceEvent contextActivationServiceEvent) {
			update();
		}
	};
	private IPageListener pageListener = new IPageListener() {
		public void pageActivated(IWorkbenchPage workbenchPage) {
			update();
		}

		public void pageClosed(IWorkbenchPage workbenchPage) {
			update();
		}

		public void pageOpened(IWorkbenchPage workbenchPage) {
			update();
		}
	};
	private IPartListener partListener = new IPartListener() {
		public void partActivated(IWorkbenchPart workbenchPart) {
			update();
		}

		public void partBroughtToTop(IWorkbenchPart workbenchPart) {
			update();
		}

		public void partClosed(IWorkbenchPart workbenchPart) {
			update();
		}

		public void partDeactivated(IWorkbenchPart workbenchPart) {
			update();
		}

		public void partOpened(IWorkbenchPart workbenchPart) {
			update();
		}
	};
	private IPerspectiveListener perspectiveListener =
		new IPerspectiveListener() {
		public void perspectiveActivated(
			IWorkbenchPage workbenchPage,
			IPerspectiveDescriptor perspectiveDescriptor) {
			update();
		}

		public void perspectiveChanged(
			IWorkbenchPage workbenchPage,
			IPerspectiveDescriptor perspectiveDescriptor,
			String changeId) {
			update();
		}
	};
	private IWorkbench workbench;
	private IContextActivationService workbenchCompoundContextActivationService;
	private IContextActivationService workbenchPageCompoundContextActivationService;
	private IContextActivationService workbenchPartSiteMutableContextActivationService;
	private IWorkbenchWindow workbenchWindow;

	WorkbenchWindowContextActivationService(IWorkbenchWindow workbenchWindow) {
		if (workbenchWindow == null)
			throw new NullPointerException();

		IWorkbench workbench = workbenchWindow.getWorkbench();

		if (workbench == null)
			throw new NullPointerException();

		this.workbenchWindow = workbenchWindow;
		this.workbench = workbench;
		workbenchWindow.addPageListener(pageListener);
		workbenchWindow.addPerspectiveListener(perspectiveListener);
		workbenchWindow.getPartService().addPartListener(partListener);
		update();
	}

	public Set getActiveContextIds() {
		return Collections.unmodifiableSet(activeContextIds);
	}

	private void setActiveContextIds(Set activeContextIds) {
		activeContextIds = Util.safeCopy(activeContextIds, String.class);
		boolean contextActivationServiceChanged = false;
		Map contextEventsByContextId = null;

		if (!this.activeContextIds.equals(activeContextIds)) {
			this.activeContextIds = activeContextIds;
			fireContextActivationServiceChanged(
				new ContextActivationServiceEvent(this, true));
		}
	}

	private void update() {
		IContextActivationService workbenchCompoundContextActivationService =
			null;
		IWorkbenchPage workbenchPage = workbenchWindow.getActivePage();
		IContextActivationService workbenchPageCompoundContextActivationService =
			null;
		IWorkbenchPart workbenchPart = null;
		IWorkbenchPartSite workbenchPartSite = null;
		IContextActivationService workbenchPartSiteMutableContextActivationService =
			null;
		IWorkbenchContextSupport workbenchContextSupport =
			workbench.getContextSupport();
		workbenchCompoundContextActivationService =
			workbenchContextSupport.getCompoundContextActivationService();

		if (workbenchPage != null) {
			IWorkbenchPageContextSupport workbenchPageContextSupport =
				workbenchPage.getContextSupport();
			workbenchPageCompoundContextActivationService =
				workbenchPageContextSupport
					.getCompoundContextActivationService();
			workbenchPart = workbenchPage.getActivePart();
		}

		if (workbenchPart != null)
			workbenchPartSite = workbenchPart.getSite();

		if (workbenchPartSite != null) {
			IWorkbenchPartSiteContextSupport workbenchPartSiteContextSupport =
				(
					IWorkbenchPartSiteContextSupport) workbenchPartSite
						.getAdapter(
					IWorkbenchPartSiteContextSupport.class);

			if (workbenchPartSiteContextSupport != null)
				workbenchPartSiteMutableContextActivationService =
					workbenchPartSiteContextSupport
						.getMutableContextActivationService();
		}

		if (this.workbenchCompoundContextActivationService
			!= workbenchCompoundContextActivationService) {
			if (this.workbenchCompoundContextActivationService != null)
				this
					.workbenchCompoundContextActivationService
					.removeContextActivationServiceListener(contextActivationServiceListener);

			this.workbenchCompoundContextActivationService =
				workbenchCompoundContextActivationService;

			if (this.workbenchCompoundContextActivationService != null)
				this
					.workbenchCompoundContextActivationService
					.addContextActivationServiceListener(contextActivationServiceListener);
		}

		if (this.workbenchPageCompoundContextActivationService
			!= workbenchPageCompoundContextActivationService) {
			if (this.workbenchPageCompoundContextActivationService != null)
				this
					.workbenchPageCompoundContextActivationService
					.removeContextActivationServiceListener(contextActivationServiceListener);

			this.workbenchPageCompoundContextActivationService =
				workbenchPageCompoundContextActivationService;

			if (this.workbenchPageCompoundContextActivationService != null)
				this
					.workbenchPageCompoundContextActivationService
					.addContextActivationServiceListener(contextActivationServiceListener);
		}

		if (this.workbenchPartSiteMutableContextActivationService
			!= workbenchPartSiteMutableContextActivationService) {
			if (this.workbenchPartSiteMutableContextActivationService != null)
				this
					.workbenchPartSiteMutableContextActivationService
					.removeContextActivationServiceListener(contextActivationServiceListener);

			this.workbenchPartSiteMutableContextActivationService =
				workbenchPartSiteMutableContextActivationService;

			if (this.workbenchPartSiteMutableContextActivationService != null)
				this
					.workbenchPartSiteMutableContextActivationService
					.addContextActivationServiceListener(contextActivationServiceListener);
		}

		Set activeContextIds = new HashSet();

		if (this.workbenchCompoundContextActivationService != null)
			activeContextIds.addAll(
				workbenchCompoundContextActivationService
					.getActiveContextIds());

		if (this.workbenchPageCompoundContextActivationService != null)
			activeContextIds.addAll(
				workbenchPageCompoundContextActivationService
					.getActiveContextIds());

		if (this.workbenchPartSiteMutableContextActivationService != null)
			activeContextIds.addAll(
				workbenchPartSiteMutableContextActivationService
					.getActiveContextIds());

		setActiveContextIds(activeContextIds);
	}
}
