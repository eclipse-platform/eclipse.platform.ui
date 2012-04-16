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
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class SplitFeedbackOverlay {
	final Display display = Display.getCurrent();

	private boolean outerDrop = false;
	private int curSide = 0;
	private Shell feedbackShell;
	private List<Rectangle> rects = new ArrayList<Rectangle>();
	private Label msgBox;
	private Rectangle caRect;

	private float ratio;

	private boolean onEdge;

	public SplitFeedbackOverlay(Shell dragShell, Rectangle rect, int side, boolean onEdge,
			float pct, boolean ctrlPressed) {
		caRect = rect;
		curSide = side;
		ratio = pct;
		this.onEdge = onEdge;

		feedbackShell = new Shell(dragShell, SWT.NO_TRIM);
		feedbackShell.setBounds(dragShell.getBounds());

		if (onEdge)
			msgBox = createMessageBox(feedbackShell, rect);

		// Show the appropriate feedback rectangles
		setOuterDrop(ctrlPressed);

		defineRegion();
		feedbackShell.setVisible(true);
	}

	public void dispose() {
		if (feedbackShell != null && !feedbackShell.isDisposed())
			feedbackShell.dispose();
		msgBox = null;
	}

	private void showRects(Rectangle rect, int side, boolean enclosed, float ratio) {
		if (side == 0)
			return;

		Rectangle ca = new Rectangle(rect.x, rect.y, rect.width, rect.height);
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
		if (side == SWT.LEFT) {
			r1 = new Rectangle(ca.x, ca.y, pctWidth, ca.height);
			r2 = new Rectangle(ca.x + r1.width + 2, ca.y, ca.width - (pctWidth + 2), ca.height);
		} else if (side == SWT.RIGHT) {
			r1 = new Rectangle(ca.x, ca.y, ca.width - pctWidth, ca.height);
			r2 = new Rectangle(ca.x + r1.width + 2, ca.y, pctWidth - 2, ca.height);
		} else if (side == SWT.TOP) {
			r1 = new Rectangle(ca.x, ca.y, ca.width, pctHeight);
			r2 = new Rectangle(ca.x, ca.y + pctHeight + 2, ca.width, ca.height - (pctHeight + 2));
		} else if (side == SWT.BOTTOM) {
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

		if (msgBox != null && !msgBox.isDisposed()) {
			rgn.add(msgBox.getBounds());
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

	private Label createMessageBox(final Shell shell, Rectangle bounds) {
		Label msgArea = new Label(shell, SWT.BORDER | SWT.CENTER);
		msgArea.setText("Press 'Ctrl' to drop outside the perspective");

		Point msgSize = msgArea.computeSize(-1, -1);
		msgSize.x += 20;
		msgArea.setSize(msgSize);

		Point dsCenter = new Point(bounds.x + (bounds.width / 2), bounds.y + (bounds.height / 2));

		Rectangle msgBounds = new Rectangle(dsCenter.x - (msgSize.x / 2), dsCenter.y
				- (msgSize.y / 2), msgSize.x, msgSize.y);
		msgBounds = Display.getCurrent().map(null, feedbackShell, msgBounds);
		msgArea.setBounds(msgBounds);

		return msgArea;
	}

	public void setOuterDrop(boolean newVal) {
		outerDrop = newVal;

		if (outerDrop && onEdge) {
			if (msgBox != null && !msgBox.isDisposed()) {
				msgBox.setBackground(msgBox.getDisplay().getSystemColor(SWT.COLOR_DARK_YELLOW));
				msgBox.setForeground(msgBox.getDisplay().getSystemColor(SWT.COLOR_WHITE));
			}

			feedbackShell.setBackground(display.getSystemColor(SWT.COLOR_DARK_YELLOW));
		} else {
			if (msgBox != null && !msgBox.isDisposed()) {
				msgBox.setBackground(msgBox.getDisplay().getSystemColor(SWT.COLOR_DARK_GREEN));
				msgBox.setForeground(msgBox.getDisplay().getSystemColor(SWT.COLOR_WHITE));
			}

			feedbackShell.setBackground(display.getSystemColor(SWT.COLOR_GREEN));
		}

		showRects(caRect, curSide, !(outerDrop && onEdge), ratio);
		defineRegion();
		feedbackShell.update();
	}

	public boolean getOuterDock() {
		return outerDrop;
	}
}
