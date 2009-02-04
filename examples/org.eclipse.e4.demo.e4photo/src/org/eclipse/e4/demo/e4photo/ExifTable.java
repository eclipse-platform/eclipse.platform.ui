package org.eclipse.e4.demo.e4photo;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.databinding.beans.PojoObservables;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.ui.services.ISelectionService;
import org.eclipse.e4.workbench.ui.behaviors.IHasInput;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

public class ExifTable implements IHasInput {

	private WritableList inputList;

	public ExifTable(Composite parent, final ISelectionService selectionService) {
		parent.setLayout(new FillLayout());

		TableViewer viewer = new TableViewer(parent, SWT.SINGLE
				| SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		viewer.getTable().setHeaderVisible(true);
		viewer.getTable().setLinesVisible(true);

		viewer.getTable().setData("id", "exif");
		viewer.getTable().setData("class", "properties");

		String[] columnNames = new String[] { "name", "make", "model",
				"orientation", "software", "timestamp", "gpsLatitude",
				"gpsLongitude", "exposure", "iso", "aperture", "exposureComp",
				"flash", "width", "height", "focalLength", "whiteBalance",
				"lightSource", "exposureProgram" };

		for (int i = 0; i < columnNames.length; i++) {
			TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE);
			column.getColumn().setText(columnNames[i]);
			column.getColumn().pack();
		}

		ObservableListContentProvider contentProvider = new ObservableListContentProvider();
		viewer.setContentProvider(contentProvider);

		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				selectionService.setValue(event.getSelection());
			}
		});

		viewer.setLabelProvider(new ObservableMapLabelProvider(PojoObservables
				.observeMaps(contentProvider.getKnownElements(), Exif.class,
						columnNames)));

		inputList = new WritableList();
		viewer.setInput(inputList);
	}

	public Class getInputType() {
		return IContainer.class;
	}

	public void setInput(Object input) {
		if (input == null) {
			return;
		}
		if (!(input instanceof IContainer))
			return;
		// XXX same as Thumbnails - handle selection at the context
		inputList.clear();
		try {
			IResource[] members = ((IContainer) input).members();
			for (int i = 0; i < members.length; i++) {
				IResource resource = members[i];
				if (resource.getType() == IResource.FILE) {
					InputStream contents = ((IFile) resource).getContents();
					try {
						Exif exif = new Exif(resource.getLocationURI(),
								contents);
						inputList.add(exif);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						System.out.println(e.getMessage() + " " + ((IFile) resource).getFullPath());
					} finally {
						try {
							contents.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
