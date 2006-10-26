package org.eclipse.debug.internal.ui.launchConfigurations;

import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.preferences.DebugPreferencesMessages;
import org.eclipse.debug.internal.ui.preferences.IDebugPreferenceConstants;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * This class extends the standard <code>MessageDialogWithToggle</code> to provide a radio 
 * button list of actions to be taken to resolve duplicate launcgh delegates.
 * 
 * @since 3.3
 * 
 * EXPERIMENTAL
 */
public class SelectLaunchDelegateActionDialog extends MessageDialogWithToggle {

	private IPreferenceStore fStore = DebugUIPlugin.getDefault().getPreferenceStore();
	private RadioGroupFieldEditor fPromptOptions = null;
	private Object fResult = null;
	
	/**
	 * Constructor
	 * @param parentShell the parent shell for this message dialog
	 */
	public SelectLaunchDelegateActionDialog(Shell parentShell) {
		super(parentShell, 
				LaunchConfigurationsMessages.SelectLaunchDelegateActionDialog_0, 
				null, 
				LaunchConfigurationsMessages.SelectLaunchDelegateActionDialog_1, 
				WARNING, 
				new String[] {IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL}, 
				0, 
				LaunchConfigurationsMessages.SelectLaunchDelegateActionDialog_4, 
				false);
	}

	/**
	 * @see org.eclipse.jface.dialogs.MessageDialog#createCustomArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createCustomArea(Composite parent) {
		GridLayout gl = (GridLayout) parent.getLayout();
		gl.verticalSpacing = 0;
		gl.marginWidth = 0;
		gl.marginTop = 0;
		fPromptOptions = new RadioGroupFieldEditor(IDebugPreferenceConstants.PREF_DEFAULT_DUPLICATE_DELEGATE_ACTION,
				"", //$NON-NLS-1$
				1,
				new String[][] {{DebugPreferencesMessages.LaunchDelegatesPreferencePage_6, IInternalDebugUIConstants.DELEGATE_ACTION_ID_DIALOG}, {DebugPreferencesMessages.LaunchDelegatesPreferencePage_10, IInternalDebugUIConstants.DELEGATE_ACTION_ID_LIST}}, 
				parent);
		fPromptOptions.setPreferenceStore(fStore);
		fPromptOptions.load();
		Composite po = fPromptOptions.getRadioBoxControl(parent);
		po.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		gl = new GridLayout(1, true);
		gl.marginLeft = getImage().getImageData().width + 8; //+10 for composite dressings
		gl.verticalSpacing = 0;
		gl.marginWidth = 0;
		gl.marginTop = 0;
		po.setLayout(gl);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, IDebugHelpContextIds.SELECT_DUPE_LAUNCH_DELEGATE_ACTION_DIALOG);
		return parent;
	}

	/**
	 * @see org.eclipse.jface.dialogs.MessageDialogWithToggle#buttonPressed(int)
	 */
	protected void buttonPressed(int buttonId) {
		if(buttonId == IDialogConstants.OK_ID) {
			fStore.setValue(IDebugPreferenceConstants.PREF_PROMPT_FOR_DUPLICATE_DELEGATES, !getToggleState());
			fPromptOptions.store();
			DebugUIPlugin.getDefault().savePluginPreferences();
			fResult = fStore.getString(IDebugPreferenceConstants.PREF_DEFAULT_DUPLICATE_DELEGATE_ACTION);
		}
		setReturnCode(buttonId);
        close();
	}
	
	/**
	 * Returns the result of the selection of the radio buttons in the dialog
	 * @return the id of the radio button that was selected when the dialog was closed
	 */
	public Object getResult() {
		return fResult;
	}
}
