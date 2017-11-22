package tech.oom.julian.media.myrecorder;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import tech.oom.julian.media.myrecorder.internal.VoiceRecorder;
import tech.oom.julian.media.myrecorder.internal.file.AudioFileHelper;
import tech.oom.julian.media.myrecorder.internal.file.AudioFileListener;
import tech.oom.julian.media.myrecorder.internal.record.VoiceRecorderCallback;
import tech.oom.julian.media.myrecorder.internal.utils.BytesTransUtil;
import tech.oom.julian.media.myrecorder.internal.utils.Log;


/**
 * 声纹注册验证核心类
 */

public class SpeechRecognizer implements VoiceRecorderCallback, AudioFileListener {

    private static SpeechRecognizer instance = null;
    private static Context context;
    private final VoiceRecorder mVoiceRecorder;
    private StageListener stageListener;
    private Handler myUIHandler;
    private AudioFileHelper audioFileHelper;
    private boolean isAudioFileHelperInit;
    private AtomicBoolean mIsStarted = new AtomicBoolean(false);
    private long minimalRecordTime = 500;
    private int mMinVoiceValueInterval = 200;
    private long recordStartTime = 0;
    private long mLastVoiceValueTime = 0L;
    private ByteArrayOutputStream bao = new ByteArrayOutputStream();
    private NoiseListener noiseListener;
    private boolean isNoiseDetectRunning;
    private long noseLastTime;
    private int noiseCount;
    private int noiseDbThreshold = 45;
    private int noiseCountThreshold = 20;
    private long maxRecordTime = 5000L;
    private StageListener noiseStageListener = new StageListener() {
        boolean isNoiseDetectError = false;
        @Override
        public void onVoiceData(short[] data, int length) {
            long v = 0;
            // 将 buffer 内容取出，进行平方和运算
            for (int i = 0; i < data.length; i++) {
                v += data[i] * data[i];
            }
            // 平方和除以数据总长度，得到音量大小。
            double mean = v / (double) length;
            final double volume = 10 * Math.log10(mean);
            if (System.currentTimeMillis() - noseLastTime >= 100) {
                if (Math.round(volume) > noiseDbThreshold) {
                    noiseCount++;
                }
                noseLastTime = System.currentTimeMillis();
                runOnUIThread(new Runnable() {
                    public void run() {
                        if (noiseListener != null) {
                            noiseListener.currentDb(volume);
                        }
                    }
                });
            }


        }

        @Override
        public void onStartRecording(SpeechRecognizer recognizer) {
            noiseCount = 0;
            isNoiseDetectRunning = true;
            isNoiseDetectError = false;
            runOnUIThread(new Runnable() {
                public void run() {
                    if (noiseListener != null) {
                        noiseListener.onStart();
                    }
                }
            });

        }

        @Override
        public void onStopRecording(SpeechRecognizer recognizer) {
            boolean isNoise = false;
            if (noiseCount > noiseCountThreshold) {
                isNoise = true;
            }
            final boolean finalIsNoise = isNoise;

            runOnUIThread(new Runnable() {
                public void run() {
                    if (noiseListener != null&&!isNoiseDetectError) {
                        noiseListener.isNoisy(finalIsNoise);
                    }
                }
            });
            runOnUIThread(new Runnable() {
                public void run() {
                    if (noiseListener != null) {
                        noiseListener.onStop();
                    }
                }
            });
            isNoiseDetectRunning = false;
        }

        @Override
        public void onRecordError(String error) {
            isNoiseDetectRunning = false;
            isNoiseDetectError = true;
            if (noiseListener != null) {
                noiseListener.error(error);
            }

        }

        @Override
        public void onRecordError(int code, String errorMsg) {
            isNoiseDetectRunning = false;
            if (noiseListener != null) {
                noiseListener.error(code, errorMsg);
            }

        }
    };


    private SpeechRecognizer() {
        instance = this;
        myUIHandler = new Handler();
//        this.mVoiceActDetector = new VoiceActDetector(this);
        this.mVoiceRecorder = new VoiceRecorder(1, 8000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, this);
        audioFileHelper = new AudioFileHelper(this);

    }



    /**
     * 获取当前应用的context
     *
     * @return 当前应用的context
     */
    public static Context getContext() {
        if (context == null)
            throw new IllegalStateException("请先在全局Application中调用 SpeechRecognizer.init() 初始化！");
        return context;
    }

    /**
     * 获取SpeechRecognizer实例
     *
     * @return SpeechRecognizer实例
     */
    public static SpeechRecognizer getInstance() {
        return SpeechRecognizerHolder.instance;
    }

    /**
     * 设置是否打印相关日志
     *
     * @param isEnable
     */
    public void setLogEnable(boolean isEnable) {
        Log.DEBUG = isEnable;

    }




    /**
     * 设置最短录音时间
     *
     * @param minRecordTime 最短录音时间 单位 毫秒
     * @return SpeechRecognizer实例
     */
    public SpeechRecognizer setMinRecordTime(int minRecordTime) {
        this.minimalRecordTime = minRecordTime;
        return this;
    }

    /**
     * 设置最长语音
     *
     * @param maxRecordTime 最长录音时间 单位 毫秒
     * @return
     */
    public SpeechRecognizer setMaxRecordTime(int maxRecordTime) {
        this.maxRecordTime = maxRecordTime;
        return this;
    }


    /**
     * 设置音量回调时长 单位毫秒
     *
     * @param interval
     * @return
     */
    public SpeechRecognizer setMinVoiceValueInterval(int interval) {
        this.mMinVoiceValueInterval = interval;
        return this;
    }

    /**
     * 设置录音保存路径
     *
     * @param path
     */
    public void setRecordFilePath(String path) {
        if (!TextUtils.isEmpty(path) && audioFileHelper != null) {
            if (!isWriteExternalStoragePermissionGranted()) {
                Log.e("SpeechRecognizer", "set recorder file path failed,because no WRITE_EXTERNAL_STORAGE permission was granted");
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
            isAudioFileHelperInit = true;
            audioFileHelper.setSavePath(path);
        }else {
            isAudioFileHelperInit = false;
            audioFileHelper.setSavePath(null);
        }
    }

    /**
     * 设置录音状态监听
     *
     * @param listener
     */
    public void setStageListener(StageListener listener) {
        this.stageListener = listener;
    }

    public void runOnUIThread(Runnable runnable) {
        myUIHandler.post(runnable);
    }

    /**
     * 开始录音
     *
     * @return
     */
    public boolean start() {

        if (mIsStarted.compareAndSet(false, true)) {
            bao.reset();
            recordStartTime = System.currentTimeMillis();
            mVoiceRecorder.start();
            Log.d("SpeechRecognizer", "engine Started");
            return true;
        } else {
            Log.e("SpeechRecognizer", "start failed,because engine already started");
            return false;

        }

    }

    /**
     * 停止录音
     */
    public void stop() {
        Log.d("SpeechRecognizer", "stop is called");
        if (this.mIsStarted.get()) {
            if (System.currentTimeMillis() - recordStartTime < minimalRecordTime) {
                Log.e("SpeechRecognizer", "recordTooShort");
                runOnUIThread(new Runnable() {
                    public void run() {
                        if (stageListener != null) {
                            stageListener.onRecordTooShort();
                        }
                    }
                });
                this.mIsStarted.set(false);
                this.mVoiceRecorder.immediateStop();

            } else {
                this.mIsStarted.set(false);
                this.mVoiceRecorder.immediateStop();

            }
        } else if (this.mVoiceRecorder != null) {
            this.mVoiceRecorder.immediateStop();
        }
    }

    /**
     * 判断是否有录音权限
     *
     * @return
     */
    public boolean isRecordAudioPermissionGranted() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * 判断是否有读写存储权限
     *
     * @return
     */
    public boolean isWriteExternalStoragePermissionGranted() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * 判断是否准备好录音
     *
     * @return
     */
    @Override
    public boolean onRecorderReady() {
        if (!isRecordAudioPermissionGranted()) {
            Log.e("SpeechRecognizer", "set recorder failed,because no RECORD_AUDIO permission was granted");
            onRecordedFail(SdkConst.RecorderErrorCode.RECORDER_PERMISSION_ERROR);
//            runOnUIThread(new Runnable() {
//                @Override
//                public void run() {
//                    if (stageListener != null) {
//                        stageListener.onRecordError(SdkConst.RecorderErrorCode.RECORDER_PERMISSION_ERROR, "没有录音权限或已被占用");
//                        stageListener.onRecordError("没有录音权限哦");
//                    }
//                }
//            });


        }
        return isRecordAudioPermissionGranted();
    }

    /**
     * 录音开始
     */
    @Override
    public boolean onRecorderStart() {
        if (isAudioFileHelperInit) {
            audioFileHelper.start();
        }

        runOnUIThread(new Runnable() {
            public void run() {
                if (stageListener != null) {
                    stageListener.onStartRecording(SpeechRecognizer.this);
                }
                Log.d("SpeechRecognizer", "onRecorderStart");
            }
        });
        return true;
    }

    /**
     * 录音正常停止
     */
    @Override
    public void onRecorderStop() {
        mIsStarted.set(false);
        if (isAudioFileHelperInit) {
            audioFileHelper.finish();
        }

        runOnUIThread(new Runnable() {
            @Override
            public void run() {
                if (stageListener != null) {
                    stageListener.onStopRecording(SpeechRecognizer.this);
                    stageListener.onRecordBase64String(Base64.encodeToString(bao.toByteArray(), Base64.DEFAULT));
                }
            }
        });
    }

    @Override
    public void onRecorded(final short[] wave) {
        byte[] bytes = BytesTransUtil.getInstance().Shorts2Bytes(wave);
        if (isAudioFileHelperInit) {

            audioFileHelper.save(bytes, 0, bytes.length);
        }
        bao.write(bytes, 0, bytes.length);
        onVoiceValue(calculateVolume(wave));
        runOnUIThread(new Runnable() {
            @Override
            public void run() {
                if (stageListener != null) {
                    stageListener.onVoiceData(wave, wave == null ? 0 : wave.length);
                }
            }
        });

        if (System.currentTimeMillis() - recordStartTime > maxRecordTime) {
            this.mVoiceRecorder.stop();
            mIsStarted.set(false);
        }
    }

    @Override
    public void onVoiceValue(final int paramInt) {
        long lastTime = this.mLastVoiceValueTime;
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastTime < this.mMinVoiceValueInterval) {
            return;
        }
        this.mLastVoiceValueTime = currentTime;
        runOnUIThread(new Runnable() {
            public void run() {
                if (stageListener != null) {
                    stageListener.onVoiceVolume(paramInt);
                }
            }
        });
    }

    /**
     * 录音失败
     *
     * @param paramInt
     */
    @Override
    public void onRecordedFail(final int paramInt) {
        if (this.mIsStarted.get()) {
            this.mIsStarted.set(false);
            this.mVoiceRecorder.stop();
        }
        if (isAudioFileHelperInit) {

            audioFileHelper.cancel();
        }
        runOnUIThread(new Runnable() {
            public void run() {
                String errorMsg = "";
                switch (paramInt) {
                    case SdkConst.RecorderErrorCode.RECORDER_EXCEPTION_OCCUR:
                        errorMsg = "启动或录音时抛出异常Exception";
                        break;
                    case SdkConst.RecorderErrorCode.RECORDER_READ_ERROR:
                        errorMsg = "Recorder.read() 过程中发生错误";
                        break;
                    case SdkConst.RecorderErrorCode.RECORDER_PERMISSION_ERROR:
                        errorMsg = "当前应用没有录音权限或者录音功能被占用";
                        break;
                    default:
                        errorMsg = "未知错误";
                }
                if (stageListener != null) {
                    stageListener.onRecordError("" + paramInt);
                    stageListener.onRecordError(paramInt, errorMsg);
                }
            }
        });

    }


    /**
     * 保存文件失败
     */
    @Override
    public void onFailure(final String reason) {

        Log.d("SpeechRecognizer", "save record file failure, this reason is " + reason);

        runOnUIThread(new Runnable() {
            public void run() {
                if (stageListener != null) {
                    stageListener.onFileSaveFailed(reason);
                }
            }
        });
    }

    /**
     * 保存文件成功
     */
    @Override
    public void onSuccess(final String savePath) {
        Log.d("SpeechRecognizer", "save record file success, the file path is" + savePath);
        runOnUIThread(new Runnable() {
            public void run() {
                if (stageListener != null) {
                    stageListener.onFileSaveSuccess(savePath);
                }
            }
        });

    }

    private int calculateVolume(short[] wave) {
        long v = 0;
        // 将 buffer 内容取出，进行平方和运算
        for (int i = 0; i < wave.length; i++) {
            v += wave[i] * wave[i];
        }
        // 平方和除以数据总长度，得到音量大小。
        double mean = v / (double) wave.length;
        double volume = 10 * Math.log10(mean);
        return (int) volume;
    }



    /**
     * 开始噪音检测的接口
     *
     * @param noiseListener 噪音回调接口
     */
    public void startNoiseDetection(NoiseListener noiseListener) {

        this.noiseListener = noiseListener;
        setStageListener(noiseStageListener);
        setMinVoiceValueInterval(100);
        setMinRecordTime(2000);
        setMaxRecordTime(4000);
        setRecordFilePath(null);
        if (mIsStarted.compareAndSet(false, true)) {
            bao.reset();
            recordStartTime = System.currentTimeMillis();
            isNoiseDetectRunning = true;
            mVoiceRecorder.start();
        } else {
            if (isNoiseDetectRunning) {
                Log.e("SpeechRecognizer", "start noise detection failed , because noise detection is running");
            } else {
                Log.e("SpeechRecognizer", "start noise detection failed , because the recorder is recording");
            }

        }

    }

    /**
     * 停止噪音检测的接口 停止后噪音检测的方法不会回调
     */
    public void stopNoiseDetection() {
        if (isNoiseDetectRunning) {
            this.noiseListener = null;
            stop();
        } else {
            Log.e("SpeechRecognizer", "stop noise detection failed , because noise detection is not started");
        }

    }

    /**
     * 设置噪音检测的音量大小的阈值  超过该值则判定为noisy  每100ms采样一次
     *
     * @param noiseDbThreshold 音量大小的阈值 默认值为45
     */
    public void setNoiseDbThreshold(int noiseDbThreshold) {
        this.noiseDbThreshold = noiseDbThreshold;
    }

    /**
     * 设置噪音检测过程中 判断为noisy的count 如果统计的值超过该阈值 则返回噪音检测结果noisy 为true  每100ms采样一次
     *
     * @param noiseCountThreshold noisy count 的阈值 默认值为20
     */
    public void setNoiseCountThreshold(int noiseCountThreshold) {
        this.noiseCountThreshold = noiseCountThreshold;
    }

    private static class SpeechRecognizerHolder {
        private final static SpeechRecognizer instance = new SpeechRecognizer();
    }
}
