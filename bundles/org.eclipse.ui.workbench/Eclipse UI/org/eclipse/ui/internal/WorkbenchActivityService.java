package org.eclipse.ui.internal;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.activities.AbstractActivityService;
import org.eclipse.ui.activities.ActivityServiceEvent;
import org.eclipse.ui.activities.IActivityService;
import org.eclipse.ui.activities.IActivityServiceListener;
import org.eclipse.ui.internal.util.Util;

final class WorkbenchActivityService extends AbstractActivityService {

	private final IActivityServiceListener activityServiceListener = new IActivityServiceListener() {
		public void activityServiceChanged(ActivityServiceEvent activityServiceEvent) {
			update();
		}
	};
	
	private IWindowListener windowListener = new IWindowListener() {
		public void windowActivated(IWorkbenchWindow window) {
			update();
		}

		public void windowClosed(IWorkbenchWindow window) {
			update();
		}

		public void windowDeactivated(IWorkbenchWindow window) {
			update();
		}

		public void windowOpened(IWorkbenchWindow window) {
			update();
		}
	};
	
	private Set activeActivityIds = new HashSet();
	private boolean started;
	private IWorkbench workbench;	
	private IActivityService workbenchPageCompoundActivityService;
	private IActivityService workbenchPartSiteMutableActivityService;			
	private WorkbenchWindow workbenchWindow;
	
	WorkbenchActivityService(IWorkbench workbench) {		
		if (workbench == null)
			throw new NullPointerException();

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
//			IActivityService workbenchActivityService = workbench.getCompoundActivityService();					
//			workbenchActivityService.addActivityServiceListener(activityServiceListener);					
//			workbenchWindow.addPageListener(pageListener);
//			workbenchWindow.getPartService().addPartListener(partListener);
//			workbenchWindow.getPerspectiveService().addPerspectiveListener(internalPerspectiveListener);					
			update();
		}
	}
	
	void stop() {
		if (started) {
			started = false;
//			IActivityService workbenchActivityService = workbench.getCompoundActivityService();					
//			workbenchActivityService.removeActivityServiceListener(activityServiceListener);						
//			workbenchWindow.removePageListener(pageListener);
//			workbenchWindow.getPartService().removePartListener(partListener);
//			workbenchWindow.getPerspectiveService().removePerspectiveListener(internalPerspectiveListener);					
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
//		IWorkbenchPage workbenchPage = workbenchWindow.getActivePage();
//		IActivityService workbenchPageCompoundActivityService = null;
//		IWorkbenchPart workbenchPart = null;
//		IWorkbenchPartSite workbenchPartSite = null;
//		IActivityService workbenchPartSiteMutableActivityService = null;		
//		
//		if (workbenchPage != null) {
//			workbenchPageCompoundActivityService = workbenchPage.getCompoundActivityService();
//			workbenchPart = workbenchPage.getActivePart();
//		}
//		
//		if (workbenchPart != null)
//			workbenchPartSite = workbenchPart.getSite();
//		
//		if (workbenchPartSite != null)
//			// TODO remove cast
//			workbenchPartSiteMutableActivityService = ((PartSite) workbenchPartSite).getMutableActivityService();
//		
//		if (this.workbenchPageCompoundActivityService != workbenchPageCompoundActivityService) {
//			if (this.workbenchPageCompoundActivityService != null)
//				this.workbenchPageCompoundActivityService.removeActivityServiceListener(activityServiceListener);
//			
//			this.workbenchPageCompoundActivityService = workbenchPageCompoundActivityService;
//		}
//
//		if (this.workbenchPartSiteMutableActivityService != workbenchPartSiteMutableActivityService) {
//			if (this.workbenchPartSiteMutableActivityService != null)
//				this.workbenchPartSiteMutableActivityService.removeActivityServiceListener(activityServiceListener);
//			
//			this.workbenchPartSiteMutableActivityService = workbenchPartSiteMutableActivityService;
//		}
//		
//		Set activeActivityIds = new HashSet();
//		
//		if (started) {
//			activeActivityIds.addAll(workbench.getCompoundActivityService().getActiveActivityIds());
//			
//			if (this.workbenchPageCompoundActivityService != null) {
//				this.workbenchPageCompoundActivityService.addActivityServiceListener(activityServiceListener);
//				activeActivityIds.addAll(workbenchPageCompoundActivityService.getActiveActivityIds());
//			}
//
//			if (this.workbenchPartSiteMutableActivityService != null) {
//				this.workbenchPartSiteMutableActivityService.addActivityServiceListener(activityServiceListener);			
//				activeActivityIds.addAll(workbenchPartSiteMutableActivityService.getActiveActivityIds());
//			}
//		}
//		
//		setActiveActivityIds(activeActivityIds);
	}
}
