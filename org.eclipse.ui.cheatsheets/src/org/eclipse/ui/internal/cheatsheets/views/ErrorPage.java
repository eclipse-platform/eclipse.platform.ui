/*
 * Created on Mar 21, 2004
 *
 * @todo To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.eclipse.ui.internal.cheatsheets.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.cheatsheets.*;

/**
 * @author lparsons
 *
 * @todo To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ErrorPage extends Page {

	public ErrorPage() {
		super();
	}

	public void createPart(Composite parent) {
		super.createPart(parent);
	}

	protected void createInfoArea(Composite parent) {
		super.createInfoArea(parent);

		String errorString = CheatSheetPlugin.getResourceString(ICheatSheetResource.ERROR_PAGE_MESSAGE); 
		StyledText st = new StyledText(infoArea, SWT.WRAP | SWT.READ_ONLY | SWT.NULL);
		st.setText(errorString);
		
		GridData bgridData = new GridData();
		bgridData.verticalAlignment = GridData.BEGINNING;
		bgridData.horizontalAlignment = GridData.FILL;
		bgridData.grabExcessHorizontalSpace = true;
		st.setLayoutData(bgridData);
		st.setEnabled(false);

		// Adjust the scrollbar increments
		scrolledComposite.getHorizontalBar().setIncrement(HORZ_SCROLL_INCREMENT);
		scrolledComposite.getVerticalBar().setIncrement(VERT_SCROLL_INCREMENT);

		//		Point newTitleSize = infoArea.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		int workbenchWindowWidth = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().getBounds().width;
		final int minWidth = workbenchWindowWidth >> 2;
		// from the computeSize(SWT.DEFAULT, SWT.DEFAULT) of all the 
		// children in infoArea excluding the wrapped styled text 
		// There is no easy way to do this.
		final boolean[] inresize = new boolean[1];
		// flag to stop unneccesary recursion
		infoArea.addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				if (inresize[0])
					return;
				inresize[0] = true;
				// Refresh problems are fixed if the following is runs twice
				for (int i = 0; i < 2; ++i) {
					// required because of bugzilla report 4579
					infoArea.layout(true);
					// required because you want to change the height that the 
					// scrollbar will scroll over when the width changes.
					int width = infoArea.getClientArea().width;
					Point p = infoArea.computeSize(width, SWT.DEFAULT);
					scrolledComposite.setMinSize(minWidth, p.y);
					inresize[0] = false;
				}
			}
		});

		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);
		Point p = infoArea.computeSize(minWidth, SWT.DEFAULT);
		infoArea.setSize(p.x, p.y);

		scrolledComposite.setMinWidth(minWidth);
		scrolledComposite.setMinHeight(p.y);
		//bug 20094	

		scrolledComposite.setContent(infoArea);
//			hascontent = true;
			
		}
	/**
	 * Creates the cheatsheet's title areawhich will consists
	 * of a title and image.
	 *
	 * @param parent the SWT parent for the title area composite
	 */
	protected String getTitle() {
		return CheatSheetPlugin.getResourceString(ICheatSheetResource.ERROR_LOADING_CHEATSHEET_CONTENT);
	}
}
