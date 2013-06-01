
package com.serotonin.m2m2.vo.hierarchy;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.serotonin.m2m2.Common;

public class PointHierarchyEventDispatcher {
    private final static List<PointHierarchyListener> LISTENERS = new CopyOnWriteArrayList<PointHierarchyListener>();

    public static void addListener(PointHierarchyListener l) {
        LISTENERS.add(l);
    }

    public static void removeListener(PointHierarchyListener l) {
        LISTENERS.remove(l);
    }

    public static void firePointHierarchySaved(PointFolder root) {
        for (PointHierarchyListener l : LISTENERS)
            Common.timer.execute(new DispatcherExecution(l, root));
    }

    public static void firePointHierarchyCleared() {
        for (PointHierarchyListener l : LISTENERS)
            Common.timer.execute(new DispatcherExecution(l, null));
    }

    static class DispatcherExecution implements Runnable {
        private final PointHierarchyListener l;
        private final PointFolder root;

        public DispatcherExecution(PointHierarchyListener l, PointFolder root) {
            this.l = l;
            this.root = root;
        }

        @Override
        public void run() {
            if (root == null)
                l.pointHierarchyCleared();
            else
                l.pointHierarchySaved(root);
        }
    }
}
