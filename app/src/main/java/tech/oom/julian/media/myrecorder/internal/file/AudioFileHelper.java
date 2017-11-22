package tech.oom.julian.media.myrecorder.internal.file;

import android.text.TextUtils;


import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import tech.oom.julian.media.myrecorder.internal.utils.Log;

/**
 *
 */

public class AudioFileHelper {

    public static final String TAG = "AudioFileHelper";
    private AudioFileListener listener;
    private String savePath;
    private RandomAccessFile randomAccessFile;
    private File targetFile;

    public AudioFileHelper(AudioFileListener listener){
        this.listener = listener;
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }


    public void start() {
        try {
            open(savePath);
        } catch (IOException e) {
            e.printStackTrace();
            if (listener != null) {
                listener.onFailure(e.toString());
            }
        }
    }

    public void save(byte[] data, int offset, int size) {
        if(randomAccessFile == null ){
            return;
        }
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

        } catch (IOException e) {
            e.printStackTrace();
            if (listener != null) {
                listener.onFailure(e.toString());
            }

        }
    }



    private void open(String path) throws IOException {
        if(TextUtils.isEmpty(path)){
            return;
        }
        targetFile = new File(path);

        if (targetFile.exists()) {
            targetFile.delete();
        } else {
            File parentDir = targetFile.getParentFile();
            if (!parentDir.exists()) {
                parentDir.mkdirs();
            }
        }

        randomAccessFile = new RandomAccessFile(targetFile, "rw");
        randomAccessFile.setLength(0);
        // Set file length to
        // 0, to prevent unexpected behavior in case the file already existed
        // 16K、16bit、单声道
    /* RIFF header */
        randomAccessFile.writeBytes("RIFF"); // riff id
        randomAccessFile.writeInt(0); // riff chunk size *PLACEHOLDER*
        randomAccessFile.writeBytes("WAVE"); // wave type

    /* fmt chunk */
        randomAccessFile.writeBytes("fmt "); // fmt id
        randomAccessFile.writeInt(Integer.reverseBytes(16)); // fmt chunk size
        randomAccessFile.writeShort(Short.reverseBytes((short) 1)); // format: 1(PCM)
        randomAccessFile.writeShort(Short.reverseBytes((short) 1)); // channels: 1
        randomAccessFile.writeInt(Integer.reverseBytes(8000)); // samples per second 采样率
        randomAccessFile.writeInt(Integer.reverseBytes((int) (1 * 8000 * 16 / 8))); // BPSecond
        randomAccessFile.writeShort(Short.reverseBytes((short) (1 * 16 / 8))); // BPSample
        randomAccessFile.writeShort(Short.reverseBytes((short) (1 * 16))); // bPSample

    /* data chunk */
        randomAccessFile.writeBytes("data"); // data id
        randomAccessFile.writeInt(0); // data chunk size *PLACEHOLDER*

        Log.d(TAG, "wav path: " + path);

    }

    private void write(RandomAccessFile file, byte[] data, int offset, int size) throws IOException {
        file.write(data, offset, size);
//        Log.d(TAG, "fwrite: " + size);
    }

    private void close() throws IOException {
        try {
            if(randomAccessFile == null){
                if (listener != null) {
                    listener.onFailure("recorder error");
                }
                return;
            }
            randomAccessFile.seek(4); // riff chunk size
            randomAccessFile.writeInt(Integer.reverseBytes((int) (randomAccessFile.length() - 8)));
            randomAccessFile.seek(40); // data chunk size
            randomAccessFile.writeInt(Integer.reverseBytes((int) (randomAccessFile.length() - 44)));

            Log.d(TAG, "wav size: " + randomAccessFile.length());
            if (listener != null) {
                listener.onSuccess(savePath);
            }

        } finally {
            if(randomAccessFile != null){
                randomAccessFile.close();
                randomAccessFile = null;
            }

        }
    }

    public void cancel(){
        if(randomAccessFile == null){
            return;
        }
        if(targetFile == null){
            return;
        }
        if(targetFile.exists()){
            targetFile.delete();
        }
        randomAccessFile = null;
        targetFile = null;

    }


}
