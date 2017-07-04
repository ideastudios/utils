package tech.oom.julian.media.recorder;

public class StageListener {

    public void onRecordData(short[] data, int length) {
    }

    public void onRecordVolume(int volume) {
    }

    public void onStartRecording(Client client) {
    }

    public void onStopRecording(Client client) {
    }

    public void onRecordError(String error) {

    }

    public void onFileSaveFailed(String error) {

    }

    public void onFileSaveSuccess(String fileUri) {

    }
}
