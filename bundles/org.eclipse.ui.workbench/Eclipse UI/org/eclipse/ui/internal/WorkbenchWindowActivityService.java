package org.eclipse.ui.internal;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.activities.AbstractActivityService;
import org.eclipse.ui.activities.ActivityServiceEvent;
import org.eclipse.ui.activities.IActivityService;
import org.eclipse.ui.activities.IActivityServiceListener;
import org.eclipse.ui.internal.util.Util;

final class WorkbenchWindowActivityService extends AbstractActivityService {

	private final IActivityServiceListener activityServiceListener = new IActivityServiceListener() {
		public void activityServiceChanged(ActivityServiceEvent activityServiceEvent) {
			update();
		}
	};
	
	private IInternalPerspectiveListener internalPerspectiveListener = new IInternalPerspectiveListener() {
		public void perspectiveActivated(IWorkbenchPage workbenchPage, IPerspectiveDescriptor perspectiveDescriptor) {
			update();
		}

		public void perspectiveChanged(IWorkbenchPage workbenchPage, IPerspectiveDescriptor perspectiveDescriptor, String changeId) {
			update();
		}

		public void perspectiveClosed(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
			update();
		}

		public void perspectiveOpened(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
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
		public void partActivated(IWorkbenchPart part) {
			update();
		}

		public void partBroughtToTop(IWorkbenchPart part) {
			update();
		}

		public void partClosed(IWorkbenchPart part) {
			update();
		}

		public void partDeactivated(IWorkbenchPart part) {
			update();
		}

		public void partOpened(IWorkbenchPart part) {		
			update();
		}
	};	
	
	private Set activeActivityIds = new HashSet();
	private boolean started;
	private IWorkbench workbench;
	private IWorkbenchPage workbenchPage;
	private IWorkbenchPartSite workbenchPartSite;
	private IWorkbenchWindow workbenchWindow;
	
	WorkbenchWindowActivityService(IWorkbenchWindow workbenchWindow) {		
		if (workbenchWindow == null)
			throw new NullPointerException();
		
		IWorkbench workbench = workbenchWindow.getWorkbench();
		
		if (workbench == null)
			throw new NullPointerException();

		this.workbenchWindow = workbenchWindow;		
		this.workbench = workbench;
		update();
	}

	public Set getActiveActivityIds() {
		return Collections.unmodifiableSet(activeActivityIds);
	}
	
	boolean isStarted() {
		return started;
	}
	
	void start() {
		if (!started) {
			started = true;			
			IActivityService workbenchActivityService = workbench.getCompoundActivityService();					
			workbenchActivityService.addActivityServiceListener(activityServiceListener);					
			workbenchWindow.addPageListener(pageListener);
			update();
		}
	}
	
	void stop() {
		if (started) {
			started = false;
			IActivityService workbenchActivityService = workbench.getCompoundActivityService();					
			workbenchActivityService.removeActivityServiceListener(activityServiceListener);						
			workbenchWindow.removePageListener(pageListener);
			update();
		}
	}
	
	private void setActiveActivityIds(Set activeActivityIds) {
		activeActivityIds = Util.safeCopy(activeActivityIds, String.class);
		boolean activityServiceChanged = false;
		Map activityEventsByActivityId = null;

		if (!this.activeActivityIds.equals(activeActivityIds)) {
			this.activeActivityIds = activeActivityIds;
			fireActivityServiceChanged(new ActivityServiceEvent(this, true));
		}
	}
	
	private void update() {	
		/*
		IWorkbenchPage workbenchPage = workbenchWindow.getActivePage();
		IWorkbenchPartSite workbenchPartSite = null;
		
		if (workbenchPage != null)
			workbenchPartSite = workbenchPage.getActivePart();
		
		
		if (workbenchPage instanceof WorkbenchPage) {		
			IActivityService activityService = ((WorkbenchPage) workbenchPage).getCompoundActivityService();
			activityService.removeActivityServiceListener(activityServiceListener);			
		}

		workbench = null;
		workbenchWindow = null;
		workbenchPage = null;
		Set activeActivityIds = new HashSet();
		
		if (started) {
			workbench = PlatformUI.getWorkbench();
					
			if (workbenchWindow != null) 
				workbenchPage = workbenchWindow.getActivePage();
			
			if (workbench instanceof Workbench) {		
				IActivityService activityService = ((Workbench) workbench).getCompoundActivityService();
				activityService.addActivityServiceListener(activityServiceListener);			
				activeActivityIds.addAll(activityService.getActiveActivityIds());
			}		
			
			if (workbenchWindow != null) 
				workbenchWindow.addPageListener(pageListener);
			
			if (workbenchWindow instanceof WorkbenchWindow) {
				IActivityService activityService = ((WorkbenchWindow) workbenchWindow).getCompositeActivityService();
				activityService.addActivityServiceListener(activityServiceListener);			
			 
				try {
					activeActivityIds.addAll(activityService.getActiveActivityIds());
				} catch (DisposedException eDisposed) {
				}			
			}
			
			if (workbenchPage instanceof WorkbenchPage) {		
				IActivityService activityService = ((WorkbenchPage) workbenchPage).getCompoundActivityService();
				activityService.addActivityServiceListener(activityServiceListener);						
				activeActivityIds.addAll(activityService.getActiveActivityIds());
			}
		}

		workbench.getCommandManager().setActiveActivityIds(activeActivityIds);
		*/
	}
}
