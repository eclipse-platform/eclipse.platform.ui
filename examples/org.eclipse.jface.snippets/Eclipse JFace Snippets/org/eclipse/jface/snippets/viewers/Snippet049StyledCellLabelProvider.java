/*******************************************************************************
 * Copyright (c) 2007, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Michael Krkoska - initial API and implementation (bug 188333)
 *     Lars Vogel (lars.vogel@gmail.com) - Bug 413427, 487940
 *******************************************************************************/
package org.eclipse.jface.snippets.viewers;

import java.io.File;
import java.text.MessageFormat;

import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * Using a {@link StyledCellLabelProvider} on table viewer.
 */

public class Snippet049StyledCellLabelProvider {


	private static final int SHELL_WIDTH= 400;
	private static final Display DISPLAY= Display.getDefault();


	public static void main(String[] args) {

		JFaceResources.getColorRegistry().put(JFacePreferences.COUNTER_COLOR, new RGB(0,127,174));

		Shell shell= new Shell(DISPLAY, SWT.CLOSE | SWT.RESIZE);
		shell.setSize(SHELL_WIDTH, 400);
		shell.setLayout(new GridLayout(1, false));

		Snippet049StyledCellLabelProvider example= new Snippet049StyledCellLabelProvider();
		Control composite= example.createPartControl(shell);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		shell.open();

		while (!shell.isDisposed()) {
			if (!DISPLAY.readAndDispatch()) {
				DISPLAY.sleep();
			}
		}
		DISPLAY.dispose();
	}

	public Snippet049StyledCellLabelProvider() {
	}

	public Composite createPartControl(Composite parent) {
		Composite composite= new Composite(parent, SWT.NONE);

		composite.setLayout(new GridLayout(1, true));

		Label label= new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		label.setText("Viewer with a StyledCellLabelProvider:"); //$NON-NLS-1$

		final TableViewer tableViewer= new TableViewer(composite, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);

		// Multi-font support only works in JFace 3.5 and above (specifically, 3.5 M4 and above).
		// With JFace 3.4, the font information (bold in this example) will be ignored.
		FontData[] boldFontData= getModifiedFontData(tableViewer.getTable().getFont().getFontData(), SWT.BOLD);

		Font boldFont = new Font(Display.getCurrent(), boldFontData);
		ExampleLabelProvider labelProvider= new ExampleLabelProvider(boldFont);
		FileSystemContentProvider contentProvider= new FileSystemContentProvider();

		tableViewer.setContentProvider(contentProvider);
		tableViewer.setLabelProvider(labelProvider);

		GridData data= new GridData(GridData.FILL, GridData.FILL, true, true);
		tableViewer.getControl().setLayoutData(data);
		tableViewer.setInput(new Object());

		return composite;
	}

	private static FontData[] getModifiedFontData(FontData[] originalData, int additionalStyle) {
		FontData[] styleData = new FontData[originalData.length];
		for (int i = 0; i < styleData.length; i++) {
			FontData base = originalData[i];
			styleData[i] = new FontData(base.getName(), base.getHeight(), base.getStyle() | additionalStyle);
		}
       	return styleData;
    }

	private static class ExampleLabelProvider extends StyledCellLabelProvider {

		private static int IMAGE_SIZE= 16;
		private static final Image IMAGE1= new Image(DISPLAY, DISPLAY.getSystemImage(SWT.ICON_WARNING).getImageData().scaledTo(IMAGE_SIZE, IMAGE_SIZE));
		private static final Image IMAGE2= new Image(DISPLAY, DISPLAY.getSystemImage(SWT.ICON_ERROR).getImageData().scaledTo(IMAGE_SIZE, IMAGE_SIZE));

		private final Styler fBoldStyler;

		public ExampleLabelProvider(final Font boldFont) {
			fBoldStyler= new Styler() {
				@Override
				public void applyStyles(TextStyle textStyle) {
					textStyle.font= boldFont;
				}
			};
		}

		@Override
		public void update(ViewerCell cell) {
			Object element= cell.getElement();

			if (element instanceof File) {
				File file= (File) element;

				// Multi-font support only works in JFace 3.5 and above (specifically, 3.5 M4 and above).
				// With JFace 3.4, the font information (bold in this example) will be ignored.
				Styler style= file.isDirectory() ? fBoldStyler: null;
				StyledString styledString= new StyledString(file.getName(), style);
				String decoration = MessageFormat.format(" ({0} bytes)", new Long(file.length())); //$NON-NLS-1$
				styledString.append(decoration, StyledString.COUNTER_STYLER);

				cell.setText(styledString.toString());
				cell.setStyleRanges(styledString.getStyleRanges());

				if (file.isDirectory()) {
					cell.setImage(IMAGE1);
				} else {
					cell.setImage(IMAGE2);
				}
			} else {
				cell.setText("Unknown element"); //$NON-NLS-1$
			}

			super.update(cell);
		}

		@Override
		protected void measure(Event event, Object element) {
			super.measure(event, element);
		}
	}

	private static class FileSystemContentProvider implements IStructuredContentProvider {

		@Override
		public Object[] getElements(Object element) {
			File[] roots = File.listRoots();
			for (int i = 0; i < roots.length; i++) {
				File[] list = roots[i].listFiles();
				if (list != null && list.length > 0) {
					return list;
				}
			}
			return roots;
		}

	}
}
