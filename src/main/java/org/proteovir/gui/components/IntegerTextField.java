package org.proteovir.gui.components;

import java.awt.Toolkit;

import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

public class IntegerTextField extends PlaceholderTextField {
    private static final long serialVersionUID = 1L;

    public IntegerTextField(String placeholder) {
        super(placeholder);
        // install our integer‐only filter
        ((AbstractDocument) getDocument()).setDocumentFilter(new IntegerFilter());
    }

    /**
     * A DocumentFilter that only allows:
     *   • an optional leading minus
     *   • followed by zero or more digits
     * (so users can type “-” first, then digits, or just digits)
     */
    private static class IntegerFilter extends DocumentFilter {
        private boolean isValid(String text) {
            // matches "", "-", "-123", "0", "456", etc.
            return text.matches("-?\\d*");
        }

        @Override
        public void insertString(FilterBypass fb, int offs, String str, AttributeSet a)
                throws BadLocationException {
            StringBuilder sb = new StringBuilder(fb.getDocument().getText(0, fb.getDocument().getLength()));
            sb.insert(offs, str);
            if (isValid(sb.toString())) {
                super.insertString(fb, offs, str, a);
            } else {
                Toolkit.getDefaultToolkit().beep();
            }
        }

        @Override
        public void replace(FilterBypass fb, int offs, int length, String str, AttributeSet a)
                throws BadLocationException {
            StringBuilder sb = new StringBuilder(fb.getDocument().getText(0, fb.getDocument().getLength()));
            sb.replace(offs, offs + length, str == null ? "" : str);
            if (isValid(sb.toString())) {
                super.replace(fb, offs, length, str, a);
            } else {
                Toolkit.getDefaultToolkit().beep();
            }
        }

        @Override
        public void remove(FilterBypass fb, int offs, int length)
                throws BadLocationException {
            StringBuilder sb = new StringBuilder(fb.getDocument().getText(0, fb.getDocument().getLength()));
            sb.delete(offs, offs + length);
            if (isValid(sb.toString())) {
                super.remove(fb, offs, length);
            } else {
                Toolkit.getDefaultToolkit().beep();
            }
        }
    }
}
