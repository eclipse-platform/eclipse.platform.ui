package org.eclipse.team.internal.ccvs.ui;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;

public class WorkInProgressPage extends CVSFieldEditorPreferencePage {

	private BooleanFieldEditor enableClientUpdate;
	private RadioGroupFieldEditor updateStyle;

	protected String getPageHelpContextId() {
		// TODO Auto-generated method stub
		return null;
	}

	protected String getPageDescription() {
		return null;
	}

	protected void createFieldEditors() {
		enableClientUpdate = new BooleanFieldEditor(
				        ICVSUIConstants.PREF_ENABLEMODELUPDATE, 
						CVSUIMessages.WorkInProgress_EnableModelUpdate,  
						BooleanFieldEditor.DEFAULT, 
						getFieldEditorParent());
		addField(enableClientUpdate);
		updateStyle = new RadioGroupFieldEditor(
						ICVSUIConstants.PREF_UPDATE_HANDLING,
						"When performing a Team Update",
						1,
						new String[][] {
							new String[] {"Show all changes in the merge dialog",  ICVSUIConstants.PREF_UPDATE_HANDLING_PREVIEW},
							new String[] {"Merge all non-conflicting changes and only show conflicts in the dialog", ICVSUIConstants.PREF_UPDATE_HANDLING_PERFORM }
						},
						getFieldEditorParent(),
						true);
		addField(updateStyle);
		
		updateStyle.setEnabled(CVSUIPlugin.getPlugin().getPreferenceStore().getBoolean(ICVSUIConstants.PREF_ENABLEMODELUPDATE), getFieldEditorParent());

	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getSource() == enableClientUpdate) {
			updateStyle.setEnabled(((Boolean)event.getNewValue()).booleanValue(), getFieldEditorParent());
		}
		super.propertyChange(event);
	}

}
