package org.eclipse.ui.texteditor;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.Platform;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;

import org.eclipse.jface.text.IFindReplaceTarget;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.plugin.AbstractUIPlugin;



/**
 * Find/Replace dialog. The dialog is opened on a particular 
 * target but can be re-targeted.
 */
class FindReplaceDialog extends Dialog {

	/**
	 * Updates the find replace dialog on activation changes.
	 */
	class ActivationListener extends ShellAdapter {
		
		public void shellActivated(ShellEvent e) {
			fActiveShell= (Shell) e.widget;
			updateButtonState();
			if (getShell() == fActiveShell && !fFindField.isDisposed())
				fFindField.setFocus();
		}
		
		public void shellDeactivated(ShellEvent e) {
			fActiveShell= null;
			updateButtonState();
		}
	};
	
	private static final int HISTORY_SIZE= 5;

	private boolean fWrapInit, fCaseInit, fWholeWordInit, fForwardInit;
	private List fFindHistory;
	private List fReplaceHistory;
	private String fLastFindContent;

	private IFindReplaceTarget fTarget;
	private Shell fParentShell;
	private Shell fActiveShell;

	private ActivationListener fActivationListener= new ActivationListener();

	private Label fReplaceLabel, fStatusLabel;
	private Button fForwardRadioButton;
	private Button fCaseCheckBox, fWrapCheckBox, fWholeWordCheckBox;
	private Button fReplaceSelectionButton, fReplaceFindButton, fFindNextButton, fReplaceAllButton;
	private Combo fFindField, fReplaceField;
	private Rectangle fDialogPositionInit;

	private ResourceBundle fResourceBundle;
	private IDialogSettings fDialogSettings;
	
	private String fWindowTitle;
	private Image fWindowImage;
	

	/**
	 * Default constructor.
	 */
	public FindReplaceDialog(Shell parentShell, String windowTitle, Image windowImage) {
		super(parentShell);
		
		fWindowTitle= windowTitle;
		fWindowImage= windowImage;
		
		fParentShell= null;
		fTarget= null;

		fDialogPositionInit= null;
		fFindHistory= new ArrayList(HISTORY_SIZE - 1);
		fReplaceHistory= new ArrayList(HISTORY_SIZE - 1);

		fLastFindContent= "";

		fWrapInit= false;
		fCaseInit= false;
		fWholeWordInit= false;
		fForwardInit= true;

		readConfiguration();
		
		setShellStyle(SWT.CLOSE | SWT.MODELESS | SWT.BORDER | SWT.TITLE);
		setBlockOnOpen(false);
	}
	/*
	 * @see Dialog#buttonPressed
	 */
	protected void buttonPressed(int buttonID) {
		if (buttonID == 101)
			close();
	}
	/*
	 * @see Window#close()
	 */
	public boolean close() {
		handleDialogClose();
		return super.close();
	}
	/*
	 * @see Window#create
	 */
	public void create() {
		
		super.create();
		
		Shell shell= getShell();		
		shell.addShellListener(fActivationListener);
		
		// set help context
		WorkbenchHelp.setHelp(shell, new Object[] { IAbstractTextEditorHelpContextIds.FIND_REPLACE_DIALOG });

		// fill in combo contents
		updateCombo(fFindField, fFindHistory);
		updateCombo(fReplaceField, fReplaceHistory);

		// get find string
		initFindStringFromSelection();

		// set dialog position
		if (fDialogPositionInit != null)
			shell.setBounds(fDialogPositionInit);
		
		shell.setText(fWindowTitle);
		shell.setImage(fWindowImage);
	}
	/**
	 * Create the button section of the find/replace dialog
	 *
	 * @param parent the parent composite
	 * @return the button section
	 */
	private Composite createButtonSection(Composite parent) {
		
		Composite panel= new Composite(parent, SWT.NULL);
		
		GridLayout layout= new GridLayout();
		layout.numColumns= -2;
		layout.makeColumnsEqualWidth= true;
		panel.setLayout(layout);
		
		fFindNextButton= makeButton(panel, "FindNextButton", 102, true, new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				performSearch();
				updateFindHistory();
				fFindNextButton.setFocus();
			}
		});
		setGridData(fFindNextButton, GridData.FILL, true, GridData.FILL, false);
				
		fReplaceFindButton= makeButton(panel, "ReplaceFindButton", 103, false, new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				performReplaceSelection();
				performSearch();
				updateFindAndReplaceHistory();
				fReplaceFindButton.setFocus();
			}
		});
		setGridData(fReplaceFindButton, GridData.FILL, true, GridData.FILL, false);
				
		fReplaceSelectionButton= makeButton(panel, "ReplaceSelectionButton", 104, false, new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				performReplaceSelection();
				updateFindAndReplaceHistory();
				fFindNextButton.setFocus();
			}
		});
		setGridData(fReplaceSelectionButton, GridData.FILL, true, GridData.FILL, false);
		
		fReplaceAllButton= makeButton(panel, "ReplaceAllButton", 105, false, new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				performReplaceAll();
				updateFindAndReplaceHistory();
				fFindNextButton.setFocus();
			}
		});
		setGridData(fReplaceAllButton, GridData.FILL, true, GridData.FILL, false);
		
		// Make the all the buttons the same size as the Remove Selection button.
		fReplaceAllButton.setEnabled(isEditable());
		
		return panel;
	}
	/**
	 * Creates the options configuration section of the find replace dialog.
	 *
	 * @param parent the parent composite
	 * @return the options configuration section
	 */
	private Composite createConfigPanel(Composite parent) {

		Composite panel= new Composite(parent, SWT.NULL);
		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		layout.makeColumnsEqualWidth= true;
		panel.setLayout(layout);

		Composite directionGroup= createDirectionGroup(panel);
		setGridData(directionGroup, GridData.FILL, true, GridData.BEGINNING, false);
		Composite optionsGroup= createOptionsGroup(panel);
		setGridData(optionsGroup, GridData.FILL, true, GridData.BEGINNING, false);

		return panel;
	}
	/*
	 * @see Window#createContents
	 */
	protected Control createContents(Composite parent) {

		Composite panel= new Composite(parent, SWT.NULL);
		GridLayout layout= new GridLayout();
		layout.numColumns= 1;
		layout.makeColumnsEqualWidth= true;
		panel.setLayout(layout);

		Composite inputPanel= createInputPanel(panel);
		setGridData(inputPanel, GridData.FILL, true, GridData.CENTER, false);

		Composite configPanel= createConfigPanel(panel);
		setGridData(configPanel, GridData.FILL, true, GridData.CENTER, true);
		
		Composite buttonPanelB= createButtonSection(panel);
		setGridData(buttonPanelB, GridData.FILL, true, GridData.CENTER, false);
		
		Composite statusBar= createStatusAndCloseButton(panel);
		setGridData(statusBar, GridData.FILL, true, GridData.CENTER, false);
		
		updateButtonState();
		
		return panel;
	}
	/**
	 * Creates the direction defining part of the options defining section
	 * of the find replace dialog.
	 *
	 * @param parent the parent composite
	 * @return the direction defining part
	 */
	private Composite createDirectionGroup(Composite parent) {

		Composite panel= new Composite(parent, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.numColumns= 1;
		panel.setLayout(layout);

		Group group= new Group(panel, SWT.SHADOW_ETCHED_IN);
		group.setText(getResourceBundle().getString("Direction"));
		GridLayout groupLayout= new GridLayout();
		group.setLayout(groupLayout);

		fForwardRadioButton= new Button(group, SWT.RADIO | SWT.LEFT);
		fForwardRadioButton.setText(getResourceBundle().getString("ForwardRadioButton.label"));
		setGridData(fForwardRadioButton, GridData.BEGINNING, false, GridData.CENTER, false);

		Button backwardRadioButton= new Button(group, SWT.RADIO | SWT.LEFT);
		backwardRadioButton.setText(getResourceBundle().getString("BackwardRadioButton.label"));
		setGridData(backwardRadioButton, GridData.BEGINNING, false, GridData.CENTER, false);

		backwardRadioButton.setSelection(!fForwardInit);
		fForwardRadioButton.setSelection(fForwardInit);

		return panel;
	}
	/**
	 * Create the panel where the user specifies the text to search
	 * for and the optional replacement text
	 *
	 * @param parent the parent composite
	 * @return the input panel
	 */
	private Composite createInputPanel(Composite parent) {

		ModifyListener listener= new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateButtonState();
			}
		};

		Composite panel= new Composite(parent, SWT.NULL);
		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		panel.setLayout(layout);

		Label findLabel= new Label(panel, SWT.LEFT);
		findLabel.setText(getResourceBundle().getString("Find.label"));
		setGridData(findLabel, GridData.BEGINNING, false, GridData.CENTER, false);

		fFindField= new Combo(panel, SWT.DROP_DOWN | SWT.BORDER);
		setGridData(fFindField, GridData.FILL, true, GridData.CENTER, false);
		fFindField.addModifyListener(listener);

		fReplaceLabel= new Label(panel, SWT.LEFT);
		fReplaceLabel.setText(getResourceBundle().getString("Replace.label"));
		setGridData(fReplaceLabel, GridData.BEGINNING, false, GridData.CENTER, false);

		fReplaceField= new Combo(panel, SWT.DROP_DOWN | SWT.BORDER);
		setGridData(fReplaceField, GridData.FILL, true, GridData.CENTER, false);
		fReplaceField.addModifyListener(listener);

		return panel;
	}
	/**
	 * Creates the functional options part of the options defining
	 * section of the find replace dialog.
	 *
	 * @param the parent composite
	 * @return the options group
	 */
	private Composite createOptionsGroup(Composite parent) {

		Composite panel= new Composite(parent, SWT.NULL);
		GridLayout layout= new GridLayout();
		layout.numColumns= 1;
		panel.setLayout(layout);

		Group group= new Group(panel, SWT.SHADOW_NONE);
		group.setText(getResourceBundle().getString("Options"));
		GridLayout groupLayout= new GridLayout();
		group.setLayout(groupLayout);

		fCaseCheckBox= new Button(group, SWT.CHECK | SWT.LEFT);
		fCaseCheckBox.setText(getResourceBundle().getString("CaseCheckBox.label"));
		setGridData(fCaseCheckBox, GridData.BEGINNING, false, GridData.CENTER, false);
		fCaseCheckBox.setSelection(fCaseInit);

		fWrapCheckBox= new Button(group, SWT.CHECK | SWT.LEFT);
		fWrapCheckBox.setText(getResourceBundle().getString("WrapCheckBox.label"));
		setGridData(fWrapCheckBox, GridData.BEGINNING, false, GridData.CENTER, false);
		fWrapCheckBox.setSelection(fWrapInit);

		fWholeWordCheckBox= new Button(group, SWT.CHECK | SWT.LEFT);
		fWholeWordCheckBox.setText(getResourceBundle().getString("WholeWordCheckBox.label"));
		setGridData(fWholeWordCheckBox, GridData.BEGINNING, false, GridData.CENTER, false);
		fWholeWordCheckBox.setSelection(fWholeWordInit);

		return panel;
	}
	/**
	 * Creates the status and close section of the dialog.
	 *
	 * @param parent the parent composite
	 * @return the status and close button
	 */
	private Composite createStatusAndCloseButton(Composite parent) {

		Composite panel= new Composite(parent, SWT.NULL);
		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		panel.setLayout(layout);

		fStatusLabel= new Label(panel, SWT.LEFT);
		setGridData(fStatusLabel, GridData.FILL, true, GridData.CENTER, false);

		String label= getResourceBundle().getString("CloseButton.label");
		Button closeButton= createButton(panel, 101, label, false);
		setGridData(closeButton, GridData.END, false, GridData.CENTER, false);

		return panel;
	}
	// ------- action invocation ---------------------------------------

	/**
	 * Returns the position of the specified search string, or -1 if the string can
	 * not be found when searching using the given options.
	 */
	private int findIndex(String findString, int startPosition, boolean forwardSearch, boolean caseSensitive, boolean wrapSearch, boolean wholeWord) {

		if (forwardSearch) {
			if (wrapSearch) {
				int index= fTarget.findAndSelect(startPosition, findString, true, caseSensitive, wholeWord);
				if (index == -1)
					index= fTarget.findAndSelect(-1, findString, true, caseSensitive, wholeWord);
				return index;
			}
			return fTarget.findAndSelect(startPosition, findString, true, caseSensitive, wholeWord);
		}

		// backward
		if (wrapSearch) {
			int index= fTarget.findAndSelect(startPosition - 1, findString, false, caseSensitive, wholeWord);
			if (index == -1) {
				index= fTarget.findAndSelect(-1, findString, false, caseSensitive, wholeWord);
			}
			return index;
		}
		return fTarget.findAndSelect(startPosition - 1, findString, false, caseSensitive, wholeWord);
	}
	/**
	 * Returns whether the specified  search string can be found using the given options.
	 */
	private boolean findNext(String findString, boolean forwardSearch, boolean caseSensitive, boolean wrapSearch, boolean wholeWord) {

		Point r= fTarget.getSelection();
		int findReplacePosition= r.x;
		if (forwardSearch)
			findReplacePosition += r.y;

		int index= findIndex(findString, findReplacePosition, forwardSearch, caseSensitive, wrapSearch, wholeWord);

		if (index != -1)
			return true;
		
		return false;
	}
	/**
	 * Returns the dialog's boundaries.
	 */
	private Rectangle getDialogBoundaries() {
		if (okToUse(getShell())) {
			return getShell().getBounds();
		} else {
			return fDialogPositionInit;
		}
	}
	/**
	 * Returns the dialog settings object used to share state 
	 * between several find/replace dialogs.
	 * 
	 * @return the dialog settings to be used
	 */
	private IDialogSettings getDialogSettings() {
		AbstractUIPlugin plugin= (AbstractUIPlugin) Platform.getPlugin(PlatformUI.PLUGIN_ID);
		IDialogSettings settings= plugin.getDialogSettings();
		fDialogSettings= settings.getSection(getClass().getName());
		if (fDialogSettings == null)
			fDialogSettings= settings.addNewSection(getClass().getName());
		return fDialogSettings;
	}
	/**
	 * Returns the dialogs history.
	 */
	private List getFindHistory() {
		return fFindHistory;
	}
	// ------- accessors ---------------------------------------

	/**
	 * Retrieves the string to search for from the appriopriate text
	 * input field and returns it. 
	 */
	private String getFindString() {
		if (okToUse(fFindField)) {
			return fFindField.getText();
		}
		return fLastFindContent;
	}
	/**
	 * Returns this dialogs parent shell.
	 */
	public Shell getParentShell() {
		return super.getParentShell();
	}
	/**
	 * Returns the dialog's replace history.
	 */
	private List getReplaceHistory() {
		return fReplaceHistory;
	}
	/**
	 * Retrieves the replacement string from the appriopriate text
	 * input field and returns it. 
	 */
	private String getReplaceString() {
		if (okToUse(fReplaceField)) {
			return fReplaceField.getText();
		}
		return "";
	}
	//--------------- configuration handling --------------
	
	/**
	 * Returns the dialog's resource bundle.
	 * 
	 * @return the dialog's resource bundle
	 */
	private ResourceBundle getResourceBundle() {
		if (fResourceBundle == null)
			fResourceBundle= ResourceBundle.getBundle("org.eclipse.ui.texteditor.FindReplaceDialogResources");
		return fResourceBundle;
	}
	// ------- init / close ---------------------------------------

	/**
	 * Returns the actual selection of the find replace target
	 */
	private String getSelectionString() {
		
		/*
		 * 1GF86V3: ITPUI:WINNT - Internal errors using Find/Replace Dialog
		 * Now uses TextUtilities rather than focussing on '\n'  
		 */
		String selection= fTarget.getSelectionText();
		if (selection != null && selection.length() > 0) {
			int[] info= TextUtilities.indexOf(TextUtilities.fgDelimiters, selection, 0);
			if (info[0] > 0)
				return selection.substring(0, info[0]);
			else if (info[0] == -1)
				return selection;
		}
		return null;
	}
	/**
	 * Removes focus changed listener from browser and stores settings for re-open.
	 */
	private void handleDialogClose() {

		// remove listeners
		if (fParentShell != null) {
			fParentShell.removeShellListener(fActivationListener);
			fParentShell= null;
		}
		
		getShell().removeShellListener(fActivationListener);
		
		// store current settings in case of re-open
		fDialogPositionInit= getDialogBoundaries();
		fLastFindContent= getFindString();
		fWrapInit= isWrapSearch();
		fWholeWordInit= isWholeWordSearch();
		fCaseInit= isCaseSensitiveSearch();
		fForwardInit= isForwardSearch();

		// prevent leaks
		fActiveShell= null;
		fTarget= null;

		writeConfiguration();
	}
	/**
	 * Initializes the string to search for and the appropriate
	 * text inout field based on the selection found in the
	 * action's target.
	 */
	private void initFindStringFromSelection() {
		if (fTarget != null && okToUse(fFindField)) {
			String selection= getSelectionString();
			if (selection != null) {
				fFindField.setText(selection);
			} else {
				if ("".equals(fFindField.getText())) {
					fFindField.setText(fLastFindContent);
				}
			}
		}
	}
	// ------- history ---------------------------------------
	
	/**
	 * Initialize the find history.
	 */
	private void initHistory(List history, List init) {
		history.clear();
		for (int i= 0; i < init.size() && i < HISTORY_SIZE - 1; i++) {
			history.add(init.get(i));
		}
	}
	/**
	 * Retrieves and returns the option case sensitivity from
	 * the appropriate check box.
	 */
	private boolean isCaseSensitiveSearch() {
		if (okToUse(fCaseCheckBox)) {
			return fCaseCheckBox.getSelection();
		}
		return fCaseInit;
	}
	/**
	 * Returns whether the target is editable
	 */
	private boolean isEditable() {
		return fTarget == null ? false : fTarget.isEditable();
	}
	/**
	 * Retrieves and returns the option search direction from
	 * the appropriate check box.
	 */
	private boolean isForwardSearch() {
		if (okToUse(fForwardRadioButton)) {
			return fForwardRadioButton.getSelection();
		}
		return fForwardInit;
	}
	/**
	 * Retrieves and returns the option search whole words from
	 * the appropriate check box.
	 */
	private boolean isWholeWordSearch() {
		if (okToUse(fWholeWordCheckBox)) {
			return fWholeWordCheckBox.getSelection();
		}
		return fWholeWordInit;
	}
	/**
	 * Retrieves and returns the option wrap search from
	 * the appropriate check box.
	 */
	private boolean isWrapSearch() {
		if (okToUse(fWrapCheckBox)) {
			return fWrapCheckBox.getSelection();
		}
		return fWrapInit;
	}
	/**
	 * Creates a button.
	 */
	private Button makeButton(Composite parent, String key, int id, boolean dfltButton, SelectionListener listener) {
		String label= getResourceBundle().getString(key + ".label");
		Button b= createButton(parent, id, label, dfltButton);
		b.addSelectionListener(listener);
		return b;
	}
	/**
	 * Returns <code>true</code> if control can be used
	 *
	 * @param control the control to be checked
	 * @return <code>true</code> if control can be used
	 */
	private boolean okToUse(Control control) {
		return control != null && !control.isDisposed();
	}
	/**
	 * Replaces all occurrences of the user's findString with
	 * the replace string.  Indicate to the user the number of replacements
	 * that occur.
	 */
	private void performReplaceAll() {

		int replaceCount= 0;
		String replaceString= getReplaceString();
		String findString= getFindString();

		if (replaceString == null)
			replaceString= "";

		if (findString != null && findString.length() > 0) {

			replaceCount= replaceAll(findString, replaceString, isForwardSearch(), isCaseSensitiveSearch(), isWrapSearch(), isWholeWordSearch());

			if (replaceCount != 0) {
				if (replaceCount == 1) { // not plural
					fStatusLabel.setText(getResourceBundle().getString("Status.replacement.label"));
				} else {
					String msg= getResourceBundle().getString("Status.replacements.label");
					msg= MessageFormat.format(msg, new Object[] {String.valueOf(replaceCount)});
					fStatusLabel.setText(msg);
				}
			} else {
				getShell().getDisplay().beep();
				fStatusLabel.setText(getResourceBundle().getString("Status.noMatch.label"));
			}
		}

		updateButtonState();
	}
	/**
	 * Replaces the current selection of the target with the user's
	 * replace string.
	 */
	private void performReplaceSelection() {

		String replaceString= getReplaceString();
		if (replaceString == null)
			replaceString= "";

		fTarget.replaceSelection(replaceString);
		updateButtonState();
	}
	/**
	 * Locates the user's findString in the text of the target.
	 */
	private void performSearch() {

		String findString= getFindString();

		if (findString != null && findString.length() > 0) {

			boolean somethingFound= findNext(findString, isForwardSearch(), isCaseSensitiveSearch(), isWrapSearch(), isWholeWordSearch());

			if (somethingFound) {
				fStatusLabel.setText("");
			} else {
				getShell().getDisplay().beep();
				fStatusLabel.setText(getResourceBundle().getString("Status.noMatch.label"));
			}
		}

		updateButtonState();
	}
	/**
	 * Initializes itself from the dialog settings with the same state
	 * as at the previous invocation.
	 */
	private void readConfiguration() {
		IDialogSettings s= getDialogSettings();
		fWrapInit= s.getBoolean("wrap");
		fCaseInit= s.getBoolean("casesensitive");
		fWholeWordInit= s.getBoolean("wholeword");
		
		String[] findHistory= s.getArray("findhistory");
		if (findHistory != null) {
			List history= getFindHistory();
			for (int i= 0; i < findHistory.length; i++)
				history.add(findHistory[i]);
		}
		
		String[] replaceHistory= s.getArray("replacehistory");
		if (replaceHistory != null) {
			List history= getReplaceHistory();
			for (int i= 0; i < replaceHistory.length; i++)
				history.add(replaceHistory[i]);
		}
	}
	/**
	 * Replaces all occurrences of the user's findString with
	 * the replace string.  Returns the number of replacements
	 * that occur.
	 */
	private int replaceAll(String findString, String replaceString, boolean forwardSearch, boolean caseSensitive, boolean wrapSearch, boolean wholeWord) {

		int replaceCount= 0;
		int findReplacePosition= 0;

		if (wrapSearch) { // search the whole text
			findReplacePosition= 0;
			forwardSearch= true;
		} else if (forwardSearch) {
			if (fTarget.getSelectionText() != null) { // the cursor is always set to the end of selected text
				findReplacePosition= fTarget.getSelection().x;
			}
		}

		int length= findString.length();
		int index= 0;
		while (index != -1) {
			index= fTarget.findAndSelect(findReplacePosition, findString, forwardSearch, caseSensitive, wholeWord);
			if (index != -1) { // substring not contained from current position
				findReplacePosition= index + replaceString.length();
				fTarget.replaceSelection(replaceString);
				replaceCount++;
			}
		}

		return replaceCount;
	}
	// ------- ui creation ---------------------------------------
	
	/**
	 * Attaches the given layout specification to the <code>component</code>
	 */
	private void setGridData(Control component, int horizontalAlignment, boolean grabExcessHorizontalSpace, int verticalAlignment, boolean grabExcessVerticalSpace) {
		GridData gd= new GridData();
		gd.horizontalAlignment= horizontalAlignment;
		gd.grabExcessHorizontalSpace= grabExcessHorizontalSpace;
		gd.verticalAlignment= verticalAlignment;
		gd.grabExcessVerticalSpace= grabExcessVerticalSpace;
		component.setLayoutData(gd);
	}
	/** 
	 * Sets the parent shell of this dialog to be the given shell.
	 *
	 * @param shell the new parent shell
	 */
	public void setParentShell(Shell shell) {
		if (shell != fParentShell) {
			
			if (fParentShell != null)
				fParentShell.removeShellListener(fActivationListener);
							
			fParentShell= shell;
			fParentShell.addShellListener(fActivationListener);
		}
		
		fActiveShell= shell;
	}
	/** 
	 * Updates the enabled state of the buttons.
	 */
	private void updateButtonState() {
		if (okToUse(getShell()) && okToUse(fFindNextButton)) {
			String selectedText= null;
			if (fTarget != null) {
				selectedText= fTarget.getSelectionText();
			}

			boolean selection= (selectedText != null && selectedText.length() > 0);

			boolean enable= fTarget != null && (fActiveShell == fParentShell || fActiveShell == getShell());
			String str= getFindString();
			boolean findString= (str != null && str.length() > 0);

			fFindNextButton.setEnabled(enable && findString);
			fReplaceSelectionButton.setEnabled(enable && isEditable() && selection);
			fReplaceFindButton.setEnabled(enable && isEditable() && findString && selection);
			fReplaceAllButton.setEnabled(enable && isEditable() && findString);
		}
	}
	/**
	 * Updates the given combo with the given content.
	 */
	private void updateCombo(Combo combo, List content) {
		combo.removeAll();
		for (int i= 0; i < content.size(); i++) {
			combo.add(content.get(i).toString());
		}
	}
	// ------- open / reopen ---------------------------------------
	
	/**
	 * Called after executed find/replace action to update the history
	 */
	private void updateFindAndReplaceHistory() {
		updateFindHistory();
		if (okToUse(fReplaceField)) {
			updateHistory(fReplaceField, fReplaceHistory);
		}

	}
	/**
	 * Called after executed find action to update the history
	 */
	private void updateFindHistory() {
		if (okToUse(fFindField)) {
			updateHistory(fFindField, fFindHistory);
		}
	}
	/**
	 * Updates the combo with the history.
	 */
	private void updateHistory(Combo combo, List history) {
		String findString= combo.getText();
		int index= history.indexOf(findString);
		if (index != 0) {
			if (index != -1) {
				history.remove(index);
			}
			history.add(0, findString);
			updateCombo(combo, history);
			combo.setText(findString);
		}
	}
	/**
	 * Updates this dialog because of a different target.
	 *
	 * @return target the new target
	 */
	public void updateTarget(IFindReplaceTarget target) {
		fTarget= target;
		if (okToUse(fReplaceLabel)) {
			fReplaceLabel.setEnabled(isEditable());
			fReplaceField.setEnabled(isEditable());
			initFindStringFromSelection();
			updateButtonState();
		}
	}
	/**
	 * Stores it current configuration in the dialog store.
	 */
	private void writeConfiguration() {
		IDialogSettings s= getDialogSettings();
		s.put("wrap", fWrapInit);
		s.put("casesensitive", fCaseInit);
		s.put("wholeword", fWholeWordInit);
		
		List history= getFindHistory();
		String[] names= new String[history.size()];
		history.toArray(names);
		s.put("findhistory", names);
		
		history= getReplaceHistory();
		names= new String[history.size()];
		history.toArray(names);
		s.put("replacehistory", names);
	}
}
