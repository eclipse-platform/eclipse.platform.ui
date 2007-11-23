/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
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
import org.eclipse.jface.viewers.OwnerDrawLabelProvider;
import org.eclipse.jface.viewers.SimpleStyledCellLabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;


/**
 * Using a {@link SimpleStyledCellLabelProvider} on tree viewer. Compare the result with a native tree viewer.
 */
public class Snippet100SimpleStyledCellLabelProvider {
	
	
	private static final int SHELL_WIDTH= 640;
	private static final Display DISPLAY= Display.getDefault();


	public static void main(String[] args) {

		Shell shell= new Shell(DISPLAY, SWT.CLOSE | SWT.RESIZE);
		shell.setSize(SHELL_WIDTH, 300);
		shell.setLayout(new GridLayout(1, false));

		Snippet100SimpleStyledCellLabelProvider example= new Snippet100SimpleStyledCellLabelProvider();
		example.createPartControl(shell);

		shell.open();

		while (!shell.isDisposed()) {
			if (!DISPLAY.readAndDispatch()) {
				DISPLAY.sleep();
			}
		}
		DISPLAY.dispose();
	}

	public Snippet100SimpleStyledCellLabelProvider() {
	}

	public void createPartControl(Composite parent) {
		Composite composite= new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		composite.setLayout(new GridLayout(2, true));

		ExampleLabelProvider labelProvider= new ExampleLabelProvider();
		ModifiedDateLabelProvider dateLabelProvider= new ModifiedDateLabelProvider();

		final ColumnViewer ownerDrawViewer= createViewer("Owner draw viewer:", composite, new DecoratingLabelProvider(labelProvider), new DecoratingDateLabelProvider(dateLabelProvider)); //$NON-NLS-1$
		OwnerDrawLabelProvider.setUpOwnerDraw(ownerDrawViewer);

		final ColumnViewer normalViewer= createViewer("Normal viewer:", composite, labelProvider, dateLabelProvider); //$NON-NLS-1$

		Button button= new Button(parent, SWT.NONE);
		button.setText("Refresh Viewers"); //$NON-NLS-1$
		button.addListener(SWT.Modify, new Listener() {

			public void handleEvent(Event event) {
				ownerDrawViewer.refresh();
				normalViewer.refresh();
			}
		});

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
		File[] roots = File.listRoots();
		File root = null;
		for (int i = 0; i < roots.length; i++) {
			String[] list = roots[i].list();
			if (list != null && list.length > 0) {
				root = roots[i];
				break;
			}
		}
		if (root == null) {
			throw new RuntimeException("couldn't get a non-empty root file");
		}
		treeViewer.setInput(root);

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
