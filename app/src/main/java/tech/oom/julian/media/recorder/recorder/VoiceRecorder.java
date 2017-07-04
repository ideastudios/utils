package tech.oom.julian.media.recorder.recorder;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

/**
 * Created by issuser on 2017/6/13 0013.
 */

public class VoiceRecorder {

    private final static int[] sampleRates = {44100, 22050, 11025, 8000};
    /**
     * The interval in which the recorded samples are output to the file Used only in uncompressed mode
     */
    private static final int TIMER_INTERVAL = 120;

    private final VoiceRecorderCallback mCallback;
    private int channelConfig;


    /**
     * Number of channels, sample rate, sample size(size in bits), buffer size,audio source, sample size(see AudioFormat)
     */
    private short nChannels;
    private int sampleRate;
    private short bSamples;
    private int bufferSize;
    private int audioSource;
    private int audioFormat;

    private boolean isRecord = false;

    /**
     * Recorder used for uncompressed recording
     */
    private AudioRecord audioRecorder = null;

    /**
     * Number of frames written to file on each output(only in uncompressed mode)
     */
    private int framePeriod;

    private Thread mThread = null;

    private short[] wave = new short['ŀ'];

    private Runnable RecordRun = new Runnable() {


        public void run() {
            if ((audioRecorder != null) && (audioRecorder.getState() == 1)) {
                Log.i("VoiceRecorder", "run run run");

                try {
                    audioRecorder.stop();
                    audioRecorder.startRecording();
                } catch (Exception e) {
                    e.printStackTrace();
                    mCallback.onRecordedFail(0);
                    audioRecorder = null;
                }
            }
            if ((audioRecorder != null) &&
                    (audioRecorder.getState() == 1) && (audioRecorder.getRecordingState() == 1)) {
                mCallback.onRecordedFail(3);
                audioRecorder = null;
            }

            while (isRecord) {
                int nLen = 0;
                double sum = 0;
                try {
                    nLen = audioRecorder.read(wave, 0, wave.length);
                    Log.i("VoiceRecorder", "read length " + nLen);
                    for (int i = 0; i < nLen; i++) {
                        sum += wave [i] * wave [i];
                    }
                    if (nLen > 0) {
                        final double amplitude = sum / nLen;
                        mCallback.onRecordVolume(Math.sqrt(amplitude));
                    }
                } catch (Exception e) {
                    isRecord = false;
                    mCallback.onRecordedFail(0);
                }
                if (nLen == wave.length) {

                    mCallback.onRecorded(wave);
                } else {
                    mCallback.onRecordedFail(1);
                    isRecord = false;
                }
            }
            Log.i("VoiceRecorder", "out of the loop i don't want to record");
            unInitializeRecord();
            doRecordStop();
        }
    };


    /**
     * @param audioSource   the recording source.
     *                      See {@link MediaRecorder.AudioSource} for the recording source definitions.
     *                      recommend {@link MediaRecorder.AudioSource#MIC}
     * @param sampleRate    the sample rate expressed in Hertz. 44100Hz is currently the only
     *                      rate that is guaranteed to work on all devices, but other rates such as 22050,
     *                      16000, and 11025 may work on some devices.
     *                      {@link AudioFormat#SAMPLE_RATE_UNSPECIFIED} means to use a route-dependent value
     *                      which is usually the sample rate of the source.
     * @param channelConfig describes the configuration of the audio channels.
     *                      See {@link AudioFormat#CHANNEL_IN_MONO} and
     *                      {@link AudioFormat#CHANNEL_IN_STEREO}.  {@link AudioFormat#CHANNEL_IN_MONO} is guaranteed
     *                      to work on all devices.
     * @param audioFormat   the format in which the audio data is to be returned.
     *                      See {@link AudioFormat#ENCODING_PCM_8BIT}, {@link AudioFormat#ENCODING_PCM_16BIT},
     *                      and {@link AudioFormat#ENCODING_PCM_FLOAT}.
     */
    private VoiceRecorder(int audioSource, int sampleRate, int channelConfig, int audioFormat, VoiceRecorderCallback callback) {
        if (audioFormat == AudioFormat.ENCODING_PCM_16BIT) {
            bSamples = 16;
        } else {
            bSamples = 8;
        }

        if (channelConfig == AudioFormat.CHANNEL_IN_MONO) {
            nChannels = 1;
        } else {
            nChannels = 2;
        }
        this.channelConfig = channelConfig;
        this.audioSource = audioSource;
        this.sampleRate = sampleRate;
        this.audioFormat = audioFormat;

        framePeriod = sampleRate * TIMER_INTERVAL / 1000;
        bufferSize = framePeriod * 2 * bSamples * nChannels / 8;
        if (bufferSize < AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)) {
            //  Check to make sure buffer size is not smaller than the smallest allowed one
            bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
            Log.d("VoiceRecorder", "Increasing buffer size to " + Integer.toString(bufferSize));
            // Set frame period and timer interval accordingly
            framePeriod = bufferSize / (2 * bSamples * nChannels / 8);
        }
        mCallback = callback;
    }

    public VoiceRecorder(RecordConfig config, VoiceRecorderCallback callback) {
        this(config.audioSource, config.sampleRate, config.channelConfig, config.audioFormat, callback);
    }

    public void start() {
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
                    }
                }
            }
        }
//        isRecord = false;
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
                if (audioRecorder != null) {
                    unInitializeRecord();
                }
                audioRecorder = new AudioRecord(audioSource, sampleRate, channelConfig, audioFormat, bufferSize);
                if (audioRecorder.getState() != 1) {
                    audioRecorder = null;

                    throw new Exception("AudioRecord initialization failed");
                }
//                audioRecorder.setPositionNotificationPeriod(framePeriod);
//                wave = new short[framePeriod*bSamples/8*nChannels];
                Log.i("VoiceRecorder", "initialize  Record");
                return true;
            } catch (Throwable e) {
                if (e.getMessage() != null) {
                    Log.e("VoiceRecorder", getClass().getName() + e.getMessage());
                } else {
                    Log.e("VoiceRecorder", getClass().getName() + "Unknown error occured while initializing recording");
                }
                Log.e("websocket", "recording error");

                return false;
            }
        }
    }

    private void unInitializeRecord() {
        Log.i("VoiceRecorder", "unInitializeRecord");
        synchronized (this) {
            if (audioRecorder != null) {
                try {
                    audioRecorder.stop();
                    audioRecorder.release();
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("VoiceRecorder", "mAudioRecorder release error!");
                }
                audioRecorder = null;
            }
        }
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


    private boolean doRecordReady() {
        if (mCallback != null) {
            return mCallback.onRecorderReady();
        }
        return true;
    }

    private boolean doRecordStart() {
        if (mCallback != null) {
            return mCallback.onRecorderStart();
        }
        return true;
    }

    private void doRecordStop() {
        if (mCallback != null) {
            mCallback.onRecorderStop();
        }
    }

    public static class RecordConfig {
        int audioSource = MediaRecorder.AudioSource.MIC;
        int sampleRate = 16000;
        int channelConfig = AudioFormat.CHANNEL_IN_MONO;
        int audioFormat = AudioFormat.ENCODING_PCM_16BIT;

        public int getAudioSource() {
            return audioSource;
        }

        /**
         * @param audioSource the recording source.
         *                    See {@link MediaRecorder.AudioSource} for the recording source definitions.
         *                    recommend {@link MediaRecorder.AudioSource#MIC}
         */
        public RecordConfig setAudioSource(int audioSource) {
            this.audioSource = audioSource;
            return this;
        }

        public int getSampleRate() {
            return sampleRate;
        }

        /**
         * @param sampleRate the sample rate expressed in Hertz. 44100Hz is currently the only
         *                   rate that is guaranteed to work on all devices, but other rates such as 22050,
         *                   16000, and 11025 may work on some devices.
         *                   {@link AudioFormat#SAMPLE_RATE_UNSPECIFIED} means to use a route-dependent value
         *                   which is usually the sample rate of the source.
         */
        public RecordConfig setSampleRate(int sampleRate) {
            this.sampleRate = sampleRate;
            return this;
        }

        public int getChannelConfig() {
            return channelConfig;
        }

        /**
         * @param channelConfig describes the configuration of the audio channels.
         *                      See {@link AudioFormat#CHANNEL_IN_MONO} and
         *                      {@link AudioFormat#CHANNEL_IN_STEREO}.  {@link AudioFormat#CHANNEL_IN_MONO} is guaranteed
         *                      to work on all devices.
         */
        public RecordConfig setChannelConfig(int channelConfig) {
            this.channelConfig = channelConfig;
            return this;
        }

        public int getAudioFormat() {
            return audioFormat;
        }

        /**
         * @param audioFormat the format in which the audio data is to be returned.
         *                    See {@link AudioFormat#ENCODING_PCM_8BIT}, {@link AudioFormat#ENCODING_PCM_16BIT},
         *                    and {@link AudioFormat#ENCODING_PCM_FLOAT}.
         */
        public RecordConfig setAudioFormat(int audioFormat) {
            this.audioFormat = audioFormat;
            return this;
        }
    }
}
