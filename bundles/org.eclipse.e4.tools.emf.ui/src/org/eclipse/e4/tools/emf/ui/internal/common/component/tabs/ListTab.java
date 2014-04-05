/*******************************************************************************
 * Copyright (c) 2014 TwelveTone LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Steven Spungin <steven@spungin.tv> - initial API and implementation
 *******************************************************************************/

package org.eclipse.e4.tools.emf.ui.internal.common.component.tabs;

import java.util.ArrayList;
import java.util.Collection;
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
import org.eclipse.e4.tools.emf.ui.internal.common.xml.EMFDocumentResourceMediator;
import org.eclipse.e4.tools.services.IResourcePool;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.command.DeleteCommand;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * A tab that contains a list EObjects, and provides editable columns for
 * EObject features.
 * 
 * @author Steven Spungin
 *
 */
public class ListTab implements IViewEObjects {

	// The table will only include EObjects that define this attribute
	private static final String FILTER_ATTRUBUTE_ID = "elementId"; //$NON-NLS-1$

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
	MApplication app;

	private CTabItem tabItem;

	private ModelResourceContentProvider provider;

	private IDocumentListener documentListener;

	private Collection<?> highlightedItems;

	@PreDestroy
	public void preDestroy() {
		// race condition issue with observables (exception is not thrown when
		// break points are set)
		tvResults.setContentProvider(ArrayContentProvider.getInstance());
		context.get(EMFDocumentResourceMediator.class).getDocument().removeDocumentListener(documentListener);
	}

	@PostConstruct
	public void postConstruct(final CTabFolder tabFolder) {

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

		tabItem = new CTabItem(tabFolder, SWT.NONE);

		Composite composite = new Composite(tabFolder, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(2, false));
		tabItem.setControl(composite);
		tabItem.setText(Messages.ListTab_0);

		tabItem.setImage(resourcePool.getImageUnchecked(ResourceProvider.IMG_Obj16_world_edit));
		{
			Label label = new Label(composite, SWT.NONE);
			GridData layoutData = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
			layoutData.horizontalSpan = 2;
			label.setLayoutData(layoutData);
			label.setText(Messages.ListTab_7 + FILTER_ATTRUBUTE_ID);
		}

		tvResults = new TableViewer(composite);
		tvResults.getTable().setHeaderVisible(true);
		tvResults.getTable().setLinesVisible(true);
		// tvResults.setContentProvider(ArrayContentProvider.getInstance());

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

		TableViewerColumn colItem = new TableViewerColumn(tvResults, SWT.NONE);
		colItem.getColumn().setText(Messages.ListTab_col_item);
		colItem.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				EObject eObject = (EObject) element;
				return super.getText(eObject.eClass().getName());
			}
		});

		app.getContext().set("org.eclipse.e4.tools.active-object-viewer", this); //$NON-NLS-1$

		TableViewerColumn colId = new TableViewerColumn(tvResults, SWT.NONE);
		colId.getColumn().setText(Messages.ListTab_col_id);
		colId.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				EObject eObject = (EObject) element;
				EAttribute eAtt = EmfUtil.getAttribute(eObject, FILTER_ATTRUBUTE_ID);
				return super.getText(eObject.eGet(eAtt));
			}

			@Override
			public Color getBackground(Object element) {
				Color ret = super.getBackground(element);
				if (highlightedItems != null && highlightedItems.contains(element)) {
					return ret = tvResults.getTable().getDisplay().getSystemColor(SWT.COLOR_YELLOW);
				}
				return ret;
			}
		});
		colId.setEditingSupport(new EAttributeEditingSupport(tvResults, FILTER_ATTRUBUTE_ID, context));

		@SuppressWarnings("unused")
		EAttributeTableViewerColumn colLabel = new EAttributeTableViewerColumn(tvResults, Messages.ListTab_col_label, "label", context); //$NON-NLS-1$

		// Testing the commandName attribute column
		// Any attribute can be added this way
		// @SuppressWarnings("unused")
		//EAttributeTableViewerColumn colName = new EAttributeTableViewerColumn(tvResults, Messages.ListTab_commandname, "commandName", context); //$NON-NLS-1$

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
}
