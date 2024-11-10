package com.huskit.gradle.testsync;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestTag;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class SynchronizingTestListener implements TestExecutionListener {

    private static final SyncTag[] EMPTY_TAGS = new SyncTag[0];
    Delegate delegate;

    public SynchronizingTestListener() {
        this.delegate = buildDelegate();
    }

    @Override
    public void executionStarted(TestIdentifier testIdentifier) {
        delegate.onExecutionStarted.accept(testIdentifier);
    }

    @Override
    public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
        delegate.onExecutionFinished.accept(testIdentifier);
    }

    private Delegate buildDelegate() {
        var tagsList = parseTags();
        if (tagsList.length == 0) {
            return new Delegate(
                    testIdentifier -> {
                    },
                    testIdentifier -> {
                    }
            );
        } else {
            var syncMap = new ConcurrentHashMap<String, LockHolder[]>();
            return new Delegate(
                    testIdentifier -> {
                        if (testIdentifier.isTest()) {
                            var testIdentifierTags = testIdentifier.getTags();
                            if (!testIdentifierTags.isEmpty()) {
                                LockHolder[] locks = null;
                                for (var i = 0; i < tagsList.length; i++) {
                                    var syncTag = tagsList[i];
                                    if (testIdentifierTags.contains(syncTag.testTag)) {
                                        if (locks == null) {
                                            locks = new LockHolder[tagsList.length];
                                        }
                                        try {
                                            locks[i] = new LockHolder(syncTag.fileName, syncTag.syncFileChannel.lock());
                                        } catch (Exception e) {
                                            printErr("Failed to acquire file lock for file [" + syncTag.fileName + "] - " + e.getMessage());
                                        }
                                    }
                                    if (locks != null) {
                                        syncMap.put(testIdentifier.getUniqueId(), locks);
                                    }
                                }
                            }
                        }
                    },
                    testIdentifier -> {
                        if (testIdentifier.isTest()) {
                            var uniqueId = testIdentifier.getUniqueId();
                            var lockHolders = syncMap.get(uniqueId);
                            if (lockHolders != null) {
                                try {
                                    for (var lockHolder : lockHolders) {
                                        if (lockHolder != null) {
                                            try {
                                                lockHolder.lock.release();
                                            } catch (Exception e) {
                                                printErr("Failed to release file lock for file [" + lockHolder.fileName + "] - " + e.getMessage());
                                            }
                                        }
                                    }
                                } finally {
                                    syncMap.remove(uniqueId);
                                }
                            }
                        }
                    }
            );
        }
    }

    private SyncTag[] parseTags() {
        var syncProperty = System.getProperty(Constants.SYNC_PROPERTY);
        if (syncProperty == null || syncProperty.isBlank()) {
            return EMPTY_TAGS;
        }
        try {
            var syncValues = syncProperty.split(Constants.SYNC_PROPERTY_SEPARATOR);
            if (syncValues.length == 1) {
                return parse(syncValues[0])
                        .map(syncTag -> new SyncTag[]{syncTag})
                        .orElse(EMPTY_TAGS);
            } else {
                var tagsList = new SyncTag[syncValues.length];
                for (var i = 0; i < syncValues.length; i++) {
                    var syncValue = syncValues[i];
                    var maybeSyncTag = parse(syncValue);
                    if (maybeSyncTag.isEmpty()) {
                        return EMPTY_TAGS;
                    }
                    var syncTag = maybeSyncTag.get();
                    tagsList[i] = syncTag;
                }
                return tagsList;
            }
        } catch (Exception e) {
            printErr("Test synchronization will be disabled, failed to parse sync tags [" + syncProperty + "]- " + e.getMessage());
            return EMPTY_TAGS;
        }
    }

    @SneakyThrows
    private Optional<SyncTag> parse(String syncProperty) {
        var split = syncProperty.split(Constants.TAG_SEPARATOR);
        var tag = split[0];
        var syncFilePathStr = split[1];
        try {
            return Optional.of(
                    new SyncTag(
                            syncFilePathStr,
                            TestTag.create(tag),
                            new RandomAccessFile(new File(syncFilePathStr), "rw").getChannel())
            );
        } catch (Exception e) {
            printErr("Test synchronization will be disabled because of failure during initialization: " + e.getMessage());
            return Optional.empty();
        }
    }

    private void printErr(String message) {
        System.err.println(SynchronizingTestListener.class.getName() + " - " + message);
    }

    @RequiredArgsConstructor
    private static final class LockHolder {

        String fileName;
        FileLock lock;
    }

    @RequiredArgsConstructor
    private static final class SyncTag {

        String fileName;
        TestTag testTag;
        FileChannel syncFileChannel;
    }

    @RequiredArgsConstructor
    private static final class Delegate {

        Consumer<TestIdentifier> onExecutionStarted;
        Consumer<TestIdentifier> onExecutionFinished;
    }
}
