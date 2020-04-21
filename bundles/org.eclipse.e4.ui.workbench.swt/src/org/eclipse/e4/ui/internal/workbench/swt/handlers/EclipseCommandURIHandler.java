/*******************************************************************************
 * Copyright (c) 2020 Red Hat Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 ******************************************************************************/

package org.eclipse.e4.ui.internal.workbench.swt.handlers;

import java.net.URI;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.internal.workbench.swt.E4WorkbenchSWTMessages;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.urischeme.IUriSchemeHandler;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

/**
 * Handler for eclipse+command:// URLs, eg.
 * eclipse+command://org.eclipse.ui.window.preferences?preferencePageId=org.eclipse.ui.browser.preferencePage
 */
public class EclipseCommandURIHandler implements IUriSchemeHandler {

	@Override
	public void handle(String uriString) {
		URI uri = URI.create(uriString);
		String commandId = uri.getHost();
		BundleContext bundleContext = FrameworkUtil.getBundle(IWorkbench.class).getBundleContext();
		IEclipseContext context = EclipseContextFactory.getServiceContext(bundleContext).getActiveLeaf();
		ECommandService commandService = context.get(ECommandService.class);
		EHandlerService eHandlerService = context.get(EHandlerService.class);
		Command command = commandService.getCommand(commandId);
		String query = uri.getQuery();
		if (query == null) {
			query = ""; //$NON-NLS-1$
		}
		Map<String, String> uriParams = Arrays.stream(query.split("&")) //$NON-NLS-1$
				.filter(s -> !s.isEmpty()) //
				.map(param -> param.split("=")) //$NON-NLS-1$
				.collect(Collectors.toMap(segments -> segments[0], segements -> segements[1]));
		ParameterizedCommand parametrizedCommand = ParameterizedCommand.generateCommand(command, uriParams);
		// a confirmation dialog is required as invoking arbitrary commands from
		// external applications is an unsecure operation
		Display.getDefault().asyncExec(() -> {
			String commandName = E4WorkbenchSWTMessages.openCommandFromUIHandler_undefined;
			try {
				commandName = command.getName();
			} catch (NotDefinedException e) {
				// nothing to do
			}
			if (MessageDialog.openConfirm(Display.getCurrent().getActiveShell(),
					E4WorkbenchSWTMessages.openCommandFromURIHandler_confirm_title,
					NLS.bind(E4WorkbenchSWTMessages.openCommandFromURIHandler_confirm_message, uri, commandName))) {
				eHandlerService.executeHandler(parametrizedCommand);
			}
		});
	}


}
