package org.eclipse.ui.internal.commands.util.old;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

//	  IDEAS
//	  =====
//	   - types of keys and their order:
//		  - menu (accelerators + mnemonics)
//		  - traversal (tab + mnemonics)
//		  - key down
//	   - old code (accels run before) 
//		   - hidden menu runs first, before menu mnemonics
//	   - new code (accels run after)
//		   - every time an accelerator is added, fix menus
//		   - can cancel traversal with doit (fixes TAB and button mnemonics)
//		   - how to cancel menu mnemonics? (get rid of '&F' but need to know about ALT+'F')
//	   - stop rest of events from running by setting event.type = 0
//	   - use -SWT.KeyDown to put at front of event list?
//

class AccelTable {
	int[] accels;
	Runnable[] runnables;

	void add(int accelerator, Runnable runnable) {
		if (accels == null)
			accels = new int[128];
		if (runnables == null)
			runnables = new Runnable[128];
		int index = 0;
		while (index < accels.length) {
			if (accels[index] == 0)
				break;
			index++;
		}
		if (index == accels.length) {
			//NOT DONE - GROW
		}
		accels[index] = accelerator;
		runnables[index] = runnable;
	}

	void remove(int accelerator, Runnable runnable) {
		//NOT DONE - REMOVE
	}

	boolean run(Event event) {
		if (accels == null)
			return false;
			
		int accelerator = KeyFilter.acceleratorFromEvent(event);

		for (int i = 0; i < accels.length; i++) {
			if (accels[i] == accelerator) {
				if (runnables[i] != null) {
					runnables[i].run();
					return true;
				}
			}
		}
		return false;
	}
}

public class KeyFilter {

	static AccelTable table = new AccelTable();
	static Listener keyListener = new Listener() {
		public void handleEvent(Event e) {
			String string = "*UNKNOWN";
			switch (e.type) {
				case SWT.KeyDown :
					string = "DOWN ";
					break;
				case SWT.KeyUp :
					string = "UP ";
					break;
				case SWT.Traverse :
					string = "TRAVERSE";
					break;
			}
			string += ": stateMask=0x" + Integer.toHexString(e.stateMask);
			if ((e.stateMask & SWT.CTRL) != 0)
				string += " CTRL";
			if ((e.stateMask & SWT.ALT) != 0)
				string += " ALT";
			if ((e.stateMask & SWT.SHIFT) != 0)
				string += " SHIFT";
			if ((e.stateMask & SWT.COMMAND) != 0)
				string += " COMMAND";
			string += ", keyCode=0x" + Integer.toHexString(e.keyCode);
			string += ", character=0x" + Integer.toHexString(e.character);
			switch (e.character) {
				case 0 :
					string += " '\\0'";
					break;
				case SWT.BS :
					string += " '\\b'";
					break;
				case SWT.CR :
					string += " '\\r'";
					break;
				case SWT.DEL :
					string += " DEL";
					break;
				case SWT.ESC :
					string += " ESC";
					break;
				case SWT.LF :
					string += " '\\n'";
					break;
				case SWT.TAB :
					string += " '\\t'";
					break;
				default :
					string += " '" + e.character + "'";
					break;
			}
			System.out.println(string);
		}
	};

	static Listener verifyListener = new Listener() {
		public void handleEvent(Event e) {
			System.out.println(
				e.widget
					+ " verify \""
					+ e.text
					+ "\" "
					+ e.start
					+ " "
					+ e.end);
		}
	};

	static Listener keyFilter = new Listener() {
		public void handleEvent(Event e) {
			if ((e.keyCode & SWT.MODIFIER_MASK) != 0)
				return;
			String string = "\tEAT?";
			if (table.run(e)) {
				switch (e.type) {
					case SWT.KeyDown :
					case SWT.KeyUp :
						e.doit = false;
						break;
					case SWT.Traverse :
						e.detail = SWT.TRAVERSE_NONE;
						e.doit = true;
				}
				e.type = SWT.NONE;
				string += " - Yes!";
			} else {
				string += " - No.";
			}
			System.out.println(string);
			//Thread.dumpStack ();
		}
	};

	static int acceleratorFromEvent(Event event) {
		//CHECK - CTRL+ALT+'Q' on German, accent keys and IME 
		int key = event.character;
		if (key == 0) {
			key = event.keyCode;
		} else {
			if (0 <= key && key <= 0x1F) {
				if ((event.stateMask & SWT.CTRL) != 0) {
					key += 0x40;
				}
			} else {
				if ('a' <= key && key <= 'z') {
					key -= 'a' - 'A';
				}
			}
		}
		int mods = event.stateMask & SWT.MODIFIER_MASK;
		return mods + key;
	}

	public static void main(String[] args) {
		//CASE PROBLEM
		final char ch = 'S';
		table.add(ch, new Runnable() {
			public void run() {
				System.out.println("\t" + ch);
			}
		});
		table.add(SWT.CTRL + ch, new Runnable() {
			public void run() {
				System.out.println("\tCTRL+" + ch);
			}
		});
		table.add(SWT.ALT + ch, new Runnable() {
			public void run() {
				System.out.println("\tALT+" + ch);
			}
		});
		table.add(SWT.SHIFT + ch, new Runnable() {
			public void run() {
				System.out.println("\tSHIFT+" + ch);
			}
		});
		table.add(SWT.ESC, new Runnable() {
			public void run() {
				System.out.println("\tESC");
			}
		});
		table.add(SWT.TAB, new Runnable() {
			public void run() {
				System.out.println("\tTAB");
			}
		});

		Display display = new Display();
		display.addFilter(SWT.Traverse, keyFilter);
		display.addFilter(SWT.KeyDown, keyFilter);
		Shell shell = new Shell(display);
		RowLayout layout = new RowLayout(SWT.VERTICAL);
		layout.fill = true;
		shell.setLayout(layout);
		Composite comp = new Composite(shell, SWT.BORDER);
		Text text1 = new Text(shell, SWT.BORDER | SWT.SINGLE);
		text1.addListener(SWT.Verify, verifyListener);
		Text text2 = new Text(shell, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		text2.addListener(SWT.Verify, verifyListener);
		StyledText styled = new StyledText(shell, SWT.BORDER | SWT.SINGLE);
		styled.addListener(SWT.Verify, verifyListener);
		Combo combo = new Combo(shell, SWT.BORDER);
		combo.addListener(SWT.DefaultSelection, new Listener() {
			public void handleEvent(Event e) {
				System.out.println("*** DefaultSelection");
			}
		});

		Control[] controls =
			new Control[] { comp, text1, text2, styled, combo };
		for (int i = 0; i < controls.length; i++) {
			controls[i].addListener(SWT.Traverse, keyListener);
			controls[i].addListener(SWT.KeyDown, keyListener);
			controls[i].addListener(SWT.KeyUp, keyListener);
		}

		shell.pack();
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}
}
