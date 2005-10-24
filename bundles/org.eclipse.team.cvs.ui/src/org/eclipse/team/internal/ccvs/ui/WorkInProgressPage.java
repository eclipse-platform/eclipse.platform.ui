package org.eclipse.team.internal.ccvs.ui;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class WorkInProgressPage extends CVSFieldEditorPreferencePage {

	protected String getPageHelpContextId() {
		// TODO Auto-generated method stub
		return null;
	}

	protected String getPageDescription() {
		return null;
	}

	protected void createFieldEditors() {
		addField(new BooleanFieldEditor(
		        ICVSUIConstants.PREF_ENABLEMODELUPDATE, 
				CVSUIMessages.WorkInProgress_EnableModelUpdate,  
				BooleanFieldEditor.DEFAULT, 
				getFieldEditorParent()) {
            protected Button getChangeControl(Composite parent) {
                Button button = super.getChangeControl(parent);
                //PlatformUI.getWorkbench().getHelpSystem().setHelp(button, IHelpContextIds.PREF_CONSIDER_CONTENT);
                return button;
            }
		});

	}

}
