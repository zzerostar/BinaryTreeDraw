package com.zero.binarytreedraw.widget;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.zero.binarytreedraw.R;
import com.zero.binarytreedraw.datastruture.BiNode;
import com.zero.binarytreedraw.datastruture.BiTree;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * @author zz
 * @email zzerostar@163.com
 * @date 2018/3/28
 */
public class BiTreeView extends View implements Animator.AnimatorListener {

    private static final String TAG = "BiTreeView";

    @IntDef({ STATE_NORMAL, STATE_PRE_ORDER_TRAVERSAL, STATE_IN_ORDER_TRAVERSAL, STATE_POST_ORDER_TRAVERSAL })
    private @interface State {
    }

    private static final int STATE_NORMAL = -1;
    private static final int STATE_PRE_ORDER_TRAVERSAL = 1;
    private static final int STATE_IN_ORDER_TRAVERSAL = 2;
    private static final int STATE_POST_ORDER_TRAVERSAL = 3;
    private static final int STATE_LEVEL_TRAVERSAL = 4;

    private @State int state = STATE_NORMAL;

    private int mCircleRadius;
    private int xGap;
    private int yGap;
    private int topAndBottomOffset;

    private int textSize;

    private int commonColor;
    private int traversalColor;

    private int mWidth;
    private int mHeight;

    private Paint mLinePaint;
    private Paint mCircleFillPaint;
    private Paint mCircleStrokePaint;
    private Paint mTextPaint;

    private ValueAnimator mValueAnimator;
    private int alpha;

    private AnimEndListener mAnimEndListener;

    private int lastX;

    private BiTree mBiTree;

    private int step;

    private int stepLimit;

    public BiTreeView(Context context) {
        this(context, null);
    }

    public BiTreeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BiTreeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {

        commonColor = getResources().getColor(R.color.app_common_color);
        traversalColor = getResources().getColor(R.color.home_nav_sort_color);
        topAndBottomOffset = getResources().getDimensionPixelOffset(R.dimen.bitree_top_bottom_offset);

        if (attrs != null) {
            Resources res = getResources();
            TypedArray ta = res.obtainAttributes(attrs, R.styleable.BiTreeView);
            mCircleRadius = ta.getDimensionPixelSize(R.styleable.BiTreeView_circle_radius, res.getDimensionPixelSize(R.dimen.bitree_radius_default));
            xGap = ta.getDimensionPixelSize(R.styleable.BiTreeView_x_gap, res.getDimensionPixelSize(R.dimen.bitree_x_gap_default));
            yGap = ta.getDimensionPixelSize(R.styleable.BiTreeView_y_gap, res.getDimensionPixelSize(R.dimen.bitree_y_gap_default));
            textSize = ta.getDimensionPixelSize(R.styleable.BiTreeView_text_size, res.getDimensionPixelSize(R.dimen.bitree_text_size_default));
        }

        initPaint();
        initAnimator();
    }

    private void initPaint() {
        mLinePaint = new Paint();
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setStrokeWidth(2);
        mLinePaint.setAntiAlias(true);
        mLinePaint.setColor(getResources().getColor(R.color.app_common_color));

        mCircleFillPaint = new Paint();
        mCircleFillPaint.setStyle(Paint.Style.FILL);
        mCircleFillPaint.setColor(getResources().getColor(R.color.app_common_color));

        mCircleStrokePaint = new Paint();
        mCircleStrokePaint.setStyle(Paint.Style.STROKE);
        mCircleStrokePaint.setStrokeWidth(2);
        mCircleStrokePaint.setAntiAlias(true);
        mCircleStrokePaint.setColor(getResources().getColor(R.color.dark_blue));
        mCircleStrokePaint.setStrokeCap(Paint.Cap.ROUND);

        mTextPaint = new Paint();
        mTextPaint.setColor(getResources().getColor(R.color.text_color_white));
        mTextPaint.setTextSize(textSize);
    }

    private void initAnimator() {
        mValueAnimator = ValueAnimator.ofInt(0, 255);
        mValueAnimator.setDuration(800);
        mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                alpha = (int) animation.getAnimatedValue();
                invalidate();
            }
        });
        mValueAnimator.addListener(this);
    }

    public void setBiTree(BiTree biTree) {
        this.mBiTree = biTree;
        stepLimit = 0;
        requestLayout();
    }

    public void setAnimDuring(int during) {
        mValueAnimator.setDuration(during);
    }

    public int getNodeCount() {
        return mBiTree.getNodeCount();
    }

    public int getStepLimit() {
        return stepLimit;
    }

    public void next() {
        int count = mBiTree.getNodeCount();
        if (stepLimit > count) {
            return;
        }
        stepLimit++;
        mValueAnimator.start();
    }

    public void beginPreOrderTraversal() {
        stepLimit = 1;
        state = STATE_PRE_ORDER_TRAVERSAL;
        mValueAnimator.start();

    }

    public void beginInOrderTraversal() {
        stepLimit = 1;
        state = STATE_IN_ORDER_TRAVERSAL;
        mValueAnimator.start();

    }

    public void beginPostOrderTraversal() {
        stepLimit = 1;
        state = STATE_POST_ORDER_TRAVERSAL;
        mValueAnimator.start();

    }

    public void beginLevelTraversal() {
        stepLimit = 1;
        state = STATE_LEVEL_TRAVERSAL;
        mValueAnimator.start();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mBiTree != null) {
            int treeHeight = mBiTree.height();
            int maxLeafCount = (int) Math.pow(2, treeHeight - 1);

            //最大的叶子数量 * 直径 + (最大叶子数量 + 1) * 间隔
            mWidth = mCircleRadius * 2 * maxLeafCount + 2 * xGap * (maxLeafCount + 1);

            //树高度 * 直径 + (树高度 - 1) * 间隔 + offset
            mHeight = mCircleRadius * 2 * treeHeight + yGap * (treeHeight - 1) + 2 * topAndBottomOffset;

            setMeasuredDimension(mWidth, mHeight);

            Log.d(TAG, "onMeasure width = " + mWidth + " height = " + mHeight);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mBiTree == null) {
            return;
        }
        canvas.drawColor(Color.WHITE);
        preOrderDraw(canvas, mBiTree.getRoot(), mWidth / 2, mCircleRadius + topAndBottomOffset, 0, 0);

        step = 0;
        if (state == STATE_PRE_ORDER_TRAVERSAL) {
            preOrderTraversal(canvas, mBiTree.getRoot(), mWidth / 2, mCircleRadius + topAndBottomOffset, 0, 0);
        } else if (state == STATE_IN_ORDER_TRAVERSAL) {
            inOrderTraversal(canvas, mBiTree.getRoot(), mWidth / 2, mCircleRadius + topAndBottomOffset, 0, 0);
        } else if (state == STATE_POST_ORDER_TRAVERSAL) {
            postOrderTraversal(canvas, mBiTree.getRoot(), mWidth / 2, mCircleRadius + topAndBottomOffset, 0, 0);
        } else if (state == STATE_LEVEL_TRAVERSAL) {
            levelTraversal(canvas);
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        //获取到手指处的横坐标和纵坐标
        int x = (int) event.getX();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastX = x;
                break;

            case MotionEvent.ACTION_MOVE:
                //计算移动的距离
                int offsetX = x - lastX;
                //调用layout方法来重新放置它的位置
                layout(getLeft() + offsetX, getTop(), getRight() + offsetX, getBottom());
                break;
        }

        return true;
    }


    private void preOrderDraw(Canvas canvas, BiNode biNode, int x, int y, int parentX, int parentY) {
        if (parentX != 0 && parentY != 0) {
            drawLine(canvas, parentX, parentY, x, y);
        }
        drawNode(canvas, biNode, x, y);

        int height = mBiTree.height();
        int level = mBiTree.getLevel(biNode.data);

        int xOffset = (int) Math.pow(2, (height - level - 1)) * (mCircleRadius + xGap);
        int yOffset = yGap + mCircleRadius * 2;

        if (biNode.leftChild != null) {
            preOrderDraw(canvas, biNode.leftChild, x - xOffset, y + yOffset, x, y);
        }

        if (biNode.rightChild != null) {
            preOrderDraw(canvas, biNode.rightChild, x + xOffset, y + yOffset, x, y);
        }
    }

    private void preOrderTraversal(Canvas canvas, BiNode biNode, int x, int y, int parentX, int parentY) {
        if (step >= stepLimit) {
            return;
        }

        if (parentX != 0 && parentY != 0) {
            traversalLine(canvas, parentX, parentY, x, y);
        }

        int height = mBiTree.height();
        int level = mBiTree.getLevel(biNode.data);
        traversalNode(canvas, biNode, x, y);
        step++;

        int xOffset = (int) Math.pow(2, (height - level - 1)) * (mCircleRadius + xGap);
        int yOffset = yGap + mCircleRadius * 2;

        if (biNode.leftChild != null) {
            preOrderTraversal(canvas, biNode.leftChild, x - xOffset, y + yOffset, x, y);
        }

        if (biNode.rightChild != null) {
            preOrderTraversal(canvas, biNode.rightChild, x + xOffset, y + yOffset, x, y);
        }
    }

    private void inOrderTraversal(Canvas canvas, BiNode biNode, int x, int y, int parentX, int parentY) {
        int height = mBiTree.height();
        int level = mBiTree.getLevel(biNode.data);
        int xOffset = (int) Math.pow(2, (height - level - 1)) * (mCircleRadius + xGap);
        int yOffset = yGap + mCircleRadius * 2;

        if (biNode.leftChild != null) {
            inOrderTraversal(canvas, biNode.leftChild, x - xOffset, y + yOffset, x, y);
        }

        if (step >= stepLimit) {
            return;
        }

        if (parentX != 0 && parentY != 0) {
            traversalLine(canvas, parentX, parentY, x, y);
        }
        traversalNode(canvas, biNode, x, y);
        step++;

        if (biNode.rightChild != null) {
            inOrderTraversal(canvas, biNode.rightChild, x + xOffset, y + yOffset, x, y);
        }
    }

    private void postOrderTraversal(Canvas canvas, BiNode biNode, int x, int y, int parentX, int parentY) {
        int height = mBiTree.height();
        int level = mBiTree.getLevel(biNode.data);
        int xOffset = (int) Math.pow(2, (height - level - 1)) * (mCircleRadius + xGap);
        int yOffset = yGap + mCircleRadius * 2;

        if (biNode.leftChild != null) {
            postOrderTraversal(canvas, biNode.leftChild, x - xOffset, y + yOffset, x, y);
        }

        if (biNode.rightChild != null) {
            postOrderTraversal(canvas, biNode.rightChild, x + xOffset, y + yOffset, x, y);
        }

        if (step >= stepLimit) {
            return;
        }

        if (parentX != 0 && parentY != 0) {
            traversalLine(canvas, parentX, parentY, x, y);
        }
        traversalNode(canvas, biNode, x, y);
        step++;

    }

    public void levelTraversal(Canvas canvas) {

        Queue<BiNodePosition> queue = new ArrayDeque<>();
        Queue<BiNodePosition> output = new ArrayDeque<>();
        queue.add(new BiNodePosition(mBiTree.getRoot(), mWidth / 2, mCircleRadius + topAndBottomOffset, 0, 0));
        output.add(new BiNodePosition(mBiTree.getRoot(), mWidth / 2, mCircleRadius + topAndBottomOffset, 0, 0));

        step++;
        BiNodePosition curPosition;
        while (!queue.isEmpty()) {
            curPosition = queue.peek();
            BiNode biNode = curPosition.mBiNode;

            int height = mBiTree.height();
            int level = mBiTree.getLevel(biNode.data);
            int xOffset = (int) Math.pow(2, (height - level - 1)) * (mCircleRadius + xGap);
            int yOffset = yGap + mCircleRadius * 2;
            if (biNode.leftChild != null) {
                if (step >= stepLimit) {
                    break;
                }
                step++;

                output.add(new BiNodePosition(biNode.leftChild, curPosition.x - xOffset, curPosition.y + yOffset, curPosition.x, curPosition.y));
                queue.add(new BiNodePosition(biNode.leftChild, curPosition.x - xOffset, curPosition.y + yOffset, curPosition.x, curPosition.y));
            }

            if (biNode.rightChild != null) {
                if (step >= stepLimit ) {
                    break;
                }
                step++;

                output.add(new BiNodePosition(biNode.rightChild, curPosition.x + xOffset, curPosition.y + yOffset, curPosition.x, curPosition.y));
                queue.add(new BiNodePosition(biNode.rightChild, curPosition.x + xOffset, curPosition.y + yOffset, curPosition.x, curPosition.y));
            }
            queue.poll();
        }

        while (!output.isEmpty()) {
            BiNodePosition biNodePosition = output.poll();
            traversalNode(canvas, biNodePosition.mBiNode.data, biNodePosition.x, biNodePosition.y, output.isEmpty());
            if (biNodePosition.parentX != 0 && biNodePosition.parentY != 0) {
                traversalLine(canvas, biNodePosition.x, biNodePosition.y, biNodePosition.parentX, biNodePosition.parentY);
            }
        }
    }

    private void drawLine(Canvas canvas, int px, int py, int x, int y) {
        mLinePaint.setColor(commonColor);

        int difX = x - px;
        int difY = y - py;
        int distance = (int) Math.sqrt(Math.pow(difX, 2) + Math.pow(difY, 2));
        int offsetX = difX * mCircleRadius / distance;
        int offsetY = difY * mCircleRadius / distance;
        canvas.drawLine(px + offsetX, py + offsetY, x - offsetX, y - offsetY, mLinePaint);
    }

    private void drawNode(Canvas canvas, BiNode biNode, int x, int y) {
        mCircleFillPaint.setColor(commonColor);
        canvas.drawCircle(x, y, mCircleRadius, mCircleFillPaint);
        canvas.drawCircle(x, y, mCircleRadius, mCircleStrokePaint);

        String text = biNode.data;
        Rect rect = new Rect();
        mTextPaint.getTextBounds(text, 0, text.length(), rect);
        canvas.drawText(biNode.data, x - rect.width() / 2, y + rect.height() / 2, mTextPaint);
    }

    private void traversalLine(Canvas canvas, int px, int py, int x, int y) {
        mLinePaint.setColor(traversalColor);

        int difX = x - px;
        int difY = y - py;
        int distance = (int) Math.sqrt(Math.pow(difX, 2) + Math.pow(difY, 2));
        int offsetX = difX * mCircleRadius / distance;
        int offsetY = difY * mCircleRadius / distance;
        canvas.drawLine(px + offsetX, py + offsetY, x - offsetX, y - offsetY, mLinePaint);

    }

    private void traversalNode(Canvas canvas, BiNode biNode, int x, int y) {
        mCircleFillPaint.setColor(traversalColor);
        if (step == stepLimit - 1) {
            mCircleFillPaint.setAlpha(alpha);
        } else {
            mCircleFillPaint.setAlpha(255);
        }
        canvas.drawCircle(x, y, mCircleRadius, mCircleFillPaint);
        canvas.drawCircle(x, y, mCircleRadius, mCircleStrokePaint);

        String text = biNode.data;
        Rect rect = new Rect();
        mTextPaint.getTextBounds(text, 0, text.length(), rect);
        canvas.drawText(biNode.data, x - rect.width() / 2, y + rect.height() / 2, mTextPaint);

    }

    private void traversalNode(Canvas canvas, String text, int x, int y, boolean isLast) {
        mCircleFillPaint.setColor(traversalColor);
        if (isLast) {
            mCircleFillPaint.setAlpha(alpha);
        } else {
            mCircleFillPaint.setAlpha(255);
        }
        canvas.drawCircle(x, y, mCircleRadius, mCircleFillPaint);
        canvas.drawCircle(x, y, mCircleRadius, mCircleStrokePaint);

        Rect rect = new Rect();
        mTextPaint.getTextBounds(text, 0, text.length(), rect);
        canvas.drawText(text, x - rect.width() / 2, y + rect.height() / 2, mTextPaint);

    }

    @Override
    public void onAnimationStart(Animator animation) {


    }

    @Override
    public void onAnimationEnd(Animator animation) {
        if (mAnimEndListener != null) {
            mAnimEndListener.animEnd(this);
        }

    }

    @Override
    public void onAnimationCancel(Animator animation) {

    }

    @Override
    public void onAnimationRepeat(Animator animation) {

    }


    public void setAnimEndListener(AnimEndListener animEndListener) {
        mAnimEndListener = animEndListener;
    }
}
