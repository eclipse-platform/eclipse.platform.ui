package org.eclipse.ui.internal;

import java.util.HashSet;
import java.util.Iterator;
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
import org.eclipse.ui.contexts.activationservice.ContextActivationServiceEvent;
import org.eclipse.ui.contexts.activationservice.ContextActivationServiceFactory;
import org.eclipse.ui.contexts.activationservice.IContextActivationService;
import org.eclipse.ui.contexts.activationservice.IContextActivationServiceListener;
import org.eclipse.ui.contexts.activationservice.ICompoundContextActiviationService;
import org.eclipse.ui.internal.contexts.activationservice.AbstractContextActivationService;

final class WorkbenchWindowContextActivationService extends AbstractContextActivationService {

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

	private IPerspectiveListener perspectiveListener = new IPerspectiveListener() {
		public void perspectiveActivated(IWorkbenchPage workbenchPage, IPerspectiveDescriptor perspectiveDescriptor) {
			update();
		}

		public void perspectiveChanged(IWorkbenchPage workbenchPage, IPerspectiveDescriptor perspectiveDescriptor, String changeId) {
			update();
		}
	};

	private ICompoundContextActiviationService compoundContextActivationService = ContextActivationServiceFactory.getCompoundContextActivationService();
	private boolean started;
	private IWorkbench workbench;
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

		compoundContextActivationService.addContextActivationServiceListener(new IContextActivationServiceListener() {
			public void contextActivationServiceChanged(ContextActivationServiceEvent contextActivationServiceEvent) {
				ContextActivationServiceEvent proxyContextActivationServiceEvent =
					new ContextActivationServiceEvent(compoundContextActivationService, contextActivationServiceEvent.haveActiveContextIdsChanged());
				fireContextActivationServiceChanged(contextActivationServiceEvent);
			}
		});
	}

	public Set getActiveContextIds() {
		return compoundContextActivationService.getActiveContextIds();
	}

	boolean isStarted() {
		return started;
	}

	void start() {
		if (!started) {
			started = true;
			compoundContextActivationService.addContextActivationService(workbench.getCompoundContextActivationService());
			workbenchWindow.addPageListener(pageListener);
			workbenchWindow.addPerspectiveListener(perspectiveListener);
			workbenchWindow.getPartService().addPartListener(partListener);
			update();
		}
	}

	void stop() {
		if (started) {
			started = false;
			compoundContextActivationService.removeContextActivationService(workbench.getCompoundContextActivationService());
			workbenchWindow.removePageListener(pageListener);
			workbenchWindow.removePerspectiveListener(perspectiveListener);
			workbenchWindow.getPartService().removePartListener(partListener);
			update();
		}
	}

	private void update() {
		IWorkbenchPage workbenchPage = workbenchWindow.getActivePage();
		IContextActivationService workbenchPageCompoundContextActivationService = null;
		IWorkbenchPart workbenchPart = null;
		IWorkbenchPartSite workbenchPartSite = null;
		IContextActivationService workbenchPartSiteMutableContextActivationService = null;

		if (workbenchPage != null) {
			workbenchPageCompoundContextActivationService = workbenchPage.getCompoundContextActivationService();
			workbenchPart = workbenchPage.getActivePart();
		}

		if (workbenchPart != null)
			workbenchPartSite = workbenchPart.getSite();

		if (workbenchPartSite != null)
			// TODO remove cast
			workbenchPartSiteMutableContextActivationService = ((PartSite) workbenchPartSite).getMutableContextActivationService();

		Set removals = new HashSet();

		if (this.workbenchPageCompoundContextActivationService != workbenchPageCompoundContextActivationService) {
			if (this.workbenchPageCompoundContextActivationService != null)
				removals.add(this.workbenchPageCompoundContextActivationService);

			this.workbenchPageCompoundContextActivationService = workbenchPageCompoundContextActivationService;
		}

		if (this.workbenchPartSiteMutableContextActivationService != workbenchPartSiteMutableContextActivationService) {
			if (this.workbenchPartSiteMutableContextActivationService != null)
				removals.add(this.workbenchPartSiteMutableContextActivationService);

			this.workbenchPartSiteMutableContextActivationService = workbenchPartSiteMutableContextActivationService;
		}

		for (Iterator iterator = removals.iterator(); iterator.hasNext();) {
			IContextActivationService contextActivationService = (IContextActivationService) iterator.next();
			compoundContextActivationService.removeContextActivationService(contextActivationService);
		}

		if (started) {
			Set additions = new HashSet();

			if (this.workbenchPageCompoundContextActivationService != null)
				additions.add(this.workbenchPageCompoundContextActivationService);

			if (this.workbenchPartSiteMutableContextActivationService != null)
				additions.add(this.workbenchPartSiteMutableContextActivationService);

			for (Iterator iterator = additions.iterator(); iterator.hasNext();) {
				IContextActivationService contextActivationService = (IContextActivationService) iterator.next();
				compoundContextActivationService.addContextActivationService(contextActivationService);
			}
		}
	}
}
