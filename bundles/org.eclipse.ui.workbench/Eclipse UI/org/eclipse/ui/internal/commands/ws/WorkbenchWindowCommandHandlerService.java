package org.eclipse.ui.internal.commands.ws;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
import org.eclipse.ui.commands.ICommandHandlerService;
import org.eclipse.ui.commands.ICommandHandlerServiceListener;
import org.eclipse.ui.commands.IHandler;
import org.eclipse.ui.commands.IWorkbenchCommandSupport;
import org.eclipse.ui.commands.IWorkbenchPageCommandSupport;
import org.eclipse.ui.commands.IWorkbenchPartSiteCommandSupport;
import org.eclipse.ui.internal.commands.AbstractCommandHandlerService;
import org.eclipse.ui.internal.util.Util;

final class WorkbenchWindowCommandHandlerService
	extends AbstractCommandHandlerService {
	private final ICommandHandlerServiceListener commandHandlerServiceListener =
		new ICommandHandlerServiceListener() {
		public void commandHandlerServiceChanged(CommandHandlerServiceEvent commandHandlerServiceEvent) {
			update();
		}
	};
	private Map handlersByCommandId = new HashMap();
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
	private IWorkbench workbench;
	private ICommandHandlerService workbenchCompoundCommandHandlerService;
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
		workbenchWindow.addPageListener(pageListener);
		workbenchWindow.addPerspectiveListener(perspectiveListener);
		workbenchWindow.getPartService().addPartListener(partListener);
		update();
	}

	public Map getHandlersByCommandId() {
		return Collections.unmodifiableMap(handlersByCommandId);
	}

	private void setHandlersByCommandId(Map handlersByCommandId) {
		handlersByCommandId =
			Util.safeCopy(
				handlersByCommandId,
				String.class,
				IHandler.class,
				false,
				true);
		boolean commandHandlerServiceChanged = false;
		Map commandEventsByCommandId = null;

		if (!this.handlersByCommandId.equals(handlersByCommandId)) {
			this.handlersByCommandId = handlersByCommandId;
			fireCommandHandlerServiceChanged(
				new CommandHandlerServiceEvent(this, true));
		}
	}

	private void update() {
		ICommandHandlerService workbenchCompoundCommandHandlerService = null;
		IWorkbenchPage workbenchPage = workbenchWindow.getActivePage();
		ICommandHandlerService workbenchPageCompoundCommandHandlerService =
			null;
		IWorkbenchPart workbenchPart = null;
		IWorkbenchPartSite workbenchPartSite = null;
		ICommandHandlerService workbenchPartSiteMutableCommandHandlerService =
			null;
		IWorkbenchCommandSupport workbenchCommandSupport =
			workbench.getCommandSupport();
		workbenchCompoundCommandHandlerService =
			workbenchCommandSupport.getCompoundCommandHandlerService();

		if (workbenchPage != null) {
			IWorkbenchPageCommandSupport workbenchPageCommandSupport =
				workbenchPage.getCommandSupport();
			workbenchPageCompoundCommandHandlerService =
				workbenchPageCommandSupport.getCompoundCommandHandlerService();
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

		if (this.workbenchCompoundCommandHandlerService
			!= workbenchCompoundCommandHandlerService) {
			if (this.workbenchCompoundCommandHandlerService != null)
				this
					.workbenchCompoundCommandHandlerService
					.removeCommandHandlerServiceListener(
					commandHandlerServiceListener);

			this.workbenchCompoundCommandHandlerService =
				workbenchCompoundCommandHandlerService;

			if (this.workbenchCompoundCommandHandlerService != null)
				this
					.workbenchCompoundCommandHandlerService
					.addCommandHandlerServiceListener(
					commandHandlerServiceListener);
		}

		if (this.workbenchPageCompoundCommandHandlerService
			!= workbenchPageCompoundCommandHandlerService) {
			if (this.workbenchPageCompoundCommandHandlerService != null)
				this
					.workbenchPageCompoundCommandHandlerService
					.removeCommandHandlerServiceListener(commandHandlerServiceListener);

			this.workbenchPageCompoundCommandHandlerService =
				workbenchPageCompoundCommandHandlerService;

			if (this.workbenchPageCompoundCommandHandlerService != null)
				this
					.workbenchPageCompoundCommandHandlerService
					.addCommandHandlerServiceListener(commandHandlerServiceListener);
		}

		if (this.workbenchPartSiteMutableCommandHandlerService
			!= workbenchPartSiteMutableCommandHandlerService) {
			if (this.workbenchPartSiteMutableCommandHandlerService != null)
				this
					.workbenchPartSiteMutableCommandHandlerService
					.removeCommandHandlerServiceListener(commandHandlerServiceListener);

			this.workbenchPartSiteMutableCommandHandlerService =
				workbenchPartSiteMutableCommandHandlerService;

			if (this.workbenchPartSiteMutableCommandHandlerService != null)
				this
					.workbenchPartSiteMutableCommandHandlerService
					.addCommandHandlerServiceListener(commandHandlerServiceListener);
		}

		Map handlersByCommandId = new HashMap();

		if (this.workbenchCompoundCommandHandlerService != null)
			handlersByCommandId.putAll(
				workbenchCompoundCommandHandlerService
					.getHandlersByCommandId());

		if (this.workbenchPageCompoundCommandHandlerService != null)
			handlersByCommandId.putAll(
				workbenchPageCompoundCommandHandlerService
					.getHandlersByCommandId());

		if (this.workbenchPartSiteMutableCommandHandlerService != null)
			handlersByCommandId.putAll(
				workbenchPartSiteMutableCommandHandlerService
					.getHandlersByCommandId());

		setHandlersByCommandId(handlersByCommandId);
	}
}
