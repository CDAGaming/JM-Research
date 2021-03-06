package journeymap.client.api.impl;

import journeymap.client.api.event.DisplayUpdateEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;

class DisplayUpdateEventThrottle {
    private final Queue fullscreenQueue;
    private final Queue minimapQueue;
    private final Queue[] queues;
    private final ArrayList<DisplayUpdateEvent> readyEvents;
    private final Comparator<DisplayUpdateEvent> comparator;

    DisplayUpdateEventThrottle() {
        this.fullscreenQueue = new Queue(1000L);
        this.minimapQueue = new Queue(2000L);
        this.queues = new Queue[]{this.fullscreenQueue, this.minimapQueue};
        this.readyEvents = new ArrayList<>(3);
        this.comparator = Comparator.comparingLong(o -> o.timestamp);
    }

    public void add(final DisplayUpdateEvent event) {
        switch (event.uiState.ui) {
            case Fullscreen: {
                this.fullscreenQueue.offer(event);
                break;
            }
            case Minimap: {
                this.minimapQueue.offer(event);
                break;
            }
            default: {
                throw new UnsupportedOperationException("Can't throttle events for UI." + event.uiState.ui);
            }
        }
    }

    public Iterator<DisplayUpdateEvent> iterator() {
        final long now = System.currentTimeMillis();
        for (final Queue queue : this.queues) {
            if (queue.lastEvent != null && now >= queue.releaseTime) {
                this.readyEvents.add(queue.remove());
            }
        }
        if (this.readyEvents.size() > 0) {
            this.readyEvents.sort(this.comparator);
        }
        return this.readyEvents.iterator();
    }

    public boolean isReady() {
        final long now = System.currentTimeMillis();
        for (final Queue queue : this.queues) {
            if (queue.lastEvent != null && now >= queue.releaseTime) {
                return true;
            }
        }
        return false;
    }

    class Queue {
        private final long delay;
        private DisplayUpdateEvent lastEvent;
        private boolean throttleNext;
        private long releaseTime;

        Queue(final long delay) {
            this.delay = delay;
        }

        void offer(final DisplayUpdateEvent event) {
            if (this.releaseTime == 0L && this.lastEvent != null) {
                this.releaseTime = System.currentTimeMillis() + this.delay;
            }
            this.lastEvent = event;
        }

        DisplayUpdateEvent remove() {
            final DisplayUpdateEvent event = this.lastEvent;
            this.lastEvent = null;
            this.releaseTime = 0L;
            return event;
        }
    }
}
