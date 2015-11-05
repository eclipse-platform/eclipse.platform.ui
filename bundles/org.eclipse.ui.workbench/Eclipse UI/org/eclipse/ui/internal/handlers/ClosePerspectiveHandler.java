/*******************************************************************************
 * Copyright (c) 2007, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.handlers;

import java.util.Map;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.e4.compatibility.E4Util;

public class ClosePerspectiveHandler extends AbstractHandler {


	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow activeWorkbenchWindow = HandlerUtil
				.getActiveWorkbenchWindow(event);
		if (activeWorkbenchWindow != null) {
			WorkbenchPage page = (WorkbenchPage) activeWorkbenchWindow
					.getActivePage();
			if (page != null) {
				Map parameters = event.getParameters();
				String value = (String) parameters
						.get(IWorkbenchCommandConstants.WINDOW_CLOSE_PERSPECTIVE_PARM_ID);
				if (value == null) {
					page.closePerspective(page.getPerspective(), true, true);
				} else {
					IPerspectiveDescriptor perspective = activeWorkbenchWindow
							.getWorkbench().getPerspectiveRegistry()
							.findPerspectiveWithId(value);
					if (perspective != null) {
						page.closePerspective(perspective, true, true);
					}
				}
			}
		}
		return null;
	}

	/**
	 * Closes the specified perspective. Nothing will happen if the given page
	 * or perspective are <code>null</null>.
	 * @param page
	 * 		a reference to the page
	 * @param persp
	 * 		the perspective to close
	 */
	public static void closePerspective(WorkbenchPage page, Object persp) {
		// if (page != null && persp != null) {
		// page.closePerspective(persp.getDesc(), true, true);
		// }
		E4Util.unsupported("Need a better way to close the perspective"); //$NON-NLS-1$
	}
}
