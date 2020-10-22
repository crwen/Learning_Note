# 回溯

## [子集](https://leetcode-cn.com/problems/subsets/)

> 给定一组不含重复元素的整数数组 nums，返回该数组所有可能的子集

```java
class Solution {

    private List<List<Integer>> res = new ArrayList<>();

    public List<List<Integer>> subsets(int[] nums) {
        if (nums == null || nums.length == 0)
            return res;
        subsets(nums, 0, new ArrayList());
        return res;
    }

    private void subsets(int[] nums, int index, List<Integer> list) {
        res.add(new ArrayList(list));
        for (int i = index; i < nums.length; i ++) {
            list.add(nums[i]);
            subsets(nums, i + 1, list);
            list.remove(list.size() - 1);
        }
    }
}
```

## [全排列](https://leetcode-cn.com/problems/permutations/)

> 给定一个没有重复数字的序列，返回其所有可能的全排列

```java
class Solution {

    private List<List<Integer>> res = new ArrayList<>();
    private boolean[] visited;

    public List<List<Integer>> permute(int[] nums) {
        if (nums == null || nums.length == 0)
            return res;
        visited = new boolean[nums.length];
        permute(nums, new ArrayList());
        return res;
    }

    private void permute(int[] nums, List<Integer> list) {
        if (list.size() == nums.length) {
            res.add(new ArrayList(list));
            return ;
        }

        for (int i = 0; i < nums.length; i ++) {
            if (!visited[i]) {
                visited[i] = true;
                list.add(nums[i]);
                permute(nums, list);
                list.remove(list.size() - 1);
                visited[i] = false;
            }
        }
    }
}
```

## [全排列II](https://leetcode-cn.com/problems/permutations-ii/)

> 给定一个可能有重复数字的序列，返回其所有可能的全排列

```java
class Solution {
    private List<List<Integer>> res = new ArrayList<>();
    private boolean[] visited;

    public List<List<Integer>> permuteUnique(int[] nums) {
        if(nums == null || nums.length == 0)
            return res;
        Arrays.sort(nums);
        visited = new boolean[nums.length];
        permuteUnique(nums, new ArrayList<Integer>());
        return res;
    }

    private void permuteUnique(int[] nums, List<Integer> list) {
        if (list.size() == nums.length) {
            res.add(new ArrayList(list));
            return ;
        }


        for (int i = 0; i < nums.length; i ++) {
            if (!visited[i]) {
                // nums[i-1] 没用过，说明回溯到了同一层，此时接着使用会与同层用 nums[i-1] 重复
                // nums[i-1] 用过了，说明此时咋 num[i-1] 的下一层，相等不会重复
                if (i > 0 && !visited[i-1] && nums[i] == nums[i-1])
                    continue;
                visited[i] = true;
                list.add(nums[i]);
                permuteUnique(nums, list);
                list.remove(list.size() - 1);
                visited[i] = false;
            }
        }
    }
}
```

## [字符串的排序](https://www.nowcoder.com/practice/fe6b651b66ae47d7acce78ffdd9a96c7?tpId=13&&tqId=11180&rp=1&ru=/ta/coding-interviews&qru=/ta/coding-interviews/question-ranking)

> 输入一个字符串，按字典序打印出该字符串中字符的所有排列。例如输入字符串 abc, 则按字典序打印出由字符 a,b,c 所能排列出来的所有字符串 abc,acb,bac,bca,cab 和 cba。

```java
import java.util.ArrayList;
import java.util.Arrays;
public class Solution {

    private ArrayList<String> res = new ArrayList<>();
    private boolean[] visited;

    public ArrayList<String> Permutation(String str) {
       if (str == null || str.length() == 0)
           return new ArrayList();
        char[] arr = str.toCharArray();
        Arrays.sort(arr);
        visited = new boolean[arr.length];
        permutation(arr, "");
        return res;
    }

    private void permutation(char[] arr, String pre) {
        if (pre.length() == arr.length) {
            res.add(new String(pre));
            return ;
        }

        for (int i = 0; i < arr.length; i ++) {
            if (!visited[i]) {
                if (i > 0 && arr[i] == arr[i-1] && !visited[i-1])
                    continue ;
                visited[i] = true;
                permutation(arr, pre + arr[i]);
                visited[i] = false;
            }
        }
    }
}
```

## [组合](https://leetcode-cn.com/problems/combinations/)

> 给定两个整数 n 和 k，返回 1...n 中所有可能的 k 个数的组合

```java
class Solution {

    private List<List<Integer>> res = new ArrayList<>();

    public List<List<Integer>> combine(int n, int k) {
        if (k < 1 || n < 1)
            return res;
        combine(n, k, 1, new ArrayList<Integer>());
        return res;
    }

    private void combine(int n, int k, int index, List<Integer> list) {
        if (list.size() == k) {
            res.add(new ArrayList(list));
            return ;
        }

        // 剪枝 剩余元素个数 < 还需元素个数
        if (n - index + 1 < k - list.size())
            return ;

        for (int i = index; i <= n; i ++) {
            list.add(i);
            combine(n, k, i + 1, list);
            list.remove(list.size() - 1);
        }
    }
}
```

## [电话号码的字母组合](https://leetcode-cn.com/problems/letter-combinations-of-a-phone-number/)

> 给定一个仅包含数字 `2-9` 的字符串，返回所有它能表示的字母组合。给出数字到字母的映射（与电话按键相同）。注意 1 不对应任何字母。

```java
class Solution {
    private String[] letters = new String[] {
        "", "", "abc", "def", "ghi", "jkl", 
        "mno", "pqrs", "tuv", "wxyz"
    };
    private List<String> res = new ArrayList<>();
    public List<String> letterCombinations(String digits) {
        if (digits == null || digits.length() == 0)
            return res;
        letterCombinations(digits.toCharArray(), 0, new StringBuffer());
        return res;
    }

    private void letterCombinations(char[] arr, int index, StringBuffer pre) {
        if (index == arr.length) {
            res.add(new String(pre.toString()));
            return ;
        }
        char[] chArr = letters[arr[index]-'0'].toCharArray();
        for (int i = 0; i < chArr.length; i ++) {
            letterCombinations(arr, index + 1, pre.append(chArr[i]));
            pre.deleteCharAt(pre.length() - 1);
        }
    }
}
```

## [组合总和](https://leetcode-cn.com/problems/combination-sum/)

> 给定一个**无重复**元素的数组 candidates 和一个目标数 target ，找出 candidates 中所有可以使数字和为 target 的组合。**candidates 中的数字可以无限制重复被选取。**

```java
class Solution {

    private List<List<Integer>> res = new ArrayList<>();

    public List<List<Integer>> combinationSum(int[] candidates, int target) {
        if (candidates == null || candidates.length == 0)
            return res;
        combinationSum(candidates, target, 0, new ArrayList<Integer>());
        return res;
    }

    private void combinationSum(int[] candidates, int target, int index, List<Integer> list) {
        if (target == 0) {
            res.add(new ArrayList(list));
            return ;
        }

        if (index > candidates.length || target < 0)
            return ;

        for (int i = index; i < candidates.length; i ++) {
            if (target < candidates[i])
                continue;
            list.add(candidates[i]);
            combinationSum(candidates, target - candidates[i], i , list);
            list.remove(list.size() - 1);
        }
    }
}
```

## [组合总和II](https://leetcode-cn.com/problems/combination-sum-ii/)

> 给定一个数组 `candidates` 和一个目标数 `target` ，找出 `candidates` 中所有可以使数字和为 `target` 的组合。**每个数只能用一次**

```java
class Solution {

    private List<List<Integer>> res = new ArrayList<>();

    public List<List<Integer>> combinationSum2(int[] candidates, int target) {
        if (candidates == null || candidates.length == 0)
            return res;
        Arrays.sort(candidates);
        combinationSum2(candidates, target, 0, new ArrayList<Integer>());
        return res;
    }

    private void combinationSum2(int[] candidates, int target, int index, List<Integer> list) {
        if (index > candidates.length )
            return ;
        if (target == 0) {
            res.add(new ArrayList(list));
            return ;
        }

        for (int i = index; i < candidates.length; i ++) {
            if (target - candidates[i] < 0)
                continue;
            // i > index 表示 candiates[i-1] 还没有使用
            if (i > index && candidates[i] == candidates[i-1])
                continue;
            list.add(candidates[i]);
            combinationSum2(candidates, target - candidates[i], i + 1, list);
            list.remove(list.size() - 1);
        }
    }
}
```

```java
class Solution {
    private Set<List<Integer>> list = new HashSet<>();
    public List<List<Integer>> combinationSum2(int[] candidates, int target) {
        if (candidates == null || candidates.length == 0)
            return Collections.emptyList();
        Arrays.sort(candidates);
        find(candidates, target, 0, 0, new ArrayList<Integer>());
        return new ArrayList<>(list);
    }

    private void find(int[] candidates, int target, int index, int res, List<Integer> t) {
        if (target == res) {
            list.add(t);
            return ;
        }
        if (index >= candidates.length)
            return ;
        for (int i = index; i < candidates.length; i++) {
            if (candidates[i] + res > target)
                continue;
            t.add(candidates[i]);
            find(candidates, target, i + 1, res + candidates[i], new ArrayList<>(t));
            t.remove(t.size() - 1);
        }
    }
}
```

## [单词搜索]()

> 给定一个二维网格和一个单词，找出该单词是否存在于网格中。
>
> 单词必须按照字母顺序，通过相邻的单元格内的字母构成，其中 “相邻” 单元格是那些水平相邻或垂直相邻的单元格。同一个单元格内的字母不允许被重复使用。

```markup
board =
[
  ['A','B','C','E'],
  ['S','F','C','S'],
  ['A','D','E','E']
]

给定 word = "ABCCED", 返回 true
给定 word = "SEE", 返回 true
给定 word = "ABCB", 返回 false
```

```java
class Solution {

    boolean[][] visited;
    int m, n;
    int[][] dir = new int[][] {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

    public boolean exist(char[][] board, String word) {
        if (board == null)
            return false;
        m = board.length;
        if (m == 0 || board[0].length == 0)
            return false;
        n = board[0].length;

        visited = new boolean[m][n];
        for (int i = 0; i < m; i ++) {
            for (int j = 0; j < n; j ++) {
                if (exist(board, word, i, j, 0))
                    return true;
            }
        }
        return false;
    }

    private boolean exist(char[][] board, String word, int x, int y, int index) {
        if (index == word.length() - 1) {
            return word.charAt(index) == board[x][y];
        }
        if (board[x][y] == word.charAt(index)) {
            visited[x][y] = true;
            for (int i = 0; i < dir.length; i ++) {
                int newX = x + dir[i][0];
                int newY = y + dir[i][1];
                if (check(board, newX, newY) && !visited[newX][newY] ) {
                    if (exist(board, word, newX, newY, index + 1))
                        return true;
                }
            }
            visited[x][y] = false;
        }

        return false;
    }

    private boolean check(char[][] board, int x, int y) {
        if (x < 0 || x >= board.length || y < 0 || y >= board[0].length)
            return false;
        return true;
    }
}
```

