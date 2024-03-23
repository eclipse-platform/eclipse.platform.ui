/*******************************************************************************
 * Copyright (c) 2023 Vector Informatik GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Vector Informatik GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.texteditor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGBA;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Scrollable;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.runtime.preferences.InstanceScope;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.jface.window.Window;

import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.IFindReplaceTargetExtension;

import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.findandreplace.FindReplaceLogic;
import org.eclipse.ui.internal.findandreplace.SearchOptions;
import org.eclipse.ui.internal.findandreplace.status.FindStatus;
import org.eclipse.ui.internal.findandreplace.status.IFindReplaceStatus;
import org.eclipse.ui.internal.findandreplace.status.InvalidRegExStatus;

/**
 * @since 3.17
 */
public class FindReplaceOverlay extends Dialog {

	FindReplaceAction parentAction;
	FindReplaceLogic findReplaceLogic;
	IWorkbenchPart targetPart;
	boolean overlayOpen;
	boolean replaceBarOpen;

	Composite container;
	Button replaceToggle;

	Composite contentGroup;

	Composite searchContainer;
	Composite searchBarContainer;
	Text searchBar;
	ToolBar searchTools;

	ToolItem searchInSelectionButton;
	ToolItem wholeWordSearchButton;
	ToolItem caseSensitiveSearchButton;
	ToolItem regexSearchButton;
	ToolItem searchUpButton;
	ToolItem searchDownButton;
	ToolItem searchAllButton;

	Composite replaceContainer;
	Composite replaceBarContainer;
	Text replaceBar;
	ToolBar replaceTools;
	ToolItem replaceButton;
	ToolItem replaceAllButton;

	Color backgroundToUse;
	Color normalTextForegroundColor;

	IPreferenceChangeListener overlayDialogPreferenceListener = new IPreferenceChangeListener() {

		@Override
		public void preferenceChange(PreferenceChangeEvent event) {
			if (event.getKey().equals("usefindreplaceoverlay")) { //$NON-NLS-1$
				close();
			}
		}

	};

	public FindReplaceOverlay(Shell parent, IWorkbenchPart part, IFindReplaceTarget target,
			FindReplaceAction parentAction) {
		super(parent);
		this.parentAction = parentAction;
		createFindReplacer(target);

		setShellStyle(SWT.MODELESS);
		setBlockOnOpen(false);
		targetPart = part;

		IEclipsePreferences preferences = InstanceScope.INSTANCE.getNode("org.eclipse.ui.editors"); //$NON-NLS-1$
		preferences.addPreferenceChangeListener(overlayDialogPreferenceListener);
	}

	@Override
	protected boolean isResizable() {
		return false;
	}

	private void createFindReplacer(IFindReplaceTarget target) {
		findReplaceLogic = new FindReplaceLogic();
		boolean isTargetEditable = false;
		if (target != null) {
			isTargetEditable = target.isEditable();
		}
		findReplaceLogic.updateTarget(target, isTargetEditable);
		findReplaceLogic.activate(SearchOptions.INCREMENTAL);
		findReplaceLogic.activate(SearchOptions.GLOBAL);
		findReplaceLogic.activate(SearchOptions.WRAP);
		findReplaceLogic.activate(SearchOptions.FORWARD);
	}

	KeyListener shortcuts = new KeyListener() {

		private void performEnterAction(KeyEvent e) {
			boolean isShiftPressed = (e.stateMask & SWT.SHIFT) != 0;
			boolean isCtrlPressed = (e.stateMask & SWT.CTRL) != 0;
			if (okayToUse(replaceBar) && replaceBar.isFocusControl()) {
				if (isCtrlPressed) {
					findReplaceLogic.performReplaceAll(getFindString(), getReplaceString(), getShell().getDisplay());
				} else {
					performSingleReplace();
					evaluateFindReplacerStatus();
				}
			} else {
				if (isCtrlPressed) {
					findReplaceLogic.performSelectAll(getFindString(), getShell().getDisplay());
					evaluateFindReplacerStatus();
				} else {
					boolean oldForwardSearchSetting = findReplaceLogic.isActive(SearchOptions.FORWARD);
					activateInFindReplacerIf(SearchOptions.FORWARD, !isShiftPressed);
					findReplaceLogic.deactivate(SearchOptions.INCREMENTAL);
					findReplaceLogic.performSearch(getFindString());
					evaluateFindReplacerStatus();
					activateInFindReplacerIf(SearchOptions.FORWARD, oldForwardSearchSetting);
					findReplaceLogic.activate(SearchOptions.INCREMENTAL);
				}
			}
		}

		@Override
		public void keyPressed(KeyEvent e) {
			if ((e.stateMask & SWT.CTRL) != 0 && (e.keyCode == 'F' || e.keyCode == 'f')) {
				parentAction.closeModernOverlay();
			} else if ((e.stateMask & SWT.CTRL) != 0 && (e.keyCode == 'R' || e.keyCode == 'r')) {
				replaceToggle.setSelection(!replaceToggle.getSelection());
				replaceToggle.notifyListeners(SWT.Selection, null);
			} else if ((e.stateMask & SWT.CTRL) != 0 && (e.keyCode == 'W' || e.keyCode == 'w')) {
				wholeWordSearchButton.setSelection(!wholeWordSearchButton.getSelection());
				wholeWordSearchButton.notifyListeners(SWT.Selection, null);
			} else if ((e.stateMask & SWT.CTRL) != 0 && (e.keyCode == 'P' || e.keyCode == 'p')) {
				regexSearchButton.setSelection(!regexSearchButton.getSelection());
				regexSearchButton.notifyListeners(SWT.Selection, null);
			} else if ((e.stateMask & SWT.CTRL) != 0 && (e.keyCode == 'A' || e.keyCode == 'a')) {
				searchInSelectionButton.setSelection(!searchInSelectionButton.getSelection());
				searchInSelectionButton.notifyListeners(SWT.Selection, null);
			} else if ((e.stateMask & SWT.CTRL) != 0 && (e.keyCode == 'C' || e.keyCode == 'c')) {
				caseSensitiveSearchButton.setSelection(!caseSensitiveSearchButton.getSelection());
				caseSensitiveSearchButton.notifyListeners(SWT.Selection, null);
			} else if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR) {
				performEnterAction(e);
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {
			// Do nothing
		}

	};
	ControlListener shellMovementListener = new ControlListener() {
		@Override
		public void controlMoved(ControlEvent e) {
			positionToPart();
		}

		@Override
		public void controlResized(ControlEvent e) {
			positionToPart();
		}
	};
	PaintListener widgetMovementListener = new PaintListener() {

		@Override
		public void paintControl(PaintEvent e) {
			positionToPart();
		}

	};
	IPartListener partListener = new IPartListener() {
		@Override
		public void partActivated(IWorkbenchPart part) {
			if (getShell() != null) {
				getShell().setVisible(isPartCurrentlyDisplayedInPartSash());
			}
		}

		@Override
		public void partDeactivated(IWorkbenchPart part) {
			// Do nothing
		}

		@Override
		public void partBroughtToTop(IWorkbenchPart part) {
			if (getShell() != null) {
				getShell().setVisible(isPartCurrentlyDisplayedInPartSash());
			}
		}

		@Override
		public void partClosed(IWorkbenchPart part) {
			close();
		}

		@Override
		public void partOpened(IWorkbenchPart part) {
			// Do nothing
		}
	};

	public boolean isPartCurrentlyDisplayedInPartSash() {
		IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

		// Check if the targetPart is currently displayed on the active page
		boolean isPartDisplayed = false;

		if (activePage != null) {
			IWorkbenchPart activePart = activePage.getActivePart();
			if (activePart != null && activePart == targetPart) {
				isPartDisplayed = true;
			}
		}

		return isPartDisplayed;
	}

	@Override
	public void create() {
		if (overlayOpen) {
			return;
		}
		super.create();
	}

	@Override
	public boolean close() {
		if (!overlayOpen) {
			return true;
		}
		findReplaceLogic.activate(SearchOptions.GLOBAL);
		overlayOpen = false;
		replaceBarOpen = false;
		unbindListeners();
		container.dispose();
		backgroundToUse.dispose();
		return super.close();
	}

	@Override
	public int open() {
		int returnCode;
		positionToPart();
		if (overlayOpen) {
			searchBar.forceFocus();
			returnCode = Window.CANCEL;
		} else {
			bindListeners();
			returnCode = super.open();
		}
		overlayOpen = true;
		normalTextForegroundColor = searchBar.getForeground();

		applyOverlayColors(backgroundToUse, true);

		initFindStringFromSelection();
		return returnCode;
	}

	private void applyOverlayColors(Color color, boolean tryToColorReplaceBar) {
		searchTools.setBackground(color);
		searchInSelectionButton.setBackground(color);
		wholeWordSearchButton.setBackground(color);
		regexSearchButton.setBackground(color);
		caseSensitiveSearchButton.setBackground(color);
		searchAllButton.setBackground(color);
		searchUpButton.setBackground(color);
		searchDownButton.setBackground(color);

		searchBarContainer.setBackground(color);
		searchBar.setBackground(color);
		searchContainer.setBackground(color);

		if (replaceBarOpen && tryToColorReplaceBar) {
			replaceContainer.setBackground(color);
			replaceBar.setBackground(color);
			replaceBarContainer.setBackground(color);
			replaceAllButton.setBackground(color);
			replaceButton.setBackground(color);
		}
	}

	private void unbindListeners() {
		if (targetPart != null && targetPart instanceof StatusTextEditor textEditor) {
			Control targetWidget = textEditor.getSourceViewer().getTextWidget();
			if (targetWidget != null) {
				targetWidget.getShell().removeControlListener(shellMovementListener);
				targetWidget.removePaintListener(widgetMovementListener);
				targetPart.getSite().getPage().removePartListener(partListener);
			}
		}
	}

	private void bindListeners() {
		if (targetPart instanceof StatusTextEditor textEditor) {
			Control targetWidget = textEditor.getSourceViewer().getTextWidget();

			targetWidget.getShell().addControlListener(shellMovementListener);
			targetWidget.addPaintListener(widgetMovementListener);
			targetPart.getSite().getPage().addPartListener(partListener);
		}
	}

	@Override
	public Control createContents(Composite parent) {
		backgroundToUse = new Color(getShell().getDisplay(), new RGBA(0, 0, 0, 0));
		Control ret = createDialog(parent);
		positionToPart();
		return ret;
	}

	public Control createDialog(final Composite parent) {
		createMainContainer(parent);

		retrieveBackgroundColor();

		createFindContainer();
		createSearchBar();
		createSearchTools();

		container.layout();

		applyDialogFont(container);
		return container;
	}

	/**
	 * HACK: In order to not introduce a hard-coded color, we need to retrieve the
	 * color of the "SWT.SEARCH"-Text. Since that search-bar has a border, we don't
	 * want to have it in our own form. Instead, we create such a bar at start-up,
	 * grab it's color and then immediately dispose of that bar.
	 */
	private void retrieveBackgroundColor() {
		Text textBarForRetrievingTheRightColor = new Text(container, SWT.SINGLE | SWT.SEARCH);
		container.layout();
		// this allocates a new color that needs to be disposed correctly!
		backgroundToUse = new Color(getShell().getDisplay(), textBarForRetrievingTheRightColor.getBackground().getRGB());
		textBarForRetrievingTheRightColor.dispose();
	}

	private void createSearchTools() {
		searchTools = new ToolBar(searchContainer, SWT.HORIZONTAL);
		GridDataFactory.fillDefaults().grab(false, true).align(GridData.CENTER, GridData.END).applyTo(searchTools);
		searchInSelectionButton = new ToolItem(searchTools, SWT.CHECK);
		searchInSelectionButton.setImage(FindReplaceOverlayImages.get(FindReplaceOverlayImages.OBJ_WHOLE_WORD));
		searchInSelectionButton.setToolTipText("Only find in selected Area (Ctrl + shift + A)"); //$NON-NLS-1$
		searchInSelectionButton.setSelection(findReplaceLogic.isActive(SearchOptions.WHOLE_WORD));
		searchInSelectionButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				activateInFindReplacerIf(SearchOptions.GLOBAL, !searchInSelectionButton.getSelection());
				updateIncrementalSearch();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// Do Nothing
			}
		});
		wholeWordSearchButton = new ToolItem(searchTools, SWT.CHECK);
		wholeWordSearchButton.setImage(FindReplaceOverlayImages.get(FindReplaceOverlayImages.OBJ_WHOLE_WORD));
		wholeWordSearchButton.setToolTipText("Only find in whole words (Ctrl + shift + W)"); //$NON-NLS-1$
		wholeWordSearchButton.setSelection(findReplaceLogic.isActive(SearchOptions.WHOLE_WORD));
		wholeWordSearchButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				activateInFindReplacerIf(SearchOptions.WHOLE_WORD, wholeWordSearchButton.getSelection());
				updateIncrementalSearch();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// Do Nothing
			}
		});
		caseSensitiveSearchButton = new ToolItem(searchTools, SWT.CHECK);
		caseSensitiveSearchButton.setImage(FindReplaceOverlayImages.get(FindReplaceOverlayImages.OBJ_CASE_SENSITIVE));
		caseSensitiveSearchButton.setToolTipText("Match case (Ctrl + shift + C)"); //$NON-NLS-1$
		caseSensitiveSearchButton.setSelection(findReplaceLogic.isActive(SearchOptions.CASE_SENSITIVE));
		caseSensitiveSearchButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				activateInFindReplacerIf(SearchOptions.CASE_SENSITIVE, caseSensitiveSearchButton.getSelection());
				updateIncrementalSearch();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// Do Nothing
			}
		});
		regexSearchButton = new ToolItem(searchTools, SWT.CHECK);
		regexSearchButton.setImage(FindReplaceOverlayImages.get(FindReplaceOverlayImages.OBJ_FIND_REGEX));
		regexSearchButton.setToolTipText("Match regular-expression pattern (Ctrl + shift + P)"); //$NON-NLS-1$
		regexSearchButton.setSelection(findReplaceLogic.isActive(SearchOptions.REGEX));
		regexSearchButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				activateInFindReplacerIf(SearchOptions.REGEX, ((ToolItem) e.widget).getSelection());
				wholeWordSearchButton.setEnabled(!findReplaceLogic.isActive(SearchOptions.REGEX));
				updateIncrementalSearch();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// Do nothing
			}
		});

		@SuppressWarnings("unused")
		ToolItem separator = new ToolItem(searchTools, SWT.SEPARATOR);

		searchAllButton = new ToolItem(searchTools, SWT.PUSH);
		searchAllButton.setImage(FindReplaceOverlayImages.get(FindReplaceOverlayImages.OBJ_SEARCH_ALL));
		searchAllButton.setToolTipText("Search all (Ctrl + Enter)"); //$NON-NLS-1$
		searchAllButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				findReplaceLogic.performSelectAll(getFindString(), getShell().getDisplay());
				evaluateFindReplacerStatus();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// Do nothing
			}

		});
		searchUpButton = new ToolItem(searchTools, SWT.PUSH);
		searchUpButton.setImage(FindReplaceOverlayImages.get(FindReplaceOverlayImages.ELCL_FIND_PREV));
		searchUpButton.setToolTipText("Search backward (Shift + Enter)"); //$NON-NLS-1$
		searchUpButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				findReplaceLogic.deactivate(SearchOptions.FORWARD);
				findReplaceLogic.performSearch(getFindString());
				evaluateFindReplacerStatus();
				findReplaceLogic.activate(SearchOptions.FORWARD);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// Do Nothing
			}
		});
		searchDownButton = new ToolItem(searchTools, SWT.PUSH);
		searchDownButton.setSelection(true); // by default, search down
		searchDownButton.setImage(FindReplaceOverlayImages.get(FindReplaceOverlayImages.ELCL_FIND_NEXT));
		searchDownButton.setToolTipText("Search forward (Enter)"); //$NON-NLS-1$
		searchDownButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				findReplaceLogic.activate(SearchOptions.FORWARD);
				findReplaceLogic.deactivate(SearchOptions.INCREMENTAL);
				findReplaceLogic.performSearch(getFindString());
				evaluateFindReplacerStatus();
				findReplaceLogic.deactivate(SearchOptions.INCREMENTAL);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// Do nothing
			}
		});

	}

	private void createReplaceTools() {
		Color warningColor = JFaceColors.getErrorText(getShell().getDisplay());

		replaceTools = new ToolBar(replaceContainer, SWT.HORIZONTAL);
		GridDataFactory.fillDefaults().grab(false, true).align(GridData.CENTER, GridData.END).applyTo(replaceTools);
		replaceButton = new ToolItem(replaceTools, SWT.PUSH);
		replaceButton.setImage(FindReplaceOverlayImages.get(FindReplaceOverlayImages.OBJ_REPLACE));
		replaceButton.setToolTipText("Replace (Enter)"); //$NON-NLS-1$
		replaceButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (getFindString().isEmpty()) {
					showUserFeedback(warningColor);
					return;
				}
				performSingleReplace();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// Do nothing
			}
		});
		replaceAllButton = new ToolItem(replaceTools, SWT.PUSH);
		replaceAllButton.setImage(FindReplaceOverlayImages.get(FindReplaceOverlayImages.OBJ_REPLACE_ALL));
		replaceAllButton.setToolTipText("Replace All (Ctrl + Enter)"); //$NON-NLS-1$
		replaceAllButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (getFindString().isEmpty()) {
					showUserFeedback(warningColor);
					return;
				}
				findReplaceLogic.performReplaceAll(getFindString(), getReplaceString(), getShell().getDisplay());
				evaluateFindReplacerStatus();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// Do Nothing
			}
		});
	}

	private void createSearchBar() {
		searchBar = new Text(searchBarContainer, SWT.SINGLE);
		GridDataFactory.fillDefaults().grab(true, false).align(GridData.FILL, GridData.END).applyTo(searchBar);
		searchBar.forceFocus();
		searchBar.selectAll();
		searchBar.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if (!isWholeWord(getFindString())) {
					wholeWordSearchButton.setEnabled(false);
				} else {
					wholeWordSearchButton.setEnabled(true);
				}

				showUserFeedback(normalTextForegroundColor);
				if (!getFindString().equals(findReplaceLogic.getTarget().getSelectionText())) { // don't perform
					// incremental search if we are already on the word.
					updateIncrementalSearch();
				}
			}
		});
		searchBar.addFocusListener(new FocusListener() {

			@Override
			public void focusGained(FocusEvent e) {
				// we want to update the base-location of where we start incremental search
				// to the currently selected position in the target
				// when coming back into the dialog
				findReplaceLogic.activate(SearchOptions.INCREMENTAL);
			}

			@Override
			public void focusLost(FocusEvent e) {
				// Do nothing
			}

		});
		searchBar.addKeyListener(shortcuts);
		searchBar.setMessage("Find"); //$NON-NLS-1$
	}

	private void updateIncrementalSearch() {
		// clear the current incrementally searched selection to avoid having an old
		// selection left when incrementally searching for an invalid string
		if (findReplaceLogic.getTarget() instanceof IFindReplaceTargetExtension targetExtension) {
			targetExtension.setSelection(targetExtension.getLineSelection().x, 0);
		}
		findReplaceLogic.performIncrementalSearch(getFindString());
		evaluateFindReplacerStatus();
	}

	private void createReplaceBar() {
		replaceBar = new Text(replaceBarContainer, SWT.SINGLE);
		GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.END).applyTo(replaceBar);
		replaceBar.setMessage("Replace"); //$NON-NLS-1$
		replaceBar.addKeyListener(shortcuts);
	}

	private void createFindContainer() {
		searchContainer = new Composite(contentGroup, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).align(GridData.FILL, GridData.FILL).applyTo(searchContainer);
		GridLayoutFactory.fillDefaults().numColumns(2).extendedMargins(4, 4, 2, 8).equalWidth(false)
				.applyTo(searchContainer);
		searchContainer.setBackground(getShell().getDisplay().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		searchBarContainer = new Composite(searchContainer, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).align(GridData.FILL, GridData.END).applyTo(searchBarContainer);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(searchBarContainer);
	}

	private void createReplaceContainer() {
		replaceContainer = new Composite(contentGroup, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).align(GridData.FILL, GridData.FILL).applyTo(replaceContainer);
		GridLayoutFactory.fillDefaults().margins(0, 1).numColumns(2).extendedMargins(4, 4, 2, 8).equalWidth(false)
				.applyTo(replaceContainer);
		replaceContainer.setBackground(getShell().getDisplay().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		replaceBarContainer = new Composite(replaceContainer, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).align(GridData.FILL, GridData.END).applyTo(replaceBarContainer);
		GridLayoutFactory.fillDefaults().numColumns(1).equalWidth(false).applyTo(replaceBarContainer);
	}

	private void createMainContainer(final Composite parent) {
		container = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(false).margins(2, 2).spacing(2, 0).applyTo(container);
		GridDataFactory.fillDefaults().grab(true, true).align(GridData.FILL, GridData.FILL).applyTo(container);

		if (findReplaceLogic.getTarget().isEditable()) {
			createReplaceToggle();
		}

		contentGroup = new Composite(container, SWT.NULL);
		GridLayoutFactory.fillDefaults().numColumns(1).equalWidth(false).spacing(2, 3).applyTo(contentGroup);
		GridDataFactory.fillDefaults().grab(true, true).align(GridData.FILL, GridData.FILL).applyTo(contentGroup);
	}

	private void createReplaceToggle() {
		replaceToggle = new Button(container, SWT.PUSH); // https://stackoverflow.com/questions/33161797/how-to-remove-border-of-swt-button-so-that-it-seems-like-a-label
		GridDataFactory.fillDefaults().grab(false, true).align(GridData.BEGINNING, GridData.FILL)
				.applyTo(replaceToggle);
		replaceToggle.setToolTipText("Toggle input for replace (Ctrl+R)"); //$NON-NLS-1$
		replaceToggle.setImage(FindReplaceOverlayImages.get(FindReplaceOverlayImages.OBJ_OPEN_REPLACE));
		replaceToggle.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!replaceBarOpen) {
					createReplaceDialog();
					replaceToggle.setImage(FindReplaceOverlayImages.get(FindReplaceOverlayImages.OBJ_CLOSE_REPLACE));
				} else {
					hideReplace();
					replaceToggle.setImage(FindReplaceOverlayImages.get(FindReplaceOverlayImages.OBJ_OPEN_REPLACE));
				}
				replaceToggle.setSelection(false); // We don't want the button to look "locked in", hence why we don't
													// use it's selectionState
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// Do nothing
			}
		});
	}

	public void hideReplace() {
		if (!replaceBarOpen) {
			return;
		}
		replaceBarOpen = false;
		replaceContainer.dispose();
		replaceTools.dispose();
		replaceBar.dispose();
		positionToPart();
		searchBar.forceFocus();
	}

	public void createReplaceDialog() {
		if (replaceBarOpen) {
			return;
		}
		replaceBarOpen = true;
		createReplaceContainer();
		createReplaceBar();
		createReplaceTools();
		positionToPart();
		replaceBar.forceFocus();
		applyOverlayColors(backgroundToUse, true);
	}

	private void enableSearchTools(boolean enable) {
		((GridData) searchTools.getLayoutData()).exclude = !enable;
		searchTools.setVisible(enable);

		if (enable) {
			((GridLayout) searchTools.getParent().getLayout()).numColumns = 2;
		} else {
			((GridLayout) searchTools.getParent().getLayout()).numColumns = 1;
		}
	}

	private void enableReplaceToggle(boolean enable) {
		if (!okayToUse(replaceToggle)) {
			return;
		}
		((GridData) replaceToggle.getLayoutData()).exclude = !enable;
		replaceToggle.setVisible(enable);
	}

	private void enableReplaceTools(boolean enable) {
		if (!okayToUse(replaceTools)) {
			return;
		}
		((GridData) replaceTools.getLayoutData()).exclude = !enable;
		replaceTools.setVisible(enable);

		if (enable) {
			((GridLayout) replaceTools.getParent().getLayout()).numColumns = 2;
		} else {
			((GridLayout) replaceTools.getParent().getLayout()).numColumns = 1;
		}
	}

	private int getIdealDialogWidth(Rectangle targetBounds) {
		enableSearchTools(true);
		enableReplaceTools(true);
		enableReplaceToggle(true);
		Point toolBarWidth = searchTools.getSize();
		GC gc = new GC(searchBar);
		gc.setFont(searchBar.getFont());
		Point idealWidth = gc.stringExtent("THIS TEXT HAS A REASONABLE LENGTH FOR SEARCHING"); //$NON-NLS-1$
		Point idealCompromiseWidth = gc.stringExtent("THIS TEXT HAS A REASONABLE"); //$NON-NLS-1$
		Point worstCompromiseWidth = gc.stringExtent("THIS TEXT "); //$NON-NLS-1$

		int newWidth = idealWidth.x + toolBarWidth.x;
		if (newWidth > targetBounds.width * 0.7) {
			newWidth = (int) (targetBounds.width * 0.7);
		}
		if (newWidth < idealCompromiseWidth.x + toolBarWidth.x) {
			enableSearchTools(false);
			enableReplaceTools(false);
		}
		if (newWidth < worstCompromiseWidth.x + toolBarWidth.x) {
			newWidth = (int) (targetBounds.width * 0.95);
			enableReplaceToggle(false);
		}
		return newWidth;
	}

	private Point getNewPosition(Widget targetTextWidget, Point targetOrigin, Rectangle targetBounds) {
		Point scrollBarSize = ((Scrollable) targetTextWidget).getVerticalBar().getSize();

		int newX = targetOrigin.x + targetBounds.width - container.getBounds().width - scrollBarSize.x
				- ((StyledText) targetTextWidget).getRightMargin();
		int newY = targetOrigin.y;
		return new Point(newX, newY);
	}

	/**
	 * When making the text-bar 100% small and then regrowing it, we want the text
	 * to start at the first character again.
	 */
	private void repositionTextSelection() {
		if (okayToUse(searchBar) && !searchBar.isFocusControl()) {
			searchBar.setSelection(0, 0);
		}
		if (okayToUse(replaceBar) && !replaceBar.isFocusControl()) {
			replaceBar.setSelection(0, 0);
		}
	}

	public void positionToPart() {
		getShell().layout(true);
		container.layout(true);
		if (!(targetPart instanceof StatusTextEditor)) {
			return;
		}

		StatusTextEditor textEditor = (StatusTextEditor) targetPart;
		Control targetWidget = textEditor.getSourceViewer().getTextWidget();
		if (targetWidget == null || targetWidget.isDisposed()) {
			this.close();
			return;
		}
		targetWidget = textEditor.getSourceViewer().getTextWidget();

		Point targetOrigin = targetWidget.toDisplay(0, 0);
		Rectangle targetBounds = targetWidget.getBounds();

		int newWidth = getIdealDialogWidth(targetBounds);
		int newHeight = container.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
		Point newPosition = getNewPosition(targetWidget, targetOrigin, targetBounds);

		getShell().setSize(new Point(newWidth, newHeight));
		getShell().setLocation(newPosition);

		repositionTextSelection();
		container.layout(true);
		getShell().layout(true);
	}

	private String getFindString() {
		return searchBar.getText();
	}

	private String getReplaceString() {
		if (replaceBar.isDisposed()) {
			return ""; //$NON-NLS-1$
		}
		return replaceBar.getText();

	}

	private void performSingleReplace() {
		findReplaceLogic.performReplaceAndFind(getFindString(), getReplaceString());
		evaluateFindReplacerStatus();
	}

	private void initFindStringFromSelection() {
		String initText = findReplaceLogic.getTarget().getSelectionText();
		if (initText.isEmpty()) {
			return;
		}
		if (initText.contains("\n")) { //$NON-NLS-1$
			findReplaceLogic.deactivate(SearchOptions.GLOBAL);
			searchInSelectionButton.setSelection(true);
		} else {
			searchBar.setText(initText);
			searchBar.setSelection(0, initText.length());
		}
	}

	private void evaluateFindReplacerStatus() {
		Color warningColor = JFaceColors.getErrorText(getShell().getDisplay());
		IFindReplaceStatus status = findReplaceLogic.getStatus();
		showUserFeedback(normalTextForegroundColor);

		if (status instanceof InvalidRegExStatus) {
			showUserFeedback(warningColor);
		}

		if (status instanceof FindStatus statusMessage) {
			switch (statusMessage.getMessageCode()) {
			case NO_MATCH:
				showUserFeedback(warningColor);
				//$FALL-THROUGH$
			case READONLY:
			case WRAPPED:
				tryToBeep();
				break;
			default:
				break;
			}
		}
	}

	private void showUserFeedback(Color feedbackColor) {
		boolean colorReplaceBar = replaceBar != null && !replaceBar.isDisposed() && replaceBar.isFocusControl();
		searchBar.setForeground(feedbackColor);
		if (colorReplaceBar) {
			replaceBar.setForeground(feedbackColor);
		}
	}

	private void tryToBeep() {
		Shell dialogShell = getShell();
		if (dialogShell != null && !dialogShell.isDisposed()) {
			getShell().getDisplay().beep();
		}
	}

	private void activateInFindReplacerIf(SearchOptions option, boolean shouldActivate) {
		if (shouldActivate) {
			findReplaceLogic.activate(option);
		} else {
			findReplaceLogic.deactivate(option);
		}
	}

	private boolean okayToUse(Widget widget) {
		return widget != null && !widget.isDisposed();
	}

	private boolean isWholeWord(String findString) {
		return !findString.contains(" "); //$NON-NLS-1$
	}
}