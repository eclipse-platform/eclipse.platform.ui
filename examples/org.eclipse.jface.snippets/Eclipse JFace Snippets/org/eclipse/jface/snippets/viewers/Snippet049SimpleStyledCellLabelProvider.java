/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Michael Krkoska - initial API and implementation (bug 188333)
 *******************************************************************************/
package org.eclipse.jface.snippets.viewers;

import java.io.File;
import java.text.MessageFormat;
import java.util.Date;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SimpleStyledCellLabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;


/**
 * Using a {@link SimpleStyledCellLabelProvider} on tree viewer. Compare the result with a native tree viewer.
 */
public class Snippet049SimpleStyledCellLabelProvider {
	
	
	private static final int SHELL_WIDTH= 640;
	private static final Display DISPLAY= Display.getDefault();


	public static void main(String[] args) {

		Shell shell= new Shell(DISPLAY, SWT.CLOSE | SWT.RESIZE);
		shell.setSize(SHELL_WIDTH, 300);
		shell.setLayout(new GridLayout(1, false));

		Snippet049SimpleStyledCellLabelProvider example= new Snippet049SimpleStyledCellLabelProvider();
		example.createPartControl(shell);

		shell.open();

		while (!shell.isDisposed()) {
			if (!DISPLAY.readAndDispatch()) {
				DISPLAY.sleep();
			}
		}
		DISPLAY.dispose();
	}

	public Snippet049SimpleStyledCellLabelProvider() {
	}

	public void createPartControl(Composite parent) {
		Composite composite= new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(2, true));

		ExampleLabelProvider labelProvider= new ExampleLabelProvider();
		ModifiedDateLabelProvider dateLabelProvider= new ModifiedDateLabelProvider();

		final ColumnViewer ownerDrawViewer= createViewer("Owner draw viewer:", composite, new DecoratingLabelProvider(labelProvider), new DecoratingDateLabelProvider(dateLabelProvider)); //$NON-NLS-1$

		final ColumnViewer normalViewer= createViewer("Normal viewer:", composite, labelProvider, dateLabelProvider); //$NON-NLS-1$

		Composite buttons= new Composite(parent, SWT.NONE);
		buttons.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		buttons.setLayout(new GridLayout(3, false));
		
		
		Button button1= new Button(buttons, SWT.PUSH);
		button1.setText("Refresh Viewers"); //$NON-NLS-1$
		button1.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				ownerDrawViewer.refresh();
				normalViewer.refresh();
			}
		});
		
		final Button button2= new Button(buttons, SWT.CHECK);
		button2.setText("Owner draw on column 1"); //$NON-NLS-1$
		button2.setSelection(true);
		button2.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				boolean newState= button2.getSelection();
				((DecoratingLabelProvider) ownerDrawViewer.getLabelProvider(0)).setOwnerDrawEnabled(newState);
				ownerDrawViewer.refresh();
			}
		});
		
		final Button button3= new Button(buttons, SWT.CHECK);
		button3.setText("Owner draw on column 2"); //$NON-NLS-1$
		button3.setSelection(true);
		button3.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				boolean newState= button3.getSelection();
				((DecoratingDateLabelProvider) ownerDrawViewer.getLabelProvider(1)).setOwnerDrawEnabled(newState);
				ownerDrawViewer.refresh();
			}
		});

	}
	
	private static class FileSystemRoot {
		public File[] getRoots() {
			return File.listRoots();
		}
	}

	private ColumnViewer createViewer(String description, Composite parent, CellLabelProvider labelProvider1, CellLabelProvider labelProvider2) {

		Composite composite= new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		composite.setLayout(new GridLayout(1, true));

		Label label= new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		label.setText(description);

		TreeViewer treeViewer= new TreeViewer(composite, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		treeViewer.getTree().setHeaderVisible(true);
		treeViewer.setContentProvider(new FileSystemContentProvider());
		
		TreeViewerColumn tvc1 = new TreeViewerColumn(treeViewer, SWT.NONE);
		tvc1.getColumn().setText("Name");
		tvc1.getColumn().setWidth(200);
		tvc1.setLabelProvider(labelProvider1);

		TreeViewerColumn tvc2 = new TreeViewerColumn(treeViewer, SWT.NONE);
		tvc2.getColumn().setText("Date Modified");
		tvc2.getColumn().setWidth(200);
		tvc2.setLabelProvider(labelProvider2);
		
		GridData data= new GridData(GridData.FILL, GridData.FILL, true, true);
		treeViewer.getControl().setLayoutData(data);

		treeViewer.setInput(new FileSystemRoot());

		return treeViewer;
	}
	
	/**
	 * Implements a {@link SimpleStyledCellLabelProvider} that wraps a normal label
	 * provider and adds some decorations in color
	 */
	private static class DecoratingLabelProvider extends SimpleStyledCellLabelProvider {

		private static final StyleRange[] NO_RANGES= new StyleRange[0];
		private final ILabelProvider fWrappedLabelProvider;
		
		public DecoratingLabelProvider(ILabelProvider labelProvider) {
			fWrappedLabelProvider= labelProvider;
		}

		protected LabelPresentationInfo getLabelPresentationInfo(Object element) {
			String text= fWrappedLabelProvider.getText(element);
			Image image= fWrappedLabelProvider.getImage(element);

			
			StyleRange[] ranges= NO_RANGES;
			if (element instanceof File) {
				File file= (File) element;
				if (file.isFile()) {
					String decoration= MessageFormat.format(" ({0} bytes)", new Object[] { new Long(file.length()) }); //$NON-NLS-1$
										
					int decorationStart= text.length();
					int decorationLength= decoration.length();

					text+= decoration;
					
					Color decorationColor= Display.getDefault().getSystemColor(SWT.COLOR_DARK_BLUE);
					
					StyleRange styleRange= new StyleRange(decorationStart, decorationLength, decorationColor, null);
					ranges= new StyleRange[] { styleRange };
				}
			}
			return new LabelPresentationInfo(text, ranges, image, null, null, null);
		}
		
		public void dispose() {
			super.dispose();
			fWrappedLabelProvider.dispose();
		}
	}
	
	private static class DecoratingDateLabelProvider extends SimpleStyledCellLabelProvider {
		
		private static final String[] DAYS = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
		private static final StyleRange[] NO_RANGES= new StyleRange[0];
		private final ILabelProvider fWrappedLabelProvider;
		
		public DecoratingDateLabelProvider(ILabelProvider labelProvider) {
			fWrappedLabelProvider= labelProvider;
		}
		
		protected LabelPresentationInfo getLabelPresentationInfo(Object element) {
			String text= fWrappedLabelProvider.getText(element);
			Image image= fWrappedLabelProvider.getImage(element);
			
			StyleRange[] ranges= NO_RANGES;
			if (element instanceof File) {
				File file= (File) element;
				String decoration= " " + DAYS[new Date(file.lastModified()).getDay()]; 
				
				int decorationStart= text.length();
				int decorationLength= decoration.length();
				
				text+= decoration;
				
				Color decorationColor= Display.getDefault().getSystemColor(SWT.COLOR_GRAY);
				
				StyleRange styleRange= new StyleRange(decorationStart, decorationLength, decorationColor, null);
				ranges= new StyleRange[] { styleRange };
			}
			return new LabelPresentationInfo(text, ranges, image, null, null, null);
		}
		
		public void dispose() {
			super.dispose();
			fWrappedLabelProvider.dispose();
		}
	}
	

	/**
	 * A simple label provider
	 */
	private static class ExampleLabelProvider extends ColumnLabelProvider {
		
		private static int IMAGE_SIZE= 16;
		private static final Image IMAGE1= new Image(DISPLAY, DISPLAY.getSystemImage(SWT.ICON_WARNING).getImageData().scaledTo(IMAGE_SIZE, IMAGE_SIZE));
		private static final Image IMAGE2= new Image(DISPLAY, DISPLAY.getSystemImage(SWT.ICON_ERROR).getImageData().scaledTo(IMAGE_SIZE, IMAGE_SIZE));

		public Image getImage(Object element) {
			if (element instanceof File) {
				File file= (File) element;
				if (file.isDirectory()) {
					return IMAGE1;
				} else {
					return IMAGE2;
				}
			}
			return null;
		}

		public String getText(Object element) {
			if (element instanceof File) {
				File file= (File) element;
				if (file.getName().length() == 0) {
					return file.getAbsolutePath();
				}
				return file.getName();
			}
			return "null"; //$NON-NLS-1$
		}

	}
	
	private static class ModifiedDateLabelProvider extends ColumnLabelProvider {
		public String getText(Object element) {
			if (element instanceof File) {
				File file= (File) element;
				return new Date(file.lastModified()).toLocaleString();
			}
			return "-"; //$NON-NLS-1$
		}
	}
	
	private static class FileSystemContentProvider implements ITreeContentProvider {

		public Object[] getChildren(Object element) {
			if (element instanceof File) {
				File file= (File) element;
				if (file.isDirectory()) {
					File[] listFiles= file.listFiles();
					if (listFiles != null) {
						return listFiles;
					}
				}
			} else if (element instanceof FileSystemRoot) {
				return ((FileSystemRoot) element).getRoots();
			}
			return new Object[0];
		}

		public Object getParent(Object element) {
			if (element instanceof File) {
				File file= (File) element;
				return file.getParentFile();
			}
			return null;
		}

		public boolean hasChildren(Object element) {
			return getChildren(element).length > 0;
		}

		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}




}
