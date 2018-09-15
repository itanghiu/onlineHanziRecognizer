/*
 * Copyright (C) 2003 Jordan Kiang
 * jordan-at-hanzirecog.swingui.uicommon.org
 *
 *  Refactorized by I-Tang HIU
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

package hanzirecog.engine.util;

import hanzirecog.engine.beans.CharacterMatch;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 *
 * Refactorized by I-Tang HIU August 2018
 * A simple priority queue backed by a LinkedList.  Added objects are inserted in order.
 * Because the internals are based on a List, insertion is O(n) (must scan the List to
 * find the insertion point).  This means that a more efficient heap-based alternative should
 * be used when the number of elements held in the PriorityList isn't small.  For any non-trivial
 * operation use the 1.5 PriorityQueue or the Apache Commons PriorityBuffer instead...
 * <p>
 * Extends AbstractCollection rather than AbstractList, because we don't want to expose the
 * ability to edit the characterMatches by index, since ordering should be defined only by the priority.
 * <p>
 * Basically written because I only needed a simple priority queue that holds a couple of
 * elements simultaneously and the pre 1.5 API doesn't supply a priority queue.
 * <p>
 * The name might be confusing since the List functionality isn't exposed.
 * The name is just a reminder of the internal implementation.
 */
public class PriorityList<T extends Object> extends AbstractCollection {

    private LinkedList<T> characterMatches;            // LinkedList that backs the PriorityList
    private boolean allowsDuplicates;    // whether to allow duplicate values
    private boolean ascendingOrder;        // order ascending or descending

    /**
     * Constructs a new PriorityList ordered by the natural ordering of its elements,
     * allowing duplicates, and in ascending order.  Natural ordering requires that
     * elements implement the Comparable interface, and be mutually comparable.
     */
    public PriorityList() {
        this(true, true);
    }

    /**
     * Constructs a new PriorityList ordered according to the supplied comparator,
     * specifying duplicates and whether ordering should be ascending or descending.
     * @param allowsDuplicates if <code>true</code> the PriorityList can contain
     *                         multiple instances of objects evaluating as equal
     * @param ascendingOrder   if <code>true</code> then the elements should be
     *                         arranged in ascending order, <code>false</code> false for descending.
     */
    public PriorityList( boolean allowsDuplicates, boolean ascendingOrder) {

        characterMatches = new LinkedList();
        this.allowsDuplicates = allowsDuplicates;
        this.ascendingOrder = ascendingOrder;
    }

    /**
     * Inserts the given Object in the characterMatches in correct order according to the PriorityList configuration.
     * @param o the Object to add
     * @return true if the add call changed this PriorityList
     */
    public boolean add(Object o) {

        ListIterator listIter = characterMatches.listIterator();
        while (listIter.hasNext()) {
            Object next = listIter.next();
            int compareVal = compare(o, next);
            if (compareVal < 0) {
                // Add o before next.
                listIter.previous();    // back up one so that insertion is before next
                break;
            }
            else if (compareVal == 0) {
                // o and next are equal.
                if (allowsDuplicates)
                    break;// If duplicates are allowed then we add at the current position.
                else
                    return false;// Duplicate found and duplicates not allowed, no need to do anything.
            }
        }
        // listIter should now be in the proper position to add o.
        listIter.add(o);
        return true;
    }

    /**
     * A wrapper around the Comparator's compare that includes ascending/descending ordering.
     *
     * @param o1
     * @param o2
     * @return =1 if o1 is before o2, 1 if o1 is after o2, 0 if they are the same
     */
    private int compare(Object o1, Object o2) {

        int compareVal = ((Comparable)o1).compareTo(o2);
        if (this.ascendingOrder && compareVal < 0 || !this.ascendingOrder && compareVal > 0)
            return -1;
        else if (compareVal != 0)
            return 1;
        return 0;
    }

    /**
     * @return an Iterator over the PriorityList's elements
     * @see Collection#iterator()
     */
    @Override
    public Iterator iterator() {
        return characterMatches.iterator();
    }

    /**
     * @return the number of Objects in the PriorityList
     * @see Collection#size()
     */
    @Override
    public int size() {
        return characterMatches.size();
    }

    /**
     * @return the element ordered last.
     * @throws NoSuchElementException if empty.
     */
    public Object getLast() {
        return characterMatches.getLast();
    }

    /**
     * Remove and return the element ordered last.
     *
     * @return last ordered element that was removed
     * @throws NoSuchElementException if empty
     */
    public Object removeLast() {
        return characterMatches.removeLast();
    }
}
