package org.eclipse.ui.internal.commands.ws;

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
import org.eclipse.ui.commands.CommandHandlerServiceEvent;
import org.eclipse.ui.commands.CommandHandlerServiceFactory;
import org.eclipse.ui.commands.ICompoundCommandHandlerService;
import org.eclipse.ui.commands.ICommandHandlerService;
import org.eclipse.ui.commands.ICommandHandlerServiceListener;
import org.eclipse.ui.commands.IWorkbenchCommandSupport;
import org.eclipse.ui.commands.IWorkbenchPageCommandSupport;
import org.eclipse.ui.commands.IWorkbenchPartSiteCommandSupport;
import org.eclipse.ui.internal.commands.AbstractCommandHandlerService;

final class WorkbenchWindowCommandHandlerService
	extends AbstractCommandHandlerService {

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

	private ICompoundCommandHandlerService compoundCommandHandlerService =
		CommandHandlerServiceFactory.getCompoundCommandHandlerService();
	private IWorkbench workbench;
	private ICommandHandlerService workbenchPageCompoundCommandHandlerService;
	private ICommandHandlerService workbenchPartSiteMutableCommandHandlerService;
	private IWorkbenchWindow workbenchWindow;

	WorkbenchWindowCommandHandlerService(IWorkbenchWindow workbenchWindow) {
		if (workbenchWindow == null)
			throw new NullPointerException();

		IWorkbench workbench = workbenchWindow.getWorkbench();

		if (workbench == null)
			throw new NullPointerException();

		this.workbenchWindow = workbenchWindow;
		this.workbench = workbench;

		compoundCommandHandlerService
			.addCommandHandlerServiceListener(
				new ICommandHandlerServiceListener() {
			public void commandHandlerServiceChanged(CommandHandlerServiceEvent commandHandlerServiceEvent) {
				CommandHandlerServiceEvent proxyCommandHandlerServiceEvent =
					new CommandHandlerServiceEvent(
							WorkbenchWindowCommandHandlerService.this,
						commandHandlerServiceEvent
							.haveActiveCommandIdsChanged());
				fireCommandHandlerServiceChanged(
					(proxyCommandHandlerServiceEvent));
			}
		});

		IWorkbenchCommandSupport workbenchCommandSupport =
			(IWorkbenchCommandSupport) workbench.getAdapter(
				IWorkbenchCommandSupport.class);

		if (workbenchCommandSupport != null)
			compoundCommandHandlerService.addCommandHandlerService(
				workbenchCommandSupport.getCompoundCommandHandlerService());

		workbenchWindow.addPageListener(pageListener);
		workbenchWindow.addPerspectiveListener(perspectiveListener);
		workbenchWindow.getPartService().addPartListener(partListener);
		update();
	}

	public Set getActiveCommandIds() {
		return compoundCommandHandlerService.getActiveCommandIds();
	}

	private void update() {
		IWorkbenchPage workbenchPage = workbenchWindow.getActivePage();
		ICommandHandlerService workbenchPageCompoundCommandHandlerService =
			null;
		IWorkbenchPart workbenchPart = null;
		IWorkbenchPartSite workbenchPartSite = null;
		ICommandHandlerService workbenchPartSiteMutableCommandHandlerService =
			null;

		if (workbenchPage != null) {
			IWorkbenchPageCommandSupport workbenchPageCommandSupport =
				(IWorkbenchPageCommandSupport) workbenchPage.getAdapter(
					IWorkbenchPageCommandSupport.class);

			if (workbenchPageCommandSupport != null)
				workbenchPageCompoundCommandHandlerService =
					workbenchPageCommandSupport
						.getCompoundCommandHandlerService();

			workbenchPart = workbenchPage.getActivePart();
		}

		if (workbenchPart != null)
			workbenchPartSite = workbenchPart.getSite();

		if (workbenchPartSite != null) {
			IWorkbenchPartSiteCommandSupport workbenchPartSiteCommandSupport =
				(
					IWorkbenchPartSiteCommandSupport) workbenchPartSite
						.getAdapter(
					IWorkbenchPartSiteCommandSupport.class);

			if (workbenchPartSiteCommandSupport != null)
				workbenchPartSiteMutableCommandHandlerService =
					workbenchPartSiteCommandSupport
						.getMutableCommandHandlerService();
		}

		Set removals = new HashSet();

		if (this.workbenchPageCompoundCommandHandlerService
			!= workbenchPageCompoundCommandHandlerService) {
			if (this.workbenchPageCompoundCommandHandlerService != null)
				removals.add(
					this.workbenchPageCompoundCommandHandlerService);

			this.workbenchPageCompoundCommandHandlerService =
				workbenchPageCompoundCommandHandlerService;
		}

		if (this.workbenchPartSiteMutableCommandHandlerService
			!= workbenchPartSiteMutableCommandHandlerService) {
			if (this.workbenchPartSiteMutableCommandHandlerService != null)
				removals.add(
					this.workbenchPartSiteMutableCommandHandlerService);

			this.workbenchPartSiteMutableCommandHandlerService =
				workbenchPartSiteMutableCommandHandlerService;
		}

		for (Iterator iterator = removals.iterator(); iterator.hasNext();) {
			ICommandHandlerService commandHandlerService =
				(ICommandHandlerService) iterator.next();
			compoundCommandHandlerService.removeCommandHandlerService(
				commandHandlerService);
		}

		Set additions = new HashSet();

		if (this.workbenchPageCompoundCommandHandlerService != null)
			additions.add(this.workbenchPageCompoundCommandHandlerService);

		if (this.workbenchPartSiteMutableCommandHandlerService != null)
			additions.add(
				this.workbenchPartSiteMutableCommandHandlerService);

		for (Iterator iterator = additions.iterator(); iterator.hasNext();) {
			ICommandHandlerService commandHandlerService =
				(ICommandHandlerService) iterator.next();
			compoundCommandHandlerService.addCommandHandlerService(
				commandHandlerService);
		}
	}
}
