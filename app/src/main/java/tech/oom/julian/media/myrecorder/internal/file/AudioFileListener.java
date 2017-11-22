package tech.oom.julian.media.myrecorder.internal.file;

/**
 * Created by issuser on 2017/6/8 0008.
 */

public interface AudioFileListener {
    void onFailure(String reason);
    void onSuccess(String savePath);
}
