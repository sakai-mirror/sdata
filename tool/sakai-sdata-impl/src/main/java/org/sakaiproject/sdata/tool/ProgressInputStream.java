/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.sdata.tool;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * @author ieb
 */
public class ProgressInputStream extends InputStream
{

	private InputStream in;

	private int nread;

	private int lastnread;

	private long lasttime;

	private long start;

	private Map<String, Object> progressMap;

	/**
	 * @param in
	 * @param progressID
	 */
	public ProgressInputStream(InputStream in, Map<String, Object> progressMap)
	{
		this.in = in;
		this.progressMap = progressMap;
		nread = 0;
		start = System.currentTimeMillis();
		lasttime = start;
		lastnread = 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#read()
	 */
	@Override
	public int read() throws IOException
	{
		nread++;
		int v = in.read();
		if ((nread % 10240) == 0 || v < 0)
		{
			updateProgress(v);
		}
		return v;
	}

	/**
	 * time_start: 'time_start',
		time_last: 'time_last',
		speed_average: 'speed_average',
		speed_last: 'speed_last',
		bytes_uploaded: 'bytes_uploaded',
		bytes_total: 'bytes_total',
		files_uploaded: 'files_uploaded',
		est_sec: 'est_sec'

	 * 
	 */
	private void updateProgress(int v)
	{
		long now = System.currentTimeMillis();
		long nread_interval = nread - lastnread;
		long time_interval = now - lasttime+1;
		long overall = now - start+1;
		lastnread = nread;
		lasttime = now;

		long overall_bps = (1000 * nread) / (overall);
		long current_bps = (1000 * nread_interval) / (time_interval);
		progressMap.put("time_start", start);
		progressMap.put("time_last", now);
		progressMap.put("speed_average", overall_bps);
		progressMap.put("speed_last", current_bps);
		progressMap.put("bytes_uploaded", nread);
		if (v < 0)
		{
			progressMap.put("complete", "true");
		}
	}

}
