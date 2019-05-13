/*******************************************************************************
 * Copyright (c) 2017 Fabian Pfaff and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Fabian Pfaff - Bug 517461
 *******************************************************************************/
package org.eclipse.jface.snippets.viewers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DecoratingStyledCellLabelProvider;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;

public class Snippet066TableViewerWithLabelDecorator {

	private class Person {
		private final int id;
		private final String firstName;
		private final String lastName;

		public Person(int id, String firstName, String lastName) {
			this.id = id;
			this.firstName = firstName;
			this.lastName = lastName;
		}

		public int getId() {
			return id;
		}

		public String getFirstName() {
			return firstName;
		}

		public String getLastName() {
			return lastName;
		}
	}

	private static class StatusLabelDecorator implements ILabelDecorator {
		private static final String ERROR_IMG_ID_POSTFIX = "_ERROR";
		private static final int ERROR_OVERLAY_SIZE = 16;
		private static final ImageDescriptor ERROR_OVERLAY = ImageDescriptor.createFromImageDataProvider(
				_zoom ->
				Display.getCurrent().getSystemImage(SWT.ICON_ERROR).getImageData().scaledTo(ERROR_OVERLAY_SIZE,
						ERROR_OVERLAY_SIZE));
		private static final String WORD_PATTERN = "(\\w)+";
		private final ImageRegistry imageRegistry = new ImageRegistry();

		@Override
		public Image decorateImage(Image image, Object element) {
			Person person = (Person) element;
			if (isValidWord(person.getFirstName()) && isValidWord(person.getLastName())) {
				return image;
			}

			String decoratedImageId = image.hashCode() + ERROR_IMG_ID_POSTFIX;
			if (imageRegistry.get(decoratedImageId) == null) {
				createAndStoreDecoratedImage(image, decoratedImageId);
			}
			return imageRegistry.get(decoratedImageId);
		}

		private boolean isValidWord(String toCheck) {
			return toCheck.matches(WORD_PATTERN);
		}

		private void createAndStoreDecoratedImage(Image baseImage, String decoratedImageId) {
			Point size = new Point(baseImage.getBounds().width, baseImage.getBounds().height);
			DecorationOverlayIcon decoratedImage = new DecorationOverlayIcon(baseImage,
					new ImageDescriptor[] { null, null, null, ERROR_OVERLAY, null }, size);
			imageRegistry.put(decoratedImageId, decoratedImage);
		}

		@Override
		public String decorateText(String text, Object element) {
			return null;
		}

		@Override
		public void addListener(ILabelProviderListener listener) {
		}

		@Override
		public void dispose() {
			imageRegistry.dispose();
		}

		@Override
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		@Override
		public void removeListener(ILabelProviderListener listener) {
		}
	}

	private class StatusStyledLabelProvider implements IStyledLabelProvider {

		@Override
		public StyledString getStyledText(Object element) {
			return new StyledString();
		}

		@Override
		public Image getImage(Object element) {
			return Display.getCurrent().getSystemImage(SWT.ICON_INFORMATION);
		}

		@Override
		public void addListener(ILabelProviderListener listener) {
		}

		@Override
		public void dispose() {
		}

		@Override
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		@Override
		public void removeListener(ILabelProviderListener listener) {
		}

	}

	private Snippet066TableViewerWithLabelDecorator(Shell shell) {
		final TableViewer v = new TableViewer(shell);

		createColumns(v);
		v.setContentProvider(ArrayContentProvider.getInstance());
		v.setInput(createModel());
		v.getTable().setLinesVisible(true);
		v.getTable().setHeaderVisible(true);
	}

	private void createColumns(TableViewer viewer) {
		final String[] columnHeadings = { "ID", "First Name", "Last Name", "Status" };
		final int[] bounds = { 100, 100, 100, 100 };

		createIdColumn(viewer, columnHeadings[0], bounds[0]);

		createFirstNameColumn(viewer, columnHeadings[1], bounds[1]);

		createLastNameColumn(viewer, columnHeadings[2], bounds[2]);

		createStatusColumn(viewer, columnHeadings[3], bounds[3]);
	}

	private void createIdColumn(TableViewer v, final String title, final int bound) {
		TableViewerColumn column = createTableViewerColumn(v, title, bound);
		column.setLabelProvider(new StyledCellLabelProvider() {
			@Override
			public void update(final ViewerCell cell) {
				final Person person = (Person) cell.getElement();
				final String cellText = String.valueOf(person.getId());
				cell.setText(cellText);
			}
		});
	}

	private void createFirstNameColumn(TableViewer v, final String title, final int bound) {
		TableViewerColumn column;
		column = createTableViewerColumn(v, title, bound);
		column.setLabelProvider(new StyledCellLabelProvider() {
			@Override
			public void update(final ViewerCell cell) {
				final Person person = (Person) cell.getElement();
				final String cellText = person.getFirstName();
				cell.setText(cellText);
			}
		});
	}

	private void createLastNameColumn(TableViewer v, final String title, final int bound) {
		TableViewerColumn column = createTableViewerColumn(v, title, bound);
		column.setLabelProvider(new StyledCellLabelProvider() {
			@Override
			public void update(final ViewerCell cell) {
				final Person person = (Person) cell.getElement();
				final String cellText = person.getLastName();
				cell.setText(cellText);
			}
		});
	}

	private void createStatusColumn(TableViewer v, final String title, final int bound) {
		TableViewerColumn column = createTableViewerColumn(v, title, bound);
		column.setLabelProvider(new DecoratingStyledCellLabelProvider(new StatusStyledLabelProvider(),
				new StatusLabelDecorator(), null));
	}

	private TableViewerColumn createTableViewerColumn(final TableViewer viewer, final String title, final int bound) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(bound);
		column.setResizable(true);
		return viewerColumn;
	}

	private List<Person> createModel() {
		List<Person> persons = new ArrayList<>();

		persons.add(new Person(0, "Lars", "Vogel"));
		persons.add(new Person(1, "Jennifer", "Nerlich"));
		persons.add(new Person(2, "Simon", "Scholz"));
		persons.add(new Person(3, "David", "Weiser"));
		persons.add(new Person(3, "Fabian", "Pfaff"));

		return persons;
	}

	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		new Snippet066TableViewerWithLabelDecorator(shell);
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		display.dispose();
	}

}
