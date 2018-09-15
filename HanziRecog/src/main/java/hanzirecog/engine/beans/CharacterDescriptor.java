/*
 * Copyright (C) 2006 Jordan Kiang
 * jordan-at-hanzirecog.swingui.uicommon.org
 *
 * Refactorized by I-Tang HIU
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package hanzirecog.engine.beans;

/**
 * Refactorized by I-Tang HIU August 2018
 * A CharacterDescriptor is recognizer holder for storing all the recognizer
 * needed to compare to characters for a match.
 * 
 * Most importantly it has the directions and lengths 
 */
public class CharacterDescriptor {
	
    // Constants for the total maximum number of strokes/substrokes allowed in a character.
    // We put upper bounds just so we can allocate a reusable matrices and avoid having allocate 
    // new arrays for every single character.  These constants can easily be increased if needed.
    static public final int MAX_CHARACTER_STROKE_COUNT = 48;
	static public final int MAX_CHARACTER_SUB_STROKE_COUNT = 64;

	private Character character;// The actual Character.
	private int characterType;// (traditional, simplified, etc).
	private int strokeCount;	// number of strokes
	private int subStrokeCount; // number of "substrokes"
	
	// the directions and lengths of each substroke.
	// indexed by substroke index - 1
	private double[] directions = new double[MAX_CHARACTER_SUB_STROKE_COUNT];
	private double[] lengths	= new double[MAX_CHARACTER_SUB_STROKE_COUNT];
	
	public Character getCharacter() {
		return character;
	}
	
	public void setCharacter(Character character) {
		this.character = character;
	}
	
	public int getCharacterType() {
		return characterType;
	}
	
	public void setCharacterType(int characterType) {
		this.characterType = characterType;
	}
	
	public int getStrokeCount() {
		return strokeCount;
	}
	
	public void setStrokeCount(int strokeCount) {
		this.strokeCount = strokeCount;
	}
	
	public int getSubStrokeCount() {
		return subStrokeCount;
	}
	
	public void setSubStrokeCount(int subStrokeCount) {
		this.subStrokeCount = subStrokeCount;
	}
	
	public double[] getDirections() {
		return directions;
	}
	
	public double[] getLengths() {
		return lengths;
	}
}
