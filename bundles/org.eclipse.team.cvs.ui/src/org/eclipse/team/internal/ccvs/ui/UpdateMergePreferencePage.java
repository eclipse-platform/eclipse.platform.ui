package org.eclipse.team.internal.ccvs.ui;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;

public class UpdateMergePreferencePage extends CVSFieldEditorPreferencePage {

	private BooleanFieldEditor enableClientUpdate;
	private RadioGroupFieldEditor updateStyle;
	private RadioGroupFieldEditor updatePreviewStyle;

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
						CVSUIMessages.WorkInProgressPage_0,
						1,
						new String[][] {
							new String[] {CVSUIMessages.WorkInProgressPage_1, ICVSUIConstants.PREF_UPDATE_HANDLING_PREVIEW},
							new String[] {CVSUIMessages.WorkInProgressPage_2, ICVSUIConstants.PREF_UPDATE_HANDLING_PERFORM},
							new String[] {CVSUIMessages.UpdateMergePreferencePage_0, ICVSUIConstants.PREF_UPDATE_HANDLING_TRADITIONAL}
						},
						getFieldEditorParent(),
						true);
		addField(updateStyle);
		
		updatePreviewStyle = new RadioGroupFieldEditor(
				ICVSUIConstants.PREF_UPDATE_PREVIEW,
				CVSUIMessages.UpdateMergePreferencePage_1,
				1,
				new String[][] {
					new String[] {CVSUIMessages.UpdateMergePreferencePage_2,  ICVSUIConstants.PREF_UPDATE_PREVIEW_IN_DIALOG},
					new String[] {CVSUIMessages.UpdateMergePreferencePage_3, ICVSUIConstants.PREF_UPDATE_PREVIEW_IN_SYNCVIEW}
				},
				getFieldEditorParent(),
				true);
		addField(updatePreviewStyle);
		
		// TODO: Add option for sync and compare to use old or new (add to sync compare page)
		
		// TODO Add option for merge to use old or new
		
		updateStyle.setEnabled(CVSUIPlugin.getPlugin().getPreferenceStore().getBoolean(ICVSUIConstants.PREF_ENABLEMODELUPDATE), getFieldEditorParent());
		updatePreviewStyle.setEnabled(CVSUIPlugin.getPlugin().getPreferenceStore().getBoolean(ICVSUIConstants.PREF_ENABLEMODELUPDATE), getFieldEditorParent());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getSource() == enableClientUpdate) {
			updateStyle.setEnabled(((Boolean)event.getNewValue()).booleanValue(), getFieldEditorParent());
			updatePreviewStyle.setEnabled(((Boolean)event.getNewValue()).booleanValue(), getFieldEditorParent());
		}
		super.propertyChange(event);
	}

}
