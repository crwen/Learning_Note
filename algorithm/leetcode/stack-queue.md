# 栈与队列

## [栈](https://leetcode-cn.com/problemset/all/?topicSlugs=stack&listId=2cktkvj)

### [20 有效括号](https://leetcode-cn.com/problems/valid-parentheses/)

```java
class Solution {
    public boolean isValid(String s) {
        if (s == null || s.length() == 0)
            return true;
        Stack<Character> stack = new Stack<>();
        for (int i = 0; i < s.length(); i ++) {
            char ch = s.charAt(i);
            if (ch == '(' || ch == '[' || ch == '{')
                stack.push(ch);
            else if (ch == ')' || ch == ']' || ch == '}') {
                if (stack.isEmpty() || !match(stack.pop(), ch))
                    return false;
            } else {
                return false; // 其他字符
            }
        }
        if (!stack.isEmpty())
            return false;
        return true;
    }

    private boolean match(char ch1, char ch2) {
        if (ch1 == '(' && ch2 == ')')
            return true;
        if (ch1 == '[' && ch2 == ']')
            return true;
        if (ch1 == '{' && ch2 == '}')
            return true;
        return false;
    }
}
```

leetcode 上大神的答案

```java
public boolean isValid(String s) {
    Stack<Character> stack = new Stack<Character>();
    for (char c : s.toCharArray()) {
        if (c == '(')
            stack.push(')');
        else if (c == '{')
            stack.push('}');
        else if (c == '[')
            stack.push(']');
        else if (stack.isEmpty() || stack.pop() != c)
            return false;
    }
    return stack.isEmpty();
}
```

### [92 二叉树的中序遍历](https://leetcode-cn.com/problems/binary-tree-inorder-traversal/)

```java
class Solution {
    public List<Integer> inorderTraversal(TreeNode root) {
        if (root == null)
            return Collections.emptyList();
        List<Integer> res = new ArrayList<>();
        Stack<TreeNode> stack = new Stack<>();
           TreeNode curr = root;
        while (curr != null || ! stack.isEmpty()) {
            while (curr != null) {
                stack.push(curr);
                curr = curr.left;
            }
            curr = stack.pop();
            res.add(curr.val);
            // 对右子树重复上面的操作
            curr = curr.right;
        }
        return res;
    }
}
```

### 144 二叉树的前序遍历

**递归**

```java
class Solution {

    public List<Integer> preorderTraversal(TreeNode root) {
        if (root == null)
            return Collections.emptyList();
        List<Integer> res = new ArrayList<>();
        preorder(root, res);
        return res;
    }

    private void preorder(TreeNode node, List<Integer> res) {
        if (node == null)
            return;
        res.add(node.val);
        preorder(node.left, res);
        preorder(node.right, res);
    }
}
```

**迭代**

```java
class Solution {
    public List<Integer> preorderTraversal(TreeNode root) {
        if (root == null)
             return Collections.emptyList();
        List<Integer> res = new ArrayList<>();
        Stack<TreeNode> stack = new Stack<>();
        stack.add(root);
        while (! stack.isEmpty()) {
            TreeNode node = stack.pop();
            res.add(node.val);
            if (node.right != null) {
                stack.add(node.right);
            }
            if (node.left != null) {
                stack.add(node.left);
            }
        }
        return res;
    }

}
```

```java
    public List<Integer> preorderTraversal(TreeNode root) {
        List < Integer > res = new ArrayList < > ();
        Stack < TreeNode > stack = new Stack < > ();
        TreeNode curr = root;
        while (curr != null || !stack.isEmpty()) {
            // 一直遍历到左子树最下面
            while (curr != null) {
                stack.push(curr);
                res.add(curr.val);
                curr = curr.left;
            }
            curr = stack.pop();
            // 对右子树重复上面的操作
            curr = curr.right;
        }
        return res;
    }
}
```

### [155 最小栈](https://leetcode-cn.com/problems/min-stack/submissions/)

```java
class MinStack {

    private Stack<Integer> dataStack; // 存放所有元素的普通栈
    private Stack<Integer> minStack; // 存放最小元素

    /** initialize your data structure here. */
    public MinStack() {
        dataStack = new Stack<>();
        minStack = new Stack<>();
    }

    public void push(int x) {
        dataStack.push(x);
        // 如果栈为空或 x < 栈顶元素，说明 x 为最小元素，入 minStack
        if (minStack.isEmpty() || x <= minStack.peek()) {
            minStack.push(x);
            return ;
        }
    }

    public void pop() {
        int ret = dataStack.pop();
        if (ret == minStack.peek()) {
            minStack.pop();
        }
    }

    public int top() {
        return dataStack.peek();
    }

    public int getMin() {
        return minStack.peek();
    }
}
```

### [739 每日温度](https://leetcode-cn.com/problems/daily-temperatures/)

```java
class Solution {
    public int[] dailyTemperatures(int[] temperatures) {

        Stack<Integer> stack = new Stack<>();
        int[] res = new int[temperatures.length];
        for (int current = 0; current < temperatures.length; current++) {
            while (!stack.isEmpty() && temperatures[current] > temperatures[stack.peek()]) {
                int pre = stack.pop();
                res[pre] = current - pre;
            }
            stack.push(current);
        }
        return res;
    }
}
```

## [队列](https://leetcode-cn.com/problemset/all/)

### [102 二叉树的层序遍历](https://leetcode-cn.com/problems/binary-tree-level-order-traversal/)

```java
class Solution {

    class NewNode {
        TreeNode node;
        int level;
        public NewNode(TreeNode node, int level) {
            this.node = node;
            this.level = level;
        }
    }

    public List<List<Integer>> levelOrder(TreeNode root) {
        if (root == null)
            return Collections.emptyList();
        List<List<Integer>> res = new ArrayList<>();
        Queue<NewNode> q = new LinkedList<>();
        q.add(new NewNode(root, 0));
        while (! q.isEmpty()) {
            NewNode newNode = q.poll();
            TreeNode node = newNode.node;
            int level = newNode.level;

            if (level == res.size()) { // 判断是否在同一层
                res.add(new ArrayList<Integer>());
            } 
            List<Integer> list = res.get(level);
            list.add(node.val);

            if (node.left != null) {
                q.add(new NewNode(node.left, level + 1));
            }
            if (node.right != null) {
                q.add(new NewNode(node.right, level + 1));
            } 
        }
        return res;
    }
}
```

```java
class Solution {
    public List<List<Integer>> levelOrder(TreeNode root) {
        if (root == null)
            return Collections.emptyList();

        List<List<Integer>> res = new ArrayList<>();
        Queue<TreeNode> queue = new ArrayDeque<>();
        queue.add(root);

        while (!queue.isEmpty()) {
            int n = queue.size();
            List<Integer> level = new ArrayList<>();
            // 弹出队列中所有节点，并存储下一层中所有节点
            for (int i = 0; i < n; i++) { 
                TreeNode node = queue.poll();
                level.add(node.val);
                if (node.left != null) {
                    queue.add(node.left);
                }
                if (node.right != null) {
                    queue.add(node.right);
                }
            }
            res.add(level);
        }

        return res;
    }
}
```

### [103 二叉树的锯齿形层次遍历](https://leetcode-cn.com/problems/binary-tree-zigzag-level-order-traversal/)

```java
class Solution {
    public List<List<Integer>> zigzagLevelOrder(TreeNode root) {
        if (root == null)
            return Collections.emptyList();
        int level = 0;

        List<List<Integer>> res = new ArrayList<>();
        Queue<TreeNode> queue = new LinkedList<>();
        queue.add(root);
        while ( !queue.isEmpty() ) {
            List<Integer> list = new ArrayList<>();
            int size = queue.size();
            for (int i = 0; i < size; i ++) {
                TreeNode node = queue.poll();
                if (level % 2 != 0) {
                    list.add(0, node.val);
                } else {
                    list.add(node.val);
                }

                if (node.left != null) {
                    queue.add(node.left);
                }
                if (node.right != null) {
                    queue.add(node.right);
                }
            }
            level ++;
            if (list.size() != 0)
                res.add(list);
        }
        return res;
    }
}
```

### [107 二叉树的层次遍历 II](https://leetcode-cn.com/problems/binary-tree-level-order-traversal-ii/)

```java
class Solution {
    public List<List<Integer>> levelOrderBottom(TreeNode root) {
        if (root == null)
            return Collections.emptyList();
        List<List<Integer>> res = new ArrayList<>();
        Queue<TreeNode> queue = new LinkedList<>();
        queue.add(root);
        while ( !queue.isEmpty() ) {
            List<Integer> level = new ArrayList<>();
            int size = queue.size();
            for (int i = 0; i < size; i++) {
                TreeNode node = queue.poll();
                level.add(node.val);
                if (node.left != null)
                    queue.add(node.left);
                if (node.right != null)
                    queue.add(node.right);
            }
            if (level.size() != 0)
                res.add(0, level);
        }
        return res;
    }
}
```

