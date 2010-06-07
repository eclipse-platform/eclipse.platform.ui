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
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.core.databinding.beans.PojoObservables;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.EventUtils;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.core.services.util.JSONObject;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;
import org.osgi.service.event.EventAdmin;

public class ExifTable {

	private PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
	private WritableList inputList = new WritableList();
	private IContainer input;
	private String persistedState;
	
	final static public String EVENT_NAME = "org/eclipse/e4/demo/e4photo/exif"; 

	@Inject
	private Composite parent;
	@Inject
	private Logger logger;
	
	@Inject
	private EventAdmin eventAdmin;
	
	private TableViewer viewer;

	public ExifTable() {
		super();
	}

	@Inject @Optional
	void setSelection(@Named(IServiceConstants.ACTIVE_SELECTION) IResource selection) {
		if (selection == null)
			return;
		IContainer newInput;
		if (selection instanceof IContainer)
			newInput = (IContainer) selection;
		else
			newInput = selection.getParent();
		if (newInput == input)
			return;
		input = newInput;
	
		inputList.clear();
		try {
			IResource[] members = input.members();
			for (int i = 0; i < members.length; i++) {
				IResource resource = members[i];
				if (resource.getType() == IResource.FILE) {
					InputStream contents = ((IFile) resource).getContents();
					try {
						Exif exif = new Exif(resource.getLocationURI(), contents);
						inputList.add(exif);
					} catch (Exception e) {
						logger.warn(((IFile) resource).getFullPath() + ": "
								+ e.getMessage());
					} finally {
						try {
							contents.close();
						} catch (IOException e) {
							logger.warn(e, "Could not close stream");
						}
					}
				}
			}
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Inject @Optional
	void setPersistedState(@Named("persistedState") String persistedState) {
		changeSupport.firePropertyChange("persistedState", this.persistedState,
				this.persistedState = persistedState);
	}

	@PostConstruct
	void init() {
		parent.setLayout(new FillLayout());

		viewer = new TableViewer(parent, SWT.SINGLE | SWT.FULL_SELECTION
				| SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		viewer.getTable().setHeaderVisible(true);
		viewer.getTable().setLinesVisible(true);

		viewer.getTable().setData("org.eclipse.e4.ui.css.id", "exif");
		viewer.getTable().setData("org.eclipse.e4.ui.css.CssClassName", "properties");

		final JSONObject state = persistedState == null ? new JSONObject() : JSONObject
				.deserialize(persistedState);
		String[] columnNames = new String[] { "name", "make", "model", "orientation",
				"software", "timestamp", "gpsLatitude", "gpsLongitude", "exposure",
				"iso", "aperture", "exposureComp", "flash", "width", "height",
				"focalLength", "whiteBalance", "lightSource", "exposureProgram" };

		for (int i = 0; i < columnNames.length; i++) {
			TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE);
			final TableColumn c = column.getColumn();
			final String name = columnNames[i];
			c.setText(name);
			String width = state.getString(name);
			if (width != null) {
				c.setWidth(Integer.parseInt(width));
			} else {
				c.pack();
			}
			c.addControlListener(new ControlAdapter() {
				@Override
				public void controlResized(ControlEvent e) {
					state.set(name, c.getWidth() + "");
					setPersistedState(state.serialize());
				}
			});
		}

		ObservableListContentProvider contentProvider = new ObservableListContentProvider();
		viewer.setContentProvider(contentProvider);

		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				Object selected = ((StructuredSelection) event.getSelection()).getFirstElement();
				if (eventAdmin != null)
					EventUtils.post(eventAdmin, EVENT_NAME, selected);
			}
		});

		viewer
				.setLabelProvider(new ObservableMapLabelProvider(PojoObservables
						.observeMaps(contentProvider.getKnownElements(), Exif.class,
								columnNames)));

		viewer.setInput(inputList);
	}
	
	@Focus
	void setFocus() {
		viewer.getControl().setFocus();
	}

	void dispose() {
		//nothing to do
	}
}
