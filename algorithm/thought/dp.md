# 动态规划

**解题方法：**

* 一维数组：**在子数组`array[0..i]`中，以**`array[i]`**结尾的目标子序列（最长递增子序列）的长度是`dp[i]`**。
* 二维数组
  * **在子数组`arr1[0..i]`和子数组`arr2[0..j]`中，我们要求的子序列（最长公共子序列）长度为`dp[i][j]`**。
  * **在子数组`array[i..j]`中，我们要求的子序列（如最长回文子序列）的长度为`dp[i][j]`**。

## 一维数组

**在子数组`array[0..i]`中，以\**`array[i]`\*_结尾的目标子序列（如最长递增子序列）的长度是`dp[i]`\*_。

### [买股票的最佳时机](https://leetcode-cn.com/problems/best-time-to-buy-and-sell-stock/)

```java
class Solution {
    public int maxProfit(int[] prices) {
        int minPrice = Integer.MAX_VALUE;
        int maxProfit = 0;
        for (int i = 0; i < prices.length; i ++) {
            if (prices[i] < minPrice)
                minPrice = prices[i];
            else if (prices[i] - minPrice > maxProfit) 
                maxProfit = prices[i] - minPrice;
        }
        return maxProfit;
    }
}
```

### [最大子序和](https://leetcode-cn.com/problems/maximum-subarray/)

> 给定一个整数数组 `nums` ，找到一个具有最大和的连续子数组（子数组最少包含一个元素），返回其最大和。

**dp 数组含义**：dp\[i\] 表示以第 i 个字符结尾的子序列最大和

**状态转移方程**：dp\[i\] = max\(dp\[i-1\], 0\) + nums\[i\]

```java
class Solution {
    public int maxSubArray(int[] nums) {
        if (nums == null || nums.length == 0)
            return 0;
        int[] dp = new int[nums.length];
        dp[0] = nums[0];
        int max = dp[0];
        for (int i = 1; i < nums.length; i ++) {
            dp[i] = (dp[i-1] > 0 ? dp[i-1] : 0) + nums[i];
            max = max > dp[i] ? max : dp[i];
        }
        return max;
    }
}
```

优化

```java
class Solution {
    public int maxSubArray(int[] nums) {
        if (nums == null || nums.length == 0)
            return 0;
        int pre = nums[0];
        int max = pre;
        for (int i = 1; i < nums.length; i ++) {
            pre = (pre > 0 ? pre : 0) + nums[i];
            max = max > pre ? max : pre;
        }
        return max;
    }
}
```

### [最长上升子序列](https://leetcode-cn.com/problems/longest-increasing-subsequence/)

> 给定一个无需的整数数组，找到其中最长上升子序列的长度

```markup
input: [10, 9, 2, 5, 3, 7, 101, 18]
output:4
[2, 3, 7, 10]
```

**dp 数组含义**：dp\[i\] 表示以 nums\[i\] 结尾的最长递增子序列的长度

**状态转移方程**：

* nums\[i\] &gt; nums\[j\]（0 &lt; i &lt; len, 0 &lt; j &lt; i）：dp\[i\] = max\(dp\[i\], dp\[j\] + 1\)

```java
class Solution {
    public int lengthOfLIS(int[] nums) {
        if (nums == null || nums.length == 0)
            return 0;
        int[] dp = new int[nums.length];
        Arrays.fill(dp, 1);
        for (int i = 1;  i < nums.length;  i++) {
            for (int j = 0; j < i; j ++) {
                if (nums[i] > nums[j]) {
                    dp[i] = Math.max(dp[j] + 1, dp[i]);
                }
            }
        }

        int res = 0;
        for (int i = 0; i < dp.length; i ++) {
            res = Math.max(res, dp[i]);
        }
        return res;
    }
}
```

### [打家劫舍](https://leetcode-cn.com/problems/house-robber/)

**dp 数组含义**：dp\[i\] 表示尝试偷取第 i 家，有两种可能，偷第 i 家，不偷第 i 家

**状态转移方程**：dp\[i\] = max\(dp\[i-1\], dp\[i-2\] + nums\[i\]\)

```java
class Solution {
    public int rob(int[] nums) {
        if (nums == null || nums.length == 0) 
            return 0;
        int[] dp = new int[nums.length];
        dp[0] = nums[0];

        for (int i = 1; i < nums.length; i ++) {
            dp[i] = Math.max(dp[i-1], (i >= 2 ? dp[i-2]  : 0) + nums[i]);
        }

        return dp[nums.length - 1];
    }
}
```

## 二维数组

* **在子数组`arr1[0..i]`和子数组`arr2[0..j]`中，我们要求的子序列（最长公共子序列）长度为`dp[i][j]`**。
* **在子数组`array[i..j]`中，我们要求的子序列（最长回文子序列）的长度为`dp[i][j]`**。

### [最长公共子序列](https://leetcode-cn.com/problems/longest-common-subsequence/)

> 给定两个字符串 `text1` 和 `text2`，返回这两个字符串的最长公共子序列的长度。

**dp 数组含义**：对于字符串 s1 和 s2，dp\[i\] \[j\] 表示 s1 的前 i 个字符，与 s2 的前 j 个字符的最长公共子序列

**状态转移方程**：

* s1\[i\] == s\[j\]， `dp[i] [j] = dp[i-1] [j-1] + 1`
* s1\[i\] != s\[j\]，`dp[i] [j] = max(dp[i] [j-1], dp[i-1] [j]])`

```java
class Solution {

    private int[][] memo;

    public int longestCommonSubsequence(String text1, String text2) {
        if (text1 == null || text2 == null)
            return 0;
        memo = new int[text1.length()][text2.length()];
        return lcs(text1.toCharArray(), text1.length() - 1, text2.toCharArray(), text2.length() - 1);
    }

    private int lcs(char[] text1, int i, char[] text2, int j) {
        if (i < 0 || j < 0)
            return 0;
        if (memo[i][j] != 0)
            return memo[i][j];
        if (text1[i] == text2[j]) {
            memo[i][j] = lcs(text1, i - 1, text2, j - 1) + 1;
        } else {
            memo[i][j] = Math.max(lcs(text1, i - 1, text2, j), lcs(text1, i, text2, j- 1));
        }
        return memo[i][j];
    }
}
```

```java
class Solution {

    public int longestCommonSubsequence(String text1, String text2) {
        if (text1 == null || text2 == null)
            return 0;
        int[][] dp = new int[text1.length()+1][text2.length()+1];
        char[] s1 = text1.toCharArray();
        char[] s2 = text2.toCharArray();

        for (int i = 1; i < s1.length + 1; i ++) {
            for (int j = 1; j < s2.length + 1; j ++) {
                if (s1[i-1] == s2[j-1]) {
                    dp[i][j] = dp[i-1][j-1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }
        return dp[s1.length][s2.length];
    }
}
```

### [最小路径和](https://leetcode-cn.com/problems/minimum-path-sum/)

> 给定一个包含非负整数的 _m_ x _n_ 网格，请找出一条从左上角到右下角的路径，使得路径上的数字总和为最小。
>
> **说明：**每次只能向下或者向右移动一步。

**dp 数组含义**：`dp[i][j]`表示从 \(0, 0\) 到 \(i, j\) 的最小路径和

**状态转移方程**：`dp[i][j] = max(dp[i-1][j], dp[i][j-1])`

```java
class Solution {

    // dp[i][j] = min(dp[i-1][j], dp[i][j-1])
    public int minPathSum(int[][] grid) {   
        if (grid == null || grid.length == 0)
            return 0;
        if (grid[0] == null || grid[0].length == 0)
            return 0;
        int m = grid.length;
        int n = grid[0].length;

        int[][] dp = new int[m][n];
        dp[0][0] = grid[0][0];
        for (int i = 1; i < m; i ++) {
            dp[i][0] = dp[i-1][0] + grid[i][0];
        }
        for (int i = 1; i < n; i ++) {
            dp[0][i] = dp[0][i-1] + grid[0][i];
        }

        for (int i = 1; i < m; i ++) {
            for (int j = 1; j < n; j ++) {
                dp[i][j] = Math.min(dp[i-1][j], dp[i][j-1]) + grid[i][j];
            }
        }

        return dp[m-1][n-1];
    }
}
```

