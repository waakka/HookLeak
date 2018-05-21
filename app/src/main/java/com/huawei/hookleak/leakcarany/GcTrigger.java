package com.huawei.hookleak.leakcarany;

public class GcTrigger {

    public static void runGc(){
        Runtime.getRuntime().gc();
        enqueueReferences();
        System.runFinalization();
    };

    public static void enqueueReferences() {
        // Hack. We don't have a programmatic way to wait for the reference queue daemon to move
        // references to the appropriate queues.
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new AssertionError();
        }
    }
}
