
package com.serotonin.m2m2.rt.publish;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.vo.publish.PublishedPointVO;


public class PublishQueue<T extends PublishedPointVO> {
    private static final Log LOG = LogFactory.getLog(PublishQueue.class);
    private static final long SIZE_CHECK_DELAY = 5000;

    protected final ConcurrentLinkedQueue<PublishQueueEntry<T>> queue = new ConcurrentLinkedQueue<PublishQueueEntry<T>>();
    private final PublisherRT<T> owner;
    private final int warningSize;
    private final int dewarningSize;
    private final int discardSize;
    private boolean warningActive = false;
    private long lastSizeCheck;

    public PublishQueue(PublisherRT<T> owner, int warningSize, int discardSize) {
        this.owner = owner;
        this.warningSize = warningSize;
        this.dewarningSize = (int) (warningSize * 0.9); // Deactivate the size warning at 90% of the warning size.
        this.discardSize = discardSize;
    }

    public void add(T vo, PointValueTime pvt) {
        queue.add(new PublishQueueEntry<T>(vo, pvt));
        sizeCheck();
    }

    public void add(T vo, List<PointValueTime> pvts) {
        for (PointValueTime pvt : pvts)
            queue.add(new PublishQueueEntry<T>(vo, pvt));
        sizeCheck();
    }

    public PublishQueueEntry<T> next() {
        return queue.peek();
    }

    public List<PublishQueueEntry<T>> get(int max) {
        if (queue.isEmpty())
            return null;

        Iterator<PublishQueueEntry<T>> iter = queue.iterator();
        List<PublishQueueEntry<T>> result = new ArrayList<PublishQueueEntry<T>>(max);
        while (iter.hasNext() && result.size() < max)
            result.add(iter.next());

        return result;
    }

    public void remove(PublishQueueEntry<T> e) {
        queue.remove(e);
        sizeCheck();
    }

    public void removeAll(List<PublishQueueEntry<T>> list) {
        queue.removeAll(list);
        sizeCheck();
    }

    public int getSize() {
        return queue.size();
    }

    private void sizeCheck() {
        long now = System.currentTimeMillis();
        if (lastSizeCheck + SIZE_CHECK_DELAY < now) {
            lastSizeCheck = now;
            int size = queue.size();

            synchronized (owner) {
                if (size > discardSize) {
                    for (int i = discardSize; i < size; i++)
                        queue.remove();
                    LOG.warn("Publisher queue " + owner.getVo().getName() + " discarded " + (size - discardSize)
                            + " entries");
                }

                if (warningActive) {
                    if (size <= dewarningSize) {
                        owner.deactivateQueueSizeWarningEvent();
                        warningActive = false;
                    }
                }
                else {
                    if (size > warningSize) {
                        owner.fireQueueSizeWarningEvent();
                        warningActive = true;
                    }
                }
            }
        }
    }
}
