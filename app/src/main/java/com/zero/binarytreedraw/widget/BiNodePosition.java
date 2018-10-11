package com.zero.binarytreedraw.widget;


import com.zero.binarytreedraw.datastruture.BiNode;

/**
 * @author zz
 * @email zzerostar@163.com
 * @date 2018/5/21
 */
public class BiNodePosition {

    public BiNode mBiNode;

    public int x;

    public int y;

    public int parentX;

    public int parentY;

    public BiNodePosition(BiNode biNode, int x, int y, int parentX, int parentY) {
        this.mBiNode = biNode;
        this.x = x;
        this.y = y;
        this.parentX = parentX;
        this.parentY = parentY;
    }
}
