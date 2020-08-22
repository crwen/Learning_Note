# 动态规划

## [198 打家劫舍](https://leetcode-cn.com/problems/house-robber/)

> 题目描述：抢劫一排住户，但是不能抢邻近的住户，求最大抢劫量。

**记忆化搜索**

```java
class Solution {

    private int[] memo;

    public int rob(int[] nums) {
        memo = new int[nums.length + 1];
        Arrays.fill(memo, -1);
        return rob(nums, 0);

    }

    // 尝试偷取 [index...n-1]
    private int  rob(int[] nums, int index) {
        if (index >= nums.length)
            return 0;
        if (index == nums.length - 1) {
            return nums[index];
        }
        if (memo[index] != -1)
            return memo[index];
        int res = 0;
        for (int i = index; i < nums.length; i ++) {
            res = Math.max(rob(nums, i + 2) + nums[i], res);
        }
        memo[index] = res;
        return res;
    }
}
```

从自顶向下的搜索中，可以得到状态转移方程

> f\(x\) 表示考虑偷取 \[x...n - 1\] 能够偷取的最大值
>
> f\(x\) = max\( f\(x\), nums\[x\] + f\(x + 2\) \)

```java
class Solution {

    public int rob(int[] nums) {
        int n = nums.length;
        if (n == 0)
            return 0;
        int[] memo = new int[n];
        memo[n-1] = nums[n-1];
        // 考虑偷取 [i...n-1]
        for(int i = n - 2 ; i >= 0 ; i --)
            for (int j = i; j < n; j ++)
                memo[i] = Math.max(memo[i],
                              nums[j] + (j + 2 < n ? memo[j + 2] : 0));
        return memo[0];
    }
}
```

对于上面动态规划的方法还可以优化

```java
class Solution {

    public int rob(int[] nums) {
        int n = nums.length;
        if (n == 0)
            return 0;
        int[] memo = new int[n];
        memo[n-1] = nums[n-1];
        // 考虑偷取 [i...n-1]
        for(int i = n - 2 ; i >= 0 ; i --)
                memo[i] = Math.max(memo[i+1],
                              nums[i] + (i + 2 < n ? memo[i + 2] : 0));
        return memo[0];
    }
}
```

上面定义的状态 x 为 **考虑偷取 \[x...n-1\]**，我们也可以定义另外一个状态：**考虑偷取 \[0...x\]** ，最终的代码也是类似的。

```java
class Solution {

    private int[] memo;

    public int rob(int[] nums) {
        memo = new int[nums.length];
        Arrays.fill(memo, -1);
        return rob(nums, nums.length - 1);

    }

    // 尝试偷取 [0...index]
    private int  rob(int[] nums, int index) {
        if (index < 0) {
            return 0;
        }
        if (memo[index] != -1)
            return memo[index];
        memo[index] = Math.max( nums[index] + rob(nums, index - 2) , 
                               rob(nums, index - 1) );
        return memo[index];
    }
}
```

```java
class Solution {
    // 考虑偷取 [0...x]
    // f(x) = max(nums[x] + f(x - 2), f(x - 1))
    public int rob(int[] nums) {
        if (nums == null || nums.length == 0)
            return 0;
        // 考虑偷取 [0...x]
        int[] memo = new int[nums.length];
        memo[0] = nums[0];
        for (int i = 1; i < nums.length; i ++) {
            memo[i] = Math.max(memo[i - 1],
                               nums[i] + (i - 2 >= 0 ? memo[i - 2] : 0));
        }
        return memo[nums.length-1];
    }
}
```

## [322 零钱兑换](https://leetcode-cn.com/problems/coin-change/)

> 给定不同面额的硬币 coins 和一个总金额 amount。计算可以凑成总金额所需的最少的硬币个数。如果没有任何一种硬币组合能组成总金额，返回 -1。

**记忆化搜索**

```java
class Solution {

    private int[] memo;
    public int coinChange(int[] coins, int amount) {
        memo = new int[amount + 1];
        helper(coins, amount);
        return memo[amount] == Integer.MAX_VALUE ? -1 : memo[amount];
    }

    private int helper(int[] coins, int amount) {
        if (amount == 0) {
            return 0;
        }
        if (amount < 0)
            return -1;
        if (memo[amount] != 0)
            return memo[amount];
        int min = Integer.MAX_VALUE;
        for (int i = 0; i < coins.length; i++) {
            int num = helper(coins, amount - coins[i]);
            if (num >= 0 && num < min) {
                min = num + 1;
            }
        }
        memo[amount] = min;

        return min;
    }
}
```

从自定向下的搜索中，可以得到状态转移公式

> f\(0\) = 0
>
> f\(i\) = min\( f\( i - coins\[i\] \), f\(i\) \)

```java
class Solution {

    public int coinChange(int[] coins, int amount) {
        int[] dp = new int[amount + 1];
        Arrays.fill(dp, amount + 1); 
        dp[0] = 0; 

        for (int k = 1; k <= amount; k ++) {
            for (int i = 0; i < coins.length; i ++) {
                if (k >= coins[i])
                    dp[k] = Math.min(dp[k], dp[k - coins[i]] +1);
            }
        }
        return dp[amount] > amount ? -1 : dp[amount];
    }
}
```

