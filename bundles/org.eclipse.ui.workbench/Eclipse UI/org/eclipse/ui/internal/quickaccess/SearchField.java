/*******************************************************************************
 * Copyright (c) 2010, 2016 IBM Corporation and others.
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
 *     Tom Hochstein (Freescale) - Bug 393703: NotHandledException selecting inactive command under 'Previous Choices' in Quick access
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 428050, 472654
 *     Brian de Alwis - Fix size computation to account for trim
 *     Markus Kuppe <bugs.eclipse.org@lemmster.de> - Bug 449485: [QuickAccess] "Widget is disposed" exception in errorlog during shutdown due to quickaccess.SearchField.storeDialog
 *     Elena Laskavaia <elaskavaia.cdt@gmail.com> - Bug 433746: [QuickAccess] SWTException on closing quick access shell
 *     Patrik Suzzi <psuzzi@gmail.com> - Bug 488926, 491278, 491291, 491312, 491293, 436788, 513436
 ******************************************************************************/
package org.eclipse.ui.internal.quickaccess;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionInfo;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.ui.bindings.internal.BindingTableManager;
import org.eclipse.e4.ui.bindings.internal.ContextSet;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.util.Util;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.ACC;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.quickaccess.providers.ActionProvider;
import org.eclipse.ui.internal.quickaccess.providers.CommandProvider;
import org.eclipse.ui.internal.quickaccess.providers.EditorProvider;
import org.eclipse.ui.internal.quickaccess.providers.PerspectiveProvider;
import org.eclipse.ui.internal.quickaccess.providers.PreferenceProvider;
import org.eclipse.ui.internal.quickaccess.providers.PropertiesProvider;
import org.eclipse.ui.internal.quickaccess.providers.ViewProvider;
import org.eclipse.ui.internal.quickaccess.providers.WizardProvider;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.progress.WorkbenchJob;
import org.eclipse.ui.quickaccess.QuickAccessElement;
import org.eclipse.ui.swt.IFocusService;

public class SearchField {

	private static final String QUICK_ACCESS_COMMAND_ID = "org.eclipse.ui.window.quickAccess"; //$NON-NLS-1$

	private static final String TEXT_ARRAY = "textArray"; //$NON-NLS-1$
	private static final String TEXT_ENTRIES = "textEntries"; //$NON-NLS-1$
	private static final String ORDERED_PROVIDERS = "orderedProviders"; //$NON-NLS-1$
	private static final String ORDERED_ELEMENTS = "orderedElements"; //$NON-NLS-1$
	private static final int MAXIMUM_NUMBER_OF_ELEMENTS = 60;
	private static final int MAXIMUM_NUMBER_OF_TEXT_ENTRIES_PER_ELEMENT = 3;
	private static final String DIALOG_HEIGHT = "dialogHeight"; //$NON-NLS-1$
	private static final String DIALOG_WIDTH = "dialogWidth"; //$NON-NLS-1$
	private static final int MINIMUM_DIALOG_HEIGHT = 50;
	private static final int MINIMUM_DIALOG_WIDTH = 150;

	private MApplication application;
	private MWindow window;

	private Text txtQuickAccess;
	Shell shell;
	private Table table;

	private String lastSelectionFilter = ""; //$NON-NLS-1$
	private QuickAccessContents quickAccessContents;

	private Map<String, QuickAccessElement> elementMap = Collections.synchronizedMap(new HashMap<>());
	private Map<QuickAccessElement, ArrayList<String>> textMap = Collections.synchronizedMap(new HashMap<>());
	private List<QuickAccessElement> previousPicksList = Collections.synchronizedList(new LinkedList<>());

	private int dialogHeight = -1;
	private int dialogWidth = -1;
	private Control previousFocusControl;
	boolean activated = false;

	private String selectedString = ""; //$NON-NLS-1$
	private AccessibleAdapter accessibleListener;

	@Inject
	private IBindingService bindingService;
	@Inject
	private EPartService partService;

	private TriggerSequence triggerSequence = null;

	private volatile boolean isLoadingPreviousElements;
	private Job restoreDialogEntriesJob;
	private UIJob refreshQuickAccessContents;

	@PostConstruct
	void createControls(final Composite parent, MApplication application, MWindow window) {
		this.window = window;
		this.application = application;
		final Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout());
		txtQuickAccess = createText(comp);
		updateQuickAccessText();

		parent.getShell().addControlListener(new ControlListener() {
			@Override
			public void controlResized(ControlEvent e) {
				closeDropDown();
			}

			@Override
			public void controlMoved(ControlEvent e) {
				closeDropDown();
			}

			private void closeDropDown() {
				if (shell == null || shell.isDisposed() || txtQuickAccess.isDisposed() || !shell.isVisible())
					return;
				quickAccessContents.doClose();
			}
		});

		hookUpSelectAll();

		txtQuickAccess.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				// release mouse button = click = CTRL+3 -> activate QuickAccess
				showList();
			}
		});
		txtQuickAccess.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				// Once the focus event is complete, check if we should close the shell
				if (table != null) {
					table.getDisplay().asyncExec(() -> checkFocusLost(table, txtQuickAccess));
				}
				activated = false;
			}

			@Override
			public void focusGained(FocusEvent e) {
				previousFocusControl = (Control) e.getSource();
				activated = true;
			}
		});
		txtQuickAccess.addModifyListener(e -> showList());
		txtQuickAccess.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.ESC) {
					activated = false;
					txtQuickAccess.setText(""); //$NON-NLS-1$
					if (txtQuickAccess == previousFocusControl) {
						txtQuickAccess.getShell().forceFocus();
					} else if (previousFocusControl != null && !previousFocusControl.isDisposed())
						previousFocusControl.setFocus();
				} else if (e.keyCode == SWT.ARROW_UP) {
					// Windows moves caret left/right when pressing up/down,
					// avoid this as the table selection changes for up/down
					e.doit = false;
				} else if (e.keyCode == SWT.ARROW_DOWN) {
					e.doit = false;
				}
				if (e.doit == false) {
					// arrow key pressed
					notifyAccessibleTextChanged();
				}
			}
		});
	}

	private void createContentsAndTable() {
		if (quickAccessContents != null) {
			return;
		}
		final CommandProvider commandProvider = new CommandProvider();
		txtQuickAccess.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				IHandlerService hs = SearchField.this.window.getContext().get(IHandlerService.class);
				if (commandProvider.getContextSnapshot() == null) {
					commandProvider.setSnapshot(hs.createContextSnapshot(true));
				}
			}
		});
		List<QuickAccessProvider> providers = new ArrayList<>();
		providers.add(new PreviousPicksProvider(previousPicksList));
		providers.add(new EditorProvider());
		providers.add(new ViewProvider(application, window));
		providers.add(new PerspectiveProvider());
		providers.add(commandProvider);
		providers.add(new ActionProvider());
		providers.add(new WizardProvider());
		providers.add(new PreferenceProvider());
		providers.add(new PropertiesProvider());
		providers.addAll(QuickAccessExtensionManager.getProviders(() -> {
			txtQuickAccess.getDisplay().asyncExec(() -> {
				txtQuickAccess.setText(lastSelectionFilter);
				txtQuickAccess.setFocus();
				SearchField.this.showList();
			});
		}));

		quickAccessContents = new QuickAccessContents(providers.toArray(new QuickAccessProvider[providers.size()])) {
			@Override
			protected void updateFeedback(boolean filterTextEmpty, boolean showAllMatches) {
			}

			@Override
			protected void doClose() {
				txtQuickAccess.setText(""); //$NON-NLS-1$
				resetProviders();
				dialogHeight = shell.getSize().y;
				dialogWidth = shell.getSize().x;
				shell.setVisible(false);
				removeAccessibleListener();
			}

			@Override
			protected QuickAccessElement getPerfectMatch(String filter) {
				return elementMap.get(filter);
			}

			@Override
			protected void handleElementSelected(String string, Object selectedElement) {
				lastSelectionFilter = string;
				if (selectedElement instanceof QuickAccessElement) {
					QuickAccessElement element = (QuickAccessElement) selectedElement;
					addPreviousPick(string, element);
					element.execute();

					// after execution, the search box might be disposed
					if (txtQuickAccess.isDisposed()) {
						return;
					}

					/*
					 * By design, attempting to activate a part that is already active does not
					 * change the focus. However in the case of using Quick Access, focus is not in
					 * the active part, so re-activating the active part results in focus being left
					 * behind in the text field. If this happens then assign focus to the active
					 * part explicitly.
					 */
					if (txtQuickAccess.isFocusControl()) {
						MPart activePart = partService.getActivePart();
						if (activePart != null) {
							IPresentationEngine pe = activePart.getContext().get(IPresentationEngine.class);
							pe.focusGui(activePart);
						}
					}

					if (shell.isVisible()) {
						// after selection, closes the shell
						quickAccessContents.doClose();
					}
				}
			}

			@Override
			public void refresh(String filter) {
				super.refresh(filter);
				if (isLoadingPreviousElements) {
					showHintText(QuickAccessMessages.QuickAccessContents_RestoringPreviousChoicesLabel, null);
				}
			}
		};

		restoreDialog();

		quickAccessContents.hookFilterText(txtQuickAccess);
		shell = new Shell(txtQuickAccess.getShell(), SWT.RESIZE | SWT.ON_TOP);
		shell.setBackground(shell.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		shell.setMinimumSize(new Point(MINIMUM_DIALOG_WIDTH, MINIMUM_DIALOG_HEIGHT));
		shell.setText(QuickAccessMessages.QuickAccess_EnterSearch); // just for debugging, not shown anywhere
		GridLayoutFactory.fillDefaults().applyTo(shell);
		quickAccessContents.createHintText(shell, Window.getDefaultOrientation());
		table = quickAccessContents.createTable(shell, Window.getDefaultOrientation());
		table.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				// Once the focus event is complete, check if we should close
				// the shell
				table.getDisplay().asyncExec(() -> checkFocusLost(table, txtQuickAccess));
			}
		});
		quickAccessContents.createInfoLabel(shell);
	}

	@Inject
	@Optional
	protected void keybindingPreferencesChanged(
			@SuppressWarnings("restriction") @Preference(nodePath = "org.eclipse.ui.workbench", value = "org.eclipse.ui.commands") String preferenceValue) {
		if (preferenceValue != null) {
			updateQuickAccessText();
		}

	}

	private void showList() {
		boolean wasVisible = shell != null && shell.getVisible();
		boolean nowVisible = txtQuickAccess.getText().length() > 0 || activated;
		if (!wasVisible && nowVisible) {
			createContentsAndTable();
			layoutShell();
			addAccessibleListener();
			quickAccessContents.preOpen();
		}
		if (wasVisible && !nowVisible) {
			removeAccessibleListener();
		}
		if (nowVisible) {
			notifyAccessibleTextChanged();
		}
		shell.setVisible(nowVisible);
	}

	@Inject
	private BindingTableManager manager;
	@Inject
	private ECommandService eCommandService;
	@Inject
	private IContextService contextService;

	/**
	 * Compute the best binding for the command and sets the trigger
	 *
	 */
	protected void updateQuickAccessTriggerSequence() {
		triggerSequence = bindingService.getBestActiveBindingFor(QUICK_ACCESS_COMMAND_ID);
		// FIXME Bug 491701 - [KeyBinding] get best active binding is not working
		if (triggerSequence == null) {
			ParameterizedCommand cmd = eCommandService.createCommand(QUICK_ACCESS_COMMAND_ID, null);
			ContextSet contextSet = manager.createContextSet(Arrays.asList(contextService.getDefinedContexts()));
			Binding binding = manager.getBestSequenceFor(contextSet, cmd);
			triggerSequence = (binding == null) ? null : binding.getTriggerSequence();
		}
	}

	private Text createText(Composite parent) {
		Text text = new Text(parent, SWT.SEARCH | SWT.ICON_CANCEL);
		text.setMessage(QuickAccessMessages.QuickAccess_EnterSearch);
		return text;
	}

	private void updateQuickAccessText() {
		if (txtQuickAccess == null || txtQuickAccess.isDisposed()) {
			return;
		}
		updateQuickAccessTriggerSequence();

		if (triggerSequence != null) {
			txtQuickAccess.setToolTipText(
					NLS.bind(QuickAccessMessages.QuickAccess_TooltipDescription, triggerSequence.format()));
		} else {
			txtQuickAccess.setToolTipText(QuickAccessMessages.QuickAccess_TooltipDescription_Empty);
		}

		GC gc = new GC(txtQuickAccess);

		// workaround for Bug 491317
		if (Util.isWin32() || Util.isGtk()) {
			FontMetrics fm = gc.getFontMetrics();
			int wHint = QuickAccessMessages.QuickAccess_EnterSearch.length() * fm.getAverageCharWidth();
			int hHint = fm.getHeight();
			gc.dispose();
			txtQuickAccess.setSize(txtQuickAccess.computeSize(wHint, hHint));
		} else {
			Point p = gc.textExtent(QuickAccessMessages.QuickAccess_EnterSearch);
			Rectangle r = txtQuickAccess.computeTrim(0, 0, p.x, p.y);
			gc.dispose();

			// computeTrim() may result in r.x < 0
			GridDataFactory.fillDefaults().hint(r.width - r.x, SWT.DEFAULT).applyTo(txtQuickAccess);
		}
		txtQuickAccess.requestLayout();

	}

	private void hookUpSelectAll() {
		final IEclipseContext windowContext = window.getContext();
		IFocusService focus = windowContext.get(IFocusService.class);
		focus.addFocusTracker(txtQuickAccess, SearchField.class.getName());

		Expression focusExpr = new Expression() {
			@Override
			public void collectExpressionInfo(ExpressionInfo info) {
				info.addVariableNameAccess(ISources.ACTIVE_FOCUS_CONTROL_ID_NAME);
			}

			@Override
			public EvaluationResult evaluate(IEvaluationContext context) {
				return EvaluationResult.valueOf(
						SearchField.class.getName().equals(context.getVariable(ISources.ACTIVE_FOCUS_CONTROL_ID_NAME)));
			}
		};

		IHandlerService whService = windowContext.get(IHandlerService.class);
		whService.activateHandler(IWorkbenchCommandConstants.EDIT_SELECT_ALL, new AbstractHandler() {
			@Override
			public Object execute(ExecutionEvent event) {
				txtQuickAccess.selectAll();
				return null;
			}
		}, focusExpr);
		whService.activateHandler(IWorkbenchCommandConstants.EDIT_CUT, new AbstractHandler() {
			@Override
			public Object execute(ExecutionEvent event) {
				txtQuickAccess.cut();
				return null;
			}
		}, focusExpr);
		whService.activateHandler(IWorkbenchCommandConstants.EDIT_COPY, new AbstractHandler() {
			@Override
			public Object execute(ExecutionEvent event) {
				txtQuickAccess.copy();
				return null;
			}
		}, focusExpr);
		whService.activateHandler(IWorkbenchCommandConstants.EDIT_PASTE, new AbstractHandler() {
			@Override
			public Object execute(ExecutionEvent event) {
				txtQuickAccess.paste();
				return null;
			}
		}, focusExpr);
	}

	/**
	 * This method was copy/pasted from JFace.
	 */
	private Rectangle getConstrainedShellBounds(Display display, Rectangle preferredSize) {
		Rectangle result = new Rectangle(preferredSize.x, preferredSize.y, preferredSize.width, preferredSize.height);

		Point topLeft = new Point(preferredSize.x, preferredSize.y);
		Monitor mon = Util.getClosestMonitor(display, topLeft);
		Rectangle bounds = mon.getClientArea();

		if (result.height > bounds.height) {
			result.height = bounds.height;
		}

		if (result.width > bounds.width) {
			result.width = bounds.width;
		}

		result.x = Math.max(bounds.x, Math.min(result.x, bounds.x + bounds.width - result.width));
		result.y = Math.max(bounds.y, Math.min(result.y, bounds.y + bounds.height - result.height));

		return result;
	}

	void layoutShell() {
		Display display = txtQuickAccess.getDisplay();
		Rectangle tempBounds = txtQuickAccess.getBounds();
		Rectangle compBounds = display.map(txtQuickAccess, null, tempBounds);
		Rectangle shellBounds = txtQuickAccess.getShell().getBounds();
		int preferredWidth = Math.max(MINIMUM_DIALOG_WIDTH,
				dialogWidth == -1 ? (int) (shellBounds.width * 0.6) : dialogWidth);
		int width = Math.max(preferredWidth, compBounds.width);
		int height = Math.max(MINIMUM_DIALOG_HEIGHT,
				dialogHeight == -1 ? (int) (shellBounds.height * 0.9) : dialogHeight);

		// If size would extend past the right edge of the shell, try to move it
		// to the left of the text
		if (compBounds.x + width > shellBounds.x + shellBounds.width) {
			compBounds.x = Math.max(shellBounds.x, (compBounds.x + compBounds.width - width));
		}

		shell.setBounds(getConstrainedShellBounds(display,
				new Rectangle(compBounds.x, compBounds.y + compBounds.height, width, height)));
		shell.layout();
	}

	public void activate(Control previousFocusControl) {
		this.previousFocusControl = previousFocusControl;
		createContentsAndTable();
		if (!shell.isVisible()) {
			layoutShell();
			quickAccessContents.preOpen();
			shell.setVisible(true);
			addAccessibleListener();
			quickAccessContents.refresh(txtQuickAccess.getText().toLowerCase());
		} else {
			quickAccessContents.setShowAllMatches(!quickAccessContents.getShowAllMatches());
		}
	}

	/**
	 * Checks if the text or shell has focus. If not, closes the shell.
	 *
	 * @param table the shell's table
	 * @param text  the search text field
	 */
	protected void checkFocusLost(final Table table, final Text text) {
		if (shell == null) {
			return;
		}
		if (!shell.isDisposed() && !table.isDisposed() && !text.isDisposed()) {
			if (table.getDisplay().getActiveShell() == table.getShell()) {
				// If the user selects the trim shell, leave focus on the text
				// so shell stays open
				text.setFocus();
				return;
			}
			if (!shell.isFocusControl() && !table.isFocusControl() && !text.isFocusControl()) {
				quickAccessContents.doClose();
			}
		}
	}

	/**
	 * Adds a listener to the <code>org.eclipse.swt.accessibility.Accessible</code>
	 * object assigned to the Quick Access search box. The listener sets a name of a
	 * selected element in the search result list as a text to read for a screen
	 * reader.
	 */
	private void addAccessibleListener() {
		if (accessibleListener == null) {
			accessibleListener = new AccessibleAdapter() {
				@Override
				public void getName(AccessibleEvent e) {
					e.result = selectedString;
				}
			};
			txtQuickAccess.getAccessible().addAccessibleListener(accessibleListener);
		}
	}

	/**
	 * Removes a listener from the
	 * <code>org.eclipse.swt.accessibility.Accessible</code> object assigned to the
	 * Quick Access search box.
	 */
	private void removeAccessibleListener() {
		if (accessibleListener != null) {
			txtQuickAccess.getAccessible().removeAccessibleListener(accessibleListener);
			accessibleListener = null;
		}
		selectedString = ""; //$NON-NLS-1$
	}

	/**
	 * Notifies <code>org.eclipse.swt.accessibility.Accessible<code> object that
	 * selected item has been changed.
	 */
	private void notifyAccessibleTextChanged() {
		if (table.getSelection().length == 0) {
			return;
		}
		TableItem item = table.getSelection()[0];
		selectedString = NLS.bind(QuickAccessMessages.QuickAccess_SelectedString, item.getText(0), item.getText(1));
		txtQuickAccess.getAccessible().sendEvent(ACC.EVENT_NAME_CHANGED, null);
	}

	private void restoreDialog() {
		IDialogSettings dialogSettings = getDialogSettings();
		if (dialogSettings != null) {
			try {
				dialogHeight = dialogSettings.getInt(DIALOG_HEIGHT);
				dialogWidth = dialogSettings.getInt(DIALOG_WIDTH);
			} catch (NumberFormatException e) {
				dialogHeight = -1;
				dialogWidth = -1;
			}

			/*
			 * add place holders, so that we don't change element order due to first
			 * restoring non-UI elements and then restoring UI elements
			 */
			String[] orderedProviders = dialogSettings.getArray(ORDERED_PROVIDERS);
			if (orderedProviders != null) {
				previousPicksList.addAll(Arrays.asList(new QuickAccessElement[orderedProviders.length]));
			}

			isLoadingPreviousElements = true;
			refreshQuickAccessContents = new WorkbenchJob("Finish restoring quick access elements") { //$NON-NLS-1$
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					try {
						if (txtQuickAccess.isDisposed()) {
							return Status.OK_STATUS;
						}
						restoreDialogEntries(dialogSettings, true, monitor);
						quickAccessContents.refresh(txtQuickAccess.getText());
						List<QuickAccessElement> previousPicks = getLoadedPreviousPicks();
						previousPicksList.clear();
						previousPicksList.addAll(previousPicks);
					} finally {
						isLoadingPreviousElements = false;
					}
					return Status.OK_STATUS;
				}
			};
			refreshQuickAccessContents.setRule(RESTORE_DIALOG_ENTRIES_SCHEDULING_RULE);
			refreshQuickAccessContents.setSystem(true);
			restoreDialogEntriesJob = Job.createSystem("Restore quick access elements", (IProgressMonitor monitor) -> { //$NON-NLS-1$
				if (txtQuickAccess.isDisposed()) {
					isLoadingPreviousElements = false;
					return;
				}
				try {
					restoreDialogEntries(dialogSettings, false, monitor);
				} catch (OperationCanceledException e) {
					// ignore
				} finally {
					refreshQuickAccessContents.schedule();
				}
			});
			restoreDialogEntriesJob.setRule(RESTORE_DIALOG_ENTRIES_SCHEDULING_RULE);
			restoreDialogEntriesJob.schedule();
		}
	}

	private void restoreDialogEntries(IDialogSettings dialogSettings, boolean restoreUiElements,
			IProgressMonitor monitor) throws OperationCanceledException {
		String[] orderedElements = dialogSettings.getArray(ORDERED_ELEMENTS);
		String[] orderedProviders = dialogSettings.getArray(ORDERED_PROVIDERS);
		String[] textEntries = dialogSettings.getArray(TEXT_ENTRIES);
		String[] textArray = dialogSettings.getArray(TEXT_ARRAY);

		if (orderedElements != null && orderedProviders != null && textEntries != null && textArray != null) {
			int arrayIndex = 0;
			int elementCount = orderedElements.length;
			SubMonitor subMonitor = SubMonitor.convert(monitor, "Restoring quick access elements.", elementCount); //$NON-NLS-1$

			for (int i = 0; i < elementCount; i++) {
				QuickAccessProvider quickAccessProvider = quickAccessContents.getProvider(orderedProviders[i]);
				int numTexts = Integer.parseInt(textEntries[i]);
				subMonitor.split(1).setTaskName("Restoring quick access element \"" + orderedElements[i] + "\"."); //$NON-NLS-1$ //$NON-NLS-2$
				if (quickAccessProvider != null && restoreUiElements == quickAccessProvider.requiresUiAccess()) {
					QuickAccessElement quickAccessElement = quickAccessProvider.getElementForId(orderedElements[i]);

					if (quickAccessElement != null) {
						quickAccessContents.registerProviderFor(quickAccessElement, quickAccessProvider);
						ArrayList<String> arrayList = new ArrayList<>();
						for (int j = arrayIndex; j < arrayIndex + numTexts; j++) {
							String text = textArray[j];
							// text length can be zero for old workspaces,
							// see bug 190006
							if (text.length() > 0) {
								arrayList.add(text);
								elementMap.put(text, quickAccessElement);
							}
						}
						textMap.put(quickAccessElement, arrayList);
						previousPicksList.set(i, quickAccessElement);
					}
				}
				arrayIndex += numTexts;
			}
		}
	}

	@PreDestroy
	void dispose() {
		if (restoreDialogEntriesJob != null) {
			restoreDialogEntriesJob.cancel();
		}
		if (refreshQuickAccessContents != null) {
			refreshQuickAccessContents.cancel();
		}
		storeDialog();
		elementMap.clear();
		previousPicksList.clear();
		textMap.clear();
		partService = null;
	}

	private void storeDialog() {
		List<QuickAccessElement> previousPicks = getLoadedPreviousPicks();
		String[] orderedElements = new String[previousPicks.size()];
		String[] orderedProviders = new String[previousPicks.size()];
		String[] textEntries = new String[previousPicks.size()];
		ArrayList<String> arrayList = new ArrayList<>();
		for (int i = 0; i < orderedElements.length; i++) {
			QuickAccessElement quickAccessElement = previousPicks.get(i);
			ArrayList<String> elementText = textMap.get(quickAccessElement);
			Assert.isNotNull(elementText);
			orderedElements[i] = quickAccessElement.getId();
			orderedProviders[i] = quickAccessContents.getProviderFor(quickAccessElement).getId();
			arrayList.addAll(elementText);
			textEntries[i] = elementText.size() + ""; //$NON-NLS-1$
		}
		String[] textArray = arrayList.toArray(new String[arrayList.size()]);
		IDialogSettings dialogSettings = getDialogSettings();
		dialogSettings.put(ORDERED_ELEMENTS, orderedElements);
		dialogSettings.put(ORDERED_PROVIDERS, orderedProviders);
		dialogSettings.put(TEXT_ENTRIES, textEntries);
		dialogSettings.put(TEXT_ARRAY, textArray);
		dialogSettings.put(DIALOG_HEIGHT, dialogHeight);
		dialogSettings.put(DIALOG_WIDTH, dialogWidth);
	}

	/**
	 * If the original list was not fully restored yet, it may contain null
	 * elements, so we return here only already resolved, non null elements
	 */
	private List<QuickAccessElement> getLoadedPreviousPicks() {
		List<QuickAccessElement> previousPicks = previousPicksList.stream().filter(Objects::nonNull)
				.collect(Collectors.toList());
		return previousPicks;
	}

	private IDialogSettings getDialogSettings() {
		final IDialogSettings workbenchDialogSettings = WorkbenchPlugin.getDefault().getDialogSettings();
		IDialogSettings result = workbenchDialogSettings.getSection(getId());
		if (result == null) {
			result = workbenchDialogSettings.addNewSection(getId());
		}
		return result;
	}

	private String getId() {
		return "org.eclipse.ui.internal.QuickAccess"; //$NON-NLS-1$
	}

	/**
	 * @param element
	 */
	private void addPreviousPick(String text, QuickAccessElement element) {
		// previousPicksList:
		// Remove element from previousPicksList so there are no duplicates
		// If list is max size, remove last(oldest) element
		// Remove entries for removed element from elementMap and textMap
		// Add element to front of previousPicksList

		previousPicksList.remove(element);
		if (previousPicksList.size() == MAXIMUM_NUMBER_OF_ELEMENTS) {
			Object removedElement = previousPicksList.remove(previousPicksList.size() - 1);
			ArrayList<String> removedList = textMap.remove(removedElement);
			for (int i = 0; i < removedList.size(); i++) {
				elementMap.remove(removedList.get(i));
			}
		}
		previousPicksList.add(0, element);

		// textMap:
		// Get list of strings for element from textMap
		// Create new list for element if there isn't one and put
		// element->textList in textMap
		// Remove rememberedText from list
		// If list is max size, remove first(oldest) string
		// Remove text from elementMap
		// Add rememberedText to list of strings for element in textMap
		ArrayList<String> textList = textMap.get(element);
		if (textList == null) {
			textList = new ArrayList<>();
			textMap.put(element, textList);
		}

		textList.remove(text);
		if (textList.size() == MAXIMUM_NUMBER_OF_TEXT_ENTRIES_PER_ELEMENT) {
			Object removedText = textList.remove(0);
			elementMap.remove(removedText);
		}

		if (text.length() > 0) {
			textList.add(text);

			// elementMap:
			// Put rememberedText->element in elementMap
			// If it replaced a different element update textMap and
			// PreviousPicksList
			QuickAccessElement replacedElement = elementMap.put(text, element);
			if (replacedElement != null && !replacedElement.equals(element)) {
				textList = textMap.get(replacedElement);
				if (textList != null) {
					textList.remove(text);
					if (textList.isEmpty()) {
						textMap.remove(replacedElement);
						previousPicksList.remove(replacedElement);
					}
				}
			}
		}
	}

	/**
	 * Returns the quick access shell for testing. Should not be referenced outside
	 * of the tests.
	 *
	 * @return the current quick access shell or <code>null</code>
	 */
	public Shell getQuickAccessShell() {
		createContentsAndTable();
		return shell;
	}

	/**
	 * Returns the quick access search text for testing. Should not be referenced
	 * outside of the tests.
	 *
	 * @return the search text in the workbench window or <code>null</code>
	 */
	public Text getQuickAccessSearchText() {
		return txtQuickAccess;
	}

	/**
	 * Returns the table in the shell for testing. Should not be referenced outside
	 * of the tests.
	 *
	 * @return the table created in the shell or <code>null</code>
	 */
	public Table getQuickAccessTable() {
		createContentsAndTable();
		return table;
	}

	/**
	 * Bug 539510: in case of multiple workbench windows we must avoid loading
	 * commands simultaneously for each window
	 */
	private static final ISchedulingRule RESTORE_DIALOG_ENTRIES_SCHEDULING_RULE = new ISchedulingRule() {
		@Override
		public boolean isConflicting(ISchedulingRule rule) {
			return RESTORE_DIALOG_ENTRIES_SCHEDULING_RULE == rule;
		}

		@Override
		public boolean contains(ISchedulingRule rule) {
			return RESTORE_DIALOG_ENTRIES_SCHEDULING_RULE == rule;
		}
	};
}
