package hanzirecog.engine.beans;

import hanzirecog.engine.enums.CharacterType;

/*
 * Copyright (C) 2006 Jordan Kiang
 * jordan-at-hanzirecog.swingui.uicommon.org
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
 * A TypeDescriptor defines a Character type and possibly its relationship to another Character.
 */
public class TypeDescriptor {

  private CharacterType type;
  private Character unicode;
  private Character altUnicode;

  /**
   * Instantiate a new TypeDescriptor with the given recognizer.
   * <p>
   * GENERIC_TYPE means that the unicode code point is common to both simplified and traditional character sets.  altUnicode should be null.
   * SIMPLIFIED_TYPE means that the unicode code point is a simplified form of the character altUnicode.
   * TRADITIONAL_TYPE means that the unicode code point is a traditional form of the character altUnicode.
   * EQUIVALENT_TYPE means that the unicode code point is equivalent to the character altUnicode.
   *
   * @param type         the type of the Character / relationship
   * @param character    the character described by this TypeDescriptor
   * @param altCharacter another character that the main character shares a relationship to, can be null
   */
  public TypeDescriptor(CharacterType type, Character character, Character altCharacter) {

    this.type = type;
    this.unicode = character;
    this.altUnicode = altCharacter;
  }

  /**
   * @return the type described by this descriptor
   */
  public CharacterType getType() {
    return type;
  }

  /**
   * @return the primary character of this descriptor
   */
  public Character getUnicode() {
    return unicode;
  }

  /**
   * @return the alternate character defined by this descriptor's relationship
   */
  public Character getAlUnicode() {
    return altUnicode;
  }

  public boolean isGeneric() {
    return type.isGeneric();
  }

  public boolean isSimplified() {
    return type.isSimplified();
  }

  public boolean isTraditional() {
    return type.isTraditional();
  }

  public boolean isEquivalent() {
    return type.isEquivalent();
  }
}