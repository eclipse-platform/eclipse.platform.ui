/*******************************************************************************
 * Copyright (c) 2010, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Tom Hochstein (Freescale) - Bug 393703: NotHandledException selecting inactive command under 'Previous Choices' in Quick access
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 428050, 472654
 *     Brian de Alwis - Fix size computation to account for trim
 *     Markus Kuppe <bugs.eclipse.org@lemmster.de> - Bug 449485: [QuickAccess] "Widget is disposed" exception in errorlog during shutdown due to quickaccess.SearchField.storeDialog
 *     Elena Laskavaia <elaskavaia.cdt@gmail.com> - Bug 433746: [QuickAccess] SWTException on closing quick access shell
 *     Patrik Suzzi <psuzzi@gmail.com> - Bug 488926, 491278
 ******************************************************************************/
package org.eclipse.ui.internal.quickaccess;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionInfo;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.Assert;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.util.Geometry;
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
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.swt.IFocusService;


public class SearchField {

	private static final String TEXT_ARRAY = "textArray"; //$NON-NLS-1$
	private static final String TEXT_ENTRIES = "textEntries"; //$NON-NLS-1$
	private static final String ORDERED_PROVIDERS = "orderedProviders"; //$NON-NLS-1$
	private static final String ORDERED_ELEMENTS = "orderedElements"; //$NON-NLS-1$
	private static final int MAXIMUM_NUMBER_OF_ELEMENTS = 60;
	private static final int MAXIMUM_NUMBER_OF_TEXT_ENTRIES_PER_ELEMENT = 3;
	private static final String DIALOG_HEIGHT = "dialogHeight"; //$NON-NLS-1$
	private static final String DIALOG_WIDTH = "dialogWidth"; //$NON-NLS-1$

	Shell shell;
	private Text txtQuickAcesss;

	private QuickAccessContents quickAccessContents;

	private MWindow window;

	private Map<String, QuickAccessProvider> providerMap = new HashMap<>();

	private Map<String, QuickAccessElement> elementMap = new HashMap<>();

	private Map<QuickAccessElement, ArrayList<String>> textMap = new HashMap<>();

	private LinkedList<QuickAccessElement> previousPicksList = new LinkedList<>();
	private int dialogHeight = -1;
	private int dialogWidth = -1;
	private Control previousFocusControl;
	boolean activated = false;

	@Inject
	private EPartService partService;
	private Table table;

	private String selectedString = ""; //$NON-NLS-1$
	private AccessibleAdapter accessibleListener;

	@PostConstruct
	void createControls(final Composite parent, MApplication application, MWindow window) {
		this.window = window;
		final Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout());
		txtQuickAcesss = createText(comp);

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
				if (shell == null || shell.isDisposed() || txtQuickAcesss.isDisposed() || !shell.isVisible())
					return;
				quickAccessContents.doClose();
			}
		});

		hookUpSelectAll();

		final CommandProvider commandProvider = new CommandProvider();
		QuickAccessProvider[] providers = new QuickAccessProvider[] {
				new PreviousPicksProvider(previousPicksList),
				new EditorProvider(), new ViewProvider(application, window),
				new PerspectiveProvider(), commandProvider, new ActionProvider(),
				new WizardProvider(), new PreferenceProvider(), new PropertiesProvider() };
		for (int i = 0; i < providers.length; i++) {
			providerMap.put(providers[i].getId(), providers[i]);
		}
		restoreDialog();

		quickAccessContents = new QuickAccessContents(providers) {
			@Override
			protected void updateFeedback(boolean filterTextEmpty, boolean showAllMatches) {
			}

			@Override
			protected void doClose() {
				txtQuickAcesss.setText(""); //$NON-NLS-1$
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
				if (selectedElement instanceof QuickAccessElement) {
					QuickAccessElement element = (QuickAccessElement) selectedElement;
					addPreviousPick(string, element);
					txtQuickAcesss.setText(""); //$NON-NLS-1$
					element.execute();

					/*
					 * By design, attempting to activate a part that is already
					 * active does not change the focus. However in the case of
					 * using Quick Access, focus is not in the active part, so
					 * re-activating the active part results in focus being left
					 * behind in the text field. If this happens then assign
					 * focus to the active part explicitly.
					 */
					if (txtQuickAcesss.isFocusControl()) {
						MPart activePart = partService.getActivePart();
						if (activePart != null) {
							IPresentationEngine pe = activePart.getContext().get(
									IPresentationEngine.class);
							pe.focusGui(activePart);
						}
					}
				}
			}
		};
		quickAccessContents.hookFilterText(txtQuickAcesss);
		shell = new Shell(parent.getShell(), SWT.RESIZE | SWT.ON_TOP);
		shell.setBackground(shell.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		shell.setText(QuickAccessMessages.QuickAccess_EnterSearch); // just for debugging, not shown anywhere
		GridLayoutFactory.fillDefaults().applyTo(shell);
		table = quickAccessContents.createTable(shell, Window.getDefaultOrientation());
		txtQuickAcesss.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				// Once the focus event is complete, check if we should close the shell
				table.getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						checkFocusLost(table, txtQuickAcesss);
					}
				});
				activated = false;
			}

			@Override
			public void focusGained(FocusEvent e) {
				IHandlerService hs = SearchField.this.window.getContext().get(IHandlerService.class);
				if (commandProvider.getContextSnapshot() == null) {
					commandProvider.setSnapshot(hs.createContextSnapshot(true));
				}
				previousFocusControl = (Control) e.getSource();
				activated = true;
				showList();
			}
		});
		table.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				// Once the focus event is complete, check if we should close
				// the shell
				table.getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						checkFocusLost(table, txtQuickAcesss);
					}
				});
			}
		});
		txtQuickAcesss.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				showList();
			}
		});
		txtQuickAcesss.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.ESC) {
					activated = false;
					txtQuickAcesss.setText(""); //$NON-NLS-1$
					if (txtQuickAcesss == previousFocusControl) {
						txtQuickAcesss.getShell().forceFocus();
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
		quickAccessContents.createInfoLabel(shell);
	}


	private void showList() {
		boolean wasVisible = shell.getVisible();
		boolean nowVisible = txtQuickAcesss.getText().length() > 0 || activated;
		if (!wasVisible && nowVisible) {
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

	private Text createText(Composite parent) {
		Text text = new Text(parent, SWT.SEARCH);
		text.setToolTipText(QuickAccessMessages.QuickAccess_TooltipDescription);
		text.setMessage(QuickAccessMessages.QuickAccess_EnterSearch);

		GC gc = new GC(text);
		Point p = gc.textExtent(QuickAccessMessages.QuickAccess_EnterSearch);
		Rectangle r = text.computeTrim(0, 0, p.x, p.y);
		gc.dispose();

		// computeTrim() may result in r.x < 0
		GridDataFactory.fillDefaults().hint(r.width - r.x, SWT.DEFAULT).applyTo(text);
		return text;
	}

	private void hookUpSelectAll() {
		final IEclipseContext windowContext = window.getContext();
		IFocusService focus = windowContext.get(IFocusService.class);
		focus.addFocusTracker(txtQuickAcesss, SearchField.class.getName());

		Expression focusExpr = new Expression() {
			@Override
			public void collectExpressionInfo(ExpressionInfo info) {
				info.addVariableNameAccess(ISources.ACTIVE_FOCUS_CONTROL_ID_NAME);
			}

			@Override
			public EvaluationResult evaluate(IEvaluationContext context) {
				return EvaluationResult.valueOf(SearchField.class.getName().equals(
						context.getVariable(ISources.ACTIVE_FOCUS_CONTROL_ID_NAME)));
			}
		};

		IHandlerService whService = windowContext.get(IHandlerService.class);
		whService.activateHandler(IWorkbenchCommandConstants.EDIT_SELECT_ALL,
				new AbstractHandler() {
					@Override
					public Object execute(ExecutionEvent event) {
						txtQuickAcesss.selectAll();
						return null;
					}
				}, focusExpr);
		whService.activateHandler(IWorkbenchCommandConstants.EDIT_CUT, new AbstractHandler() {
			@Override
			public Object execute(ExecutionEvent event) {
				txtQuickAcesss.cut();
				return null;
			}
		}, focusExpr);
		whService.activateHandler(IWorkbenchCommandConstants.EDIT_COPY, new AbstractHandler() {
			@Override
			public Object execute(ExecutionEvent event) {
				txtQuickAcesss.copy();
				return null;
			}
		}, focusExpr);
		whService.activateHandler(IWorkbenchCommandConstants.EDIT_PASTE, new AbstractHandler() {
			@Override
			public Object execute(ExecutionEvent event) {
				txtQuickAcesss.paste();
				return null;
			}
		}, focusExpr);
	}

	/**
	 * This method was copy/pasted from JFace.
	 */
	private static Monitor getClosestMonitor(Display toSearch, Point toFind) {
		int closest = Integer.MAX_VALUE;

		Monitor[] monitors = toSearch.getMonitors();
		Monitor result = monitors[0];

		for (int idx = 0; idx < monitors.length; idx++) {
			Monitor current = monitors[idx];

			Rectangle clientArea = current.getClientArea();

			if (clientArea.contains(toFind)) {
				return current;
			}

			int distance = Geometry.distanceSquared(Geometry.centerPoint(clientArea), toFind);
			if (distance < closest) {
				closest = distance;
				result = current;
			}
		}

		return result;
	}

	/**
	 * This method was copy/pasted from JFace.
	 */
	private Rectangle getConstrainedShellBounds(Display display, Rectangle preferredSize) {
		Rectangle result = new Rectangle(preferredSize.x, preferredSize.y, preferredSize.width,
				preferredSize.height);

		Point topLeft = new Point(preferredSize.x, preferredSize.y);
		Monitor mon = getClosestMonitor(display, topLeft);
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
		Display display = txtQuickAcesss.getDisplay();
		Rectangle tempBounds = txtQuickAcesss.getBounds();
		Rectangle compBounds = display.map(txtQuickAcesss, null, tempBounds);
		int preferredWidth = dialogWidth == -1 ? 350 : dialogWidth;
		int width = Math.max(preferredWidth, compBounds.width);
		int height = dialogHeight == -1 ? 250 : dialogHeight;

		// If size would extend past the right edge of the shell, try to move it
		// to the left of the text
		Rectangle shellBounds = txtQuickAcesss.getShell().getBounds();
		if (compBounds.x + width > shellBounds.x + shellBounds.width){
			compBounds.x = Math.max(shellBounds.x, (compBounds.x + compBounds.width - width));
		}

		shell.setBounds(getConstrainedShellBounds(display, new Rectangle(compBounds.x, compBounds.y
				+ compBounds.height, width, height)));
		shell.layout();
	}

	public void activate(Control previousFocusControl) {
		this.previousFocusControl = previousFocusControl;
		if (!shell.isVisible()) {
			layoutShell();
			quickAccessContents.preOpen();
			shell.setVisible(true);
			addAccessibleListener();
			quickAccessContents.refresh(txtQuickAcesss.getText().toLowerCase());
		} else {
			quickAccessContents.setShowAllMatches(!quickAccessContents.getShowAllMatches());
		}
	}

	/**
	 * Checks if the text or shell has focus. If not, closes the shell.
	 *
	 * @param table
	 *            the shell's table
	 * @param text
	 *            the search text field
	 */
	protected void checkFocusLost(final Table table, final Text text) {
		if (!shell.isDisposed() && !table.isDisposed() && !text.isDisposed()) {
			if (table.getDisplay().getActiveShell() == table.getShell()) {
				// If the user selects the trim shell, leave focus on the text
				// so shell stays open
				text.setFocus();
				return;
			}
			if (!shell.isFocusControl() && !table.isFocusControl()
					&& !text.isFocusControl()) {
				quickAccessContents.doClose();
			}
		}
	}

	/**
	 * Adds a listener to the
	 * <code>org.eclipse.swt.accessibility.Accessible</code> object assigned to
	 * the Quick Access search box. The listener sets a name of a selected
	 * element in the search result list as a text to read for a screen reader.
	 */
	private void addAccessibleListener() {
		if (accessibleListener == null) {
			accessibleListener = new AccessibleAdapter() {
				@Override
				public void getName(AccessibleEvent e) {
					e.result = selectedString;
				}
			};
			txtQuickAcesss.getAccessible().addAccessibleListener(accessibleListener);
		}
	}

	/**
	 * Removes a listener from the
	 * <code>org.eclipse.swt.accessibility.Accessible</code> object assigned to
	 * the Quick Access search box.
	 */
	private void removeAccessibleListener() {
		if (accessibleListener != null) {
			txtQuickAcesss.getAccessible().removeAccessibleListener(accessibleListener);
			accessibleListener = null;
		}
		selectedString = ""; //$NON-NLS-1$
	}

	/**
	 * Notifies <code>org.eclipse.swt.accessibility.Accessible<code> object
	 * that selected item has been changed.
	 */
	private void notifyAccessibleTextChanged() {
		if (table.getSelection().length == 0) {
			return;
		}
		TableItem item = table.getSelection()[0];
		selectedString = NLS.bind(QuickAccessMessages.QuickAccess_SelectedString, item.getText(0),
				item.getText(1));
		txtQuickAcesss.getAccessible().sendEvent(ACC.EVENT_NAME_CHANGED, null);
	}

	private void restoreDialog() {
		IDialogSettings dialogSettings = getDialogSettings();
		if (dialogSettings != null) {
			String[] orderedElements = dialogSettings.getArray(ORDERED_ELEMENTS);
			String[] orderedProviders = dialogSettings.getArray(ORDERED_PROVIDERS);
			String[] textEntries = dialogSettings.getArray(TEXT_ENTRIES);
			String[] textArray = dialogSettings.getArray(TEXT_ARRAY);
			try {
				dialogHeight = dialogSettings.getInt(DIALOG_HEIGHT);
				dialogWidth = dialogSettings.getInt(DIALOG_WIDTH);
			} catch (NumberFormatException e) {
				dialogHeight = -1;
				dialogWidth = -1;
			}

			if (orderedElements != null && orderedProviders != null && textEntries != null
					&& textArray != null) {
				int arrayIndex = 0;
				for (int i = 0; i < orderedElements.length; i++) {
					QuickAccessProvider quickAccessProvider = providerMap.get(orderedProviders[i]);
					int numTexts = Integer.parseInt(textEntries[i]);
					if (quickAccessProvider != null) {
						QuickAccessElement quickAccessElement = quickAccessProvider
								.getElementForId(orderedElements[i]);
						if (quickAccessElement != null) {
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
							previousPicksList.add(quickAccessElement);
						}
					}
					arrayIndex += numTexts;
				}
			}
		}
	}

	@PreDestroy
	void dispose() {
		storeDialog();
	}

	private void storeDialog() {
		String[] orderedElements = new String[previousPicksList.size()];
		String[] orderedProviders = new String[previousPicksList.size()];
		String[] textEntries = new String[previousPicksList.size()];
		ArrayList<String> arrayList = new ArrayList<>();
		for (int i = 0; i < orderedElements.length; i++) {
			QuickAccessElement quickAccessElement = previousPicksList.get(i);
			ArrayList<String> elementText = textMap.get(quickAccessElement);
			Assert.isNotNull(elementText);
			orderedElements[i] = quickAccessElement.getId();
			orderedProviders[i] = quickAccessElement.getProvider().getId();
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

	private IDialogSettings getDialogSettings() {
		final IDialogSettings workbenchDialogSettings = WorkbenchPlugin.getDefault()
				.getDialogSettings();
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
			Object removedElement = previousPicksList.removeLast();
			ArrayList<String> removedList = textMap.remove(removedElement);
			for (int i = 0; i < removedList.size(); i++) {
				elementMap.remove(removedList.get(i));
			}
		}
		previousPicksList.addFirst(element);

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
	 * Returns the quick access shell for testing. Should not be referenced
	 * outside of the tests.
	 *
	 * @return the current quick access shell or <code>null</code>
	 */
	public Shell getQuickAccessShell() {
		return shell;
	}

	/**
	 * Returns the quick access search text for testing. Should not be
	 * referenced outside of the tests.
	 *
	 * @return the search text in the workbench window or <code>null</code>
	 */
	public Text getQuickAccessSearchText() {
		return txtQuickAcesss;
	}

	/**
	 * Returns the table in the shell for testing. Should not be referenced
	 * outside of the tests.
	 *
	 * @return the table created in the shell or <code>null</code>
	 */
	public Table getQuickAccessTable(){
		return table;
	}
}
