# 树

## [98 验证二叉树](https://leetcode-cn.com/problems/validate-binary-search-tree/)

```java
// 检查中序遍历结果是否是有序的
class Solution {
    private List<Integer> list = new ArrayList<>();
    public boolean isValidBST(TreeNode root) {
        if (root == null)
            return true;
        inorder(root);

        for (int i = 1; i < list.size(); i++) {
            if (list.get(i-1) >= list.get(i))
                return false;
        }
        return true;
    }

    private void inorder(TreeNode node) {
        if (node == null)
            return ;
        inorder(node.left);
        list.add(node.val);
        inorder(node.right);
    }
}
```

```java
class Solution {
    public boolean isValidBST(TreeNode root) {
        if (root == null || (root.left == null && root.right == null))
            return true;
        if (root.left != null && root.val <= getMax(root.left))
            return false;
        if (root.right != null && root.val >= getMin(root.right))
            return false;
        return isValidBST(root.left) && isValidBST(root.right);
    }

    // 获取以 node 为根节点的二叉树的最大值
    private int getMax(TreeNode node) {
        if (node == null)
            return 0;
        if (node.right == null)
            return node.val;
        return getMax(node.right);
    }

    // 获取以 node 为根节点的二叉树的最小值
    private int getMin(TreeNode node) {
        if (node == null)
            return 0;
        if (node.left == null)
            return node.val;
        return getMax(node.left);
    }
}
```

## [101 对称二叉树](https://leetcode-cn.com/problems/symmetric-tree/)

```java
class Solution {
    public static boolean isSymmetric(TreeNode root) {
        if (root == null)
            return true;
        if (root.left == null && root.right == null)
            return true;
        if (root.left == null || root.right == null)
            return false;
        return isMirror(root, root);
    }

    private static boolean isMirror(TreeNode p, TreeNode q) {
        if (p == null && q == null)
            return true;
        if (p == null || q == null)
            return false;
        if (p.val != q.val)
            return false;
        return isMirror(p.left, q.right) && isMirror(p.right, q.left);
    }

}
```

## [104 二叉树的最大深度](https://leetcode-cn.com/problems/maximum-depth-of-binary-tree/)

**递归**

```java
class Solution {
    public int maxDepth(TreeNode root) {
        if (root == null)
            return 0;
        int maxLeft = maxDepth(root.left);
        int maxRight = maxDepth(root.right);
        return Math.max(maxLeft, maxRight) + 1;
    }
}
```

## [105 重建二叉树](https://leetcode-cn.com/problems/construct-binary-tree-from-preorder-and-inorder-traversal/)

```java
class Solution {
    public TreeNode buildTree(int[] preorder, int[] inorder) {
        if (preorder == null || inorder == null 
            || preorder.length == 0 || inorder.length == 0)
            return null;
        return build(preorder, inorder, 0, preorder.length - 1, 0, inorder.length - 1);

    }

    // 重构 preorder[preL,preR] inorder[inL,inR]
    // 返回以 preorder[preL] 为根的二叉树
    private TreeNode build(int[] preorder, int[] inorder, int preL, int preR, int inL, int inR ) {
        if (preL > preR)
            return null;
        TreeNode node = new TreeNode(preorder[preL]);
        int i = inL;
        // 找到中序遍历序列中 preorder[pre[L]]的位置，左边为左子树元素，右边为右子树元素
        for (; i <= inR; i++) {
            if (inorder[i] == preorder[preL])
                break;
        }

        int leftSize = i - inL; // 左子树元素 个数
        node.left = build(preorder, inorder, preL + 1, preL + leftSize, inL , i - 1);
        node.right = build(preorder, inorder, preL + leftSize + 1, preR, i + 1, inR);
        return node;
    }
}
```

 我们发现在 `build()` 中我们每次都要遍历中序遍历数组，来找到根节点的下标。我们可以用一个 map 来将中序遍历数组存储起来，这样一来就可以使用常数的时间复杂度来获取 根节点下标了。

## [111 二叉树的最小深度](https://leetcode-cn.com/problems/minimum-depth-of-binary-tree/)

```java
class Solution {
    public int minDepth(TreeNode root) {
        if (root == null)
            return 0;
        // if (root.left == null)
        //     return minDepth(root.right) + 1;
        // if (root.right == null)
        //     return minDepth(root.left) + 1;
        int right =  minDepth(root.right);
        int left =  minDepth(root.left);
        if (left == 0 || right == 0)
            return left + right + 1;
        return Math.min(left, right) + 1;
    }
}
```

## [226 翻转二叉树](https://leetcode-cn.com/problems/invert-binary-tree/)

```java
class Solution {
    public TreeNode invertTree(TreeNode root) {
        if (root == null)
            return null;
        TreeNode leftTree = invertTree(root.left);
        TreeNode rightTree = invertTree(root.right);
        root.left = rightTree;
        root.right = leftTree;
        return root;
    }
}
```

## [437 路径综合 III](https://leetcode-cn.com/problems/path-sum-iii/)

```java
class Solution {
    private int res = 0;
    public int pathSum(TreeNode root, int sum) {
        if (root == null)
            return 0;
        findPath(root, sum);
        pathSum(root.left, sum);
        pathSum(root.right, sum);
        return res;
    }

    private void findPath(TreeNode node,int sum) {
        if (node == null) 
            return ;
        if (sum == node.val)
            res ++;
        findPath(node.left, sum - node.val);
        findPath(node.right, sum - node.val);
    }
}
```

## [543 二叉树的直径](https://leetcode-cn.com/problems/diameter-of-binary-tree/)

```java
class Solution {
    private int res = 0;
    public int diameterOfBinaryTree(TreeNode root) {
        if (root == null)
            return 0;
        getDepth(root);
        return res - 1;
    }

    // 返回以 node 为根的二叉树的最大深度
    private int getDepth(TreeNode node) {
        if (node == null)
            return 0;

        int leftDepth = getDepth(node.left);
        int rightDepth = getDepth(node.right);
        res = Math.max(res, leftDepth + rightDepth + 1);
        return Math.max(leftDepth, rightDepth) + 1;
    }
}
```

## [617 合并二叉树](https://leetcode-cn.com/problems/merge-two-binary-trees/)

```java
class Solution {
    public TreeNode mergeTrees(TreeNode t1, TreeNode t2) {
        if(t1==null || t2==null) {
            return t1==null? t2 : t1;
        }

        t1.val += t2.val;
        t1.left = mergeTrees(t1.left, t2.left);
        t1.right = mergeTrees(t1.right, t2.right);

        return t1;
    }
}
```

