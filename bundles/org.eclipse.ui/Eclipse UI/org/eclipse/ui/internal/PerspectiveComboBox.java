package org.eclipse.ui.internal;

import java.util.ArrayList;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.action.ControlContribution;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.IPerspectiveListener;

public class PerspectiveComboBox extends ControlContribution
	implements IPropertyListener
{

	private IWorkbenchWindow window;	
	private Combo combo;
	private PerspectiveHistory history;
	
	private IPerspectiveListener perspListener = new IPerspectiveListener() {
		public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
			fill();
		}
		public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, String changeId) {
			fill();
		}
	};
	
	private IPageListener pageListener = new IPageListener() {
		public void pageActivated(IWorkbenchPage page) {
			fill();
		}
		public void pageClosed(IWorkbenchPage page) {
			fill();
		}
		public void pageOpened(IWorkbenchPage page) {
			fill();
		}
	};

	/**
	 * Creates a control contribution item with the given id.
	 *
	 * @param id the contribution item id
	 */
	protected PerspectiveComboBox(IWorkbenchWindow window) {
		super("X");
		this.window = window;
		window.addPerspectiveListener(perspListener);
		window.addPageListener(pageListener);
		history = ((Workbench)(window.getWorkbench())).getPerspectiveHistory();
		history.addListener(this);
	}	
	
	/**
	 * Creates and returns the control for this contribution item
	 * under the given parent composite.
	 * <p>
	 * This framework method must be implemented by concrete
	 * subclasses.
	 * </p>
	 *
	 * @param parent the parent composite
	 * @return the new control
	 */
	protected Control createControl(Composite parent) {
		combo = new Combo(parent, SWT.READ_ONLY);
		fill();
		combo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleSelection();
			}
		});
		combo.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				history.removeListener(PerspectiveComboBox.this);
			}
		});
		return combo;
	}
	
	/*
	 * Fill the perspective combo.
	 */
	private void fill() {
		// Lifecycle check.
		if (combo == null)
			return;
			
		// Empty combo box.
		combo.removeAll();
		
		// Get active perspective.
		IWorkbenchPage page = window.getActivePage();
		if (page == null)
			return;
		IPerspectiveDescriptor activePersp = page.getPerspective();
			
		// Fill the combo box.
		ArrayList perspArray = getMruShortcuts();
		for (int nX = 0; nX < perspArray.size(); nX ++) {
			IPerspectiveDescriptor persp = (IPerspectiveDescriptor)perspArray.get(nX);
			combo.add(persp.getLabel());	
		}
		
		// Update selection.
		int index = combo.indexOf(activePersp.getLabel());
		if (index >= 0)
			combo.select(index);
	}
	
	/*
	 * Select a particular perspective.
	 */
	private void handleSelection() {
		int x = combo.getSelectionIndex();
		if (x >= 0) {
			String str = combo.getItem(x);
			IPerspectiveRegistry reg = window.getWorkbench().getPerspectiveRegistry();
			IPerspectiveDescriptor desc = reg.findPerspectiveWithLabel(str);
			if (desc != null)
				handleSelection(desc);
		}
	}
	
	/*
	 * Select a particular perspective.
	 */
	private void handleSelection(IPerspectiveDescriptor desc) {
		IWorkbenchPage page = window.getActivePage();
		if (page != null) {
			page.setPerspective(desc);
		} else {
			try {
				window.openPage(desc.getId(), ResourcesPlugin.getWorkspace().getRoot());
			} catch (WorkbenchException e) {
			}
		}
	}
	
	/* (non-Javadoc)
	 * Returns the MRU shortcut perspectives.
	 *
	 * The shortcut list is formed from the global perspective history
	 * in the workbench.
	 */
	private ArrayList getMruShortcuts() 
	{
		Workbench wb = (Workbench)window.getWorkbench();
		return wb.getPerspectiveHistory().getItems();
	}
	
	/*
	 * @see IPropertyListener#propertyChanged(Object, int)
	 */
	public void propertyChanged(Object source, int propId) {
		if (source == history)
			fill();
	}

}

