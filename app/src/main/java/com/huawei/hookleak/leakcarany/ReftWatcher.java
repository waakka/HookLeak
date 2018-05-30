package com.huawei.hookleak.leakcarany;

import android.os.Debug;
import android.util.Log;

import com.huawei.hookleak.FileUtil;

import java.io.File;
import java.io.IOException;
import java.lang.ref.ReferenceQueue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

import de.robv.android.xposed.XposedBridge;

import static com.huawei.hookleak.leakcarany.Retryable.Result.DONE;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

public class ReftWatcher {
    private  WatchExecutor watchExecutor;
    private  Set<String> retainedKeys;
    private  ReferenceQueue<Object> queue;


    public ReftWatcher() {
        retainedKeys = new CopyOnWriteArraySet<>();
        queue = new ReferenceQueue<>();
//        watchExecutor = new AndroidWatchExecutor(5000);
    }

    public void watch(Object watchedReference, String referenceName) {
        watchExecutor = new AndroidWatchExecutor(5000);
        final long watchStartNanoTime = System.nanoTime();
        String key = UUID.randomUUID().toString();
        retainedKeys.add(key);
        final KeyedWeakReference reference =
                new KeyedWeakReference(watchedReference, key, referenceName, queue);
        XposedBridge.log("本次检测"+referenceName+",key="+reference.key);
        ensureGoneAsync(watchStartNanoTime, reference);
    }

    private void ensureGoneAsync(final long watchStartNanoTime, final KeyedWeakReference reference) {
        watchExecutor.execute(new Retryable() {
            @Override public Retryable.Result run() {
                return ensureGone(reference, watchStartNanoTime);
            }
        });
    }


    Retryable.Result ensureGone(final KeyedWeakReference reference, final long watchStartNanoTime) {
        long gcStartNanoTime = System.nanoTime();
        long watchDurationMs = NANOSECONDS.toMillis(gcStartNanoTime - watchStartNanoTime);

        removeWeaklyReachableReferences();

//        if (debuggerControl.isDebuggerAttached()) {
//            // The debugger can create false leaks.
//            return RETRY;
//        }
        if (gone(reference)) {
            return DONE;
        }
        //TODO  再次gc
        GcTrigger.runGc();
        removeWeaklyReachableReferences();
        if (!gone(reference)) {
            long startDumpHeap = System.nanoTime();
            long gcDurationMs = NANOSECONDS.toMillis(startDumpHeap - gcStartNanoTime);

            //TODO  发现内存泄露，需要heapDump


            XposedBridge.log("HPROF_ATN:"+reference.name+",HPROF_KEY:"+reference.key+",HPROF_VALUE:"+reference.name);

            File file = FileUtil.getHprofFile(reference.name);
            try {
                Debug.dumpHprofData(file.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
////            File heapDumpFile = heapDumper.dumpHeap();
//            if (heapDumpFile == RETRY_LATER) {
//                // Could not dump the heap.
//                return RETRY;
//            }
//            long heapDumpDurationMs = NANOSECONDS.toMillis(System.nanoTime() - startDumpHeap);
//            heapdumpListener.analyze(
//                    new HeapDump(heapDumpFile, reference.key, reference.name, excludedRefs, watchDurationMs,
//                            gcDurationMs, heapDumpDurationMs));
        }
        return DONE;
    }


    private boolean gone(KeyedWeakReference reference) {
        return !retainedKeys.contains(reference.key);
    }

    private void removeWeaklyReachableReferences() {
        // WeakReferences are enqueued as soon as the object to which they point to becomes weakly
        // reachable. This is before finalization or garbage collection has actually happened.
        KeyedWeakReference ref;
        while ((ref = (KeyedWeakReference) queue.poll()) != null) {
            retainedKeys.remove(ref.key);
        }
    }
}
