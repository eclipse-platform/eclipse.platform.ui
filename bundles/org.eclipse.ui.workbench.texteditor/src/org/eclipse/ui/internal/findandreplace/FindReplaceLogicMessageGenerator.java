/*******************************************************************************
 * Copyright (c) 2023 Vector Informatik GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Vector Informatik GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.findandreplace;

import org.eclipse.ui.internal.findandreplace.status.FindAllStatus;
import org.eclipse.ui.internal.findandreplace.status.FindStatus;
import org.eclipse.ui.internal.findandreplace.status.IStatus;
import org.eclipse.ui.internal.findandreplace.status.IStatusVisitor;
import org.eclipse.ui.internal.findandreplace.status.InvalidRegExStatus;
import org.eclipse.ui.internal.findandreplace.status.NoStatus;
import org.eclipse.ui.internal.findandreplace.status.ReplaceAllStatus;
import org.eclipse.ui.internal.texteditor.NLSUtility;

public class FindReplaceLogicMessageGenerator implements IStatusVisitor<String> {

	@Override
	public String visit(IStatus status) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String visit(ReplaceAllStatus status) {
		int replaceCount = status.getReplaceCount();
		if (replaceCount == 1) {
			return FindReplaceMessages.FindReplace_Status_replacement_label;
		}
		return NLSUtility.format(FindReplaceMessages.FindReplace_Status_replacements_label, replaceCount);
	}

	@Override
	public String visit(FindStatus status) {
		FindStatus.StatusCode messageCode = status.getMessageCode();
		String message;
		switch (messageCode) {
		case NO_MATCH:
			message = FindReplaceMessages.FindReplace_Status_noMatch_label;
			break;
		case WRAPPED:
			message = FindReplaceMessages.FindReplace_Status_wrapped_label;
			break;
		case READONLY:
			message = FindReplaceMessages.FindReplaceDialog_read_only;
			break;
		default:
			message = ""; //$NON-NLS-1$
		}

		return message;
	}

	@Override
	public String visit(InvalidRegExStatus status) {
		return status.getMessage();
	}

	@Override
	public String visit(FindAllStatus status) {
		int selectCount = status.getSelectCount();
		if (selectCount == 1) {
			return FindReplaceMessages.FindReplace_Status_selection_label;
		}
		return NLSUtility.format(FindReplaceMessages.FindReplace_Status_selections_label, selectCount);
	}

	@Override
	public String visit(NoStatus status) {
		return ""; //$NON-NLS-1$
	}

}
