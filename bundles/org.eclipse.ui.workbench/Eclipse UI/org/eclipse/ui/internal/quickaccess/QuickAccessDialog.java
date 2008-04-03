/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.quickaccess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.Command;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.SWTKeySupport;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.progress.ProgressManagerUtil;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.themes.ColorUtil;


/**
 * @since 3.3
 * 
 */
public class QuickAccessDialog extends PopupDialog {
	private static final int INITIAL_COUNT_PER_PROVIDER = 5;
	private static final int MAX_COUNT_TOTAL = 20;

	private Text filterText;

	private QuickAccessProvider[] providers;
	private IWorkbenchWindow window;

	private Table table;

	private LocalResourceManager resourceManager = new LocalResourceManager(
			JFaceResources.getResources());

	private static final String TEXT_ARRAY = "textArray"; //$NON-NLS-1$
	private static final String TEXT_ENTRIES = "textEntries"; //$NON-NLS-1$
	private static final String ORDERED_PROVIDERS = "orderedProviders"; //$NON-NLS-1$
	private static final String ORDERED_ELEMENTS = "orderedElements"; //$NON-NLS-1$
	static final int MAXIMUM_NUMBER_OF_ELEMENTS = 60;
	static final int MAXIMUM_NUMBER_OF_TEXT_ENTRIES_PER_ELEMENT = 3;

	protected String rememberedText;

	protected Map textMap = new HashMap();

	protected Map elementMap = new HashMap();

	private LinkedList previousPicksList = new LinkedList();

	protected Map providerMap;
	// private Font italicsFont;
	private Color grayColor;
	private TextLayout textLayout;
	private TriggerSequence[] invokingCommandKeySequences;
	private Command invokingCommand;
	private KeyAdapter keyAdapter;
	private boolean showAllMatches = false;
	protected boolean resized = false;


	QuickAccessDialog(IWorkbenchWindow window, final Command invokingCommand) {
		super(ProgressManagerUtil.getDefaultParent(), SWT.RESIZE, true, true, // persist size
				false, // but not location
				true, true, null,
				QuickAccessMessages.QuickAccess_StartTypingToFindMatches);

		this.window = window;
		BusyIndicator.showWhile(window.getShell() == null ? null : window
				.getShell().getDisplay(), new Runnable() {
			public void run() {
				QuickAccessDialog.this.providers = new QuickAccessProvider[] {
						new PreviousPicksProvider(), new EditorProvider(),
						new ViewProvider(), new PerspectiveProvider(),
						new CommandProvider(), new ActionProvider(),
						new WizardProvider(), new PreferenceProvider(),
						new PropertiesProvider() };
				providerMap = new HashMap();
				for (int i = 0; i < providers.length; i++) {
					providerMap.put(providers[i].getId(), providers[i]);
				}
				restoreDialog();
				QuickAccessDialog.this.invokingCommand = invokingCommand;
				if (QuickAccessDialog.this.invokingCommand != null
						&& !QuickAccessDialog.this.invokingCommand.isDefined()) {
					QuickAccessDialog.this.invokingCommand = null;
				} else {
					// Pre-fetch key sequence - do not change because scope will
					// change later.
					getInvokingCommandKeySequences();
				}
				// create early
				create();
			}
		});
		// Ugly hack to avoid bug 184045. If this gets fixed, replace the
		// following code with a call to refresh("").
		getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				final Shell shell = getShell();
				if (shell != null && !shell.isDisposed()) {
					Point size = shell.getSize();
					shell.setSize(size.x, size.y + 1);
				}
			}
		});
	}

	protected Control createTitleControl(Composite parent) {
		filterText = new Text(parent, SWT.NONE);

		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false)
				.applyTo(filterText);

		filterText.addKeyListener(getKeyAdapter());
		filterText.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == 0x0D) {
					handleSelection();
					return;
				} else if (e.keyCode == SWT.ARROW_DOWN) {
					int index = table.getSelectionIndex();
					if (index != -1 && table.getItemCount() > index + 1) {
						table.setSelection(index + 1);
					}
					table.setFocus();
				} else if (e.keyCode == SWT.ARROW_UP) {
					int index = table.getSelectionIndex();
					if (index != -1 && index >= 1) {
						table.setSelection(index - 1);
						table.setFocus();
					}
				} else if (e.character == 0x1B) // ESC
					close();
			}

			public void keyReleased(KeyEvent e) {
				// do nothing
			}
		});
		filterText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String text = ((Text) e.widget).getText().toLowerCase();
				refresh(text);
			}
		});

		return filterText;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.PopupDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		boolean isWin32 = "win32".equals(SWT.getPlatform()); //$NON-NLS-1$
		GridLayoutFactory.fillDefaults().extendedMargins(isWin32 ? 0 : 3, 3, 2, 2).applyTo(composite);
		Composite tableComposite = new Composite(composite, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(tableComposite);
		TableColumnLayout tableColumnLayout = new TableColumnLayout();
		tableComposite.setLayout(tableColumnLayout);
		table = new Table(tableComposite, SWT.SINGLE | SWT.FULL_SELECTION);
		textLayout = new TextLayout(table.getDisplay());
		textLayout.setOrientation(getDefaultOrientation());
		Font boldFont = resourceManager.createFont(FontDescriptor.createFrom(
				JFaceResources.getDialogFont()).setStyle(SWT.BOLD));
		textLayout.setFont(table.getFont());
		textLayout.setText(QuickAccessMessages.QuickAccess_AvailableCategories);
		int maxProviderWidth = (int) (textLayout.getBounds().width * 1.1);
		textLayout.setFont(boldFont);
		for (int i = 0; i < providers.length; i++) {
			QuickAccessProvider provider = providers[i];
			textLayout.setText(provider.getName());
			int width = (int) (textLayout.getBounds().width * 1.1);
			if (width > maxProviderWidth) {
				maxProviderWidth = width;
			}
		}
		tableColumnLayout.setColumnData(new TableColumn(table, SWT.NONE),
				new ColumnWeightData(0, maxProviderWidth));
		tableColumnLayout.setColumnData(new TableColumn(table, SWT.NONE),
				new ColumnWeightData(100, 100));
		table.getShell().addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				if (!showAllMatches) {
					if (!resized) {
						resized = true;
						e.display.timerExec(100, new Runnable() {
							public void run() {
								if (getShell() != null
										&& !getShell().isDisposed()) {
									refresh(filterText.getText().toLowerCase());
								}
								resized = false;
							}
						});
					}
				}
			}
		});

		table.addKeyListener(getKeyAdapter());
		table.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.ARROW_UP && table.getSelectionIndex() == 0) {
					filterText.setFocus();
				} else if (e.character == SWT.ESC) {
					close();
				}
			}

			public void keyReleased(KeyEvent e) {
				// do nothing
			}
		});
		table.addMouseListener(new MouseAdapter() {
			public void mouseUp(MouseEvent e) {

				if (table.getSelectionCount() < 1)
					return;

				if (e.button != 1)
					return;

				if (table.equals(e.getSource())) {
					Object o= table.getItem(new Point(e.x, e.y));
					TableItem selection= table.getSelection()[0];
					if (selection.equals(o))
						handleSelection();
				}
			}
		});

		table.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				// do nothing
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				handleSelection();
			}
		});

		// italicsFont = resourceManager.createFont(FontDescriptor.createFrom(
		// table.getFont()).setStyle(SWT.ITALIC));
		grayColor = resourceManager.createColor(ColorUtil.blend(table
				.getBackground().getRGB(), table.getForeground().getRGB()));
		final TextStyle boldStyle = new TextStyle(boldFont, null, null);
		Listener listener = new Listener() {
			public void handleEvent(Event event) {
				QuickAccessEntry entry = (QuickAccessEntry) event.item
						.getData();
				if (entry != null) {
					switch (event.type) {
					case SWT.MeasureItem:
						entry.measure(event, textLayout, resourceManager,
								boldStyle);
						break;
					case SWT.PaintItem:
						entry.paint(event, textLayout, resourceManager,
								boldStyle, grayColor);
						break;
					case SWT.EraseItem:
						entry.erase(event);
						break;
					}
				}
			}
		};
		table.addListener(SWT.MeasureItem, listener);
		table.addListener(SWT.EraseItem, listener);
		table.addListener(SWT.PaintItem, listener);
				
		return composite;
	}

	/**
	 * 
	 */
	private int computeNumberOfItems() {
		int height = table.getClientArea().height;
		int lineWidth = table.getLinesVisible() ? table.getGridLineWidth() : 0;
		return (height - lineWidth) / (table.getItemHeight() + lineWidth);
	}

	/**
	 * 
	 */
	private void refresh(String filter) {
		int numItems = computeNumberOfItems();

		// perfect match, to be selected in the table if not null
		QuickAccessElement perfectMatch = (QuickAccessElement) elementMap
				.get(filter);

		List[] entries = computeMatchingEntries(filter, perfectMatch, numItems);

		int selectionIndex = refreshTable(perfectMatch, entries);

		if (table.getItemCount() > 0) {
			table.setSelection(selectionIndex);
		} else if (filter.length() == 0) {
			{
				TableItem item = new TableItem(table, SWT.NONE);
				item.setText(0,
						QuickAccessMessages.QuickAccess_AvailableCategories);
				item.setForeground(0, grayColor);
			}
			for (int i = 0; i < providers.length; i++) {
				QuickAccessProvider provider = providers[i];
				TableItem item = new TableItem(table, SWT.NONE);
				item.setText(1, provider.getName());
				item.setForeground(1, grayColor);
			}
		}

		if (filter.length() == 0) {
			setInfoText(QuickAccessMessages.QuickAccess_StartTypingToFindMatches);
		} else {
			TriggerSequence[] sequences = getInvokingCommandKeySequences();
			if (showAllMatches || sequences == null || sequences.length == 0) {
				setInfoText(""); //$NON-NLS-1$
			} else {
				setInfoText(NLS
						.bind(
								QuickAccessMessages.QuickAccess_PressKeyToShowAllMatches,
								sequences[0].format()));
			}
		}
	}

	final protected TriggerSequence[] getInvokingCommandKeySequences() {
		if (invokingCommandKeySequences == null) {
			if (invokingCommand != null) {
				IBindingService bindingService = (IBindingService) window
						.getWorkbench().getAdapter(IBindingService.class);
				invokingCommandKeySequences = bindingService
						.getActiveBindingsFor(invokingCommand.getId());
			}
		}
		return invokingCommandKeySequences;
	}

	private KeyAdapter getKeyAdapter() {
		if (keyAdapter == null) {
			keyAdapter = new KeyAdapter() {
				public void keyPressed(KeyEvent e) {
					int accelerator = SWTKeySupport
							.convertEventToUnmodifiedAccelerator(e);
					KeySequence keySequence = KeySequence
							.getInstance(SWTKeySupport
									.convertAcceleratorToKeyStroke(accelerator));
					TriggerSequence[] sequences = getInvokingCommandKeySequences();
					if (sequences == null)
						return;
					for (int i = 0; i < sequences.length; i++) {
						if (sequences[i].equals(keySequence)) {
							e.doit = false;
							toggleShowAllMatches();
							return;
						}
					}
				}
			};
		}
		return keyAdapter;
	}

	private void toggleShowAllMatches() {
		showAllMatches = !showAllMatches;
		refresh(filterText.getText().toLowerCase());
	}

	private int refreshTable(QuickAccessElement perfectMatch, List[] entries) {
		if (table.getItemCount() > entries.length
				&& table.getItemCount() - entries.length > 20) {
			table.removeAll();
		}
		TableItem[] items = table.getItems();
		int selectionIndex = -1;
		int index = 0;
		for (int i = 0; i < providers.length; i++) {
			if (entries[i] != null) {
				boolean firstEntry = true;
				for (Iterator it = entries[i].iterator(); it.hasNext();) {
					QuickAccessEntry entry = (QuickAccessEntry) it.next();
					entry.firstInCategory = firstEntry;
					firstEntry = false;
					if (!it.hasNext()) {
						entry.lastInCategory = true;
					}
					TableItem item;
					if (index < items.length) {
						item = items[index];
						table.clear(index);
					} else {
						item = new TableItem(table, SWT.NONE);
					}
					if (perfectMatch == entry.element && selectionIndex == -1) {
						selectionIndex = index;
					}
					item.setData(entry);
					item.setText(0, entry.provider.getName());
					item.setText(1, entry.element.getLabel());
					if (SWT.getPlatform().equals("wpf")) { //$NON-NLS-1$
						item.setImage(1, entry.getImage(entry.element,
							resourceManager));
					}
					index++;
				}
			}
		}
		if (index < items.length) {
			table.remove(index, items.length - 1);
		}
		if (selectionIndex == -1) {
			selectionIndex = 0;
		}
		return selectionIndex;
	}

	private List[] computeMatchingEntries(String filter,
			QuickAccessElement perfectMatch, int maxCount) {
		// collect matches in an array of lists
		List[] entries = new ArrayList[providers.length];
		int[] indexPerProvider = new int[providers.length];
		int countPerProvider = Math.min(maxCount / 4,
				INITIAL_COUNT_PER_PROVIDER);
		int countTotal = 0;
		boolean perfectMatchAdded = true;
		if (perfectMatch != null) {
			// reserve one entry for the perfect match
			maxCount--;
			perfectMatchAdded = false;
		}
		boolean done;
		do {
			// will be set to false if we find a provider with remaining
			// elements
			done = true;
			for (int i = 0; i < providers.length
					&& (showAllMatches || countTotal < maxCount); i++) {
				if (entries[i] == null) {
					entries[i] = new ArrayList();
					indexPerProvider[i] = 0;
				}
				int count = 0;
				QuickAccessProvider provider = providers[i];
				if (filter.length() > 0
						|| provider instanceof PreviousPicksProvider
						|| showAllMatches) {
					QuickAccessElement[] elements = provider
							.getElementsSorted();
					int j = indexPerProvider[i];
					while (j < elements.length
							&& (showAllMatches || (count < countPerProvider && countTotal < maxCount))) {
						QuickAccessElement element = elements[j];
						QuickAccessEntry entry;
						if (filter.length() == 0) {
							if (i == 0 || showAllMatches) {
								entry = new QuickAccessEntry(element, provider,
										new int[0][0], new int[0][0]);
							} else {
								entry = null;
							}
						} else {
							entry = element.match(filter, provider);
						}
						if (entry != null) {
							entries[i].add(entry);
							count++;
							countTotal++;
							if (i == 0 && entry.element == perfectMatch) {
								perfectMatchAdded = true;
								maxCount = MAX_COUNT_TOTAL;
							}
						}
						j++;
					}
					indexPerProvider[i] = j;
					if (j < elements.length) {
						done = false;
					}
				}
			}
			// from now on, add one element per provider
			countPerProvider = 1;
		} while ((showAllMatches || countTotal < maxCount) && !done);
		if (!perfectMatchAdded) {
			QuickAccessEntry entry = perfectMatch.match(filter, providers[0]);
			if (entry != null) {
				if (entries[0] == null) {
					entries[0] = new ArrayList();
					indexPerProvider[0] = 0;
				}
				entries[0].add(entry);
			}
		}
		return entries;
	}

	protected Control getFocusControl() {
		return filterText;
	}

	public boolean close() {
		storeDialog(getDialogSettings());
		if (textLayout != null && !textLayout.isDisposed()) {
			textLayout.dispose();
		}
		if (resourceManager != null) {
			resourceManager.dispose();
			resourceManager = null;
		}
		return super.close();
	}

	protected Point getDefaultSize() {
		return new Point(350, 420);
	}

	protected Point getDefaultLocation(Point initialSize) {
		Point size = new Point(400, 400);
		Rectangle parentBounds = getParentShell().getBounds();
		int x = parentBounds.x + parentBounds.width / 2 - size.x / 2;
		int y = parentBounds.y + parentBounds.height / 2 - size.y / 2;
		return new Point(x, y);
	}

	protected IDialogSettings getDialogSettings() {
		final IDialogSettings workbenchDialogSettings = WorkbenchPlugin
				.getDefault().getDialogSettings();
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
		ArrayList arrayList = new ArrayList();
		for (int i = 0; i < orderedElements.length; i++) {
			QuickAccessElement quickAccessElement = (QuickAccessElement) previousPicksList
					.get(i);
			ArrayList elementText = (ArrayList) textMap.get(quickAccessElement);
			Assert.isNotNull(elementText);
			orderedElements[i] = quickAccessElement.getId();
			orderedProviders[i] = quickAccessElement.getProvider().getId();
			arrayList.addAll(elementText);
			textEntries[i] = elementText.size() + ""; //$NON-NLS-1$
		}
		String[] textArray = (String[]) arrayList.toArray(new String[arrayList
				.size()]);
		dialogSettings.put(ORDERED_ELEMENTS, orderedElements);
		dialogSettings.put(ORDERED_PROVIDERS, orderedProviders);
		dialogSettings.put(TEXT_ENTRIES, textEntries);
		dialogSettings.put(TEXT_ARRAY, textArray);
	}

	private void restoreDialog() {
		IDialogSettings dialogSettings = getDialogSettings();
		if (dialogSettings != null) {
			String[] orderedElements = dialogSettings
					.getArray(ORDERED_ELEMENTS);
			String[] orderedProviders = dialogSettings
					.getArray(ORDERED_PROVIDERS);
			String[] textEntries = dialogSettings.getArray(TEXT_ENTRIES);
			String[] textArray = dialogSettings.getArray(TEXT_ARRAY);
			elementMap = new HashMap();
			textMap = new HashMap();
			previousPicksList = new LinkedList();
			if (orderedElements != null && orderedProviders != null
					&& textEntries != null && textArray != null) {
				int arrayIndex = 0;
				for (int i = 0; i < orderedElements.length; i++) {
					QuickAccessProvider quickAccessProvider = (QuickAccessProvider) providerMap
							.get(orderedProviders[i]);
					int numTexts = Integer.parseInt(textEntries[i]);
					if (quickAccessProvider != null) {
						QuickAccessElement quickAccessElement = quickAccessProvider
								.getElementForId(orderedElements[i]);
						if (quickAccessElement != null) {
							ArrayList arrayList = new ArrayList();
							for (int j = arrayIndex; j < arrayIndex + numTexts; j++) {
								String text = textArray[j];
								// text length can be zero for old workspaces, see bug 190006
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

	protected void handleElementSelected(String text, Object selectedElement) {
		IWorkbenchPage activePage = window.getActivePage();
		if (activePage != null) {
			if (selectedElement instanceof QuickAccessElement) {
				addPreviousPick(text, selectedElement);
				storeDialog(getDialogSettings());
				QuickAccessElement element = (QuickAccessElement) selectedElement;
				element.execute();
			}
		}
	}

	/**
	 * @param element
	 */
	private void addPreviousPick(String text, Object element) {
		// previousPicksList:
		// Remove element from previousPicksList so there are no duplicates
		// If list is max size, remove last(oldest) element
		// Remove entries for removed element from elementMap and textMap
		// Add element to front of previousPicksList
		previousPicksList.remove(element);
		if (previousPicksList.size() == MAXIMUM_NUMBER_OF_ELEMENTS) {
			Object removedElement = previousPicksList.removeLast();
			ArrayList removedList = (ArrayList) textMap.remove(removedElement);
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
		ArrayList textList = (ArrayList) textMap.get(element);
		if (textList == null) {
			textList = new ArrayList();
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
			Object replacedElement = elementMap.put(text, element);
			if (replacedElement != null && !replacedElement.equals(element)) {
				textList = (ArrayList) textMap.get(replacedElement);
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
	 * 
	 */
	private void handleSelection() {
		QuickAccessElement selectedElement = null;
		String text = filterText.getText().toLowerCase();
		if (table.getSelectionCount() == 1) {
			QuickAccessEntry entry = (QuickAccessEntry) table
					.getSelection()[0].getData();
			selectedElement = entry == null ? null : entry.element;
		}
		close();
		if (selectedElement != null) {
			handleElementSelected(text, selectedElement);
		}
	}

	private class PreviousPicksProvider extends QuickAccessProvider {

		public QuickAccessElement getElementForId(String id) {
			return null;
		}

		public QuickAccessElement[] getElements() {
			return (QuickAccessElement[]) previousPicksList
					.toArray(new QuickAccessElement[previousPicksList.size()]);
		}

		public QuickAccessElement[] getElementsSorted() {
			return getElements();
		}

		public String getId() {
			return "org.eclipse.ui.previousPicks"; //$NON-NLS-1$
		}

		public ImageDescriptor getImageDescriptor() {
			return WorkbenchImages
					.getImageDescriptor(IWorkbenchGraphicConstants.IMG_OBJ_NODE);
		}

		public String getName() {
			return QuickAccessMessages.QuickAccess_Previous;
		}
	}

}
