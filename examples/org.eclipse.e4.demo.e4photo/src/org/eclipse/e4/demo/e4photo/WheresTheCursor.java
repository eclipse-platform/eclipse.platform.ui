/*******************************************************************************
 * Copyright (c) 2008, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.demo.e4photo;

import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.databinding.beans.PojoObservables;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.core.services.JSONObject;
import org.eclipse.e4.core.services.Logger;
import org.eclipse.e4.core.services.annotations.Optional;
import org.eclipse.e4.core.services.annotations.PostConstruct;
import org.eclipse.e4.ui.model.application.MElementContainer;
import org.eclipse.e4.ui.model.application.MUIElement;
import org.eclipse.e4.ui.model.application.MUILabel;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.services.events.IEventBroker;
import org.eclipse.e4.workbench.ui.IPresentationEngine;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;

public class WheresTheCursor {
	@Inject
	private Composite parent;
	@Inject
	private Logger logger;
	@Inject
	private IPresentationEngine renderer;
	
	Control curControl;
	MUIElement curElement;
	Widget curItem;
	MUIElement itemElement;
	private int itemIndex;
	
	MUIElement dragElement;
	
	Listener mouseListener = new Listener() {
		public void handleEvent(Event event) {
			if (event.button != 1 || event.stateMask != SWT.ALT) {
				System.out.println(event.stateMask);
				return;
			}

			if (dragHost == null) {
				dragElement = null;
				if (itemElement != null) {
					dragElement = itemElement;
					System.out.println("Got an Item!!");
				}
				else if (curElement != null) {
					dragElement = curElement;
					System.out.println("Got an element!!");
				}
				
				if (dragElement != null) {
					startDrag(dragElement);
				}
			}
			else {
				endDrag();
			}
		}
	};
	
	Listener displayListener = new Listener() {

		public void handleEvent(Event event) {
			if (dragHost != null) {
				Point p = event.display.getCursorLocation();
				p.x = p.x + 20;
				p.y = p.y + 20;
				dragHost.getShell().setLocation(p);
			}
			trackElements(event);
		}

		private void trackElements(Event event) {
			curControl = null;
			curElement = null;
			curItem = null;
			itemElement = null;
			
			curControl = event.display.getCursorControl();
			if (curControl == null) {
				return;
			}
	
			curElement = getModelElement(curControl);
			
			if (curControl instanceof CTabFolder) {
				CTabFolder ctf = (CTabFolder) curControl;
				Point p = new Point(event.x, event.y);
				p = ctf.getDisplay().map(ctf.getShell(), ctf, p);
				curItem = ctf.getItem(new Point(event.x, event.y));
				if (curItem != null) {
					itemElement = (MUIElement) curItem.getData("modelElement");
					CTabItem[] items = ctf.getItems();
					for (int i = 0; i < items.length; i++) {
						if (items[i] == curItem)
							itemIndex = i;
					}
				}
			}
			else if (curControl instanceof ToolBar) {
				ToolBar tb = (ToolBar) curControl;
				Point p = new Point(event.x, event.y);
				p = tb.getDisplay().map(tb.getShell(), tb, p);
				curItem = tb.getItem(new Point(event.x, event.y));
				if (curItem != null) {
					itemElement = (MUIElement) curItem.getData("modelElement");
					ToolItem[] items = tb.getItems();
					for (int i = 0; i < items.length; i++) {
						if (items[i] == curItem)
							itemIndex = i;
					}
				}
			}
			formatText();
		}
		
		private String getLabelStr(MUIElement element) {
			if (element == null)
				return "<null>";
			
			if (element instanceof MUILabel)
				return ((MUILabel)element).getLabel();

			EObject eObj = (EObject) element;
			return eObj.eClass().getName();
		}
		
		private void formatText() {
			String str = null;
			if (curElement != null) {
				str += "Model Element: " + getLabelStr(curElement);
			}
			if (itemElement != null) {
				str += "\r\n Item Element: " + getLabelStr(itemElement) + " Index: " + itemIndex;
			}
			info.setText(str);
		}

		private MUIElement getModelElement(Control ctrl) {
			if (ctrl == null)
				return null;
			
			MUIElement element = (MUIElement) ctrl.getData("modelElement");
			if (element != null)
				return element;

			return getModelElement(ctrl.getParent());
		}
	};
	
	@Inject
	private IEventBroker eventBroker;
	private Button listen;
	private Button capture;
	private Text info;
	
	private DragHost dragHost;

	public WheresTheCursor() {
	}

	protected void endDrag() {		
		if (curElement instanceof MElementContainer<?>) {
			dragHost.drop((MElementContainer<MUIElement>) curElement, itemIndex);
		}
		dragHost = null;
	}

	protected void startDrag(MUIElement dragElement) {
		dragHost = new DragHost(dragElement);
	}

	@PostConstruct
	void init() {
		Composite myComp = new Composite(parent, SWT.NONE);
		myComp.setLayout(new GridLayout(1, true));
		
		listen = new Button(myComp, SWT.CHECK);
		listen.setText("Display Listener");
		listen.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				setDisplayListener(listen.getSelection());
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		capture = new Button(myComp, SWT.CHECK);
		capture.setText("Capture Mouse");
		capture.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				setCapture(capture.getSelection());
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
				
		info = new Text(myComp, SWT.BORDER);
		info.setLayoutData(new GridData(GridData.FILL_BOTH));
	}

	protected void setCapture(boolean capture) {
		System.out.println("set Capture: " + capture);
		parent.getShell().setCapture(capture);
	}

	protected void setDisplayListener(boolean listen) {
		System.out.println("set Listener: " + listen);
		if (listen) {
			parent.getDisplay().addFilter(SWT.MouseMove, displayListener);
			parent.getDisplay().addFilter(SWT.MouseDown, mouseListener);
		}
		else {
			parent.getDisplay().removeFilter(SWT.MouseMove, displayListener);
			parent.getDisplay().removeFilter(SWT.MouseDown, mouseListener);
		}
	}

	void dispose() {
	}
}
