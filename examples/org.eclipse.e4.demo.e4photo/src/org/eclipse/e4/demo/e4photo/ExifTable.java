package org.eclipse.e4.demo.e4photo;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.databinding.beans.PojoObservables;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.core.services.JSONObject;
import org.eclipse.e4.core.services.Logger;
import org.eclipse.e4.demo.e4photo.annotations.In;
import org.eclipse.e4.demo.e4photo.annotations.Out;
import org.eclipse.e4.demo.e4photo.annotations.PostConstruct;
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

public class ExifTable {

	private PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
	private WritableList inputList = new WritableList();
	private IContainer input;
	private Exif selection;
	private String persistedState;

	@In
	private Composite parent;
	@In
	private Logger logger;

	public ExifTable() {
	}

	@Out
	public String getPersistedState() {
		return persistedState;
	}

	@Out
	public Exif getSelection() {
		return selection;
	}

	@In
	void setInput(IResource selection) {
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

	@In
	void setPersistedState(String persistedState) {
		changeSupport.firePropertyChange("persistedState", this.persistedState,
				this.persistedState = persistedState);
	}

	private void setSelection(Exif newSelection) {
		changeSupport.firePropertyChange("selection", this.selection,
				this.selection = newSelection);
	}

	@PostConstruct
	void init() {
		parent.setLayout(new FillLayout());

		TableViewer viewer = new TableViewer(parent, SWT.SINGLE | SWT.FULL_SELECTION
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
				setSelection((Exif) ((StructuredSelection) event.getSelection())
						.getFirstElement());
			}
		});

		viewer
				.setLabelProvider(new ObservableMapLabelProvider(PojoObservables
						.observeMaps(contentProvider.getKnownElements(), Exif.class,
								columnNames)));

		viewer.setInput(inputList);
	}

	public void addPropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		changeSupport.addPropertyChangeListener(propertyName, listener);
	}

	public void removePropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		changeSupport.removePropertyChangeListener(propertyName, listener);
	}

	void dispose() {
		System.out.println("dispose called!");
	}

}
