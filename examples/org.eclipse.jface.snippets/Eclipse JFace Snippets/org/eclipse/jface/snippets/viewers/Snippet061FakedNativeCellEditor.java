/*******************************************************************************
 * Copyright (c) 2006, 2010 Tom Schindl and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl<tom.schindl@bestsolution.at> - initial API and implementation
 *     Florian Potschka<signalrauschen@gmail.com> - in bug 260061
 *     Alexander Ljungberg<siker@norwinter.com> - in bug 260061
 *******************************************************************************/

package org.eclipse.jface.snippets.viewers;

import java.util.ArrayList;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.Util;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.OwnerDrawLabelProvider;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.TreeViewerEditor;
import org.eclipse.jface.viewers.TreeViewerFocusCellManager;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.TreeItem;

/**
 * A simple TreeViewer to demonstrate usage
 * 
 * @author Tom Schindl <tom.schindl@bestsolution.at>
 * 
 */
public class Snippet061FakedNativeCellEditor {
	public abstract class EmulatedNativeCheckBoxLabelProvider extends
			OwnerDrawLabelProvider {
		private static final String CHECKED_KEY = "CHECKED";
		private static final String UNCHECK_KEY = "UNCHECKED";

		public EmulatedNativeCheckBoxLabelProvider(ColumnViewer viewer) {
			if (JFaceResources.getImageRegistry().getDescriptor(CHECKED_KEY) == null) {
				JFaceResources.getImageRegistry().put(UNCHECK_KEY,
						makeShot(viewer.getControl(), false));
				JFaceResources.getImageRegistry().put(CHECKED_KEY,
						makeShot(viewer.getControl(), true));
			}
		}

		private Image makeShot(Control control, boolean type) {
			// Hopefully no platform uses exactly this color because we'll make
			// it transparent in the image.
			Color greenScreen = new Color(control.getDisplay(), 222, 223, 224);

			Shell shell = new Shell(control.getShell(), SWT.NO_TRIM);
			
			// otherwise we have a default gray color
			shell.setBackground(greenScreen);

			if( Util.isMac() ) {
				Button button2 = new Button(shell, SWT.CHECK);
				Point bsize = button2.computeSize(SWT.DEFAULT, SWT.DEFAULT);
				
				// otherwise an image is stretched by width
				bsize.x = Math.max(bsize.x - 1, bsize.y - 1);
				bsize.y = Math.max(bsize.x - 1, bsize.y - 1);
				button2.setSize(bsize);
				button2.setLocation(100, 100);				
			}
			
			Button button = new Button(shell, SWT.CHECK);
			button.setBackground(greenScreen);
			button.setSelection(type);

			// otherwise an image is located in a corner
			button.setLocation(1, 1);
			Point bsize = button.computeSize(SWT.DEFAULT, SWT.DEFAULT);
			
			// otherwise an image is stretched by width
			bsize.x = Math.max(bsize.x - 1, bsize.y - 1);
			bsize.y = Math.max(bsize.x - 1, bsize.y - 1);
			button.setSize(bsize);
			
			shell.setSize(bsize);

			shell.open();
			
			GC gc = new GC(shell);
			Image image = new Image(control.getDisplay(), bsize.x, bsize.y);
			gc.copyArea(image, 0, 0);
			gc.dispose();
			shell.close();

			ImageData imageData = image.getImageData();
			imageData.transparentPixel = imageData.palette.getPixel(greenScreen
					.getRGB());

			Image img = new Image(control.getDisplay(), imageData);
			image.dispose();
			
			return img;
		}

		public Image getImage(Object element) {
			if (isChecked(element)) {
				return JFaceResources.getImageRegistry().get(CHECKED_KEY);
			} else {
				return JFaceResources.getImageRegistry().get(UNCHECK_KEY);
			}
		}

		protected void measure(Event event, Object element) {
			event.height = getImage(element).getBounds().height;
		}

		protected void paint(Event event, Object element) {

			Image img = getImage(element);

			if (img != null) {
				Rectangle bounds;

				if (event.item instanceof TableItem) {
					bounds = ((TableItem) event.item).getBounds(event.index);
				} else {
					bounds = ((TreeItem) event.item).getBounds(event.index);
				}

				Rectangle imgBounds = img.getBounds();
				bounds.width /= 2;
				bounds.width -= imgBounds.width / 2;
				bounds.height /= 2;
				bounds.height -= imgBounds.height / 2;

				int x = bounds.width > 0 ? bounds.x + bounds.width : bounds.x;
				int y = bounds.height > 0 ? bounds.y + bounds.height : bounds.y;

				if (SWT.getPlatform().equals("carbon")) {
					event.gc.drawImage(img, x + 2, y - 1);
				} else {
					event.gc.drawImage(img, x, y - 1);
				}

			}
		}

		protected abstract boolean isChecked(Object element);
	}

	public Snippet061FakedNativeCellEditor(final Shell shell) {
		final TreeViewer v = new TreeViewer(shell, SWT.BORDER
				| SWT.FULL_SELECTION);
		v.getTree().setLinesVisible(true);
		v.getTree().setBackgroundMode(SWT.INHERIT_DEFAULT);
		v.getTree().setHeaderVisible(true);

		FocusCellOwnerDrawHighlighter h = new FocusCellOwnerDrawHighlighter(v) {

			protected Color getSelectedCellBackgroundColorNoFocus(
					ViewerCell cell) {
				return shell.getDisplay().getSystemColor(
						SWT.COLOR_LIST_SELECTION);
			}

			protected Color getSelectedCellForegroundColorNoFocus(
					ViewerCell cell) {
				return shell.getDisplay().getSystemColor(
						SWT.COLOR_WIDGET_FOREGROUND);
			}
		};

		TreeViewerFocusCellManager focusCellManager = new TreeViewerFocusCellManager(
				v, h);
		ColumnViewerEditorActivationStrategy actSupport = new ColumnViewerEditorActivationStrategy(
				v);

		TreeViewerEditor.create(v, focusCellManager, actSupport,
				ColumnViewerEditor.TABBING_HORIZONTAL
						| ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR
						| ColumnViewerEditor.TABBING_VERTICAL
						| ColumnViewerEditor.KEYBOARD_ACTIVATION);

		final TextCellEditor textCellEditor = new TextCellEditor(v.getTree());
		final BooleanCellEditor booleanCellEditor = new BooleanCellEditor(v
				.getTree());
		booleanCellEditor.setChangeOnActivation(true);

		TreeViewerColumn column = new TreeViewerColumn(v, SWT.NONE);
		column.getColumn().setWidth(200);
		column.getColumn().setMoveable(true);
		column.getColumn().setText("File");
		column.setLabelProvider(new OwnerDrawLabelProvider() {

			protected void measure(Event event, Object element) {

			}

			protected void paint(Event event, Object element) {
				((TreeItem) event.item).setText(element.toString());
			}

		});
		column.setEditingSupport(new EditingSupport(v) {
			protected boolean canEdit(Object element) {
				return true;
			}

			protected CellEditor getCellEditor(Object element) {
				return textCellEditor;
			}

			protected Object getValue(Object element) {
				return ((File) element).counter + "";
			}

			protected void setValue(Object element, Object value) {
				((File) element).counter = Integer.parseInt(value.toString());
				v.update(element, null);
			}
		});

		column = new TreeViewerColumn(v, SWT.CENTER);
		column.getColumn().setWidth(200);
		column.getColumn().setMoveable(true);
		column.getColumn().setText("Read");
		column.setLabelProvider(new EmulatedNativeCheckBoxLabelProvider(v) {

			protected boolean isChecked(Object element) {
				return ((File) element).read;
			}

		});
		column.setEditingSupport(new EditingSupport(v) {
			protected boolean canEdit(Object element) {
				return true;
			}

			protected CellEditor getCellEditor(Object element) {
				return booleanCellEditor;
			}

			protected Object getValue(Object element) {
				return new Boolean(((File) element).read);
			}

			protected void setValue(Object element, Object value) {
				((File) element).read = ((Boolean) value).booleanValue();
				v.update(element, null);
			}
		});

		column = new TreeViewerColumn(v, SWT.CENTER);
		column.getColumn().setWidth(200);
		column.getColumn().setMoveable(true);
		column.getColumn().setText("Write");
		column.setLabelProvider(new EmulatedNativeCheckBoxLabelProvider(v) {

			protected boolean isChecked(Object element) {
				return ((File) element).write;
			}

		});
		column.setEditingSupport(new EditingSupport(v) {
			protected boolean canEdit(Object element) {
				return true;
			}

			protected CellEditor getCellEditor(Object element) {
				return booleanCellEditor;
			}

			protected Object getValue(Object element) {
				return new Boolean(((File) element).write);
			}

			protected void setValue(Object element, Object value) {
				((File) element).write = ((Boolean) value).booleanValue();
				v.update(element, null);
			}
		});

		column = new TreeViewerColumn(v, SWT.CENTER);
		column.getColumn().setWidth(200);
		column.getColumn().setMoveable(true);
		column.getColumn().setText("Execute");
		column.setLabelProvider(new EmulatedNativeCheckBoxLabelProvider(v) {

			protected boolean isChecked(Object element) {
				return ((File) element).execute;
			}

		});
		column.setEditingSupport(new EditingSupport(v) {
			protected boolean canEdit(Object element) {
				return true;
			}

			protected CellEditor getCellEditor(Object element) {
				return booleanCellEditor;
			}

			protected Object getValue(Object element) {
				return new Boolean(((File) element).execute);
			}

			protected void setValue(Object element, Object value) {
				((File) element).execute = ((Boolean) value).booleanValue();
				v.update(element, null);
			}
		});

		v.setContentProvider(new MyContentProvider());

		v.setInput(createModel());
	}

	private File createModel() {

		File root = new File(0, null);
		root.counter = 0;

		File tmp;
		File subItem;
		for (int i = 1; i < 10; i++) {
			tmp = new File(i, root);
			root.child.add(tmp);
			for (int j = 1; j < i; j++) {
				subItem = new File(j, tmp);
				subItem.child.add(new File(j * 100, subItem));
				tmp.child.add(subItem);
			}
		}

		return root;
	}

	public static void main(String[] args) {
		try {
			Display display = new Display();

			Shell shell = new Shell(display);
			shell.setLayout(new FillLayout());
			new Snippet061FakedNativeCellEditor(shell);
			shell.open();

			while (!shell.isDisposed()) {
				if (!display.readAndDispatch())
					display.sleep();
			}

			display.dispose();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private class MyContentProvider implements ITreeContentProvider {

		public Object[] getElements(Object inputElement) {
			return ((File) inputElement).child.toArray();
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		public Object[] getChildren(Object parentElement) {
			return getElements(parentElement);
		}

		public Object getParent(Object element) {
			if (element == null) {
				return null;
			}
			return ((File) element).parent;
		}

		public boolean hasChildren(Object element) {
			return ((File) element).child.size() > 0;
		}

	}

	public class File {
		public File parent;

		public ArrayList child = new ArrayList();

		public int counter;

		public boolean read;
		public boolean write;
		public boolean execute;

		public File(int counter, File parent) {
			this.parent = parent;
			this.counter = counter;
			this.read = counter % 2 == 0;
			this.write = counter % 3 == 0;
			this.execute = counter % 4 == 0;
		}

		public String toString() {
			String rv = "Item ";
			if (parent != null) {
				rv = parent.toString() + ".";
			}

			rv += counter;

			return rv;
		}
	}

}
