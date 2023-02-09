/*******************************************************************************
 * Copyright (c) 2008, 2023 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.examples.contributions.view;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.examples.contributions.model.IPersonService;
import org.eclipse.ui.examples.contributions.model.Person;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Allow login as the selected user. If you are doing this for real, I would
 * suggest checking their credentials!
 *
 * @since 3.4
 */
public class LoginHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPart part = HandlerUtil.getActivePartChecked(event);
		ISelection sel = HandlerUtil.getActiveMenuSelection(event);
		if (sel == null) {
			sel = HandlerUtil.getCurrentSelection(event);
		}
		if (sel instanceof IStructuredSelection selection && !sel.isEmpty()) {
			Person person = (Person) selection.getFirstElement();
			IPersonService service = part.getSite().getService(IPersonService.class);
			service.login(person);
		}
		return null;
	}

}
