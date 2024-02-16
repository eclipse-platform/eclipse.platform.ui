package org.eclipse.jface.internal.text.contentassist;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsArrayWithSize.arrayWithSize;
import static org.hamcrest.collection.IsArrayWithSize.emptyArray;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.mockito.Mockito;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import org.eclipse.jface.internal.text.TableOwnerDrawSupport;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.JFaceResources;

public class CompletionTableDrawSupportTest {

	@Test
	public void testInstall() {
		Shell shell= new Shell();
		Table table= new Table(shell, SWT.NONE);

		assertThat(table.getListeners(SWT.MeasureItem), emptyArray());
		assertThat(table.getListeners(SWT.EraseItem), emptyArray());
		assertThat(table.getListeners(SWT.PaintItem), emptyArray());

		CompletionTableDrawSupport.install(table);

		assertThat(table.getListeners(SWT.MeasureItem), arrayWithSize(1));
		assertThat(table.getListeners(SWT.MeasureItem)[0], instanceOf(TableOwnerDrawSupport.class));
		assertThat(table.getListeners(SWT.EraseItem), arrayWithSize(1));
		assertThat(table.getListeners(SWT.EraseItem)[0], instanceOf(TableOwnerDrawSupport.class));
		assertThat(table.getListeners(SWT.PaintItem), arrayWithSize(1));
		assertThat(table.getListeners(SWT.PaintItem)[0], instanceOf(TableOwnerDrawSupport.class));
	}

	@Test
	public void testStoreStyleRanges() {
		Shell shell= new Shell();
		Table table= new Table(shell, SWT.NONE);
		TableItem tableItem= new TableItem(table, SWT.NONE);
		StyleRange[] ranges= new StyleRange[] {};

		CompletionTableDrawSupport.storeStyleRanges(tableItem, 2, ranges);

		assertEquals(ranges, tableItem.getData("styled_ranges2"));
	}

	@Test
	public void testPaintNonFocusSelectionInFocusColors() {
		int EVENT_Y= 2;
		int EVENT_HEIGHT= 3;

		Shell shell= new Shell();
		Table table= new Table(shell, SWT.NONE);
		TableItem tableItem= new TableItem(table, SWT.NONE);

		ColorRegistry colorRegistry= JFaceResources.getColorRegistry();
		colorRegistry.put("org.eclipse.ui.workbench.SELECTED_CELL_BACKGROUND", new RGB(0, 0, 0));
		colorRegistry.put("org.eclipse.ui.workbench.SELECTED_CELL_FOREGROUND", new RGB(1, 1, 1));
		colorRegistry.put("org.eclipse.ui.workbench.SELECTED_CELL_BACKGROUND_NO_FOCUS", new RGB(2, 2, 2));
		colorRegistry.put("org.eclipse.ui.workbench.SELECTED_CELL_FOREGROUND_NO_FOCUS", new RGB(3, 3, 3));

		GC gc= Mockito.mock(GC.class);
		Event event= new Event();
		event.type= SWT.PaintItem;
		event.gc= gc;
		event.detail= SWT.SELECTED;
		event.item= tableItem;
		event.y= EVENT_Y;
		event.height= EVENT_HEIGHT;

		CompletionTableDrawSupport.install(table);

		table.notifyListeners(SWT.PaintItem, event);

		Mockito.verify(gc).setBackground(new Color(new RGB(0, 0, 0)));
		Mockito.verify(gc).setForeground(new Color(new RGB(1, 1, 1)));
		Mockito.verify(gc).fillRectangle(0, EVENT_Y, table.getBounds().width, EVENT_HEIGHT);
	}

}
