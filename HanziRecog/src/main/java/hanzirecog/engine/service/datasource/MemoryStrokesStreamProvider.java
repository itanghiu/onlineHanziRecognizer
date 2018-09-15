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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A StrokesStreamProvider that serves up InputStreams from an in-memory byte bucket.
 * Use this guy if you just want to dump all the stroke recognizer in memory, as it will
 * probably speed up lookups.
 * 
 * @see StrokesStreamProvider
 */
public class MemoryStrokesStreamProvider implements StrokesStreamProvider {

	private byte[] strokeBytes;
	
	/**
	 * Create an instance from an existing byte array of stroke recognizer
	 * @param strokeBytes stroke recognizer
	 */
	public MemoryStrokesStreamProvider(byte[] strokeBytes) {
		this.strokeBytes = strokeBytes;
	}
	
	/**
	 * Create an instance by reading the recognizer into memory from the given stream
	 * @param inputStream stroke recognizer stream
	 * @throws IOException
	 */
	public MemoryStrokesStreamProvider(InputStream inputStream) throws IOException {

		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		// fully read in the stream
		byte[] buffer = new byte[1024];
		for(int bytesRead = inputStream.read(buffer); bytesRead > -1; bytesRead = inputStream.read(buffer)) {
			bytes.write(buffer, 0, bytesRead);
		}
		strokeBytes = bytes.toByteArray();
	}
	
	/**
	 * @return InputStream from in memory byte bucket
	 * @see StrokesStreamProvider#getStrokesStream()
	 */
	public InputStream getStrokesStream() {
		return new ByteArrayInputStream(this.strokeBytes);
	}
}
