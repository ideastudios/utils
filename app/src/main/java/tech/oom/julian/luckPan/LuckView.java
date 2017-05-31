package tech.oom.julian.luckPan;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;

import com.pingan.crowdsourcing.R;
import com.pingan.crowdsourcing.common.imageloader.BitmapLoadingListener;
import com.pingan.crowdsourcing.common.imageloader.ImageLoader;



public class LuckView extends View {
    //抽奖奖品相关数据结构
    private LuckBean mLuckBean;

    //当前旋转角度，用于绘制转盘的偏移，形成动画效果
    private int currentDegree = 0;

    //先加速 后减速的插值器
    private AccelerateDecelerateInterpolator mAccelerateDecelerateInterpolator = new AccelerateDecelerateInterpolator();

    //抽奖开始时间 ，currenttime - startTIME >animTime ,表示一次抽奖结束
    private long startTime;

    //一次抽奖的持续时间
    private long animTime = 3000;
    //一次抽奖的旋转角度
    private int allDegree = 0;
    //是否正在抽奖
    private boolean isAnim;

    //抽奖回到接口
    private LuckPanListener mLuckPanListener;

    //抽奖指针
    private Bitmap mArrow = ((BitmapDrawable) getResources().getDrawable(R.mipmap.icon_lottery_arrow)).getBitmap();

    //转盘的bitmap
    private Bitmap mLotteryPan;

    //停下的位置 ，-1 表示抽奖问题了，不用回调
    private int stopIndex = 0;

    //圆盘的绘制区域
    private RectF mCircleRect;

    public LuckView(Context context) {
        super(context);
    }

    public LuckView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LuckView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    public void loadData(LuckBean bean) {
        mLuckBean = bean;
        //正常绘制是从水平开始的 ，指针是竖直的， 所以要旋转 90°
//        currentDegree = 90 + 360 / mLuckBean.details.size() / 2;
        //结束时候的角度 ，整个旋转过程 就是 从 currentDegree --allDegree ，刚好回到原点
        allDegree = currentDegree + 3600;

        ImageView imageView = new ImageView(getContext());
        //下载抽奖奖项圆盘
        ImageLoader.load(getContext(), imageView, mLuckBean.wheel_icon_url, new BitmapLoadingListener() {
            @Override
            public void onSuccess(Bitmap bm) {

                //缩放bitmap，达到跟view的大小一致
                Matrix matrix = new Matrix();
                float scale = mCircleRect.width() / bm.getWidth();
                matrix.setScale(scale, scale);
                mLotteryPan = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, false);

                invalidate();
                if (mLuckPanListener != null) {
                    mLuckPanListener.onLoadSuccess();
                }
            }

            @Override
            public void onError() {
                if (mLuckPanListener != null) {
                    mLuckPanListener.onLoadFailed();
                }
            }
        });
    }


    /**
     * 设置抽奖结果
     * @param index 结果对应的位置
     */
    public void stopIndex(int index) {
        stopIndex = index;

        //根据选中的位置，计算一个偏移量 ，在原有的完整周期上面，加上偏移 就对应到了正确的选项
        allDegree += (360-(index * 360 / mLuckBean.details.size()));
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //测量结束，给予了正方形区域用于绘制才draw
        if (mCircleRect == null) {
            return;
        }

        //没有对应的数据结构，不绘制
        if (mLuckBean == null || mLuckBean.details.size() == 0) {
            return;
        }

        //没有拿到圆盘数据，不绘制
        if (mLotteryPan == null) {
            return;
        }

        //保存当前的画布状态
        canvas.save();
        //旋转画布
        canvas.rotate(currentDegree, mCircleRect.centerX(), mCircleRect.centerY());
        //画奖项转盘
        canvas.drawBitmap(mLotteryPan, 0, 0, null);
        //还原画布
        canvas.restore();
        //画指针
        canvas.drawBitmap(mArrow, (getMeasuredWidth() - mArrow.getWidth()) / 2, (getMeasuredHeight() - mArrow.getHeight()) / 2, null);

        if (isAnim) {
            doCircle();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int centerX = getMeasuredWidth() / 2;
        int centerY = getMeasuredHeight() / 2;

        //还没在Viewtree视图上面绘制出来的时候，不管点击事件
        if (centerX <= 0 || centerY <= 0) {
            return true;
        }

        //宽度位置超过箭头图标区域
        if (Math.abs(event.getX() - centerX) > mArrow.getWidth() / 2) {
            return true;
        }
        //高度位置超过箭头图标区域
        if (Math.abs(event.getY() - centerY) > mArrow.getHeight() / 2) {
            return true;
        }

        //点击在箭头上面
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            onStartViewClick();
        }

        return true;
    }


    /**
     * 设置抽奖的监听回调
     *
     * @param luckPanListener 回调
     */
    public void setLuckPanListener(LuckPanListener luckPanListener) {
        mLuckPanListener = luckPanListener;
    }

    /**
     * 开始动画
     */
    private void onStartViewClick() {

        //之前的动画还没有停止，不响应这个事件
        if (isAnim) {
            return;
        }

        startTime = System.currentTimeMillis();
        isAnim = true;
        if (mLuckPanListener != null) {
            mLuckPanListener.onStartLottery();
        }
        currentDegree = 0;
        allDegree = 3600;
        //出发动画
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (w != 0 && h != 0) {
            mCircleRect = new RectF(0, 0, w, h);
        }

        if (w != h) {
            throw new RuntimeException("抽奖转盘需放置正方形中");
        }
    }

    /**
     * 循环判断，不断的触发重绘方法
     */
    private void doCircle() {
        //动画时间结束，重置字段，回调事件
        if (System.currentTimeMillis() * 1.0 - startTime >= animTime) {
            isAnim = false;
//            currentDegree = 90 + 360 / mLuckBean.details.size() / 2;
            startTime = 0;
            allDegree = currentDegree + 3600;
            if (mLuckPanListener != null && stopIndex != -1) {
                mLuckPanListener.onStopLottery(mLuckBean.details.get(stopIndex));
            }
            return;
        }

        //从插值器中计算最新的旋转角度，形成加速减速效果
        float outPut = mAccelerateDecelerateInterpolator.getInterpolation((float) ((System.currentTimeMillis() * 1.0 - startTime) / animTime));
        currentDegree = (int) (allDegree * outPut);

        //出发重绘
        postInvalidate();
    }


}
