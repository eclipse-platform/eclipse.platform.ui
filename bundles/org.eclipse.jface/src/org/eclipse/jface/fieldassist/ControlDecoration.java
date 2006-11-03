/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.fieldassist;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

/**
 * ControlDecoration renders an image decoration near a control. It allows
 * clients to specify an image decoration and a position for the decoration
 * relative to the control. Decorations may be assigned descriptions, which can
 * optionally be shown when the user hovers over the decoration. Clients can
 * decorate any kind of control.
 * <p>
 * Decorations always appear on the left or right side of the field, never above
 * or below it. Decorations can be positioned at the top, center, or bottom of
 * either side of the control. Future implementations may provide additional
 * positioning options for decorations.
 * <p>
 * ControlDecoration is used in a manner similar to {@link DecoratedField}. The
 * primary difference between these mechanisms is that {@link DecoratedField}
 * ensures adequate space is allocated for any field decorations by creating a
 * composite that parents the decorations and fields, and reserving space for
 * any decorations added to the field. ControlDecoration simply renders the
 * decoration adjacent to the specified (already created) control, with no
 * guarantee that the decoration won't be clipped or otherwise obscured or
 * overlapped by adjacent controls, including another ControlDecoration placed
 * in the same location. The tradeoff is one of guaranteed placement (via
 * {@link DecoratedField}) vs. more flexibility in creating the control, using
 * ControlDecoration, along with less concern for aligning decorated and
 * non-decorated fields.
 * <p>
 * Clients using ControlDecoration should typically ensure that enough margin
 * space is reserved for a decoration by altering the layout data margins,
 * although this is not assumed or required by the ControlDecoration
 * implementation.
 * <p>
 * This class is intended to be instantiated and used by clients. It is not
 * intended to be subclassed by clients.
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong> This class or interface has been added as part
 * of a work in progress. This API may change at any given time. Please do not
 * use this API without consulting with the Platform/UI team.
 * 
 * @since 3.3
 * 
 * @see FieldDecoration
 * @see FieldDecorationRegistry
 * @see DecoratedField
 */
public class ControlDecoration {
	/**
	 * Debug flag for tracing
	 */
	private static boolean DEBUG = false;

	/**
	 * Cached platform flags for dealing with platform-specific issues.
	 */
	private static boolean CARBON = "carbon".equals(SWT.getPlatform()); //$NON-NLS-1$

	/**
	 * The associated control
	 */
	private Control control;

	/**
	 * The associated decoration.
	 */
	private FieldDecoration decoration;

	/**
	 * The position of the decoration.
	 */
	private int position;

	/**
	 * The decoration's visibility flag
	 */
	private boolean visible = true;

	/**
	 * Boolean indicating whether the decoration should only be shown when the
	 * control has focus
	 */
	private boolean showOnlyOnFocus = false;

	/**
	 * Boolean indicating whether the decoration should show its description
	 * text in a hover when the user hovers over the decoration.
	 */
	private boolean showHover = true;

	/**
	 * Margin width used between the decorator and the control.
	 */
	private int marginWidth = 0;

	/**
	 * The focus listener
	 */
	private FocusListener focusListener;

	/**
	 * The dispose listener
	 */
	private DisposeListener disposeListener;

	/**
	 * The paint listener installed for drawing the decoration
	 */
	private PaintListener paintListener;

	/**
	 * The mouse listener installed for tracking the hover
	 */
	private MouseTrackListener mouseListener;

	/**
	 * The mouse move listener installed for tracking the hover
	 */
	private MouseMoveListener mouseMoveListener;

	/**
	 * Control that we last installed a move listener on. We only want one at a
	 * time.
	 */
	private Control moveListeningTarget = null;

	/**
	 * Debug counter used to match add and remove listeners
	 */
	private int listenerInstalls = 0;

	/**
	 * The current rectangle used for tracking mouse moves
	 */
	private Rectangle decorationRectangle;

	/**
	 * An internal flag tracking whether we have focus. We use this rather than
	 * isFocusControl() so that we can set the flag as soon as we get the focus
	 * callback, rather than having to do an asyncExec in the middle of a focus
	 * callback to ensure that isFocusControl() represents the outcome of the
	 * event.
	 */
	private boolean hasFocus = false;

	/**
	 * The hover used for showing description text
	 */
	private Hover hover;

	/**
	 * The hover used to show a decoration image's description.
	 */
	class Hover {
		private static final String EMPTY = ""; //$NON-NLS-1$

		/**
		 * Offset of info hover arrow from the left or right side.
		 */
		private int hao = 10;

		/**
		 * Width of info hover arrow.
		 */
		private int haw = 8;

		/**
		 * Height of info hover arrow.
		 */
		private int hah = 10;

		/**
		 * Margin around info hover text.
		 */
		private int hm = 2;

		/**
		 * This info hover's shell.
		 */
		Shell hoverShell;

		/**
		 * The info hover text.
		 */
		String text = EMPTY;

		/**
		 * The region used to manage the shell shape
		 */
		Region region;

		/**
		 * Boolean indicating whether the last computed polygon location had an
		 * arrow on left. (true if left, false if right).
		 */
		boolean arrowOnLeft = true;

		/*
		 * Create a hover parented by the specified shell.
		 */
		Hover(Shell parent) {
			final Display display = parent.getDisplay();
			hoverShell = new Shell(parent, SWT.NO_TRIM | SWT.ON_TOP
					| SWT.NO_FOCUS);
			hoverShell.setBackground(display
					.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
			hoverShell.setForeground(display
					.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
			hoverShell.addPaintListener(new PaintListener() {
				public void paintControl(PaintEvent pe) {
					pe.gc.drawString(text, hm, hm);
					if (!CARBON) {
						pe.gc.drawPolygon(getPolygon(true));
					}
				}
			});
			hoverShell.addMouseListener(new MouseAdapter() {
				public void mouseDown(MouseEvent e) {
					hideHover();
				}
			});
		}

		/*
		 * Compute a polygon that represents a hover with an arrow pointer. If
		 * border is true, compute the polygon inset by 1-pixel border. Consult
		 * the arrowOnLeft flag to determine which side the arrow is on.
		 */
		int[] getPolygon(boolean border) {
			Point e = getExtent();
			int b = border ? 1 : 0;
			if (arrowOnLeft) {
				return new int[] { 0, 0, e.x - b, 0, e.x - b, e.y - b,
						hao + haw, e.y - b, hao + haw / 2, e.y + hah - b, hao,
						e.y - b, 0, e.y - b, 0, 0 };
			}
			return new int[] { 0, 0, e.x - b, 0, e.x - b, e.y - b,
					e.x - hao - b, e.y - b, e.x - hao - haw / 2, e.y + hah - b,
					e.x - hao - haw, e.y - b, 0, e.y - b, 0, 0 };
		}

		/*
		 * Dispose the hover, it is no longer needed. Dispose any resources
		 * allocated by the hover.
		 */
		void dispose() {
			if (!hoverShell.isDisposed()) {
				hoverShell.dispose();
			}
			if (region != null) {
				region.dispose();
			}
		}

		/*
		 * Set the visibility of the hover.
		 */
		void setVisible(boolean visible) {
			if (visible) {
				if (!hoverShell.isVisible()) {
					hoverShell.setVisible(true);
				}
			} else {
				if (hoverShell.isVisible()) {
					hoverShell.setVisible(false);
				}
			}
		}

		/*
		 * Set the text of the hover to the specified text. Recompute the size
		 * and location of the hover to hover near the decoration rectangle,
		 * pointing the arrow toward the target control.
		 */
		void setText(String t, Rectangle decorationRectangle,
				Control targetControl) {
			if (t == null) {
				t = EMPTY;
			}
			if (!t.equals(text)) {
				Point oldSize = getExtent();
				text = t;
				hoverShell.redraw();
				Point newSize = getExtent();
				if (!oldSize.equals(newSize)) {
					// set a flag that indicates the direction of arrow
					arrowOnLeft = decorationRectangle.x <= targetControl
							.getLocation().x;
					setNewShape();
				}
			}

			Point extent = getExtent();
			int y = -extent.y - hah + 1;
			int x = arrowOnLeft ? -hao + haw / 2 : -extent.x + hao + haw / 2;

			hoverShell.setLocation(control.getParent().toDisplay(
					decorationRectangle.x + x, decorationRectangle.y + y));
		}

		/*
		 * Return whether or not the hover (shell) is visible.
		 */
		boolean isVisible() {
			return hoverShell.isVisible();
		}

		/*
		 * Compute the extent of the hover for the current text.
		 */
		Point getExtent() {
			GC gc = new GC(hoverShell);
			Point e = gc.textExtent(text);
			gc.dispose();
			e.x += hm * 2;
			e.y += hm * 2;
			return e;
		}

		/*
		 * Compute a new shape for the hover shell.
		 */
		void setNewShape() {
			Region oldRegion = region;
			region = new Region();
			region.add(getPolygon(false));
			hoverShell.setRegion(region);
			if (oldRegion != null) {
				oldRegion.dispose();
			}

		}
	}

	/**
	 * Construct a ControlDecoration for the specified control, with the
	 * specified decoration and position.
	 * <p>
	 * SWT constants are used to specify the position of the decoration relative
	 * to the control. The position should include style bits describing both
	 * the vertical and horizontal orientation. <code>SWT.LEFT</code> and
	 * <code>SWT.RIGHT</code> describe the horizontal placement of the
	 * decoration relative to the control, and the constants
	 * <code>SWT.TOP</code>, <code>SWT.CENTER</code>, and
	 * <code>SWT.BOTTOM</code> describe the vertical alignment of the
	 * decoration relative to the control. Decorations always appear on either
	 * the left or right side of the control, never above or below it. For
	 * example, a decoration appearing on the left side of the field, at the
	 * top, is specified as SWT.LEFT | SWT.TOP. If no position style bits are
	 * specified, the control decoration will be positioned to the left and
	 * center of the control (<code>SWT.LEFT | SWT.CENTER</code>).
	 * </p>
	 * 
	 * @param control
	 *            the control to be decorated
	 * @param decoration
	 *            the decoration specifying the image and description to be
	 *            shown adjacent to the control.
	 * @param position
	 *            bit-wise or of position constants (<code>SWT.TOP</code>,
	 *            <code>SWT.BOTTOM</code>, <code>SWT.LEFT</code>,
	 *            <code>SWT.RIGHT</code>, and <code>SWT.CENTER</code>).
	 */
	public ControlDecoration(Control control, FieldDecoration decoration,
			int position) {
		this.position = position;
		this.decoration = decoration;
		this.control = control;

		addControlListeners();
	}

	/**
	 * Construct a ControlDecoration for decorating the specified control at the
	 * specified position. The decoration to be displayed will be specified at a
	 * later time.
	 * <p>
	 * SWT constants are used to specify the position of the decoration relative
	 * to the control. The position should include style bits describing both
	 * the vertical and horizontal orientation. <code>SWT.LEFT</code> and
	 * <code>SWT.RIGHT</code> describe the horizontal placement of the
	 * decoration relative to the control, and the constants
	 * <code>SWT.TOP</code>, <code>SWT.CENTER</code>, and
	 * <code>SWT.BOTTOM</code> describe the vertical alignment of the
	 * decoration relative to the control. Decorations always appear on either
	 * the left or right side of the control, never above or below it. For
	 * example, a decoration appearing on the left side of the field, at the
	 * top, is specified as SWT.LEFT | SWT.TOP. If no position style bits are
	 * specified, the control decoration will be positioned to the left and
	 * center of the control (<code>SWT.LEFT | SWT.CENTER</code>).
	 * </p>
	 * 
	 * @param control
	 *            the control to be decorated
	 * @param position
	 *            bit-wise or of position constants (<code>SWT.TOP</code>,
	 *            <code>SWT.BOTTOM</code>, <code>SWT.LEFT</code>,
	 *            <code>SWT.RIGHT</code>, and <code>SWT.CENTER</code>).
	 * 
	 * @see #setDecoration(FieldDecoration)
	 */
	public ControlDecoration(Control control, int position) {
		this(control, null, position);
	}

	/**
	 * Get the control that is decorated by the receiver.
	 * 
	 * @return the Control decorated by the receiver.
	 */
	public Control getControl() {
		return control;
	}

	/**
	 * Add any listeners needed on the target control.
	 */
	private void addControlListeners() {
		disposeListener = new DisposeListener() {
			public void widgetDisposed(DisposeEvent event) {
				dispose();
			}
		};
		printAddListener(control, "DISPOSE"); //$NON-NLS-1$
		control.addDisposeListener(disposeListener);

		focusListener = new FocusListener() {
			public void focusGained(FocusEvent event) {
				hasFocus = true;
				if (showOnlyOnFocus) {
					update();
				}
			}

			public void focusLost(FocusEvent event) {
				hasFocus = false;
				if (showOnlyOnFocus) {
					update();
				}
			}
		};
		printAddListener(control, "FOCUS"); //$NON-NLS-1$
		control.addFocusListener(focusListener);

		// Listener for painting the decoration
		paintListener = new PaintListener() {
			public void paintControl(PaintEvent event) {
				Control control = (Control) event.widget;
				Rectangle rect = getDecorationRectangle(control);
				if (shouldShowDecoration()) {
					event.gc.drawImage(getImage(), rect.x, rect.y);
				}
			}
		};

		// Listener for tracking the end of a hover. Only installed
		// after a hover begins.
		mouseMoveListener = new MouseMoveListener() {
			public void mouseMove(MouseEvent event) {
				if (showHover) {
					if (!decorationRectangle.contains(event.x, event.y)) {
						hideHover();
						// No need to listen any longer
						printRemoveListener(event.widget, "MOUSEMOVE"); //$NON-NLS-1$
						((Control) event.widget)
								.removeMouseMoveListener(mouseMoveListener);
						moveListeningTarget = null;
					}
				}
			}
		};

		// Listener for tracking the beginning of a hover. Always installed.
		mouseListener = new MouseTrackListener() {
			public void mouseExit(MouseEvent event) {
				// Just in case we didn't catch it before.
				Control target = (Control) event.widget;
				if (target == moveListeningTarget) {
					printRemoveListener(target, "MOUSEMOVE"); //$NON-NLS-1$
					target.removeMouseMoveListener(mouseMoveListener);
					moveListeningTarget = null;
				}
				hideHover();
			}

			public void mouseHover(MouseEvent event) {
				if (showHover) {
					decorationRectangle = getDecorationRectangle((Control) event.widget);
					if (decorationRectangle.contains(event.x, event.y)) {
						showHoverText(getDescriptionText());
						Control target = (Control) event.widget;
						if (moveListeningTarget == null) {
							printAddListener(target, "MOUSEMOVE"); //$NON-NLS-1$
							target.addMouseMoveListener(mouseMoveListener);
							moveListeningTarget = target;
						} else if (target != moveListeningTarget) {
							printRemoveListener(moveListeningTarget,
									"MOUSEMOVE"); //$NON-NLS-1$
							moveListeningTarget
									.removeMouseMoveListener(mouseMoveListener);
							printAddListener(target, "MOUSEMOVE"); //$NON-NLS-1$
							target.addMouseMoveListener(mouseMoveListener);
							moveListeningTarget = target;
						} else {
							// It is already installed on this control.
						}
					}
				}
			}

			public void mouseEnter(MouseEvent event) {
				// Nothing to do until a hover occurs.
			}
		};

		// We are never quite sure which parent in the control hierarchy
		// is providing the margin space, so hook all the way up.
		Control c = control.getParent();
		while (c != null) {
			printAddListener(c, "PAINT"); //$NON-NLS-1$
			c.addPaintListener(paintListener);
			printAddListener(c, "MOUSE"); //$NON-NLS-1$
			c.addMouseTrackListener(mouseListener);
			c.redraw();
			if (c instanceof Shell)
				break;
			c = c.getParent();
		}
	}

	/**
	 * Show the specified text using the same hover dialog as is used to show
	 * decorator descriptions. When {@link #setShowHover(boolean)} has been set
	 * to <code>true</code>, a decoration's description text will be shown in
	 * an info hover over the field's control whenever the mouse hovers over the
	 * decoration. This method can be used to show a decoration's description
	 * text at other times (such as when the control receives focus), or to show
	 * other text associated with the field.
	 * 
	 * @param text
	 *            the text to be shown in the info hover, or <code>null</code>
	 *            if no text should be shown.
	 */
	public void showHoverText(String text) {
		if (control == null) {
			return;
		}
		showHoverText(text, control);
	}

	/**
	 * Hide any hover popups that are currently showing on the control. When
	 * {@link #setShowHover(boolean)} has been set to <code>true</code>, a
	 * decoration's description text will be shown in an info hover over the
	 * field's control as long as the mouse hovers over the decoration, and will
	 * be hidden when the mouse exits the decoration. This method can be used to
	 * hide a hover, whether it was shown explicitly using
	 * {@link #showHoverText(String)}, or was showing because the user was
	 * hovering in the decoration.
	 * <p>
	 * This message has no effect if there is no current hover.
	 * 
	 */
	public void hideHover() {
		if (hover != null) {
			hover.setVisible(false);
		}
	}

	/**
	 * Show the control decoration. This message has no effect if the decoration
	 * is already showing. If {@link #setShowOnlyOnFocus(boolean)} is set to
	 * <code>true</code>, the decoration will only be shown if the control
	 * has focus.
	 */
	public void show() {
		if (!visible) {
			visible = true;
			update();
		}
	}

	/**
	 * Hide the control decoration. This message has no effect if the decoration
	 * is already hidden.
	 */
	public void hide() {
		if (visible) {
			visible = false;
			update();
		}
	}

	/**
	 * Set the decoration for this control decoration to the specified
	 * decoration. Update any visuals appropriate for the new decoration.
	 * 
	 * @param decoration
	 *            the decoration to be shown adjacent to the control
	 */
	public void setDecoration(FieldDecoration decoration) {
		this.decoration = decoration;
		update();
	}

	/**
	 * Get the boolean that controls whether the decoration is shown only when
	 * the control has focus. The default value of this setting is
	 * <code>false</code>.
	 * 
	 * @return <code>true</code> if the decoration should only be shown when
	 *         the control has focus, and <code>false</code> if it should
	 *         always be shown.
	 */
	public boolean getShowOnlyOnFocus() {
		return showOnlyOnFocus;
	}

	/**
	 * Set the boolean that controls whether the decoration is shown only when
	 * the control has focus. The default value of this setting is
	 * <code>false</code>.
	 * 
	 * @param showOnlyOnFocus
	 *            <code>true</code> if the decoration should only be shown
	 *            when the control has focus, and <code>false</code> if it
	 *            should always be shown.
	 */
	public void setShowOnlyOnFocus(boolean showOnlyOnFocus) {
		this.showOnlyOnFocus = showOnlyOnFocus;
		update();
	}

	/**
	 * Get the boolean that controls whether the decoration's description text
	 * should be shown in a hover when the user hovers over the decoration. The
	 * default value of this setting is <code>true</code>.
	 * 
	 * @return <code>true</code> if a hover popup containing the decoration's
	 *         description text should be shown when the user hovers over the
	 *         decoration, and <code>false</code> if a hover should not be
	 *         shown.
	 */
	public boolean getShowHover() {
		return showHover;
	}

	/**
	 * Set the boolean that controls whether the decoration's description text
	 * should be shown in a hover when the user hovers over the decoration. The
	 * default value of this setting is <code>true</code>.
	 * 
	 * @param showHover
	 *            <code>true</code> if a hover popup containing the
	 *            decoration's description text should be shown when the user
	 *            hovers over the decoration, and <code>false</code> if a
	 *            hover should not be shown.
	 */
	public void setShowHover(boolean showHover) {
		this.showHover = showHover;
		update();
	}

	/**
	 * Get the margin width in pixels that should be used between the decorator
	 * and the horizontal edge of the control. The default value of this setting
	 * is <code>0</code>.
	 * 
	 * @return the number of pixels that should be reserved between the
	 *         horizontal edge of the control and the adjacent edge of the
	 *         decoration.
	 */
	public int getMarginWidth() {
		return marginWidth;
	}

	/**
	 * Set the margin width in pixels that should be used between the decorator
	 * and the horizontal edge of the control. The default value of this setting
	 * is <code>0</code>.
	 * 
	 * @param marginWidth
	 *            the number of pixels that should be reserved between the
	 *            horizontal edge of the control and the adjacent edge of the
	 *            decoration.
	 */
	public void setMarginWidth(int marginWidth) {
		this.marginWidth = marginWidth;
		update();
	}

	/**
	 * Something has changed, requiring redraw. Redraw the decoration and update
	 * the hover text if appropriate.
	 */
	protected void update() {
		if (control == null) {
			return;
		}
		Rectangle rect = getDecorationRectangle(control.getShell());
		// Redraw this rectangle in all children
		control.getShell()
				.redraw(rect.x, rect.y, rect.width, rect.height, true);
		control.getShell().update();
		if (hover != null && getDescriptionText() != null) {
			hover.setText(getDescriptionText(), getDecorationRectangle(control
					.getParent()), control);
		}
	}

	/*
	 * Show the specified text in the hover, positioning the hover near the
	 * specified control.
	 */
	private void showHoverText(String text, Control hoverNear) {
		// If we aren't to show a hover, don't do anything.
		if (!showHover) {
			return;
		}
		// If there is no text, don't do anything.
		if (text == null) {
			hideHover();
			return;
		}
		
		// If there is no control, nothing to do
		if (control == null) {
			return;
		}
		// Create the hover if it's not showing
		if (hover == null) {
			hover = new Hover(hoverNear.getShell());
		}
		hover.setText(text, getDecorationRectangle(control.getParent()),
				control);
		hover.setVisible(true);
	}

	/*
	 * The associated control is being disposed.
	 */
	private void dispose() {
		if (hover != null) {
			hover.dispose();
			hover = null;
		}
		removeControlListeners();
	}

	/*
	 * Remove any listeners installed on the controls.
	 */
	private void removeControlListeners() {
		if (control == null) {
			return;
		}
		printRemoveListener(control, "FOCUS"); //$NON-NLS-1$
		control.removeFocusListener(focusListener);
		focusListener = null;

		printRemoveListener(control, "DISPOSE"); //$NON-NLS-1$
		control.removeDisposeListener(disposeListener);
		disposeListener = null;

		// We installed paint and track listeners all the way up the parent
		// tree.
		Control c = control.getParent();
		while (c != null) {
			printRemoveListener(c, "PAINT"); //$NON-NLS-1$
			c.removePaintListener(paintListener);
			printRemoveListener(c, "MOUSE"); //$NON-NLS-1$
			c.removeMouseTrackListener(mouseListener);
			if (c instanceof Shell)
				break;
			c = c.getParent();
		}
		paintListener = null;
		mouseListener = null;

		// We may have a remaining mouse move listener installed
		if (moveListeningTarget != null) {
			printRemoveListener(moveListeningTarget, "MOUSEMOVE"); //$NON-NLS-1$
			moveListeningTarget.removeMouseMoveListener(mouseMoveListener);
			moveListeningTarget = null;
			mouseMoveListener = null;
		}
		if (DEBUG) {
			if (listenerInstalls > 0) {
				System.out.println("LISTENER LEAK>>>CHECK TRACE ABOVE"); //$NON-NLS-1$
			} else if (listenerInstalls < 0) {
				System.out
						.println("REMOVED UNREGISTERED LISTENERS>>>CHECK TRACE ABOVE"); //$NON-NLS-1$
			} else {
				System.out.println("ALL INSTALLED LISTENERS WERE REMOVED."); //$NON-NLS-1$
			}
		}
	}

	/*
	 * Return the rectangle in which the decoration should be rendered, in
	 * coordinates relative to the specified control. If the specified control
	 * is null, return the rectangle in display coordinates.
	 */
	protected Rectangle getDecorationRectangle(Control targetControl) {
		if (getImage() == null || control == null) {
			return new Rectangle(0, 0, 0, 0);
		}
		// Compute the bounds first relative to the control's parent.
		Rectangle imageBounds = getImage().getBounds();
		Rectangle controlBounds = control.getBounds();
		int x, y;
		// Compute x
		if ((position & SWT.RIGHT) == SWT.RIGHT) {
			x = controlBounds.x + controlBounds.width + marginWidth;
		} else {
			// default is left
			x = controlBounds.x - imageBounds.width - marginWidth;
		}
		// Compute y
		if ((position & SWT.TOP) == SWT.TOP) {
			y = controlBounds.y;
		} else if ((position & SWT.BOTTOM) == SWT.BOTTOM) {
			y = controlBounds.y + control.getBounds().height
					- imageBounds.height;
		} else {
			// default is center
			y = controlBounds.y
					+ (control.getBounds().height - imageBounds.height) / 2;
		}

		// Now convert to coordinates relative to the target control.
		Point globalPoint = control.getParent().toDisplay(x, y);
		Point targetPoint;
		if (targetControl == null) {
			targetPoint = globalPoint;
		} else {
			targetPoint = targetControl.toControl(globalPoint);
		}
		return new Rectangle(targetPoint.x, targetPoint.y, imageBounds.width,
				imageBounds.height);
	}

	/*
	 * Return true if the decoration should be shown, false if it should not.
	 */
	private boolean shouldShowDecoration() {
		if (!visible) {
			return false;
		}
		if (control == null || control.isDisposed() || getImage() == null) {
			return false;
		}
		if (showOnlyOnFocus) {
			return hasFocus;
		}
		return true;
	}

	/*
	 * If in debug mode, print info about adding the specified listener.
	 */
	private void printAddListener(Widget widget, String listenerType) {
		listenerInstalls++;
		if (DEBUG) {
			System.out
					.println("Added listener>>>" + listenerType + " to>>>" + widget); //$NON-NLS-1$//$NON-NLS-2$
		}
	}

	/*
	 * If in debug mode, print info about adding the specified listener.
	 */
	private void printRemoveListener(Widget widget, String listenerType) {
		listenerInstalls--;
		if (DEBUG) {
			System.out
					.println("Removed listener>>>" + listenerType + " from>>>" + widget); //$NON-NLS-1$//$NON-NLS-2$
		}
	}

	/*
	 * Get the image that should be shown. May be null.
	 */
	private Image getImage() {
		if (decoration == null) {
			return null;
		}
		return decoration.getImage();
	}

	/*
	 * Get the description text that should be shown. May be null.
	 */
	private String getDescriptionText() {
		if (decoration == null) {
			return null;
		}
		return decoration.getDescription();
	}
}
