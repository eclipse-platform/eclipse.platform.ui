package org.eclipse.ui.internal.commands.ws;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.commands.CommandHandlerServiceEvent;
import org.eclipse.ui.commands.CommandHandlerServiceFactory;
import org.eclipse.ui.commands.ICompoundCommandHandlerService;
import org.eclipse.ui.commands.ICommandHandlerService;
import org.eclipse.ui.commands.ICommandHandlerServiceListener;
import org.eclipse.ui.commands.IWorkbenchWindowCommandSupport;
import org.eclipse.ui.internal.commands.AbstractCommandHandlerService;

final class WorkbenchCommandHandlerService
	extends AbstractCommandHandlerService {

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

	private ICompoundCommandHandlerService compoundCommandHandlerService =
		CommandHandlerServiceFactory.getCompoundCommandHandlerService();
	private IWorkbench workbench;
	private Set workbenchWindows = Collections.EMPTY_SET;

	WorkbenchCommandHandlerService(IWorkbench workbench) {
		if (workbench == null)
			throw new NullPointerException();

		this.workbench = workbench;

		compoundCommandHandlerService
			.addCommandHandlerServiceListener(
				new ICommandHandlerServiceListener() {
			public void commandHandlerServiceChanged(CommandHandlerServiceEvent commandHandlerServiceEvent) {
				CommandHandlerServiceEvent proxyCommandHandlerServiceEvent =
					new CommandHandlerServiceEvent(
						compoundCommandHandlerService,
						commandHandlerServiceEvent
							.haveActiveCommandIdsChanged());
				fireCommandHandlerServiceChanged(commandHandlerServiceEvent);
			}
		});

		workbench.addWindowListener(windowListener);
		update();
	}

	public Set getActiveCommandIds() {
		return compoundCommandHandlerService.getActiveCommandIds();
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
			IWorkbenchWindowCommandSupport workbenchWindowCommandSupport =
				(IWorkbenchWindowCommandSupport) workbenchWindow.getAdapter(
					IWorkbenchWindowCommandSupport.class);

			if (workbenchWindowCommandSupport != null) {
				ICommandHandlerService commandHandlerService =
					workbenchWindowCommandSupport.getCommandHandlerService();
				compoundCommandHandlerService
					.removeCommandHandlerService(
					commandHandlerService);
			}
		}

		for (Iterator iterator = additions.iterator(); iterator.hasNext();) {
			IWorkbenchWindow workbenchWindow =
				(IWorkbenchWindow) iterator.next();
			IWorkbenchWindowCommandSupport workbenchWindowCommandSupport =
				(IWorkbenchWindowCommandSupport) workbenchWindow.getAdapter(
					IWorkbenchWindowCommandSupport.class);

			if (workbenchWindowCommandSupport != null) {
				ICommandHandlerService commandHandlerService =
					workbenchWindowCommandSupport.getCommandHandlerService();
				compoundCommandHandlerService.addCommandHandlerService(
					commandHandlerService);
			}
		}

		this.workbenchWindows = workbenchWindows;
	}
}
