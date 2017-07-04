package tech.oom.julian.media.recorder;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import tech.oom.julian.media.recorder.file.AudioFileHelper;
import tech.oom.julian.media.recorder.file.AudioFileListener;
import tech.oom.julian.media.recorder.recorder.VoiceRecorder;
import tech.oom.julian.media.recorder.recorder.VoiceRecorderCallback;
import tech.oom.julian.media.recorder.utils.BytesTransUtil;

/**
 * Created by issuser on 2017/6/13 0013.
 */

public class Client implements VoiceRecorderCallback, AudioFileListener {

    private Handler myUIHandler;
    private VoiceRecorder voiceRecorder;
    private VoiceRecorder.RecordConfig config;
    private StageListener stageListener;
    private AudioFileHelper audioFileHelper;
    private Context context;
    private long mLastVoiceValueTime;
    private BytesTransUtil bytesTransUtil = BytesTransUtil.getInstance();
    private long volumeInterval;
    private boolean isFileHelperReady;

    private Client(Context context, VoiceRecorder.RecordConfig config, StageListener stageListener) {
        this.context = context;
        this.config = config;
        this.stageListener = stageListener;
        myUIHandler = new Handler();
        voiceRecorder = new VoiceRecorder(config, this);
        audioFileHelper = new AudioFileHelper(this);
    }

    public static Client newInstance(Context context, VoiceRecorder.RecordConfig config, StageListener stageListener) {
        return new Client(context, config, stageListener);
    }

    /**
     * 设置录音保存路径
     *
     * @param path
     */
    public void setRecordFilePath(String path) {
        if (!TextUtils.isEmpty(path) && audioFileHelper != null) {
            if(!isWriteExternalStoragePermissionGranted()){
                runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        if (stageListener != null) {
                            stageListener.onFileSaveFailed("没有SD卡读取权限哦");
                        }
                    }
                });
                return;
            }
            audioFileHelper.setSavePath(path);
            audioFileHelper.setRecordConfig(config);
            isFileHelperReady = true;
        }
    }


    public void start() {
        voiceRecorder.start();
    }

    public void stop() {
        voiceRecorder.immediateStop();
    }

    public void setVolumeInterval(long interval){
        this.volumeInterval = interval;
    }


    private void runOnUIThread(Runnable runnable) {
        myUIHandler.post(runnable);
    }

    @Override
    public boolean onRecorderReady() {
        boolean recordAudioPermissionGranted = isRecordAudioPermissionGranted();
        if (!recordAudioPermissionGranted) {
            runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    if (stageListener != null) {
                        stageListener.onRecordError("没有录音权限哦");
                    }
                }
            });

        }
        return recordAudioPermissionGranted;

    }

    @Override
    public boolean onRecorderStart() {
        if(isFileHelperReady){
            audioFileHelper.start();
        }
        runOnUIThread(new Runnable() {
            @Override
            public void run() {
                if (stageListener != null) {
                    stageListener.onStartRecording(Client.this);
                }
            }
        });
        return true;
    }

    @Override
    public void onRecordedFail(final int errorCode) {
        runOnUIThread(new Runnable() {
            @Override
            public void run() {
                if (stageListener != null) {
                    stageListener.onRecordError("record failed errorCode" + errorCode);
                }
            }
        });

    }

    @Override
    public void onRecorded(final short[] wave) {
        byte[] bytes = bytesTransUtil.Shorts2Bytes(wave);
        if(isFileHelperReady){
            audioFileHelper.save(bytes, 0, bytes.length);
        }

        runOnUIThread(new Runnable() {
            @Override
            public void run() {
                if (stageListener != null) {
                    stageListener.onRecordData(wave, wave.length);
                }
            }
        });
    }



    @Override
    public void onRecorderStop() {
        if(isFileHelperReady){
            audioFileHelper.finish();
        }
        runOnUIThread(new Runnable() {
            @Override
            public void run() {
                if (stageListener != null) {
                    stageListener.onStopRecording(Client.this);
                }
            }
        });
    }

    @Override
    public void onRecordVolume(double volume) {
        onVoiceValue((int)volume);
    }

    private void onVoiceValue(final int value) {
        long lastTime = mLastVoiceValueTime;
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastTime < volumeInterval) {
            return;
        }
        mLastVoiceValueTime = currentTime;
        runOnUIThread(new Runnable() {
            public void run() {
                if (stageListener != null) {
                    stageListener.onRecordVolume(value);
                }
            }
        });
    }


    public boolean isRecordAudioPermissionGranted() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    public boolean isWriteExternalStoragePermissionGranted() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }


    @Override
    public void onFailure(final String reason) {
        runOnUIThread(new Runnable() {
            public void run() {
                if (stageListener != null) {
                    stageListener.onFileSaveFailed(reason);
                }
            }
        });
    }

    @Override
    public void onSuccess(final String savePath) {
        runOnUIThread(new Runnable() {
            public void run() {
                if (stageListener != null) {
                    stageListener.onFileSaveSuccess(savePath);
                }
            }
        });
    }
}
