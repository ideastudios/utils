package tech.oom.julian.luckPan;



public class LuckItemInfo {
    public int record_id; // 抽奖记录id
    public int prize_id; //奖品id
    public int index; //奖品所处位置
    public String prize_name; //奖品名称
    public int prize_type; //奖品类型：1-积分 2-实物
    public int prize_value; //奖品价值
    public String prize_icon_url;//奖品图标
    public String color;
    public int next_order_num;//下一次抽奖需要的任务次数
    public int remaining_lottery_chance;//剩余抽奖机会
    public int config_id;//config_id,用于后期恢复库存
}
