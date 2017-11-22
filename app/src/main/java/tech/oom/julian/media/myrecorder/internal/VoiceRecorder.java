package tech.oom.julian.media.myrecorder.internal;

import android.media.AudioRecord;

import tech.oom.julian.media.myrecorder.SdkConst;
import tech.oom.julian.media.myrecorder.internal.record.VoiceRecorderCallback;
import tech.oom.julian.media.myrecorder.internal.utils.Log;


public class VoiceRecorder {
    private static final String TAG = "VoiceRecorder";
    private AudioRecord mAudioRecorder = null;
    private VoiceRecorderCallback mCallback;
    private int sRate;
    private int bufferSize;
    private int channel;
    private int aSource;
    private int aFormat;
    private boolean isRecord = false;
    private Thread mThread = null;
    private Runnable RecordRun = new Runnable() {
        short[] wave = new short[320];

        public void run() {
            if ((mAudioRecorder != null) && (mAudioRecorder.getState() == 1)) {

                try {
                    mAudioRecorder.stop();
                    mAudioRecorder.startRecording();
                } catch (Exception e) {
                    e.printStackTrace();
                    recordFailed(SdkConst.RecorderErrorCode.RECORDER_EXCEPTION_OCCUR);
                    mAudioRecorder = null;
                }
            }
            if ((mAudioRecorder != null) &&
                    (mAudioRecorder.getState() == 1) && (mAudioRecorder.getRecordingState() == 1)) {
                Log.e("VoiceRecorder", "no recorder permission or recorder is not available right now");
                recordFailed(SdkConst.RecorderErrorCode.RECORDER_PERMISSION_ERROR);
                mAudioRecorder = null;
            }
            for (int i = 0; i < 2; i++) {
                if (mAudioRecorder == null) {
                    isRecord = false;
                    break;
                }
                mAudioRecorder.read(wave, 0, wave.length);
            }
            while (isRecord) {
                int nLen = 0;
                try {
                    nLen = mAudioRecorder.read(wave, 0, wave.length);

                } catch (Exception e) {
                    isRecord = false;
                    recordFailed(SdkConst.RecorderErrorCode.RECORDER_EXCEPTION_OCCUR);
                }
                if (nLen == wave.length) {
                    mCallback.onRecorded(wave);
                } else {
                    recordFailed(SdkConst.RecorderErrorCode.RECORDER_READ_ERROR);
                    isRecord = false;
                }
            }
            Log.i("VoiceRecorder", "out of the loop i don't want to record");
            unInitializeRecord();
            doRecordStop();
        }
    };


    public VoiceRecorder(int audioSource, int sampleRate, int channelConfig, int audioFormat, VoiceRecorderCallback callback) {
        aSource = audioSource;
        sRate = sampleRate;
        aFormat = audioFormat;
        channel = channelConfig;
        bufferSize = 3840;
        mCallback = callback;
    }


    public boolean start() {
        isRecord = true;
        synchronized (this) {
            if (doRecordReady()) {
                Log.d("VoiceRecorder", "doRecordReady");
                if (initializeRecord()) {
                    Log.d("VoiceRecorder", "initializeRecord");
                    if (doRecordStart()) {
                        Log.d("VoiceRecorder", "doRecordStart");

                        mThread = new Thread(RecordRun);
                        mThread.start();
                        return true;
                    }
                }
            }
        }
        isRecord = false;
        return false;
    }


    public void stop() {
        synchronized (this) {
            mThread = null;
            isRecord = false;
        }
    }

    public void immediateStop() {
        isRecord = false;
        if (mThread != null) {
            try {
                mThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        mThread = null;
    }

    public boolean isStarted() {
        return isRecord;
    }

    private boolean initializeRecord() {
        synchronized (this) {
            try {
                if (mCallback == null) {
                    Log.e("VoiceRecorder", "Error VoiceRecorderCallback = null");
                    return false;
                }
                int nMinSize = AudioRecord.getMinBufferSize(sRate, channel, aFormat);
                if (bufferSize < nMinSize) {
                    bufferSize = nMinSize;

                    Log.d("VoiceRecorder", "Increasing buffer size to " + Integer.toString(bufferSize));
                }
                if (mAudioRecorder != null) {
                    unInitializeRecord();
                }
                mAudioRecorder = new AudioRecord(aSource, sRate, channel, aFormat, bufferSize);
                if (mAudioRecorder.getState() != 1) {
                    mAudioRecorder = null;
                    recordFailed(SdkConst.RecorderErrorCode.RECORDER_PERMISSION_ERROR);
                    Log.e("VoiceRecorder", "AudioRecord initialization failed,because of no RECORD permission or unavailable AudioRecord ");
                    throw new Exception("AudioRecord initialization failed");
                }
                mAudioRecorder.setPositionNotificationPeriod(3200);
                Log.i("VoiceRecorder", "initialize  Record");
                return true;
            } catch (Throwable e) {
                if (e.getMessage() != null) {
                    Log.e("VoiceRecorder", getClass().getName() + e.getMessage());
                } else {
                    Log.e("VoiceRecorder", getClass().getName() + "Unknown error occured while initializing recording");
                }
                return false;
            }
        }
    }

    private void unInitializeRecord() {
        Log.i("VoiceRecorder", "unInitializeRecord");
        synchronized (this) {
            if (mAudioRecorder != null) {
                try {
                    mAudioRecorder.stop();
                    mAudioRecorder.release();
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("VoiceRecorder", "mAudioRecorder release error!");
                }
                mAudioRecorder = null;
            }
        }
    }

    private boolean doRecordStart() {
        if (mCallback != null) {
            return mCallback.onRecorderStart();
        }
        return true;
    }

    private boolean doRecordReady() {
        if (mCallback != null) {
            return mCallback.onRecorderReady();
        }
        return true;
    }

    private void doRecordStop() {
        if (mCallback != null) {
            mCallback.onRecorderStop();
        }
    }

    private void recordFailed(int errorCode){
        if (mCallback != null) {
            mCallback.onRecordedFail(errorCode);
        }
    }
}
