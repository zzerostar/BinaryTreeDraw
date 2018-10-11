package com.zero.binarytreedraw.datastruture;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * @author zz
 * @email zzerostar@163.com
 * @date 2018/3/6
 */
public class BiTree {

    private BiNode root;

    private static int counter;

    private static StringBuilder sBuilder;

    public BiTree() {
        root = new BiNode();
    }

    public BiTree(BiNode root) {
        this.root = root;
    }

    public BiTree(String s) {
        this();
        createBiTree(s);
    }

    public BiTree createBiTree(String[] elems) {
        counter = 0;
        createBiTreeNode(root, elems, counter++);
        return this;
    }

    public BiTree createBiTree(String src) {
        String[] elems = new String[src.length()];
        for (int i = 0; i < src.length(); i++) {
            char c = src.charAt(i);
            elems[i] = String.valueOf(c);
        }
        createBiTree(elems);
        return this;
    }

    public BiNode createBiTreeNode(BiNode root, String[] elems, int i) {
        if (i < elems.length) {
            if (elems[i] == null || elems[i].equals("#")) {
                root = null;
            } else {
                BiNode left = new BiNode();
                BiNode right = new BiNode();
                root.data = elems[i];
                root.leftChild = createBiTreeNode(left, elems, counter++);
                root.rightChild = createBiTreeNode(right, elems, counter++);
            }
        }
        return root;
    }

    public BiNode getRoot() {
        return root;
    }

    public int getLevel(String key) {
        return getLevel(root, key);
    }

    private int getLevel(BiNode biNode, String key) {
        int level = 0;
        int levelRight = 0;
        int levelLeft = 0;

        if (biNode.data.equals(key)) {
            return 1;
        }

        if (biNode.leftChild != null) {
            levelLeft = getLevel(biNode.leftChild, key);
        }
        if (biNode.rightChild != null) {
            levelRight = getLevel(biNode.rightChild, key);
        }

        if (levelLeft > 0 && levelRight == 0) {
            level = levelLeft;
        } else if (levelRight > 0 && levelLeft == 0) {
            level = levelRight;
        }

        if (levelLeft != 0 || levelRight != 0) {
            level++;
        }

        return level;

    }

    public int height() {
        return height(root);
    }

    public int height(BiNode root) {
        if (root == null) {
            return 0;
        } else {
            int left = height(root.leftChild);
            int right = height(root.rightChild);
            return left > right ? left + 1 : right + 1;
        }
    }

    public String getPreOrderTraversal() {
        if (sBuilder == null) {
            sBuilder = new StringBuilder();
        }
        sBuilder.delete(0, sBuilder.length());
        preOrderTraversal(root);
        return sBuilder.toString();
    }

    private void preOrderTraversal(BiNode biNode) {

        if (biNode == null) {
            return;
        }
        sBuilder.append(biNode.data);
        preOrderTraversal(biNode.leftChild);
        preOrderTraversal(biNode.rightChild);
    }

    public String getInOrderTraversal() {
        if (sBuilder == null) {
            sBuilder = new StringBuilder();
        }
        sBuilder.delete(0, sBuilder.length());
        inOrderTraversal(root);
        return sBuilder.toString();
    }

    private void inOrderTraversal(BiNode biNode) {
        if (biNode == null) {
            return;
        }
        inOrderTraversal(biNode.leftChild);
        sBuilder.append(biNode.data);
        inOrderTraversal(biNode.rightChild);
    }

    public String getPostOrderTraversal() {
        if (sBuilder == null) {
            sBuilder = new StringBuilder();
        }
        sBuilder.delete(0, sBuilder.length());
        postOrderTraversal(root);
        return sBuilder.toString();
    }

    private void postOrderTraversal(BiNode biNode) {
        if (biNode == null) {
            return;
        }

        postOrderTraversal(biNode.leftChild);
        postOrderTraversal(biNode.rightChild);
        sBuilder.append(biNode.data);
    }

    public String getLevelOrderTraversal() {
        if (sBuilder == null) {
            sBuilder = new StringBuilder();
        }
        sBuilder.delete(0, sBuilder.length());
        levelOrderTraversal(root);
        return sBuilder.toString();
    }

    private void levelOrderTraversal(BiNode biNode) {
        if (biNode == null) {
            return;
        }

        Queue<BiNode> queue = new ArrayDeque<>();
        queue.add(biNode);

        BiNode curNode;
        while (!queue.isEmpty()) {
            curNode = queue.peek();
            sBuilder.append(curNode.data);

            if (curNode.leftChild != null) {
                queue.add(curNode.leftChild);
            }
            if (curNode.rightChild != null) {
                queue.add(curNode.rightChild);
            }

            queue.poll();
        }
    }

    public int getNodeCount() {
        String s = getPreOrderTraversal();
        return s.length();
    }
}
