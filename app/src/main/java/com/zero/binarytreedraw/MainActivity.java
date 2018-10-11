package com.zero.binarytreedraw;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.zero.binarytreedraw.datastruture.BiTree;
import com.zero.binarytreedraw.widget.AnimEndListener;
import com.zero.binarytreedraw.widget.BiTreeView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AnimEndListener {

    private static final String BITREE_EXAMPLE = "ABD#G##E##C#FH##I##";

    private static final int MSG_UPDATE_TRAVERSAL = 1;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mBiTreeView.next();
        }
    };

    BiTreeView mBiTreeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBiTreeView = (BiTreeView) findViewById(R.id.bitree_view);

        BiTree biTree = new BiTree();
        biTree.createBiTree(BITREE_EXAMPLE);

        mBiTreeView.setBiTree(biTree);
        mBiTreeView.setAnimEndListener(this);

        findViewById(R.id.btn_pre).setOnClickListener(this);
        findViewById(R.id.btn_in).setOnClickListener(this);
        findViewById(R.id.btn_post).setOnClickListener(this);
        findViewById(R.id.btn_level).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_pre:
                mBiTreeView.beginPreOrderTraversal();
                break;

            case R.id.btn_in:
                mBiTreeView.beginInOrderTraversal();
                break;

            case R.id.btn_post:
                mBiTreeView.beginPostOrderTraversal();
                break;

            case R.id.btn_level:
                mBiTreeView.beginLevelTraversal();
                break;
        }
    }

    @Override
    public void animEnd(View view) {
        int id = view.getId();
        mHandler.sendEmptyMessage(MSG_UPDATE_TRAVERSAL);
    }
}
