/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.cheatsheets.composite.parser;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.internal.cheatsheets.Messages;
import org.eclipse.ui.internal.cheatsheets.composite.model.AbstractTask;
import org.eclipse.ui.internal.cheatsheets.registry.CheatSheetRegistryReader;
import org.w3c.dom.Node;

public class EditableTaskParseStrategy implements ITaskParseStrategy {
	
	private boolean editableChildErrorReported;
	
	public void init() {
		editableChildErrorReported = false;
	}

	public boolean parseElementNode(Node childNode, Node parentNode,
			AbstractTask parentTask, IStatusContainer status) {	
		boolean isElementHandled = true;
		String nodeName = childNode.getNodeName();
		if (CompositeCheatSheetParser.isAbstractTask(nodeName)) {
			if (!editableChildErrorReported  ){
				 editableChildErrorReported = true;
				 String message = NLS.bind(
							Messages.ERROR_EDITABLE_TASK_WITH_CHILDREN,
							(new Object[] { parentTask.getName()}));
				status.addStatus(IStatus.ERROR, message, null);
			}
		} else {
			isElementHandled = false;
		}
		return isElementHandled;
	}

	public void parsingComplete(AbstractTask parentTask, IStatusContainer status) {
		if (parentTask.getKind() == null) {
			String message = NLS.bind(
					Messages.ERROR_PARSING_TASK_NO_KIND,
					(new Object[] { parentTask.getName()}));
		    status.addStatus(IStatus.ERROR, message, null);
		} else if (CheatSheetRegistryReader.getInstance().
				findTaskEditor(parentTask.getKind()) == null) {
			String message = NLS.bind(
					Messages.ERROR_PARSING_TASK_INVALID_KIND,
					(new Object[] { parentTask.getKind(), ICompositeCheatsheetTags.TASK, parentTask.getName()}));
		    status.addStatus(IStatus.ERROR, message, null);
		}
	}

}
