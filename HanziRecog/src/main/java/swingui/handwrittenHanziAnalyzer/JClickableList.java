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

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.JList;

/**
 * The same as a normal JList, except that it also fires a ListClickedEvent
 * whenever a mouse press is released on an element that leaves the element
 * in a selected state.  A convenience for situations where events triggered
 * by clicks are more natural to use than ListSelectionEvents.
 */
public class JClickableList extends JList {
    
    // LinkedHashSet ensures uniqueness, iterates in order.
    private Set listClickedListeners = new LinkedHashSet();
    
    public JClickableList() {
        super();
        this.initEventListeners();
    }
    
    /*
     * Set up the mouse listening behavior via an inner listener class.
     * Also sets up key listening so that hitting enter when there is a selected
     * item will have the same effect as clicking.
     */
    private void initEventListeners() {
        
        this.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                int clickedIndex = JClickableList.this.locationToIndex(e.getPoint());
                
                if(clickedIndex > -1 && JClickableList.this.isSelectedIndex(clickedIndex)) {
                    // Only fire the event if the clicked element was left in a selected
                    // state after the click event.  It is assumed that the normal mouse
                    // selection behavior already took place via an earlier registered
                    // MouseListener.
                
                    JClickableList.this.notifyClickListeners();
                }
            }
        });
        
        this.addKeyListener(new KeyAdapter() {
           public void keyPressed(KeyEvent e) {
               // We want pressing when there is a selected index to trigger a ListClickedEvent. 
               if(KeyEvent.VK_ENTER == e.getKeyCode()) {
                   int selectedIndex = JClickableList.this.getSelectedIndex();
                   
                   if(selectedIndex > -1) {
                       JClickableList.this.notifyClickListeners();
                   }
               }
           }
        });
    }
    
    /*
     * Fire a new ListClickedEvent for all registered liListClickedListener.
     */
    private void notifyClickListeners() {

        synchronized(this.listClickedListeners) {
	        for(Iterator listenerIter = this.listClickedListeners.iterator(); listenerIter.hasNext();) {
	            ListClickedListener nextListener = (ListClickedListener)listenerIter.next();
	            nextListener.listClicked(new ListClickedEvent());
	        }
        }
    }

    /**
     * @param listener the ListClickedListener to add
     */
    public void addListClickedListener(ListClickedListener listener) {

        if(null != listener) {
            synchronized(this.listClickedListeners) {
	            this.listClickedListeners.add(listener);
	        }
        }
        // No effect if listener is null.
    }
    
    /**
     * @param listener the ListClickedListener to remove
     */
    public synchronized void removeListClickedListener(ListClickedListener listener) {

    	if(null != listener) {
    	    synchronized(this.listClickedListeners) {
                this.listClickedListeners.remove(listener);
            }
        }
        
        // No effect if listener is null.
    }
    
    /**
     * Signals that an element in this JClickableList has been clicked and that the
     * click left it in a selected state.
     */
    public class ListClickedEvent extends EventObject {

        private ListClickedEvent() {
            // The source should always be this JClickableList.
            super(JClickableList.this);
        }
    }
    
    /**
     * A Listener that is notified when an element in a JClickableList is clicked
     * and the click leaves the element in a selected state. 
     */
    static public interface ListClickedListener {
        
        /**
         * Called when an element is selected.
         * @param e the event
         */
        public void listClicked(ListClickedEvent e);
    }
}
