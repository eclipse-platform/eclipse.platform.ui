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

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.osgi.framework.FrameworkUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Scrollable;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceColors;

import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.FindReplaceDocumentAdapterContentProposalProvider;
import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.ITextViewer;

import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.fieldassist.ContentAssistCommandAdapter;
import org.eclipse.ui.internal.SearchDecoration;
import org.eclipse.ui.internal.findandreplace.FindReplaceLogic;
import org.eclipse.ui.internal.findandreplace.FindReplaceMessages;
import org.eclipse.ui.internal.findandreplace.HistoryStore;
import org.eclipse.ui.internal.findandreplace.IFindReplaceLogic;
import org.eclipse.ui.internal.findandreplace.SearchOptions;

import org.eclipse.ui.texteditor.FindReplaceAction;
import org.eclipse.ui.texteditor.IAbstractTextEditorHelpContextIds;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.StatusTextEditor;

public class FindReplaceOverlay {
	private static final class KeyboardShortcuts {
		private static final List<KeyStroke> SEARCH_FORWARD = List.of( //
				KeyStroke.getInstance(SWT.CR), KeyStroke.getInstance(SWT.KEYPAD_CR));
		private static final List<KeyStroke> SEARCH_BACKWARD = List.of( //
				KeyStroke.getInstance(SWT.SHIFT, SWT.CR), KeyStroke.getInstance(SWT.SHIFT, SWT.KEYPAD_CR));
		private static final List<KeyStroke> SEARCH_ALL = List.of( //
				KeyStroke.getInstance(SWT.MOD1, SWT.CR), KeyStroke.getInstance(SWT.MOD1, SWT.KEYPAD_CR));
		private static final List<KeyStroke> OPTION_CASE_SENSITIVE = List.of( //
				KeyStroke.getInstance(SWT.MOD1 | SWT.SHIFT, 'C'), KeyStroke.getInstance(SWT.MOD1 | SWT.SHIFT, 'c'));
		private static final List<KeyStroke> OPTION_WHOLE_WORD = List.of( //
				KeyStroke.getInstance(SWT.MOD1 | SWT.SHIFT, 'D'), KeyStroke.getInstance(SWT.MOD1 | SWT.SHIFT, 'd'));
		private static final List<KeyStroke> OPTION_REGEX = List.of( //
				KeyStroke.getInstance(SWT.MOD1 | SWT.SHIFT, 'P'), KeyStroke.getInstance(SWT.MOD1 | SWT.SHIFT, 'p'));
		private static final List<KeyStroke> OPTION_SEARCH_IN_SELECTION = List.of( //
				KeyStroke.getInstance(SWT.MOD1 | SWT.SHIFT, 'I'), KeyStroke.getInstance(SWT.MOD1 | SWT.SHIFT, 'i'));
		private static final List<KeyStroke> CLOSE = List.of( //
				KeyStroke.getInstance(SWT.ESC), KeyStroke.getInstance(SWT.MOD1, 'F'),
				KeyStroke.getInstance(SWT.MOD1, 'f'));
		private static final List<KeyStroke> TOGGLE_REPLACE = List.of( //
				KeyStroke.getInstance(SWT.MOD1, 'R'), KeyStroke.getInstance(SWT.MOD1, 'r'));
	}

	public static final String ID_DATA_KEY = "org.eclipse.ui.internal.findreplace.overlay.FindReplaceOverlay.id"; //$NON-NLS-1$

	private static final String REPLACE_BAR_OPEN_DIALOG_SETTING = "replaceBarOpen"; //$NON-NLS-1$
	private static final double WORST_CASE_RATIO_EDITOR_TO_OVERLAY = 0.95;
	private static final double BIG_WIDTH_RATIO_EDITOR_TO_OVERLAY = 0.7;
	private static final String MINIMAL_WIDTH_TEXT = "THIS TEXT IS SHORT "; //$NON-NLS-1$
	private static final String IDEAL_WIDTH_TEXT = "THIS TEXT HAS A REASONABLE LENGTH FOR SEARCHING"; //$NON-NLS-1$
	private static final int HISTORY_SIZE = 15;

	private final IFindReplaceLogic findReplaceLogic;
	private final IWorkbenchPart targetPart;
	private boolean replaceBarOpen;

	private final Composite targetControl;
	private Composite containerControl;
	private AccessibleToolBar replaceToggleTools;
	private ToolItem replaceToggle;

	private Composite contentGroup;

	private Composite searchContainer;
	private Composite searchBarContainer;
	private HistoryTextWrapper searchBar;
	private AccessibleToolBar searchTools;
	private ToolItem searchInSelectionButton;
	private ToolItem wholeWordSearchButton;
	private ToolItem caseSensitiveSearchButton;
	private ToolItem regexSearchButton;
	private ToolItem searchBackwardButton;
	private ToolItem searchForwardButton;
	private ToolItem selectAllButton;
	private AccessibleToolBar closeTools;

	private Composite replaceContainer;
	private Composite replaceBarContainer;
	private HistoryTextWrapper replaceBar;
	private AccessibleToolBar replaceTools;
	private ToolItem replaceButton;
	private ToolItem replaceAllButton;

	private Color widgetBackgroundColor;
	private Color overlayBackgroundColor;
	private Color normalTextForegroundColor;
	private Color errorTextForegroundColor;

	private boolean positionAtTop = true;
	private ControlDecoration searchBarDecoration;
	private ContentAssistCommandAdapter contentAssistSearchField, contentAssistReplaceField;

	private FindReplaceOverlayCommandSupport commandSupport;

	private final FocusListener targetActionActivationHandling = new FocusListener() {
		@Override
		public void focusGained(FocusEvent e) {
			if (e.widget == searchBar.getTextBar()) {
				commandSupport.searchBarActivated();
			} else if (replaceBar != null && e.widget == replaceBar.getTextBar()) {
				commandSupport.replaceBarActivated();
			}
		}

		@Override
		public void focusLost(FocusEvent e) {
			commandSupport.searchOrReplaceBarDeactivated();
		}
	};

	private final CustomFocusOrder customFocusOrder = new CustomFocusOrder();

	private class CustomFocusOrder {
		private final Listener searchBarToReplaceBar = e -> {
			if (e.detail == SWT.TRAVERSE_TAB_NEXT) {
				e.doit = false;
				replaceBar.forceFocus();
			}
		};

		private final Listener replaceBarToSearchBarAndTools = e -> {
			switch (e.detail) {
			case SWT.TRAVERSE_TAB_NEXT:
				e.doit = false;
				searchBar.getDropDownTool().getFirstControl().forceFocus();
				break;
			case SWT.TRAVERSE_TAB_PREVIOUS:
				e.doit = false;
				searchBar.getTextBar().forceFocus();
				break;
			default:
				// Proceed as normal
			}
		};

		private final Listener searchToolsToReplaceBar = e -> {
			switch (e.detail) {
			case SWT.TRAVERSE_TAB_PREVIOUS:
				e.doit = false;
				replaceBar.forceFocus();
				break;
			default:
				// Proceed as normal
			}
		};

		private final Listener closeToolsToReplaceTools = e -> {
			switch (e.detail) {
			case SWT.TRAVERSE_TAB_NEXT:
				e.doit = false;
				replaceBar.getDropDownTool().getFirstControl().forceFocus();
				break;
			default:
				// Proceed as normal
			}
		};

		private final Listener replaceToolsToCloseTools = e -> {
			switch (e.detail) {
			case SWT.TRAVERSE_TAB_PREVIOUS:
				e.doit = false;
				closeTools.getFirstControl().forceFocus();
				break;
			default:
				// Proceed as normal
			}
		};

		void apply() {
			searchBar.getTextBar().addListener(SWT.Traverse, searchBarToReplaceBar);
			replaceBar.getTextBar().addListener(SWT.Traverse, replaceBarToSearchBarAndTools);
			searchBar.getDropDownTool().getFirstControl().addListener(SWT.Traverse, searchToolsToReplaceBar);
			closeTools.getFirstControl().addListener(SWT.Traverse, closeToolsToReplaceTools);
			replaceBar.getDropDownTool().getFirstControl().addListener(SWT.Traverse, replaceToolsToCloseTools);
		}

		void dispose() {
			searchBar.getTextBar().removeListener(SWT.Traverse, searchBarToReplaceBar);
			replaceBar.getTextBar().removeListener(SWT.Traverse, replaceBarToSearchBarAndTools);
			searchBar.getDropDownTool().getFirstControl().removeListener(SWT.Traverse, searchToolsToReplaceBar);
			closeTools.getFirstControl().removeListener(SWT.Traverse, closeToolsToReplaceTools);
			replaceBar.getDropDownTool().getFirstControl().removeListener(SWT.Traverse, replaceToolsToCloseTools);
		}
	}

	public FindReplaceOverlay(Shell parent, IWorkbenchPart part, IFindReplaceTarget target) {
		targetPart = part;
		commandSupport = new FindReplaceOverlayCommandSupport(targetPart);
		targetControl = getTargetControl(parent, part);
		findReplaceLogic = createFindReplaceLogic(target);
		createContainerAndSearchControls(targetControl);
		containerControl.setVisible(false);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(containerControl,
				IAbstractTextEditorHelpContextIds.FIND_REPLACE_OVERLAY);
	}

	private static Composite getTargetControl(Shell targetShell, IWorkbenchPart targetPart) {
		if (targetPart instanceof StatusTextEditor textEditor) {
			return textEditor.getAdapter(ITextViewer.class).getTextWidget();
		} else {
			return targetShell;
		}
	}

	private boolean insertedInTargetParent() {
		return targetControl instanceof StyledText;
	}

	private IFindReplaceLogic createFindReplaceLogic(IFindReplaceTarget target) {
		IFindReplaceLogic logic = new FindReplaceLogic();
		boolean isTargetEditable = false;
		if (target != null) {
			isTargetEditable = target.isEditable();
		}
		logic.updateTarget(target, isTargetEditable);
		logic.activate(SearchOptions.INCREMENTAL);
		logic.activate(SearchOptions.GLOBAL);
		logic.activate(SearchOptions.WRAP);
		logic.activate(SearchOptions.FORWARD);
		return logic;
	}

	public Composite getContainerControl() {
		return containerControl;
	}

	private void performReplaceAll() {
		BusyIndicator.showWhile(containerControl.getShell() != null ? containerControl.getShell().getDisplay() : Display.getCurrent(),
				findReplaceLogic::performReplaceAll);
		evaluateStatusAfterReplace();
		replaceBar.storeHistory();
		searchBar.storeHistory();
	}

	private void performSelectAll() {
		BusyIndicator.showWhile(containerControl.getShell() != null ? containerControl.getShell().getDisplay() : Display.getCurrent(),
				findReplaceLogic::performSelectAll);
		searchBar.storeHistory();
	}

	private final ControlListener targetMovementListener = ControlListener
			.controlResizedAdapter(__ -> asyncExecIfOpen(FindReplaceOverlay.this::updatePlacementAndVisibility));

	private void asyncExecIfOpen(Runnable operation) {
		if (!containerControl.isDisposed()) {
			containerControl.getDisplay().asyncExec(() -> {
				if (containerControl != null && !containerControl.isDisposed()) {
					operation.run();
				}
			});
		}
	}

	private final FocusListener targetFocusListener = FocusListener.focusGainedAdapter(__ ->  {
			removeSearchScope();
			searchBar.storeHistory();
	});

	private final KeyListener closeOnTargetEscapeListener = KeyListener.keyPressedAdapter(c -> {
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
	private IDialogSettings getDialogSettings() {
		IDialogSettings settings = PlatformUI
				.getDialogSettingsProvider(FrameworkUtil.getBundle(FindReplaceAction.class)).getDialogSettings();
		IDialogSettings dialogSettings = settings.getSection(FindReplaceAction.class.getClass().getName());
		if (dialogSettings == null) {
			dialogSettings = settings.addNewSection(FindReplaceAction.class.getClass().getName());
		}
		return dialogSettings;
	}

	public void close() {
		if (containerControl.isDisposed() || !containerControl.isVisible()) {
			return;
		}
		if (targetPart != null) {
			targetPart.setFocus();
		}
		storeOverlaySettings();

		findReplaceLogic.activate(SearchOptions.GLOBAL);
		unbindListeners();
		containerControl.setVisible(false);
	}

	public void open() {
		if (!containerControl.isVisible()) {
			containerControl.setVisible(true);
			bindListeners();
			restoreOverlaySettings();
		}
		assignIDs();
		containerControl.layout();
		containerControl.moveAbove(null);
		updatePlacementAndVisibility();
		updateContentAssistAvailability();

		searchBar.setFocus();
		updateFromTargetSelection();
	}

	private void storeOverlaySettings() {
		getDialogSettings().put(REPLACE_BAR_OPEN_DIALOG_SETTING, replaceBarOpen);
	}

	private void restoreOverlaySettings() {
		Boolean shouldOpenReplaceBar = getDialogSettings().getBoolean(REPLACE_BAR_OPEN_DIALOG_SETTING);
		setReplaceVisible(shouldOpenReplaceBar);
	}

	@SuppressWarnings("nls")
	private void assignIDs() {
		replaceToggle.setData(ID_DATA_KEY, "replaceToggle");
		searchBar.setData(ID_DATA_KEY, "searchInput");
		searchBackwardButton.setData(ID_DATA_KEY, "searchBackward");
		searchForwardButton.setData(ID_DATA_KEY, "searchForward");
		selectAllButton.setData(ID_DATA_KEY, "selectAll");
		searchInSelectionButton.setData(ID_DATA_KEY, "searchInSelection");
		wholeWordSearchButton.setData(ID_DATA_KEY, "wholeWordSearch");
		regexSearchButton.setData(ID_DATA_KEY, "regExSearch");
		caseSensitiveSearchButton.setData(ID_DATA_KEY, "caseSensitiveSearch");

		if (replaceBarOpen) {
			replaceBar.setData(ID_DATA_KEY, "replaceInput");
			replaceButton.setData(ID_DATA_KEY, "replaceOne");
			replaceAllButton.setData(ID_DATA_KEY, "replaceAll");
		}
	}

	private void unbindListeners() {
		targetControl.removeFocusListener(targetFocusListener);
		targetControl.removeControlListener(targetMovementListener);
		targetControl.removeKeyListener(closeOnTargetEscapeListener);
	}

	private void bindListeners() {
		targetControl.addFocusListener(targetFocusListener);
		targetControl.addControlListener(targetMovementListener);
		targetControl.addKeyListener(closeOnTargetEscapeListener);
	}

	private void createContainerAndSearchControls(Composite parent) {
		if (insertedInTargetParent()) {
			parent = parent.getParent();
		}
		retrieveColors();
		createMainContainer(parent);
		initializeSearchShortcutHandlers();

		containerControl.layout();
	}

	private void initializeSearchShortcutHandlers() {
		commandSupport.registerCommonActionShortcutsAtControl(searchBar);
		commandSupport.registerSearchActionShortcutsAtControl(searchBar);
	}

	/**
	 * HACK: In order to not introduce a hard-coded color, we need to retrieve the
	 * background color of text widgets and composite to color those widgets that
	 * would otherwise inherit non-fitting custom colors from the containing
	 * StyledText.
	 */
	private void retrieveColors() {
		if (targetPart instanceof StatusTextEditor textEditor) {
			Control targetWidget = textEditor.getAdapter(ITextViewer.class).getTextWidget();
			widgetBackgroundColor = targetWidget.getBackground();
			normalTextForegroundColor = targetWidget.getForeground();
		} else {
			Text textBarForRetrievingTheRightColor = new Text(targetControl.getShell(), SWT.SINGLE | SWT.SEARCH);
			targetControl.getShell().layout();
			widgetBackgroundColor = textBarForRetrievingTheRightColor.getBackground();
			normalTextForegroundColor = textBarForRetrievingTheRightColor.getForeground();
			textBarForRetrievingTheRightColor.dispose();
		}
		overlayBackgroundColor = retrieveDefaultCompositeBackground();
		errorTextForegroundColor = JFaceColors.getErrorText(targetControl.getShell().getDisplay());
	}

	private Color retrieveDefaultCompositeBackground() {
		AtomicReference<Color> colorReference = new AtomicReference<>();
		Dialog dummyDialogForColorRetrieval = new Dialog(targetControl.getShell()) {
			@Override
			public void create() {
				super.create();
				colorReference.set(getContents().getBackground());
			}

		};
		dummyDialogForColorRetrieval.create();
		dummyDialogForColorRetrieval.close();
		return colorReference.get();
	}

	/**
	 * A composite with a fixed background color, not adapting to theming.
	 */
	private class FixedColorComposite extends Composite {
		private final Color fixColor;

		public FixedColorComposite(Composite parent, int style, Color backgroundColor) {
			super(parent, style);
			this.fixColor = backgroundColor;
			setBackground(backgroundColor);
		}

		@Override
		public void setBackground(Color unusedColor) {
			super.setBackground(fixColor);
		}
	}

	private void createMainContainer(final Composite parent) {
		containerControl = new FixedColorComposite(parent, SWT.NONE, overlayBackgroundColor);
		GridDataFactory.fillDefaults().grab(true, true).align(GridData.FILL, GridData.FILL).applyTo(containerControl);
		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(false).margins(2, 2).spacing(2, 0)
				.applyTo(containerControl);

		createReplaceToggle();
		createContentsContainer();
	}

	private void createReplaceToggle() {
		replaceToggleTools = new AccessibleToolBar(containerControl);
		GridDataFactory.fillDefaults().grab(false, true).align(GridData.FILL, GridData.FILL)
				.applyTo(replaceToggleTools);
		replaceToggleTools.addMouseListener(MouseListener.mouseDownAdapter(__ -> setReplaceVisible(!replaceBarOpen)));

		FindReplaceOverlayAction replaceToggleAction = new FindReplaceOverlayAction(() -> setReplaceVisible(!replaceBarOpen));
		replaceToggleAction.addShortcuts(KeyboardShortcuts.TOGGLE_REPLACE);
		commandSupport.registerCommonAction(replaceToggleAction);

		replaceToggle = new AccessibleToolItemBuilder(replaceToggleTools)
				.withImage(FindReplaceOverlayImages.get(FindReplaceOverlayImages.KEY_OPEN_REPLACE_AREA))
				.withToolTipText(FindReplaceMessages.FindReplaceOverlay_replaceToggle_toolTip)
				.withAction(replaceToggleAction)
				.build();
	}

	private void createContentsContainer() {
		contentGroup = new FixedColorComposite(containerControl, SWT.NONE, overlayBackgroundColor);
		GridLayoutFactory.fillDefaults().numColumns(1).equalWidth(false).spacing(0, 2).applyTo(contentGroup);
		GridDataFactory.fillDefaults().grab(true, true).align(GridData.FILL, GridData.FILL).applyTo(contentGroup);

		createSearchContainer();
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

		FindReplaceOverlayAction searchBackwardAction = new FindReplaceOverlayAction(() -> performSearch(false));
		searchBackwardAction.addShortcuts(KeyboardShortcuts.SEARCH_BACKWARD);
		commandSupport.registerSearchAction(searchBackwardAction);
		searchBackwardButton = new AccessibleToolItemBuilder(searchTools).withStyleBits(SWT.PUSH)
				.withImage(FindReplaceOverlayImages.get(FindReplaceOverlayImages.KEY_FIND_PREV))
				.withToolTipText(FindReplaceMessages.FindReplaceOverlay_upSearchButton_toolTip)
				.withAction(searchBackwardAction).build();

		FindReplaceOverlayAction searchForwardAction = new FindReplaceOverlayAction(() -> performSearch(true));
		searchForwardAction.addShortcuts(KeyboardShortcuts.SEARCH_FORWARD);
		commandSupport.registerSearchAction(searchForwardAction);
		searchForwardButton = new AccessibleToolItemBuilder(searchTools).withStyleBits(SWT.PUSH)
				.withImage(FindReplaceOverlayImages.get(FindReplaceOverlayImages.KEY_FIND_NEXT))
				.withToolTipText(FindReplaceMessages.FindReplaceOverlay_downSearchButton_toolTip)
				.withAction(searchForwardAction).build();

		FindReplaceOverlayAction selectAllAction = new FindReplaceOverlayAction(this::performSelectAll);
		selectAllAction.addShortcuts(KeyboardShortcuts.SEARCH_ALL);
		commandSupport.registerSearchAction(selectAllAction);
		selectAllButton = new AccessibleToolItemBuilder(searchTools).withStyleBits(SWT.PUSH)
				.withImage(FindReplaceOverlayImages.get(FindReplaceOverlayImages.KEY_SEARCH_ALL))
				.withToolTipText(FindReplaceMessages.FindReplaceOverlay_searchAllButton_toolTip)
				.withAction(selectAllAction).build();
	}

	private void createCloseTools() {
		closeTools = new AccessibleToolBar(searchContainer);
		GridDataFactory.fillDefaults().grab(false, true).align(GridData.END, GridData.END).applyTo(closeTools);

		FindReplaceOverlayAction closeAction = new FindReplaceOverlayAction(this::close);
		closeAction.addShortcuts(KeyboardShortcuts.CLOSE);
		commandSupport.registerCommonAction(closeAction);

		// Close button
		new AccessibleToolItemBuilder(closeTools).withStyleBits(SWT.PUSH)
				.withImage(FindReplaceOverlayImages.get(FindReplaceOverlayImages.KEY_CLOSE))
				.withToolTipText(FindReplaceMessages.FindReplaceOverlay_closeButton_toolTip) //
				.withAction(closeAction).build();
	}

	private void createAreaSearchButton() {
		FindReplaceOverlaySearchOptionAction searchInSelectionAction = new FindReplaceOverlaySearchOptionAction(SearchOptions.GLOBAL, findReplaceLogic);
		searchInSelectionAction.addExecutionListener(this::updateIncrementalSearch);
		searchInSelectionAction.addShortcuts(KeyboardShortcuts.OPTION_SEARCH_IN_SELECTION);
		commandSupport.registerCommonAction(searchInSelectionAction);
		searchInSelectionButton = new AccessibleToolItemBuilder(searchTools).withStyleBits(SWT.CHECK)
				.withImage(FindReplaceOverlayImages.get(FindReplaceOverlayImages.KEY_SEARCH_IN_AREA))
				.withToolTipText(FindReplaceMessages.FindReplaceOverlay_searchInSelectionButton_toolTip)
				.withAction(searchInSelectionAction).displayInverted().build();
	}

	private void createRegexSearchButton() {
		FindReplaceOverlaySearchOptionAction regexAction = new FindReplaceOverlaySearchOptionAction(SearchOptions.REGEX,
				findReplaceLogic);
		regexAction.addExecutionListener(this::updateIncrementalSearch);
		regexAction.addShortcuts(KeyboardShortcuts.OPTION_REGEX);
		commandSupport.registerCommonAction(regexAction);
		regexSearchButton = new AccessibleToolItemBuilder(searchTools).withStyleBits(SWT.CHECK)
				.withImage(FindReplaceOverlayImages.get(FindReplaceOverlayImages.KEY_FIND_REGEX))
				.withToolTipText(FindReplaceMessages.FindReplaceOverlay_regexSearchButton_toolTip)
				.withAction(regexAction).build();
		findReplaceLogic.addSearchOptionActivationChangedListener(SearchOptions.REGEX, activated -> {
			updateContentAssistAvailability();
			decorate();
		});
	}

	private void createCaseSensitiveButton() {
		FindReplaceOverlaySearchOptionAction caseSensitiveAction = new FindReplaceOverlaySearchOptionAction(
				SearchOptions.CASE_SENSITIVE, findReplaceLogic);
		caseSensitiveAction.addExecutionListener(this::updateIncrementalSearch);
		caseSensitiveAction.addShortcuts(KeyboardShortcuts.OPTION_CASE_SENSITIVE);
		commandSupport.registerCommonAction(caseSensitiveAction);
		caseSensitiveSearchButton = new AccessibleToolItemBuilder(searchTools).withStyleBits(SWT.CHECK)
				.withImage(FindReplaceOverlayImages.get(FindReplaceOverlayImages.KEY_CASE_SENSITIVE))
				.withToolTipText(FindReplaceMessages.FindReplaceOverlay_caseSensitiveButton_toolTip)
				.withAction(caseSensitiveAction).build();
	}

	private void createWholeWordsButton() {
		FindReplaceOverlaySearchOptionAction wholeWordAction = new FindReplaceOverlaySearchOptionAction(
				SearchOptions.WHOLE_WORD, findReplaceLogic);
		wholeWordAction.addExecutionListener(this::updateIncrementalSearch);
		wholeWordAction.addShortcuts(KeyboardShortcuts.OPTION_WHOLE_WORD);
		commandSupport.registerCommonAction(wholeWordAction);
		wholeWordSearchButton = new AccessibleToolItemBuilder(searchTools).withStyleBits(SWT.CHECK)
				.withImage(FindReplaceOverlayImages.get(FindReplaceOverlayImages.KEY_WHOLE_WORD))
				.withToolTipText(FindReplaceMessages.FindReplaceOverlay_wholeWordsButton_toolTip)
				.withAction(wholeWordAction).build();
	}

	private void createReplaceTools() {
		replaceTools = new AccessibleToolBar(replaceContainer);

		replaceTools.createToolItem(SWT.SEPARATOR);

		GridDataFactory.fillDefaults().grab(false, true).align(GridData.CENTER, GridData.END).applyTo(replaceTools);
		FindReplaceOverlayAction replaceAction = new FindReplaceOverlayAction(() -> {
			if (getFindString().isEmpty()) {
				applyErrorColor(replaceBar);
				return;
			}
			performSingleReplace();
		});
		replaceAction.addShortcuts(KeyboardShortcuts.SEARCH_FORWARD);
		commandSupport.registerReplaceAction(replaceAction);
		replaceButton = new AccessibleToolItemBuilder(replaceTools).withStyleBits(SWT.PUSH)
				.withImage(FindReplaceOverlayImages.get(FindReplaceOverlayImages.KEY_REPLACE))
				.withToolTipText(FindReplaceMessages.FindReplaceOverlay_replaceButton_toolTip)
				.withAction(replaceAction).build();

		FindReplaceOverlayAction replaceAllAction = new FindReplaceOverlayAction(() -> {
			if (getFindString().isEmpty()) {
				applyErrorColor(replaceBar);
				return;
			}
			performReplaceAll();
		});
		replaceAllAction.addShortcuts(KeyboardShortcuts.SEARCH_ALL);
		commandSupport.registerReplaceAction(replaceAllAction);
		replaceAllButton = new AccessibleToolItemBuilder(replaceTools).withStyleBits(SWT.PUSH)
				.withImage(FindReplaceOverlayImages.get(FindReplaceOverlayImages.KEY_REPLACE_ALL))
				.withToolTipText(FindReplaceMessages.FindReplaceOverlay_replaceAllButton_toolTip)
				.withAction(replaceAllAction).build();
	}

	private ContentAssistCommandAdapter createContentAssistField(HistoryTextWrapper control, boolean isFind) {
		TextContentAdapter contentAdapter = new TextContentAdapter();
		FindReplaceDocumentAdapterContentProposalProvider findProposer = new FindReplaceDocumentAdapterContentProposalProvider(
				isFind);
		return new ContentAssistCommandAdapter(control.getTextBar(), contentAdapter, findProposer,
				ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS, new char[0], true);
	}

	private void createSearchBar() {
		searchBarContainer = new Composite(searchContainer, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).align(GridData.FILL, GridData.FILL).applyTo(searchBarContainer);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(searchBarContainer);
		HistoryStore searchHistory = new HistoryStore(getDialogSettings(), "findhistory", //$NON-NLS-1$
				HISTORY_SIZE);
		searchBar = new HistoryTextWrapper(searchHistory, searchBarContainer, SWT.SINGLE);
		searchBarDecoration = new ControlDecoration(searchBar, SWT.BOTTOM | SWT.LEFT);
		GridDataFactory.fillDefaults().grab(true, true).align(GridData.FILL, GridData.FILL).applyTo(searchBar);
		searchBar.forceFocus();
		searchBar.selectAll();
		searchBar.addModifyListener(e -> {
			updateIncrementalSearch();
			decorate();
		});
		searchBar.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				findReplaceLogic.resetIncrementalBaseLocation();
			}
			@Override
			public void focusLost(FocusEvent e) {
				resetErrorColoring();
			}
		});
		searchBar.addFocusListener(targetActionActivationHandling);
		searchBar.setMessage(FindReplaceMessages.FindReplaceOverlay_searchBar_message);
		contentAssistSearchField = createContentAssistField(searchBar, true);
		searchBar.setTabList(null);
	}

	private void updateIncrementalSearch() {
		findReplaceLogic.setFindString(searchBar.getText());
		evaluateStatusAfterFind();
	}

	private void createReplaceBar() {
		replaceBarContainer = new Composite(replaceContainer, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).align(GridData.FILL, GridData.END).applyTo(replaceBarContainer);
		GridLayoutFactory.fillDefaults().numColumns(1).equalWidth(false).applyTo(replaceBarContainer);

		HistoryStore replaceHistory = new HistoryStore(getDialogSettings(), "replacehistory", HISTORY_SIZE); //$NON-NLS-1$
		replaceBar = new HistoryTextWrapper(replaceHistory, replaceBarContainer, SWT.SINGLE);
		GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.END).applyTo(replaceBar);
		replaceBar.setMessage(FindReplaceMessages.FindReplaceOverlay_replaceBar_message);
		replaceBar.addModifyListener(e -> {
			findReplaceLogic.setReplaceString(replaceBar.getText());
			resetErrorColoring();
		});
		replaceBar.addFocusListener(targetActionActivationHandling);
		replaceBar.addFocusListener(FocusListener.focusLostAdapter(e -> resetErrorColoring()));
		contentAssistReplaceField = createContentAssistField(replaceBar, false);
	}

	private void createSearchContainer() {
		searchContainer = new FixedColorComposite(contentGroup, SWT.NONE, widgetBackgroundColor);
		GridDataFactory.fillDefaults().grab(true, true).align(GridData.FILL, GridData.FILL).applyTo(searchContainer);
		GridLayoutFactory.fillDefaults().numColumns(3).extendedMargins(7, 4, 3, 5).equalWidth(false)
				.applyTo(searchContainer);

		createSearchBar();
		createSearchTools();
		createCloseTools();
	}

	private void createReplaceContainer() {
		replaceContainer = new FixedColorComposite(contentGroup, SWT.NONE, widgetBackgroundColor);
		GridDataFactory.fillDefaults().grab(true, true).align(GridData.FILL, GridData.FILL).applyTo(replaceContainer);
		GridLayoutFactory.fillDefaults().margins(0, 0).numColumns(2).extendedMargins(7, 4, 3, 5).equalWidth(false)
				.applyTo(replaceContainer);

		createReplaceBar();
		createReplaceTools();
	}

	private void setReplaceVisible(boolean visible) {
		if (findReplaceLogic.getTarget().isEditable() && visible) {
			createReplaceDialog();
			replaceToggle.setImage(FindReplaceOverlayImages.get(FindReplaceOverlayImages.KEY_CLOSE_REPLACE_AREA));
		} else {
			hideReplace();
			replaceToggle.setImage(FindReplaceOverlayImages.get(FindReplaceOverlayImages.KEY_OPEN_REPLACE_AREA));
		}
		updateContentAssistAvailability();
	}

	private void hideReplace() {
		if (!replaceBarOpen) {
			return;
		}
		customFocusOrder.dispose();
		commandSupport.unregisterReplaceActions();
		searchBar.forceFocus();
		contentAssistReplaceField = null;
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
		initializeReplaceShortcutHandlers();

		updatePlacementAndVisibility();
		assignIDs();
		replaceBar.forceFocus();
		customFocusOrder.apply();
	}

	private void initializeReplaceShortcutHandlers() {
		commandSupport.registerCommonActionShortcutsAtControl(replaceBar);
		commandSupport.registerReplaceActionShortcutsAtControl(replaceBar);
	}

	private void enableSearchTools(boolean enable) {
		((GridData) searchTools.getLayoutData()).exclude = !enable;
		searchTools.setVisible(enable);
	}

	private void enableReplaceToggle(boolean enable) {
		if (!okayToUse(replaceToggle)) {
			return;
		}
		boolean shouldBeVisible = enable && findReplaceLogic.getTarget().isEditable();
		((GridLayout) containerControl.getLayout()).numColumns = shouldBeVisible ? 2 : 1;
		((GridData) replaceToggleTools.getLayoutData()).exclude = !shouldBeVisible;
		replaceToggleTools.setVisible(shouldBeVisible);
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
		if (!okayToUse(targetControl)) {
			this.close();
			return;
		}

		containerControl.requestLayout();
		Rectangle targetControlBounds = calculateControlBounds(targetControl);
		Rectangle overlayBounds = calculateDesiredOverlayBounds(targetControlBounds);
		updatePosition(overlayBounds);
		configureDisplayedWidgetsForWidth(overlayBounds.width);
		updateVisibility(targetControlBounds, overlayBounds);

		repositionTextSelection();
	}

	private Rectangle calculateControlBounds(Control control) {
		Rectangle controlBounds = control.getBounds();
		int width = controlBounds.width;
		int height = controlBounds.height;
		int x = 0;
		int y = 0;
		if (insertedInTargetParent()) {
			x = controlBounds.x;
			y = controlBounds.y;
		}
		if (control instanceof Scrollable scrollable) {
			ScrollBar verticalBar = scrollable.getVerticalBar();
			ScrollBar horizontalBar = scrollable.getHorizontalBar();
			if (verticalBar != null) {
				width -= verticalBar.getSize().x;
			}
			if (horizontalBar != null) {
				height -= horizontalBar.getSize().y;
			}
		}
		if (control instanceof StyledText styledText) {
			width -= styledText.getRightMargin();
		}
		return new Rectangle(x, y, width, height);
	}

	private Rectangle calculateDesiredOverlayBounds(Rectangle targetControlBounds) {
		int width = getIdealOverlayWidth(targetControlBounds);
		int height = containerControl.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;

		int x = targetControlBounds.x + targetControlBounds.width - width;
		int y = targetControlBounds.y;
		if (!positionAtTop) {
			y += targetControlBounds.height - height;
		}

		return new Rectangle(x, y, width, height);
	}

	private void updatePosition(Rectangle overlayBounds) {
		containerControl.setSize(new Point(overlayBounds.width, overlayBounds.height));
		containerControl.setLocation(new Point(overlayBounds.x, overlayBounds.y));
		containerControl.layout(true);
	}

	private void updateVisibility(Rectangle targetControlBounds, Rectangle overlayBounds) {
		boolean shallBeVisible = true;
		if (positionAtTop) {
			shallBeVisible = overlayBounds.y + overlayBounds.height <= targetControlBounds.y
					+ targetControlBounds.height;
		} else {
			shallBeVisible = overlayBounds.y >= targetControlBounds.y;
		}
		if (shallBeVisible != containerControl.isVisible()) {
			containerControl.setVisible(shallBeVisible);
		}
	}

	private String getFindString() {
		return searchBar.getText();
	}

	private void performSingleReplace() {
		if (findReplaceLogic.performSelectAndReplace()) {
			findReplaceLogic.performSearch();
			evaluateStatusAfterFind();
		} else {
			evaluateStatusAfterReplace();
		}

		replaceBar.storeHistory();
		searchBar.storeHistory();
	}

	private void performSearch(boolean forward) {
		boolean oldForwardSearchSetting = findReplaceLogic.isActive(SearchOptions.FORWARD);
		activateInFindReplacerIf(SearchOptions.FORWARD, forward);
		findReplaceLogic.performSearch();
		activateInFindReplacerIf(SearchOptions.FORWARD, oldForwardSearchSetting);
		evaluateStatusAfterFind();
		searchBar.storeHistory();
	}

	private void updateFromTargetSelection() {
		String selectionText = findReplaceLogic.getTarget().getSelectionText();
		if (selectionText.contains("\n")) { //$NON-NLS-1$
			findReplaceLogic.deactivate(SearchOptions.GLOBAL);
		} else if (!selectionText.isEmpty()) {
			if (findReplaceLogic.isAvailableAndActive(SearchOptions.REGEX)) {
				selectionText = FindReplaceDocumentAdapter.escapeForRegExPattern(selectionText);
			}
			searchBar.setText(selectionText);
			findReplaceLogic.findAndSelect(findReplaceLogic.getTarget().getSelection().x);
		}
		searchBar.setSelection(0, searchBar.getText().length());
	}

	private void evaluateStatusAfterFind() {
		resetErrorColoring();
		if (!findReplaceLogic.getStatus().wasSuccessful()) {
			applyErrorColor(searchBar);
		}
	}

	private void evaluateStatusAfterReplace() {
		resetErrorColoring();
		if (!findReplaceLogic.getStatus().wasSuccessful()) {
			applyErrorColor(replaceBar);
		}
	}

	private void applyErrorColor(HistoryTextWrapper inputField) {
		inputField.setForeground(errorTextForegroundColor);
	}

	private void resetErrorColoring() {
		searchBar.setForeground(normalTextForegroundColor);
		if (okayToUse(replaceBar)) {
			replaceBar.setForeground(normalTextForegroundColor);
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
		if (containerControl != null && containerControl.isVisible()) {
			updatePlacementAndVisibility();
		}
	}

	private void removeSearchScope() {
		findReplaceLogic.activate(SearchOptions.GLOBAL);
	}

	private void setContentAssistsEnablement(boolean enable) {
		contentAssistSearchField.setEnabled(enable);
		if (okayToUse(replaceBar)) {
			contentAssistReplaceField.setEnabled(enable);
		}
	}

	private void updateContentAssistAvailability() {
		setContentAssistsEnablement(findReplaceLogic.isAvailableAndActive(SearchOptions.REGEX));
	}

	private void decorate() {
		if (findReplaceLogic.isAvailableAndActive(SearchOptions.REGEX)) {
			SearchDecoration.validateRegex(getFindString(), searchBarDecoration);
		} else {
			searchBarDecoration.hide();
		}
	}

}
