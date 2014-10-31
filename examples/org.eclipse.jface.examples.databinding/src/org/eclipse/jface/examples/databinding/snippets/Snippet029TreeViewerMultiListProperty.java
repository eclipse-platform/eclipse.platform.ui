/*******************************************************************************
 * Copyright (c) 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 175735)
 *     Matthew Hall - bugs 262407, 260337
 ******************************************************************************/

package org.eclipse.jface.examples.databinding.snippets;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.property.list.IListProperty;
import org.eclipse.core.databinding.property.list.MultiListProperty;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.databinding.viewers.ObservableListTreeContentProvider;
import org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * @since 3.2
 *
 */
public class Snippet029TreeViewerMultiListProperty {
	protected Shell shell;
	private TreeViewer viewer;

	/**
	 * Launch the application
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			Snippet029TreeViewerMultiListProperty window = new Snippet029TreeViewerMultiListProperty();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window
	 */
	public void open() {
		final Display display = Display.getDefault();
		Realm.runWithDefault(DisplayRealm.getRealm(display), new Runnable() {
			@Override
			public void run() {
				createContents();
				shell.open();
				shell.layout();
				while (!shell.isDisposed()) {
					if (!display.readAndDispatch())
						display.sleep();
				}
			}
		});
	}

	protected void createContents() {
		shell = new Shell();
		shell.setSize(509, 375);
		shell.setText("Snippet028DuplexingObservableValue.java");
		final GridLayout gridLayout = new GridLayout();
		gridLayout.makeColumnsEqualWidth = true;
		gridLayout.numColumns = 4;
		shell.setLayout(new FillLayout());

		viewer = new TreeViewer(shell, SWT.FULL_SELECTION | SWT.MULTI
				| SWT.BORDER);

		bindUI();
	}

	// Minimal JavaBeans support
	public static abstract class AbstractModelObject {
		private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(
				this);

		public void addPropertyChangeListener(PropertyChangeListener listener) {
			propertyChangeSupport.addPropertyChangeListener(listener);
		}

		public void addPropertyChangeListener(String propertyName,
				PropertyChangeListener listener) {
			propertyChangeSupport.addPropertyChangeListener(propertyName,
					listener);
		}

		public void removePropertyChangeListener(PropertyChangeListener listener) {
			propertyChangeSupport.removePropertyChangeListener(listener);
		}

		public void removePropertyChangeListener(String propertyName,
				PropertyChangeListener listener) {
			propertyChangeSupport.removePropertyChangeListener(propertyName,
					listener);
		}

		protected void firePropertyChange(String propertyName, Object oldValue,
				Object newValue) {
			propertyChangeSupport.firePropertyChange(propertyName, oldValue,
					newValue);
		}
	}

	public static class Catalog extends AbstractModelObject {
		private String name;
		private List catalogs = new ArrayList();
		private List items = new ArrayList();

		public Catalog(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			firePropertyChange("name", this.name, this.name = name);
		}

		public List getCatalogs() {
			return catalogs;
		}

		public void setCatalogs(List catalogs) {
			firePropertyChange("catalogs", this.catalogs,
					this.catalogs = catalogs);
		}

		public List getItems() {
			return items;
		}

		public void setItems(List items) {
			firePropertyChange("items", this.items, this.items = items);
		}
	}

	public static class CatalogItem extends AbstractModelObject {
		private String name;

		public CatalogItem(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			firePropertyChange("name", this.name, this.name = name);
		}
	}

	private void bindUI() {
		List items;

		Catalog fruits = new Catalog("Fruits");
		items = new ArrayList();
		items.add(new CatalogItem("Apple"));
		items.add(new CatalogItem("Orange"));
		fruits.setCatalogs(items);

		Catalog vegetables = new Catalog("Vegetables");
		items = new ArrayList();
		items.add(new CatalogItem("Peas"));
		items.add(new CatalogItem("Carrots"));
		items.add(new CatalogItem("Potatoes"));
		vegetables.setItems(items);

		Catalog foods = new Catalog("Foods");
		items = new ArrayList();
		items.add(fruits);
		items.add(vegetables);
		foods.setCatalogs(items);

		items = new ArrayList();
		items.add(new CatalogItem("Own Hand"));
		foods.setItems(items);

		Catalog catalog = new Catalog("Main Catalog");
		items = new ArrayList();
		items.add(foods);
		catalog.setCatalogs(items);

		IListProperty childrenProperty = new MultiListProperty(
				new IListProperty[] { BeanProperties.list("catalogs"),
						BeanProperties.list("items") });

		ObservableListTreeContentProvider contentProvider = new ObservableListTreeContentProvider(
				childrenProperty.listFactory(), null);
		viewer.setContentProvider(contentProvider);

		ObservableMapLabelProvider labelProvider = new ObservableMapLabelProvider(
				BeanProperties.value("name").observeDetail(
						contentProvider.getKnownElements())) {
			Image catalogImage = createCatalogImage();
			Image catalogItemImage = createCatalogItemImage();

			@Override
			public Image getImage(Object element) {
				if (element instanceof Catalog)
					return catalogImage;
				if (element instanceof CatalogItem)
					return catalogItemImage;
				return super.getImage(element);
			}

			@Override
			public void dispose() {
				catalogImage.dispose();
				catalogItemImage.dispose();
				super.dispose();
			}

			private Image createCatalogImage() {
				Display display = Display.getCurrent();
				Image catalogImage = new Image(display, 12, 12);
				GC gc = new GC(catalogImage);
				gc.setBackground(display.getSystemColor(SWT.COLOR_GREEN));
				gc.fillArc(1, 1, 10, 10, 0, 360);
				gc.dispose();
				return catalogImage;
			}

			private Image createCatalogItemImage() {
				Display display = Display.getCurrent();
				Image catalogImage = new Image(display, 12, 12);
				GC gc = new GC(catalogImage);
				gc.setBackground(display.getSystemColor(SWT.COLOR_CYAN));
				gc.fillPolygon(new int[] { 1, 10, 5, 1, 6, 1, 10, 10, 1, 10 });
				gc.dispose();
				return catalogImage;
			}
		};
		viewer.setLabelProvider(labelProvider);

		viewer.setInput(catalog);
	}
}
