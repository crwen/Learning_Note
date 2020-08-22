# 递归与回溯

## [17 电话号码的字母组合](https://leetcode-cn.com/problems/letter-combinations-of-a-phone-number/)

```java
class Solution {

    private final String[] letters = {
            "", "", "abc", "def", "ghi",
            "jkl", "mno", "pqrs", "tuv", "wxyz"
    };

    private List<String> res = new ArrayList<>();

    public List<String> letterCombinations(String digits) {
        if (digits == null || digits.length() == 0)
            return res;
        findLetters(digits, 0, new StringBuilder());
        return res;
    }

    public void findLetters(String digits, int index, StringBuilder prefix) {
        if (index == digits.length()) {
            res.add(prefix.toString());
            return ;
        }

        String str = letters[digits.charAt(index) - '0'];
        for (int i = 0; i < str.length(); i++) {
            findLetters(digits, index + 1, prefix.append(str.charAt(i)));
            prefix.deleteCharAt(prefix.length() - 1);
        }
    }
}
```

## [22 括号生成](https://leetcode-cn.com/problems/generate-parentheses/)

```java
class Solution {
    List<String> res = new ArrayList<>();
    public List<String> generateParenthesis(int n) {
        if (n == 0)
            return res;
        generateParenthesis(0, 0, n, "");
        return res;
    }

    private void generateParenthesis(int left, int right, int n, String pre) {
        // terminator
        if (left == n && right == n) {
            res.add(pre);
        }

        // process
        if (left < n)
            generateParenthesis(left + 1, right, n, pre + "(");
        if (left <= n && right < left) {
            generateParenthesis(left, right + 1, n, pre + ")");
        }
    }
}
```

## [39 组合总和](https://leetcode-cn.com/problems/combination-sum/)

> 给定一个**无重复元素**的数组 `candidates` 和一个目标数 `target` ，找出 `candidates` 中所有可以使数字和为 `target` 的组合

```java
class Solution {
    private List<List<Integer>> res = new ArrayList<>();
    public List<List<Integer>> combinationSum(int[] candidates, int target) {
        if (candidates == null || candidates.length == 0)
            return res;
        findSum(candidates, target, 0, new ArrayList<>());
        return res;
    }

    private void findSum(int[] candidates, int target, int index, List<Integer> list) {
        if (target == 0) {
            res.add(new ArrayList<>(list));
            return;
        }
        if (index > candidates.length || target < 0)
            return;

        for (int i = index; i < candidates.length; i++) {
            if (target < candidates[i])
                continue;
            list.add(candidates[i]);
            findSum(candidates, target - candidates[i], i , list);
            list.remove(list.size() - 1);
        }
    }
}
```

## [46 全排列](https://leetcode-cn.com/problems/permutations/)

```java
class Solution {
    private List<List<Integer>> res = new ArrayList<>();
    boolean[] visited;

    public List<List<Integer>> permute(int[] nums) {
        if (nums == null || nums.length == 0)
            return res;
        visited = new boolean[nums.length];
        permute(nums, new ArrayList<Integer>());
        return res;
    }

    private void permute(int[] nums, ArrayList<Integer> pre) {
        if (pre.size() == nums.length) {
            res.add(new ArrayList<>(pre));
        }
        for (int i = 0; i < nums.length; i ++) {
            if (!visited[i]) {
                visited[i] = true;
                pre.add(nums[i]);
                permute(nums, pre);
                pre.remove(pre.size() - 1);
                visited[i] = false;
            }
        }
    }
}
```

## [70 爬楼梯](https://leetcode-cn.com/problems/climbing-stairs/)

> 思路
>
> 1. 递归，记忆化搜索
> 2. 迭代，dp

```java
class Solution {

    private int[] res;

    public int climbStairs(int n) {
        res = new int[n+1];

        return climb(n);
    }

    private int climb(int n) {
        if (n == 1 || n == 2)
            return n;
        if (res[n] > 0)
            return res[n];
        res[n] =  climb(n - 1) + climb(n - 2);
        return res[n];
    }
}
```

```java
class Solution {
    public int climbStairs(int n) {
        if (n == 1 || n == 2)
            return n;
        int first = 2;
        int second = 1;
        int res = 0;
        for (int i = 3; i <= n; i++) {
            res = first + second;
            second = first;
            first = res;
        }
        return res;
    }
}
```

## [77 组合](https://leetcode-cn.com/problems/combinations/)

```java
class Solution {

    List<List<Integer>> res = new ArrayList<>();
    public List<List<Integer>> combine(int n, int k) {
        backTrace(n, k, new ArrayList<>(), 1);
        return res;
    }
    private void backTrace(int n, int k, List<Integer> list, int index){
        if(list.size() == k){
            res.add(new ArrayList<>(list));
            return;
        }
        if (n - index + 1 < k - list.size())
            return ;

        for(int i = index; i <= n ; i++){
            list.add(i);
            backTrace(n, k, list,i + 1);
            list.remove(list.size() -1);
        }
    }
}
```

## [78 子集](https://leetcode-cn.com/problems/subsets/)

```java
class Solution {
    private ArrayList<List<Integer>> res = new ArrayList<>();
    public List<List<Integer>> subsets(int[] nums) {
        if (nums == null || nums.length == 0)
            return res;
        subsets(nums, 0, new ArrayList<Integer>());
        return res;
    }

    private void subsets(int[] nums, int index, List<Integer> pre) {
        res.add(new ArrayList<>(pre));
        for (int i = index; i < nums.length; i++) {
            pre.add(nums[i]);
            subsets(nums, i + 1, pre);
            pre.remove(pre.size() - 1);
        }
    }
}
```

## [79 单词搜索](https://leetcode-cn.com/problems/word-search/)

```java
class Solution {

    private int[][] dir = new int[][] {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
    private boolean[][] visited;


    public boolean exist(char[][] board, String word) {
        if (board == null || word == null)
            return false;

        visited = new boolean[board.length][board[0].length];
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                if (findWord(board, i, j, word, 0))
                    return true;
            }
        }
        return false;
    }

    private boolean findWord(char[][] board, int x, int y, String target, int index) {
        if (index == target.length() - 1) {
            return target.charAt(index) == board[x][y];
        }
        if (target.charAt(index) == board[x][y]) {
            visited[x][y] = true;
            for (int i = 0; i < dir.length; i++) {
                int newX = x + dir[i][0];
                int newY = y + dir[i][1];
                if (check(board, newX, newY) && !visited[newX][newY]) {
                    if (findWord(board, newX, newY, target, index + 1))
                        return true;
                }
            }
            visited[x][y] = false;
        }

        return false;
    }

    private boolean check(char[][] board, int x, int y) {
        if (x < 0 || y < 0 || x >= board.length || y >= board[0].length)
            return false;
        return true;
    }
}
```

