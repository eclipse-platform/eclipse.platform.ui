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
import org.eclipse.ui.internal.findandreplace.status.IFindReplaceStatus;
import org.eclipse.ui.internal.findandreplace.status.IFindReplaceStatusVisitor;
import org.eclipse.ui.internal.findandreplace.status.InvalidRegExStatus;
import org.eclipse.ui.internal.findandreplace.status.NoStatus;
import org.eclipse.ui.internal.findandreplace.status.ReplaceAllStatus;
import org.eclipse.ui.internal.texteditor.NLSUtility;

public class FindReplaceLogicMessageGenerator implements IFindReplaceStatusVisitor<String> {

	@Override
	public String visit(IFindReplaceStatus status) {
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
		return switch (messageCode) {
			case NO_MATCH -> FindReplaceMessages.FindReplace_Status_noMatch_label;
			case WRAPPED -> FindReplaceMessages.FindReplace_Status_wrapped_label;
			case READONLY -> FindReplaceMessages.FindReplaceDialog_read_only;
			default -> ""; //$NON-NLS-1$
		};
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
