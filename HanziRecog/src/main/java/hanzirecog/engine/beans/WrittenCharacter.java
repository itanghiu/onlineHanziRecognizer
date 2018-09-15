/*
 * Copyright (C) 2005 Jordan Kiang
 * jordan-at-hanzirecog.swingui.uicommon.org
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

package hanzirecog.engine.beans;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class WrittenCharacter {

  // Edges to keep track of the coordinates of the bounding box of the character.
  // Used to normalize lengths so that a character can be written in any size as long as proportional.
  private double leftX;
  private double rightX;
  private double topY;
  private double bottomY;

  // List of WrittenStrokes.
  private List<WrittenStroke> strokes = new ArrayList();

  /**
   * Instantiates a new WrittenCharacter object.
   * Strokes can be added to it as inputted.
   */
  public WrittenCharacter() {
    this.resetEdges();
  }

  /**
   * @return the List of Strokes objects that defines this WrittenCharacter.
   */
  public List<WrittenStroke> getStrokes() {
    return strokes;
  }

  /**
   * Add the given Stroke to this WrittenCharacter.
   *
   * @param stroke the Stroke to add to the WrittenCharacter
   */
  public void addStroke(WrittenStroke stroke) {
    strokes.add(stroke);
  }

  /**
   * Resets this character.
   */
  public void clear() {

    strokes.clear();
    resetEdges();
  }

  /**
   * Resets the edges.  Any new point will be more/less than these reset values.
   */
  private void resetEdges() {

    leftX = Double.POSITIVE_INFINITY;
    rightX = Double.NEGATIVE_INFINITY;
    topY = Double.POSITIVE_INFINITY;
    bottomY = Double.NEGATIVE_INFINITY;
  }

  public void analyzeAndMark() {

    for (WrittenStroke nextStroke : strokes) {
      if (!nextStroke.isAnalyzed())
        // If the written character has not been analyzed yet, we need to analyze it.
        nextStroke.analyzeAndMark();
    }
  }

  /**
   * Translate this WrittenCharacter into a CharacterDescriptor.
   * The written recognizer is distilled into SubStrokes in the CharacterDescriptor.
   * The CharacterDescriptor can be used against StrokesRepository to find the closest matches.
   *
   * @return a CharacterDescriptor translated from this WrittenCharacter.
   */
  public CharacterDescriptor buildCharacterDescriptor() {

    int strokeCount = strokes.size();
    int subStrokeCount = 0;
    CharacterDescriptor descriptor = new CharacterDescriptor();
    double[] directions = descriptor.getDirections();
    double[] lengths = descriptor.getLengths();

    // Iterate over the WrittenStrokes, and translate them into CharacterDescriptor.SubStrokes.
    // Add all of the CharacterDescriptor.SubStrokes to the version.
    // When we run out of substroke positions we truncate all the remaining stroke and substroke information.
    for (Iterator strokeIter = strokes.iterator(); strokeIter.hasNext() && subStrokeCount < CharacterDescriptor.MAX_CHARACTER_SUB_STROKE_COUNT; ) {
      WrittenStroke nextStroke = (WrittenStroke) strokeIter.next();

      // Add each substroke's direction and length to the arrays.
      // All substrokes are lumped sequentially.  What strokes they
      // were a part of is not factored into the algorithm.
      // Don't run off the end of the array, if we do we just truncate.
      List subStrokes = nextStroke.getSubStrokes(this);
      for (Iterator subStrokeIter = subStrokes.iterator(); subStrokeIter.hasNext() && subStrokeCount < CharacterDescriptor.MAX_CHARACTER_SUB_STROKE_COUNT; subStrokeCount++) {
        SubStrokeDescriptor subStroke = (SubStrokeDescriptor) subStrokeIter.next();
        directions[subStrokeCount] = subStroke.getDirection();
        lengths[subStrokeCount] = subStroke.getLength();
      }
    }
    descriptor.setStrokeCount(strokeCount);
    descriptor.setSubStrokeCount(subStrokeCount);
    return descriptor;
  }

  public void computeBoundingBox() {

    strokes.forEach((writtenStroke) -> {
      List<WrittenPoint> points = writtenStroke.getPoints();
      for (WrittenPoint writtenPoint : points)
        expandBoundingBox(writtenPoint);
    });
  }

  public void expandBoundingBox(WrittenPoint point) {

    int pointX = (int) point.getX();
    int pointY = (int) point.getY();
    // Expand the bounding box coordinates for this WrittenCharacter in necessary.
    setLeftX(Math.min(pointX, getLeftX()));
    setRightX(Math.max(pointX, getRightX()));
    setTopY(Math.min(pointY, getTopY()));
    setBottomY(Math.max(pointY, getBottomY()));
  }

  /**
   * "Undo" the last stroke added to the character.
   */
  public void undo() {

    List strokesList = getStrokes();
    if (strokesList.size() > 0)
      strokesList.remove(strokesList.size() - 1);
  }

  /**
   * Normalized length takes into account the size of the WrittenCharacter on the canvas.
   * For example, if the WrittenCharacter was written small in the upper left portion of the canvas,
   * then the lengths not be based on the full size of the canvas, but rather only on the relative
   * size of the WrittenCharacter.
   *
   * @param comparePoint the point to get the normalized distance to from this point
   * @return the normalized length from this point to the compare point
   */
  public double getDistanceNormalized(WrittenPoint point1, WrittenPoint comparePoint) {

    double width = rightX - leftX;
    double height = bottomY - topY;

    // normalizer is a diagonal along a square with sides of size the larger dimension of the bounding box
    double dimensionSquared = width > height ? width * width : height * height;
    double normalizer = Math.sqrt(dimensionSquared + dimensionSquared);
    double distanceNormalized = point1.distance(comparePoint) / normalizer;
    distanceNormalized = Math.min(distanceNormalized, 1.0);    // shouldn't be longer than 1 if it's normalized
    return distanceNormalized;
  }

  public void setLeftX(double leftX) {
    this.leftX = leftX;
  }

  public void setRightX(double rightX) {
    this.rightX = rightX;
  }

  public void setTopY(double topY) {
    this.topY = topY;
  }

  public void setBottomY(double bottomY) {
    this.bottomY = bottomY;
  }

  public double getLeftX() {
    return leftX;
  }

  public double getRightX() {
    return rightX;
  }

  public double getTopY() {
    return topY;
  }

  public double getBottomY() {
    return bottomY;
  }
}
