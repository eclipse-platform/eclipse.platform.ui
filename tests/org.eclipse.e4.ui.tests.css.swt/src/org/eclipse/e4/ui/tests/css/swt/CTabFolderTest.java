package org.eclipse.e4.ui.tests.css.swt;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class CTabFolderTest extends CSSTestCase {

	static final RGB BLUE = new RGB(0, 0, 255);

	protected CTabFolder createTestCTabFolder(String styleSheet) {
		Display display = Display.getDefault();
		CSSEngine engine = createEngine(styleSheet, display);

		// Create widgets
		Shell shell = new Shell(display, SWT.SHELL_TRIM);
		FillLayout layout = new FillLayout();
		shell.setLayout(layout);
		Composite panel = new Composite(shell, SWT.NONE);
		panel.setLayout(new FillLayout());

		CTabFolder folderToTest = new CTabFolder(panel, SWT.NONE);
		CTabItem tab1 = new CTabItem(folderToTest, SWT.NONE);
		tab1.setText("A TAB ITEM");
		
		engine.applyStyles(shell, true);

		shell.pack();
		shell.open();
		return folderToTest;
	}
	
	public void testBackgroundColor() throws Exception {
		CTabFolder folderToTest = createTestCTabFolder("CTabFolder { background-color: #0000FF }");
		assertEquals(BLUE, folderToTest.getBackground().getRGB());
		folderToTest.getShell().close();
	}

	public void testTextColor() throws Exception {
		CTabFolder folderToTest = createTestCTabFolder("CTabFolder { color: #0000FF }");
		assertEquals(BLUE, folderToTest.getForeground().getRGB());
		folderToTest.getShell().close();
	}

	public void testGradientColor() throws Exception {
		CTabFolder folderToTest = createTestCTabFolder("CTabFolder { background-color: #FF0000  #0000FF }");
		assertEquals(BLUE, folderToTest.getSelectionBackground());
		folderToTest.getShell().close();
	}

	//test for :selected
}
