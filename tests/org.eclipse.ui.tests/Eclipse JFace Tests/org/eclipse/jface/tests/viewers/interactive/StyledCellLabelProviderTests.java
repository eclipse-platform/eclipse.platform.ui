/*******************************************************************************
 * Copyright (c) 2007, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Michael Krkoska - initial API and implementation (bug 188333)
 *     Pawel Piech - Bug 291245 - [Viewers] StyledCellLabelProvider.paint(...) does not respect column alignment
 *******************************************************************************/
package org.eclipse.jface.tests.viewers.interactive;

import java.text.DecimalFormat;
import java.text.MessageFormat;

import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * Using a {@link StyledCellLabelProvider} on table viewer.
 */

public class StyledCellLabelProviderTests {
	
	private static int IMAGE_SIZE= 16;

	private static Image IMAGE1;
	private static Image IMAGE2;

	public static void main(String[] args) {

		Display display = new Display();

		JFaceResources.getColorRegistry().put(JFacePreferences.COUNTER_COLOR, new RGB(0,127,174));
		
		IMAGE1= new Image(display, display.getSystemImage(SWT.ICON_WARNING).getImageData().scaledTo(IMAGE_SIZE, IMAGE_SIZE));
		IMAGE2= new Image(display, display.getSystemImage(SWT.ICON_ERROR).getImageData().scaledTo(IMAGE_SIZE, IMAGE_SIZE));

		Shell shell= new Shell(display , SWT.CLOSE | SWT.RESIZE);
		shell.setSize(400, 600);
		shell.setLayout(new GridLayout(1, false));

		StyledCellLabelProviderTests example= new StyledCellLabelProviderTests();
		Control composite= example.createPartControl(shell);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose();
	}

	protected boolean useBold;
	protected TableViewerColumn column;

	public StyledCellLabelProviderTests() {
	}

	public Composite createPartControl(Composite parent) {
		Composite composite= new Composite(parent, SWT.NONE);

		composite.setLayout(new GridLayout(1, true));

		final Label label= new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		label.setText("Operations per second: xxxxx"); //$NON-NLS-1$
		
		final Runnable[] operation = new Runnable[1];
		
		final Button timeButton = new Button(composite, SWT.CHECK);
		timeButton.setText("Time");
		timeButton.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				setTimer(timeButton.getDisplay(), timeButton.getSelection(), operation, label);
			}
		});
		
		final Button stylingButton = new Button(composite, SWT.CHECK);
		stylingButton.setText("enable styling");
		stylingButton.setSelection(true);
		
		final Button boldButton = new Button(composite, SWT.CHECK);
		boldButton.setText("use bold");

		final Button leftButton = new Button(composite, SWT.RADIO);
		leftButton.setText("align left");
		leftButton.setSelection(true);
		final Button centerButton = new Button(composite, SWT.RADIO);
		centerButton.setText("align center");
		final Button rightButton = new Button(composite, SWT.RADIO);
		rightButton.setText("align right");

		final TableViewer tableViewer= new TableViewer(composite, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		tableViewer.getTable().setHeaderVisible(true);
		FontData[] boldFontData= getModifiedFontData(tableViewer.getTable().getFont().getFontData(), SWT.BOLD);
		Font boldFont = new Font(Display.getCurrent(), boldFontData);
		final ExampleLabelProvider labelProvider= new ExampleLabelProvider(boldFont);
		
		createColumn(tableViewer, SWT.LEFT, labelProvider);

		boldButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				useBold = boldButton.getSelection();
				tableViewer.refresh();
			}
		});
		
		operation[0] = new Runnable(){
			public void run() {
				tableViewer.refresh();
			}
		};

		SelectionAdapter adapter = new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				if (((Button)e.getSource()).getSelection()) {
					column.getColumn().dispose();
					int style = e.getSource() == leftButton ? SWT.LEFT : (e.getSource() == centerButton ? SWT.CENTER : SWT.RIGHT);
					createColumn(tableViewer, style, labelProvider);
				}
			}
		}; 
		leftButton.addSelectionListener(adapter);
		centerButton.addSelectionListener(adapter);
		rightButton.addSelectionListener(adapter);

		TestContentProvider contentProvider= new TestContentProvider();
		
		tableViewer.setContentProvider(contentProvider);
		
		stylingButton.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				labelProvider.setOwnerDrawEnabled(stylingButton.getSelection());
				tableViewer.refresh();
			}
		});


		GridData data= new GridData(GridData.FILL, GridData.FILL, true, true);
		tableViewer.getControl().setLayoutData(data);
		tableViewer.setInput(new Object());

		return composite;
	}
	
	private void createColumn(TableViewer viewer, int style, CellLabelProvider labelProvider) {
		column = new TableViewerColumn(viewer, style);
		column.getColumn().setWidth(200);
		column.getColumn().setText("Column");
		column.setLabelProvider(labelProvider);
		viewer.refresh();
	}
	
	boolean timerOn = false;
	long startTime;
	int numOperations;
	DecimalFormat decimalFormat = new DecimalFormat("##.#");
	
	protected void setTimer(final Display display, boolean selection, final Runnable[] operation, final Label resultLabel) {
		timerOn = selection;
		if (timerOn) {
			startTime = System.currentTimeMillis();
			numOperations = 0;
			display.asyncExec(new Runnable() {
				public void run() {
					if (display.isDisposed() || resultLabel.isDisposed()) {
						return;
					}
					if (operation[0] != null) {
						operation[0].run();
					}
					numOperations++;
					long currentTime = System.currentTimeMillis();
					long elapsedTime = currentTime - startTime;
					if (elapsedTime >= 1000) {
						double timePerOperation = elapsedTime / 1000.0 / numOperations;
						double operationsPerSecond = 1.0/timePerOperation;
						resultLabel.setText("Operations per second: " + decimalFormat.format(operationsPerSecond));
						numOperations = 0;
						startTime = System.currentTimeMillis();
					}
					if (timerOn) {
						display.asyncExec(this);
					}
				}
			});
		} else {
			resultLabel.setText("Operations per second: xxxx");
		}
	}

	private static FontData[] getModifiedFontData(FontData[] originalData, int additionalStyle) {
		FontData[] styleData = new FontData[originalData.length];
		for (int i = 0; i < styleData.length; i++) {
			FontData base = originalData[i];
			styleData[i] = new FontData(base.getName(), base.getHeight(), base.getStyle() | additionalStyle);
		}
       	return styleData;
    }
	
	private class ExampleLabelProvider extends StyledCellLabelProvider {

		private final Styler fBoldStyler; 
		
		public ExampleLabelProvider(final Font boldFont) {
			fBoldStyler= new Styler() {
				public void applyStyles(TextStyle textStyle) {
					textStyle.font= boldFont;
				}
			};
		}
		
		public void update(ViewerCell cell) {
			Object element= cell.getElement();
			
			if (element instanceof File) {
				File file= (File) element;
				
				Styler style= file.isDirectory() && useBold ? fBoldStyler: null;
				StyledString styledString= new StyledString(file.getName(), style);
				String decoration = MessageFormat.format(" ({0} bytes)", new Object[] { new Long(file.length()) }); //$NON-NLS-1$
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
		
		protected void measure(Event event, Object element) {
			super.measure(event, element);
		}
	}

	static class File {

		private final String name;
		private final int length;
		private final boolean dir;

		File(String name, int length, boolean dir) {
			this.name = name;
			this.length = length;
			this.dir = dir;
		}

		public int length() {
			return length;
		}

		public String getName() {
			return name;
		}

		boolean isDirectory() {
			return dir;
		}

	}
	
	private static class TestContentProvider implements IStructuredContentProvider {

		public Object[] getElements(Object element) {
			return new File[]{
					new File("asdfkjghfasdkjasdfhjgasdfkjhg", 2348, false),
					new File("sdafkuyasdfkljh", 2348, false),
					new File("asdklufhalsdkhlkjhnklj hlh", 2348, true),
					new File("asdfasdf asdf ", 2348, false),
					new File("fds sdf", 2348, true),
					new File(" sdafuh lsdfahj alsdfk hl", 2348, false),
					new File("sdfahj sdfajk hsdfjkh", 2348, false),
					new File("sdafkja sdfjkh asdfkhj", 2348, false),
					new File("sdfakj hasdfljkha sdfljkh sdfa", 348, true),
					new File("hj ka g", 1334, true),
					new File("asdfjk hsdfaljkh", 2348, false),
					new File("asdh gasdflhg ", 3348, true),
					new File("asd ghasdfkjg sdfkyug ", 4345, false),
					new File("asdf hjasdflkjh sdfal", 5345, false),
					new File("asdlfuh afsdhjg fdsalhj", 6648, false),
					new File("uiy viuh vhj v", 7448, true),
					new File("sdfauighsdvpyu ghasjkn", 8848, true),
					new File("asduih cuia ;nac", 9548, false),
					new File("chju kljhuuklh jk;", 348, false),
					new File("cdailukhu l;hj .n", 448, false),
					new File("auihy akl;h l;j", 2348, false),
					new File("caiugh j l;kjlh jcd", 2328, true),
					new File("auio;h jkh lhjl h ljjhbvj", 2348, true),
					new File("ajklkja kj lkjh jklh ", 2248, false),
					new File("asdfkjghfasdkjasdfhjgasdfkjhg", 2348, true),
					new File("sdafkuyasdfkljh", 2348, false),
					new File("asdklufhalsdkhlkjhnklj hlh", 2348, true),
					new File("asdfasdf asdf ", 2348, false),
					new File("fds sdf", 2348, true),
					new File(" sdafuh lsdfahj alsdfk hl", 2348, true),
					new File("sdfahj sdfajk hsdfjkh", 2348, false),
					new File("sdafkja sdfjkh asdfkhj", 2348, true),
					new File("sdfakj hasdfljkha sdfljkh sdfa", 348, true),
					new File("hj ka g", 1334, false),
					new File("asdfjk hsdfaljkh", 2348, false),
					new File("asdh gasdflhg ", 3348, true),
					new File("asd ghasdfkjg sdfkyug ", 4345, true),
					new File("asdf hjasdflkjh sdfal", 5345, true),
					new File("asdlfuh afsdhjg fdsalhj", 6648, false),
					new File("uiy viuh vhj v", 7448, false),
					new File("sdfauighsdvpyu ghasjkn", 8848, true),
					new File("asduih cuia ;nac", 9548, false),
					new File("chju kljhuuklh jk;", 348, true),
					new File("cdailukhu l;hj .n", 448, true),
					new File("auihy akl;h l;j", 2348, false),
					new File("caiugh j l;kjlh jcd", 2328, true),
					new File("auio;h jkh lhjl h ljjhbvj", 2348, false),
					new File("ajklkja kj lkjh jklh ", 2248, true),
			};
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
}
