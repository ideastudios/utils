package tech.oom.julian.media.recorder.file;


import android.media.AudioFormat;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import tech.oom.record2.recorder.VoiceRecorder;
import tech.oom.record2.utils.Log;

/**
 * Created by issuser on 2017/6/8 0008.
 */

public class AudioFileHelper {

    public static final String TAG = "AudioFileHelper";
    private AudioFileListener listener;
    private String savePath;
    private RandomAccessFile randomAccessFile;
    private VoiceRecorder.RecordConfig config;
    private short bSamples;
    private short nChannels;
    private int sRate;

    public AudioFileHelper(AudioFileListener listener) {

        this.listener = listener;
        this.config = new VoiceRecorder.RecordConfig();
    }

    public AudioFileHelper setSavePath(String savePath) {

        this.savePath = savePath;
        return this;
    }

    public AudioFileHelper setRecordConfig(VoiceRecorder.RecordConfig config) {

        if (config != null) {
            this.config = config;
        }
        return this;
    }


    public void start() {
        try {

            if (config.getAudioFormat() == AudioFormat.ENCODING_PCM_16BIT) {
                bSamples = 16;
            } else {
                bSamples = 8;
            }

            if (config.getChannelConfig() == AudioFormat.CHANNEL_IN_MONO) {
                nChannels = 1;
            } else {
                nChannels = 2;
            }
            sRate = config.getSampleRate();
            open(savePath);
        } catch (IOException e) {
            e.printStackTrace();
            if (listener != null) {
                listener.onFailure(e.toString());
            }
        }
    }

    public void save(byte[] data, int offset, int size) {
        try {
            write(randomAccessFile, data, offset, size);
        } catch (IOException e) {
            e.printStackTrace();
            if (listener != null) {
                listener.onFailure(e.toString());
            }

        }
    }

    public void finish() {
        try {
            close();
            if (listener != null) {
                listener.onSuccess(savePath);
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (listener != null) {
                listener.onFailure(e.toString());
            }

        }
    }


    private void open(String path) throws IOException {
        if (path == null) {
            return;
        }
        File f = new File(path);

        if (f.exists()) {
            f.delete();
        } else {
//            f.mkdirs();
            File parentDir = f.getParentFile();
            if (!parentDir.exists()) {
                parentDir.mkdirs();
            }
        }

//        randomAccessFile = new RandomAccessFile(f, "rw");
//        randomAccessFile.setLength(0);
//        // Set file length to
//        // 0, to prevent unexpected behavior in case the file already existed
//        // 16K、16bit、单声道
//    /* RIFF header */
//        randomAccessFile.writeBytes("RIFF"); // riff id
//        randomAccessFile.writeInt(0); // riff chunk size *PLACEHOLDER*
//        randomAccessFile.writeBytes("WAVE"); // wave type
//
//    /* fmt chunk */
//        randomAccessFile.writeBytes("fmt "); // fmt id
//        randomAccessFile.writeInt(Integer.reverseBytes(16)); // fmt chunk size
//        randomAccessFile.writeShort(Short.reverseBytes((short) 1)); // format: 1(PCM)
//        randomAccessFile.writeShort(Short.reverseBytes((short) 1)); // channels: 1
//        randomAccessFile.writeInt(Integer.reverseBytes(8000)); // samples per second
//        randomAccessFile.writeInt(Integer.reverseBytes((int) (1 * 8000 * 16 / 8))); // BPSecond
//        randomAccessFile.writeShort(Short.reverseBytes((short) (2 * 16 / 8))); // BPSample
//        randomAccessFile.writeShort(Short.reverseBytes((short) (1 * 16))); // bPSample
//
//    /* data chunk */
//        randomAccessFile.writeBytes("data"); // data id
//        randomAccessFile.writeInt(0); // data chunk size *PLACEHOLDER*


        // write file header
        randomAccessFile = new RandomAccessFile(f, "rw");

        randomAccessFile.setLength(0); // Set file length to
        // 0, to prevent unexpected behavior in case the file already existed
        randomAccessFile.writeBytes("RIFF");
        randomAccessFile.writeInt(0); // Final file size not
        // known yet, write 0
        randomAccessFile.writeBytes("WAVE");
        randomAccessFile.writeBytes("fmt ");
        randomAccessFile.writeInt(Integer.reverseBytes(16)); // Sub-chunk
        // size, 16 for PCM
        randomAccessFile.writeShort(Short.reverseBytes((short) 1)); // AudioFormat,1 for PCM
        randomAccessFile.writeShort(Short.reverseBytes(nChannels));// Number of channels, 1 for mono, 2 for stereo
        randomAccessFile.writeInt(Integer.reverseBytes(sRate)); // Sample rate
        randomAccessFile.writeInt(Integer.reverseBytes(sRate * bSamples * nChannels / 8)); // Byte rate,SampleRate*NumberOfChannels*BitsPerSample/8
        randomAccessFile.writeShort(Short.reverseBytes((short) (nChannels * bSamples / 8))); // Block align, NumberOfChannels*BitsPerSample/8
        randomAccessFile.writeShort(Short.reverseBytes(bSamples)); // Bits per sample
        randomAccessFile.writeBytes("data");
        randomAccessFile.writeInt(0); // Data chunk size not known yet, write 0
        Log.d(TAG, "wav path: " + path);

    }

    private void write(RandomAccessFile file, byte[] data, int offset, int size) throws IOException {
        file.write(data, offset, size);
        Log.d(TAG, "fwrite: " + size);
    }

    private void close() throws IOException {
        try {
            randomAccessFile.seek(4); // riff chunk size
            randomAccessFile.writeInt(Integer.reverseBytes((int) (randomAccessFile.length() - 8)));
            randomAccessFile.seek(40); // data chunk size
            randomAccessFile.writeInt(Integer.reverseBytes((int) (randomAccessFile.length() - 44)));
//            randomAccessFile.seek(4); // Write size to RIFF header
//            randomAccessFile.writeInt(Integer.reverseBytes(36+payloadSize));
//
//            randomAccessFile.seek(40); // Write size to Subchunk2Size field
//            randomAccessFile.writeInt(Integer.reverseBytes(payloadSize));

            Log.d(TAG, "wav size: " + randomAccessFile.length());

        } finally {
            randomAccessFile.close();
            randomAccessFile = null;
        }
    }


}
