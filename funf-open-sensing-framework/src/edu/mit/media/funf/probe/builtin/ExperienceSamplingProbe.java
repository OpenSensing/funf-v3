/**
 * Funf: Open Sensing Framework
 * Copyright (C) 2010-2011 Nadav Aharony, Wei Pan, Alex Pentland.
 * Acknowledgments: Alan Gardner
 * Contact: nadav@media.mit.edu
 *
 * This file is part of Funf.
 *
 * Funf is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * Funf is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Funf. If not, see <http://www.gnu.org/licenses/>.
 */
package edu.mit.media.funf.probe.builtin;

import android.net.Uri;
import edu.mit.media.funf.probe.CursorCell;
import edu.mit.media.funf.probe.DatedContentProviderProbe;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ExperienceSamplingProbe extends DatedContentProviderProbe {

    // todo: create central FieldContract for the content provider when the experience sampling app is migrated to this.
    private static final String AUTHORITY = "dk.dtu.imm.experiencesampling.answers.contentprovider";
    private static final String BASE_PATH = "answers";
    private static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);

    private static final String DATA_NAME = "QUESTIONS";
    private static final String DATA_DISPLAY_NAME = "Experience Sampling Probe";

    private static final String _ID = "_id";
    private static final String QUESTION_TYPE = "question_type";
    private static final String QUESTION_ANSWER_TYPE = "question_answer_type";
    private static final String QUESTION_ANSWER = "question_answer";
    private static final String QUESTION_TIMESTAMP = "question_timestamp";

    @Override
    protected Uri getContentProviderUri() {
        return CONTENT_URI;
    }

    @Override
    protected String getDateColumnName() {
        return  QUESTION_TIMESTAMP;
    }

    protected TimeUnit getDateColumnTimeUnit() {
        return  TimeUnit.SECONDS;
    }


    @Override
    protected String getDataName() {
        return DATA_NAME;
    }

    @Override
    protected String getDisplayName() {
        return DATA_DISPLAY_NAME;
    }

    @Override
    protected Map<String, CursorCell<?>> getProjectionMap() {
        Map<String, CursorCell<?>> projectionMap = new HashMap<String, CursorCell<?>>();
        projectionMap.put(_ID, longCell());
        projectionMap.put(QUESTION_TYPE, stringCell());
        projectionMap.put(QUESTION_ANSWER_TYPE, stringCell());
        projectionMap.put(QUESTION_ANSWER, stringCell());
        projectionMap.put(QUESTION_TIMESTAMP, longCell());
        return projectionMap;
    }

    @Override
    public String[] getRequiredPermissions() {
        return new String[] {
                android.Manifest.permission.INTERNET,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.RECEIVE_BOOT_COMPLETED,
                android.Manifest.permission.ACCESS_FINE_LOCATION
        };
    }

    protected long getDefaultPeriod() {
        return 604800L;
    }

}

