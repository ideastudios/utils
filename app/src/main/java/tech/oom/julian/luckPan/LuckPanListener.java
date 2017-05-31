package tech.oom.julian.luckPan;

/**
 * Created by chenyou729 on 17/3/24.
 */

public interface LuckPanListener {

    /**
     * 转盘数据加载成功回调
     */
    void onLoadSuccess();

    /**
     * 转盘数据加载失败回调
     */
    void onLoadFailed();

    /**
     * 转盘开始旋转 抽奖
     */
    void onStartLottery();

    /**
     * 转盘停止旋转，抽奖结束
     *
     * @param info 抽奖结果
     */
    void onStopLottery(LuckItemInfo info);
}
