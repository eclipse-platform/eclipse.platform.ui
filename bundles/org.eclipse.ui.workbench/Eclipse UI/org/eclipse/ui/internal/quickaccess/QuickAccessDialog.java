/*******************************************************************************
 * Copyright (c) 2005, 2016 IBM Corporation and others.
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
 *     Tom Hochstein (Freescale) - Bug 393703 - NotHandledException selecting inactive command under 'Previous Choices' in Quick access
 *     René Brandstetter - Bug 433778
 *     Patrik Suzzi <psuzzi@gmail.com> - Bug 491410
 *******************************************************************************/
package org.eclipse.ui.internal.quickaccess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.eclipse.core.commands.Command;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.Assert;
import org.eclipse.e4.core.commands.ExpressionContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.SWTKeySupport;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.util.Util;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.progress.ProgressManagerUtil;
import org.eclipse.ui.internal.quickaccess.providers.ActionProvider;
import org.eclipse.ui.internal.quickaccess.providers.CommandProvider;
import org.eclipse.ui.internal.quickaccess.providers.EditorProvider;
import org.eclipse.ui.internal.quickaccess.providers.PerspectiveProvider;
import org.eclipse.ui.internal.quickaccess.providers.PreferenceProvider;
import org.eclipse.ui.internal.quickaccess.providers.PropertiesProvider;
import org.eclipse.ui.internal.quickaccess.providers.ViewProvider;
import org.eclipse.ui.internal.quickaccess.providers.WizardProvider;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.quickaccess.QuickAccessElement;

/**
 * This is the quick access popup dialog used in 3.x. The new quick access is
 * done through a shell in {@link SearchField}.
 *
 * @since 3.3
 *
 */
public class QuickAccessDialog extends PopupDialog {
	private TriggerSequence[] invokingCommandKeySequences;
	private Command invokingCommand;
	private QuickAccessContents contents;
	private KeyAdapter keyAdapter;
	private Text filterText;
	private IWorkbenchWindow window;
	private LinkedList<QuickAccessElement> previousPicksList = new LinkedList<>();
	private static final String TEXT_ARRAY = "textArray"; //$NON-NLS-1$
	private static final String TEXT_ENTRIES = "textEntries"; //$NON-NLS-1$
	private static final String ORDERED_PROVIDERS = "orderedProviders"; //$NON-NLS-1$
	private static final String ORDERED_ELEMENTS = "orderedElements"; //$NON-NLS-1$
	static final int MAXIMUM_NUMBER_OF_ELEMENTS = 60;
	static final int MAXIMUM_NUMBER_OF_TEXT_ENTRIES_PER_ELEMENT = 3;
	protected Map<QuickAccessElement, ArrayList<String>> textMap = new HashMap<>();
	protected Map<String, QuickAccessElement> elementMap = new HashMap<>();
	protected Map<String, QuickAccessProvider> providerMap;
	private final Display display;
	private String lastSelectionFilter = ""; //$NON-NLS-1$

	public QuickAccessDialog(final IWorkbenchWindow window, final Command invokingCommand) {
		super(ProgressManagerUtil.getDefaultParent(), SWT.RESIZE, true, true, // persist
																				// size
				false, // but not location
				true, true, QuickAccessMessages.QuickAccessContents_QuickAccess,
				QuickAccessMessages.QuickAccess_StartTypingToFindMatches);
		this.window = window;
		this.display = window.getShell() != null ? window.getShell().getDisplay() : null;
		WorkbenchWindow workbenchWindow = (WorkbenchWindow) window;
		final MWindow model = workbenchWindow.getModel();

		BusyIndicator.showWhile(window.getShell() == null ? null : window.getShell().getDisplay(), () -> {
			final CommandProvider commandProvider = new CommandProvider();
			commandProvider.setSnapshot(new ExpressionContext(model.getContext().getActiveLeaf()));
			List<QuickAccessProvider> providers = new ArrayList<>();
			providers.add(new PreviousPicksProvider(previousPicksList));
			providers.add(new EditorProvider());
			providers.add(new ViewProvider(model.getContext().get(MApplication.class), model));
			providers.add(new PerspectiveProvider());
			providers.add(commandProvider);
			providers.add(new ActionProvider());
			providers.add(new WizardProvider());
			providers.add(new PreferenceProvider());
			providers.add(new PropertiesProvider());
			providers.addAll(QuickAccessExtensionManager.getProviders(() -> {
				if (display != null) {
					display.asyncExec(() -> {
						QuickAccessDialog dialog = new QuickAccessDialog(window, invokingCommand);
						dialog.filterText.setText(lastSelectionFilter);
						dialog.open();
					});
				}
			}));
			providerMap = new HashMap<>();
			for (QuickAccessProvider provider : providers) {
				providerMap.put(provider.getId(), provider);
			}
			QuickAccessDialog.this.contents = new QuickAccessContents(
					providers.toArray(new QuickAccessProvider[providers.size()])) {
				@Override
				protected void updateFeedback(boolean filterTextEmpty, boolean showAllMatches) {
					TriggerSequence[] sequences = getInvokingCommandKeySequences();
					if (showAllMatches || sequences == null || sequences.length == 0) {
						setInfoText(""); //$NON-NLS-1$
					} else {
						setInfoText(NLS.bind(QuickAccessMessages.QuickAccess_PressKeyToShowAllMatches,
								sequences[0].format()));
					}
				}

				@Override
				protected void doClose() {
					QuickAccessDialog.this.close();
				}

				/**
				 * @param element
				 */
				void addPreviousPick(String text, QuickAccessElement element) {
					// previousPicksList:
					// Remove element from previousPicksList so
					// there are no duplicates
					// If list is max size, remove last(oldest)
					// element
					// Remove entries for removed element from
					// elementMap and textMap
					// Add element to front of previousPicksList
					previousPicksList.remove(element);
					if (previousPicksList.size() == MAXIMUM_NUMBER_OF_ELEMENTS) {
						QuickAccessElement removedElement = previousPicksList.removeLast();
						ArrayList<String> removedList = textMap.remove(removedElement);
						for (int i = 0; i < removedList.size(); i++) {
							elementMap.remove(removedList.get(i));
						}
					}
					previousPicksList.addFirst(element);

					// textMap:
					// Get list of strings for element from textMap
					// Create new list for element if there isn't
					// one and put
					// element->textList in textMap
					// Remove rememberedText from list
					// If list is max size, remove first(oldest)
					// string
					// Remove text from elementMap
					// Add rememberedText to list of strings for
					// element in textMap
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
						// If it replaced a different element update
						// textMap and
						// PreviousPicksList
						Object replacedElement = elementMap.put(text, element);
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

				@Override
				protected QuickAccessElement getPerfectMatch(String filter) {
					QuickAccessElement perfectMatch = elementMap.get(filter);
					return perfectMatch;
				}

				@Override
				protected void handleElementSelected(String text, Object selectedElement) {
					lastSelectionFilter = text;
					if (selectedElement instanceof QuickAccessElement) {
						addPreviousPick(text, (QuickAccessElement) selectedElement);
						storeDialog(getDialogSettings());

						/*
						 * Execute after the dialog has been fully closed/disposed and the correct
						 * EclipseContext is in place.
						 */
						final QuickAccessElement element = (QuickAccessElement) selectedElement;
						window.getShell().getDisplay().asyncExec(() -> element.execute());
					}
				}
			};
			restoreDialog();
			QuickAccessDialog.this.invokingCommand = invokingCommand;
			if (QuickAccessDialog.this.invokingCommand != null && !QuickAccessDialog.this.invokingCommand.isDefined()) {
				QuickAccessDialog.this.invokingCommand = null;
			} else {
				// Pre-fetch key sequence - do not change because
				// scope will
				// change later.
				getInvokingCommandKeySequences();
			}
			// create early
			create();
		});
		QuickAccessDialog.this.contents.refresh(""); //$NON-NLS-1$
	}

	@Override
	protected Control createTitleControl(Composite parent) {
		parent.getShell().setText(QuickAccessMessages.QuickAccessContents_QuickAccess);
		filterText = new Text(parent, SWT.NONE);

		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(filterText);

		contents.hookFilterText(filterText);
		filterText.addKeyListener(getKeyAdapter());

		return filterText;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		boolean isWin32 = Util.isWindows();
		GridLayoutFactory.fillDefaults().extendedMargins(isWin32 ? 0 : 3, 3, 2, 2).applyTo(composite);
		Label hintText = contents.createHintText(composite, SWT.DEFAULT);
		GridData gridData = new GridData(SWT.FILL, SWT.DEFAULT, true, false,
				((GridLayout) composite.getLayout()).numColumns, 1);
		gridData.horizontalIndent = IDialogConstants.HORIZONTAL_MARGIN;
		hintText.setLayoutData(gridData);

		Table table = contents.createTable(composite, getDefaultOrientation());
		table.addKeyListener(getKeyAdapter());

		return composite;
	}

	final protected TriggerSequence[] getInvokingCommandKeySequences() {
		if (invokingCommandKeySequences == null) {
			if (invokingCommand != null) {
				IBindingService bindingService = Adapters.adapt(window.getWorkbench(), IBindingService.class);
				invokingCommandKeySequences = bindingService.getActiveBindingsFor(invokingCommand.getId());
			}
		}
		return invokingCommandKeySequences;
	}

	private KeyAdapter getKeyAdapter() {
		if (keyAdapter == null) {
			keyAdapter = new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					int accelerator = SWTKeySupport.convertEventToUnmodifiedAccelerator(e);
					KeySequence keySequence = KeySequence
							.getInstance(SWTKeySupport.convertAcceleratorToKeyStroke(accelerator));
					TriggerSequence[] sequences = getInvokingCommandKeySequences();
					if (sequences == null)
						return;
					for (TriggerSequence sequence : sequences) {
						if (sequence.equals(keySequence)) {
							e.doit = false;
							contents.setShowAllMatches(!contents.getShowAllMatches());
							return;
						}
					}
				}
			};
		}
		return keyAdapter;
	}

	@Override
	protected Control getFocusControl() {
		return filterText;
	}

	@Override
	public boolean close() {
		storeDialog(getDialogSettings());
		return super.close();
	}

	@Override
	protected Point getDefaultSize() {
		GC gc = new GC(getContents());
		FontMetrics fontMetrics = gc.getFontMetrics();
		gc.dispose();
		int x = Dialog.convertHorizontalDLUsToPixels(fontMetrics, 300);
		if (x < 350) {
			x = 350;
		}
		int y = Dialog.convertVerticalDLUsToPixels(fontMetrics, 270);
		if (y < 420) {
			y = 420;
		}
		return new Point(x, y);
	}

	@Override
	protected Point getDefaultLocation(Point initialSize) {
		Point size = new Point(400, 400);
		Rectangle parentBounds = getParentShell().getBounds();
		int x = parentBounds.x + parentBounds.width / 2 - size.x / 2;
		int y = parentBounds.y + parentBounds.height / 2 - size.y / 2;
		return new Point(x, y);
	}

	@Override
	protected IDialogSettings getDialogSettings() {
		final IDialogSettings workbenchDialogSettings = WorkbenchPlugin.getDefault().getDialogSettings();
		IDialogSettings result = workbenchDialogSettings.getSection(getId());
		if (result == null) {
			result = workbenchDialogSettings.addNewSection(getId());
		}
		return result;
	}

	protected String getId() {
		return "org.eclipse.ui.internal.QuickAccess"; //$NON-NLS-1$
	}

	private void storeDialog(IDialogSettings dialogSettings) {
		String[] orderedElements = new String[previousPicksList.size()];
		String[] orderedProviders = new String[previousPicksList.size()];
		String[] textEntries = new String[previousPicksList.size()];
		ArrayList<String> arrayList = new ArrayList<>();
		for (int i = 0; i < orderedElements.length; i++) {
			QuickAccessElement quickAccessElement = previousPicksList.get(i);
			ArrayList<String> elementText = textMap.get(quickAccessElement);
			Assert.isNotNull(elementText);
			orderedElements[i] = quickAccessElement.getId();
			orderedProviders[i] = contents.getProviderFor(quickAccessElement).getId();
			arrayList.addAll(elementText);
			textEntries[i] = elementText.size() + ""; //$NON-NLS-1$
		}
		String[] textArray = arrayList.toArray(new String[arrayList.size()]);
		dialogSettings.put(ORDERED_ELEMENTS, orderedElements);
		dialogSettings.put(ORDERED_PROVIDERS, orderedProviders);
		dialogSettings.put(TEXT_ENTRIES, textEntries);
		dialogSettings.put(TEXT_ARRAY, textArray);
	}

	private void restoreDialog() {
		IDialogSettings dialogSettings = getDialogSettings();
		if (dialogSettings != null) {
			String[] orderedElements = dialogSettings.getArray(ORDERED_ELEMENTS);
			String[] orderedProviders = dialogSettings.getArray(ORDERED_PROVIDERS);
			String[] textEntries = dialogSettings.getArray(TEXT_ENTRIES);
			String[] textArray = dialogSettings.getArray(TEXT_ARRAY);
			elementMap = new HashMap<>();
			textMap = new HashMap<>();
			previousPicksList = new LinkedList<>();
			if (orderedElements != null && orderedProviders != null && textEntries != null && textArray != null) {
				int arrayIndex = 0;
				for (int i = 0; i < orderedElements.length; i++) {
					QuickAccessProvider quickAccessProvider = providerMap.get(orderedProviders[i]);
					int numTexts = Integer.parseInt(textEntries[i]);
					if (quickAccessProvider != null) {
						QuickAccessElement quickAccessElement = quickAccessProvider.getElementForId(orderedElements[i]);
						if (quickAccessElement != null) {
							contents.registerProviderFor(quickAccessElement, quickAccessProvider);
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

}
