/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.examples.contributions.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.examples.contributions.ContributionMessages;
import org.eclipse.ui.examples.contributions.model.IPersonService;
import org.eclipse.ui.examples.contributions.model.Person;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.ViewPart;

/**
 * Our example view.
 * 
 * @since 3.3
 */
public class InfoView extends ViewPart {
	public static final String ID = "org.eclipse.ui.examples.contributions.view"; //$NON-NLS-1$

	private static final String VIEW_COUNT_ID = "org.eclipse.ui.examples.contributions.view.count"; //$NON-NLS-1$
	private static final String VIEW_CONTEXT_ID = "org.eclipse.ui.examples.contributions.view.context"; //$NON-NLS-1$
	private ListViewer viewer;
	private IHandler countHandler;
	private ArrayList viewerInput;

	private IPropertyChangeListener personListener = new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
			if (IPersonService.PROP_CHANGE.equals(event.getProperty())) {
				refresh();
			} else if (IPersonService.PROP_ADD.equals(event.getProperty())) {
				viewerInput.add(event.getNewValue());
				viewer.add(event.getNewValue());
			}
		}
	};

	private static class ContentProvider implements IStructuredContentProvider {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		public void dispose() {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
		 *      java.lang.Object, java.lang.Object)
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// nothing to do here
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
		 */
		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof List) {
				return ((List) inputElement).toArray();
			}
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		viewer = new ListViewer(parent);
		viewer.setContentProvider(new ContentProvider());
		viewer.setLabelProvider(new LabelProvider());
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				editSelection();
			}
		});
		IPersonService service = (IPersonService) getSite().getService(
				IPersonService.class);
		viewerInput = new ArrayList(service.getPeople());
		service.addPersonChangeListener(personListener);
		viewer.setInput(viewerInput);
		getSite().setSelectionProvider(viewer);

		MenuManager contextMenu = new MenuManager();
		contextMenu.setRemoveAllWhenShown(true);

		getSite().registerContextMenu(contextMenu, viewer);
		Control control = viewer.getControl();
		Menu menu = contextMenu.createContextMenu(control);
		control.setMenu(menu);

		activateContext();
		createHandlers();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	/**
	 * Activate a context that this view uses. It will be tied to this view
	 * activation events and will be removed when the view is disposed.
	 */
	private void activateContext() {
		IContextService contextService = (IContextService) getSite()
				.getService(IContextService.class);
		// this will get cleaned up automatically when the site
		// is disposed
		contextService.activateContext(VIEW_CONTEXT_ID);
	}

	/**
	 * Instantiate any handlers specific to this view and activate them.
	 */
	private void createHandlers() {
		IHandlerService handlerService = (IHandlerService) getSite()
				.getService(IHandlerService.class);
		countHandler = new AbstractHandler() {
			public Object execute(ExecutionEvent event) {
				List elements = (List) viewer.getInput();
				MessageDialog.openInformation(getSite().getShell(),
						ContributionMessages.SampleHandler_plugin_name,
						NLS.bind(ContributionMessages.InfoView_countElements,
								new Integer(elements.size())));
				return null;
			}
		};
		handlerService.activateHandler(VIEW_COUNT_ID, countHandler);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	public void dispose() {
		if (countHandler != null) {
			// we must dispose our handlers, although in this case it will
			// be a no-op
			countHandler.dispose();
			countHandler = null;
		}
		super.dispose();
	}

	/**
	 * Swap the 2 given elements from the model.
	 * 
	 * @param p1
	 * @param p2
	 */
	public void swap(Person p1, Person p2) {
		List elements = viewerInput;
		int i1 = elements.indexOf(p1);
		int i2 = elements.indexOf(p2);
		Collections.swap(elements, i1, i2);
		viewer.refresh();
	}

	/**
	 * Refresh the viewer from the model.
	 */
	public void refresh() {
		viewer.refresh();
	}

	private void editSelection() {
		IHandlerService handlerService = (IHandlerService) getSite()
				.getService(IHandlerService.class);
		try {
			handlerService.executeCommand(EditInfoHandler.ID, null);
		} catch (ExecutionException e) {
			// perhaps some logging here
		} catch (NotDefinedException e) {
		} catch (NotEnabledException e) {
		} catch (NotHandledException e) {
		}
	}
}
