package org.eclipse.e4.ui.tests.css.swt;

import java.io.IOException;
import java.io.StringReader;

import junit.framework.TestCase;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.engine.CSSSWTEngineImpl;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class LabelTest extends TestCase {

	static final RGB RED = new RGB(255, 0, 0);
	static final RGB GREEN = new RGB(0, 255, 0);
	static final RGB BLUE = new RGB(0, 0, 255);
		
	protected Label createTestLabel(String styleSheet) {
		Display display = Display.getDefault();
		CSSEngine engine = new CSSSWTEngineImpl(display);
		
		try {
			engine.parseStyleSheet(new StringReader(styleSheet));
		} catch (IOException e) {
			fail(e.getMessage());
		}

		// Create widgets
		Shell shell = new Shell(display, SWT.SHELL_TRIM);
		FillLayout layout = new FillLayout();
		shell.setLayout(layout);

		Composite panel = new Composite(shell, SWT.NONE);
		panel.setLayout(new FillLayout());

		Label label = new Label(panel, SWT.NONE);
		label.setText("Label");

		// Apply styles
		engine.applyStyles(shell, true);

		shell.pack();
		shell.open();
		return label;
	}
	
	public void testWidgetClass() throws Exception {
		Label label = createTestLabel("Label { background-color: #FF0000 }");
		assertEquals(RED, label.getBackground().getRGB());
	}

}
