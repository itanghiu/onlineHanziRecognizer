/*
 * Copyright (C) 2005 Jordan Kiang
 * jordan-at-swingui.uicommon.org
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

package engine.service.datasource;

import engine.beans.TypeDescriptor;
import engine.enums.CharacterType;

import java.util.Map;

/**
 * Refactorized by I-Tang HIU August 2018
 * A data repository for describing the types of Chinese Characters (ie simplified, traditional, mappings between the two, etc).
 * Generally these will only be built by CharacterTypeParsers when they are done parsing a type file.
 *
 * @see CharacterTypeParser
 */
public class CharacterTypeRepository {

  private Map<Character, TypeDescriptor> typeMap; // thinly wraps a Map that maps Characters to TypeDescriptors.


  /**
   * Instantiate a new CharacterTypeRepository using the map provided.
   *
   * @param typeMap a Map of Characters to TypeDescriptors.
   */
  public CharacterTypeRepository(Map typeMap) {
    this.typeMap = typeMap;
  }

  /**
   * Retrieve the TypeDescriptor associated with the given Character.
   *
   * @param character the Character whose TypeDescriptor we want
   * @return the TypeDescriptor associated with the Character, null if none found
   */
  public TypeDescriptor lookup(Character character) {

    // Just pass the lookup onto the underlying map.
    TypeDescriptor descriptor = typeMap.get(character);
    return descriptor;
  }

  /**
   * Gets the type of the given Character.
   * If the character is considered equivalent to another character,
   * then the type of that equivalent character is returned instead.
   * Return value should be one of the defined constants.
   *
   * @param character the Character whose type we want to know
   * @return the type of the Character, -1 if the Character wasn't found
   */
  public CharacterType getType(Character character) {

    TypeDescriptor typeDescriptor = lookup(character);
    if (typeDescriptor == null)
      return CharacterType.NOT_FOUND;
    if (typeDescriptor.isGeneric() || typeDescriptor.isSimplified() ||
            typeDescriptor.isTraditional()) {
      // Normally we can just return the type set on the TypeDescriptor...
      return typeDescriptor.getType();
    }
    else if (typeDescriptor.isEquivalent()) {
      // except in the case of an equivalent type.
      // In that case the type we return is actually the type of the equivalent mapped to.
      // It's possible that if a mistake mistake in the recognizer file could cause in infinite loop here.
      return getType(typeDescriptor.getAlUnicode());
    }
    return null;
  }
}
