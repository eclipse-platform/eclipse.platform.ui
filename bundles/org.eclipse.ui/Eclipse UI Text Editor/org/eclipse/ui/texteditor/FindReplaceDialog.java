package org.eclipse.ui.texteditor;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.text.MessageFormat;import java.util.ArrayList;import java.util.List;import java.util.ResourceBundle;import org.eclipse.core.runtime.Platform;import org.eclipse.swt.SWT;import org.eclipse.swt.events.ModifyEvent;import org.eclipse.swt.events.ModifyListener;import org.eclipse.swt.events.SelectionAdapter;import org.eclipse.swt.events.SelectionEvent;import org.eclipse.swt.events.SelectionListener;import org.eclipse.swt.events.ShellAdapter;import org.eclipse.swt.events.ShellEvent;import org.eclipse.swt.graphics.Image;import org.eclipse.swt.graphics.Point;import org.eclipse.swt.graphics.Rectangle;import org.eclipse.swt.layout.GridData;import org.eclipse.swt.layout.GridLayout;import org.eclipse.swt.widgets.Button;import org.eclipse.swt.widgets.Combo;import org.eclipse.swt.widgets.Composite;import org.eclipse.swt.widgets.Control;import org.eclipse.swt.widgets.Group;import org.eclipse.swt.widgets.Label;import org.eclipse.swt.widgets.Shell;import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.dialogs.Dialog;import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.text.IFindReplaceTarget;import org.eclipse.jface.text.IFindReplaceTargetExtension;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.ui.IEditorActionBarContributor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;import org.eclipse.ui.help.WorkbenchHelp;import org.eclipse.ui.part.EditorActionBarContributor;
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
			
			String oldText= fFindField.getText(); // XXX workaround for 10766
			List oldList= new ArrayList();
			oldList.addAll(fFindHistory);

			readConfiguration();
			updateCombo(fFindField, fFindHistory);

			fFindField.removeModifyListener(fFindModifyListener);
			if (!fFindHistory.equals(oldList) && !fFindHistory.isEmpty())
				fFindField.setText((String) fFindHistory.get(0));
			else 
				fFindField.setText(oldText);
			fFindField.addModifyListener(fFindModifyListener);

			fActiveShell= (Shell) e.widget;
			updateButtonState();
			if (getShell() == fActiveShell && !fFindField.isDisposed())
				fFindField.setFocus();
		}
		
		public void shellDeactivated(ShellEvent e) {
			storeSettings();

			fGlobalRadioButton.setSelection(true);
			fSelectedRangeRadioButton.setSelection(false);

			if (fTarget != null && (fTarget instanceof IFindReplaceTargetExtension))
				((IFindReplaceTargetExtension) fTarget).setScope(null);
			
			fOldScope= null;

			fActiveShell= null;			
			updateButtonState();
		}
	}

	private class FindModifyListener implements ModifyListener {
		public void modifyText(ModifyEvent e) {
			if (isIncrementalSearch()) {
				if (fFindField.getText().equals("") && fTarget != null) { //$NON-NLS-1$
					// empty selection at base location
					int offset= isForwardSearch()
						? fIncrementalBaseLocation.x + fIncrementalBaseLocation.y
						: fIncrementalBaseLocation.x;
					
					fTarget.findAndSelect(offset, "", isForwardSearch(), isCaseSensitiveSearch(), isWholeWordSearch()); //$NON-NLS-1$
				} else {
					performSearch();
				}
			}
			
			updateButtonState();
		}
	}

	private static final int HISTORY_SIZE= 5;

	private Point fLocation;
	private Point fIncrementalBaseLocation;
	private boolean fWrapInit, fCaseInit, fWholeWordInit, fForwardInit, fGlobalInit, fIncrementalInit;
	private List fFindHistory;
	private List fReplaceHistory;

	private IFindReplaceTarget fTarget;
	private Shell fParentShell;
	private Shell fActiveShell;

	private ActivationListener fActivationListener= new ActivationListener();
	private ModifyListener fFindModifyListener= new FindModifyListener();

	private Label fReplaceLabel, fStatusLabel;
	private Button fForwardRadioButton, fGlobalRadioButton, fSelectedRangeRadioButton;
	private Button fCaseCheckBox, fWrapCheckBox, fWholeWordCheckBox, fIncrementalCheckBox;
	private Button fReplaceSelectionButton, fReplaceFindButton, fFindNextButton, fReplaceAllButton;
	private Combo fFindField, fReplaceField;
	private Rectangle fDialogPositionInit;
	private IRegion fOldScope;

	private IDialogSettings fDialogSettings;

	/**
	 * Default constructor.
	 */
	public FindReplaceDialog(Shell parentShell) {
		super(parentShell);
		
		fParentShell= null;
		fTarget= null;

		fDialogPositionInit= null;
		fFindHistory= new ArrayList(HISTORY_SIZE - 1);
		fReplaceHistory= new ArrayList(HISTORY_SIZE - 1);

		fWrapInit= false;
		fCaseInit= false;
		fWholeWordInit= false;
		fIncrementalInit= false;
		fGlobalInit= true;
		fForwardInit= true;

		readConfiguration();
		
		setShellStyle(SWT.CLOSE | SWT.MODELESS | SWT.BORDER | SWT.TITLE);
		setBlockOnOpen(false);
	}
	
	/**
	 * Returns this dialogs parent shell.
	 */
	public Shell getParentShell() {
		return super.getParentShell();
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
	
	/*
	 * @see Window#create
	 */
	public void create() {
		
		super.create();
		
		Shell shell= getShell();		
		shell.addShellListener(fActivationListener);
		if (fLocation != null)
			shell.setLocation(fLocation);
		
		// set help context
		WorkbenchHelp.setHelp(shell, IAbstractTextEditorHelpContextIds.FIND_REPLACE_DIALOG);

		// fill in combo contents
		updateCombo(fFindField, fFindHistory);
		updateCombo(fReplaceField, fReplaceHistory);

		// get find string
		initFindStringFromSelection();
		
		// set dialog position
		if (fDialogPositionInit != null)
			shell.setBounds(fDialogPositionInit);
		
		shell.setText(EditorMessages.getString("FindReplace.title")); //$NON-NLS-1$
		// shell.setImage(null);
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
		
		fFindNextButton= makeButton(panel, "FindReplace.FindNextButton.label", 102, true, new SelectionAdapter() { //$NON-NLS-1$
			public void widgetSelected(SelectionEvent e) {
				if (isIncrementalSearch())
					initIncrementalBaseLocation();

				performSearch();
				updateFindHistory();
				fFindNextButton.setFocus();
			}
		});
		setGridData(fFindNextButton, GridData.FILL, true, GridData.FILL, false);
				
		fReplaceFindButton= makeButton(panel, "FindReplace.ReplaceFindButton.label", 103, false, new SelectionAdapter() { //$NON-NLS-1$
			public void widgetSelected(SelectionEvent e) {
				performReplaceSelection();
				performSearch();
				updateFindAndReplaceHistory();
				fReplaceFindButton.setFocus();
			}
		});
		setGridData(fReplaceFindButton, GridData.FILL, true, GridData.FILL, false);
				
		fReplaceSelectionButton= makeButton(panel, "FindReplace.ReplaceSelectionButton.label", 104, false, new SelectionAdapter() { //$NON-NLS-1$
			public void widgetSelected(SelectionEvent e) {
				performReplaceSelection();
				updateFindAndReplaceHistory();
				fFindNextButton.setFocus();
			}
		});
		setGridData(fReplaceSelectionButton, GridData.FILL, true, GridData.FILL, false);
		
		fReplaceAllButton= makeButton(panel, "FindReplace.ReplaceAllButton.label", 105, false, new SelectionAdapter() { //$NON-NLS-1$
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
		setGridData(directionGroup, GridData.FILL, true, GridData.FILL, false);
		Composite scopeGroup= createScopeGroup(panel);
		setGridData(scopeGroup, GridData.FILL, true, GridData.FILL, false);

		Composite optionsGroup= createOptionsGroup(panel);
		setGridData(optionsGroup, GridData.FILL, true, GridData.FILL, false);
		GridData data= (GridData) optionsGroup.getLayoutData();
		data.horizontalSpan= 2;
		optionsGroup.setLayoutData(data);

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
		layout.marginWidth= 0;
		layout.marginHeight= 0;
		panel.setLayout(layout);

		Group group= new Group(panel, SWT.SHADOW_ETCHED_IN);
		group.setText(EditorMessages.getString("FindReplace.Direction")); //$NON-NLS-1$
		GridLayout groupLayout= new GridLayout();
		group.setLayout(groupLayout);
		group.setLayoutData(new GridData(GridData.FILL_BOTH));

		SelectionListener selectionListener= new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				if (isIncrementalSearch())
					initIncrementalBaseLocation();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		};

		fForwardRadioButton= new Button(group, SWT.RADIO | SWT.LEFT);
		fForwardRadioButton.setText(EditorMessages.getString("FindReplace.ForwardRadioButton.label")); //$NON-NLS-1$
		setGridData(fForwardRadioButton, GridData.BEGINNING, false, GridData.CENTER, false);
		fForwardRadioButton.addSelectionListener(selectionListener);

		Button backwardRadioButton= new Button(group, SWT.RADIO | SWT.LEFT);
		backwardRadioButton.setText(EditorMessages.getString("FindReplace.BackwardRadioButton.label")); //$NON-NLS-1$
		setGridData(backwardRadioButton, GridData.BEGINNING, false, GridData.CENTER, false);
		backwardRadioButton.addSelectionListener(selectionListener);

		backwardRadioButton.setSelection(!fForwardInit);
		fForwardRadioButton.setSelection(fForwardInit);

		return panel;
	}

	/**
	 * Creates the scope defining part of the find replace dialog.
	 *
	 * @param parent the parent composite
	 * @return the scope defining part
	 */
	private Composite createScopeGroup(Composite parent) {

		Composite panel= new Composite(parent, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.marginWidth= 0;
		layout.marginHeight= 0;
		panel.setLayout(layout);		

		Group group= new Group(panel, SWT.SHADOW_ETCHED_IN);
		group.setText(EditorMessages.getString("FindReplace.Scope")); //$NON-NLS-1$
		GridLayout groupLayout= new GridLayout();
		group.setLayout(groupLayout);
		group.setLayoutData(new GridData(GridData.FILL_BOTH));

		fGlobalRadioButton= new Button(group, SWT.RADIO | SWT.LEFT);
		fGlobalRadioButton.setText(EditorMessages.getString("FindReplace.GlobalRadioButton.label")); //$NON-NLS-1$
		setGridData(fGlobalRadioButton, GridData.BEGINNING, false, GridData.CENTER, false);
		fGlobalRadioButton.setSelection(fGlobalInit);
		fGlobalRadioButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				if (!fGlobalRadioButton.getSelection())
					return;
				
				useSelectedLines(false);
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		fSelectedRangeRadioButton= new Button(group, SWT.RADIO | SWT.LEFT);
		fSelectedRangeRadioButton.setText(EditorMessages.getString("FindReplace.SelectedRangeRadioButton.label")); //$NON-NLS-1$
		setGridData(fSelectedRangeRadioButton, GridData.BEGINNING, false, GridData.CENTER, false);
		fSelectedRangeRadioButton.setSelection(!fGlobalInit);
		fSelectedRangeRadioButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				if (!fSelectedRangeRadioButton.getSelection())
					return;

				useSelectedLines(true);
			}
			
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		return panel;
	}

	private void useSelectedLines(boolean selectedLines) {
		if (isIncrementalSearch())
			initIncrementalBaseLocation();

		if (fTarget == null || !(fTarget instanceof IFindReplaceTargetExtension))
			return;

		IFindReplaceTargetExtension extensionTarget= (IFindReplaceTargetExtension) fTarget;

		if (selectedLines) {

			IRegion scope;
			if (fOldScope == null) {
				Point lineSelection= extensionTarget.getLineSelection();
				scope= new Region(lineSelection.x, lineSelection.y);
			} else {
				scope= fOldScope;
				fOldScope= null;
			}

			int offset= isForwardSearch()
				? scope.getOffset() 
				: scope.getOffset() + scope.getLength();					

			extensionTarget.setSelection(offset, 0);					
			extensionTarget.setScope(scope);
		} else {
			fOldScope= extensionTarget.getScope();
			extensionTarget.setScope(null);			
		}
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
		findLabel.setText(EditorMessages.getString("FindReplace.Find.label")); //$NON-NLS-1$
		setGridData(findLabel, GridData.BEGINNING, false, GridData.CENTER, false);

		fFindField= new Combo(panel, SWT.DROP_DOWN | SWT.BORDER);
		setGridData(fFindField, GridData.FILL, true, GridData.CENTER, false);
		fFindField.addModifyListener(fFindModifyListener);

		fReplaceLabel= new Label(panel, SWT.LEFT);
		fReplaceLabel.setText(EditorMessages.getString("FindReplace.Replace.label")); //$NON-NLS-1$
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
		layout.marginWidth= 0;
		layout.marginHeight= 0;
		panel.setLayout(layout);

		Group group= new Group(panel, SWT.SHADOW_NONE);
		group.setText(EditorMessages.getString("FindReplace.Options")); //$NON-NLS-1$
		GridLayout groupLayout= new GridLayout();
		groupLayout.numColumns= 2;
		groupLayout.makeColumnsEqualWidth= true;		
		group.setLayout(groupLayout);
		group.setLayoutData(new GridData(GridData.FILL_BOTH));

		SelectionListener selectionListener= new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				storeSettings();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		};

		fCaseCheckBox= new Button(group, SWT.CHECK | SWT.LEFT);
		fCaseCheckBox.setText(EditorMessages.getString("FindReplace.CaseCheckBox.label")); //$NON-NLS-1$
		setGridData(fCaseCheckBox, GridData.BEGINNING, false, GridData.CENTER, false);
		fCaseCheckBox.setSelection(fCaseInit);
		fCaseCheckBox.addSelectionListener(selectionListener);

		fWrapCheckBox= new Button(group, SWT.CHECK | SWT.LEFT);
		fWrapCheckBox.setText(EditorMessages.getString("FindReplace.WrapCheckBox.label")); //$NON-NLS-1$
		setGridData(fWrapCheckBox, GridData.BEGINNING, false, GridData.CENTER, false);
		fWrapCheckBox.setSelection(fWrapInit);
		fWrapCheckBox.addSelectionListener(selectionListener);

		fWholeWordCheckBox= new Button(group, SWT.CHECK | SWT.LEFT);
		fWholeWordCheckBox.setText(EditorMessages.getString("FindReplace.WholeWordCheckBox.label")); //$NON-NLS-1$
		setGridData(fWholeWordCheckBox, GridData.BEGINNING, false, GridData.CENTER, false);
		fWholeWordCheckBox.setSelection(fWholeWordInit);
		fWholeWordCheckBox.addSelectionListener(selectionListener);

		fIncrementalCheckBox= new Button(group, SWT.CHECK | SWT.LEFT);
		fIncrementalCheckBox.setText(EditorMessages.getString("FindReplace.IncrementalCheckBox.label")); //$NON-NLS-1$
		setGridData(fIncrementalCheckBox, GridData.BEGINNING, false, GridData.CENTER, false);
		fIncrementalCheckBox.setSelection(fIncrementalInit);
		fIncrementalCheckBox.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				if (isIncrementalSearch())
					initIncrementalBaseLocation();
					
				storeSettings();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

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
		layout.marginWidth= 0;
		layout.marginHeight= 0;		
		panel.setLayout(layout);

		fStatusLabel= new Label(panel, SWT.LEFT);
		setGridData(fStatusLabel, GridData.FILL, true, GridData.CENTER, false);

		String label= EditorMessages.getString("FindReplace.CloseButton.label"); //$NON-NLS-1$
		Button closeButton= createButton(panel, 101, label, false);
		setGridData(closeButton, GridData.END, false, GridData.END, false);

		return panel;
	}

	/*
	 * @see Dialog#buttonPressed
	 */
	protected void buttonPressed(int buttonID) {
		if (buttonID == 101)
			close();
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
	private boolean findNext(String findString, boolean forwardSearch, boolean caseSensitive, boolean wrapSearch, boolean wholeWord, boolean incremental, boolean global) {

		Point r= fTarget.getSelection();
		int findReplacePosition= r.x;
		if (forwardSearch)
			findReplacePosition += r.y;

		if (incremental)
			findReplacePosition= forwardSearch
				? fIncrementalBaseLocation.x + fIncrementalBaseLocation.y
				: fIncrementalBaseLocation.x;

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
		return ""; //$NON-NLS-1$
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
		return ""; //$NON-NLS-1$
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
	
	/*
	 * @see Window#close()
	 */
	public boolean close() {
		handleDialogClose();
		return super.close();
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
		storeSettings();

		if (fTarget != null && fTarget instanceof IFindReplaceTargetExtension)
			((IFindReplaceTargetExtension) fTarget).endSession();

		// prevent leaks
		fActiveShell= null;
		fTarget= null;		
	}
	
	private void storeSettings() {
		fDialogPositionInit= getDialogBoundaries();
		fWrapInit= isWrapSearch();
		fWholeWordInit= isWholeWordSearch();
		fCaseInit= isCaseSensitiveSearch();
		fIncrementalInit= isIncrementalSearch();
		fForwardInit= isForwardSearch();

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
			fFindField.removeModifyListener(fFindModifyListener);
			if (selection != null) {
				fFindField.setText(selection);
				if (!selection.equals(fTarget.getSelectionText())) {
					useSelectedLines(true);
					fGlobalRadioButton.setSelection(false);
					fSelectedRangeRadioButton.setSelection(true);
				}
			} else {
				if ("".equals(fFindField.getText())) { //$NON-NLS-1$
					if (fFindHistory.size() > 0)
						fFindField.setText((String) fFindHistory.get(0));
					else
						fFindField.setText(""); //$NON-NLS-1$				
				}
			}
			fFindField.addModifyListener(fFindModifyListener);
		}
	}

	private void initIncrementalBaseLocation() {
		if (fTarget != null && isIncrementalSearch()) {
			fIncrementalBaseLocation= fTarget.getSelection();
		} else {
			fIncrementalBaseLocation= new Point(0, 0);	
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
	 * Retrieves and returns the option global scope from
	 * the appropriate check box.
	 */
	private boolean isGlobalSearch() {
		if (okToUse(fGlobalRadioButton)) {
			return fGlobalRadioButton.getSelection();
		}
		return fGlobalInit;
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
	 * Retrieves and returns the option incremental search from
	 * the appropriate check box.
	 */
	private boolean isIncrementalSearch() {
		if (okToUse(fIncrementalCheckBox)) {
			return fIncrementalCheckBox.getSelection();
		}
		return fIncrementalInit;
	}

	/**
	 * Creates a button.
	 */
	private Button makeButton(Composite parent, String key, int id, boolean dfltButton, SelectionListener listener) {
		String label= EditorMessages.getString(key);
		Button b= createButton(parent, id, label, dfltButton);
		b.addSelectionListener(listener);
		return b;
	}

	private IStatusLineManager getStatusLineManager() {
		AbstractUIPlugin plugin= (AbstractUIPlugin) Platform.getPlugin(PlatformUI.PLUGIN_ID);
		IWorkbenchWindow window= plugin.getWorkbench().getActiveWorkbenchWindow();
		if (window == null)
			return null;

		IWorkbenchPage page= window.getActivePage();
		if (page == null)
			return null;
			
		IEditorPart editor= page.getActiveEditor();
		if (editor == null)
			return null;
			
		IEditorActionBarContributor contributor= editor.getEditorSite().getActionBarContributor();
		if (contributor instanceof EditorActionBarContributor) {
			return ((EditorActionBarContributor) contributor).getActionBars().getStatusLineManager();
		}
		return null;
	}

	private void statusError(String message) {
		fStatusLabel.setText(message);

		IStatusLineManager manager= getStatusLineManager();
		if (manager == null)				
			return;

		manager.setErrorMessage(message);
		manager.setMessage(""); //$NON-NLS-1$		

		getShell().getDisplay().beep();
	}

	private void statusMessage(String message) {
		fStatusLabel.setText(message);

		IStatusLineManager manager= getStatusLineManager();
		if (manager == null)				
			return;
			
		manager.setErrorMessage(""); //$NON-NLS-1$
		manager.setMessage(message);
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
			replaceString= ""; //$NON-NLS-1$

		if (findString != null && findString.length() > 0) {

			replaceCount= replaceAll(findString, replaceString, isForwardSearch(), isCaseSensitiveSearch(), isWrapSearch(), isWholeWordSearch(), isGlobalSearch());

			if (replaceCount != 0) {
				if (replaceCount == 1) { // not plural
					statusMessage(EditorMessages.getString("FindReplace.Status.replacement.label")); //$NON-NLS-1$
				} else {
					String msg= EditorMessages.getString("FindReplace.Status.replacements.label"); //$NON-NLS-1$
					msg= MessageFormat.format(msg, new Object[] {String.valueOf(replaceCount)});
					statusMessage(msg);
				}
			} else {
				statusError(EditorMessages.getString("FindReplace.Status.noMatch.label")); //$NON-NLS-1$
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
			replaceString= ""; //$NON-NLS-1$

		fTarget.replaceSelection(replaceString);
		updateButtonState();
	}

	/**
	 * Locates the user's findString in the text of the target.
	 */
	private void performSearch() {

		String findString= getFindString();

		if (findString != null && findString.length() > 0) {

			boolean somethingFound= findNext(findString, isForwardSearch(), isCaseSensitiveSearch(), isWrapSearch(), isWholeWordSearch(), isIncrementalSearch(), isGlobalSearch());

			if (somethingFound) {
				statusMessage(""); //$NON-NLS-1$
			} else {
				statusError(EditorMessages.getString("FindReplace.Status.noMatch.label")); //$NON-NLS-1$
			}
		}

		updateButtonState();
	}
	
	/**
	 * Replaces all occurrences of the user's findString with
	 * the replace string.  Returns the number of replacements
	 * that occur.
	 */
	private int replaceAll(String findString, String replaceString, boolean forwardSearch, boolean caseSensitive, boolean wrapSearch, boolean wholeWord, boolean global) {

		int replaceCount= 0;
		int findReplacePosition= 0;

		if (wrapSearch) { // search the whole text
			findReplacePosition= 0;
			forwardSearch= true;
		} else if (fTarget.getSelectionText() != null) {
			// the cursor is set to the end or beginning of the selected text
			Point selection= fTarget.getSelection();
			if (forwardSearch)
				findReplacePosition= selection.x;
			else
				findReplacePosition= selection.x + selection.y;
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
			fFindField.removeModifyListener(fFindModifyListener);
			updateHistory(fFindField, fFindHistory);
			fFindField.addModifyListener(fFindModifyListener);
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
	 * Returns whether the target is editable
	 */
	private boolean isEditable() {
		return fTarget == null ? false : fTarget.isEditable();
	}
	
	/**
	 * Updates this dialog because of a different target.
	 *
	 * @return target the new target
	 */
	public void updateTarget(IFindReplaceTarget target) {
		if (target != fTarget) {
			if (fTarget != null && fTarget instanceof IFindReplaceTargetExtension)
				((IFindReplaceTargetExtension) fTarget).endSession();

			fTarget= target;
	
			if (fTarget != null && fTarget instanceof IFindReplaceTargetExtension) {
				((IFindReplaceTargetExtension) fTarget).beginSession();

				fGlobalInit= true;
				fGlobalRadioButton.setSelection(fGlobalInit);
				fSelectedRangeRadioButton.setSelection(!fGlobalInit);
			}
		}

		if (okToUse(fReplaceLabel)) {
			fReplaceLabel.setEnabled(isEditable());
			fReplaceField.setEnabled(isEditable());
			initFindStringFromSelection();
			initIncrementalBaseLocation();
			updateButtonState();
		}
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
	
	
	//--------------- configuration handling --------------
	
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
	 * Initializes itself from the dialog settings with the same state
	 * as at the previous invocation.
	 */
	private void readConfiguration() {
		IDialogSettings s= getDialogSettings();

		try {
			int x= s.getInt("x"); //$NON-NLS-1$
			int y= s.getInt("y"); //$NON-NLS-1$
			fLocation= new Point(x, y);
		} catch (NumberFormatException e) {
			fLocation= null;
		}
			
		fWrapInit= s.getBoolean("wrap"); //$NON-NLS-1$
		fCaseInit= s.getBoolean("casesensitive"); //$NON-NLS-1$
		fWholeWordInit= s.getBoolean("wholeword"); //$NON-NLS-1$
		fIncrementalInit= s.getBoolean("incremental"); //$NON-NLS-1$
		
		String[] findHistory= s.getArray("findhistory"); //$NON-NLS-1$
		if (findHistory != null) {
			List history= getFindHistory();
			history.clear();
			for (int i= 0; i < findHistory.length; i++)
				history.add(findHistory[i]);
		}		
		
		String[] replaceHistory= s.getArray("replacehistory"); //$NON-NLS-1$
		if (replaceHistory != null) {
			List history= getReplaceHistory();
			history.clear();
			for (int i= 0; i < replaceHistory.length; i++)
				history.add(replaceHistory[i]);
		}
	}
	
	/**
	 * Stores it current configuration in the dialog store.
	 */
	private void writeConfiguration() {
		IDialogSettings s= getDialogSettings();

		Point location= getShell().getLocation();
		s.put("x", location.x); //$NON-NLS-1$
		s.put("y", location.y); //$NON-NLS-1$
		
		s.put("wrap", fWrapInit); //$NON-NLS-1$
		s.put("casesensitive", fCaseInit); //$NON-NLS-1$
		s.put("wholeword", fWholeWordInit); //$NON-NLS-1$
		s.put("incremental", fIncrementalInit); //$NON-NLS-1$
		
		List history= getFindHistory();
		while (history.size() > 8)
			history.remove(8);
		String[] names= new String[history.size()];
		history.toArray(names);
		s.put("findhistory", names); //$NON-NLS-1$
		
		history= getReplaceHistory();
		while (history.size() > 8)
			history.remove(8);
		names= new String[history.size()];
		history.toArray(names);
		s.put("replacehistory", names); //$NON-NLS-1$
	}
}
