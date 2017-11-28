/*******************************************************************************
 * Copyright (c) 2017 vogella GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * - Simon Scholz <simon.scholz@vogella.com> - initial implementation
 *******************************************************************************/
package org.eclipse.ui.genericeditor.examples.dotproject.hyperlink;

import java.util.Collections;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.handlers.ShowInSystemExplorerHandler;

@SuppressWarnings("restriction")
public class ShowInSystemExplorerHyperlink implements IHyperlink {

	private String fileLocation;
	private IRegion region;

	public ShowInSystemExplorerHyperlink(String fileLocation, IRegion region) {
		this.fileLocation = fileLocation;
		this.region = region;
	}

	@Override
	public IRegion getHyperlinkRegion() {
		return region;
	}

	@Override
	public String getTypeLabel() {
		return null;
	}

	@Override
	public String getHyperlinkText() {
		return "Show in System Explorer";
	}

	@Override
	public void open() {
		ECommandService commandService = PlatformUI.getWorkbench().getService(ECommandService.class);
		EHandlerService handlerService = PlatformUI.getWorkbench().getService(EHandlerService.class);

		Command command = commandService.getCommand(ShowInSystemExplorerHandler.ID);
		if (command.isDefined()) {
			ParameterizedCommand parameterizedCommand = commandService.createCommand(ShowInSystemExplorerHandler.ID,
					Collections.singletonMap(ShowInSystemExplorerHandler.RESOURCE_PATH_PARAMETER, fileLocation));
			if (handlerService.canExecute(parameterizedCommand)) {
				handlerService.executeHandler(parameterizedCommand);
			}
		}
	}

}
