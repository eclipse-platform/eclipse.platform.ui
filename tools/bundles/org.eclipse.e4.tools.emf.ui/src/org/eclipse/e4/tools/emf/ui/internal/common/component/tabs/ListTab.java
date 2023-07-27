/*******************************************************************************
 * Copyright (c) 2014, 2022 TwelveTone LLC and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Steven Spungin <steven@spungin.tv> - initial API and implementation, Bug 432555, Bug 436889, Bug 437372, Bug 440469,
 * Ongoing Maintenance
 *******************************************************************************/

package org.eclipse.e4.tools.emf.ui.internal.common.component.tabs;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.tools.emf.ui.common.IModelResource;
import org.eclipse.e4.tools.emf.ui.common.ModelEditorPreferences;
import org.eclipse.e4.tools.emf.ui.common.Plugin;
import org.eclipse.e4.tools.emf.ui.internal.Messages;
import org.eclipse.e4.tools.emf.ui.internal.ResourceProvider;
import org.eclipse.e4.tools.emf.ui.internal.common.ModelEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs.BundleImageCache;
import org.eclipse.e4.tools.emf.ui.internal.common.component.tabs.EAttributeEditingSupport.ATT_TYPE;
import org.eclipse.e4.tools.emf.ui.internal.common.component.tabs.empty.E;
import org.eclipse.e4.tools.emf.ui.internal.common.component.tabs.empty.EmptyFilterOption;
import org.eclipse.e4.tools.emf.ui.internal.common.component.tabs.empty.TitleAreaFilterDialog;
import org.eclipse.e4.tools.emf.ui.internal.common.component.tabs.empty.TitleAreaFilterDialogWithEmptyOptions;
import org.eclipse.e4.tools.emf.ui.internal.common.xml.EMFDocumentResourceMediator;
import org.eclipse.e4.tools.services.IResourcePool;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.command.DeleteCommand;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.osgi.service.prefs.BackingStoreException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * A tab that contains a list EObjects, and provides editable columns for
 * EObject features.
 *
 * @author Steven Spungin
 *
 */
public class ListTab implements IViewEObjects {

	static final String ELIPSIS = "..."; //$NON-NLS-1$

	ConcurrentHashMap<String, List<EObject>> mapId_Object = new ConcurrentHashMap<>();

	@Inject
	private IEclipseContext context;

	@Inject
	private IModelResource modelResource;

	private TableViewer tvResults;

	@Inject
	private IGotoObject gotoObjectHandler;

	@Inject
	private IResourcePool resourcePool;

	@Inject
	@Translation
	protected Messages Messages;

	@Inject
	MApplication app;

	BundleImageCache imageCache;

	private CTabItem tabItem;

	private ModelResourceContentProvider provider;

	private IDocumentListener documentListener;

	private Collection<?> highlightedItems;

	protected Image imgMarkedItem;

	LinkedHashMap<String, EAttributeTableViewerColumn> defaultColumns = new LinkedHashMap<>();
	LinkedHashMap<String, EAttributeTableViewerColumn> optionalColumns = new LinkedHashMap<>();
	LinkedHashMap<String, TableColumn> requiredColumns = new LinkedHashMap<>();
	private TableViewerColumn colItem;
	private TableViewerColumn colGo;
	private TableViewerColumn colGoXmi;
	private TableViewerColumn colMarked;

	private ToolItem filterByItem;
	private ToolItem filterByAttribute;
	private String filterByAttrName;
	private String filterByItemName;
	private EmptyFilterOption filterByAttrEmptyOption;

	@PreDestroy
	public void preDestroy() {
		// race condition issue with observables (exception is not thrown when
		// break points are set)
		tvResults.setContentProvider(ArrayContentProvider.getInstance());
		context.get(EMFDocumentResourceMediator.class).getDocument().removeDocumentListener(documentListener);
	}

	// save custom column and filter settings
	public void saveSettings() {
		final IEclipsePreferences pref = InstanceScope.INSTANCE.getNode(Plugin.ID);
		try {
			final Document doc = DocUtil.createDocument("list-tab"); //$NON-NLS-1$
			final Element cols = DocUtil.createChild(doc.getDocumentElement(), "columns"); //$NON-NLS-1$

			final ArrayList<TableColumn> allCols = TableViewerUtil.getColumnsInDisplayOrder(tvResults);
			for (final TableColumn col : allCols) {
				String id;
				if (requiredColumns.containsValue(col)) {
					id = getKey(requiredColumns, col);
				} else {
					id = col.getText();
				}
				saveColumn(cols, id, col);
			}

			final Element filters = DocUtil.createChild(doc.getDocumentElement(), "filters"); //$NON-NLS-1$
			if (E.notEmpty(filterByAttrName)) {
				final Element filter = DocUtil.createChild(filters, "filter"); //$NON-NLS-1$
				DocUtil.createChild(filter, "type").setTextContent("attribute"); //$NON-NLS-1$//$NON-NLS-2$
				DocUtil.createChild(filter, "condition").setTextContent(filterByAttrName); //$NON-NLS-1$
				DocUtil.createChild(filter, "emptyOption").setTextContent(filterByAttrEmptyOption.name()); //$NON-NLS-1$
			}
			if (E.notEmpty(filterByItemName)) {
				final Element filter = DocUtil.createChild(filters, "filter"); //$NON-NLS-1$
				DocUtil.createChild(filter, "type").setTextContent("item"); //$NON-NLS-1$ //$NON-NLS-2$
				DocUtil.createChild(filter, "condition").setTextContent(filterByItemName); //$NON-NLS-1$
			}

			pref.put("list-tab-xml", docToString(doc)); //$NON-NLS-1$
		} catch (final ParserConfigurationException e1) {
			e1.printStackTrace();
		} catch (final TransformerException e) {
			e.printStackTrace();
		}

		try {
			pref.flush();
		} catch (final BackingStoreException e) {
			e.printStackTrace();
		}
	}

	private String getKey(Map<String, ?> map, Object value) {
		for (final Entry<String, ?> entry : map.entrySet()) {
			if (entry.getValue().equals(value)) {
				return entry.getKey();
			}
		}
		return null;
	}

	private void saveColumn(Element eleCols, String columnName, TableColumn objCol) {
		final Element col = DocUtil.createChild(eleCols, "column"); //$NON-NLS-1$

		DocUtil.createChild(col, "attribute").setTextContent(columnName); //$NON-NLS-1$

		final Integer width = objCol.getWidth();
		DocUtil.createChild(col, "width").setTextContent(width.toString()); //$NON-NLS-1$
	}

	// load custom column and filter settings
	private void loadSettings() {
		final IEclipsePreferences pref = InstanceScope.INSTANCE.getNode(Plugin.ID);

		final boolean restoreColumns = pref.getBoolean(ModelEditorPreferences.LIST_TAB_REMEMBER_COLUMNS, false);
		final boolean restoreFilters = pref.getBoolean(ModelEditorPreferences.LIST_TAB_REMEMBER_FILTERS, false);
		if (!restoreColumns && !restoreFilters) {
			return;
		}

		final String xml = pref.get("list-tab-xml", ""); //$NON-NLS-1$ //$NON-NLS-2$
		if (E.notEmpty(xml)) {
			try {
				@SuppressWarnings("restriction")
				final Document doc = org.eclipse.core.internal.runtime.XmlProcessorFactory
						.parseWithErrorOnDOCTYPE(new InputSource(new StringReader(xml)));
				final XPath xpath = XPathFactory.newInstance().newXPath();
				NodeList list;
				if (restoreColumns) {
					// restore columns and column widths
					list = (NodeList) xpath.evaluate("//columns/column", doc, XPathConstants.NODESET); //$NON-NLS-1$
					for (int i = 0; i < list.getLength(); i++) {
						final Element ele = (Element) list.item(i);
						TableColumn col;
						final String colName = xpath.evaluate("attribute/text()", ele); //$NON-NLS-1$
						if (colName.isEmpty()) {
							continue;
						}
						col = requiredColumns.get(colName);
						if (col == null) {
							col = addColumn(colName).getTableViewerColumn().getColumn();
						}

						// move it to the end of the list.
						final int currentIndex = TableViewerUtil.getVisibleColumnIndex(tvResults, col);
						final int[] order = tvResults.getTable().getColumnOrder();
						for (int idx = 0; idx < order.length; idx++) {
							if (order[idx] > currentIndex) {
								order[idx]--;
							} else if (order[idx] == currentIndex) {
								order[idx] = order.length - 1;
							}
						}
						tvResults.getTable().setColumnOrder(order);

						//					if ("Item".equals(colName)) { //$NON-NLS-1$
						// col = colItem;
						//					} else if ("Item".equals(colName)) { //$NON-NLS-1$
						// col = colItem;
						// }

						final String sWidth = xpath.evaluate("width/text()", ele); //$NON-NLS-1$
						try {
							col.setWidth(Integer.parseInt(sWidth));
						} catch (final Exception e) {
						}
					}
				}

				if (restoreFilters) {
					// restore filters
					list = (NodeList) xpath.evaluate("//filters/filter", doc, XPathConstants.NODESET); //$NON-NLS-1$
					for (int i = 0; i < list.getLength(); i++) {
						final Element ele = (Element) list.item(i);
						final String type = xpath.evaluate("type/text()", ele); //$NON-NLS-1$
						final String condition = xpath.evaluate("condition/text()", ele); //$NON-NLS-1$
						final String emptyOption = xpath.evaluate("emptyOption/text()", ele); //$NON-NLS-1$
						if ("item".equals(type)) { //$NON-NLS-1$
							filterByItem(condition);
						} else if ("attribute".equals(type)) { //$NON-NLS-1$
							EmptyFilterOption emptyFilterOption;
							try {
								emptyFilterOption = EmptyFilterOption.valueOf(emptyOption);
							} catch (final Exception e) {
								emptyFilterOption = EmptyFilterOption.INCLUDE;
							}
							filterByAttribute(condition, emptyFilterOption);
						}
					}
				}
			} catch (final Exception e) {
			}
		}
	}

	// @Refactor
	static private String docToString(Document doc) throws TransformerException {
		@SuppressWarnings("restriction")
		final TransformerFactory tf = org.eclipse.core.internal.runtime.XmlProcessorFactory
				.createTransformerFactoryWithErrorOnDOCTYPE();
		final Transformer transformer = tf.newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes"); //$NON-NLS-1$
		final StringWriter writer = new StringWriter();
		transformer.transform(new DOMSource(doc), new StreamResult(writer));
		final String output = writer.getBuffer().toString().replaceAll("\n|\r", ""); //$NON-NLS-1$ //$NON-NLS-2$
		return output;
	}

	// @Refactor
	static String join(Collection<String> items, String separator) {
		final StringBuilder sb = new StringBuilder();
		for (final String item : items) {
			sb.append(item);
			sb.append(separator);
		}
		if (sb.length() > 0) {
			sb.setLength(sb.length() - separator.length());
		}
		return sb.toString();
	}

	@PostConstruct
	public void postConstruct(final CTabFolder tabFolder) {
		imageCache = new BundleImageCache(context.get(Display.class), getClass().getClassLoader());
		tabFolder.addDisposeListener(e -> imageCache.dispose());
		try {
			imgMarkedItem = imageCache.create(Plugin.ID, "/icons/full/obj16/mark_occurrences.png"); //$NON-NLS-1$
		} catch (final Exception e2) {
			e2.printStackTrace();
		}

		documentListener = new IDocumentListener() {

			@Override
			public void documentChanged(DocumentEvent event) {
				reload();
			}

			@Override
			public void documentAboutToBeChanged(DocumentEvent event) {
			}
		};
		context.get(EMFDocumentResourceMediator.class).getDocument().addDocumentListener(documentListener);

		tabItem = new CTabItem(tabFolder, SWT.NONE, 1);

		final Composite composite = new Composite(tabFolder, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(2, false));
		tabItem.setControl(composite);
		tabItem.setText(Messages.ListTab_0);

		tabItem.setImage(resourcePool.getImageUnchecked(ResourceProvider.IMG_Widgets_table_obj));

		final ToolBar toolBar = new ToolBar(composite, SWT.FLAT);
		toolBar.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));

		{
			final ToolItem button = new ToolItem(toolBar, SWT.PUSH);
			button.setText(Messages.ListTab_addColumn + ELIPSIS);
			button.setImage(imageCache.create("/icons/full/obj16/add_column.gif")); //$NON-NLS-1$

			button.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					final TitleAreaFilterDialogWithEmptyOptions dlg = createEObjectAttributePicker(Messages.ListTab_addColumn);
					dlg.setShowEmptyOptions(false);
					if (dlg.open() == Window.OK) {
						// Add Column
						final String attName = dlg.getFirstElement().toString();
						final EAttributeTableViewerColumn col = addColumn(attName);
						col.getTableViewerColumn().getColumn().pack();
						tvResults.refresh();
					}
				}
			});
		}

		{
			final ToolItem button = new ToolItem(toolBar, SWT.PUSH);
			button.setText(Messages.ListTab_resetColumns);
			button.setImage(imageCache.create("/icons/full/obj16/reset_columns.gif")); //$NON-NLS-1$

			button.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					for (final EAttributeTableViewerColumn col : optionalColumns.values()) {
						col.dispose();
					}
					optionalColumns.clear();
				}
			});
		}

		new ToolItem(toolBar, SWT.SEPARATOR);

		filterByItem = new ToolItem(toolBar, SWT.NONE);
		{

			filterByItem.setText(Messages.ListTab_filterByItem + ELIPSIS);
			filterByItem.setImage(imageCache.create("/icons/full/obj16/filter_by_item.gif")); //$NON-NLS-1$

			filterByItem.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					final TitleAreaFilterDialog dlg = createElementTypePicker(Messages.ListTab_filterByItem);
					if (dlg.open() == Window.OK) {
						filterByItem(dlg.getFirstElement().toString());
					}
				}
			});
		}

		filterByAttribute = new ToolItem(toolBar, SWT.NONE);
		{

			filterByAttribute.setText(Messages.ListTab_filterByAttribute + ELIPSIS);
			filterByAttribute.setImage(imageCache.create("/icons/full/obj16/filter_by_attribute.gif")); //$NON-NLS-1$

			filterByAttribute.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					final TitleAreaFilterDialogWithEmptyOptions dlg = createEObjectAttributePicker(Messages.ListTab_filterByAttribute);
					if (dlg.open() == Window.OK) {
						filterByAttribute(dlg.getFirstElement().toString(), dlg.getEmptyFilterOption());
					}
				}
			});
		}

		{
			final ToolItem filterRemove = new ToolItem(toolBar, SWT.NONE);
			filterRemove.setText(Messages.ListTab_removeFilter);
			filterRemove.setImage(imageCache.create("/icons/full/obj16/remove_filter.png")); //$NON-NLS-1$

			filterRemove.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					filterByItemName = null;
					filterByAttrName = null;
					tvResults.setFilters();
					filterByItem.setText(Messages.ListTab_filterByItem + ELIPSIS);
					filterByAttribute.setText(Messages.ListTab_markAttribute + ELIPSIS);
				}
			});
		}

		new ToolItem(toolBar, SWT.SEPARATOR);

		{
			final E4ToolItemMenu tiCommands = new E4ToolItemMenu(toolBar, context);
			tiCommands.getToolItem().setImage(imageCache.create("/icons/full/obj16/command.gif")); //$NON-NLS-1$
			tiCommands.getToolItem().setText(Messages.ListTab_more + ELIPSIS);
			final ArrayList<String> commandIds = new ArrayList<>();
			commandIds.add("org.eclipse.e4.tools.emf.ui.command.mark_duplicate_attributes"); //$NON-NLS-1$
			commandIds.add("org.eclipse.e4.tools.emf.ui.command.mark_duplicate_ids"); //$NON-NLS-1$
			commandIds.add("org.eclipse.e4.tools.emf.ui.command.mark_duplicate_labels"); //$NON-NLS-1$
			commandIds.add("org.eclipse.e4.tools.emf.ui.command.repair_duplicate_ids"); //$NON-NLS-1$
			commandIds.add(E4ToolItemMenu.SEPARATOR);
			commandIds.add("org.eclipse.e4.tools.emf.ui.command.unmark"); //$NON-NLS-1$
			commandIds.add(E4ToolItemMenu.SEPARATOR);
			commandIds.add("org.eclipse.e4.tools.emf.ui.command.autosizeColumns"); //$NON-NLS-1$
			commandIds.add("org.eclipse.e4.tools.emf.ui.command.resetToDefault"); //$NON-NLS-1$
			tiCommands.addCommands(commandIds);
		}

		tvResults = new TableViewer(composite, SWT.FULL_SELECTION);
		tvResults.getTable().setHeaderVisible(true);
		tvResults.getTable().setLinesVisible(true);

		provider = new ModelResourceContentProvider();
		tvResults.setContentProvider(provider);

		tvResults.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		((GridData) tvResults.getTable().getLayoutData()).horizontalSpan = 2;

		final Image imgForm = resourcePool.getImageUnchecked(ResourceProvider.IMG_Obj16_application_form);
		final Image imgXmi = resourcePool.getImageUnchecked(ResourceProvider.IMG_Obj16_chart_organisation);

		colGo = new TableViewerColumn(tvResults, SWT.NONE);
		colGo.getColumn().setText(Messages.ListTab_col_go);
		requiredColumns.put("GoTree", colGo.getColumn()); //$NON-NLS-1$
		colGo.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public Image getImage(Object element) {
				return imgForm;
			}

			@Override
			public String getText(Object element) {
				return ""; //$NON-NLS-1$
			}
		});

		colGoXmi = new TableViewerColumn(tvResults, SWT.NONE);
		colGoXmi.getColumn().setText(Messages.ListTab_col_go2);
		requiredColumns.put("GoXmi", colGoXmi.getColumn()); //$NON-NLS-1$
		colGoXmi.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ""; //$NON-NLS-1$
			}

			@Override
			public Image getImage(Object element) {
				return imgXmi;
			}
		});

		tvResults.getTable().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				if (TableViewerUtil.isColumnClicked(tvResults, e, colGo)) {
					gotoObjectHandler.gotoEObject(ModelEditor.TAB_FORM, (EObject) TableViewerUtil.getData(tvResults, e));
				} else if (TableViewerUtil.isColumnClicked(tvResults, e, colGoXmi)) {
					gotoObjectHandler.gotoEObject(ModelEditor.TAB_XMI, (EObject) TableViewerUtil.getData(tvResults, e));
				}
			}
		});

		colMarked = new TableViewerColumn(tvResults, SWT.NONE);
		colMarked.getColumn().setWidth(16);
		colMarked.getColumn().setText(Messages.ListTab_mark);
		requiredColumns.put("Marked", colMarked.getColumn()); //$NON-NLS-1$
		colMarked.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public Image getImage(Object element) {
				Image ret = null;
				if (isHighlighted(element)) {
					try {
						ret = imgMarkedItem;
					} catch (final Exception e) {
					}
				}
				return ret;
			}

			@Override
			public String getText(Object element) {
				return ""; //$NON-NLS-1$
			}
		});

		colItem = new TableViewerColumn(tvResults, SWT.NONE);
		colItem.getColumn().setText(Messages.ListTab_col_item);
		requiredColumns.put("Item", colItem.getColumn()); //$NON-NLS-1$
		colItem.setLabelProvider(new ColumnLabelProvider_Markable() {
			@Override
			public String getText(Object element) {
				final EObject eObject = (EObject) element;
				return super.getText(eObject.eClass().getName());
			}
		});

		app.getContext().set("org.eclipse.e4.tools.active-object-viewer", this); //$NON-NLS-1$

		final EAttributeTableViewerColumn colId = new EAttributeTableViewerColumn(tvResults,
				Messages.ListTab_elementId, "elementId", context); //$NON-NLS-1$
		defaultColumns.put("elementId", colId); //$NON-NLS-1$

		final EAttributeTableViewerColumn colLabel = new EAttributeTableViewerColumn_Markable(tvResults,
				"label", "label", context); //$NON-NLS-1$//$NON-NLS-2$
		defaultColumns.put("label", colLabel); //$NON-NLS-1$

		// Custom selection for marked items
		tvResults.getTable().addListener(SWT.EraseItem, event -> {
			event.detail &= ~SWT.HOT;
			if ((event.detail & SWT.SELECTED) == 0) {
				return; // / item not selected
			}

			final TableItem item = (TableItem) event.item;
			if (isHighlighted(item.getData())) {

				final Table table = (Table) event.widget;
				final int clientWidth = table.getClientArea().width;
				final GC gc = event.gc;
				// Color oldForeground = gc.getForeground();
				// Color oldBackground = gc.getBackground();

				// gc.setBackground(item.getDisplay().getSystemColor(SWT.COLOR_YELLOW));
				gc.setForeground(item.getDisplay().getSystemColor(SWT.COLOR_RED));
				gc.fillRectangle(0, event.y, clientWidth, event.height);

				// gc.setForeground(oldForeground);
				// gc.setBackground(oldBackground);
				event.detail &= ~SWT.SELECTED;
			}
		});

		tvResults.getTable().setFocus();

		for (final EAttributeTableViewerColumn col : defaultColumns.values()) {
			col.getTableViewerColumn().getColumn().setMoveable(true);
		}
		for (final TableColumn col : requiredColumns.values()) {
			col.setMoveable(true);
		}

		makeSortable(colId.getTableViewerColumn().getColumn(), new AttributeColumnLabelSorter(colId
				.getTableViewerColumn().getColumn(), "elementId")); //$NON-NLS-1$
		makeSortable(colLabel.getTableViewerColumn().getColumn(), new AttributeColumnLabelSorter(colLabel
				.getTableViewerColumn().getColumn(), "label")); //$NON-NLS-1$
		makeSortable(colItem.getColumn(), new TableViewerUtil.ColumnLabelSorter(colItem.getColumn()));
		makeSortable(colMarked.getColumn(), new TableViewerUtil.AbstractInvertableTableSorter() {

			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				final boolean mark1 = isHighlighted(e1);
				final boolean mark2 = isHighlighted(e2);
				if (mark1 && !mark2) {
					return -1;
				} else if (mark2 && !mark1) {
					return 1;
				} else {
					return 0;
				}
			}
		});

		reload();
		TableViewerUtil.refreshAndPack(tvResults);
		loadSettings();
	}

	private void makeSortable(TableColumn column, TableViewerUtil.AbstractInvertableTableSorter sorter) {
		new TableViewerUtil.TableSortSelectionListener(tvResults, column, sorter, SWT.UP, false);
	}

	public void reload() {
		tvResults.setInput(modelResource);
	}

	public TableViewer getViewer() {
		return tvResults;
	}

	public CTabItem getTabItem() {
		return tabItem;
	}

	public IEclipseContext getContext() {
		return context;
	}

	@Override
	public void highlightEObjects(Collection<EObject> items) {
		highlightedItems = items;
		tvResults.refresh();
	}

	@Override
	public List<EObject> getAllEObjects() {
		final ArrayList<EObject> list = new ArrayList<>();
		final TreeIterator<Object> itTree = EcoreUtil.getAllContents(modelResource.getRoot());
		while (itTree.hasNext()) {
			final Object object = itTree.next();
			final EObject eObject = (EObject) object;
			final EAttribute att = EmfUtil.getAttribute(eObject, "elementId"); //$NON-NLS-1$
			if (att != null) {
				list.add(eObject);
			}
		}
		return list;
	}

	@Override
	public Collection<EObject> getSelectedEObjects() {
		final ArrayList<EObject> selected = new ArrayList<>();
		for (final Object item : ((IStructuredSelection) tvResults.getSelection()).toList()) {
			if (item instanceof EObject) {
				selected.add((EObject) item);

			}
		}
		return selected;
	}

	@Override
	public void deleteEObjects(Collection<EObject> list) {
		if (list.isEmpty() == false) {
			final Command cmd = DeleteCommand.create(modelResource.getEditingDomain(), list);
			if (cmd.canExecute()) {
				modelResource.getEditingDomain().getCommandStack().execute(cmd);
			}
			reload();
		}
	}

	private TitleAreaFilterDialogWithEmptyOptions createEObjectAttributePicker(final String title) {
		// Get Attribute Names
		final HashSet<String> set = new HashSet<>();
		final Collection<EObject> allEObjects = getAllEObjects();
		for (final EObject obj : allEObjects) {
			for (final EAttribute attribute : obj.eClass().getEAllAttributes()) {
				set.add(attribute.getName());
			}
		}
		final ArrayList<String> sorted = new ArrayList<>(set);
		sorted.sort(null);

		// Select Attribute
		final ILabelProvider renderer = new LabelProvider() {
			@Override
			public String getText(Object element) {
				return String.valueOf(element);
			}
		};
		final TitleAreaFilterDialogWithEmptyOptions dlg = new TitleAreaFilterDialogWithEmptyOptions(
				context.get(Shell.class), renderer) {
			@Override
			protected Control createContents(Composite parent) {
				final Control ret = super.createContents(parent);
				setMessage(Messages.ListTab_selectAnAttribute);
				try {
					setTitleImage(imageCache.create(Plugin.ID, "/icons/full/wizban/attribute_wiz.gif")); //$NON-NLS-1$
				} catch (final Exception e) {
					e.printStackTrace();
				}
				setTitle(title);
				setElements(sorted.toArray(new String[0]));
				return ret;
			}
		};
		return dlg;
	}

	private TitleAreaFilterDialog createElementTypePicker(final String title) {
		// Get Attribute Names
		final HashSet<String> set = new HashSet<>();
		final Collection<EObject> allEObjects = getAllEObjects();
		for (final EObject obj : allEObjects) {
			set.add(obj.eClass().getName());
		}

		final ArrayList<String> sorted = new ArrayList<>(set);
		sorted.sort(null);

		final ILabelProvider renderer = new LabelProvider() {
			@Override
			public String getText(Object element) {
				return String.valueOf(element);
			}
		};
		final TitleAreaFilterDialog dlg = new TitleAreaFilterDialog(context.get(Shell.class), renderer) {

			@Override
			protected Control createContents(Composite parent) {
				final Control ret = super.createContents(parent);
				setMessage(Messages.ListTab_selectAType);
				setTitle(title);
				setElements(sorted.toArray(new String[0]));
				return ret;
			}
		};
		return dlg;
	}

	/**
	 * Adds a column if it does not already exist
	 *
	 * @param attName
	 * @return The existing or newly created column
	 */
	private EAttributeTableViewerColumn addColumn(String attName) {
		EAttributeTableViewerColumn colName = defaultColumns.get(attName);
		if (colName == null) {
			colName = optionalColumns.get(attName);
			if (colName == null) {
				colName = new EAttributeTableViewerColumn_Markable(tvResults, attName, attName, context);
				optionalColumns.put(attName, colName);
				colName.getTableViewerColumn().getColumn().setMoveable(true);
				makeSortable(colName.getTableViewerColumn().getColumn(), new AttributeColumnLabelSorter(colName
						.getTableViewerColumn().getColumn(), attName));
				tvResults.refresh();
			}
		}
		return colName;
	}

	static private class AttributeColumnLabelSorter extends TableViewerUtil.ColumnLabelSorter {

		private final String attName;

		AttributeColumnLabelSorter(TableColumn col, String attName) {
			super(col);
			this.attName = attName;
		}

		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			// if either is boolean, use boolean value, otherwise use text value
			final ATT_TYPE e1Type = EAttributeEditingSupport.getAttributeType(e1, attName);
			final ATT_TYPE e2Type = EAttributeEditingSupport.getAttributeType(e2, attName);
			if (e1Type == ATT_TYPE.BOOLEAN || e2Type == ATT_TYPE.BOOLEAN) {
				final Boolean b1 = (Boolean) EmfUtil.getAttributeValue((EObject) e1, attName);
				final Boolean b2 = (Boolean) EmfUtil.getAttributeValue((EObject) e2, attName);
				if (b1 == null && b2 != null) {
					return -2;
				} else if (b2 == null && b1 != null) {
					return 2;
				} else {
					return b1.compareTo(b2);
				}
			}
			return super.compare(viewer, e1, e2);
		}
	}

	private class EAttributeTableViewerColumn_Markable extends EAttributeTableViewerColumn {
		public EAttributeTableViewerColumn_Markable(TableViewer tvResults, String label, String attName,
				IEclipseContext context) {
			super(tvResults, label, attName, context);
		}

		@Override
		public Color getBackground(Object element) {
			Color ret;
			if (isHighlighted(element)) {
				return ret = tvResults.getTable().getDisplay().getSystemColor(SWT.COLOR_YELLOW);
			}
			ret = super.getBackground(element);
			return ret;
		}
	}

	private class ColumnLabelProvider_Markable extends ColumnLabelProvider {
		@Override
		public Color getBackground(Object element) {
			Color ret;
			if (isHighlighted(element)) {
				ret = tvResults.getTable().getDisplay().getSystemColor(SWT.COLOR_YELLOW);
			} else {
				ret = super.getBackground(element);
			}
			return ret;
		}
	}

	@Override
	public EditingDomain getEditingDomain() {
		return modelResource.getEditingDomain();
	}

	public boolean isHighlighted(Object element) {
		return highlightedItems != null && highlightedItems.contains(element);
	}

	private void filterByItem(String name) {
		filterByItemName = name;
		filterByAttrName = null;
		filterByAttrEmptyOption = null;
		mapId_Object.clear();
		final ArrayList<EObject> filtered = new ArrayList<>();
		for (final EObject object : getAllEObjects()) {
			if (object.eClass().getName().equals(filterByItemName)) {
				filtered.add(object);
				// filter.setText(Messages.ListTab_7 +
				// attFilter);

			}

			final ViewerFilter viewerFilter = new ViewerFilter() {

				@Override
				public boolean select(Viewer viewer, Object parentElement, Object element) {
					return filtered.contains(element);
				}

			};
			tvResults.setFilters(viewerFilter);
			filterByItem.setText(Messages.ListTab_filterByItem + ELIPSIS + "(" + filterByItemName + ")"); //$NON-NLS-1$//$NON-NLS-2$
			filterByAttribute.setText(Messages.ListTab_filterByAttribute + ELIPSIS);
		}
	}

	private void filterByAttribute(String name, final EmptyFilterOption emptyOption) {
		filterByAttrName = name;
		filterByAttrEmptyOption = emptyOption;
		filterByItemName = null;
		mapId_Object.clear();
		final ArrayList<EObject> filtered = new ArrayList<>();
		for (final EObject object : getAllEObjects()) {
			if (EmfUtil.getAttribute(object, filterByAttrName) != null) {
				filtered.add(object);
				final ViewerFilter viewerFilter = new ViewerFilter() {

					@Override
					public boolean select(Viewer viewer, Object parentElement, Object element) {
						// if filtering on attribute, always
						// reject if not defined for model
						// element
						if (EmfUtil.getAttribute((EObject) element, filterByAttrName) == null) {
							return false;
						}
						switch (emptyOption) {
						case EXCLUDE:
							if (E.isEmpty(EmfUtil.getAttributeValue((EObject) element, filterByAttrName))) {
								return false;
							}
							return filtered.contains(element);
						case ONLY:
							if (E.notEmpty(EmfUtil.getAttributeValue((EObject) element, filterByAttrName))) {
								return false;
							}
							return true;
						default:
						case INCLUDE:
							if (E.isEmpty(EmfUtil.getAttributeValue((EObject) element, filterByAttrName))) {
								return true;
							}
							return filtered.contains(element);
						}
					}
				};
				tvResults.setFilters(viewerFilter);
				filterByAttribute.setText(Messages.ListTab_filterByAttribute + ELIPSIS + "(" + filterByAttrName + ")"); //$NON-NLS-1$//$NON-NLS-2$
				filterByItem.setText(Messages.ListTab_filterByItem + ELIPSIS);

			}
		}
		final TableViewerColumn viewerColumn = addColumn(filterByAttrName).getTableViewerColumn();
		viewerColumn.getColumn().pack();
	}

	@Override
	public void autosizeContent() {
		for (final TableColumn col : tvResults.getTable().getColumns()) {
			col.pack();
			if (col.getWidth() < 10) {
				col.setWidth(10);
			}
		}
	}

	@Override
	public void resetToDefault() {
		for (final EAttributeTableViewerColumn col : optionalColumns.values()) {
			col.dispose();
		}
		optionalColumns.clear();

		TableViewerUtil.resetColumnOrder(tvResults);
		TableViewerUtil.packAllColumns(tvResults);
	}

}
