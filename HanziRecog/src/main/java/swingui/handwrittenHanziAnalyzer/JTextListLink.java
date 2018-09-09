/*
 * Copyright (C) 2005 Jordan Kiang
 * jordan-at-swingui.uicommon.org
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package swingui.handwrittenHanziAnalyzer;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *	@author Jordan Kiang
 * 
 *	Links the contents of a given JTextField to a given JList so that:
 *
 * 	1.  Typing into the JTextField will update the JList so that it tries to match its selection to the typed input.
 *	2.	Making a selection in the JList will update the JTextField so that its text matches the selection.
 *
 *	Relies on the toString() implementation of the JList's Objects to do the matching with the JTextField text.
 *	Obviously it only makes sense to use JLists with ListSelectionModel.SINGLE_SELECTION selection mode.
 *	Doesn't determine any layout.  Just pass an existing JTextField and JList to link them up behind the scenes.
 */
public class JTextListLink {

    private JTextField textField;
    private JList list;
    
    /**
     *	Construct a new instance linking the given JTextField to the given JList.
     * 
     *	@param textField the JTextField to link to the given JList
     *	@param list the JList to link to the given JTextField
     *	@param initiallyMatchListToText true if the initial state is that the JList selected value should try to match the JTextField, false if the JTextField should have its contents set the the JList selection
     */
    public JTextListLink(JTextField textField, JList list, boolean initiallyMatchListToText) {
        this.textField = textField;   
        this.list = list;
        UpdateListener updateListener = new UpdateListener();
        
        // match up the initial states of the components
        if(initiallyMatchListToText) {
            updateListener.matchListToText();
        } else {
            updateListener.matchTextToList();
        }
        
        textField.getDocument().addDocumentListener(updateListener);
        list.addListSelectionListener(updateListener);
    }
    
    /**
     *	We need to update the JList when the JTextField is changed, and vice versa.
     *	An instance of this listener is registered with both components, and it handles the updates.
     *	
     *	Note that we need to avoid loop/lock problems (ie if I update the JTextField, it updates
     *	the selection on the JList, which fires a ListSelectionEvent, which we don't want to cycle
     *	back to the JTextField).
     */
    private class UpdateListener implements ListSelectionListener, DocumentListener {
        
        // isAdjusting flag is used to determine whether one component is already in the middle of updating the other.
        // If this is the case, then we don't want update events on the second component to fire change events on the first.
        // This is not rock solid (nothing guarantees that the first event triggered on the opposite component is the
        // result of the event on the first component).  TODO, figure out a better way of doing this. 
        private boolean isAdjusting = false;
        
        /**
         *	Toggle the isAdjusting flag and return the new value.
         *	Each component should check the return value of this method to decide whether they should attempt to update the other.
         * 
         *	@return the new isAdjusting value, having been toggled from the previous value
         */
        synchronized private boolean toggleIsAdjustingLock() {

            this.isAdjusting = !this.isAdjusting;
            return this.isAdjusting;
        }
        
        /**
         *	The selection on the JList is changed.
         *	@see ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
         */
        public void valueChanged(ListSelectionEvent e) {
            this.matchTextToList();
        }
        
        /**
         *	Text was inserted into the JTextField.
         *	@see DocumentListener#insertUpdate(javax.swing.event.DocumentEvent)
         */
        public void insertUpdate(DocumentEvent e) {
            this.matchListToText();
        }

        /**
         *	Text was removed from the JTextField.
         *	@see DocumentListener#removeUpdate(javax.swing.event.DocumentEvent)
         */
        public void removeUpdate(DocumentEvent e) {
            this.matchListToText();
        }

        /**
         *	JTextField change.
         *	@see DocumentListener#changedUpdate(javax.swing.event.DocumentEvent)
         */
        public void changedUpdate(DocumentEvent e) {
            this.matchListToText();
        }
        
        /**
         *	Compare the two Strings to determine the closest match.
         *	In this case the most intuitive behavior seems to come from having the best score
         *	awarded to the Strings that have the most number of leading characters matching.
         *	Case insensitive.
         * 
         *	@param string1 the first String to compare
         *	@param string2 the second String to compare
         *	@return the number of leading characters the two Strings have in common
         */
        private int getStringMatchScore(String string1, String string2) {
            // Can only compare as many characters as are in the shorter String.
            int compareLength = Math.min(string1.length(), string2.length());
            
            // Case insensitive.
            string1 = string1.substring(0, compareLength).toUpperCase();
            string2 = string2.substring(0, compareLength).toUpperCase();
            
            // Just count the number of matching leading characters.
            int characterMatchCount = 0;
            for(int i = 0; i < compareLength; i++) {
                if(string1.charAt(i) == string2.charAt(i)) {
                    characterMatchCount++;
                } else {
                    break;
                }
            }
            
            return characterMatchCount;
        }
        
        /**
         *	Update the JList selection to find the closest match to the contents of the JTextField.
         *	Does nothing if another event is already in progress.
         *
         *	@return true if the matching was carried out, false if another event was blocking action
         */
        private boolean matchListToText() {
            if(this.toggleIsAdjustingLock()) {
                // If this event was caused directly by user input then we want to adjust the JList to match the JTextField text.
                
                String text = JTextListLink.this.textField.getText();
                
                // We can get the list objects from the model.
                ListModel model = JTextListLink.this.list.getModel();
                int modelSize = model.getSize();
                if(modelSize > 0) {
                    // Iterate over each Object in the JList and try to find the Object whose toString value
                    // most closely matches the text input in the JTextField.  Simple interative checking should
                    // be fine since there shouldn't be many entries in a JList.
                    
                    // Note that if the JList contains complex objects whose toString function is expensive,
                    // we might want to cache those ahead of time.
                    int bestMatchCount = this.getStringMatchScore(text, model.getElementAt(0).toString());
                    int selectedIndex = 0;
                    for(int i = 1; i < modelSize; i++) {
                        int matchCount = this.getStringMatchScore(text, model.getElementAt(i).toString());
                       
                        if(matchCount > bestMatchCount) {
                            // The Object at index i was a better match.
                            bestMatchCount = matchCount;
                            selectedIndex = i;
                        }
                    }
                   
                    // Update the JList selection to the closest match.
                    JTextListLink.this.list.setSelectedIndex(selectedIndex);
                    JTextListLink.this.list.ensureIndexIsVisible(selectedIndex);
                }
                
                return true;
            }
            
            return false;
        }
        
        /**
         *	Update the JTextField so that its contents match the selected JList value.
         *	Does nothing if another event is already in progress.
         *
         *	@return true if the matching was carried out, false if another event was blocking action
         */
        private boolean matchTextToList() {
            if(this.toggleIsAdjustingLock()) {
                // If this event was caused directly by user input then we want to adjust the JTextField to match the JList selection.
                Object selectedValue = JTextListLink.this.list.getSelectedValue();
                if(null != selectedValue) {
                    JTextListLink.this.textField.setText(selectedValue.toString());
                } else {
                    JTextListLink.this.textField.setText("");
                }

                return true;
            }
            
            return false;
        }
    }
    
    static public void main(String[] args) {
        
        JFrame frame = new JFrame();
        
        String[] listData = {"america", "aardvark", "abate", "alabaster", "asynchronous", "amen", "alimony", "ack", "acknowledge", "ache", "ape", "antidisestablishmentarianism"};
        
        JTextField textField = new JTextField();
        JList list = new JList(listData);
        JTextListLink link = new JTextListLink(textField, list, true);
        
        frame.getContentPane().add(textField, BorderLayout.NORTH);
        frame.getContentPane().add(list, BorderLayout.CENTER);
        
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
