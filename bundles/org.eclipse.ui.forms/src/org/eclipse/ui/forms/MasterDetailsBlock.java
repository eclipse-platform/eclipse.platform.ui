/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.forms;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

/**
 * This class implements the 'master/details' UI pattern suitable for inclusion
 * in a form. The block consists of two parts: 'master' and 'details' in a sash
 * form that allows users to change the relative ratio on the page. The master
 * part needs to be created by the users of this class. The details part is
 * created by the block.
 * <p>
 * The master part is responsible for adding itself as a form part and firing
 * selection events. The details part catches the selection events and tries to
 * load a page registered to handle the selected object(s). The page shows the
 * details of the selected object(s) and allows users to edit them.
 * <p>
 * Details pages can be registered statically using 'registerPage' or
 * dynamically through the use of 'IDetailsPageProvider' in case where different
 * pages need to be shown for objects of the same type depending on their state.
 * <p>
 * Subclasses are required to implement abstract methods of this class. Master
 * part must be created and at least one details page should be registered in
 * order to show details of the objects selected in the master part. Tool bar
 * actions can be optionally added to the tool bar manager.
 * 
 * @see DetailsPart
 * @see IDetailsPage
 * @see IDetailsPageProvider
 * @since 3.0
 */
public abstract class MasterDetailsBlock {
	/**
	 * Details part created by the block. No attempt should be made to access
	 * this field inside <code>createMasterPart</code> because it has not been
	 * created yet and will be <code>null</code>.
	 */
	protected DetailsPart detailsPart;

	/**
	 * The form that is the parent of both master and details part. The form
	 * allows users to change the ratio between the two parts.
	 */
	protected SashForm sashForm;
	
	static final int DRAGGER_SIZE = 40;
	
	class MDSashForm extends SashForm {
		ArrayList sashes = new ArrayList();
		Listener listener = new Listener () {
			public void handleEvent(Event e) {
				switch (e.type) {
				case SWT.MouseEnter:
					e.widget.setData("hover", Boolean.TRUE); //$NON-NLS-1$
					((Control)e.widget).redraw();
					break;
				case SWT.MouseExit:
					e.widget.setData("hover", null); //$NON-NLS-1$
					((Control)e.widget).redraw();
					break;
				case SWT.Paint:
					onSashPaint(e);
				break;
				case SWT.Resize:
					hookSashListeners();
				break;
				}
			}
		};
		public MDSashForm(Composite parent, int style) {
			super(parent, style);
		}
		
		public void layout(boolean changed) {
			super.layout(changed);
			hookSashListeners();
		}
		
		public void layout(Control [] children) {
			super.layout(children);
			hookSashListeners();
		}

		private void hookSashListeners() {
			purgeSashes();
			Control [] children = getChildren();
			for (int i=0; i<children.length; i++) {
				if (children[i] instanceof Sash) {
					Sash sash = (Sash)children[i];
					if (sashes.contains(sash))
						continue;
					sash.addListener(SWT.Paint, listener);
					sash.addListener(SWT.MouseEnter, listener);
					sash.addListener(SWT.MouseExit, listener);
					sashes.add(sash);
				}
			}
		}
		private void purgeSashes() {
			for (Iterator iter=sashes.iterator(); iter.hasNext();) {
				Sash sash = (Sash)iter.next();
				if (sash.isDisposed())
					iter.remove();
			}
		}
	}

	/**
	 * Creates the content of the master/details block inside the managed form.
	 * This method should be called as late as possible inside the parent part.
	 * 
	 * @param managedForm
	 *            the managed form to create the block in
	 */
	public void createContent(IManagedForm managedForm) {
		final ScrolledForm form = managedForm.getForm();
		FormToolkit toolkit = managedForm.getToolkit();
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		form.getBody().setLayout(layout);
		sashForm = new MDSashForm(form.getBody(), SWT.NULL);
		sashForm.setData("form", managedForm); //$NON-NLS-1$
		toolkit.adapt(sashForm, false, false);
		sashForm.setMenu(form.getBody().getMenu());
		sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
		createMasterPart(managedForm, sashForm);
		createDetailsPart(managedForm, sashForm);
		hookResizeListener();
		createToolBarActions(managedForm);
		form.updateToolBar();
	}
	
	private void hookResizeListener() {
		Listener listener = ((MDSashForm)sashForm).listener;
		Control [] children = sashForm.getChildren();
		for (int i=0; i<children.length; i++) {
			if (children[i] instanceof Sash) continue;
			children[i].addListener(SWT.Resize, listener);
		}
	}

	/**
	 * Implement this method to create a master part in the provided parent.
	 * Typical master parts are section parts that contain tree or table viewer.
	 * 
	 * @param managedForm
	 *            the parent form
	 * @param parent
	 *            the parent composite
	 */
	protected abstract void createMasterPart(IManagedForm managedForm,
			Composite parent);

	/**
	 * Implement this method to statically register pages for the expected
	 * object types. This mechanism can be used when there is 1-&gt;1 mapping
	 * between object classes and details pages.
	 * 
	 * @param detailsPart
	 *            the details part
	 */
	protected abstract void registerPages(DetailsPart detailsPart);

	/**
	 * Implement this method to create form tool bar actions and add them to the
	 * form tool bar if desired.
	 * 
	 * @param managedForm
	 *            the form that owns the tool bar
	 */
	protected abstract void createToolBarActions(IManagedForm managedForm);

	private void createDetailsPart(final IManagedForm mform, Composite parent) {
		detailsPart = new DetailsPart(mform, parent, SWT.NULL);
		mform.addPart(detailsPart);
		registerPages(detailsPart);
	}
	
	private void onSashPaint(Event e) {
		Sash sash = (Sash)e.widget;
		IManagedForm form = (IManagedForm)sash.getParent().getData("form"); //$NON-NLS-1$
		FormColors colors = form.getToolkit().getColors();
		boolean vertical = (sash.getStyle() & SWT.VERTICAL)!=0;
		GC gc = e.gc;
		Boolean hover = (Boolean)sash.getData("hover"); //$NON-NLS-1$
		gc.setBackground(colors.getColor(IFormColors.TB_BG));
		gc.setForeground(colors.getColor(IFormColors.TB_BORDER));
		Point size = sash.getSize();
		if (vertical) {
			if (hover!=null)
				gc.fillRectangle(0, 0, size.x, size.y);
			//else
				//gc.drawLine(1, 0, 1, size.y-1);
		}
		else {
			if (hover!=null)
				gc.fillRectangle(0, 0, size.x, size.y);
			//else
				//gc.drawLine(0, 1, size.x-1, 1);				
		}
	}
}
