/*
 * Copyright (C) 2006 Jordan Kiang
 * jordan-at-hanzirecog.swingui.uicommon.org
 *
 *  Refactorized by I-Tang HIU
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

package hanzirecog.engine.service.datasource;

import hanzirecog.engine.StrokesStreamProvider;

import java.io.InputStream;

/**
 * A StrokesStreamProvider whose recognizer comes from reading in a resource file.
 * Use this guy to save on the memory of holding the strokes recognizer resident in memory.
 */
public class ResourceStrokesStreamProvider implements StrokesStreamProvider {

    private String resourcePath;

    /**
     * @param resourcePath the path to the strokes recognizer resource file
     */
    public ResourceStrokesStreamProvider(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    /**
     * @return InputStream from the resource
     * @see StrokesStreamProvider#getStrokesStream()
     */
    public InputStream getStrokesStream() {

        InputStream resourceStream = getClass().getResourceAsStream(resourcePath);
        if (null == resourceStream)
            throw new NullPointerException("Unable to stream resource: " + resourcePath);
        return resourceStream;
    }
}
