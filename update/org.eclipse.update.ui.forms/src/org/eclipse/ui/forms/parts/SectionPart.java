/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.forms.parts;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.*;

public abstract class SectionPart implements IPropertyChangeListener {
	/**
	 * Title style. If used, title will be rendered at the top of the part.
	 */
	public static final int TITLE = 1;
	/**
	 * Separator style. If used, a separator will be rendered below the title.
	 */
	public static final int SEPARATOR = 2;
	/**
	 * Description style. If used, description will be rendered below the
	 * title.
	 */
	public static final int DESCRIPTION = 4;

	/**
	 * Collapsable style. If used, a twistie will be added and section will be
	 * collapsable.
	 */
	public static final int COLLAPSABLE = 8;
	/**
	 * Compact style. If used, preferred width of the collapsed section will be
	 * computed to only fit the title width. Otherwise, client width will also
	 * be considered at all times.
	 */
	public static final int COMPACT = 16;

	public static final int SELECTION = 1;
	private String title;
	private Control client;
	protected Label titleLabel;
	protected Control separator;
	private SectionChangeManager sectionManager;
	private String description;
	private boolean dirty;
	protected Label descriptionLabel;
	private Twistie twistie;
	private boolean readOnly;
	private boolean collapsed = false;
	private Composite control;
	private int style;
	/*
	 * This is a special layout for the section. Both the header and the
	 * description labels will wrap and they will use client's size to
	 * calculate needed height. This kind of behaviour is not possible with
	 * stock grid layout.
	 */
	private class SectionLayout extends Layout implements ILayoutExtension {
		int vspacing = 3;
		int sepHeight = 2;

		public int computeMinimumWidth(Composite parent, boolean flush) {
			return 30;
		}

		public int computeMaximumWidth(Composite parent, boolean flush) {
			int maxWidth = 0;
			if (client != null) {
				if (client instanceof Composite) {
					Layout cl = ((Composite) client).getLayout();
					if (cl instanceof ILayoutExtension)
						maxWidth =
							((ILayoutExtension) cl).computeMaximumWidth(
								(Composite) client,
								flush);
				}
				if (maxWidth == 0) {
					Point csize =
						client.computeSize(SWT.DEFAULT, SWT.DEFAULT, flush);
					maxWidth = csize.x;
				}
			}
			if (hasStyle(TITLE) && titleLabel != null) {
				Point hsize =
					titleLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT, flush);
				maxWidth = Math.max(maxWidth, hsize.x);
			}
			if (hasStyle(DESCRIPTION) && descriptionLabel != null) {
				Point dsize =
					descriptionLabel.computeSize(
						SWT.DEFAULT,
						SWT.DEFAULT,
						flush);
				maxWidth = Math.max(maxWidth, dsize.x);
			}
			return maxWidth;
		}

		protected Point computeSize(
			Composite parent,
			int wHint,
			int hHint,
			boolean flush) {
			int width = 0;
			int height = 0;
			int cwidth = 0;
			int collapsedHeight = 0;

			if (wHint != SWT.DEFAULT)
				width = wHint;
			if (hHint != SWT.DEFAULT)
				height = hHint;

			cwidth = width;

			if (client != null && !client.isDisposed()) {
				if (twistie != null
					&& twistie.getSelection()
					&& hasStyle(COMPACT)) {
				} else {
					//Point csize = client.computeSize(SWT.DEFAULT,
					// SWT.DEFAULT, flush);
					Point csize = client.computeSize(wHint, SWT.DEFAULT);
					if (width == 0) {
						width = csize.x;
						cwidth = width;
					}
					if (height == 0)
						height = csize.y;
				}
			}

			Point toggleSize = null;

			if (hasStyle(COLLAPSABLE) && twistie != null)
				toggleSize =
					twistie.computeSize(SWT.DEFAULT, SWT.DEFAULT, flush);

			if (hHint == SWT.DEFAULT
				&& hasStyle(TITLE)
				&& titleLabel != null) {
				int hwidth = cwidth;
				if (toggleSize != null)
					hwidth = cwidth - toggleSize.x - 5;
				Point hsize =
					titleLabel.computeSize(hwidth, SWT.DEFAULT, flush);
				height += hsize.y;
				collapsedHeight = hsize.y;
				height += vspacing;
			}

			if (hHint == SWT.DEFAULT && hasStyle(SEPARATOR)) {
				height += sepHeight;
				height += vspacing;
				collapsedHeight += vspacing + sepHeight;
			}
			if (hHint == SWT.DEFAULT
				&& hasStyle(DESCRIPTION)
				&& descriptionLabel != null) {
				Point dsize =
					descriptionLabel.computeSize(cwidth, SWT.DEFAULT, flush);
				height += dsize.y;
				height += vspacing;
			}
			if (twistie != null && twistie.getSelection()) {
				// collapsed state
				height = collapsedHeight;
			}
			return new Point(width, height);
		}
		protected void layout(Composite parent, boolean flush) {
			int width = parent.getClientArea().width;
			int height = parent.getClientArea().height;
			int y = 0;
			Point toggleSize = null;

			if (hasStyle(COLLAPSABLE)) {
				toggleSize =
					twistie.computeSize(SWT.DEFAULT, SWT.DEFAULT, flush);
			}
			if (hasStyle(TITLE) && titleLabel != null) {
				Point hsize;

				int availableWidth = width;
				if (toggleSize != null)
					availableWidth = width - toggleSize.x - 5;
				hsize =
					titleLabel.computeSize(availableWidth, SWT.DEFAULT, flush);
				int hx = 0;
				if (twistie != null) {
					int ty = y + hsize.y - toggleSize.y;
					twistie.setBounds(0, ty, toggleSize.x, toggleSize.y);
					hx = toggleSize.x; // + 5;
				}
				titleLabel.setBounds(hx, y, availableWidth, hsize.y);

				y += hsize.y + vspacing;
			}
			if (hasStyle(SEPARATOR) && separator != null) {
				separator.setBounds(0, y, width, 2);
				y += sepHeight + vspacing;
			}
			if (twistie != null && twistie.getSelection()) {
				return;
			}
			if (hasStyle(DESCRIPTION) && descriptionLabel != null) {
				Point dsize =
					descriptionLabel.computeSize(width, SWT.DEFAULT, flush);
				descriptionLabel.setBounds(0, y, width, dsize.y);
				y += dsize.y + vspacing;
			}
			if (client != null) {
				client.setBounds(0, y, width, height - y);
			}
		}
	}
	/**
	 * The default constructor. Section part will be created with a title,
	 * separator and description area.
	 *  
	 */
	public SectionPart() {
		this(TITLE | SEPARATOR | DESCRIPTION);
	}
	/**
	 * Creates a new section part with the combination of styles ORed together.
	 * 
	 * @param style
	 *            One or more of the following styles ORed together: TITLE,
	 *            SEPARATOR, DESCRIPTION, COLLAPSABLE and COMPACT.
	 */
	public SectionPart(int style) {
		JFaceResources.getFontRegistry().addListener(this);
	}

	public void commitChanges(boolean onSave) {
	}

/**
 * Creates the client of the section part. Client is positioned
 * below the title, separator and description and consumes most of
 * the available space.
 * @param parent
 * @param toolkit
 * @return
 */
	protected abstract Composite createClient(
		Composite parent,
		FormToolkit toolkit);

	public final Control createControl(
		Composite parent,
		final FormToolkit toolkit) {
		Composite section = toolkit.createComposite(parent);
		SectionLayout slayout = new SectionLayout();
		section.setLayout(slayout);
		section.setData(this);

		FormColors colors = toolkit.getColors();

		if (hasStyle(TITLE)) {
			Color headerColor = colors.getColor(FormColors.TITLE);
			titleLabel =
				toolkit.createHeadingLabel(
					section,
					getTitle(),
					headerColor,
					SWT.WRAP);
			if (hasStyle(COLLAPSABLE)) {
				twistie = toolkit.createTwistie(section);
				twistie.setSelection(collapsed);
				twistie.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						doToggle();
					}
				});
				titleLabel.addMouseListener(new MouseAdapter() {
					public void mouseDown(MouseEvent e) {
						twistie.setSelection(!twistie.getSelection());
						twistie.redraw();
						doToggle();
					}
				});
				titleLabel.addMouseTrackListener(new MouseTrackAdapter() {
					public void mouseEnter(MouseEvent e) {
						titleLabel.setCursor(FormsResources.getHandCursor());
					}
					public void mouseExit(MouseEvent e) {
						titleLabel.setCursor(null);
					}
				});
			}
		}

		if (hasStyle(SEPARATOR)) {
			separator = toolkit.createCompositeSeparator(section);
		}

		if (hasStyle(DESCRIPTION) && description != null) {
			descriptionLabel =
				toolkit.createLabel(section, description, SWT.WRAP);
		}
		client = createClient(section, toolkit);
		section.setData(this);
		control = section;
		return section;
	}

	private void doToggle() {
		collapsed = twistie.getSelection();
		reflow();
		if (descriptionLabel != null)
			descriptionLabel.setVisible(!collapsed);
		if (client != null)
			client.setVisible(!collapsed);
	}

	protected void reflow() {
		control.setRedraw(false);
		control.getParent().setRedraw(false);
		control.layout(true);
		control.getParent().layout(true);
		control.setRedraw(true);
		control.getParent().setRedraw(true);
	}


	public void dispose() {
		JFaceResources.getFontRegistry().removeListener(this);
	}
	public boolean doGlobalAction(String actionId) {
		return false;
	}
	public void expandTo(Object object) {
	}
	public final void fireChangeNotification(
		int changeType,
		Object changeObject) {
		if (sectionManager == null)
			return;
		sectionManager.dispatchNotification(this, changeType, changeObject);
	}
	public final void fireSelectionNotification(Object changeObject) {
		fireChangeNotification(SELECTION, changeObject);
	}
	public String getDescription() {
		return description;
	}
	public String getTitle() {
		return title;
	}
	public void initialize(Object input) {
	}
	public boolean isDirty() {
		return dirty;
	}
	public boolean isReadOnly() {
		return readOnly;
	}
	public void sectionChanged(
		SectionPart source,
		int changeType,
		Object changeObject) {
	}

	public void setDescription(java.lang.String newDescription) {
		description = newDescription;
		if (descriptionLabel != null)
			descriptionLabel.setText(newDescription);
	}
	public void setDirty(boolean newDirty) {
		dirty = newDirty;
	}
	public void setFocus() {
		if (twistie != null)
			twistie.setFocus();
	}

	public void setTitle(String newTitle) {
		title = newTitle;
		if (titleLabel != null)
			titleLabel.setText(title);
	}
	void setManager(SectionChangeManager manager) {
		this.sectionManager = manager;
	}
	public void setReadOnly(boolean newReadOnly) {
		readOnly = newReadOnly;
	}

	public void update() {
	}

	public void propertyChange(PropertyChangeEvent arg0) {
		if (control != null
			&& titleLabel != null
			&& !control.isDisposed()
			&& !titleLabel.isDisposed()) {
			titleLabel.setFont(JFaceResources.getBannerFont());
			control.layout(true);
		}
	}

	public void setCollapsed(boolean collapsed) {
		this.collapsed = collapsed;
	}

	public boolean isCollapsed() {
		return collapsed;
	}

	public boolean canPaste(Clipboard clipboard) {
		return false;
	}
	private boolean hasStyle(int value) {
		return (style & value) != 0;
	}
}