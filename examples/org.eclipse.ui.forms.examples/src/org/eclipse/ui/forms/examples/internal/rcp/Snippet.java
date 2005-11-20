package org.eclipse.ui.forms.examples.internal.rcp;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.GlyphMetrics;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

public class Snippet {
	
	public static void main(String[] args) {
		Display display = new Display();
		final Shell shell = new Shell(display, SWT.SHELL_TRIM | SWT.DOUBLE_BUFFERED);
		shell.setText("Embedding objects in text");
		final Image[] images = {new Image(display, 32, 32), new Image(display, 20, 40), new Image(display, 40, 20)};
		int[] colors  = {SWT.COLOR_BLUE, SWT.COLOR_MAGENTA, SWT.COLOR_GREEN};
		for (int i = 0; i < images.length; i++) {
			GC gc = new GC(images[i]);
			gc.setBackground(display.getSystemColor(colors[i]));
			gc.fillRectangle(images[i].getBounds());
			gc.dispose();
		}
		
		final Button button = new Button(shell, SWT.PUSH);
		button.setText("Button");
		button.pack();
		String text = "Here is some text with a blue image \uFFFC, a magenta image \uFFFC, a green image \uFFFC, and a button: \uFFFC.";
		final int[] imageOffsets = {36, 55, 72};
		final TextLayout layout = new TextLayout(display);
		layout.setText(text);
		for (int i = 0; i < images.length; i++) {
			Rectangle bounds = images[i].getBounds();
			TextStyle imageStyle = new TextStyle(null, null, null);
			imageStyle.metrics = new GlyphMetrics(bounds.height, 0, bounds.width); 
			layout.setStyle(imageStyle, imageOffsets[i], imageOffsets[i]);
		}
		Rectangle bounds = button.getBounds();
		TextStyle buttonStyle = new TextStyle(null, null, null);
		buttonStyle.metrics = new GlyphMetrics(bounds.height, 0, bounds.width); 
		final int buttonOffset = text.length() - 2;
		layout.setStyle(buttonStyle, buttonOffset, buttonOffset);
		
		shell.addListener(SWT.Paint, new Listener() {
			public void handleEvent(Event event) {
				GC gc = event.gc;
				Point margin = new Point(10, 10);
				layout.setWidth(shell.getClientArea().width - 2 * margin.x);
				layout.draw(event.gc, margin.x, margin.y);
				for (int i = 0; i < images.length; i++) {
					int offset = imageOffsets[i];
					int lineIndex = layout.getLineIndex(offset);
					FontMetrics lineMetrics = layout.getLineMetrics(lineIndex);
					Point point = layout.getLocation(offset, false);
					GlyphMetrics glyphMetrics = layout.getStyle(offset).metrics;
					gc.drawImage(images[i], point.x + margin.x, point.y + margin.y + lineMetrics.getAscent() - glyphMetrics.ascent);
				}
				int lineIndex = layout.getLineIndex(buttonOffset);
				FontMetrics lineMetrics = layout.getLineMetrics(lineIndex);
				Point point = layout.getLocation(buttonOffset, false);
				GlyphMetrics glyphMetrics = layout.getStyle(buttonOffset).metrics;
				button.setLocation(point.x + margin.x, point.y + margin.y + lineMetrics.getAscent() - glyphMetrics.ascent);
			}
		});

		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) display.sleep();
		}
		layout.dispose();
		for (int i = 0; i < images.length; i++) {
			images[i].dispose();
		}
		display.dispose();
	}
	}
