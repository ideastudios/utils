package tech.oom.julian.luckPan;

import java.util.List;



public class LuckBean {
    public int lottery_id; //抽奖转盘ID
    public String lottery_desc; //抽奖转盘描述
    public String wheel_icon_url;//转盘url
    public String point_icon_url; //指针url
    public String background_icon_url;// 转盘背景图
    public List<LuckItemInfo> details;
}
