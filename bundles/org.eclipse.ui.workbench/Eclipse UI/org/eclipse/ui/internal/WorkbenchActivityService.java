package org.eclipse.ui.internal;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.activities.service.AbstractActivityService;
import org.eclipse.ui.activities.service.ActivityServiceEvent;
import org.eclipse.ui.activities.service.ActivityServiceFactory;
import org.eclipse.ui.activities.service.IActivityService;
import org.eclipse.ui.activities.service.IActivityServiceListener;
import org.eclipse.ui.activities.service.ICompoundActivityService;

final class WorkbenchActivityService extends AbstractActivityService {

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

	private ICompoundActivityService compoundActivityService = ActivityServiceFactory.getCompoundActivityService();
	private boolean started;
	private IWorkbench workbench;
	private Set workbenchWindows = Collections.EMPTY_SET;

	WorkbenchActivityService(IWorkbench workbench) {
		if (workbench == null)
			throw new NullPointerException();

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
			workbench.addWindowListener(windowListener);
			update();
		}
	}

	void stop() {
		if (started) {
			started = false;
			workbench.removeWindowListener(windowListener);
			update();
		}
	}

	private void update() {
		Set workbenchWindows = new HashSet();

		if (started)
			workbenchWindows.addAll(Arrays.asList(workbench.getWorkbenchWindows()));

		Set removals = new HashSet(this.workbenchWindows);
		removals.removeAll(workbenchWindows);
		Set additions = new HashSet(workbenchWindows);
		additions.removeAll(this.workbenchWindows);

		for (Iterator iterator = removals.iterator(); iterator.hasNext();) {
			IWorkbenchWindow workbenchWindow = (IWorkbenchWindow) iterator.next();
			// TODO remove cast
			IActivityService activityService = ((WorkbenchWindow) workbenchWindow).getActivityService();
			compoundActivityService.removeActivityService(activityService);
		}

		for (Iterator iterator = additions.iterator(); iterator.hasNext();) {
			IWorkbenchWindow workbenchWindow = (IWorkbenchWindow) iterator.next();
			// TODO remove cast
			IActivityService activityService = ((WorkbenchWindow) workbenchWindow).getActivityService();
			compoundActivityService.addActivityService(activityService);
		}

		this.workbenchWindows = workbenchWindows;
	}
}
