package tech.oom.julian.media.recorder.recorder;

/**
 * Created by issuser on 2017/6/13 0013.
 */

public interface VoiceRecorderCallback {

    boolean onRecorderReady();

    boolean onRecorderStart();

    void onRecordedFail(int errorCode);

    void onRecorded(short[] wave);

    void onRecorderStop();

    void onRecordVolume(double volume);

}
