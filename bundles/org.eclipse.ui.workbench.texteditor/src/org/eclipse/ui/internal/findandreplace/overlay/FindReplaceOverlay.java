/*******************************************************************************
 * Copyright (c) 2024 Vector Informatik GmbH and others.
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
package org.eclipse.ui.internal.findandreplace.overlay;

import org.osgi.framework.FrameworkUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGBA;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Scrollable;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.jface.window.Window;

import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.IFindReplaceTargetExtension;
import org.eclipse.jface.text.ITextViewer;

import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.findandreplace.FindReplaceLogic;
import org.eclipse.ui.internal.findandreplace.FindReplaceMessages;
import org.eclipse.ui.internal.findandreplace.SearchOptions;
import org.eclipse.ui.internal.findandreplace.status.IFindReplaceStatus;

import org.eclipse.ui.texteditor.StatusTextEditor;

/**
 * @since 3.17
 */
public class FindReplaceOverlay extends Dialog {

	private static final String REPLACE_BAR_OPEN_DIALOG_SETTING = "replaceBarOpen"; //$NON-NLS-1$
	private static final double WORST_CASE_RATIO_EDITOR_TO_OVERLAY = 0.95;
	private static final double BIG_WIDTH_RATIO_EDITOR_TO_OVERLAY = 0.7;
	private static final String MINIMAL_WIDTH_TEXT = "THIS TEXT IS SHORT "; //$NON-NLS-1$
	private static final String IDEAL_WIDTH_TEXT = "THIS TEXT HAS A REASONABLE LENGTH FOR SEARCHING"; //$NON-NLS-1$
	private FindReplaceLogic findReplaceLogic;
	private IWorkbenchPart targetPart;
	private boolean overlayOpen;
	private boolean replaceBarOpen;

	private Composite container;
	private Button replaceToggle;

	private Composite contentGroup;

	private Composite searchContainer;
	private Composite searchBarContainer;
	private Text searchBar;
	private AccessibleToolBar searchTools;
	private ToolItem searchInSelectionButton;
	private ToolItem wholeWordSearchButton;
	private ToolItem caseSensitiveSearchButton;
	private ToolItem regexSearchButton;
	private ToolItem searchUpButton;
	private ToolItem searchDownButton;
	private ToolItem searchAllButton;
	private AccessibleToolBar closeTools;
	private ToolItem closeButton;

	private Composite replaceContainer;
	private Composite replaceBarContainer;
	private Text replaceBar;
	private AccessibleToolBar replaceTools;
	private ToolItem replaceButton;
	private ToolItem replaceAllButton;

	private Color backgroundToUse;
	private Color normalTextForegroundColor;
	private boolean positionAtTop = true;

	public FindReplaceOverlay(Shell parent, IWorkbenchPart part, IFindReplaceTarget target) {
		super(parent);
		createFindReplaceLogic(target);

		setShellStyle(SWT.MODELESS);
		setBlockOnOpen(false);
		targetPart = part;

	}

	@Override
	protected boolean isResizable() {
		return false;
	}

	private void createFindReplaceLogic(IFindReplaceTarget target) {
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

	private void performReplaceAll() {
		BusyIndicator.showWhile(getShell() != null ? getShell().getDisplay() : Display.getCurrent(),
				() -> findReplaceLogic.performReplaceAll(getFindString(), getReplaceString()));
		evaluateFindReplaceStatus();
	}

	private void performSelectAll() {
		BusyIndicator.showWhile(getShell() != null ? getShell().getDisplay() : Display.getCurrent(),
				() -> findReplaceLogic.performSelectAll(getFindString()));
		evaluateFindReplaceStatus();
	}

	private KeyListener shortcuts = KeyListener.keyPressedAdapter(e -> {
		e.doit = false;
		if ((e.stateMask & SWT.CTRL) != 0 && (e.keyCode == 'F' || e.keyCode == 'f')) {
			close();
		} else if ((e.stateMask & SWT.CTRL) != 0 && (e.keyCode == 'R' || e.keyCode == 'r')) {
			toggleReplace();
		} else if ((e.stateMask & SWT.CTRL) != 0 && (e.keyCode == 'W' || e.keyCode == 'w')) {
			toggleToolItem(wholeWordSearchButton);
		} else if ((e.stateMask & SWT.CTRL) != 0 && (e.keyCode == 'P' || e.keyCode == 'p')) {
			toggleToolItem(regexSearchButton);
		} else if ((e.stateMask & SWT.CTRL) != 0 && (e.keyCode == 'A' || e.keyCode == 'a')) {
			toggleToolItem(searchInSelectionButton);
		} else if ((e.stateMask & SWT.CTRL) != 0 && (e.keyCode == 'C' || e.keyCode == 'c')) {
			toggleToolItem(caseSensitiveSearchButton);
		} else if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR) {
			performEnterAction(e);
		} else if (e.keyCode == SWT.ESC) {
			close();
		} else {
			e.doit = true;
		}
	});

	private void performEnterAction(KeyEvent e) {
		boolean isShiftPressed = (e.stateMask & SWT.SHIFT) != 0;
		boolean isCtrlPressed = (e.stateMask & SWT.CTRL) != 0;
		if (okayToUse(replaceBar) && replaceBar.isFocusControl()) {
			if (isCtrlPressed) {
				performReplaceAll();
			} else {
				performSingleReplace();
			}
		} else {
			if (isCtrlPressed) {
				performSelectAll();
			} else {
				performSearch(!isShiftPressed);
			}
		}
	}

	private void toggleToolItem(ToolItem toolItem) {
		toolItem.setSelection(!toolItem.getSelection());
		toolItem.notifyListeners(SWT.Selection, null);
	}

	private ControlListener shellMovementListener = new ControlListener() {
		@Override
		public void controlMoved(ControlEvent e) {
			if (getShell() != null) {
				getShell().getDisplay().asyncExec(() -> positionToPart());
			}
		}

		@Override
		public void controlResized(ControlEvent e) {
			if (getShell() != null) {
				getShell().getDisplay().asyncExec(() -> positionToPart());
			}
		}
	};

	private ShellAdapter overlayDeactivationListener = new ShellAdapter() {
		@Override
		public void shellActivated(ShellEvent e) {
			// Do nothing
		}

		@Override
		public void shellDeactivated(ShellEvent e) {
			removeSearchScope();
		}
	};

	private PaintListener widgetMovementListener = __ -> positionToPart();

	private IPartListener partListener = new IPartListener() {
		@Override
		public void partActivated(IWorkbenchPart part) {
			getShell().getDisplay().asyncExec(this::adaptToPartActivationChange);
		}

		@Override
		public void partDeactivated(IWorkbenchPart part) {
			getShell().getDisplay().asyncExec(this::adaptToPartActivationChange);
		}

		@Override
		public void partBroughtToTop(IWorkbenchPart part) {
			getShell().getDisplay().asyncExec(this::adaptToPartActivationChange);
		}

		@Override
		public void partClosed(IWorkbenchPart part) {
			close();
		}

		@Override
		public void partOpened(IWorkbenchPart part) {
			// Do nothing
		}

		private void adaptToPartActivationChange() {
			if (getShell() == null || targetPart.getSite().getPart() == null) {
				return;
			}

			boolean isOverlayVisible = isPartCurrentlyDisplayedInPartSash();

			getShell().setVisible(isOverlayVisible);

			if (isOverlayVisible) {
				targetPart.getSite().getShell().setActive();
				targetPart.setFocus();
				getShell().getDisplay().asyncExec(this::focusTargetWidget);
			}
		}

		private void focusTargetWidget() {
			if (getShell() == null || targetPart.getSite().getPart() == null) {
				return;
			}
			if (!(targetPart instanceof StatusTextEditor)) {
				return;
			}
			StatusTextEditor textEditor = (StatusTextEditor) targetPart;
			Control targetWidget = textEditor.getAdapter(ITextViewer.class).getTextWidget();

			targetWidget.forceFocus();
		}
	};
	private KeyListener closeOnTargetEscapeListener = KeyListener.keyPressedAdapter(c -> {
		if (c.keyCode == SWT.ESC) {
			this.close();
		}
	});

	private boolean isPartCurrentlyDisplayedInPartSash() {
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

	/**
	 * Returns the dialog settings object used to share state between several
	 * find/replace overlays.
	 *
	 * @return the dialog settings to be used
	 */
	private static IDialogSettings getDialogSettings() {
		IDialogSettings settings = PlatformUI
				.getDialogSettingsProvider(FrameworkUtil.getBundle(FindReplaceOverlay.class)).getDialogSettings();
		return settings;
	}

	@Override
	public boolean close() {
		if (!overlayOpen) {
			return true;
		}
		storeOverlaySettings();

		findReplaceLogic.activate(SearchOptions.GLOBAL);
		overlayOpen = false;
		replaceBarOpen = false;
		unbindListeners();
		container.dispose();
		return super.close();
	}

	@Override
	public int open() {
		int returnCode = Window.OK;
		if (!overlayOpen) {
			returnCode = super.open();
			bindListeners();
			restoreOverlaySettings();
		}
		overlayOpen = true;
		applyOverlayColors(backgroundToUse, true);
		updateFromTargetSelection();

		getShell().layout();
		positionToPart();

		searchBar.forceFocus();
		return returnCode;
	}

	private void storeOverlaySettings() {
		getDialogSettings().put(REPLACE_BAR_OPEN_DIALOG_SETTING, replaceBarOpen);
	}

	private void restoreOverlaySettings() {
		Boolean shouldOpenReplaceBar = getDialogSettings().getBoolean(REPLACE_BAR_OPEN_DIALOG_SETTING);
		if (shouldOpenReplaceBar) {
			toggleReplace();
		}
	}

	private void applyOverlayColors(Color color, boolean tryToColorReplaceBar) {
		closeTools.setBackground(color);
		closeButton.setBackground(color);

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
		getShell().removeShellListener(overlayDeactivationListener);
		if (targetPart != null && targetPart instanceof StatusTextEditor textEditor) {
			Control targetWidget = textEditor.getAdapter(ITextViewer.class).getTextWidget();
			if (targetWidget != null) {
				targetWidget.getShell().removeControlListener(shellMovementListener);
				targetWidget.removePaintListener(widgetMovementListener);
				targetWidget.removeKeyListener(closeOnTargetEscapeListener);
				targetPart.getSite().getPage().removePartListener(partListener);
			}
		}
	}

	private void bindListeners() {
		getShell().addShellListener(overlayDeactivationListener);
		if (targetPart instanceof StatusTextEditor textEditor) {
			Control targetWidget = textEditor.getAdapter(ITextViewer.class).getTextWidget();

			targetWidget.getShell().addControlListener(shellMovementListener);
			targetWidget.addPaintListener(widgetMovementListener);
			targetWidget.addKeyListener(closeOnTargetEscapeListener);
			targetPart.getSite().getPage().addPartListener(partListener);
		}
	}

	@Override
	public Control createContents(Composite parent) {
		backgroundToUse = new Color(getShell().getDisplay(), new RGBA(0, 0, 0, 0));
		Control ret = createDialog(parent);

		getShell().layout();
		positionToPart();
		return ret;
	}

	private Control createDialog(final Composite parent) {
		createMainContainer(parent);

		retrieveBackgroundColor();

		createFindContainer();
		createSearchBar();
		createSearchTools();
		createCloseTools();

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
		backgroundToUse = textBarForRetrievingTheRightColor.getBackground();
		normalTextForegroundColor = textBarForRetrievingTheRightColor.getForeground();
		textBarForRetrievingTheRightColor.dispose();
	}

	private void createSearchTools() {
		searchTools = new AccessibleToolBar(searchContainer);
		GridDataFactory.fillDefaults().grab(false, true).align(GridData.END, GridData.END).applyTo(searchTools);

		createCaseSensitiveButton();
		createRegexSearchButton();
		createWholeWordsButton();
		createAreaSearchButton();

		@SuppressWarnings("unused")
		ToolItem separator = searchTools.createToolItem(SWT.SEPARATOR);

		searchUpButton = new AccessibleToolItemBuilder(searchTools).withStyleBits(SWT.PUSH)
				.withImage(FindReplaceOverlayImages.get(FindReplaceOverlayImages.KEY_FIND_PREV))
				.withToolTipText(FindReplaceMessages.FindReplaceOverlay_upSearchButton_toolTip)
				.withSelectionListener(SelectionListener.widgetSelectedAdapter(e -> performSearch(false))).build();
		searchDownButton = new AccessibleToolItemBuilder(searchTools).withStyleBits(SWT.PUSH)
				.withImage(FindReplaceOverlayImages.get(FindReplaceOverlayImages.KEY_FIND_NEXT))
				.withToolTipText(FindReplaceMessages.FindReplaceOverlay_downSearchButton_toolTip)
				.withSelectionListener(SelectionListener.widgetSelectedAdapter(e -> performSearch(true))).build();
		searchDownButton.setSelection(true); // by default, search down

		searchAllButton = new AccessibleToolItemBuilder(searchTools).withStyleBits(SWT.PUSH)
				.withImage(FindReplaceOverlayImages.get(FindReplaceOverlayImages.KEY_SEARCH_ALL))
				.withToolTipText(FindReplaceMessages.FindReplaceOverlay_searchAllButton_toolTip)
				.withSelectionListener(SelectionListener.widgetSelectedAdapter(e -> performSelectAll())).build();
	}

	private void createCloseTools() {
		closeTools = new AccessibleToolBar(searchContainer);
		GridDataFactory.fillDefaults().grab(false, true).align(GridData.END, GridData.END).applyTo(closeTools);

		closeButton = new AccessibleToolItemBuilder(closeTools).withStyleBits(SWT.PUSH)
				.withImage(FindReplaceOverlayImages.get(FindReplaceOverlayImages.KEY_CLOSE))
				.withToolTipText(FindReplaceMessages.FindReplaceOverlay_closeButton_toolTip)
				.withSelectionListener(SelectionListener.widgetSelectedAdapter(e -> close())).build();
	}

	private void createAreaSearchButton() {
		searchInSelectionButton = new AccessibleToolItemBuilder(searchTools).withStyleBits(SWT.CHECK)
				.withImage(FindReplaceOverlayImages.get(FindReplaceOverlayImages.KEY_SEARCH_IN_AREA))
				.withToolTipText(FindReplaceMessages.FindReplaceOverlay_searchInSelectionButton_toolTip)
				.withSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
					activateInFindReplacerIf(SearchOptions.GLOBAL, !searchInSelectionButton.getSelection());
					updateIncrementalSearch();
				})).build();
		searchInSelectionButton.setSelection(findReplaceLogic.isActive(SearchOptions.WHOLE_WORD));
	}

	private void createRegexSearchButton() {
		regexSearchButton = new AccessibleToolItemBuilder(searchTools).withStyleBits(SWT.CHECK)
				.withImage(FindReplaceOverlayImages.get(FindReplaceOverlayImages.KEY_FIND_REGEX))
				.withToolTipText(FindReplaceMessages.FindReplaceOverlay_regexSearchButton_toolTip)
				.withSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
					activateInFindReplacerIf(SearchOptions.REGEX, regexSearchButton.getSelection());
					wholeWordSearchButton.setEnabled(!findReplaceLogic.isActive(SearchOptions.REGEX));
					updateIncrementalSearch();
				})).build();
		regexSearchButton.setSelection(findReplaceLogic.isActive(SearchOptions.REGEX));
	}

	private void createCaseSensitiveButton() {
		caseSensitiveSearchButton = new AccessibleToolItemBuilder(searchTools).withStyleBits(SWT.CHECK)
				.withImage(FindReplaceOverlayImages.get(FindReplaceOverlayImages.KEY_CASE_SENSITIVE))
				.withToolTipText(FindReplaceMessages.FindReplaceOverlay_caseSensitiveButton_toolTip)
				.withSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
					activateInFindReplacerIf(SearchOptions.CASE_SENSITIVE, caseSensitiveSearchButton.getSelection());
					updateIncrementalSearch();
				})).build();
		caseSensitiveSearchButton.setSelection(findReplaceLogic.isActive(SearchOptions.CASE_SENSITIVE));
	}

	private void createWholeWordsButton() {
		wholeWordSearchButton = new AccessibleToolItemBuilder(searchTools).withStyleBits(SWT.CHECK)
				.withImage(FindReplaceOverlayImages.get(FindReplaceOverlayImages.KEY_WHOLE_WORD))
				.withToolTipText(FindReplaceMessages.FindReplaceOverlay_wholeWordsButton_toolTip)
				.withSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
					activateInFindReplacerIf(SearchOptions.WHOLE_WORD, wholeWordSearchButton.getSelection());
					updateIncrementalSearch();
				})).build();
		wholeWordSearchButton.setSelection(findReplaceLogic.isActive(SearchOptions.WHOLE_WORD));
	}

	private void createReplaceTools() {
		Color warningColor = JFaceColors.getErrorText(getShell().getDisplay());

		replaceTools = new AccessibleToolBar(replaceContainer);
		GridDataFactory.fillDefaults().grab(false, true).align(GridData.CENTER, GridData.END).applyTo(replaceTools);
		replaceButton = new AccessibleToolItemBuilder(replaceTools).withStyleBits(SWT.PUSH)
				.withImage(FindReplaceOverlayImages.get(FindReplaceOverlayImages.KEY_REPLACE))
				.withToolTipText(FindReplaceMessages.FindReplaceOverlay_replaceButton_toolTip)
				.withSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
					if (getFindString().isEmpty()) {
						showUserFeedback(warningColor, true);
						return;
					}
					performSingleReplace();
				})).build();

		replaceAllButton = new AccessibleToolItemBuilder(replaceTools).withStyleBits(SWT.PUSH)
				.withImage(FindReplaceOverlayImages.get(FindReplaceOverlayImages.KEY_REPLACE_ALL))
				.withToolTipText(FindReplaceMessages.FindReplaceOverlay_replaceAllButton_toolTip)
				.withSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
					if (getFindString().isEmpty()) {
						showUserFeedback(warningColor, true);
						return;
					}
					performReplaceAll();
				})).build();
	}

	private void createSearchBar() {
		searchBar = new Text(searchBarContainer, SWT.SINGLE);
		GridDataFactory.fillDefaults().grab(true, false).align(GridData.FILL, GridData.END).applyTo(searchBar);
		searchBar.forceFocus();
		searchBar.selectAll();
		searchBar.addModifyListener(e -> {
			wholeWordSearchButton.setEnabled(findReplaceLogic.isWholeWordSearchAvailable(getFindString()));

			showUserFeedback(normalTextForegroundColor, true);
			// don't perform incremental search if we are already on the word.
			if (!getFindString().equals(findReplaceLogic.getTarget().getSelectionText())) {
				updateIncrementalSearch();
			}
		});
		searchBar.addFocusListener(new FocusListener() {

			@Override
			public void focusGained(FocusEvent e) {
				// we want to update the base-location of where we start incremental search
				// to the currently selected position in the target
				// when coming back into the dialog
				findReplaceLogic.deactivate(SearchOptions.INCREMENTAL);
				findReplaceLogic.activate(SearchOptions.INCREMENTAL);
			}

			@Override
			public void focusLost(FocusEvent e) {
				showUserFeedback(normalTextForegroundColor, false);
			}

		});
		searchBar.addKeyListener(shortcuts);
		searchBar.setMessage(FindReplaceMessages.FindReplaceOverlay_searchBar_message);
	}

	private void updateIncrementalSearch() {
		// clear the current incrementally searched selection to avoid having an old
		// selection left when incrementally searching for an invalid string
		if (findReplaceLogic.getTarget() instanceof IFindReplaceTargetExtension targetExtension) {
			targetExtension.setSelection(targetExtension.getLineSelection().x, 0);
		}
		findReplaceLogic.performIncrementalSearch(getFindString());
		evaluateFindReplaceStatus();
	}

	private void createReplaceBar() {
		replaceBar = new Text(replaceBarContainer, SWT.SINGLE);
		GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.END).applyTo(replaceBar);
		replaceBar.setMessage(FindReplaceMessages.FindReplaceOverlay_replaceBar_message);
		replaceBar.addFocusListener(FocusListener.focusLostAdapter(e -> {
			replaceBar.setForeground(normalTextForegroundColor);
			searchBar.setForeground(normalTextForegroundColor);
		}));
		replaceBar.addKeyListener(shortcuts);
	}

	private void createFindContainer() {
		searchContainer = new Composite(contentGroup, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).align(GridData.FILL, GridData.FILL).applyTo(searchContainer);
		GridLayoutFactory.fillDefaults().numColumns(3).extendedMargins(4, 4, 2, 8).equalWidth(false)
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

		createReplaceToggle();

		contentGroup = new Composite(container, SWT.NULL);
		GridLayoutFactory.fillDefaults().numColumns(1).equalWidth(false).spacing(2, 3).applyTo(contentGroup);
		GridDataFactory.fillDefaults().grab(true, true).align(GridData.FILL, GridData.FILL).applyTo(contentGroup);
	}

	private void createReplaceToggle() {
		replaceToggle = new Button(container, SWT.FLAT | SWT.PUSH);
		GridDataFactory.fillDefaults().grab(false, true).align(GridData.BEGINNING, GridData.FILL)
				.applyTo(replaceToggle);
		replaceToggle.setToolTipText(FindReplaceMessages.FindReplaceOverlay_replaceToggle_toolTip);
		replaceToggle.setImage(FindReplaceOverlayImages.get(FindReplaceOverlayImages.KEY_OPEN_REPLACE_AREA));
		replaceToggle.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> toggleReplace()));
	}

	private void toggleReplace() {
		if (!replaceBarOpen && findReplaceLogic.getTarget().isEditable()) {
			createReplaceDialog();
			replaceToggle.setImage(FindReplaceOverlayImages.get(FindReplaceOverlayImages.KEY_CLOSE_REPLACE_AREA));
		} else {
			hideReplace();
			replaceToggle.setImage(FindReplaceOverlayImages.get(FindReplaceOverlayImages.KEY_OPEN_REPLACE_AREA));
		}
		replaceToggle.setSelection(false); // We don't want the button to look "locked in", so don't
											// use it's selectionState
	}

	private void hideReplace() {
		if (!replaceBarOpen) {
			return;
		}
		searchBar.forceFocus();
		replaceBarOpen = false;
		replaceContainer.dispose();
		positionToPart();
	}

	private void createReplaceDialog() {
		if (replaceBarOpen) {
			return;
		}
		replaceBarOpen = true;
		createReplaceContainer();
		createReplaceBar();
		createReplaceTools();
		positionToPart();
		applyOverlayColors(backgroundToUse, true);
		replaceBar.forceFocus();
	}

	private void enableSearchTools(boolean enable) {
		((GridData) searchTools.getLayoutData()).exclude = !enable;
		searchTools.setVisible(enable);
	}

	private void enableReplaceToggle(boolean enable) {
		if (!okayToUse(replaceToggle)) {
			return;
		}
		boolean visible = enable && findReplaceLogic.getTarget().isEditable();
		((GridData) replaceToggle.getLayoutData()).exclude = !visible;
		replaceToggle.setVisible(visible);
	}

	private void enableReplaceTools(boolean enable) {
		if (!okayToUse(replaceTools)) {
			return;
		}
		((GridData) replaceTools.getLayoutData()).exclude = !enable;
		replaceTools.setVisible(enable);
	}

	private int getIdealOverlayWidth(Rectangle targetBounds) {
		int idealOverlayWidth = calculateOverlayWidthWithToolbars(IDEAL_WIDTH_TEXT);
		int minimumOverlayWidth = Math.min(calculateOverlayWidthWithoutToolbars(MINIMAL_WIDTH_TEXT),
				(int) (targetBounds.width * WORST_CASE_RATIO_EDITOR_TO_OVERLAY));
		int maximumOverlayWidth = (int) (targetBounds.width * BIG_WIDTH_RATIO_EDITOR_TO_OVERLAY);

		int overlayWidth = idealOverlayWidth;
		if (overlayWidth > maximumOverlayWidth) {
			overlayWidth = maximumOverlayWidth;
		}
		if (overlayWidth < minimumOverlayWidth) {
			overlayWidth = minimumOverlayWidth;
		}

		return overlayWidth;
	}

	private void configureDisplayedWidgetsForWidth(int overlayWidth) {
		int minimumWidthWithToolbars = calculateOverlayWidthWithoutToolbars(IDEAL_WIDTH_TEXT);
		int minimumWidthWithReplaceToggle = calculateOverlayWidthWithoutToolbars(MINIMAL_WIDTH_TEXT);
		enableSearchTools(overlayWidth >= minimumWidthWithToolbars);
		enableReplaceTools(overlayWidth >= minimumWidthWithToolbars);
		enableReplaceToggle(overlayWidth >= minimumWidthWithReplaceToggle);
	}

	private int calculateOverlayWidthWithToolbars(String searchInput) {
		int toolbarWidth = searchTools.getSize().x;
		return calculateOverlayWidthWithoutToolbars(searchInput) + toolbarWidth;
	}

	private int calculateOverlayWidthWithoutToolbars(String searchInput) {
		int replaceToggleWidth = 0;
		if (okayToUse(replaceToggle)) {
			replaceToggleWidth = replaceToggle.getBounds().width;
		}
		int closeButtonWidth = closeTools.getSize().x;
		int searchInputWidth = getTextWidthInSearchBar(searchInput);
		return replaceToggleWidth + closeButtonWidth + searchInputWidth;
	}

	private int getTextWidthInSearchBar(String input) {
		GC gc = new GC(searchBar);
		gc.setFont(searchBar.getFont());
		int textWidth = gc.stringExtent(input).x; // $NON-NLS-1$
		gc.dispose();
		return textWidth;
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

	private void positionToPart() {
		getShell().requestLayout();
		if (!(targetPart instanceof StatusTextEditor textEditor)) {
			return;
		}

		Control targetWidget = textEditor.getAdapter(ITextViewer.class).getTextWidget();
		if (!okayToUse(targetWidget)) {
			this.close();
			return;
		}

		Rectangle targetControlBounds = calculateAbsoluteControlBounds(targetWidget);
		Rectangle overlayBounds = calculateDesiredOverlayBounds(targetControlBounds);
		updatePosition(overlayBounds);
		configureDisplayedWidgetsForWidth(overlayBounds.width);
		updateVisibility(targetControlBounds, overlayBounds);

		repositionTextSelection();
	}

	private Rectangle calculateAbsoluteControlBounds(Control control) {
		Rectangle localControlBounds = control.getBounds();
		int width = localControlBounds.width;
		int height = localControlBounds.height;
		if (control instanceof Scrollable scrollable) {
			width -= scrollable.getVerticalBar().getSize().x;
			height -= scrollable.getHorizontalBar().getSize().y;
		}
		if (control instanceof StyledText styledText) {
			width -= styledText.getRightMargin();
		}
		Point absoluteControlPosition = control.toDisplay(0, 0);
		return new Rectangle(absoluteControlPosition.x, absoluteControlPosition.y, width, height);
	}

	private Rectangle calculateDesiredOverlayBounds(Rectangle targetControlBounds) {
		int width = getIdealOverlayWidth(targetControlBounds);
		int height = container.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;

		int x = targetControlBounds.x + targetControlBounds.width - width;
		int y = targetControlBounds.y;
		if (!positionAtTop) {
			y += targetControlBounds.height - height;
		}

		return new Rectangle(x, y, width, height);
	}

	private void updatePosition(Rectangle overlayBounds) {
		getShell().setSize(new Point(overlayBounds.width, overlayBounds.height));
		getShell().setLocation(new Point(overlayBounds.x, overlayBounds.y));
		getShell().layout(true);
	}

	private void updateVisibility(Rectangle targetControlBounds, Rectangle overlayBounds) {
		if (positionAtTop) {
			getShell().setVisible(
					overlayBounds.y + overlayBounds.height <= targetControlBounds.y + targetControlBounds.height);
		} else {
			getShell().setVisible(overlayBounds.y >= targetControlBounds.y);
		}
	}

	private String getFindString() {
		return searchBar.getText();
	}

	private String getReplaceString() {
		if (!okayToUse(replaceBar)) {
			return ""; //$NON-NLS-1$
		}
		return replaceBar.getText();

	}

	private void performSingleReplace() {
		findReplaceLogic.performReplaceAndFind(getFindString(), getReplaceString());
		evaluateFindReplaceStatus();
	}

	private void performSearch(boolean forward) {
		boolean oldForwardSearchSetting = findReplaceLogic.isActive(SearchOptions.FORWARD);
		activateInFindReplacerIf(SearchOptions.FORWARD, forward);
		findReplaceLogic.deactivate(SearchOptions.INCREMENTAL);
		findReplaceLogic.performSearch(getFindString());
		activateInFindReplacerIf(SearchOptions.FORWARD, oldForwardSearchSetting);
		findReplaceLogic.activate(SearchOptions.INCREMENTAL);
		evaluateFindReplaceStatus();
	}

	private void updateFromTargetSelection() {
		String selectionText = findReplaceLogic.getTarget().getSelectionText();
		if (selectionText.isEmpty()) {
			return;
		}
		if (selectionText.contains("\n")) { //$NON-NLS-1$
			findReplaceLogic.deactivate(SearchOptions.GLOBAL);
			searchInSelectionButton.setSelection(true);
		} else {
			if (findReplaceLogic.isRegExSearchAvailableAndActive()) {
				selectionText = FindReplaceDocumentAdapter.escapeForRegExPattern(selectionText);
			}
			searchBar.setText(selectionText);
			searchBar.setSelection(0, selectionText.length());
		}
	}

	private void evaluateFindReplaceStatus() {
		Color warningColor = JFaceColors.getErrorText(getShell().getDisplay());
		IFindReplaceStatus status = findReplaceLogic.getStatus();

		if (!status.wasSuccessful()) {
			boolean colorReplaceBar = okayToUse(replaceBar) && replaceBar.isFocusControl();
			showUserFeedback(warningColor, colorReplaceBar);
		} else {
			showUserFeedback(normalTextForegroundColor, false);
		}
	}

	private void showUserFeedback(Color feedbackColor, boolean colorReplaceBar) {
		searchBar.setForeground(feedbackColor);
		if (colorReplaceBar && okayToUse(replaceBar)) {
			replaceBar.setForeground(feedbackColor);
		}
	}

	private void activateInFindReplacerIf(SearchOptions option, boolean shouldActivate) {
		if (shouldActivate) {
			findReplaceLogic.activate(option);
		} else {
			findReplaceLogic.deactivate(option);
		}
	}

	private static boolean okayToUse(Widget widget) {
		return widget != null && !widget.isDisposed();
	}

	public void setPositionToTop(boolean shouldPositionOverlayOnTop) {
		positionAtTop = shouldPositionOverlayOnTop;
	}

	private void removeSearchScope() {
		findReplaceLogic.activate(SearchOptions.GLOBAL);
		searchInSelectionButton.setSelection(false);
	}
}