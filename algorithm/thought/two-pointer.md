# 双指针

## 左右指针

### [移动零](https://leetcode-cn.com/problems/move-zeroes/)

> 给定一个数组 `nums`，编写一个函数将所有 `0` 移动到数组的末尾，同时保持非零元素的相对顺序。

```java
class Solution {
    public void moveZeroes(int[] nums) {
        if (nums == null || nums.length == 0)
            return ;
        int lastNonZero = -1;
        for (int i = 0; i < nums.length; i++) {
            if (nums[i] != 0) {
                swap(nums, i, ++lastNonZero);
            }
        }
    }

    private void swap(int[] nums, int i, int j) {
        int tmp = nums[i];
        nums[i] = nums[j];
        nums[j] = tmp;
    }
}
```

### [盛最多水的容器](https://leetcode-cn.com/problems/container-with-most-water/)

> 给你 n 个非负整数 a1，a2，...，an，每个数代表坐标中的一个点 \(i, ai\) 。在坐标内画 n 条垂直线，垂直线 i 的两个端点分别为 \(i, ai\) 和 \(i, 0\)。找出其中的两条线，使得它们与 x 轴共同构成的容器可以容纳最多的水。

盛水的高度取决于两端较短的高度

```java
class Solution {
    public int maxArea(int[] height) {
        if (height == null || height.length == 0)
            return 0;
        int i = 0;
        int j = height.length - 1;
        int max = 0;
        while (i < j) {
            int water = Math.min(height[i], height[j]) * (j - i);
            max = Math.max(max, water);
            if (height[i] < height[j]) {
                i ++;
            } else {
                j --;
            }
        }
        return max;
    }
}
```

### [颜色分类](https://leetcode-cn.com/problems/sort-colors/)

> 给定一个包含红色、白色和蓝色，一共 n 个元素的数组，原地对它们进行排序，使得相同颜色的元素相邻，并按照红色、白色、蓝色顺序排列。我们使用整数 0、 1 和 2 分别表示红色、白色和蓝色。

利用三路快排的思想排序

```java
class Solution {
    public void sortColors(int[] nums) {
        if (nums == null || nums.length == 0)
            return ;
        int i = 0;  //[0...i) < 1
        int j = nums.length  - 1; // (j...size - 1] > 1
        for (int k = 0; k <= j; ) {
            if (nums[k] < 1) {
                swap(nums, k ++, i ++);
            } else if (nums[k] > 1) {
                swap(nums, k, j --);
            } else {
                k ++;
            }
        }
    }

    private void swap(int[] nums, int i, int j) {
        int tmp = nums[i];
        nums[i] = nums[j];
        nums[j] = tmp;
    }
}
```

### [三数之和](https://leetcode-cn.com/problems/3sum/)

> 给你一个包含 n 个整数的数组 nums，判断 nums 中是否存在三个元素 a，b，c ，使得 a + b + c = 0 ？请你找出所有满足条件且不重复的三元组。**注意**：答案中不可以包含重复的三元组。

```java
class Solution {
    public List<List<Integer>> threeSum(int[] nums) {
        if(nums == null || nums.length == 0)
            return new ArrayList<>();
        List<List<Integer>> res = new ArrayList<>();
        Arrays.sort(nums);
        for (int i = 0; i < nums.length - 2; i ++) {
            // 去重
            if (i > 0 && nums[i] == nums[i-1])
                continue;
            if (nums[i] > 0)
                break;
            int k = nums.length - 1;
            for (int j = i + 1; j < k; ) {
                int sum = nums[i] + nums[j] + nums[k];
                if (sum == 0) {
                    res.add(Arrays.asList(nums[i], nums[j], nums[k]));
                    j ++;
                    k --;
                    // 去重
                    while (j < k && nums[j] == nums[j - 1]) j ++;
                    while (j < k && nums[k] == nums[k+1]) k --;
                }else if (sum > 0) {
                    k --;
                } else if (sum < 0) {
                    j ++;
                }
            }
        } 

        return res;
    }
}
```

## 快慢指针

### [环形链表](https://leetcode-cn.com/problems/linked-list-cycle/)

> 给定一个链表，判断链表中是否有环。

```java
public class Solution {
    public boolean hasCycle(ListNode head) {
        if (head == null)
            return false;
        ListNode fast = head;
        ListNode slow = head;
        while (fast != null && fast.next != null) {
            fast = fast.next.next;
            slow = slow.next;
            if (fast == slow)
                return true;
        }
        return false;
    }
}
```

### [环形链表II](https://leetcode-cn.com/problems/linked-list-cycle-ii/)

> 给定一个链表，返回链表开始入环的第一个节点。 如果链表无环，则返回 `null`。

fast = 2 \* slow

fast = slow + n \* circle（快指针比慢指针多走 n 圈）

slow = n \* circle 所以快指针走了 2 n 个环的长度，慢指针走了 n 个环的长度，再走一个非环长度就可以到达环的入口

```java
public class Solution {
    public ListNode detectCycle(ListNode head) {
        if (head == null)
            return null;
        ListNode fast = head;
        ListNode slow = head;
        while (fast != null && fast.next != null) {
            fast = fast.next.next;
            slow = slow.next;
            if (fast == slow) {
                fast = head;
                while (fast != slow) {
                    fast = fast.next;
                    slow = slow.next;
                }
                return slow;
            }
        }

        return null;
    }
}
```

### [删除链表的倒数第 N 个节点](https://leetcode-cn.com/problems/remove-nth-node-from-end-of-list/)

> 给定一个链表，删除链表的倒数第 _n_ 个节点，并且返回链表的头结点。

```markup
给定一个链表: 1->2->3->4->5, 和 n = 2.
当删除了倒数第二个节点后，链表变为 1->2->3->5.
```

```java
class Solution {
    public ListNode removeNthFromEnd(ListNode head, int n) {
        if (head == null)
            return head;
        ListNode dummyHead = new ListNode(-1);
        dummyHead.next = head;
        ListNode fast = head;
        ListNode slow = dummyHead;
        for (int i = 0; i < n && fast != null; i ++) {
            fast = fast.next;
        }
        if (fast == null)
            return head.next;

        while (fast != null) {
            fast = fast.next;
            slow = slow.next;
        }
        ListNode deleteNode = slow.next;
        slow.next = deleteNode.next;
        deleteNode = null;

        return dummyHead.next;
    }
}
```

## 滑动窗口

