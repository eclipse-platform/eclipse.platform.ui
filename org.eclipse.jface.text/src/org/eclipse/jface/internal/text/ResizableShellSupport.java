/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.internal.text;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.util.Geometry;

/**
 * Workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=23980 : Shells without borders are
 * not resizable on GTK.
 * 
 * @since 3.6
 */
public class ResizableShellSupport {
	
	/**
	 * Listeners installed on the shell.
	 */
	private static class MouseListenerMix extends MouseTrackAdapter implements MouseListener, MouseMoveListener {
		/**
		 * The shell.
		 */
		private final Shell fShell;
		/**
		 * The minimal shell width.
		 */
		private final int fMinShellWidth;
		/**
		 * The minimal shell height.
		 */
		private final int fMinShellHeight;
		
		/**
		 * The last detected edges.
		 */
		private int fCurrentEdges;
		/**
		 * The shell's original cursor.
		 */
		private Cursor fOriginalCursor;
		
		/**
		 * The location of the mouseDown event (display coordinates).
		 */
		private Point fOriginalMouseLoc;
		/**
		 * The bounds of the shell on mouseDown (display coordinates).
		 */
		private Rectangle fOriginalShellBounds;
		
		/**
		 * Creates a mouse listener mix
		 * @param shell the shell
		 */
		public MouseListenerMix(Shell shell) {
			fShell= shell;
			Rectangle trim= shell.computeTrim(0, 0, 0, 0);
			fMinShellWidth= trim.width + 2 * TRIM_SIZE;
			fMinShellHeight= trim.height + 2 * TRIM_SIZE;
		}

		/*
		 * @see org.eclipse.swt.events.MouseMoveListener#mouseMove(org.eclipse.swt.events.MouseEvent)
		 */
		public void mouseMove(MouseEvent e) {
			if (fOriginalMouseLoc != null) {
				Point mouse= fShell.toDisplay(e.x, e.y);
				int dx= mouse.x - fOriginalMouseLoc.x;
				int dy= mouse.y - fOriginalMouseLoc.y;
	
				Rectangle sb= Geometry.copy(fOriginalShellBounds);
				if ((fCurrentEdges & SWT.LEFT) != 0) {
					int w= Math.max(sb.width - dx, fMinShellWidth);
					sb.x+= sb.width - w;
					sb.width= w;
				}
				if ((fCurrentEdges & SWT.RIGHT) != 0) {
					sb.width= Math.max(sb.width + dx, fMinShellWidth);
				}
				if ((fCurrentEdges & SWT.TOP) != 0) {
					int h= Math.max(sb.height - dy, fMinShellHeight);
					sb.y+= sb.height - h;
					sb.height= h;
				}
				if ((fCurrentEdges & SWT.BOTTOM) != 0) {
					sb.height= Math.max(sb.height + dy, fMinShellHeight);
				}
				fShell.setBounds(sb);
	
			} else {
				int edges= computeEdges(e);
				setEdgesAndCursor(edges);
			}
		}
		
		/*
		 * @see org.eclipse.swt.events.MouseTrackAdapter#mouseExit(org.eclipse.swt.events.MouseEvent)
		 */
		public void mouseExit(MouseEvent e) {
			setEdgesAndCursor(0);
		}
		
		/*
		 * @see org.eclipse.swt.events.MouseAdapter#mouseDown(org.eclipse.swt.events.MouseEvent)
		 */
		public void mouseDown(MouseEvent e) {
			if (fCurrentEdges != 0) {
				fOriginalMouseLoc= fShell.toDisplay(e.x, e.y);
				fOriginalShellBounds= fShell.getBounds();
			}
		}
		
		/*
		 * @see org.eclipse.swt.events.MouseAdapter#mouseUp(org.eclipse.swt.events.MouseEvent)
		 */
		public void mouseUp(MouseEvent e) {
			fOriginalMouseLoc= null;
			fOriginalShellBounds= null;
			fShell.setFocus(); // focus should not stay in shell trim
		}
		
		/*
		 * @see org.eclipse.swt.events.MouseListener#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
		 */
		public void mouseDoubleClick(MouseEvent e) {
			// not interesting
		}
	
		/**
		 * Returns the edges at which the mouse is pointing. The result is one of
		 * {@link SWT#TOP}, {@link SWT#BOTTOM}, {@link SWT#LEFT}, or {@link SWT#RIGHT}, or a
		 * sensible combination thereof.
		 * 
		 * @param e the mouse event
		 * @return the edges or 0 if the mouse is not over an edge
		 */
		private int computeEdges(MouseEvent e) {
			Rectangle sb= fShell.getClientArea();
	
			int edges= 0;
			if (sb.contains(e.x, e.y)) {
				int bottom= sb.y + sb.height - e.y;
				int top= e.y - sb.y;
				int right= sb.x + sb.width - e.x;
				int left= e.x - sb.x;
				
				// prioritize bottom-right:
				if (bottom <= TRIM_SIZE) {
					edges|= SWT.BOTTOM;
				} else if (top <= TRIM_SIZE) {
					edges|= SWT.TOP;
				}
				if (right <= TRIM_SIZE) {
					edges|= SWT.RIGHT;
				} else if (left <= TRIM_SIZE) {
					edges|= SWT.LEFT;
				}
				
				// enlarge corners, prioritizing bottom-right:
				if ((edges & SWT.BOTTOM) != 0) {
					edges= enlargeCorner(SWT.BOTTOM, right, SWT.RIGHT, left, SWT.LEFT);
				} else if ((edges & SWT.RIGHT) != 0) {
					edges= enlargeCorner(SWT.RIGHT, bottom, SWT.BOTTOM, top, SWT.TOP);
				} else if ((edges & SWT.LEFT) != 0) {
					edges= enlargeCorner(SWT.LEFT, bottom, SWT.BOTTOM, top, SWT.TOP);
				} else if ((edges & SWT.TOP) != 0) {
					edges= enlargeCorner(SWT.TOP, right, SWT.RIGHT, left, SWT.LEFT);
				}
			}
			return edges;
		}
	
		/**
		 * Adds an adjacent edge if the distance to a corner is small enough. First corner gets
		 * priority.
		 * 
		 * @param edge the base edge
		 * @param distance1 distance to first corner
		 * @param edge1 adjacent edge of first corner
		 * @param distance2 distance to second corner
		 * @param edge2 adjacent edge of second corner
		 * @return the new edges
		 */
		private static int enlargeCorner(int edge, int distance1, int edge1, int distance2, int edge2) {
			int edges= edge;
			if (distance1 <= CORNER_SIZE) {
				edges|= edge1;
			} else if (distance2 <= CORNER_SIZE) {
				edges|= edge2;
			}
			return edges;
		}
	
		/**
		 * Sets the cursor for the given edges or resets the original if 0.
		 * 
		 * @param edges the edges or 0
		 */
		private void setEdgesAndCursor(int edges) {
			if (edges == fCurrentEdges)
				return;
		
			if (edges == 0) {
				fShell.setCursor(fOriginalCursor);
				fOriginalCursor= null;
				fCurrentEdges= edges;
				fOriginalMouseLoc= null;
				fOriginalShellBounds= null;
				return;
			}
			
			if (fCurrentEdges == 0) {
				fOriginalCursor= fShell.getCursor();
			}
			fCurrentEdges= edges;
			
			int style= 0;
			if ((edges & SWT.TOP) != 0) {
				if ((edges & SWT.LEFT) != 0) {
					style= SWT.CURSOR_SIZENW;
				} else if ((edges & SWT.RIGHT) != 0) {
					style= SWT.CURSOR_SIZENE;
				} else {
					style= SWT.CURSOR_SIZEN;
				}
			} else if ((edges & SWT.BOTTOM) != 0) {
				if ((edges & SWT.LEFT) != 0) {
					style= SWT.CURSOR_SIZESW;
				} else if ((edges & SWT.RIGHT) != 0) {
					style= SWT.CURSOR_SIZESE;
				} else {
					style= SWT.CURSOR_SIZES;
				}
			} else if ((edges & SWT.LEFT) != 0) {
				style= SWT.CURSOR_SIZEW;
			} else if ((edges & SWT.RIGHT) != 0) {
				style= SWT.CURSOR_SIZEE;
			}
		
			Cursor cursor= fShell.getDisplay().getSystemCursor(style);
			fShell.setCursor(cursor);
		}
	}

	
	/**
	 * Width of resize trim.
	 */
	private static final int TRIM_SIZE= 4;
	/**
	 * Enlarged area around corners in which dual-direction is active when mouse is in border.
	 */
	private static final int CORNER_SIZE= 22;

	/**
	 * Makes the given shell resizable on all platforms. The shell must have a {@link GridLayout}.
	 * If the shell is not resizable, this method enlarges the {@link GridLayout#marginWidth
	 * marginWidth} and {@link GridLayout#marginHeight marginHeight} and expects that the added area
	 * is not being shrunken or used in any way by other parties.
	 * 
	 * @param shell the shell
	 */
	public static void makeResizable(final Shell shell) {
		if ((shell.getStyle() & SWT.RESIZE) != 0)
			return;
		
		GridLayout layout= ((GridLayout) shell.getLayout());
		layout.marginWidth+= TRIM_SIZE;
		layout.marginHeight+= TRIM_SIZE;
		
		MouseListenerMix listenerMix= new MouseListenerMix(shell);
		shell.addMouseMoveListener(listenerMix);
		shell.addMouseTrackListener(listenerMix);
		shell.addMouseListener(listenerMix);
		
		shell.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				Rectangle clientArea= shell.getClientArea();
				int gap= - TRIM_SIZE / 2;
				Geometry.expand(clientArea, gap, gap - 1, gap, gap - 1);
				e.gc.setLineWidth(TRIM_SIZE);
				e.gc.setForeground(e.display.getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
				e.gc.drawRectangle(clientArea);
			}
		});
	}
}
