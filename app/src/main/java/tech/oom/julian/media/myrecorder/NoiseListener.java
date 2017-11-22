package tech.oom.julian.media.myrecorder;

/**
 * 噪音检测的回调
 */

public class NoiseListener {

    /**
     * 噪音检测开始
     */
    public void onStart() {
    }

    /**
     * 噪音检测结束
     */
    public void onStop() {
    }

    /**
     * 环境是否嘈杂
     * @param isNoisy 环境是否嘈杂
     */
    public void isNoisy(boolean isNoisy) {
    }

    /**
     * 当前的噪音 分贝大小
     * @param db 分贝大小
     */
    public void currentDb(double db) {
    }

    /**
     * 噪音检测错误
     * @param error 噪音检测错误
     */
    public void error(String error){

    }

    /**
     * 噪音检测错误
     * @param code 错误码
     * @param error 错误信息描述
     */
    public void error(int code ,String error){

    }
}
