package me.kgaz.tasks;

public interface Tickable {

    public CancelState cancelled = new CancelState();

    void run();

    default int getPeriod(){
        return 1;
    }

    public default void cancel() {

        cancelled.cancel();
    }

    public default boolean isCancelled(){
        return cancelled.isCancelled;
    }

    public static class CancelState {

        private boolean isCancelled = false;

        public CancelState isCancelled() {
            return cancelled;
        }

        public void cancel(){
            isCancelled = true;
        }

    }

}
