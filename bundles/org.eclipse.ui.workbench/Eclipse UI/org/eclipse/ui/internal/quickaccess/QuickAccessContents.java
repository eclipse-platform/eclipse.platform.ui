/*******************************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation and others.
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
import java.util.Iterator;
import java.util.List;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.util.Util;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.themes.ColorUtil;


/**
 * @since 3.3
 * 
 */
public abstract class QuickAccessContents {
	private static final int INITIAL_COUNT_PER_PROVIDER = 5;
	private static final int MAX_COUNT_TOTAL = 20;

	protected Text filterText;

	private QuickAccessProvider[] providers;

	protected Table table;

	private LocalResourceManager resourceManager = new LocalResourceManager(
			JFaceResources.getResources());

	protected String rememberedText;

	// private Font italicsFont;
	private Color grayColor;
	private TextLayout textLayout;
	private boolean showAllMatches = false;
	protected boolean resized = false;


	public QuickAccessContents(QuickAccessProvider[] providers) {
		this.providers = providers;
	}


	/**
	 * 
	 */
	private int computeNumberOfItems() {
		Rectangle rect = table.getClientArea ();
		int itemHeight = table.getItemHeight ();
		int headerHeight = table.getHeaderHeight ();
		return (rect.height - headerHeight + itemHeight - 1) / (itemHeight + table.getGridLineWidth());
	}

	/**
	 * 
	 */
	public void refresh(String filter) {
		int numItems = computeNumberOfItems();

		// perfect match, to be selected in the table if not null
		QuickAccessElement perfectMatch = getPerfectMatch(filter);

		List[] entries = computeMatchingEntries(filter, perfectMatch, numItems);

		int selectionIndex = refreshTable(perfectMatch, entries);

		boolean filterTextEmpty = filter.length() == 0;
		if (table.getItemCount() > 0) {
			table.setSelection(selectionIndex);
		} else if (filterTextEmpty) {
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

		updateFeedback(filterTextEmpty, showAllMatches);
	}


	/**
	 * @param filter
	 * @return
	 */
	abstract QuickAccessElement getPerfectMatch(String filter);

	/**
	 * @param filterTextEmpty
	 */
	abstract void updateFeedback(boolean filterTextEmpty, boolean showAllMatches);

	public void toggleShowAllMatches() {
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
					if (Util.isWpf()) {
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
 || provider.isAlwaysPresent()
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

	public void doDispose() {
		if (textLayout != null && !textLayout.isDisposed()) {
			textLayout.dispose();
		}
		if (resourceManager != null) {
			resourceManager.dispose();
			resourceManager = null;
		}
	}

	protected Point getDefaultSize() {
		return new Point(350, 420);
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

	abstract void handleElementSelected(String text, Object selectedElement);

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
		doClose();
		if (selectedElement != null) {
			handleElementSelected(text, selectedElement);
		}
	}

	/**
	 * Attempts to close the quick access dialog/shell. Must be called from a UI
	 * thread. Default implementation does nothing.
	 */
	abstract void doClose();

	/**
	 * @param filterText2
	 */
	public void hookFilterText(Text filterText) {
		this.filterText = filterText;
		filterText.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				switch (e.keyCode) {
				case SWT.CR:
				case SWT.KEYPAD_CR:
					handleSelection();
					break;
				case SWT.ARROW_DOWN:
					int index = table.getSelectionIndex();
					if (index != -1 && table.getItemCount() > index + 1) {
						table.setSelection(index + 1);
					}
					break;
				case SWT.ARROW_UP:
					index = table.getSelectionIndex();
					if (index != -1 && index >= 1) {
						table.setSelection(index - 1);
					}
					break;
				case SWT.ESC:
					doClose();
					break;
				}
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
	}

	/**
	 * @param composite
	 */
	public Table createTable(Composite composite, int defaultOrientation) {
		composite.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				doDispose();
			}
		});
		Composite tableComposite = new Composite(composite, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(tableComposite);
		TableColumnLayout tableColumnLayout = new TableColumnLayout();
		tableComposite.setLayout(tableColumnLayout);
		table = new Table(tableComposite, SWT.SINGLE | SWT.FULL_SELECTION);
		textLayout = new TextLayout(table.getDisplay());
		textLayout.setOrientation(defaultOrientation);
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
		tableColumnLayout.setColumnData(new TableColumn(table, SWT.NONE), new ColumnWeightData(0,
				maxProviderWidth));
		tableColumnLayout.setColumnData(new TableColumn(table, SWT.NONE), new ColumnWeightData(100,
				100));
		table.getShell().addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				if (!showAllMatches) {
					if (!resized) {
						resized = true;
						e.display.timerExec(100, new Runnable() {
							public void run() {
								if (table != null && !table.isDisposed()) {
									refresh(filterText.getText().toLowerCase());
								}
								resized = false;
							}
						});
					}
				}
			}
		});

		table.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.ARROW_UP && table.getSelectionIndex() == 0) {
					filterText.setFocus();
				} else if (e.character == SWT.ESC) {
					doClose();
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
					Object o = table.getItem(new Point(e.x, e.y));
					TableItem selection = table.getSelection()[0];
					if (selection.equals(o))
						handleSelection();
				}
			}
		});

		table.addMouseMoveListener(new MouseMoveListener() {
			TableItem lastItem = null;

			public void mouseMove(MouseEvent e) {
				if (table.equals(e.getSource())) {
					Object o = table.getItem(new Point(e.x, e.y));
					if (lastItem == null ^ o == null) {
						table.setCursor(o == null ? null : table.getDisplay().getSystemCursor(
								SWT.CURSOR_HAND));
					}
					if (o instanceof TableItem) {
						if (!o.equals(lastItem)) {
							lastItem = (TableItem) o;
							table.setSelection(new TableItem[] { lastItem });
						}
					} else if (o == null) {
						lastItem = null;
					}
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

		final TextStyle boldStyle;
		if (PlatformUI.getPreferenceStore().getBoolean(
				IWorkbenchPreferenceConstants.USE_COLORED_LABELS)) {
			boldStyle = new TextStyle(boldFont, null, null);
			// italicsFont =
			// resourceManager.createFont(FontDescriptor.createFrom(
			// table.getFont()).setStyle(SWT.ITALIC));
			grayColor = resourceManager.createColor(ColorUtil.blend(table.getBackground().getRGB(),
					table.getForeground().getRGB()));
		} else {
			boldStyle = null;
		}
		Listener listener = new Listener() {
			public void handleEvent(Event event) {
				QuickAccessEntry entry = (QuickAccessEntry) event.item.getData();
				if (entry != null) {
					switch (event.type) {
					case SWT.MeasureItem:
						entry.measure(event, textLayout, resourceManager, boldStyle);
						break;
					case SWT.PaintItem:
						entry.paint(event, textLayout, resourceManager, boldStyle, grayColor);
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

		return table;
	}

	/**
	 * 
	 */
	public void resetProviders() {
		for (QuickAccessProvider provider : providers) {
			provider.reset();
		}
	}

	public Table getTable() {
		return table;
	}

}