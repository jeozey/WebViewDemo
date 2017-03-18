package com.comslin.webviewbackdemo;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import static android.R.attr.action;

/**
 * Created by Jeo{594485991@qq.com} on 2017/1/10.
 * 解决子控件侧滑冲突
 * 参考http://blog.csdn.net/u010386612/article/details/50548977
 */
public class MySwipeRefreshLayout extends SwipeRefreshLayout {
    private static final String TAG = MySwipeRefreshLayout.class.getName();

    public MySwipeRefreshLayout(Context context) {
        super(context);
    }

    private float startY;
    private float startX;
    // 记录子view是否拖拽的标记
    private boolean mIsViewDragger;
    private int mTouchSlop;

    public MySwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //竖向滑动还是冲突
        if(true)return false;


        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                // 记录手指按下的位置
                startY = ev.getY();
                startX = ev.getX();
                // 初始化标记
                mIsViewDragger = false;
                break;
            case MotionEvent.ACTION_MOVE:
                // 如果子view正在拖拽中，那么不拦截它的事件，直接return false；
                if (mIsViewDragger) {
                    return false;
                }

                // 获取当前手指位置
                float endY = ev.getY();
                float endX = ev.getX();
                float distanceX = Math.abs(endX - startX);
                float distanceY = Math.abs(endY - startY);
                // 如果X轴位移大于Y轴位移，那么将事件交给子view处理。
                if (distanceX > mTouchSlop && distanceX > distanceY) {
                    mIsViewDragger = true;
                    return false;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                // 初始化标记
                mIsViewDragger = false;
                break;
        }
        // 如果是Y轴位移大于X轴，事件交给swipeRefreshLayout处理。
        return super.onInterceptTouchEvent(ev);
    }


}
