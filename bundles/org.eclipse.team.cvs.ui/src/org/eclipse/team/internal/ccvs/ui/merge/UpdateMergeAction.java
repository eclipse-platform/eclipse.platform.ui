package org.eclipse.team.internal.ccvs.ui.merge;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.client.Update;
import org.eclipse.team.internal.ccvs.ui.sync.CVSSyncCompareInput;
import org.eclipse.team.internal.ccvs.ui.sync.UpdateSyncAction;

public class UpdateMergeAction extends UpdateSyncAction {
	public UpdateMergeAction(CVSSyncCompareInput model, ISelectionProvider sp, String label, Shell shell) {
		super(model, sp, label, shell);
	}
	protected Command.LocalOption[] getLocalOptions(Command.LocalOption[] baseOptions) {
		List list = new ArrayList();
		list.addAll(Arrays.asList(baseOptions));
		CVSTag startTag = ((MergeEditorInput)getDiffModel()).getStartTag();
		CVSTag endTag = ((MergeEditorInput)getDiffModel()).getEndTag();

		if(!Update.IGNORE_LOCAL_CHANGES.isElementOf(baseOptions)) {
			list.add(Update.makeArgumentOption(Update.JOIN, startTag.getName()));
			list.add(Update.makeArgumentOption(Update.JOIN, endTag.getName()));
		} else {
			//list.add(Update.makeTagOption(endTag));
			list.add(Update.makeArgumentOption(Update.JOIN, endTag.getName()));
		}
		return (Command.LocalOption[]) list.toArray(new Command.LocalOption[list.size()]);
	}
}
