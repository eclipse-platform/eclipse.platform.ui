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

import static org.eclipse.ui.internal.findandreplace.overlay.FindReplaceShortcutUtil.registerActionShortcutsAtControl;

import java.util.List;
import java.util.function.Supplier;

import org.osgi.framework.FrameworkUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
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

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.jface.window.Window;

import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.IFindReplaceTargetExtension;
import org.eclipse.jface.text.ITextViewer;

import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.findandreplace.FindReplaceLogic;
import org.eclipse.ui.internal.findandreplace.FindReplaceMessages;
import org.eclipse.ui.internal.findandreplace.HistoryStore;
import org.eclipse.ui.internal.findandreplace.SearchOptions;
import org.eclipse.ui.internal.findandreplace.status.IFindReplaceStatus;
import org.eclipse.ui.part.MultiPageEditorSite;

import org.eclipse.ui.texteditor.IAbstractTextEditorHelpContextIds;
import org.eclipse.ui.texteditor.StatusTextEditor;

public class FindReplaceOverlay extends Dialog {
	private final class KeyboardShortcuts {
		private static final List<KeyStroke> SEARCH_FORWARD = List.of( //
				KeyStroke.getInstance(SWT.CR), KeyStroke.getInstance(SWT.KEYPAD_CR));
		private static final List<KeyStroke> SEARCH_BACKWARD = List.of( //
				KeyStroke.getInstance(SWT.SHIFT, SWT.CR), KeyStroke.getInstance(SWT.SHIFT, SWT.KEYPAD_CR));
		private static final List<KeyStroke> SEARCH_ALL = List.of( //
				KeyStroke.getInstance(SWT.MOD1, SWT.CR), KeyStroke.getInstance(SWT.MOD1, SWT.KEYPAD_CR));
		private static final List<KeyStroke> OPTION_CASE_SENSITIVE = List.of( //
				KeyStroke.getInstance(SWT.MOD1 | SWT.SHIFT, 'C'), KeyStroke.getInstance(SWT.MOD1 | SWT.SHIFT, 'c'));
		private static final List<KeyStroke> OPTION_WHOLE_WORD = List.of( //
				KeyStroke.getInstance(SWT.MOD1 | SWT.SHIFT, 'W'), KeyStroke.getInstance(SWT.MOD1 | SWT.SHIFT, 'w'));
		private static final List<KeyStroke> OPTION_REGEX = List.of( //
				KeyStroke.getInstance(SWT.MOD1 | SWT.SHIFT, 'P'), KeyStroke.getInstance(SWT.MOD1 | SWT.SHIFT, 'p'));
		private static final List<KeyStroke> OPTION_SEARCH_IN_SELECTION = List.of( //
				KeyStroke.getInstance(SWT.MOD1 | SWT.SHIFT, 'A'), KeyStroke.getInstance(SWT.MOD1 | SWT.SHIFT, 'a'));
		private static final List<KeyStroke> CLOSE = List.of( //
				KeyStroke.getInstance(SWT.ESC), KeyStroke.getInstance(SWT.MOD1, 'F'),
				KeyStroke.getInstance(SWT.MOD1, 'f'));
		private static final List<KeyStroke> TOGGLE_REPLACE = List.of( //
				KeyStroke.getInstance(SWT.MOD1, 'R'), KeyStroke.getInstance(SWT.MOD1, 'r'));
	}

	private static final String REPLACE_BAR_OPEN_DIALOG_SETTING = "replaceBarOpen"; //$NON-NLS-1$
	private static final double WORST_CASE_RATIO_EDITOR_TO_OVERLAY = 0.95;
	private static final double BIG_WIDTH_RATIO_EDITOR_TO_OVERLAY = 0.7;
	private static final String MINIMAL_WIDTH_TEXT = "THIS TEXT IS SHORT "; //$NON-NLS-1$
	private static final String IDEAL_WIDTH_TEXT = "THIS TEXT HAS A REASONABLE LENGTH FOR SEARCHING"; //$NON-NLS-1$
	private static final int HISTORY_SIZE = 15;

	private FindReplaceLogic findReplaceLogic;
	private final IWorkbenchPart targetPart;
	private boolean overlayOpen;
	private boolean replaceBarOpen;

	private Composite container;
	private Button replaceToggle;
	private FindReplaceOverlayAction replaceToggleShortcut;

	private Composite contentGroup;

	private Composite searchContainer;
	private Composite searchBarContainer;
	private HistoryTextWrapper searchBar;
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
	private HistoryTextWrapper replaceBar;
	private AccessibleToolBar replaceTools;
	private ToolItem replaceButton;
	private ToolItem replaceAllButton;

	private Color backgroundToUse;
	private Color normalTextForegroundColor;
	private boolean positionAtTop = true;
	private final TargetPartVisibilityHandler targetPartVisibilityHandler;

	public FindReplaceOverlay(Shell parent, IWorkbenchPart part, IFindReplaceTarget target) {
		super(parent);
		createFindReplaceLogic(target);

		setShellStyle(SWT.MODELESS);
		setBlockOnOpen(false);
		targetPart = part;
		targetPartVisibilityHandler = new TargetPartVisibilityHandler(targetPart, this::getShell, this::close,
				this::updatePlacementAndVisibility);
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
		replaceBar.storeHistory();
		searchBar.storeHistory();
	}

	private void performSelectAll() {
		BusyIndicator.showWhile(getShell() != null ? getShell().getDisplay() : Display.getCurrent(),
				() -> findReplaceLogic.performSelectAll(getFindString()));
		searchBar.storeHistory();
	}

	private ControlListener shellMovementListener = new ControlListener() {
		@Override
		public void controlMoved(ControlEvent e) {
			if (getShell() != null) {
				getShell().getDisplay().asyncExec(() -> updatePlacementAndVisibility());
			}
		}

		@Override
		public void controlResized(ControlEvent e) {
			if (getShell() != null) {
				getShell().getDisplay().asyncExec(() -> updatePlacementAndVisibility());
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

	private PaintListener widgetMovementListener = __ -> updatePlacementAndVisibility();

	private static class TargetPartVisibilityHandler implements IPartListener2, IPageChangedListener {
		private final IWorkbenchPart targetPart;
		private final IWorkbenchPart topLevelPart;
		private final Supplier<Shell> shellProvider;
		private final Runnable closeCallback;
		private final Runnable placementUpdateCallback;

		private boolean isTopLevelVisible = true;
		private boolean isNestedLevelVisible = true;

		TargetPartVisibilityHandler(IWorkbenchPart targetPart, Supplier<Shell> shellProvider, Runnable closeCallback,
				Runnable placementUpdateCallback) {
			this.targetPart = targetPart;
			this.shellProvider = shellProvider;
			this.closeCallback = closeCallback;
			this.placementUpdateCallback = placementUpdateCallback;
			if (targetPart != null && targetPart.getSite() instanceof MultiPageEditorSite multiEditorSite) {
				topLevelPart = multiEditorSite.getMultiPageEditor();
			} else {
				topLevelPart = targetPart;
			}
		}

		@Override
		public void partBroughtToTop(IWorkbenchPartReference partRef) {
			if (partRef.getPart(false) == topLevelPart && !isTopLevelVisible) {
				this.isTopLevelVisible = true;
				shellProvider.get().getDisplay().asyncExec(this::adaptToPartActivationChange);
			}
		}

		@Override
		public void partVisible(IWorkbenchPartReference partRef) {
			if (partRef.getPart(false) == topLevelPart && !isTopLevelVisible) {
				this.isTopLevelVisible = true;
				shellProvider.get().getDisplay().asyncExec(this::adaptToPartActivationChange);
			}
		}

		@Override
		public void partHidden(IWorkbenchPartReference partRef) {
			if (partRef.getPart(false) == topLevelPart && isTopLevelVisible) {
				this.isTopLevelVisible = false;
				shellProvider.get().getDisplay().asyncExec(this::adaptToPartActivationChange);
			}
		}

		@Override
		public void partClosed(IWorkbenchPartReference partRef) {
			if (partRef.getPart(false) == topLevelPart) {
				closeCallback.run();
			}
		}

		@Override
		public void pageChanged(PageChangedEvent event) {
			if (event.getSource() == topLevelPart) {
				boolean isPageVisible = event.getSelectedPage() == targetPart;
				if (isNestedLevelVisible != isPageVisible) {
					this.isNestedLevelVisible = isPageVisible;
					shellProvider.get().getDisplay().asyncExec(this::adaptToPartActivationChange);
				}
			}
		}

		private void adaptToPartActivationChange() {
			if (shellProvider.get() == null || targetPart.getSite().getPart() == null) {
				return;
			}
			placementUpdateCallback.run();

			if (!isTargetVisible()) {
				targetPart.getSite().getShell().setActive();
				targetPart.setFocus();
				shellProvider.get().getDisplay().asyncExec(this::focusTargetWidget);
			}
		}

		private void focusTargetWidget() {
			if (shellProvider.get() == null || targetPart.getSite().getPart() == null) {
				return;
			}
			if (targetPart instanceof StatusTextEditor textEditor) {
				textEditor.getAdapter(ITextViewer.class).getTextWidget().forceFocus();
			}
		}

		public boolean isTargetVisible() {
			return isTopLevelVisible && isNestedLevelVisible;
		}
	}

	private KeyListener closeOnTargetEscapeListener = KeyListener.keyPressedAdapter(c -> {
		if (c.keyCode == SWT.ESC) {
			this.close();
		}
	});

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
		searchBar.forceFocus();

		getShell().layout();
		updatePlacementAndVisibility();

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
			replaceTools.setBackground(color);
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
				targetPart.getSite().getPage().removePartListener(targetPartVisibilityHandler);
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
			targetPart.getSite().getPage().addPartListener(targetPartVisibilityHandler);
		}
	}

	@Override
	public Control createContents(Composite parent) {
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getShell(),
				IAbstractTextEditorHelpContextIds.FIND_REPLACE_OVERLAY);

		backgroundToUse = new Color(getShell().getDisplay(), new RGBA(0, 0, 0, 0));
		return createDialog(parent);
	}

	private Control createDialog(final Composite parent) {
		createMainContainer(parent);

		retrieveBackgroundColor();

		createFindContainer();
		createSearchBar();
		createSearchTools();
		createCloseTools();
		initializeSearchShortcutHandlers();

		container.layout();

		applyDialogFont(container);
		return container;
	}

	private void initializeSearchShortcutHandlers() {
		searchTools.registerActionShortcutsAtControl(searchBar);
		closeTools.registerActionShortcutsAtControl(searchBar);
		registerActionShortcutsAtControl(replaceToggleShortcut, searchBar);
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

		searchTools.createToolItem(SWT.SEPARATOR);

		createCaseSensitiveButton();
		createRegexSearchButton();
		createWholeWordsButton();
		createAreaSearchButton();

		searchTools.createToolItem(SWT.SEPARATOR);

		searchUpButton = new AccessibleToolItemBuilder(searchTools).withStyleBits(SWT.PUSH)
				.withImage(FindReplaceOverlayImages.get(FindReplaceOverlayImages.KEY_FIND_PREV))
				.withToolTipText(FindReplaceMessages.FindReplaceOverlay_upSearchButton_toolTip)
				.withOperation(() -> performSearch(false))
				.withShortcuts(KeyboardShortcuts.SEARCH_BACKWARD).build();

		searchDownButton = new AccessibleToolItemBuilder(searchTools).withStyleBits(SWT.PUSH)
				.withImage(FindReplaceOverlayImages.get(FindReplaceOverlayImages.KEY_FIND_NEXT))
				.withToolTipText(FindReplaceMessages.FindReplaceOverlay_downSearchButton_toolTip)
				.withOperation(() -> performSearch(true))
				.withShortcuts(KeyboardShortcuts.SEARCH_FORWARD).build();
		searchDownButton.setSelection(true); // by default, search down

		searchAllButton = new AccessibleToolItemBuilder(searchTools).withStyleBits(SWT.PUSH)
				.withImage(FindReplaceOverlayImages.get(FindReplaceOverlayImages.KEY_SEARCH_ALL))
				.withToolTipText(FindReplaceMessages.FindReplaceOverlay_searchAllButton_toolTip)
				.withOperation(this::performSelectAll).withShortcuts(KeyboardShortcuts.SEARCH_ALL).build();
	}

	private void createCloseTools() {
		closeTools = new AccessibleToolBar(searchContainer);
		GridDataFactory.fillDefaults().grab(false, true).align(GridData.END, GridData.END).applyTo(closeTools);

		closeButton = new AccessibleToolItemBuilder(closeTools).withStyleBits(SWT.PUSH)
				.withImage(FindReplaceOverlayImages.get(FindReplaceOverlayImages.KEY_CLOSE))
				.withToolTipText(FindReplaceMessages.FindReplaceOverlay_closeButton_toolTip) //
				.withOperation(this::close)
				.withShortcuts(KeyboardShortcuts.CLOSE).build();
	}

	private void createAreaSearchButton() {
		searchInSelectionButton = new AccessibleToolItemBuilder(searchTools).withStyleBits(SWT.CHECK)
				.withImage(FindReplaceOverlayImages.get(FindReplaceOverlayImages.KEY_SEARCH_IN_AREA))
				.withToolTipText(FindReplaceMessages.FindReplaceOverlay_searchInSelectionButton_toolTip)
				.withOperation(() -> {
					activateInFindReplacerIf(SearchOptions.GLOBAL, !searchInSelectionButton.getSelection());
					updateIncrementalSearch();
				})
				.withShortcuts(KeyboardShortcuts.OPTION_SEARCH_IN_SELECTION).build();
		searchInSelectionButton.setSelection(findReplaceLogic.isActive(SearchOptions.WHOLE_WORD));
	}

	private void createRegexSearchButton() {
		regexSearchButton = new AccessibleToolItemBuilder(searchTools).withStyleBits(SWT.CHECK)
				.withImage(FindReplaceOverlayImages.get(FindReplaceOverlayImages.KEY_FIND_REGEX))
				.withToolTipText(FindReplaceMessages.FindReplaceOverlay_regexSearchButton_toolTip)
				.withOperation(() -> {
					activateInFindReplacerIf(SearchOptions.REGEX, regexSearchButton.getSelection());
					wholeWordSearchButton.setEnabled(!findReplaceLogic.isActive(SearchOptions.REGEX));
					updateIncrementalSearch();
				}).withShortcuts(KeyboardShortcuts.OPTION_REGEX).build();
		regexSearchButton.setSelection(findReplaceLogic.isActive(SearchOptions.REGEX));
	}

	private void createCaseSensitiveButton() {
		caseSensitiveSearchButton = new AccessibleToolItemBuilder(searchTools).withStyleBits(SWT.CHECK)
				.withImage(FindReplaceOverlayImages.get(FindReplaceOverlayImages.KEY_CASE_SENSITIVE))
				.withToolTipText(FindReplaceMessages.FindReplaceOverlay_caseSensitiveButton_toolTip)
				.withOperation(() -> {
					activateInFindReplacerIf(SearchOptions.CASE_SENSITIVE, caseSensitiveSearchButton.getSelection());
					updateIncrementalSearch();
				}).withShortcuts(KeyboardShortcuts.OPTION_CASE_SENSITIVE).build();
		caseSensitiveSearchButton.setSelection(findReplaceLogic.isActive(SearchOptions.CASE_SENSITIVE));
	}

	private void createWholeWordsButton() {
		wholeWordSearchButton = new AccessibleToolItemBuilder(searchTools).withStyleBits(SWT.CHECK)
				.withImage(FindReplaceOverlayImages.get(FindReplaceOverlayImages.KEY_WHOLE_WORD))
				.withToolTipText(FindReplaceMessages.FindReplaceOverlay_wholeWordsButton_toolTip)
				.withOperation(() -> {
					activateInFindReplacerIf(SearchOptions.WHOLE_WORD, wholeWordSearchButton.getSelection());
					updateIncrementalSearch();
				}).withShortcuts(KeyboardShortcuts.OPTION_WHOLE_WORD).build();
		wholeWordSearchButton.setSelection(findReplaceLogic.isActive(SearchOptions.WHOLE_WORD));
	}

	private void createReplaceTools() {
		Color warningColor = JFaceColors.getErrorText(getShell().getDisplay());

		replaceTools = new AccessibleToolBar(replaceContainer);

		replaceTools.createToolItem(SWT.SEPARATOR);

		GridDataFactory.fillDefaults().grab(false, true).align(GridData.CENTER, GridData.END).applyTo(replaceTools);
		replaceButton = new AccessibleToolItemBuilder(replaceTools).withStyleBits(SWT.PUSH)
				.withImage(FindReplaceOverlayImages.get(FindReplaceOverlayImages.KEY_REPLACE))
				.withToolTipText(FindReplaceMessages.FindReplaceOverlay_replaceButton_toolTip)
				.withOperation(() -> {
					if (getFindString().isEmpty()) {
						showUserFeedback(warningColor, true);
						return;
					}
					performSingleReplace();
				}).withShortcuts(KeyboardShortcuts.SEARCH_FORWARD).build();

		replaceAllButton = new AccessibleToolItemBuilder(replaceTools).withStyleBits(SWT.PUSH)
				.withImage(FindReplaceOverlayImages.get(FindReplaceOverlayImages.KEY_REPLACE_ALL))
				.withToolTipText(FindReplaceMessages.FindReplaceOverlay_replaceAllButton_toolTip)
				.withOperation(() -> {
					if (getFindString().isEmpty()) {
						showUserFeedback(warningColor, true);
						return;
					}
					performReplaceAll();
				}).withShortcuts(KeyboardShortcuts.SEARCH_ALL).build();
	}

	private void createSearchBar() {
		HistoryStore searchHistory = new HistoryStore(getDialogSettings(), "searchhistory", //$NON-NLS-1$
				HISTORY_SIZE);
		searchBar = new HistoryTextWrapper(searchHistory, searchBarContainer, SWT.SINGLE);
		GridDataFactory.fillDefaults().grab(true, true).align(GridData.FILL, GridData.FILL).applyTo(searchBar);
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
		HistoryStore replaceHistory = new HistoryStore(getDialogSettings(), "replacehistory", HISTORY_SIZE); //$NON-NLS-1$
		replaceBar = new HistoryTextWrapper(replaceHistory, replaceBarContainer, SWT.SINGLE);
		GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.END).applyTo(replaceBar);
		replaceBar.setMessage(FindReplaceMessages.FindReplaceOverlay_replaceBar_message);
		replaceBar.addFocusListener(FocusListener.focusLostAdapter(e -> {
			replaceBar.setForeground(normalTextForegroundColor);
			searchBar.setForeground(normalTextForegroundColor);
		}));
	}

	private void createFindContainer() {
		searchContainer = new Composite(contentGroup, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).align(GridData.FILL, GridData.FILL).applyTo(searchContainer);
		GridLayoutFactory.fillDefaults().numColumns(3).extendedMargins(4, 4, 3, 5).equalWidth(false)
				.applyTo(searchContainer);
		searchContainer.setBackground(getShell().getDisplay().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		searchBarContainer = new Composite(searchContainer, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).align(GridData.FILL, GridData.FILL).applyTo(searchBarContainer);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(searchBarContainer);
	}

	private void createReplaceContainer() {
		replaceContainer = new Composite(contentGroup, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).align(GridData.FILL, GridData.FILL).applyTo(replaceContainer);
		GridLayoutFactory.fillDefaults().margins(0, 1).numColumns(2).extendedMargins(4, 4, 3, 5).equalWidth(false)
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
		replaceToggleShortcut = new FindReplaceOverlayAction(this::toggleReplace);
		replaceToggleShortcut.addShortcuts(KeyboardShortcuts.TOGGLE_REPLACE);
		replaceToggle = new Button(container, SWT.FLAT | SWT.PUSH);
		GridDataFactory.fillDefaults().grab(false, true).align(GridData.BEGINNING, GridData.FILL)
				.applyTo(replaceToggle);
		replaceToggle.setToolTipText(replaceToggleShortcut
				.addShortcutHintToTooltipText(FindReplaceMessages.FindReplaceOverlay_replaceToggle_toolTip));
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
		updatePlacementAndVisibility();
	}

	private void createReplaceDialog() {
		if (replaceBarOpen) {
			return;
		}
		replaceBarOpen = true;
		createReplaceContainer();
		createReplaceBar();
		createReplaceTools();
		initializeReplaceShortcutHandlers();

		updatePlacementAndVisibility();
		applyOverlayColors(backgroundToUse, true);
		replaceBar.forceFocus();
	}

	private void initializeReplaceShortcutHandlers() {
		replaceTools.registerActionShortcutsAtControl(replaceBar);
		closeTools.registerActionShortcutsAtControl(replaceBar);
		registerActionShortcutsAtControl(replaceToggleShortcut, replaceBar);
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

	private void updatePlacementAndVisibility() {
		if (!targetPartVisibilityHandler.isTargetVisible()) {
			getShell().setVisible(false);
			return;
		}
		if (isInvalidTargetShell()) {
			getShell().getDisplay().asyncExec(() -> {
				if (isInvalidTargetShell()) {
					close();
					setParentShell(targetPart.getSite().getShell());
					open();
					targetPart.setFocus();
				}
			});
			return;
		}
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

	private boolean isInvalidTargetPart() {
		return targetPart == null || targetPart.getSite() == null || targetPart.getSite().getShell() == null;
	}

	private boolean isInvalidTargetShell() {
		if (isInvalidTargetPart()) {
			return false;
		}
		return getShell() == null || !targetPart.getSite().getShell().equals(getShell().getParent());
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
		replaceBar.storeHistory();
		searchBar.storeHistory();
	}

	private void performSearch(boolean forward) {
		boolean oldForwardSearchSetting = findReplaceLogic.isActive(SearchOptions.FORWARD);
		activateInFindReplacerIf(SearchOptions.FORWARD, forward);
		findReplaceLogic.deactivate(SearchOptions.INCREMENTAL);
		findReplaceLogic.performSearch(getFindString());
		activateInFindReplacerIf(SearchOptions.FORWARD, oldForwardSearchSetting);
		findReplaceLogic.activate(SearchOptions.INCREMENTAL);
		evaluateFindReplaceStatus();
		searchBar.storeHistory();
	}

	private void updateFromTargetSelection() {
		String selectionText = findReplaceLogic.getTarget().getSelectionText();
		if (selectionText.contains("\n")) { //$NON-NLS-1$
			findReplaceLogic.deactivate(SearchOptions.GLOBAL);
			searchInSelectionButton.setSelection(true);
		} else if (!selectionText.isEmpty()) {
			if (findReplaceLogic.isRegExSearchAvailableAndActive()) {
				selectionText = FindReplaceDocumentAdapter.escapeForRegExPattern(selectionText);
			}
			searchBar.setText(selectionText);
			findReplaceLogic.findAndSelect(findReplaceLogic.getTarget().getSelection().x, searchBar.getText());
		}
		searchBar.setSelection(0, searchBar.getText().length());
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