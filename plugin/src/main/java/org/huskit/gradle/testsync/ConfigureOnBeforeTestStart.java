package org.huskit.gradle.testsync;

import lombok.RequiredArgsConstructor;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.testing.Test;

@RequiredArgsConstructor
class ConfigureOnBeforeTestStart implements Action<Task> {

    HuskitTestSyncExtension testSyncExtension;
    Provider<TestSynchronizerBuildService> syncBuildService;

    @Override
    public void execute(Task task) {
        var tags = testSyncExtension.getTags().get();
        if (!tags.isEmpty()) {
            if (!(task instanceof Test)) {
                throw new IllegalArgumentException(
                    String.format(
                        "Task must be of type '[%s], but received: [%s]'. This is likely caused by a bug in the plugin [%s].",
                        Test.class.getName(), task.getClass().getName(), Constants.PLUGIN_NAME
                    )
                );
            }
            var test = (Test) task;
            var buildService = syncBuildService.get();
            var syncProperty = buildService.buildSyncProperty(testSyncExtension);
            if (syncProperty.property().isEmpty()) {
                task.getLogger().error(
                    "No sync file created for tags {} and seed [{}]. This is likely caused by a bug in the plugin [{}].",
                    tags, syncProperty.seed(), Constants.PLUGIN_NAME
                );
            } else {
                test.systemProperty(
                    Constants.SYNC_PROPERTY,
                    syncProperty.property()
                );
                task.getLogger().info(
                    "Running test task with seed [{}] and sync property [{}]",
                    syncProperty.seed(), syncProperty.property()
                );
            }
        }
    }
}
