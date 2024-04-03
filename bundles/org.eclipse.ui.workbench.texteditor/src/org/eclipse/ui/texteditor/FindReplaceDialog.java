/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     SAP SE, christian.georgi@sap.com - Bug 487357: Make find dialog content scrollable
 *     Pierre-Yves B., pyvesdev@gmail.com - Bug 121634: [find/replace] status bar must show the string being searched when "String Not Found"
 *******************************************************************************/
package org.eclipse.ui.texteditor;

import java.util.HashMap;

import org.osgi.framework.FrameworkUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.action.LegacyActionTools;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.fieldassist.ComboContentAdapter;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.jface.util.Util;

import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.FindReplaceDocumentAdapterContentProposalProvider;
import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.IFindReplaceTargetExtension;
import org.eclipse.jface.text.IFindReplaceTargetExtension3;
import org.eclipse.jface.text.IFindReplaceTargetExtension4;
import org.eclipse.jface.text.TextUtilities;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.fieldassist.ContentAssistCommandAdapter;
import org.eclipse.ui.internal.findandreplace.FindReplaceLogic;
import org.eclipse.ui.internal.findandreplace.FindReplaceLogicMessageGenerator;
import org.eclipse.ui.internal.findandreplace.FindReplaceMessages;
import org.eclipse.ui.internal.findandreplace.HistoryStore;
import org.eclipse.ui.internal.findandreplace.IFindReplaceLogic;
import org.eclipse.ui.internal.findandreplace.SearchOptions;
import org.eclipse.ui.internal.findandreplace.status.IFindReplaceStatus;
import org.eclipse.ui.internal.texteditor.SWTUtil;

/**
 * Find/Replace dialog. The dialog is opened on a particular target but can be
 * re-targeted. Internally used by the <code>FindReplaceAction</code>
 */
class FindReplaceDialog extends Dialog {

	private static final int CLOSE_BUTTON_ID = 101;
	private IFindReplaceLogic findReplaceLogic;

	/**
	 * Updates the find replace dialog on activation changes.
	 */
	class ActivationListener extends ShellAdapter {
		@Override
		public void shellActivated(ShellEvent e) {
			fActiveShell = (Shell) e.widget;
			updateButtonState();

			if (fGiveFocusToFindField && getShell() == fActiveShell && okToUse(fFindField))
				fFindField.setFocus();

		}

		@Override
		public void shellDeactivated(ShellEvent e) {
			fGiveFocusToFindField = false;

			storeSettings();

			fGlobalRadioButton.setSelection(true);
			fSelectedRangeRadioButton.setSelection(false);
			findReplaceLogic.activate(SearchOptions.GLOBAL);
			IFindReplaceTarget target = findReplaceLogic.getTarget();

			if (target != null && (target instanceof IFindReplaceTargetExtension)) {
				((IFindReplaceTargetExtension) target).setScope(null);
			}

			fActiveShell = null;
			updateButtonState();
		}

	}

	private final FindModifyListener fFindModifyListener = new FindModifyListener();

	/**
	 * Modify listener to update the search result in case of incremental search.
	 *
	 * @since 2.0
	 */
	private class FindModifyListener implements ModifyListener {

		// XXX: Workaround for Combo bug on Linux (see bug 404202 and bug 410603)
		private boolean fIgnoreNextEvent;

		private void ignoreNextEvent() {
			fIgnoreNextEvent = true;
		}

		@Override
		public void modifyText(ModifyEvent e) {

			// XXX: Workaround for Combo bug on Linux (see bug 404202 and bug 410603)
			if (fIgnoreNextEvent) {
				fIgnoreNextEvent = false;
				return;
			}

			findReplaceLogic.performIncrementalSearch(getFindString());
			evaluateFindReplaceStatus(false);

			updateButtonState(!findReplaceLogic.isActive(SearchOptions.INCREMENTAL));
		}
	}

	/** The size of the dialogs search history. */
	private static final int HISTORY_SIZE = 15;

	private HistoryStore findHistory;
	private HistoryStore replaceHistory;

	private Shell fParentShell;
	private Shell fActiveShell;

	private final ActivationListener fActivationListener = new ActivationListener();

	private Label fReplaceLabel, fStatusLabel;
	private Button fForwardRadioButton, fGlobalRadioButton, fSelectedRangeRadioButton;
	private Button fCaseCheckBox, fWrapCheckBox, fWholeWordCheckBox, fIncrementalCheckBox;

	/**
	 * Checkbox for selecting whether the search string is a regular expression.
	 *
	 * @since 3.0
	 */
	private Button fIsRegExCheckBox;

	private Button fReplaceSelectionButton, fReplaceFindButton, fFindNextButton, fReplaceAllButton, fSelectAllButton;
	private Combo fFindField, fReplaceField;

	/**
	 * Find and replace command adapters.
	 *
	 * @since 3.3
	 */
	private ContentAssistCommandAdapter fContentAssistFindField, fContentAssistReplaceField;

	private Rectangle fDialogPositionInit;

	private IDialogSettings fDialogSettings;
	/**
	 * <code>true</code> if the find field should receive focus the next time the
	 * dialog is activated, <code>false</code> otherwise.
	 *
	 * @since 3.0
	 */
	private boolean fGiveFocusToFindField = true;

	/**
	 * Holds the mnemonic/button pairs for all buttons.
	 *
	 * @since 3.7
	 */
	private HashMap<Character, Button> fMnemonicButtonMap = new HashMap<>();

	/**
	 * Creates a new dialog with the given shell as parent.
	 *
	 * @param parentShell the parent shell
	 */
	public FindReplaceDialog(Shell parentShell) {
		super(parentShell);
		findReplaceLogic = new FindReplaceLogic();
		findReplaceLogic.activate(SearchOptions.GLOBAL);
		fParentShell = null;
		fDialogPositionInit = null;

		setupSearchHistory();
		readConfiguration();

		setShellStyle(getShellStyle() ^ SWT.APPLICATION_MODAL | SWT.MODELESS);
		setBlockOnOpen(false);
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	/**
	 * Returns this dialog's parent shell.
	 *
	 * @return the dialog's parent shell
	 */
	@Override
	public Shell getParentShell() {
		return super.getParentShell();
	}

	/**
	 * Returns <code>true</code> if control can be used.
	 *
	 * @param control the control to be checked
	 * @return <code>true</code> if control can be used
	 */
	private boolean okToUse(Control control) {
		return control != null && !control.isDisposed();
	}

	@Override
	public void create() {

		super.create();

		Shell shell = getShell();
		shell.addShellListener(fActivationListener);

		// set help context
		PlatformUI.getWorkbench().getHelpSystem().setHelp(shell, IAbstractTextEditorHelpContextIds.FIND_REPLACE_DIALOG);

		// fill in combo contents
		fFindField.removeModifyListener(fFindModifyListener);
		updateCombo(fFindField, findHistory.get());
		fFindField.addModifyListener(fFindModifyListener);
		updateCombo(fReplaceField, replaceHistory.get());

		// get find string
		initFindString();

		// set dialog position
		if (fDialogPositionInit != null)
			shell.setBounds(fDialogPositionInit);

		shell.setText(FindReplaceMessages.FindReplace_Dialog_Title);

		updateButtonState();
	}

	/**
	 * Create the button section of the find/replace dialog.
	 *
	 * @param parent the parent composite
	 * @return the button section
	 */
	private Composite createButtonSection(Composite parent) {

		Composite panel = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = -2; // this is intended
		panel.setLayout(layout);

		fFindNextButton = makeButton(panel, FindReplaceMessages.FindReplace_FindNextButton_label, 102, true,
				new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						setupFindReplaceLogic();
						boolean eventRequiresInverseSearchDirection = (e.stateMask & SWT.MODIFIER_MASK) == SWT.SHIFT;
						boolean forwardSearchActivated = findReplaceLogic.isActive(SearchOptions.FORWARD);
						activateInFindReplaceLogicIf(SearchOptions.FORWARD,
								eventRequiresInverseSearchDirection != forwardSearchActivated);
						boolean somethingFound = findReplaceLogic.performSearch(getFindString());
						activateInFindReplaceLogicIf(SearchOptions.FORWARD, forwardSearchActivated);

						writeSelection();
						updateButtonState(!somethingFound);
						updateFindHistory();
						evaluateFindReplaceStatus();
					}
				});
		setGridData(fFindNextButton, SWT.FILL, true, SWT.FILL, false);

		fSelectAllButton = makeButton(panel, FindReplaceMessages.FindReplace_SelectAllButton_label, 106, false,
				new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						findReplaceLogic.performSelectAll(getFindString(), fActiveShell.getDisplay());
						writeSelection();
						updateButtonState();
						updateFindAndReplaceHistory();
						evaluateFindReplaceStatus();
					}
				});
		setGridData(fSelectAllButton, SWT.FILL, true, SWT.FILL, false);

		@SuppressWarnings("unused")
		Label filler = new Label(panel, SWT.NONE); // filler

		fReplaceFindButton = makeButton(panel, FindReplaceMessages.FindReplace_ReplaceFindButton_label, 103, false,
				new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						if (findReplaceLogic.performReplaceAndFind(getFindString(), getReplaceString())) {
							writeSelection();
						}
						updateButtonState();
						updateFindAndReplaceHistory();
						evaluateFindReplaceStatus();
					}
				});
		setGridData(fReplaceFindButton, SWT.FILL, false, SWT.FILL, false);

		fReplaceSelectionButton = makeButton(panel, FindReplaceMessages.FindReplace_ReplaceSelectionButton_label, 104,
				false, new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						if (findReplaceLogic.performSelectAndReplace(getFindString(), getReplaceString())) {
							writeSelection();
						}
						updateButtonState();
						updateFindAndReplaceHistory();
						evaluateFindReplaceStatus();
					}
				});
		setGridData(fReplaceSelectionButton, SWT.FILL, false, SWT.FILL, false);

		fReplaceAllButton = makeButton(panel, FindReplaceMessages.FindReplace_ReplaceAllButton_label, 105, false,
				new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						findReplaceLogic.performReplaceAll(getFindString(), getReplaceString(),
								fActiveShell.getDisplay());
						writeSelection();
						updateButtonState();
						updateFindAndReplaceHistory();
						evaluateFindReplaceStatus();
					}
				});
		setGridData(fReplaceAllButton, SWT.FILL, true, SWT.FILL, false);

		return panel;
	}

	/**
	 * Creates the options configuration section of the find replace dialog.
	 *
	 * @param parent the parent composite
	 * @return the options configuration section
	 */
	private Composite createConfigPanel(Composite parent) {

		Composite panel = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = true;
		panel.setLayout(layout);

		Composite directionGroup = createDirectionGroup(panel);
		setGridData(directionGroup, SWT.FILL, true, SWT.FILL, false);

		Composite scopeGroup = createScopeGroup(panel);
		setGridData(scopeGroup, SWT.FILL, true, SWT.FILL, false);

		Composite optionsGroup = createOptionsGroup(panel);
		setGridData(optionsGroup, SWT.FILL, true, SWT.FILL, true);
		((GridData) optionsGroup.getLayoutData()).horizontalSpan = 2;

		return panel;
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite panel = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.makeColumnsEqualWidth = true;
		panel.setLayout(layout);
		setGridData(panel, SWT.FILL, true, SWT.FILL, true);

		ScrolledComposite scrolled = new ScrolledComposite(panel, SWT.V_SCROLL);
		setGridData(scrolled, SWT.FILL, true, SWT.FILL, true);

		Composite mainArea = new Composite(scrolled, SWT.NONE);
		setGridData(mainArea, SWT.FILL, true, SWT.FILL, true);
		mainArea.setLayout(new GridLayout(1, true));

		Composite inputPanel = createInputPanel(mainArea);
		setGridData(inputPanel, SWT.FILL, true, SWT.TOP, false);

		Composite configPanel = createConfigPanel(mainArea);
		setGridData(configPanel, SWT.FILL, true, SWT.TOP, true);

		scrolled.setContent(mainArea);
		scrolled.setExpandHorizontal(true);
		scrolled.setExpandVertical(true);
		scrolled.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				scrolled.setMinHeight(mainArea.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
			}
		});

		Composite buttonPanelB = createButtonSection(panel);
		setGridData(buttonPanelB, SWT.RIGHT, true, SWT.BOTTOM, false);

		Composite statusBar = createStatusAndCloseButton(panel);
		setGridData(statusBar, SWT.FILL, true, SWT.BOTTOM, false);

		panel.addTraverseListener(e -> {
			if (e.detail == SWT.TRAVERSE_RETURN) {
				if (!Util.isMac()) {
					Control controlWithFocus = getShell().getDisplay().getFocusControl();
					if (controlWithFocus != null && (controlWithFocus.getStyle() & SWT.PUSH) == SWT.PUSH)
						return;
				}
				Event event1 = new Event();
				event1.type = SWT.Selection;
				event1.stateMask = e.stateMask;
				fFindNextButton.notifyListeners(SWT.Selection, event1);
				e.doit = false;
			} else if (e.detail == SWT.TRAVERSE_MNEMONIC) {
				Character mnemonic = Character.valueOf(Character.toLowerCase(e.character));
				if (fMnemonicButtonMap.containsKey(mnemonic)) {
					Button button = fMnemonicButtonMap.get(mnemonic);
					if ((fFindField.isFocusControl() || fReplaceField.isFocusControl()
							|| (button.getStyle() & SWT.PUSH) != 0) && button.isEnabled()) {
						Event event2 = new Event();
						event2.type = SWT.Selection;
						event2.stateMask = e.stateMask;
						if ((button.getStyle() & SWT.RADIO) != 0) {
							Composite buttonParent = button.getParent();
							if (buttonParent != null) {
								for (Control child : buttonParent.getChildren())
									((Button) child).setSelection(false);
							}
							button.setSelection(true);
						} else {
							button.setSelection(!button.getSelection());
						}
						button.notifyListeners(SWT.Selection, event2);
						e.detail = SWT.TRAVERSE_NONE;
						e.doit = true;
					}
				}
			}
		});

		updateButtonState();

		applyDialogFont(panel);

		return panel;
	}

	private void setContentAssistsEnablement(boolean enable) {
		fContentAssistFindField.setEnabled(enable);
		fContentAssistReplaceField.setEnabled(enable);
	}

	/**
	 * Creates the direction defining part of the options defining section of the
	 * find replace dialog.
	 *
	 * @param parent the parent composite
	 * @return the direction defining part
	 */
	private Composite createDirectionGroup(Composite parent) {

		Composite panel = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		panel.setLayout(layout);

		Group group = new Group(panel, SWT.SHADOW_ETCHED_IN);
		group.setText(FindReplaceMessages.FindReplace_Direction);
		GridLayout groupLayout = new GridLayout();
		group.setLayout(groupLayout);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		SelectionListener selectionListener = new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				activateInFindReplaceLogicIf(SearchOptions.FORWARD, fForwardRadioButton.getSelection());
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// Do nothing
			}
		};

		fForwardRadioButton = new Button(group, SWT.RADIO | SWT.LEFT);
		fForwardRadioButton.setText(FindReplaceMessages.FindReplace_ForwardRadioButton_label);
		setGridData(fForwardRadioButton, SWT.LEFT, false, SWT.CENTER, false);
		fForwardRadioButton.addSelectionListener(selectionListener);

		storeButtonWithMnemonicInMap(fForwardRadioButton);

		Button backwardRadioButton = new Button(group, SWT.RADIO | SWT.LEFT);
		backwardRadioButton.setText(FindReplaceMessages.FindReplace_BackwardRadioButton_label);
		setGridData(backwardRadioButton, SWT.LEFT, false, SWT.CENTER, false);
		backwardRadioButton.addSelectionListener(selectionListener);
		storeButtonWithMnemonicInMap(backwardRadioButton);

		activateInFindReplaceLogicIf(SearchOptions.FORWARD, true); // search forward by default
		backwardRadioButton.setSelection(!findReplaceLogic.isActive(SearchOptions.FORWARD));
		fForwardRadioButton.setSelection(findReplaceLogic.isActive(SearchOptions.FORWARD));

		return panel;
	}

	/**
	 * Creates the scope defining part of the find replace dialog.
	 *
	 * @param parent the parent composite
	 * @return the scope defining part
	 * @since 2.0
	 */
	private Composite createScopeGroup(Composite parent) {

		Composite panel = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		panel.setLayout(layout);

		Group group = new Group(panel, SWT.SHADOW_ETCHED_IN);
		group.setText(FindReplaceMessages.FindReplace_Scope);
		GridLayout groupLayout = new GridLayout();
		group.setLayout(groupLayout);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		fGlobalRadioButton = new Button(group, SWT.RADIO | SWT.LEFT);
		fGlobalRadioButton.setText(FindReplaceMessages.FindReplace_GlobalRadioButton_label);
		setGridData(fGlobalRadioButton, SWT.LEFT, false, SWT.CENTER, false);
		fGlobalRadioButton.setSelection(findReplaceLogic.isActive(SearchOptions.GLOBAL));
		fGlobalRadioButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!fGlobalRadioButton.getSelection() || findReplaceLogic.isActive(SearchOptions.GLOBAL)) {
					return;
				}
				findReplaceLogic.activate(SearchOptions.GLOBAL);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		storeButtonWithMnemonicInMap(fGlobalRadioButton);

		fSelectedRangeRadioButton = new Button(group, SWT.RADIO | SWT.LEFT);
		fSelectedRangeRadioButton.setText(FindReplaceMessages.FindReplace_SelectedRangeRadioButton_label);
		setGridData(fSelectedRangeRadioButton, SWT.LEFT, false, SWT.CENTER, false);
		fSelectedRangeRadioButton.setSelection(!findReplaceLogic.isActive(SearchOptions.GLOBAL));
		fSelectedRangeRadioButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!fSelectedRangeRadioButton.getSelection() || !findReplaceLogic.isActive(SearchOptions.GLOBAL)) {
					return;
				}
				findReplaceLogic.deactivate(SearchOptions.GLOBAL);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		storeButtonWithMnemonicInMap(fSelectedRangeRadioButton);

		return panel;
	}

	/**
	 * Creates the panel where the user specifies the text to search for and the
	 * optional replacement text.
	 *
	 * @param parent the parent composite
	 * @return the input panel
	 */
	private Composite createInputPanel(Composite parent) {

		ModifyListener listener = e -> updateButtonState();

		Composite panel = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		panel.setLayout(layout);

		Label findLabel = new Label(panel, SWT.LEFT);
		findLabel.setText(FindReplaceMessages.FindReplace_Find_label);
		setGridData(findLabel, SWT.LEFT, false, SWT.CENTER, false);

		// Create the find content assist field
		ComboContentAdapter contentAdapter = new ComboContentAdapter();
		FindReplaceDocumentAdapterContentProposalProvider findProposer = new FindReplaceDocumentAdapterContentProposalProvider(
				true);
		fFindField = new Combo(panel, SWT.DROP_DOWN | SWT.BORDER);
		fContentAssistFindField = new ContentAssistCommandAdapter(fFindField, contentAdapter, findProposer,
				ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS, new char[0], true);
		setGridData(fFindField, SWT.FILL, true, SWT.CENTER, false);
		addDecorationMargin(fFindField);
		fFindField.addModifyListener(fFindModifyListener);

		fReplaceLabel = new Label(panel, SWT.LEFT);
		fReplaceLabel.setText(FindReplaceMessages.FindReplace_Replace_label);
		setGridData(fReplaceLabel, SWT.LEFT, false, SWT.CENTER, false);

		// Create the replace content assist field
		FindReplaceDocumentAdapterContentProposalProvider replaceProposer = new FindReplaceDocumentAdapterContentProposalProvider(
				false);
		fReplaceField = new Combo(panel, SWT.DROP_DOWN | SWT.BORDER);
		fContentAssistReplaceField = new ContentAssistCommandAdapter(fReplaceField, contentAdapter, replaceProposer,
				ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS, new char[0], true);
		setGridData(fReplaceField, SWT.FILL, true, SWT.CENTER, false);
		addDecorationMargin(fReplaceField);
		fReplaceField.addModifyListener(listener);

		return panel;
	}

	/**
	 * Creates the functional options part of the options defining section of the
	 * find replace dialog.
	 *
	 * @param parent the parent composite
	 * @return the options group
	 */
	private Composite createOptionsGroup(Composite parent) {

		Composite panel = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		panel.setLayout(layout);

		Group group = new Group(panel, SWT.SHADOW_NONE);
		group.setText(FindReplaceMessages.FindReplace_Options);
		GridLayout groupLayout = new GridLayout();
		groupLayout.numColumns = 2;
		groupLayout.makeColumnsEqualWidth = true;
		group.setLayout(groupLayout);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		SelectionListener selectionListener = new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setupFindReplaceLogic();
				storeSettings();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		};

		fCaseCheckBox = new Button(group, SWT.CHECK | SWT.LEFT);
		fCaseCheckBox.setText(FindReplaceMessages.FindReplace_CaseCheckBox_label);
		setGridData(fCaseCheckBox, SWT.LEFT, false, SWT.CENTER, false);
		fCaseCheckBox.setSelection(findReplaceLogic.isActive(SearchOptions.CASE_SENSITIVE));
		fCaseCheckBox.addSelectionListener(selectionListener);
		storeButtonWithMnemonicInMap(fCaseCheckBox);

		fWrapCheckBox = new Button(group, SWT.CHECK | SWT.LEFT);
		fWrapCheckBox.setText(FindReplaceMessages.FindReplace_WrapCheckBox_label);
		setGridData(fWrapCheckBox, SWT.LEFT, false, SWT.CENTER, false);
		fWrapCheckBox.setSelection(findReplaceLogic.isActive(SearchOptions.WRAP));
		fWrapCheckBox.addSelectionListener(selectionListener);
		storeButtonWithMnemonicInMap(fWrapCheckBox);

		fWholeWordCheckBox = new Button(group, SWT.CHECK | SWT.LEFT);
		fWholeWordCheckBox.setText(FindReplaceMessages.FindReplace_WholeWordCheckBox_label);
		setGridData(fWholeWordCheckBox, SWT.LEFT, false, SWT.CENTER, false);
		fWholeWordCheckBox.setSelection(findReplaceLogic.isActive(SearchOptions.WHOLE_WORD));
		fWholeWordCheckBox.addSelectionListener(selectionListener);
		storeButtonWithMnemonicInMap(fWholeWordCheckBox);

		fIncrementalCheckBox = new Button(group, SWT.CHECK | SWT.LEFT);
		fIncrementalCheckBox.setText(FindReplaceMessages.FindReplace_IncrementalCheckBox_label);
		setGridData(fIncrementalCheckBox, SWT.LEFT, false, SWT.CENTER, false);
		fIncrementalCheckBox.setSelection(findReplaceLogic.isActive(SearchOptions.INCREMENTAL));
		fIncrementalCheckBox.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setupFindReplaceLogic();
				storeSettings();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		storeButtonWithMnemonicInMap(fIncrementalCheckBox);

		fIsRegExCheckBox = new Button(group, SWT.CHECK | SWT.LEFT);
		fIsRegExCheckBox.setText(FindReplaceMessages.FindReplace_RegExCheckbox_label);
		setGridData(fIsRegExCheckBox, SWT.LEFT, false, SWT.CENTER, false);
		((GridData) fIsRegExCheckBox.getLayoutData()).horizontalSpan = 2;
		fIsRegExCheckBox.setSelection(findReplaceLogic.isActive(SearchOptions.REGEX));
		fIsRegExCheckBox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean newState = fIsRegExCheckBox.getSelection();
				setupFindReplaceLogic();
				storeSettings();
				updateButtonState();
				setContentAssistsEnablement(newState);
				fIncrementalCheckBox.setEnabled(findReplaceLogic.isIncrementalSearchAvailable());
			}
		});
		storeButtonWithMnemonicInMap(fIsRegExCheckBox);
		fWholeWordCheckBox.setEnabled(findReplaceLogic.isWholeWordSearchAvailable(getFindString()));
		fWholeWordCheckBox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateButtonState();
			}
		});
		fIncrementalCheckBox.setEnabled(findReplaceLogic.isIncrementalSearchAvailable());
		return panel;
	}

	/**
	 * Creates the status and close section of the dialog.
	 *
	 * @param parent the parent composite
	 * @return the status and close button
	 */
	private Composite createStatusAndCloseButton(Composite parent) {

		Composite panel = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		panel.setLayout(layout);

		fStatusLabel = new Label(panel, SWT.LEFT);
		setGridData(fStatusLabel, SWT.FILL, true, SWT.CENTER, false);

		String label = FindReplaceMessages.FindReplace_CloseButton_label;
		Button closeButton = createButton(panel, CLOSE_BUTTON_ID, label, false);
		setGridData(closeButton, SWT.RIGHT, false, SWT.BOTTOM, false);

		return panel;
	}

	/*
	 * @see Dialog#buttonPressed
	 */
	@Override
	protected void buttonPressed(int buttonID) {
		if (buttonID == 101)
			close();
	}

	// ------- action invocation ---------------------------------------

	/**
	 * Returns the dialog's boundaries.
	 *
	 * @return the dialog's boundaries
	 */
	private Rectangle getDialogBoundaries() {
		if (okToUse(getShell()))
			return getShell().getBounds();
		return fDialogPositionInit;
	}

	// ------- accessors ---------------------------------------

	/**
	 * Retrieves the string to search for from the appropriate text input field and
	 * returns it.
	 *
	 * @return the search string
	 */
	private String getFindString() {
		if (okToUse(fFindField)) {
			return fFindField.getText();
		}
		return ""; //$NON-NLS-1$
	}

	/**
	 * Retrieves the replacement string from the appropriate text input field and
	 * returns it.
	 *
	 * @return the replacement string
	 */
	private String getReplaceString() {
		if (okToUse(fReplaceField)) {
			return fReplaceField.getText();
		}
		return ""; //$NON-NLS-1$
	}

	// ------- init / close ---------------------------------------

	/**
	 * Returns the first line of the given selection.
	 *
	 * @param selection the selection
	 * @return the first line of the selection
	 */
	private String getFirstLine(String selection) {
		if (!selection.isEmpty()) {
			int delimiterOffset = TextUtilities.nextDelimiter(selection, 0).delimiterIndex;
			if (delimiterOffset > 0)
				return selection.substring(0, delimiterOffset);
			else if (delimiterOffset == -1)
				return selection;
		}
		return ""; //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.jface.window.Window#close()
	 */
	@Override
	public boolean close() {
		handleDialogClose();
		return super.close();
	}

	/**
	 * Removes focus changed listener from browser and stores settings for re-open.
	 */
	private void handleDialogClose() {

		// remove listeners
		if (okToUse(fFindField)) {
			fFindField.removeModifyListener(fFindModifyListener);
		}

		if (fParentShell != null) {
			fParentShell.removeShellListener(fActivationListener);
			fParentShell = null;
		}

		getShell().removeShellListener(fActivationListener);

		// store current settings in case of re-open
		storeSettings();

		findReplaceLogic.dispose();

		// prevent leaks
		fActiveShell = null;
	}

	/**
	 * Writes the current selection to the dialog settings.
	 *
	 * @since 3.0
	 */
	private void writeSelection() {
		String selection = getCurrentSelection();
		if (selection == null)
			return;

		IDialogSettings s = getDialogSettings();
		s.put("selection", selection); //$NON-NLS-1$
	}

	/**
	 * Stores the current state in the dialog settings.
	 *
	 * @since 2.0
	 */
	private void storeSettings() {
		fDialogPositionInit = getDialogBoundaries();

		writeConfiguration();
	}

	/**
	 * Initializes the string to search for and the according text in the find field
	 * based on either the selection in the target or, if nothing is selected, with
	 * the newest search history entry.
	 */
	private void initFindString() {
		if (!okToUse(fFindField)) {
			return;
		}

		fFindField.removeModifyListener(fFindModifyListener);
		if (hasTargetSelection()) {
			initFindStringFromSelection();
		} else {
			initFindStringFromHistory();
		}
		fFindField.setSelection(new Point(0, fFindField.getText().length()));
		fFindField.addModifyListener(fFindModifyListener);
	}

	private boolean hasTargetSelection() {
		String selection = getCurrentSelection();
		return selection != null && !selection.isEmpty();
	}

	/**
	 * Initializes the string to search for and the appropriate text in the Find
	 * field based on the selection found in the action's target.
	 */
	private void initFindStringFromSelection() {
		String selection = getCurrentSelection();
		String searchInput = getFirstLine(selection);
		boolean isSingleLineInput = searchInput.equals(selection);
		if (findReplaceLogic.isRegExSearchAvailableAndActive()) {
			searchInput = FindReplaceDocumentAdapter.escapeForRegExPattern(selection);
		}
		fFindField.setText(searchInput);

		if (isSingleLineInput) {
			// initialize search with current selection to allow for execution of replace
			// operations
			findReplaceLogic.findAndSelect(findReplaceLogic.getTarget().getSelection().x, fFindField.getText());
		} else {
			fGlobalRadioButton.setSelection(false);
			fSelectedRangeRadioButton.setSelection(true);
		}
	}

	private void initFindStringFromHistory() {
		if ("".equals(fFindField.getText())) { //$NON-NLS-1$
			if (!findHistory.isEmpty()) {
				fFindField.setText(findHistory.get(0));
			} else {
				fFindField.setText(""); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Creates a button.
	 *
	 * @param parent     the parent control
	 * @param label      the button label
	 * @param id         the button id
	 * @param dfltButton is this button the default button
	 * @param listener   a button pressed listener
	 * @return the new button
	 */
	private Button makeButton(Composite parent, String label, int id, boolean dfltButton, SelectionListener listener) {
		Button button = createButton(parent, id, label, dfltButton);
		button.addSelectionListener(listener);
		storeButtonWithMnemonicInMap(button);
		return button;
	}

	/**
	 * Stores the button and its mnemonic in {@link #fMnemonicButtonMap}.
	 *
	 * @param button button whose mnemonic has to be stored
	 * @since 3.7
	 */
	private void storeButtonWithMnemonicInMap(Button button) {
		char mnemonic = LegacyActionTools.extractMnemonic(button.getText());
		if (mnemonic != LegacyActionTools.MNEMONIC_NONE)
			fMnemonicButtonMap.put(Character.valueOf(Character.toLowerCase(mnemonic)), button);
	}

	// ------- UI creation ---------------------------------------

	/**
	 * Attaches the given layout specification to the <code>component</code>.
	 *
	 * @param component                 the component
	 * @param horizontalAlignment       horizontal alignment
	 * @param grabExcessHorizontalSpace grab excess horizontal space
	 * @param verticalAlignment         vertical alignment
	 * @param grabExcessVerticalSpace   grab excess vertical space
	 */
	private void setGridData(Control component, int horizontalAlignment, boolean grabExcessHorizontalSpace,
			int verticalAlignment, boolean grabExcessVerticalSpace) {
		GridData gd;
		if (component instanceof Button && (((Button) component).getStyle() & SWT.PUSH) != 0) {
			SWTUtil.setButtonDimensionHint((Button) component);
			gd = (GridData) component.getLayoutData();
		} else {
			gd = new GridData();
			component.setLayoutData(gd);
			gd.horizontalAlignment = horizontalAlignment;
			gd.grabExcessHorizontalSpace = grabExcessHorizontalSpace;
		}
		gd.verticalAlignment = verticalAlignment;
		gd.grabExcessVerticalSpace = grabExcessVerticalSpace;
	}

	/**
	 * Adds enough space in the control's layout data margin for the content assist
	 * decoration.
	 *
	 * @param control the control that needs a margin
	 * @since 3.3
	 */
	private void addDecorationMargin(Control control) {
		Object layoutData = control.getLayoutData();
		if (!(layoutData instanceof GridData))
			return;
		GridData gd = (GridData) layoutData;
		FieldDecoration dec = FieldDecorationRegistry.getDefault()
				.getFieldDecoration(FieldDecorationRegistry.DEC_CONTENT_PROPOSAL);
		gd.horizontalIndent = dec.getImage().getBounds().width;
	}

	/**
	 * Updates the enabled state of the buttons.
	 */
	private void updateButtonState() {
		updateButtonState(false);
	}

	/**
	 * Updates the enabled state of the buttons.
	 *
	 * @param disableReplace <code>true</code> if replace button must be disabled
	 * @since 3.0
	 */
	private void updateButtonState(boolean disableReplace) {
		setupFindReplaceLogic();
		if (okToUse(getShell()) && okToUse(fFindNextButton)) {

			boolean hasActiveSelection = false;
			String selection = getCurrentSelection();
			if (selection != null)
				hasActiveSelection = !selection.isEmpty();

			// using short-circuit-evaluation, evaluate all expressions to false
			// (disabling the corresponding button) if we cannot evaluate the other
			// expression (for example because a target is missing)
			boolean enable = (findReplaceLogic.getTarget() != null)
					&& (fActiveShell == fParentShell || fActiveShell == getShell());
			IFindReplaceTarget target = findReplaceLogic.getTarget();
			String str = getFindString();
			boolean isFindStringSet = str != null && !str.isEmpty();
			String selectionString = enable ? target.getSelection().toString() : ""; //$NON-NLS-1$
			boolean isTargetEditable = enable ? target.isEditable() : false;
			boolean isRegExSearchAvailableAndActive = findReplaceLogic.isRegExSearchAvailableAndActive();
			boolean isSelectionGoodForReplace = selectionString != "" //$NON-NLS-1$
					|| !isRegExSearchAvailableAndActive;

			fWholeWordCheckBox.setEnabled(findReplaceLogic.isWholeWordSearchAvailable(getFindString()));
			fFindNextButton.setEnabled(enable && isFindStringSet);
			fSelectAllButton.setEnabled(enable && isFindStringSet && (target instanceof IFindReplaceTargetExtension4));
			fReplaceSelectionButton.setEnabled(
					!disableReplace && enable && isTargetEditable && hasActiveSelection && isSelectionGoodForReplace);
			fReplaceFindButton.setEnabled(!disableReplace && enable && isTargetEditable && isFindStringSet
					&& hasActiveSelection && isSelectionGoodForReplace);
			fReplaceAllButton.setEnabled(enable && isTargetEditable && isFindStringSet);
		}
	}


	/**
	 * Updates the given combo with the given content.
	 *
	 * @param combo   combo to be updated
	 * @param content to be put into the combo
	 */
	private void updateCombo(Combo combo, Iterable<String> content) {
		combo.removeAll();
		for (String element : content) {
			combo.add(element.toString());
		}
	}

	// ------- open / reopen ---------------------------------------

	/**
	 * Called after executed find/replace action to update the history.
	 */
	private void updateFindAndReplaceHistory() {
		updateFindHistory();
		if (okToUse(fReplaceField)) {
			updateHistory(fReplaceField, replaceHistory);
		}

	}

	/**
	 * Called after executed find action to update the history.
	 */
	private void updateFindHistory() {
		if (okToUse(fFindField)) {
			fFindField.removeModifyListener(fFindModifyListener);

			// XXX: Workaround for Combo bug on Linux (see bug 404202 and bug 410603)
			if (Util.isLinux())
				fFindModifyListener.ignoreNextEvent();

			updateHistory(fFindField, findHistory);
			fFindField.addModifyListener(fFindModifyListener);
		}
	}

	/**
	 * Updates the combo with the history.
	 *
	 * @param combo   to be updated
	 * @param history to be put into the combo
	 */
	private void updateHistory(Combo combo, HistoryStore history) {
		String findString = combo.getText();
		history.remove(findString); // ensure findString is now on the newest index of the history
		history.add(findString);
		Point selection = combo.getSelection();
		updateCombo(combo, history.get());
		combo.setText(findString);
		combo.setSelection(selection);
	}

	/**
	 * Updates this dialog because of a different target.
	 *
	 * @param target               the new target
	 * @param isTargetEditable     <code>true</code> if the new target can be
	 *                             modified
	 * @param initializeFindString <code>true</code> if the find string of this
	 *                             dialog should be initialized based on the
	 *                             viewer's selection
	 * @since 2.0
	 */
	public void updateTarget(IFindReplaceTarget target, boolean isTargetEditable, boolean initializeFindString) {
		findReplaceLogic.updateTarget(target, isTargetEditable);

		boolean globalSearch = findReplaceLogic.isActive(SearchOptions.GLOBAL);
		fGlobalRadioButton.setSelection(globalSearch);
		boolean useSelectedLines = !globalSearch;
		fSelectedRangeRadioButton.setSelection(useSelectedLines);

		boolean targetExists = findReplaceLogic.getTarget() != null;
		if (okToUse(fIsRegExCheckBox)) {
			fIsRegExCheckBox
					.setEnabled(targetExists && findReplaceLogic.getTarget() instanceof IFindReplaceTargetExtension3);
		}

		if (okToUse(fWholeWordCheckBox)) {
			fWholeWordCheckBox.setEnabled(findReplaceLogic.isWholeWordSearchAvailable(getFindString()));
		}

		if (okToUse(fIncrementalCheckBox)) {
			fIncrementalCheckBox.setEnabled(findReplaceLogic.isIncrementalSearchAvailable());
		}

		if (okToUse(fReplaceLabel)) {
			fReplaceLabel.setEnabled(targetExists && findReplaceLogic.getTarget().isEditable());
			fReplaceField.setEnabled(targetExists && findReplaceLogic.getTarget().isEditable());
			fReplaceAllButton.setEnabled(targetExists && findReplaceLogic.getTarget().isEditable());
			if (initializeFindString) {
				initFindString();
				fGiveFocusToFindField = true;
			}
		}

		updateButtonState();

		setContentAssistsEnablement(findReplaceLogic.isRegExSearchAvailableAndActive());
	}

	/**
	 * Sets the parent shell of this dialog to be the given shell.
	 *
	 * @param shell the new parent shell
	 */
	@Override
	public void setParentShell(Shell shell) {
		if (shell != fParentShell) {

			if (fParentShell != null)
				fParentShell.removeShellListener(fActivationListener);

			fParentShell = shell;
			fParentShell.addShellListener(fActivationListener);
		}

		fActiveShell = shell;
	}

	// --------------- configuration handling --------------

	/**
	 * Sets up the required managers for search history
	 */
	private void setupSearchHistory() {
		findHistory = new HistoryStore(getDialogSettings(), "findhistory", HISTORY_SIZE); //$NON-NLS-1$
		replaceHistory = new HistoryStore(getDialogSettings(), "replacehistory", HISTORY_SIZE); //$NON-NLS-1$
	}

	/**
	 * Returns the dialog settings object used to share state between several
	 * find/replace dialogs.
	 *
	 * @return the dialog settings to be used
	 */
	private IDialogSettings getDialogSettings() {
		IDialogSettings settings = PlatformUI
				.getDialogSettingsProvider(FrameworkUtil.getBundle(FindReplaceDialog.class)).getDialogSettings();
		fDialogSettings = settings.getSection(getClass().getName());
		if (fDialogSettings == null)
			fDialogSettings = settings.addNewSection(getClass().getName());
		return fDialogSettings;
	}

	@Override
	protected IDialogSettings getDialogBoundsSettings() {
		String sectionName = getClass().getName() + "_dialogBounds"; //$NON-NLS-1$
		IDialogSettings settings = PlatformUI
				.getDialogSettingsProvider(FrameworkUtil.getBundle(FindReplaceDialog.class)).getDialogSettings();
		IDialogSettings section = settings.getSection(sectionName);
		if (section == null)
			section = settings.addNewSection(sectionName);
		return section;
	}

	@Override
	protected int getDialogBoundsStrategy() {
		return DIALOG_PERSISTLOCATION | DIALOG_PERSISTSIZE;
	}

	/**
	 * Initializes itself from the dialog settings with the same state as at the
	 * previous invocation.
	 */
	private void readConfiguration() {
		IDialogSettings s = getDialogSettings();

		activateInFindReplaceLogicIf(SearchOptions.WRAP, s.get("wrap") == null || s.getBoolean("wrap")); //$NON-NLS-1$ //$NON-NLS-2$
		activateInFindReplaceLogicIf(SearchOptions.CASE_SENSITIVE, s.getBoolean("casesensitive")); //$NON-NLS-1$
		activateInFindReplaceLogicIf(SearchOptions.WHOLE_WORD, s.getBoolean("wholeword")); //$NON-NLS-1$
		activateInFindReplaceLogicIf(SearchOptions.INCREMENTAL, s.getBoolean("incremental")); //$NON-NLS-1$
		activateInFindReplaceLogicIf(SearchOptions.REGEX, s.getBoolean("isRegEx")); //$NON-NLS-1$

	}

	private void setupFindReplaceLogic() {
		activateInFindReplaceLogicIf(SearchOptions.WRAP, fWrapCheckBox.getSelection());
		activateInFindReplaceLogicIf(SearchOptions.FORWARD, fForwardRadioButton.getSelection());
		activateInFindReplaceLogicIf(SearchOptions.CASE_SENSITIVE, fCaseCheckBox.getSelection());
		activateInFindReplaceLogicIf(SearchOptions.REGEX, fIsRegExCheckBox.getSelection());
		activateInFindReplaceLogicIf(SearchOptions.WHOLE_WORD, fWholeWordCheckBox.getSelection());
		activateInFindReplaceLogicIf(SearchOptions.INCREMENTAL, fIncrementalCheckBox.getSelection());
	}

	/**
	 * Stores its current configuration in the dialog store.
	 */
	private void writeConfiguration() {
		IDialogSettings s = getDialogSettings();

		s.put("wrap", findReplaceLogic.isActive(SearchOptions.WRAP)); //$NON-NLS-1$
		s.put("casesensitive", findReplaceLogic.isActive(SearchOptions.CASE_SENSITIVE)); //$NON-NLS-1$
		s.put("wholeword", findReplaceLogic.isActive(SearchOptions.WHOLE_WORD)); //$NON-NLS-1$
		s.put("incremental", findReplaceLogic.isActive(SearchOptions.INCREMENTAL)); //$NON-NLS-1$
		s.put("isRegEx", findReplaceLogic.isActive(SearchOptions.REGEX)); //$NON-NLS-1$

		String findString = getFindString();
		findHistory.add(findString);

		String replaceString = getReplaceString();
		replaceHistory.add(replaceString);
	}

	private void activateInFindReplaceLogicIf(SearchOptions option, boolean shouldActivate) {
		if (shouldActivate) {
			findReplaceLogic.activate(option);
		} else {
			findReplaceLogic.deactivate(option);
		}
	}

	private void evaluateFindReplaceStatus() {
		evaluateFindReplaceStatus(true);
	}

	/**
	 * Evaluate the status of the FindReplaceLogic object.
	 *
	 * @param allowBeep Whether the evaluation should beep on some codes.
	 */
	private void evaluateFindReplaceStatus(boolean allowBeep) {
		IFindReplaceStatus status = findReplaceLogic.getStatus();

		String dialogMessage = status.accept(new FindReplaceLogicMessageGenerator());
		fStatusLabel.setText(dialogMessage);
		if (status.isInputValid()) {
			fStatusLabel.setForeground(fReplaceLabel.getForeground());
		} else {
			fStatusLabel.setForeground(JFaceColors.getErrorText(fStatusLabel.getDisplay()));
		}

		if (!status.wasSuccessful()) {
			tryToBeep(allowBeep);
		}
	}

	/**
	 * Tries beeping using the default beep. Will beep if the shell is currently
	 * usable.
	 *
	 * @param allowBeep Whether or not beeps should be allowed. Suppresses all beeps
	 *                  if false.
	 */
	private void tryToBeep(boolean allowBeep) {
		if (okToUse(getShell()) && allowBeep) {
			getShell().getDisplay().beep();
		}
	}

	private String getCurrentSelection() {
		IFindReplaceTarget target = findReplaceLogic.getTarget();
		if (target == null)
			return null;
		return target.getSelectionText();
	}
}
