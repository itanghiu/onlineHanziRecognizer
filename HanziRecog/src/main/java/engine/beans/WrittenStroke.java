package engine.beans;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
 * A WrittenStroke holds onto a list of points.
 * It can use those points analyze itself and build a List of SubStrokes.
 * <p>
 * Analyzing and building a SubStroke List is a two-stage process.
 * The Stroke must be analyzed before the List can be built.
 * The reason analyzing and marking is separate from building the List is mostly
 * so that we could graphically display the SubStroke segments if we chose to.
 */
public class WrittenStroke {

    // Defines the minimum length of a SubStroke segment.
    // If a two pivot points are within this length, the first of the pivots will be unmarked as a pivot.
    static final private double MIN_SEGMENT_LENGTH = 12.5;

    // Used to find abrupt corners in a stroke that delimit two SubStrokes.
    static final private double MAX_LOCAL_LENGTH_RATIO = 1.1;

    // Used to find a gradual transition between one SubStroke and another at a curve.
    static final private double MAX_RUNNING_LENGTH_RATIO = 1.09;

    private List<WrittenPoint> points = new ArrayList();
    private boolean isAnalyzed = false; // Flag to see if this stroke has already been analyzed


    public WrittenStroke() {
    }

    public WrittenStroke(List<WrittenPoint> points) {
        this.points = points;
    }

    public List getPoints() {
        return points;
    }

    /**
     * @return true if this Stroke has already been analyzed, false otherwise
     */
    public boolean isAnalyzed() {
        return isAnalyzed;
    }

    /**
     * Add the given WrittenPoint to this WrittenStroke.
     *
     * @param point the point to add to this WrittenStroke
     */
    public void addPoint(WrittenPoint point) {
        points.add(point);
    }

    public List getSubStrokes(WrittenCharacter writtenCharacter) {

        if (!isAnalyzed)
            analyzeAndMark();
        List<SubStrokeDescriptor> subStrokes = new ArrayList();
        WrittenPoint previousPoint = null;
        boolean firstTime = true;
        for (WrittenPoint nextPoint : points) {
            if (firstTime) {
                previousPoint = nextPoint;
                firstTime = false;
                continue;
            }
            if (nextPoint.isPivot()) {
                // The direction from each previous point to each successive point, in radians.
                double direction = previousPoint.getDirection(nextPoint);
                // Use the normalized length, to account for relative character size.
                double normalizedLength = writtenCharacter.getDistanceNormalized(previousPoint, nextPoint);
                SubStrokeDescriptor subStroke = new SubStrokeDescriptor(direction, normalizedLength);
                subStrokes.add(subStroke);
                previousPoint = nextPoint;
            }
        }
        return subStrokes;
    }

    /**
     * Analyzes the given WrittenStroke and marks its constituent WrittenPoints to demarcate the SubStrokes.
     * Points that demarcate between the SubStroke segments are marked as pivot points.
     * These pivot points can later be used to build up a List of SubStroke objects.
     */
    public void analyzeAndMark() {

        Iterator<WrittenPoint> pointIter = points.iterator();
        // It should be impossible for a stroke to have < 2 points, so we are safe calling next() twice.
        WrittenPoint firstPoint = pointIter.next();
        WrittenPoint previousPoint = firstPoint;
        WrittenPoint pivotPoint = pointIter.next();

        // The first point of a Stroke is always a pivot point.
        firstPoint.setIsPivot(true);
        int subStrokeIndex = 1;

        // The first point and the next point are always part of the first SubStroke.
        firstPoint.setSubStrokeIndex(subStrokeIndex);
        pivotPoint.setSubStrokeIndex(subStrokeIndex);

        // localLength keeps track of the immediate distance between the latest three points.
        // We can use the localLength to find an abrupt change in SubStrokes, such as at a corner.
        // We do this by checking localLength against the distance between the first and last
        // of the three points.  If localLength is more than a certain amount longer than the
        // length between the first and last point, then there must have been a corner of some kind.
        double localLength = firstPoint.distance(pivotPoint);

        // runningLength keeps track of the length between the start of the current SubStroke
        // and the point we are currently examining.  If the runningLength becomes a certain
        // amount longer than the straight distance between the first point and the current
        // point, then there is a new SubStroke.  This accounts for a more gradual change
        // from one SubStroke segment to another, such as at a longish curve.
        double runningLength = localLength;

        // Iterate over the points, marking the appropriate ones as pivots.
        while (pointIter.hasNext()) {
            WrittenPoint nextPoint = pointIter.next();
            // pivotPoint is the point we're currently examining to see if it's a pivot.
            // We get the distance between this point and the next point and add it
            // to the length sums we're using.
            double pivotLength = pivotPoint.distance(nextPoint);
            localLength += pivotLength;
            runningLength += pivotLength;

            // Check the lengths against the ratios.  If the lengths are a certain among
            // longer than a straight line between the first and last point, then we
            // mark the point as a pivot.
            if (localLength >= MAX_LOCAL_LENGTH_RATIO * previousPoint.distance(nextPoint) ||
                    runningLength >= MAX_RUNNING_LENGTH_RATIO * firstPoint.distance(nextPoint)) {

                if (previousPoint.isPivot() && previousPoint.distance(pivotPoint) < MIN_SEGMENT_LENGTH) {
                    // If the previous point was a pivot and was very close to this point,
                    // which we are about to mark as a pivot, then unmark the previous point as a pivot.
                    // Also need to decrement the SubStroke that it belongs to since it's not part of
                    // the new SubStroke that begins at this pivot.
                    previousPoint.setIsPivot(false);
                    previousPoint.setSubStrokeIndex(subStrokeIndex - 1);
                }
                else {
                    // If we didn't have to unmark a previous pivot, then the we can increment the SubStrokeIndex.
                    // If we did unmark a previous pivot, then the old count still applies and we don't need to increment.
                    subStrokeIndex++;
                }
                pivotPoint.setIsPivot(true);
                // A new SubStroke has begun, so the runningLength gets reset.
                runningLength = pivotLength;
                firstPoint = pivotPoint;
            }
            // Always update the localLength, since it deals with the last three seen points.
            localLength = pivotLength;
            previousPoint = pivotPoint;
            pivotPoint = nextPoint;
            pivotPoint.setSubStrokeIndex(subStrokeIndex);
        }

        // last point (currently referenced by pivotPoint) has to be a pivot
        pivotPoint.setIsPivot(true);

        // Point before the final point may need to be handled specially.
        // Often mouse action will produce an unintended small segment at the end.
        // We'll want to unmark the previous point if it's also a pivot and very close to the lat point.
        // However if the previous point is the first point of the stroke, then don't unmark it, because then we'd only have one pivot.
        if (previousPoint.isPivot() &&
                previousPoint.distance(pivotPoint) < MIN_SEGMENT_LENGTH &&
                previousPoint != points.get(0)) {

            previousPoint.setIsPivot(false);
            pivotPoint.setSubStrokeIndex(subStrokeIndex - 1);
        }
        // Mark the stroke as analyzed so that it won't need to be analyzed again.
        isAnalyzed = true;
    }
}