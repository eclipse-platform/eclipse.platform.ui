/*
 * Created on Nov 24, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.eclipse.ui.internal.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.CTabFolder2;
import org.eclipse.swt.custom.CTabItem2;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import org.eclipse.jface.window.ColorSchemeService;

/**
 * @author MVM
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ColorThemeDemo {

	Composite sampleComposite;
	CTabFolder2 sampleTabFolder; 
	CLabel sampleClabel;
		
	/**
	 * @param parent
	 * @param style
	 */
	public ColorThemeDemo(Composite parent) {
		createControl(parent);		
	}

	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display, SWT.SHELL_TRIM);
		GridLayout grid = new GridLayout();
		shell.setLayout(grid);
		
		Composite c = new Composite(shell, SWT.NONE);
		GridLayout gl = new GridLayout();
		c.setLayout(gl);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		c.setData(gd);
		new ColorThemeDemo(c);
		shell.setVisible(true);
		
		shell.pack();
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}
	/**
	 * @param parent
	 * @param style
	 */
	private void createControl(Composite parent) {
		Composite marginComposite = new Composite(parent, SWT.NONE);
		GridLayout gl = new GridLayout();
		gl.marginHeight = 1;
		gl.marginWidth = 1;
		marginComposite.setBackground(new Color(parent.getDisplay(), 0,0,0));
		marginComposite.setLayout(gl);
		GridData gd = new GridData(GridData.FILL_BOTH);
		
		sampleComposite = new Composite(marginComposite, SWT.H_SCROLL | SWT.V_SCROLL);
//		parent.setBackground(new Color(parent.getDisplay(), 44, 255, 44));
//		parent.getParent().setBackground(new Color(parent.getDisplay(), 44, 44, 255));
		GridLayout gl2 = new GridLayout();
		gl2.marginHeight = 0;
		gl2.marginWidth = 0;
		sampleComposite.setLayout(gl2);
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gd.grabExcessHorizontalSpace = true;
		sampleComposite.setData(gridData);
			
		sampleTabFolder = new CTabFolder2(sampleComposite, SWT.BORDER);
		sampleTabFolder.setData(new GridData(GridData.FILL_BOTH));
		CTabItem2 temp = new CTabItem2(sampleTabFolder, SWT.NONE);
		temp.setText("Console");
		Text text = new Text(sampleTabFolder, SWT.MULTI);
		text.setText("Configuring your perspectives \n You can move views and editors around the workbench by dragging their titlebars. You can \nalso add more views to your current perspective by using Window > Show View.\nTo reset the perspective to its original state, choose Window > Reset Perspective.\nOnce you have arranged your perspective, you can save it using Window > Save Perspective As....\nYou can customize the views, perspectives and New menu operations that show up for\nyour perspective. To do this choose Window > Customize Perspective... .  A view can\nadditionally be converted into a Fast View by dragging it to the shortcut bar (at the\nfar left of the window).");
		temp.setControl(text);
		sampleTabFolder.setSelection(0);
		temp = new CTabItem2(sampleTabFolder, SWT.NONE);
		temp.setText("Search");

		CLabel clabel = new CLabel(sampleComposite, SWT.NONE);
		clabel.setText("Status Text");
		clabel.setData(new GridData(GridData.FILL_HORIZONTAL));
		ColorSchemeService.setStatusColors(clabel);
		
		resetColors();
	}

	/**
	 * 
	 */
	public void resetColors() {
		ColorSchemeService.setTabColors(sampleTabFolder);
	}

	/**
	 * Redraw the receiver.
	 */
	void redraw() {
		sampleTabFolder.redraw();
	}
	
	/**
	 * @param color
	 */
	public void setTabSelectionBGColor(Color color) {
		sampleTabFolder.setSelectionBackground(color);
	}
	
	/**
	 * @param color
	 */
	public void setTabSelectionFGColor(Color color) {
		sampleTabFolder.setSelectionForeground(color);
	}
	
	/**
	 * @param color
	 */
	public void setTabBGColor(Color color) {
		sampleTabFolder.setBackground(color);	
	}

	/**
	 * @param color
	 */
	public void setTabFGColor(Color color) {
		sampleTabFolder.setForeground(color);	
	}
	

}
