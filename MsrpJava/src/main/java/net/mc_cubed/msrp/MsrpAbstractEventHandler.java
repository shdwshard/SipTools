/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.mc_cubed.msrp;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 *
 * @author charles
 */
class MsrpAbstractEventHandler {

    @Inject
    protected Instance<ScheduledExecutorService> exec;

    @Inject
    Event<MsrpEvent> msrpEvent;
    
    protected Set<MsrpEventListener> listeners = new HashSet<MsrpEventListener>();

    protected void fireEvent(MsrpEvent event) {
        // Fire Weld events if we're in a supported environment
        if (msrpEvent != null) {
            msrpEvent.fire(event);
        }

        // Check whether we have an executor service available
        if (exec != null && !exec.isUnsatisfied()) {
            // Throw this off into executors
            for (MsrpEventListener listener : listeners) {
                exec.get().submit(new EventDeliverer(listener,event));
            }
        } else {
            // Do them one at a time
            for (MsrpEventListener listener : listeners) {
                try {
                    listener.eventFired(event);
                } catch (Throwable t) {
                    // Don't interrupt this thread
                    t.printStackTrace();
                }
            }
        }
    }

    protected class EventDeliverer implements Runnable {

        public EventDeliverer(MsrpEventListener listener,MsrpEvent event) {
            this.listener = listener;
            this.event = event;
        }


        protected final MsrpEvent event;

        /**
         * Get the value of event
         *
         * @return the value of event
         */
        public MsrpEvent getEvent() {
            return event;
        }

        protected final MsrpEventListener listener;

        /**
         * Get the value of listener
         *
         * @return the value of listener
         */
        public MsrpEventListener getListener() {
            return listener;
        }

        @Override
        public void run() {
            listener.eventFired(event);
        }

    }

    public void addEventListener(MsrpEventListener listener) {
        listeners.add(listener);
    }

    public void removeEventListener(MsrpEventListener listener) {
        listeners.remove(listener);
    }
}
