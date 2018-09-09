package engine.beans;

/*
 * Copyright (C) 2006 Jordan Kiang
 * jordan-at-swingui.uicommon.org
 *
 * Refactorized by I-Tang HIU August 2018
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
/**
 * A simple class that encapsulates a Character and its score.
 * It's implements Comparable so in can be easily sorted with
 * other instances.
 *
 */
public class CharacterMatch implements Comparable {

    private Character character;
    private double score;

    /**
     * @param character the Character for this result
     * @param score     the score for the Character when compared
     */
    public CharacterMatch(Character character, double score) {

        this.character = character;
        this.score = score;
    }

    /**
     * @see Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object o) {

        CharacterMatch compareMatch = (CharacterMatch) o;
        double thisScore = score;
        double compareScore = compareMatch.getScore();

        // since scores are doubles and compareTo requires an int,
        // we just just translate to a positive or negative int (1 or -1).
        if (thisScore < compareScore)
            return 1;
        else if (thisScore > compareScore)
            return -1;
        return 0;
    }

    public Character getCharacter() {
        return character;
    }

    public double getScore() {
        return score;
    }
}
