package com.huskit.gradle.testsync;

class Constants {

    static final String PLUGIN_VERSION = "1.0.01-SNAPSHOT";
    static final String SYNCHRONIZER_DEPENDENCY = "com.huskit.gradle.testsync:synchronizer:" + PLUGIN_VERSION;
    static final String EXTENSION_NAME = "huskitTestSync";
    static final String BUILD_SERVICE_NAME = "__internal_huskit_plugin_bs__";
    static final String SYNC_FILE_NAME_BASE = "syncfile_";
    static final String PLUGIN_NAME = "com.huskit.gradle.testsync-plugin";
    static final String TAG_SEPARATOR = "_:_:_";
    static final String SYNC_PROPERTY_SEPARATOR = ":___:";
    static final String SYNC_PROPERTY = "io.huskit.gradle.build.sync";
    static final String SYNC_FOLDER_PREFIX = "huskittestsync_";
}
