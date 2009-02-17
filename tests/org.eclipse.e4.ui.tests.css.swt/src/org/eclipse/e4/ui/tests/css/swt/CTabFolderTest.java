package org.eclipse.e4.ui.tests.css.swt;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class CTabFolderTest extends CSSTestCase {

	static final RGB RED = new RGB(255, 0, 0);
	static final RGB GREEN = new RGB(0, 255, 0);
	static final RGB BLUE = new RGB(0, 0, 255);
	static final RGB WHITE = new RGB(255, 255, 255);

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

//	public void testGradientColor() throws Exception {
//		CTabFolder folderToTest = createTestCTabFolder("CTabFolder { background-color: #FF0000  #0000FF }");
//		assertEquals(BLUE, folderToTest.getSelectionBackground());
//		folderToTest.getShell().close();
//	}

	public void testSelectedPseudo() throws Exception {
		CTabFolder folderToTest = createTestCTabFolder(
				"CTabFolder { color: #FFFFFF; background-color: #0000FF }\n" +
				"CTabFolder:selected { color: #FF0000;  background-color: #00FF00 }");
		assertEquals(WHITE, folderToTest.getForeground().getRGB());
		assertEquals(BLUE, folderToTest.getBackground().getRGB());
		assertEquals(RED, folderToTest.getSelectionForeground().getRGB());
		assertEquals(GREEN, folderToTest.getSelectionBackground().getRGB());
		folderToTest.getShell().close();
	}
		
	public void testFontRegular() throws Exception {
		CTabFolder folderToTest = createTestCTabFolder("CTabFolder { font: Verdana 16px }");
		assertEquals(1, folderToTest.getFont().getFontData().length);
		FontData fontData = folderToTest.getFont().getFontData()[0];
		assertEquals("Verdana", fontData.getName());
		assertEquals(16, fontData.getHeight());
		assertEquals(SWT.NORMAL, fontData.getStyle());		
		folderToTest.getShell().close();
	}

	public void testFontBold() throws Exception {
		CTabFolder folderToTest = createTestCTabFolder("CTabFolder { font: Arial 12px; font-weight: bold }");
		assertEquals(1, folderToTest.getFont().getFontData().length);
		FontData fontData = folderToTest.getFont().getFontData()[0];
		assertEquals("Arial", fontData.getName());
		assertEquals(12, fontData.getHeight());
		assertEquals(SWT.BOLD, fontData.getStyle());		
		folderToTest.getShell().close();
	}

	public void testFontItalic() throws Exception {
		CTabFolder folderToTest = createTestCTabFolder("CTabFolder { font: Arial 12px; font-style: italic }");
		assertEquals(1, folderToTest.getFont().getFontData().length);
		FontData fontData = folderToTest.getFont().getFontData()[0];
		assertEquals("Arial", fontData.getName());
		assertEquals(12, fontData.getHeight());
		assertEquals(SWT.ITALIC, fontData.getStyle());		
		folderToTest.getShell().close();
	}

	public void testBorderVisible() throws Exception {
		CTabFolder folderToTest = createTestCTabFolder("CTabFolder { borderVisible: true}");
		assertEquals(true, folderToTest.getBorderVisible());
		folderToTest.getShell().close();
		folderToTest = createTestCTabFolder("CTabFolder { borderVisible: false}");
		assertEquals(false, folderToTest.getBorderVisible());
		folderToTest.getShell().close();
	}
	
	public void testSimple() throws Exception {
		CTabFolder folderToTest = createTestCTabFolder("CTabFolder { simple: true}");
		assertEquals(true, folderToTest.getSimple());
		folderToTest.getShell().close();
		folderToTest = createTestCTabFolder("CTabFolder { simple: false}");
		assertEquals(false, folderToTest.getSimple());
		folderToTest.getShell().close();
	}
	
	public void testMaximizeVisible() throws Exception {
		CTabFolder folderToTest = createTestCTabFolder("CTabFolder { maximizeVisible: true}");
		assertEquals(true, folderToTest.getMaximizeVisible());
		folderToTest.getShell().close();
		folderToTest = createTestCTabFolder("CTabFolder { maximizeVisible: false}");
		assertEquals(false, folderToTest.getMaximizeVisible());
		folderToTest.getShell().close();
	}
	
	public void testMinimizeVisible() throws Exception {
		CTabFolder folderToTest = createTestCTabFolder("CTabFolder { minimizeVisible: true}");
		assertEquals(true, folderToTest.getMinimizeVisible());
		folderToTest.getShell().close();
		folderToTest = createTestCTabFolder("CTabFolder { minimizeVisible: false}");
		assertEquals(false, folderToTest.getMinimizeVisible());
		folderToTest.getShell().close();
	}
	
	public void testMRUVisible() throws Exception {
		CTabFolder folderToTest = createTestCTabFolder("CTabFolder { mruVisible: true}");
		assertEquals(true, folderToTest.getMRUVisible());
		folderToTest.getShell().close();
		folderToTest = createTestCTabFolder("CTabFolder { mruVisible: false}");
		assertEquals(false, folderToTest.getMRUVisible());
		folderToTest.getShell().close();
	}
	
	public void testMaximized() throws Exception {
		CTabFolder folderToTest = createTestCTabFolder("CTabFolder { maximized: true}");
		assertEquals(true, folderToTest.getMaximized());
		folderToTest.getShell().close();
		folderToTest = createTestCTabFolder("CTabFolder { maximized: false}");
		assertEquals(false, folderToTest.getMaximized());
		folderToTest.getShell().close();
	}
	
	public void testMinimized() throws Exception {
		CTabFolder folderToTest = createTestCTabFolder("CTabFolder { minimized: true}");
		assertEquals(true, folderToTest.getMinimized());
		folderToTest.getShell().close();
		folderToTest = createTestCTabFolder("CTabFolder { minimized: false}");
		assertEquals(false, folderToTest.getMinimized());
		folderToTest.getShell().close();
	}
	
	public void testSingle() throws Exception {
		CTabFolder folderToTest = createTestCTabFolder("CTabFolder { single: true}");
		assertEquals(true, folderToTest.getSingle());
		folderToTest.getShell().close();
		folderToTest = createTestCTabFolder("CTabFolder { single: false}");
		assertEquals(false, folderToTest.getSingle());
		folderToTest.getShell().close();
	}
	
	public void testUnselectedCloseVisible() throws Exception {
		CTabFolder folderToTest = createTestCTabFolder("CTabFolder { unselectedCloseVisible: true}");
		assertEquals(true, folderToTest.getUnselectedCloseVisible());
		folderToTest.getShell().close();
		folderToTest = createTestCTabFolder("CTabFolder { unselectedCloseVisible: false}");
		assertEquals(false, folderToTest.getUnselectedCloseVisible());
		folderToTest.getShell().close();
	}
	
	public void testUnselectedImageVisible() throws Exception {
		CTabFolder folderToTest = createTestCTabFolder("CTabFolder { unselectedImageVisible: true}");
		assertEquals(true, folderToTest.getUnselectedImageVisible());
		folderToTest.getShell().close();
		folderToTest = createTestCTabFolder("CTabFolder { unselectedImageVisible: false}");
		assertEquals(false, folderToTest.getUnselectedImageVisible());
		folderToTest.getShell().close();
	}
}
