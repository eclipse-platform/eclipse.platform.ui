package org.eclipse.ui.internal.incubator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.Dialog;
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
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
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
import org.eclipse.ui.themes.ColorUtil;

/**
 * @since 3.3
 * 
 */
public class QuickAccessDialog extends PopupDialog {
	private static final int MAX_COUNT_PER_PROVIDER = 5;
	private static final int MAX_COUNT_TOTAL = 20;

	private Text filterText;

	private AbstractProvider[] providers;
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
	private Font italicsFont;
	private Color grayColor;

	/**
	 * @param parent
	 * @param providers
	 */
	QuickAccessDialog(IWorkbenchWindow window, AbstractProvider[] providers) {
		super(ProgressManagerUtil.getDefaultParent(), SWT.RESIZE, true, true,
				true, true, null, null);
		this.window = window;
		this.providers = providers;
		providers[0] = new PreviousPicksProvider();
		providerMap = new HashMap();
		for (int i = 0; i < providers.length; i++) {
			providerMap.put(providers[i].getId(), providers[i]);
		}
		restoreDialog();
	}

	protected Control createTitleControl(Composite parent) {
		filterText = new Text(parent, SWT.NONE);

		GC gc = new GC(parent);
		gc.setFont(parent.getFont());
		FontMetrics fontMetrics = gc.getFontMetrics();
		gc.dispose();

		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true,
				false).hint(SWT.DEFAULT,
				Dialog.convertHeightInCharsToPixels(fontMetrics, 1)).applyTo(
				filterText);

		filterText.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == 0x0D) {
					AbstractElement selectedElement = null;
					String text = filterText.getText();
					if (table.getSelectionCount() == 1) {
						QuickAccessEntry entry = (QuickAccessEntry) table.getSelection()[0]
								.getData();
						selectedElement = entry == null ? null : entry.element;
					}
					close();
					if (selectedElement != null) {
						handleElementSelected(text, selectedElement);
					}
					return;
				} else if (e.keyCode == SWT.ARROW_DOWN) {
					table.setFocus();
					if (table.getItemCount() > 1
							&& table.getItem(1).getData() != null) {
						table.setSelection(1);
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
				String text = ((Text) e.widget).getText();
				refreshTable(text);
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
		GridLayoutFactory.fillDefaults().margins(3, 2).applyTo(composite);
		Composite tableComposite = new Composite(composite, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(tableComposite);
		TableColumnLayout tableColumnLayout = new TableColumnLayout();
		tableComposite.setLayout(tableColumnLayout);
		table = new Table(tableComposite, SWT.SINGLE | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.FULL_SELECTION);
		tableColumnLayout.setColumnData(new TableColumn(table, SWT.NONE),
				new ColumnWeightData(0, 120));
		tableColumnLayout.setColumnData(new TableColumn(table, SWT.NONE),
				new ColumnWeightData(100, 100));

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

		table.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				// do nothing
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				AbstractElement selectedElement = null;
				String text = filterText.getText();
				if (table.getSelectionCount() == 1) {
					QuickAccessEntry quickAccessEntry = (QuickAccessEntry) table.getSelection()[0]
							.getData();
					selectedElement = quickAccessEntry == null ? null
							: quickAccessEntry.element;
				}
				close();
				if (selectedElement != null) {
					handleElementSelected(text, selectedElement);
				}
			}
		});

		final TextLayout textLayout = new TextLayout(table.getDisplay());
		Font boldFont = resourceManager.createFont(FontDescriptor.createFrom(
				table.getFont()).setStyle(SWT.BOLD));
		italicsFont = resourceManager.createFont(FontDescriptor.createFrom(
				table.getFont()).setStyle(SWT.ITALIC));
		grayColor = resourceManager.createColor(ColorUtil.blend(table
				.getBackground().getRGB(), table.getForeground().getRGB()));
		final TextStyle boldStyle = new TextStyle(boldFont, null, null);
		Listener listener = new Listener() {
			public void handleEvent(Event event) {
				QuickAccessEntry entry = (QuickAccessEntry) event.item.getData();
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
		refreshTable(""); //$NON-NLS-1$
		return composite;
	}

	/**
	 * 
	 */
	private void refreshTable(String filter) {
		TableItem[] items = table.getItems();
		int countTotal = 0;
		QuickAccessEntry lastEntry = null;
		for (int i = 0; i < providers.length && countTotal < MAX_COUNT_TOTAL; i++) {
			int countPerProvider = 0;
			AbstractProvider provider = providers[i];
			if (filter.length() > 0
					|| provider instanceof PreviousPicksProvider) {
				AbstractElement[] elements = provider.getElementsSorted();
				element_loop: for (int j = 0; j < elements.length
						&& countPerProvider < MAX_COUNT_PER_PROVIDER
						&& countTotal < MAX_COUNT_TOTAL; j++) {
					AbstractElement element = elements[j];
					QuickAccessEntry entry;
					if (filter.length() == 0) {
						if (i == 0) {
							entry = new QuickAccessEntry(element, provider,
									new int[0][0], new int[0][0]);
						} else {
							entry = null;
						}
					} else {
						entry = element.match(filter, provider);
					}
					if (entry != null) {
						entry.firstInCategory = countPerProvider == 0;
						if (entry.firstInCategory && lastEntry != null) {
							lastEntry.lastInCategory = true;
						}
						lastEntry = entry;
						TableItem item;
						if (countTotal < items.length) {
							item = items[countTotal];
							table.clear(countTotal);
						} else {
							item = new TableItem(table, SWT.NONE);
						}
						item.setData(entry);
//						Rectangle bounds = item.getBounds();
//						table.redraw(bounds.x, bounds.y, bounds.width,
//								bounds.height, false);
						countPerProvider++;
						countTotal++;
						continue element_loop;
					}
				}
			}
		}
		if (lastEntry != null) {
			lastEntry.lastInCategory = true;
		}
		if (countTotal < items.length) {
			table.remove(countTotal, items.length - 1);
		}
		if (filter.length() == 0) {
			TableItem item = new TableItem(table, SWT.NONE);
			item.setText(1,
					IncubatorMessages.CtrlEAction_StartTypingToFindMatches);
			item.setFont(1, italicsFont);
			item.setForeground(1, grayColor);
		}
		if (countTotal > 0) {
			table.setSelection(0);
		} else {
			table.deselectAll();
		}
	}

	protected Control getFocusControl() {
		return filterText;
	}

	public boolean close() {
		storeDialog(getDialogSettings());
		if (resourceManager != null) {
			resourceManager.dispose();
			resourceManager = null;
		}
		return super.close();
	}

	protected Point getInitialSize() {
		if (!getPersistBounds()) {
			return new Point(400, 400);
		}
		return super.getInitialSize();
	}

	protected Point getInitialLocation(Point initialSize) {
		if (!getPersistBounds()) {
			Point size = new Point(400, 400);
			Rectangle parentBounds = getParentShell().getBounds();
			int x = parentBounds.x + parentBounds.width / 2 - size.x / 2;
			int y = parentBounds.y + parentBounds.height / 2 - size.y / 2;
			return new Point(x, y);
		}
		return super.getInitialLocation(initialSize);
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
			AbstractElement abstractElement = (AbstractElement) previousPicksList
					.get(i);
			ArrayList elementText = (ArrayList) textMap.get(abstractElement);
			Assert.isNotNull(elementText);
			orderedElements[i] = abstractElement.getId();
			orderedProviders[i] = abstractElement.getProvider().getId();
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
					AbstractProvider abstractProvider = (AbstractProvider) providerMap
							.get(orderedProviders[i]);
					int numTexts = Integer.parseInt(textEntries[i]);
					if (abstractProvider != null) {
						AbstractElement abstractElement = abstractProvider
								.getElementForId(orderedElements[i]);
						if (abstractElement != null) {
							ArrayList arrayList = new ArrayList();
							for (int j = arrayIndex; j < arrayIndex + numTexts; j++) {
								arrayList.add(textArray[j]);
								elementMap.put(textArray[j], abstractElement);
							}
							textMap.put(abstractElement, arrayList);
							previousPicksList.add(abstractElement);
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
			if (selectedElement instanceof AbstractElement) {
				addPreviousPick(text, selectedElement);
				storeDialog(getDialogSettings());
				AbstractElement element = (AbstractElement) selectedElement;
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

	private class PreviousPicksProvider extends AbstractProvider {

		public AbstractElement getElementForId(String id) {
			return null;
		}

		public AbstractElement[] getElements() {
			return (AbstractElement[]) previousPicksList
					.toArray(new AbstractElement[previousPicksList.size()]);
		}

		public AbstractElement[] getElementsSorted() {
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
			return IncubatorMessages.CtrlEAction_Previous;
		}
	}

}
