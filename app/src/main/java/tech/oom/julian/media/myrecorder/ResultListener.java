package tech.oom.julian.media.myrecorder;

/**
 * 请求结果的回调
 */

public class ResultListener {


    /**
     * 请求成功的结果
     *
     * @param result 请求成功的结果
     */
    public void onResult(String result) {

    }

    /**
     * 无可用的网络回调
     */
    public void onNetworkUnavailable() {

    }

    /**
     * 请求失败的信息
     *
     * @param errorCode 错误码 请参考HTTP Response Status Code 和SdkConst.ErrorCode
     * @param errorMsg  错误信息
     */
    public void onFailed(int errorCode, String errorMsg) {

    }


}
