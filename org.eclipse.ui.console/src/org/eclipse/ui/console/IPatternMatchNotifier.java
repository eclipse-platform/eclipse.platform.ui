package org.eclipse.ui.console;


public interface IPatternMatchNotifier {
    
    public String getPattern();
    
    public void matchFound(String text, int offset);
    
}
