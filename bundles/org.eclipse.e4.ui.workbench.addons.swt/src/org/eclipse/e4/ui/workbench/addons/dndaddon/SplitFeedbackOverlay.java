package org.eclipse.e4.ui.workbench.addons.dndaddon;

/*
 * Monitor example snippet: center a shell on the primary monitor
 *
 * For a list of all SWT example snippets see
 * http://www.eclipse.org/swt/snippets/
 * 
 * @since 3.0
 */
import java.util.ArrayList;
import java.util.List;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class SplitFeedbackOverlay {
	final Display display = Display.getCurrent();

	private Shell feedbackShell;
	private int curSide = 0;
	private float ratio;

	private List<Rectangle> rects = new ArrayList<Rectangle>();
	private Rectangle outerRect;

	public SplitFeedbackOverlay(Shell dragShell, Rectangle rect, int side, float pct,
			boolean enclosed, boolean modified) {
		outerRect = rect;
		curSide = side;
		ratio = pct;

		feedbackShell = new Shell(dragShell, SWT.NO_TRIM);
		feedbackShell.setBounds(dragShell.getBounds());

		// Show the appropriate feedback rectangles
		setFeedback(enclosed, modified);

		defineRegion();
		feedbackShell.setVisible(true);
	}

	public void dispose() {
		if (feedbackShell != null && !feedbackShell.isDisposed()) {
			Region region = feedbackShell.getRegion();
			if (region != null && !region.isDisposed())
				region.dispose();
			feedbackShell.dispose();
		}
	}

	private void showRects(boolean enclosed) {
		if (curSide == 0)
			return;

		Rectangle ca = new Rectangle(outerRect.x, outerRect.y, outerRect.width, outerRect.height);
		rects.clear();

		if (enclosed) {
			addRect(ca);
			ca.x += 4;
			ca.y += 4;
			ca.width -= 8;
			ca.height -= 8;
		}

		int pctWidth = (int) (ca.width * ratio);
		int pctHeight = (int) (ca.height * ratio);

		Rectangle r1 = null, r2 = null;
		if (curSide == SWT.LEFT) {
			r1 = new Rectangle(ca.x, ca.y, pctWidth, ca.height);
			r2 = new Rectangle(ca.x + r1.width + 2, ca.y, ca.width - (pctWidth + 2), ca.height);
		} else if (curSide == SWT.RIGHT) {
			r1 = new Rectangle(ca.x, ca.y, ca.width - pctWidth, ca.height);
			r2 = new Rectangle(ca.x + r1.width + 2, ca.y, pctWidth - 2, ca.height);
		} else if (curSide == SWT.TOP) {
			r1 = new Rectangle(ca.x, ca.y, ca.width, pctHeight);
			r2 = new Rectangle(ca.x, ca.y + pctHeight + 2, ca.width, ca.height - (pctHeight + 2));
		} else if (curSide == SWT.BOTTOM) {
			r1 = new Rectangle(ca.x, ca.y, ca.width, ca.height - pctHeight);
			r2 = new Rectangle(ca.x, ca.y + r1.height + 2, ca.width, pctHeight - 2);
		}

		addRect(r1);
		addRect(r2);
	}

	private void defineRegion() {
		Region rgn = new Region();
		for (Rectangle r : rects) {
			rgn.add(r);
			rgn.subtract(r.x + 2, r.y + 2, r.width - 4, r.height - 4);
		}

		if (feedbackShell.getRegion() != null && !feedbackShell.getRegion().isDisposed())
			feedbackShell.getRegion().dispose();
		feedbackShell.setRegion(rgn);

		feedbackShell.redraw();
		display.update();
	}

	private void addRect(Rectangle rect) {
		// Map the rect to the feedback shell
		rect = display.map(null, feedbackShell, rect);
		rects.add(rect);
	}

	public void setFeedback(boolean enclosed, boolean modified) {
		if (!modified)
			feedbackShell.setBackground(display.getSystemColor(SWT.COLOR_GREEN));
		else
			feedbackShell.setBackground(display.getSystemColor(SWT.COLOR_DARK_YELLOW));

		showRects(enclosed);
		defineRegion();
		feedbackShell.update();
	}
}
