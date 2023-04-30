import com.github.gradle.node.npm.task.NpmTask

plugins {
    id("com.github.node-gradle.node") version "3.6.0"
}

group = "es.unizar.mii.tmdad"
version = "0.0.1-SNAPSHOT"

tasks.register("npmDev", NpmTask::class.java) {
    args.addAll("run", "dev")
}

tasks.register("npmBuild", NpmTask::class.java) {
    args.addAll("run","build")
}

tasks.register("debug") {
    doLast {
        exec {
            commandLine("mkdir","-v")
        }
    }
}