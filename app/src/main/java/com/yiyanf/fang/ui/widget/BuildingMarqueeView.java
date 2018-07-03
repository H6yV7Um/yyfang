package com.yiyanf.fang.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.AnimRes;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.yiyanf.fang.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;

/**
 * 垂直滚动广告条效果
 */
public class BuildingMarqueeView extends ViewFlipper {

    private int interval = 3000;
    private boolean hasSetAnimDuration = false;
    private int animDuration = 1000;


    @AnimRes
    private int inAnimResId = R.anim.anim_bottom_in;
    @AnimRes
    private int outAnimResId = R.anim.anim_top_out;

    private int position;
    private List<String> notices = new ArrayList<>();

    public BuildingMarqueeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MarqueeViewStyle, defStyleAttr, 0);

        interval = typedArray.getInteger(R.styleable.MarqueeViewStyle_mvInterval, interval);
        hasSetAnimDuration = typedArray.hasValue(R.styleable.MarqueeViewStyle_mvAnimDuration);
        animDuration = typedArray.getInteger(R.styleable.MarqueeViewStyle_mvAnimDuration, animDuration);

        typedArray.recycle();
        setFlipInterval(interval);
    }

    /**
     * 根据字符串列表，启动翻页公告
     *
     * @param notices 字符串列表
     */
    public void startWithList(List<String> notices) {
        startWithList(notices, inAnimResId, outAnimResId);
    }

    /**
     * 根据字符串列表，启动翻页公告
     *
     * @param notices      字符串列表
     * @param inAnimResId  进入动画的resID
     * @param outAnimResID 离开动画的resID
     */
    public void startWithList(List<String> notices, @AnimRes int inAnimResId, @AnimRes int outAnimResID) {
        if (null == notices || notices.isEmpty()) {
            return;
        }
        setNotices(notices);
        start(inAnimResId, outAnimResID);
    }

    private boolean start(@AnimRes int inAnimResId, @AnimRes int outAnimResID) {
        removeAllViews();
        clearAnimation();

        position = 0;
        addView(createView(notices.get(position)));

        if (notices.size() > 1) {
            setInAndOutAnimation(inAnimResId, outAnimResID);
            startFlipping();
        }

        if (getInAnimation() != null) {
            getInAnimation().setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    position++;
                    if (position >= notices.size()) {
                        position = 0;
                    }
                    View view = createView(notices.get(position));
                    if (view.getParent() == null) {
                        addView(view);
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
        }
        return true;
    }

    private View createView(String str) {
        View mParentView = LayoutInflater.from(getContext()).inflate(R.layout.layout_building_marquee,null);
        TextView mTitle = ButterKnife.findById(mParentView,R.id.forecast_title);
        TextView mTime = ButterKnife.findById(mParentView,R.id.tv_time);
        TextView mAuthor = ButterKnife.findById(mParentView,R.id.tv_author);

        mTitle.setText(str);
        mTime.setText("时间："+str);
        mAuthor.setText(str);
        mParentView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(),"dianjin",Toast.LENGTH_LONG).show();
            }
        });
        return mParentView;
    }

    public int getPosition() {
        return (int) getCurrentView().getTag();
    }

    public List<String> getNotices() {
        return notices;
    }

    public void setNotices(List<String> notices) {
        this.notices = notices;
    }

    /**
     * 设置进入动画和离开动画
     *
     * @param inAnimResId  进入动画的resID
     * @param outAnimResID 离开动画的resID
     */
    private void setInAndOutAnimation(@AnimRes int inAnimResId, @AnimRes int outAnimResID) {
        Animation inAnim = AnimationUtils.loadAnimation(getContext(), inAnimResId);
        if (hasSetAnimDuration) inAnim.setDuration(animDuration);
        setInAnimation(inAnim);

        Animation outAnim = AnimationUtils.loadAnimation(getContext(), outAnimResID);
        if (hasSetAnimDuration) outAnim.setDuration(animDuration);
        setOutAnimation(outAnim);
    }

}
