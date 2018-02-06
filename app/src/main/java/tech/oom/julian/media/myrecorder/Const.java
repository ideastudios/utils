package tech.oom.julian.media.myrecorder;

/**
 * SDK的相关常量
 */

public class Const {
    /**
     *获取speechText时的类型
     */
    public static final class SpeechType{
        /**
         * 注册时类型
         */
        public static final String TYPE_REGISTER = "register";
        /**
         * 验证时类型
         */
        public static final String TYPE_VERIFY = "verify";
    }
    /**
     * 注册相关常量
     */
    public static final class RegisterType{
        /**
         * 注册类型
         */
        public static final String TYPE_REGISTER = "register";
        /**
         * 修改注册类型
         */
        public static final String TYPE_MODIFY = "delayModify";

        /**
         * 长语音的修改注册类型
         */
        public static final String TYPE_LONG_MODIFY = "modify";

        /**
         * 第一步
         */
        public static final int STEP_FIRST = 1;
        /**
         * 第二步
         */
        public static final int STEP_SECOND = 2;
        /**
         * 第三步
         */
        public static final int STEP_THIRD = 3;

    }

    /**
     * 场景类型
     */
    public static final class SceneType{
        /**
         * 数字解锁方式
         */
        public static final int SCENETYPE_NUMBER = 0;
        /**
         * 随机数字解锁方式
         */
        public static final int SCENETYPE_RANDOM = 1;
        /**
         * 口号解锁方式
         */
        public static final int SCENETYPE_SLOGAN = 2;
        /**
         * 长语音解锁方式
         */
        public static final int SCENETYPE_LONG_SPEECH = 3;
    }


    /**
     * 网络请求错误码
     */
    public static final class ErrorCode {
        /**
         * json解析错误
         */
        public static final int PARSE_ERROR = 2000;
        /**
         * 未知错误
         */
        public static final int UNKNOWN = 1000;

        /**
         * 网络错误
         */
        public static final int NETWORK_ERROR = 1001;

        /**
         * 请求超时
         */
        public static final int CONNECT_TIMEOUT = 1002;
        /**
         * 协议出错
         */
        public static final int PROTOCOL_ERROR = 1003;

        /**
         * 证书出错
         */
        public static final int SSL_ERROR = 1004;

        /**
         * 未知的主机异常
         */
        public static final int UNKNOWN_HOST = 1005;
    }

    /**
     * 录音错误时的返回码
     */
    public static final class RecorderErrorCode{
        /**
         * 启动或录音时抛出异常
         */
        public static final int RECORDER_EXCEPTION_OCCUR = 0;

        /**
         * Recorder.read 过程中发生错误
         */
        public static final int RECORDER_READ_ERROR = 1;

        /**
         * 当前录音没有权限或者录音功能被占用
         */
        public static final int RECORDER_PERMISSION_ERROR = 3;
    }
}
