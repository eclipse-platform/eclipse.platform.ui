/*******************************************************************************
 * Copyright (c) 2014 TwelveTone LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Steven Spungin <steven@spungin.tv> - initial API and implementation, Bug 432555
 *******************************************************************************/

package org.eclipse.e4.tools.emf.ui.internal.common.component.tabs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.tools.emf.ui.common.IModelResource;
import org.eclipse.e4.tools.emf.ui.internal.Messages;
import org.eclipse.e4.tools.emf.ui.internal.ResourceProvider;
import org.eclipse.e4.tools.emf.ui.internal.common.ModelEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs.BundleImageCache;
import org.eclipse.e4.tools.emf.ui.internal.common.component.tabs.empty.E;
import org.eclipse.e4.tools.emf.ui.internal.common.component.tabs.empty.EmptyFilterOption;
import org.eclipse.e4.tools.emf.ui.internal.common.component.tabs.empty.TitleAreaFilterDialog;
import org.eclipse.e4.tools.emf.ui.internal.common.component.tabs.empty.TitleAreaFilterDialogWithEmptyOptions;
import org.eclipse.e4.tools.emf.ui.internal.common.xml.EMFDocumentResourceMediator;
import org.eclipse.e4.tools.services.IResourcePool;
import org.eclipse.e4.tools.services.Translation;
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
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

/**
 * A tab that contains a list EObjects, and provides editable columns for
 * EObject features.
 *
 * @author Steven Spungin
 *
 */
public class ListTab implements IViewEObjects {

	static final String ELIPSIS = "..."; //$NON-NLS-1$

	// The table will only include EObjects that define this attribute
	private static final String FILTER_ATTRIBUTE_ID = "elementId"; //$NON-NLS-1$

	ConcurrentHashMap<String, List<EObject>> mapId_Object = new ConcurrentHashMap<String, List<EObject>>();

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

	HashMap<String, EAttributeTableViewerColumn> defaultColumns = new HashMap<String, EAttributeTableViewerColumn>();
	HashMap<String, EAttributeTableViewerColumn> optionalColumns = new HashMap<String, EAttributeTableViewerColumn>();

	private ToolItem filterByItem;

	private ToolItem filterByAttribute;

	protected Image imgMarkedItem;

	@PreDestroy
	public void preDestroy() {
		// race condition issue with observables (exception is not thrown when
		// break points are set)
		tvResults.setContentProvider(ArrayContentProvider.getInstance());
		context.get(EMFDocumentResourceMediator.class).getDocument().removeDocumentListener(documentListener);
	}

	@PostConstruct
	public void postConstruct(final CTabFolder tabFolder) {
		imageCache = new BundleImageCache(context.get(Display.class), getClass().getClassLoader());
		tabFolder.addDisposeListener(new DisposeListener() {

			@Override
			public void widgetDisposed(DisposeEvent e) {
				imageCache.dispose();
			}
		});
		try {
			imgMarkedItem = imageCache.create("org.eclipse.e4.tools.emf.ui", "/icons/full/obj16/mark_occurrences.png"); //$NON-NLS-1$//$NON-NLS-2$
		} catch (Exception e2) {
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

		Composite composite = new Composite(tabFolder, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(2, false));
		tabItem.setControl(composite);
		tabItem.setText(Messages.ListTab_0);

		tabItem.setImage(resourcePool.getImageUnchecked(ResourceProvider.IMG_Widgets_table_obj));

		final ToolBar toolBar = new ToolBar(composite, SWT.FLAT | SWT.NO_FOCUS);
		toolBar.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));

		{
			ToolItem button = new ToolItem(toolBar, SWT.PUSH);
			button.setText(Messages.ListTab_addColumn + ELIPSIS);
			button.setImage(imageCache.create("/icons/full/obj16/add_column.gif")); //$NON-NLS-1$

			button.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					TitleAreaFilterDialogWithEmptyOptions dlg = createEObjectAttributePicker(Messages.ListTab_addColumn);
					dlg.setShowEmptyOptions(false);
					if (dlg.open() == dlg.OK) {
						// Add Column
						String attName = dlg.getFirstElement().toString();
						addColumn(attName);
					}
				}
			});
		}

		{
			ToolItem button = new ToolItem(toolBar, SWT.PUSH);
			button.setText(Messages.ListTab_resetColumns);
			button.setImage(imageCache.create("/icons/full/obj16/reset_columns.gif")); //$NON-NLS-1$

			button.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					for (EAttributeTableViewerColumn col : optionalColumns.values()) {
						col.dispose();
					}
					optionalColumns.clear();
					TableViewerUtil.refreshAndPack(tvResults);
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
						final String name = dlg.getFirstElement().toString();
						mapId_Object.clear();
						final ArrayList<EObject> filtered = new ArrayList<EObject>();
						for (EObject object : getAllEObjects()) {
							if (object.eClass().getName().equals(name)) {
								filtered.add(object);
								// filter.setText(Messages.ListTab_7 +
								// attFilter);

							}

							ViewerFilter viewerFilter = new ViewerFilter() {

								@Override
								public boolean select(Viewer viewer, Object parentElement, Object element) {
									return filtered.contains(element);
								}

							};
							tvResults.setFilters(new ViewerFilter[] { viewerFilter });
							filterByItem.setText(Messages.ListTab_filterByItem + ELIPSIS + "(" + name + ")"); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
							filterByAttribute.setText(Messages.ListTab_filterByAttribute + ELIPSIS);
						}
					}
				};
			});
		}

		filterByAttribute = new ToolItem(toolBar, SWT.NONE);
		{

			filterByAttribute.setText(Messages.ListTab_filterByAttribute + ELIPSIS);
			filterByAttribute.setImage(imageCache.create("/icons/full/obj16/filter_by_attribute.gif")); //$NON-NLS-1$

			filterByAttribute.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					TitleAreaFilterDialogWithEmptyOptions dlg = createEObjectAttributePicker(Messages.ListTab_filterByAttribute);
					if (dlg.open() == dlg.OK) {
						final String attFilter = dlg.getFirstElement().toString();
						final EmptyFilterOption emptyOption = dlg.getEmptyFilterOption();
						mapId_Object.clear();
						final ArrayList<EObject> filtered = new ArrayList<EObject>();
						for (EObject object : getAllEObjects()) {
							if (EmfUtil.getAttribute(object, attFilter) != null) {
								filtered.add(object);
								ViewerFilter viewerFilter = new ViewerFilter() {

									@Override
									public boolean select(Viewer viewer, Object parentElement, Object element) {
										// if filtering on attribute, always
										// reject if not defined for model
										// element
										if (EmfUtil.getAttribute((EObject) element, attFilter) == null) {
											return false;
										}
										switch (emptyOption) {
										case EXCLUDE:
											if (E.isEmpty(EmfUtil.getAttributeValue((EObject) element, attFilter))) {
												return false;
											} else {
												return filtered.contains(element);
											}
										case ONLY:
											if (E.notEmpty((EmfUtil.getAttributeValue((EObject) element, attFilter)))) {
												return false;
											} else {
												return true;
											}
										default:
										case INCLUDE:
											if (E.isEmpty(EmfUtil.getAttributeValue((EObject) element, attFilter))) {
												return true;
											} else {
												return filtered.contains(element);
											}
										}
									}
								};
								tvResults.setFilters(new ViewerFilter[] { viewerFilter });
								filterByAttribute.setText(Messages.ListTab_filterByAttribute + ELIPSIS + "(" + attFilter + ")"); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
								filterByItem.setText(Messages.ListTab_filterByItem + ELIPSIS);

							}
						}
						addColumn(attFilter);
					}
				};
			});
		}

		{
			final ToolItem filterRemove = new ToolItem(toolBar, SWT.NONE);
			filterRemove.setText(Messages.ListTab_removeFilter);
			filterRemove.setImage(imageCache.create("/icons/full/obj16/remove_filter.png")); //$NON-NLS-1$

			filterRemove.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					tvResults.setFilters(new ViewerFilter[0]);
					filterByItem.setText(Messages.ListTab_filterByItem + ELIPSIS);
					filterByAttribute.setText(Messages.ListTab_markAttribute + ELIPSIS);
				};
			});
		}

		new ToolItem(toolBar, SWT.SEPARATOR);

		{
			final E4ToolItemMenu tiCommands = new E4ToolItemMenu(toolBar, context);
			tiCommands.getToolItem().setImage(imageCache.create("/icons/full/obj16/command.gif")); //$NON-NLS-1$

			ArrayList<String> commandIds = new ArrayList<String>();
			commandIds.add("org.eclipse.e4.tools.emf.ui.command.mark_duplicate_attributes"); //$NON-NLS-1$
			commandIds.add("org.eclipse.e4.tools.emf.ui.command.mark_duplicate_ids"); //$NON-NLS-1$
			commandIds.add("org.eclipse.e4.tools.emf.ui.command.mark_duplicate_labels"); //$NON-NLS-1$
			commandIds.add("org.eclipse.e4.tools.emf.ui.command.repair_duplicate_ids"); //$NON-NLS-1$
			commandIds.add(E4ToolItemMenu.SEPARATOR);
			commandIds.add("org.eclipse.e4.tools.emf.ui.command.unmark"); //$NON-NLS-1$
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

		final TableViewerColumn colGo = new TableViewerColumn(tvResults, SWT.NONE);
		colGo.getColumn().setText(Messages.ListTab_col_go);

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

		final TableViewerColumn colGoXmi = new TableViewerColumn(tvResults, SWT.NONE);
		colGoXmi.getColumn().setText(Messages.ListTab_col_go);

		colGoXmi.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ""; //$NON-NLS-1$
			};

			@Override
			public Image getImage(Object element) {
				return imgXmi;
			};
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

		TableViewerColumn colMarked = new TableViewerColumn(tvResults, SWT.NONE);
		colMarked.getColumn().setWidth(16);
		colMarked.getColumn().setText(Messages.ListTab_mark);
		colMarked.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public Image getImage(Object element) {
				Image ret = null;
				if (isHighlighted(element)) {
					try {
						ret = imgMarkedItem;
					} catch (Exception e) {
					} //$NON-NLS-1$
				} else {
					ret = null;
				}
				return ret;
			}

			@Override
			public String getText(Object element) {
				return ""; //$NON-NLS-1$
			}
		});

		TableViewerColumn colItem = new TableViewerColumn(tvResults, SWT.NONE);
		colItem.getColumn().setText(Messages.ListTab_col_item);
		colItem.setLabelProvider(new ColumnLabelProvider_Markable() {
			@Override
			public String getText(Object element) {
				EObject eObject = (EObject) element;
				return super.getText(eObject.eClass().getName());
			}
		});

		app.getContext().set("org.eclipse.e4.tools.active-object-viewer", this); //$NON-NLS-1$

		TableViewerColumn colId = new TableViewerColumn(tvResults, SWT.NONE);
		colId.getColumn().setText("elementId"); //$NON-NLS-1$
		colId.setLabelProvider(new ColumnLabelProvider_Markable() {
			@Override
			public String getText(Object element) {
				EObject eObject = (EObject) element;
				EAttribute eAtt = EmfUtil.getAttribute(eObject, "elementId"); //$NON-NLS-1$
				return super.getText(eObject.eGet(eAtt));
			}
		});
		colId.setEditingSupport(new EAttributeEditingSupport(tvResults, FILTER_ATTRIBUTE_ID, context));

		EAttributeTableViewerColumn colLabel = new EAttributeTableViewerColumn_Markable(tvResults, "label", "label", context); //$NON-NLS-1$ //$NON-NLS-2$

		defaultColumns.put("label", colLabel); //$NON-NLS-1$

		// Custom selection for marked items
		tvResults.getTable().addListener(SWT.EraseItem, new Listener() {
			@Override
			public void handleEvent(Event event) {
				event.detail &= ~SWT.HOT;
				if ((event.detail & SWT.SELECTED) == 0)
					return; // / item not selected

				TableItem item = (TableItem) event.item;
				if (isHighlighted(item.getData())) {

					Table table = (Table) event.widget;
					int clientWidth = table.getClientArea().width;
					GC gc = event.gc;
					// Color oldForeground = gc.getForeground();
					// Color oldBackground = gc.getBackground();

					// gc.setBackground(item.getDisplay().getSystemColor(SWT.COLOR_YELLOW));
					gc.setForeground(item.getDisplay().getSystemColor(SWT.COLOR_RED));
					gc.fillRectangle(0, event.y, clientWidth, event.height);

					// gc.setForeground(oldForeground);
					// gc.setBackground(oldBackground);
					event.detail &= ~SWT.SELECTED;
				}
			}
		});

		tvResults.getTable().setFocus();
		reload();
	}

	public void reload() {
		tvResults.setInput(modelResource);
		TableViewerUtil.refreshAndPack(tvResults);
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
		ArrayList<EObject> list = new ArrayList<EObject>();
		TreeIterator<Object> itTree = EcoreUtil.getAllContents(modelResource.getRoot());
		while (itTree.hasNext()) {
			Object object = itTree.next();
			EObject eObject = (EObject) object;
			EAttribute att = EmfUtil.getAttribute(eObject, "elementId"); //$NON-NLS-1$
			if (att != null) {
				list.add(eObject);
			}
		}
		return list;
	}

	@Override
	public Collection<EObject> getSelectedEObjects() {
		ArrayList<EObject> selected = new ArrayList<EObject>();
		for (Object item : ((IStructuredSelection) tvResults.getSelection()).toList()) {
			if (item instanceof EObject) {
				selected.add((EObject) item);

			}
		}
		return selected;
	}

	@Override
	public void deleteEObjects(Collection<EObject> list) {
		if (list.isEmpty() == false) {
			Command cmd = DeleteCommand.create(modelResource.getEditingDomain(), list);
			if (cmd.canExecute()) {
				modelResource.getEditingDomain().getCommandStack().execute(cmd);
			}
			reload();
		}
	}

	private TitleAreaFilterDialogWithEmptyOptions createEObjectAttributePicker(final String title) {
		// Get Attribute Names
		final HashSet<String> set = new HashSet<String>();
		Collection<EObject> allEObjects = getAllEObjects();
		for (EObject obj : allEObjects) {
			for (EAttribute attribute : obj.eClass().getEAllAttributes()) {
				set.add(attribute.getName());
			}
		}
		final ArrayList<String> sorted = new ArrayList<String>(set);
		Collections.sort(sorted);

		// Select Attribute
		ILabelProvider renderer = new LabelProvider() {
			@Override
			public String getText(Object element) {
				return String.valueOf(element);
			}
		};
		TitleAreaFilterDialogWithEmptyOptions dlg = new TitleAreaFilterDialogWithEmptyOptions(context.get(Shell.class), renderer) {
			@Override
			protected Control createContents(Composite parent) {
				Control ret = super.createContents(parent);
				setMessage(Messages.ListTab_selectAnAttribute);
				try {
					setTitleImage(imageCache.create("org.eclipse.e4.tools.emf.ui", "/icons/full/wizban/attribute_wiz.gif")); //$NON-NLS-1$ //$NON-NLS-2$
				} catch (Exception e) {
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
		final HashSet<String> set = new HashSet<String>();
		Collection<EObject> allEObjects = getAllEObjects();
		for (EObject obj : allEObjects) {
			set.add(obj.eClass().getName());
		}

		final ArrayList<String> sorted = new ArrayList<String>(set);
		Collections.sort(sorted);

		ILabelProvider renderer = new LabelProvider() {
			@Override
			public String getText(Object element) {
				return String.valueOf(element);
			}
		};
		TitleAreaFilterDialog dlg = new TitleAreaFilterDialog(context.get(Shell.class), renderer) {

			@Override
			protected Control createContents(Composite parent) {
				Control ret = super.createContents(parent);
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
	 */
	private void addColumn(String attName) {
		if (optionalColumns.containsKey(attName) == false && defaultColumns.containsKey(attName) == false) {
			EAttributeTableViewerColumn colName = new EAttributeTableViewerColumn_Markable(tvResults, attName, attName, context);
			TableViewerUtil.refreshAndPack(tvResults);
			optionalColumns.put(attName, colName);
		}
	}

	private class EAttributeTableViewerColumn_Markable extends EAttributeTableViewerColumn {
		public EAttributeTableViewerColumn_Markable(TableViewer tvResults, String label, String attName, IEclipseContext context) {
			super(tvResults, label, attName, context);
		}

		@Override
		public Color getBackground(Object element) {
			Color ret;
			if (isHighlighted(element)) {
				return ret = tvResults.getTable().getDisplay().getSystemColor(SWT.COLOR_YELLOW);
			} else {
				ret = super.getBackground(element);
			}
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
}
