package tech.oom.julian.media.myrecorder;

/**
 * 录音各种状态的回调类
 */
public class StageListener {
    /**
     * 录音时的buffer
     *
     * @param data PCM Data
     * @param length 长度
     */
    public void onVoiceData(short[] data, int length) {
    }


    /**
     * 录音时的音量
     *
     * @param volume 音量
     */
    public void onVoiceVolume(int volume) {
    }

    /**
     * 开始录音的回调
     *
     * @param recognizer SpeechRecognizer
     */
    public void onStartRecording(SpeechRecognizer recognizer) {
    }

    /**
     * 停止录音的回调
     *
     * @param recognizer SpeechRecognizer
     */
    public void onStopRecording(SpeechRecognizer recognizer) {
    }


    /**@deprecated
     * VAD stop 已经废弃的方法  不会回调
     *
     * @param recognizer SpeechRecognizer
     */
    public void onVadStop(SpeechRecognizer recognizer) {
    }

    /**@deprecated
     * 无效录音 已经废弃的方法  不会回调
     */
    public void onNoneEffectiveRecord() {
    }

    /**
     * 录音失败
     *
     * @param error 错误信息
     */
    public void onRecordError(String error) {
    }

    /**
     * 录音失败
     * @param code 错误码
     * @param errorMsg  错误信息描述
     */
    public void onRecordError(int code ,String errorMsg){

    }

    /**
     * 保存文件失败
     *
     * @param error
     */
    public void onFileSaveFailed(String error) {

    }

    /**
     * 保存录音文件成功
     *
     * @param fileUri 保存文件的路径
     */
    public void onFileSaveSuccess(String fileUri) {

    }

    /**
     * 录音时间太短
     */
    public void onRecordTooShort() {
    }

    /**
     * pcm数据做base64编码后的string
     *
     * @param base64  pcm数据做base64编码后的string
     */
    public void onRecordBase64String(String base64) {

    }
}
