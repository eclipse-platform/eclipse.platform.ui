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
import org.eclipse.ui.activities.AbstractActivityService;
import org.eclipse.ui.activities.ActivityServiceEvent;
import org.eclipse.ui.activities.ActivityServiceFactory;
import org.eclipse.ui.activities.IActivityService;
import org.eclipse.ui.activities.IActivityServiceListener;
import org.eclipse.ui.activities.ICompoundActivityService;

final class WorkbenchWindowActivityService extends AbstractActivityService {

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

	private ICompoundActivityService compoundActivityService = ActivityServiceFactory.getCompoundActivityService();
	private boolean started;
	private IWorkbench workbench;
	private IActivityService workbenchPageCompoundActivityService;
	private IActivityService workbenchPartSiteMutableActivityService;
	private IWorkbenchWindow workbenchWindow;

	WorkbenchWindowActivityService(IWorkbenchWindow workbenchWindow) {
		if (workbenchWindow == null)
			throw new NullPointerException();

		IWorkbench workbench = workbenchWindow.getWorkbench();

		if (workbench == null)
			throw new NullPointerException();

		this.workbenchWindow = workbenchWindow;
		this.workbench = workbench;

		compoundActivityService.addActivityServiceListener(new IActivityServiceListener() {
			public void activityServiceChanged(ActivityServiceEvent activityServiceEvent) {
				ActivityServiceEvent proxyActivityServiceEvent =
					new ActivityServiceEvent(compoundActivityService, activityServiceEvent.haveActiveActivityIdsChanged());
				fireActivityServiceChanged(activityServiceEvent);
			}
		});
	}

	public Set getActiveActivityIds() {
		return compoundActivityService.getActiveActivityIds();
	}

	boolean isStarted() {
		return started;
	}

	void start() {
		if (!started) {
			started = true;
			compoundActivityService.addActivityService(workbench.getCompoundActivityService());
			workbenchWindow.addPageListener(pageListener);
			workbenchWindow.addPerspectiveListener(perspectiveListener);
			workbenchWindow.getPartService().addPartListener(partListener);
			update();
		}
	}

	void stop() {
		if (started) {
			started = false;
			compoundActivityService.removeActivityService(workbench.getCompoundActivityService());
			workbenchWindow.removePageListener(pageListener);
			workbenchWindow.removePerspectiveListener(perspectiveListener);
			workbenchWindow.getPartService().removePartListener(partListener);
			update();
		}
	}

	private void update() {
		IWorkbenchPage workbenchPage = workbenchWindow.getActivePage();
		IActivityService workbenchPageCompoundActivityService = null;
		IWorkbenchPart workbenchPart = null;
		IWorkbenchPartSite workbenchPartSite = null;
		IActivityService workbenchPartSiteMutableActivityService = null;

		if (workbenchPage != null) {
			workbenchPageCompoundActivityService = workbenchPage.getCompoundActivityService();
			workbenchPart = workbenchPage.getActivePart();
		}

		if (workbenchPart != null)
			workbenchPartSite = workbenchPart.getSite();

		if (workbenchPartSite != null)
			// TODO remove cast
			workbenchPartSiteMutableActivityService = ((PartSite) workbenchPartSite).getMutableActivityService();

		Set removals = new HashSet();

		if (this.workbenchPageCompoundActivityService != workbenchPageCompoundActivityService) {
			if (this.workbenchPageCompoundActivityService != null)
				removals.add(this.workbenchPageCompoundActivityService);

			this.workbenchPageCompoundActivityService = workbenchPageCompoundActivityService;
		}

		if (this.workbenchPartSiteMutableActivityService != workbenchPartSiteMutableActivityService) {
			if (this.workbenchPartSiteMutableActivityService != null)
				removals.add(this.workbenchPartSiteMutableActivityService);

			this.workbenchPartSiteMutableActivityService = workbenchPartSiteMutableActivityService;
		}

		for (Iterator iterator = removals.iterator(); iterator.hasNext();) {
			IActivityService activityService = (IActivityService) iterator.next();
			compoundActivityService.removeActivityService(activityService);
		}

		if (started) {
			Set additions = new HashSet();

			if (this.workbenchPageCompoundActivityService != null)
				additions.add(this.workbenchPageCompoundActivityService);

			if (this.workbenchPartSiteMutableActivityService != null)
				additions.add(this.workbenchPartSiteMutableActivityService);

			for (Iterator iterator = additions.iterator(); iterator.hasNext();) {
				IActivityService activityService = (IActivityService) iterator.next();
				compoundActivityService.addActivityService(activityService);
			}
		}
	}
}
