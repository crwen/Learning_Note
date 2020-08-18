# 数组

### [1 两数之和](https://leetcode-cn.com/problems/two-sum/)

> 思路：
>
> 1. 暴力。枚举出所有可能性 O\(n^2\)
> 2. 排序，双指针 O\(nlgn\)
> 3. hash 表 O\(n\)

```java
class Solution {
    public int[] twoSum(int[] nums, int target) {
        if ( nums == null || nums.length == 0)
            throw new IllegalArgumentException();
​
        Map<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < nums.length; i++) {
            int temp = target - nums[i];
            if (map.containsKey(temp))
                return new int[]{map.get(temp), i};
            map.put(nums[i], i);
        }
        throw new IllegalArgumentException();
    }
}
```

### [11 盛最多水的容器](https://leetcode-cn.com/problems/container-with-most-water/)

> 思路
>
> 1. 暴力。枚举出每种情况，取最大值
> 2. 双指针。设置两个指针 i, j。比较 height\[i\] height\[j\] 的大小，移动小的那一侧的指针。（短板效应）

```java
    // 时间复杂度 O(n)
    public int maxArea(int[] height) {
        if (height == null || height.length == 0)
            return 0;
        int i = 0, j = height.length - 1;
        int max = -1;
        while (i < j) {
            max = Math.max(Math.min(height[i], height[j]) * (j - i), max);
            if (height[i] < height[j])
                i ++;
            else
                j --;
        }
        return max;
    }
```

### [15 三数之和](https://leetcode-cn.com/problems/3sum/)

> 思路
>
> 1. 暴力，三重循环，枚举各种情况
> 2. 排序，固定一个值，求两数和

```java
class Solution {
    public List<List<Integer>> threeSum(int[] nums) {
        List<List<Integer>> res = new ArrayList<>();
        if (nums == null || nums.length == 0)
            return res;
​
        Arrays.sort(nums);
​
        for (int i = 0; i < nums.length - 2; i ++) {
            if (nums[i] > 0)
                return res;
            if (i > 0 && nums[i-1] == nums[i]) // 去重
                continue;
            
            int target = -nums[i];
            int k = nums.length - 1;
            for (int j = i + 1; j < k; ) {
                if (nums[j] + nums[k] > target) {
                    k --;
                } else if (nums[j] + nums[k] < target) {
                    j ++;
                } else {
                    res.add(Arrays.asList(nums[i], nums[j], nums[k]));
                    j ++; 
                    k --;
                    // 跳过重复元素
                    while (j < k && nums[j] == nums[j-1]) j ++;
                    while ( k > j && nums[k] == nums[k+1]) k --;
                }
            }
        }
        return res;
    }
}
```

### [66 加一](https://leetcode-cn.com/problems/plus-one/)

> 思路
>
> ​ 由于只将数组代表的数字加一，所以加一后每一位只有两种可能
>
> * num &lt;= 9; 直接返回即可
> * num == 0；处理下一位

```java
class Solution {
    public int[] plusOne(int[] digits) {
        if (digits == null || digits.length == 0)
            throw new IllegalArgumentException();

        for (int i = digits.length - 1; i >= 0; i --) {
            digits[i] ++;
            if (digits[i] <= 9)
                return digits;
            digits[i] %= 10;
        }
        digits = new int[digits.length + 1];
        digits[0] = 1;
        return digits;
    }
}
```

### [88 合并两个有序数组](https://leetcode-cn.com/problems/merge-sorted-array/)

> 思路：
>
> ​ 利用归并排序思想。从后往前归并

```java
class Solution {
    public void merge(int[] nums1, int m, int[] nums2, int n) {
        int len = m + n - 1;
        int i = m - 1;
        int j = n - 1;
        while (i >= 0 && j >= 0) {
            if (nums1[i] > nums2[j]) {
                nums1[len --] = nums1[i --];
            } else {
                nums1[len --] = nums2[j --];
            }
        }
        while (j >= 0)
            nums1[len --] = nums2[j --];
    }
}
```

### [189 旋转数组](https://leetcode-cn.com/problems/rotate-array/)

> 思路
>
> 1. 旋转 k 次，每次只右移一位
>    1. 反转三次
>    2. 每次找到下次要放入的位置，放入

```java
// 时间复杂度 O(n*k)
class Solution {
    public void rotate(int[] nums, int k) {
        if (nums == null || nums.length == 0)
            return; ;
        for (int i = 0; i < k; i++) {
            int tmp = nums[nums.length - 1];
            for (int j = nums.length - 1; j > 0; j--) {
                nums[j] = nums[j-1];
            }
            nums[0] = tmp;
        }
    }
}
```

```java
// 时间复杂度 O(n)
class Solution {
    public void rotate(int[] nums, int k) {
        if (nums == null || nums.length == 0)
            return ;
        k %= nums.length;
        reverse(nums, 0, nums.length - 1);
        reverse(nums, 0, k - 1);
        reverse(nums, k, nums.length - 1);
    }

    // 反转数组 [left,right]
    private void reverse(int[] nums, int left, int right) {
        if (left > right || right >= nums.length)
            return;
        for (int i = left, j = right; i < j; i ++, j --) {
            swap(nums, i, j);
        }
    }

    private void swap(int[] nums, int i, int j) {
        int temp = nums[i];
        nums[i] = nums[j];
        nums[j] = temp;
    }
}
```

```java
// 时间复杂度 O(n)
class Solution {
    public void rotate(int[] nums, int k) {
        int len  = nums.length;
        k = k % len;
        int count = 0;         // 记录交换位置的次数
        for(int start = 0; count < len; start++) {
            int curIndex = start;
            int last = nums[cur];
            do{
                int nextIndex = (curIndex + k) % len;
                int temp = nums[nextIndex];    // 保留被替换元素
                nums[nextIndex] = last;
                last = temp;
                curIndex = nextIndex;
                count++;
            }while(start != curIndex)  ;     // 循环暂停，回到起始位置

        }
    }
}
```

### [283 移动零](https://leetcode-cn.com/problems/move-zeroes/)

> 思路
>
> 1. 复制一个数组，修改原数组。顺序存放非零元素，剩余位补零
> 2. 设置两个指针，i、j。j 指向已知的最后一个非零元素
>    * num\[i\] != num\[j\]; 交换位置
>    * num\[i\] == nums\[j\]; 什么都不做

```java
    // 时间复杂度 O(n) 空间复杂度 O(1)
    public void moveZeroes(int[] nums) {
        if (nums == null || nums.length == 0)
            return ;
        int i = 0, j = -1;
        for (; i < nums.length; i++) {
            if (nums[i] != 0) {
                swap(nums, i, ++j);
            }
        }
    }
​
    private void swap(int[] nums, int i, int j) {
        int temp = nums[i];
        nums[i] = nums[j];
        nums[j] = temp;
    }
```

