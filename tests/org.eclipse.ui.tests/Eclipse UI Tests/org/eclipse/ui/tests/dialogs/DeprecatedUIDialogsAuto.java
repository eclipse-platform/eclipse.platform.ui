package org.eclipse.ui.tests.dialogs;

import junit.framework.TestCase;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.YesNoCancelListSelectionDialog;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPartLabelProvider;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.model.AdaptableList;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.tests.util.DialogCheck;


public class DeprecatedUIDialogsAuto extends TestCase {
	private static final String PROJECT_SELECTION_MESSAGE = WorkbenchMessages.getString("BuildOrderPreference.selectOtherProjects");
	private static final String FILTER_SELECTION_MESSAGE = ResourceNavigatorMessagesCopy.getString("FilterSelection.message");
	
	public DeprecatedUIDialogsAuto(String name) {
		super(name);
	}
	private Shell getShell() {
		return DialogCheck.getShell();
	}
	private IWorkbench getWorkbench() {
		return WorkbenchPlugin.getDefault().getWorkbench();
	}
	
	public void testSaveAll() {
		YesNoCancelListSelectionDialog dialog = new YesNoCancelListSelectionDialog(
			getShell(),
			new AdaptableList(),
			new WorkbenchContentProvider(),
			new WorkbenchPartLabelProvider(),
			WorkbenchMessages.getString("EditorManager.saveResourcesMessage")
		);
		dialog.setTitle(WorkbenchMessages.getString("EditorManager.saveResourcesTitle"));
		DialogCheck.assertDialogTexts(dialog, this);
	}
	
}



