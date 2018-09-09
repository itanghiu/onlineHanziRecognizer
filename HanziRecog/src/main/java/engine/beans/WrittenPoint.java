package engine.beans;

import java.awt.*;
/*
 * Copyright (C) 2005 Jordan Kiang
 * jordan-at-swingui.uicommon.org
 *
 *  Refactorized by I-Tang HIU August 2018

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
/**
 * WrittenPoints are the constituent points of a WrittenStroke.
 * WrittenPoints can be marked during character analysis.
 * If they are marked as pivots, then that means that the point serves as
 * the end point of one SubStroke, and the beginning point of another.
 *
 * We mark pivot status and the subStrokeIndex on these point objects
 * so that we can display this recognizer graphically if we desire to give a
 * visual que on how the Strokes were divided up.
 */
public class WrittenPoint extends Point {

    private int subStrokeIndex;	// The index of this SubStroke in the character.
    private boolean isPivot;	// If this point is a pivot.

    /**
     * Make new WrittenPoint located at the given coordinates.
     *
     * @param x the x location of the point
     * @param y the y location of the point
     */
    public WrittenPoint(int x, int y) {
        super(x, y);
    }

    /**
     * @return the index of this SubStroke in the character, only set after analysis
     */
    public int getSubStrokeIndex() {
        return subStrokeIndex;
    }

    /**
     * @param subStrokeIndex the index of this SubStroke in the character
     */
    public void setSubStrokeIndex(int subStrokeIndex) {
        this.subStrokeIndex = subStrokeIndex;
    }

    /**
     * @return true if this point is a pivot, false otherwise
     */
    public boolean isPivot() {
        return isPivot;
    }

    /**
     * @param isPivot true if this point is a pivot, false otherwise
     */
    public void setIsPivot(boolean isPivot) {
        this.isPivot = isPivot;
    }

    /**
     * Calculates the direction in radians between this point and the given point.
     * 0 is to the right, PI / 2 is up, etc.
     *
     * @param comparePoint the point to get the direction to from this point
     * @return the direction in radians between this point and the given point.
     */
    public double getDirection(WrittenPoint comparePoint) {

        double dx = this.getX() - comparePoint.getX();
        double dy = this.getY() - comparePoint.getY();
        double direction = Math.PI - Math.atan2(dy, dx);
        return direction;
    }
}
