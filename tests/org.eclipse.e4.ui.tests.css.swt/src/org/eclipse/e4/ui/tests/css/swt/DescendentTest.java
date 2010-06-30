package org.eclipse.e4.ui.tests.css.swt;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.dom.WidgetElement;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class DescendentTest extends CSSSWTTestCase {
	
	static final RGB RED = new RGB(255, 0, 0);
	static final RGB GREEN = new RGB(0, 255, 0);
	static final RGB BLUE = new RGB(0, 0, 255);
	static final RGB WHITE = new RGB(255, 255, 255);
	static final RGB BLACK = new RGB(0, 0, 0);
	static public CSSEngine engine;
	
	protected Button[] createTestWidgets(String styleSheet) {

		Display display = Display.getDefault();
		engine = createEngine(styleSheet, display);

		// Create widgets
		Shell shell = new Shell(display, SWT.SHELL_TRIM);
		FillLayout layout = new FillLayout();
		shell.setLayout(layout);
		Composite panelA = new Composite(shell, SWT.NONE);
		panelA.setLayout(new FillLayout());
		Composite panelB = new Composite(shell, SWT.NONE);
		panelB.setLayout(new FillLayout());

		Button buttonA = new Button(panelA, SWT.NONE);
		Button buttonB = new Button(panelB, SWT.NONE);
		Button buttonC = new Button(shell, SWT.NONE);

		engine.applyStyles(shell, true);
		return new Button[] {buttonA, buttonB, buttonC};
	}
	
	public void testDescendentSpecificity() throws Exception {
		Button[] buttons = createTestWidgets(
				"Composite.special Button { background: #FF0000}\n" +  //specificity a=1 b=0 c=1 = 101
				"Composite Button { background: #00FF00}\n" + //specificity a=0 b=0 c=2 = 002
				"Composite.extraordinary Button { background: #FFFFFF}\n" + //specificity a=0 b=0 c=2 = 002
				"#parent Button { background: #000000}\n" + //specificity a=0 b=0 c=2 = 002
				"Button { background: #0000FF}");  //specificity a=0 b=0 c=1 = 001
				
		Button buttonA = buttons[0];
		Button buttonB = buttons[1];
		Button buttonC = buttons[2];
		
		WidgetElement.setCSSClass(buttonA.getParent(), "special");
		engine.applyStyles(buttonA.getShell(), true);
		
		assertEquals(RED, buttonA.getBackground().getRGB());
		assertEquals(GREEN, buttonB.getBackground().getRGB());
		assertEquals(BLUE, buttonC.getBackground().getRGB());
		
		WidgetElement.setCSSClass(buttonA.getParent(), "extraordinary");
		WidgetElement.setID(buttonB.getParent(), "parent");
		
		engine.applyStyles(buttonA.getShell(), true);
		assertEquals(WHITE, buttonA.getBackground().getRGB());
		assertEquals(BLACK, buttonB.getBackground().getRGB());
	}
}
