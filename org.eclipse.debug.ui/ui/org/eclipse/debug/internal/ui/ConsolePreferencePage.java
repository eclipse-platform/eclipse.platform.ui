package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.preference.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.texteditor.WorkbenchChainedTextFontFieldEditor;

/**
 * A page to set the preferences for the console
 */
public class ConsolePreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage, IDebugPreferenceConstants, ModifyListener {
	
	Text fMaxOutputField;
	
	/**
	 * Create the console page.
	 */
	public ConsolePreferencePage() {
		super(GRID);
		setDescription("Console text color settings.");
		setPreferenceStore(DebugUIPlugin.getDefault().getPreferenceStore());
	}

	public void createControl(Composite parent) {
		super.createControl(parent);
		WorkbenchHelp.setHelp(
			parent,
			new Object[] { IDebugHelpContextIds.CONSOLE_PREFERENCE_PAGE });
	}
	
	/**
	 * Create all field editors for this page
	 */
	public void createFieldEditors() {
		
		Composite parent= getFieldEditorParent();

		// Note: first String value is the key for the preference bundle and second the
		// second String value is the label displayed in front of the editor.
		ColorFieldEditor sysout= new ColorFieldEditor(CONSOLE_SYS_OUT_RGB, "Standard Out:", parent);
		ColorFieldEditor syserr= new ColorFieldEditor(CONSOLE_SYS_ERR_RGB, "Standard Error:", parent);
		ColorFieldEditor sysin= new ColorFieldEditor(CONSOLE_SYS_IN_RGB, "Standard In:", parent);
		
		WorkbenchChainedTextFontFieldEditor editor= new WorkbenchChainedTextFontFieldEditor(CONSOLE_FONT,
				"Console font setting: ", parent);
		
		addField(sysout);
		addField(syserr);
		addField(sysin);
		addField(editor);
		
		String defaultText= (new Integer(getConsoleMaxOutputSize())).toString();
		createLabelledTextArea(parent, defaultText, "Number of characters displayed (enter 0 for no limit):");

	}
	
	/**
	 * Creates a text box and sets the default configuration data.
	 * 
	 * @param parent the parent composite
	 * @param defaultText the default text in the text box
	 */
	private Text createTextBox(Composite parent, String defaultText) {
		Text text= new Text(parent, SWT.SINGLE | SWT.BORDER);
		text.setEditable(true);
		text.setText(defaultText);
		text.setFont(parent.getFont());

		GridData data = new GridData(GridData.FILL_BOTH);
		data.widthHint = 60;
		text.setLayoutData(data);
		fMaxOutputField= text;
		text.addModifyListener(this);
		return text;
	}	
	
	/**
	 * Creates a label and sets the default configuration data.
	 * 
	 * @param parent the parent composite
	 * @param text the label text
	 */
	private Label createLabel(Composite parent, String text) {
		Label label= new Label(parent, SWT.LEFT);
		label.setText(text);
		
		GridData data= new GridData();
		label.setLayoutData(data);
		
		return label;
	}
	
	/**
	 * Creates a text widget with a label
	 */
	private Composite createLabelledTextArea(Composite parent, String defaultText, String labelText) {
		Composite textArea= new Composite(parent, SWT.SHADOW_NONE);
		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		textArea.setLayout(layout);
		
		Label label= createLabel(textArea, labelText);
		Text text= createTextBox(textArea, defaultText);
		
		GridData data= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		data.horizontalSpan= 2;
		textArea.setLayoutData(data);
		
		return textArea;
	}
	
	/**
	 * @see ModifyListener
	 */
	public void modifyText(ModifyEvent e) {
		if (e.widget == fMaxOutputField) {
			doMaxOutputChanged();
		}
	}
	
	/**
	 * @see IPreferencePage
	 */
	public boolean performOk() {
		super.performOk(); // store the fields.
		IPreferenceStore store = getPreferenceStore();		
		
		int value= Integer.parseInt(fMaxOutputField.getText());
		store.setValue(CONSOLE_MAX_OUTPUT_SIZE, value);	
		getPreferenceStore().firePropertyChangeEvent(CONSOLE_MAX_OUTPUT_SIZE, new Integer(0), new Integer(value));
		return true;
	}	
	
	/**
	 * @see IPreferencePage
	 */
	protected void performDefaults() {
		super.performDefaults(); // store the fields.
		IPreferenceStore store = getPreferenceStore();	
		
		int defaultValue= store.getDefaultInt(CONSOLE_MAX_OUTPUT_SIZE);
		fMaxOutputField.setText((new Integer(defaultValue)).toString());
	}

	/**
	 * Called when the contents of the maximum console output field are changed.
	 * If the input is invalid, disable the preference page OK and APPLY buttons
	 * and set the description accordingly.
	 */
	private void doMaxOutputChanged() {
		boolean validInput= true;
		String maxString= fMaxOutputField.getText();
		try {
			int max= Integer.parseInt(maxString);
			if (max < 0) {
				validInput= false;
				setErrorMessage("Value must be an integer between 0 and " + Integer.MAX_VALUE);
			}
		} catch (NumberFormatException e) {
			validInput= false;
			setErrorMessage("Value must be an integer between 0 and " + Integer.MAX_VALUE);
		}
		if (validInput) {
			setErrorMessage(null);
		}
		setValid(validInput);
	}

	/**
	 * @see IWorkbenchPreferencePage#init
	 */
	public void init(IWorkbench workbench) {
	}
	
	/**
	 * Returns the a color based on the type.
	 */
	protected static Color getPreferenceColor(String type) {
		IPreferenceStore pstore= DebugUIPlugin.getDefault().getPreferenceStore();
		RGB outRGB= PreferenceConverter.getColor(pstore, type);
		ColorManager colorManager= DebugUIPlugin.getDefault().getColorManager();
		return colorManager.getColor(outRGB);
	}
	
	/**
	 * Returns the font data that describes the font to use for the console
	 */
	protected static FontData getConsoleFontData() {
		IPreferenceStore pstore= DebugUIPlugin.getDefault().getPreferenceStore();
		FontData fontData= PreferenceConverter.getFontData(pstore, CONSOLE_FONT);
		return fontData;
	}
	
	/**
	 * Returns the maximum number of characters to be displayed in the console
	 */
	public static int getConsoleMaxOutputSize() {
		IPreferenceStore prefs= DebugUIPlugin.getDefault().getPreferenceStore();
		int max= prefs.getInt(CONSOLE_MAX_OUTPUT_SIZE);
		return max;
	}	
	
	/**
	 * Initialize the default values of the preferences associated with this page
	 */
	public static void initDefaults(IPreferenceStore store) {
		WorkbenchChainedTextFontFieldEditor.startPropagate(store, CONSOLE_FONT);
		
		PreferenceConverter.setDefault(store, CONSOLE_SYS_OUT_RGB, new RGB(0, 0, 255));
		PreferenceConverter.setDefault(store, CONSOLE_SYS_IN_RGB, new RGB(0, 200, 125));
		PreferenceConverter.setDefault(store, CONSOLE_SYS_ERR_RGB, new RGB(255, 0, 0));
		store.setDefault(CONSOLE_MAX_OUTPUT_SIZE, 0);
	}	
}