package org.eclipse.ui.internal;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.contexts.activationservice.ContextActivationServiceEvent;
import org.eclipse.ui.contexts.activationservice.ContextActivationServiceFactory;
import org.eclipse.ui.contexts.activationservice.IContextActivationService;
import org.eclipse.ui.contexts.activationservice.IContextActivationServiceListener;
import org.eclipse.ui.contexts.activationservice.ICompoundContextActiviationService;
import org.eclipse.ui.internal.contexts.activationservice.AbstractContextActivationService;

final class WorkbenchContextActiviationService extends AbstractContextActivationService {

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

	private ICompoundContextActiviationService compoundContextActivationService = ContextActivationServiceFactory.getCompoundContextActivationService();
	private boolean started;
	private IWorkbench workbench;
	private Set workbenchWindows = Collections.EMPTY_SET;

	WorkbenchContextActiviationService(IWorkbench workbench) {
		if (workbench == null)
			throw new NullPointerException();

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
			IContextActivationService contextActivationService = ((WorkbenchWindow) workbenchWindow).getContextActivationService();
			compoundContextActivationService.removeContextActivationService(contextActivationService);
		}

		for (Iterator iterator = additions.iterator(); iterator.hasNext();) {
			IWorkbenchWindow workbenchWindow = (IWorkbenchWindow) iterator.next();
			// TODO remove cast
			IContextActivationService contextActivationService = ((WorkbenchWindow) workbenchWindow).getContextActivationService();
			compoundContextActivationService.addContextActivationService(contextActivationService);
		}

		this.workbenchWindows = workbenchWindows;
	}
}
