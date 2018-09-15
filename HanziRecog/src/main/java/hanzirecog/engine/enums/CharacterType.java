package hanzirecog.engine.enums;

/*
 * Copyright (C) August 2018 I-Tang HIU
 * *
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
public enum CharacterType {

    GENERIC_TYPE(0),    // Character is common to both simplified and traditional character sets.
    SIMPLIFIED_TYPE(1),    // Character is a simplified form.
    TRADITIONAL_TYPE(2),// Character is a traditional form.
    EQUIVALENT_TYPE(3),    // Character is equivalent to another character.
    NOT_FOUND(-1);

    private int code;

    CharacterType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static CharacterType get(int code) {

        for (CharacterType character : values())
            if (character.getCode() == code)
                return character;
        return null;
    }

    public boolean isGeneric() {
        return this.equals(GENERIC_TYPE);
    }

    public boolean isSimplified() {
        return this.equals(SIMPLIFIED_TYPE);
    }

    public boolean isTraditional() {
        return this.equals(TRADITIONAL_TYPE);
    }

    public boolean isEquivalent() {
        return this.equals(EQUIVALENT_TYPE);
    }

    public boolean isNotFound() {
        return this.equals(NOT_FOUND);
    }
}
