package org.eclipse.jface.internal.text;

import static org.mockito.Mockito.doReturn;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.JFaceResources;

public class TableOwnerDrawSupportTest {

	private static final int EVENT_Y= 2;

	private static final int EVENT_HEIGHT= 3;

	private Table table;

	private Event event;

	private RGB selectedRowBackgroundColor= new RGB(0, 0, 0);

	private RGB selectedRowForegroundColor= new RGB(1, 1, 1);

	private RGB selectedRowBackgroundColorNoFocus= new RGB(2, 2, 2);

	private RGB selectedRowForegroundColorNoFocus= new RGB(3, 3, 3);

	private GC gc;

	@Before
	public void setup() {
		Shell shell= new Shell(Display.getDefault());
		Table originalTable= new Table(shell, SWT.SINGLE);
		//Spy in order to overwrite isFocusControl()
		table= Mockito.spy(originalTable);

		TableItem tableItem= Mockito.mock(TableItem.class);
		Mockito.when(tableItem.getParent()).thenReturn(table);

		gc= Mockito.mock(GC.class);
		event= new Event();
		event.type= SWT.PaintItem;
		event.gc= gc;
		event.detail= SWT.SELECTED;
		event.item= tableItem;
		event.y= EVENT_Y;
		event.height= EVENT_HEIGHT;

		Display.getDefault();
		ColorRegistry colorRegistry= JFaceResources.getColorRegistry();
		colorRegistry.put("org.eclipse.ui.workbench.SELECTED_CELL_BACKGROUND", new RGB(0, 0, 0));
		colorRegistry.put("org.eclipse.ui.workbench.SELECTED_CELL_FOREGROUND", new RGB(1, 1, 1));
		colorRegistry.put("org.eclipse.ui.workbench.SELECTED_CELL_BACKGROUND_NO_FOCUS", new RGB(2, 2, 2));
		colorRegistry.put("org.eclipse.ui.workbench.SELECTED_CELL_FOREGROUND_NO_FOCUS", new RGB(3, 3, 3));

		TableOwnerDrawSupport.install(table);
	}

	@Test
	public void testPaintSelectionFocus() {
		doReturn(true).when(table).isFocusControl();

		table.notifyListeners(SWT.PaintItem, event);

		Mockito.verify(gc).setBackground(new Color(selectedRowBackgroundColor));
		Mockito.verify(gc).setForeground(new Color(selectedRowForegroundColor));
		Mockito.verify(gc).fillRectangle(0, EVENT_Y, table.getBounds().width, EVENT_HEIGHT);
	}

	@Test
	public void testPaintSelectionNoFocus() {
		doReturn(false).when(table).isFocusControl();

		table.notifyListeners(SWT.PaintItem, event);

		Mockito.verify(gc).setBackground(new Color(selectedRowBackgroundColorNoFocus));
		Mockito.verify(gc).setForeground(new Color(selectedRowForegroundColorNoFocus));
		Mockito.verify(gc).fillRectangle(0, EVENT_Y, table.getBounds().width, EVENT_HEIGHT);
	}

}
