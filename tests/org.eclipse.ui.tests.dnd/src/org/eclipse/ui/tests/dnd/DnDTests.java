package org.eclipse.ui.tests.dnd;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.function.BiConsumer;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class DnDTests {

	static Display display;
	static Shell shell;

	@BeforeAll
	public static void setupDisplay() {
		display = Display.getDefault();
		shell = new Shell(display);
	}

	@AfterAll
	public static void disposeDisplay() {
		if (shell != null && !shell.isDisposed())
			shell.dispose();
	}

	@Test
	public void testSingleItemDragAndDrop() {
		display.syncExec(() -> {
			List source = new List(shell, SWT.BORDER);
			List target = new List(shell, SWT.BORDER);
			source.add("Item 1");

			DnDTestHelper helper = new DnDTestHelper();
			helper.selectItems(source, "Item 1");
			helper.simulateDrag(source, target, DND.DROP_COPY, "Item 1");

			assertEquals(1, source.getSelectionCount());
			assertEquals("Item 1", source.getSelection()[0]);
		});
	}

	@Test
	public void testMultiItemDragAcrossWidgets() {
	    display.syncExec(() -> {
	        List listSource = new List(shell, SWT.BORDER);
	        Table tableSource = new Table(shell, SWT.BORDER);
	        Tree treeSource = new Tree(shell, SWT.BORDER);
	        
	        List listTarget = new List(shell, SWT.BORDER);
	        Table tableTarget = new Table(shell, SWT.BORDER);
	        Tree treeTarget = new Tree(shell, SWT.BORDER);
	        
	        listSource.add("L1");	
	        listSource.add("L2");
	        
	        TableItem t1 = new TableItem(tableSource, SWT.NONE);
	        t1.setText("T1");
	        TableItem t2 = new TableItem(tableSource, SWT.NONE);
	        t2.setText("T2");
	        
	        TreeItem tr1 = new TreeItem(treeSource, SWT.NONE);
	        tr1.setText("TR1");
	        TreeItem tr2 = new TreeItem(treeSource, SWT.NONE);
	        tr2.setText("TR2");
	        
	        DnDTestHelper helper = new DnDTestHelper();
	        
	        BiConsumer<Widget, Object[]> assertTargetContent = (target, expectedItems) -> {
	        	 if(target instanceof List l) {
	        		 assertEquals(expectedItems.length, l.getItemCount());
	        		 
	        	 }
	        };
	        
	     
	    });
	}

	@Test
	public void testDropWithInvalidData() {
	}

	@Test
	public void testDropOnDisposedWidget() {
	}

	@Test
	public void testDropOnInvisibleWidget() {
	}

	@Test
	public void testDragOperations() {
	}

	@Test
	public void testCustomDataTransfer() {
	}

	@Test
	public void testEventOrder() {
	}

	@Test
	public void testSelectionClearedAfterDrag() {
	}

	@Test
	public void testDragWithEmptySelection() {
	}

}
