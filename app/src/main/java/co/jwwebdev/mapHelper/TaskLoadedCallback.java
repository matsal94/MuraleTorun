package co.jwwebdev.mapHelper;

public interface TaskLoadedCallback {
    void onTaskDone(Object... values);
    void onTaskCancel();
}