package org.eclipse.ui.internal.contexts.ws;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.contexts.ContextActivationServiceEvent;
import org.eclipse.ui.contexts.ContextActivationServiceFactory;
import org.eclipse.ui.contexts.ICompoundContextActivationService;
import org.eclipse.ui.contexts.IContextActivationService;
import org.eclipse.ui.contexts.IContextActivationServiceListener;
import org.eclipse.ui.contexts.IWorkbenchWindowContextSupport;
import org.eclipse.ui.internal.contexts.AbstractContextActivationService;

final class WorkbenchContextActivationService
	extends AbstractContextActivationService {
	private ICompoundContextActivationService compoundContextActivationService =
		ContextActivationServiceFactory.getCompoundContextActivationService();
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
	private IWorkbench workbench;
	private Set workbenchWindows = Collections.EMPTY_SET;

	WorkbenchContextActivationService(IWorkbench workbench) {
		if (workbench == null)
			throw new NullPointerException();

		this.workbench = workbench;

		compoundContextActivationService
			.addContextActivationServiceListener(
				new IContextActivationServiceListener() {
			public void contextActivationServiceChanged(ContextActivationServiceEvent contextActivationServiceEvent) {
				ContextActivationServiceEvent proxyContextActivationServiceEvent =
					new ContextActivationServiceEvent(
						compoundContextActivationService,
						contextActivationServiceEvent
							.haveActiveContextIdsChanged());
				fireContextActivationServiceChanged(contextActivationServiceEvent);
			}
		});

		workbench.addWindowListener(windowListener);
		update();
	}

	public Set getActiveContextIds() {
		return compoundContextActivationService.getActiveContextIds();
	}

	private void update() {
		Set workbenchWindows =
			new HashSet(Arrays.asList(workbench.getWorkbenchWindows()));
		Set removals = new HashSet(this.workbenchWindows);
		removals.removeAll(workbenchWindows);
		Set additions = new HashSet(workbenchWindows);
		additions.removeAll(this.workbenchWindows);

		for (Iterator iterator = removals.iterator(); iterator.hasNext();) {
			IWorkbenchWindow workbenchWindow =
				(IWorkbenchWindow) iterator.next();
			IWorkbenchWindowContextSupport workbenchWindowContextSupport =
				workbenchWindow.getContextSupport();

			if (workbenchWindowContextSupport != null) {
				IContextActivationService contextActivationService =
					workbenchWindowContextSupport.getContextActivationService();
				compoundContextActivationService
					.removeContextActivationService(
					contextActivationService);
			}
		}

		for (Iterator iterator = additions.iterator(); iterator.hasNext();) {
			IWorkbenchWindow workbenchWindow =
				(IWorkbenchWindow) iterator.next();
			IWorkbenchWindowContextSupport workbenchWindowContextSupport =
				workbenchWindow.getContextSupport();

			if (workbenchWindowContextSupport != null) {
				IContextActivationService contextActivationService =
					workbenchWindowContextSupport.getContextActivationService();
				compoundContextActivationService.addContextActivationService(
					contextActivationService);
			}
		}

		this.workbenchWindows = workbenchWindows;
	}
}
