/*
Copyright 2009 Hauke Rehfeld


This file is part of QuakeInjector.

QuakeInjector is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

QuakeInjector is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with QuakeInjector.  If not, see <http://www.gnu.org/licenses/>.
*/

package de.haukerehfeld.quakeinjector;

import java.io.IOException;
import java.io.InputStream;

/**
 * report progress while reading InputStream
 *
 * Note that skipping doesn't add to the progress, and marking isn't supported
 */
public class ProgressListenerInputStream extends InputStream {
	private final InputStream in;
	private final ProgressListener progress;
	
	public ProgressListenerInputStream(InputStream in, ProgressListener progress) {
		this.in = in;
		this.progress = progress;
	}

	
	public int read() throws IOException {
		progress.publish(1);
		return in.read();
	}

	
	public int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}
	
	
	public int read(byte[] b, int off, int len) throws IOException {
		int readcount = in.read(b, off, len);

		if (readcount > 0) {
			progress.publish(readcount);
		}
		return readcount;
	}

	
	public long skip(long n) throws IOException	{
		return in.skip(n);
	}

	
	public int available() throws IOException {
		return in.available();
	}

	
	public void mark(int readlimit) {

	}

	
	public void reset() throws IOException {
		in.reset();
	}

	
	public boolean markSupported() {
		return false;
	}
}

