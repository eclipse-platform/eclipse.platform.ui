package org.eclipse.ui.internal.console;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.ui.console.IPatternMatchHandler;

public class IOConsolePatternMatcher implements IDocumentListener {
    
    private IDocument document;
    private ArrayList patterns;
    
    private class CompiledPatternMatchNotifier {
        Pattern pattern;
        IPatternMatchHandler notifier;
        
        CompiledPatternMatchNotifier(Pattern pattern, IPatternMatchHandler notifier) {
            this.pattern = pattern;
            this.notifier = notifier;
        }
    }
    
    public IOConsolePatternMatcher(IDocument doc) {
        this.document = doc;
        document.addDocumentListener(this);
        patterns = new ArrayList();
    }
    
    public void addPatternMatchNotifier(IPatternMatchHandler matchNotifier) {
        synchronized(patterns) {
            if (matchNotifier == null || matchNotifier.getPattern() == null) {
                throw new IllegalArgumentException("Pattern cannot be null"); //$NON-NLS-1$
            }
            
            Pattern pattern = Pattern.compile(matchNotifier.getPattern());
            CompiledPatternMatchNotifier notifier = new CompiledPatternMatchNotifier(pattern, matchNotifier);
            patterns.add(notifier);
            
            try {
                testForMatch(notifier, 0);
            } catch (BadLocationException e){
            }
        }
    }
    
    
    public void removePatternMatchNotifier(IPatternMatchHandler matchNotifier) {
        synchronized(patterns){
            for (Iterator iter = patterns.iterator(); iter.hasNext();) {
                CompiledPatternMatchNotifier element = (CompiledPatternMatchNotifier) iter.next();
                if (element.notifier == matchNotifier) {
                    iter.remove();
                }
            }
        }
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.text.IDocumentListener#documentAboutToBeChanged(org.eclipse.jface.text.DocumentEvent)
     */
    public void documentAboutToBeChanged(DocumentEvent event) {
        
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.text.IDocumentListener#documentChanged(org.eclipse.jface.text.DocumentEvent)
     */
    public void documentChanged(DocumentEvent event) {
        synchronized(patterns) {
            for (Iterator iter = patterns.iterator(); iter.hasNext();) {
                CompiledPatternMatchNotifier pattern = (CompiledPatternMatchNotifier) iter.next();
                try {
                    testForMatch(pattern, event.fOffset);
                } catch (BadLocationException e) {
                }
            }
        }
    }
    
    private void testForMatch(CompiledPatternMatchNotifier compiled, int documentOffset) throws BadLocationException {
        String contents = document.get(documentOffset, document.getLength()-documentOffset);
        Matcher matcher = compiled.pattern.matcher(contents);
        IPatternMatchHandler notifier = compiled.notifier;
        while(matcher.find()) {
            String group = matcher.group();
            if (group.length() > 0) {
                notifier.matchFound(group, matcher.start()+documentOffset);
            }
        }
    }
    
    public void dispose() {
        synchronized(patterns) {
            document = null;
            patterns = null;
        }
    }
    
}
