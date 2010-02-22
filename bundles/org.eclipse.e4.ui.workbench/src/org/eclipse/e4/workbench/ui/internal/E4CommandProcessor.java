/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.workbench.ui.internal;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.commands.Category;
import org.eclipse.core.commands.IParameter;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MCommand;
import org.eclipse.e4.ui.model.application.MCommandParameter;
import org.eclipse.emf.common.util.EList;

/**
 *
 */
public class E4CommandProcessor {
	public static void processCommands(IEclipseContext context, List<MCommand> commands) {
		// fill in commands
		Activator.trace(Policy.DEBUG_CMDS, "Initialize service from model", null); //$NON-NLS-1$
		ECommandService cs = (ECommandService) context.get(ECommandService.class.getName());
		Category cat = cs
				.defineCategory(MApplication.class.getName(), "Application Category", null); //$NON-NLS-1$
		for (MCommand cmd : commands) {
			IParameter[] parms = null;
			String id = cmd.getId();
			String name = cmd.getCommandName();
			EList<MCommandParameter> modelParms = cmd.getParameters();
			if (modelParms != null && !modelParms.isEmpty()) {
				ArrayList<Parameter> parmList = new ArrayList<Parameter>();
				for (MCommandParameter cmdParm : modelParms) {
					parmList.add(new Parameter(cmdParm.getId(), cmdParm.getName(), null, null,
							cmdParm.isOptional()));
				}
				parms = parmList.toArray(new Parameter[parmList.size()]);
			}
			cs.defineCommand(id, name, null, cat, parms);
		}

	}
}
