
package org.eclipse.update.ui.forms.internal.engine;

import org.eclipse.swt.graphics.GC;
import java.util.Hashtable;

public interface IBulletParagraph extends IParagraph {
	int CIRCLE = 0;
	int TEXT = 1;
	int IMAGE = 2;
	public int getBulletStyle();
	public String getBulletText();

	public void paintBullet(GC gc, Locator loc, int lineHeight, Hashtable objectTable);
}