package org.eclipse.ui.internal;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.DisposedException;
import org.eclipse.ui.activities.IActivationService;
import org.eclipse.ui.activities.IActivationServiceEvent;
import org.eclipse.ui.activities.IActivationServiceListener;

final class WorkbenchActivityMonitor {

	private static WorkbenchActivityMonitor workbenchActivityMonitor;
	
	final static WorkbenchActivityMonitor getInstance() {
		if (workbenchActivityMonitor == null)
			workbenchActivityMonitor = new WorkbenchActivityMonitor();
		
		return workbenchActivityMonitor;		
	}
	
	private final IActivationServiceListener activationServiceListener = new IActivationServiceListener() {
		public void activationServiceChanged(IActivationServiceEvent activationServiceEvent) {
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
	
	private IWindowListener windowListener = new IWindowListener() {
		public void windowActivated(IWorkbenchWindow workbenchWindow) {
			update();
		}

		public void windowClosed(IWorkbenchWindow workbenchWindow) {
			update();
		}

		public void windowDeactivated(IWorkbenchWindow workbenchWindow) {
			update();
		}

		public void windowOpened(IWorkbenchWindow workbenchWindow) {
			update();
		}
	};	
	
	private boolean started;
	private IWorkbench workbench;
	private IWorkbenchPage workbenchPage;
	private IWorkbenchWindow workbenchWindow;
	
	private WorkbenchActivityMonitor() {		
		update();
	}
	
	boolean isStarted() {
		return started;
	}
	
	void start() {
		if (!started) {
			started = true;
			update();
		}
	}
	
	void stop() {
		if (started) {
			started = false;
			update();
		}
	}
	
	private void update() {		
		if (workbench != null) 
			workbench.removeWindowListener(windowListener);
		
		if (workbench instanceof Workbench) {
			IActivationService activationService = ((Workbench) workbench).getCompositeActivationService();
			activationService.removeActivationServiceListener(activationServiceListener);			
		}		
		
		if (workbenchWindow != null) 
			workbenchWindow.removePageListener(pageListener);

		/*
		if (workbenchWindow instanceof WorkbenchWindow) {
			IActivationService activationService = ((WorkbenchWindow) workbenchWindow).getCompositeActivationService();
			activationService.removeActivationServiceListener(activationServiceListener);			
		}
		*/
		
		if (workbenchPage instanceof WorkbenchPage) {		
			IActivationService activationService = ((WorkbenchPage) workbenchPage).getCompositeActivationService();
			activationService.removeActivationServiceListener(activationServiceListener);			
		}

		workbench = null;
		workbenchWindow = null;
		workbenchPage = null;
		Set activeActivityIds = new HashSet();
		
		if (started) {
			workbench = PlatformUI.getWorkbench();
			
			if (workbench != null)
				workbenchWindow = workbench.getActiveWorkbenchWindow();
			
			if (workbenchWindow != null) 
				workbenchPage = workbenchWindow.getActivePage();
			
			if (workbench != null) 
				workbench.addWindowListener(windowListener);
			
			if (workbench instanceof Workbench) {		
				IActivationService activationService = ((Workbench) workbench).getCompositeActivationService();
				activationService.addActivationServiceListener(activationServiceListener);			
				
				try {
					activeActivityIds.addAll(activationService.getActiveActivityIds());
				} catch (DisposedException eDisposed) {
				}
			}		
			
			if (workbenchWindow != null) 
				workbenchWindow.addPageListener(pageListener);
			
			/*
			if (workbenchWindow instanceof WorkbenchWindow) {
				IActivationService activationService = ((WorkbenchWindow) workbenchWindow).getCompositeActivationService();
				activationService.addActivationServiceListener(activationServiceListener);			
			 
				try {
					activeActivityIds.addAll(activationService.getActiveActivityIds());
				} catch (DisposedException eDisposed) {
				}			
			}
			*/
			
			if (workbenchPage instanceof WorkbenchPage) {		
				IActivationService activationService = ((WorkbenchPage) workbenchPage).getCompositeActivationService();
				activationService.addActivationServiceListener(activationServiceListener);			
			
				try {
					activeActivityIds.addAll(activationService.getActiveActivityIds());
				} catch (DisposedException eDisposed) {
				}
			}
		}

		workbench.getCommandManager().setActiveActivityIds(activeActivityIds);
	}
}
