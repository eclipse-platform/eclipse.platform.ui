/*
 * Created on Mar 22, 2004
 *
 * @todo To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.eclipse.ui.internal.cheatsheets.views;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IHelpContextIds;
import org.eclipse.ui.internal.cheatsheets.data.*;
import org.eclipse.ui.internal.cheatsheets.data.Item;

/**
 * @author lparsons
 *
 * @todo To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class CheatSheetPage extends Page {
	//Colors
	private Color darkGrey;
	private Color lightGrey;
	private final RGB darkGreyRGB = new RGB(160, 192, 208);
	private final RGB HIGHLIGHT_RGB = new RGB(230, 230, 230);

	private CheatSheetDomParser parser;
	private ArrayList viewItemList;
	private CheatSheetView cheatSheetView;

	public CheatSheetPage(CheatSheetDomParser parser, ArrayList viewItemList, CheatSheetView cheatSheetView) {
		super();
		this.parser = parser;
		this.viewItemList = viewItemList;
		this.cheatSheetView = cheatSheetView;
	}

	public void createPart(Composite parent) {
		super.createPart(parent);

		WorkbenchHelp.setHelp(cheatSheetComposite, IHelpContextIds.WELCOME_EDITOR);

//		checkDynamicModel();
	}

	/**
	 * Creates the main composite area of the view.
	 *
	 * @param parent the SWT parent for the title area composite
	 * @return the created info area composite
	 */
	protected void createInfoArea(Composite parent) {
		super.createInfoArea(parent);
	
		IntroItem myintro = new IntroItem(form.getBody(), parser.getIntroItem(), darkGrey, cheatSheetView);
	
		myintro.setItemColor(myintro.lightGrey);
		myintro.boldTitle();
		viewItemList.add(myintro);
			
		//Get the content info from the parser.  This makes up all items except the intro item.
		ArrayList items = parser.getItems();

		int switcher = 0;
	
		for (int i = 0; i < items.size(); i++) {
			if (switcher == 0) {
				if (items.get(i) instanceof ItemWithSubItems) {
					CoreItem ciws = new CoreItem(form.getBody(), (ItemWithSubItems) items.get(i), backgroundColor, cheatSheetView);
					viewItemList.add(ciws);
				} else {
					CoreItem mycore = new CoreItem(form.getBody(), (Item) items.get(i), backgroundColor, cheatSheetView);
					viewItemList.add(mycore);
				}
				switcher = 1;
			} else {
				if (items.get(i) instanceof ItemWithSubItems) {
					CoreItem ciws = new CoreItem(form.getBody(), (ItemWithSubItems) items.get(i), lightGrey, cheatSheetView);
					viewItemList.add(ciws);
				} else {
					CoreItem mycore = new CoreItem(form.getBody(), (Item) items.get(i), lightGrey, cheatSheetView);
					viewItemList.add(mycore);
				}
				switcher = 0;
			}
		}
	
//		// Adjust the scrollbar increments
//		GC gc = new GC(myintro.getMainItemComposite());
//		int increment = gc.getFontMetrics().getAverageCharWidth();
//		gc.dispose();
//		scrolledComposite.getHorizontalBar().setIncrement(increment);
//		scrolledComposite.getVerticalBar().setIncrement(myintro.getMainItemComposite().getLocation().y);
//	
//		//		Point newTitleSize = infoArea.computeSize(SWT.DEFAULT, SWT.DEFAULT);
//		int workbenchWindowWidth = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().getBounds().width;
//	
//		final int minWidth = workbenchWindowWidth >> 2;
//		// from the computeSize(SWT.DEFAULT, SWT.DEFAULT) of all the 
//		// children in infoArea excluding the wrapped styled text 
//		// There is no easy way to do this.
//		final boolean[] inresize = new boolean[1];
//		// flag to stop unneccesary recursion
//		infoArea.addControlListener(new ControlAdapter() {
//			public void controlResized(ControlEvent e) {
//				if (inresize[0])
//					return;
//				inresize[0] = true;
//				// Refresh problems are fixed if the following is runs twice
//				for (int i = 0; i < 2; ++i) {
//					// required because of bugzilla report 4579
//					infoArea.layout(true);
//					// required because you want to change the height that the 
//					// scrollbar will scroll over when the width changes.
//					int width = infoArea.getClientArea().width;
//					Point p = infoArea.computeSize(width, SWT.DEFAULT);
//					scrolledComposite.setMinSize(minWidth, p.y);
//					inresize[0] = false;
//				}
//			}
//		});
//	
//		scrolledComposite.setExpandHorizontal(true);
//		scrolledComposite.setExpandVertical(true);
//		Point p = infoArea.computeSize(minWidth, SWT.DEFAULT);
//		infoArea.setSize(p.x, p.y);
//	
//		scrolledComposite.setMinWidth(minWidth);
//		scrolledComposite.setMinHeight(p.y);
//		//bug 20094	
//	
//		scrolledComposite.setContent(infoArea);
//		hascontent = true;
	}

	/**
	 * Creates the cheatsheet's title areawhich will consists
	 * of a title and image.
	 *
	 * @param parent the SWT parent for the title area composite
	 */
	protected String getTitle() {
		if(parser != null & parser.getTitle() != null)
			return parser.getTitle();
		else
			return "";//$NON-NLS-1$
	}

	public void dispose() {
		super.dispose();

		if (lightGrey != null)
			lightGrey.dispose();

		if (darkGrey != null)
			darkGrey.dispose();
	}

	protected void init(Display display) {
		super.init(display);

		lightGrey = new Color(display, HIGHLIGHT_RGB);
		darkGrey = new Color(display, darkGreyRGB);
	}
}
