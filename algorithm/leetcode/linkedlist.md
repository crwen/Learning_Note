# 链表

### [2 两数相加](https://leetcode-cn.com/problems/add-two-numbers/)

> l1 l2 逆序给出
>
> input: \(2 -&gt; 4 -&gt; 3\) + \(5 -&gt; 6 -&gt; 4\)
>
> output: 7 -&gt; 0 -&gt; 8

```java
class Solution {
   public ListNode addTwoNumbers(ListNode l1, ListNode l2) {
        if (l1 == null && l2 == null)
            return null;

        ListNode head = new ListNode(0);
        ListNode node = head;
        int step = 0;
        while (l1 != null || l2 != null) {
            int a = l1 == null ? 0 : l1.val;
            int b = l2 == null ? 0 : l2.val;
            l1 = l1 == null ?  l1 : l1.next;
            l2 = l2 == null ? l2 : l2.next;
            int sum = a + b + step;
            node.next = new ListNode(sum % 10);
            step = sum / 10;
            node = node.next;
        }
        if (step != 0) {
            node.next = new ListNode(step);
        }

        return head.next;
    }
}
```

### [19 删除链表倒数第 N 个节点](https://leetcode-cn.com/problems/remove-nth-node-from-end-of-list/)

```java
class Solution {
    public ListNode removeNthFromEnd(ListNode head, int n) {
        if (head == null || n == 0)
            return head;

        ListNode dummyHead = new ListNode(-1);
        dummyHead.next = head;
        ListNode fast = head;
        ListNode slow = dummyHead;
        for (int i = 0; i  < n; i++) {
            fast = fast.next;
        }

        while (fast != null) {
            fast = fast.next;
            slow = slow.next;
        }

        ListNode next = slow.next;
        slow.next = next.next;
        next = null;
        return dummyHead.next;
    }
}
```

### [21 合并两个有序链表](https://leetcode-cn.com/problems/merge-two-sorted-lists/)

**迭代**

```java
class Solution {
    public ListNode mergeTwoLists(ListNode l1, ListNode l2) {
        if (l1 == null) return l2;
        if (l2 == null) return l1;

        ListNode dummyHead = new ListNode(-1);
        ListNode node = dummyHead;
        while (l1 != null && l2 != null) {
            if (l1.val < l2.val) {
                node.next = l1;
                l1 = l1.next;
            } else {
                node.next = l2;
                l2 = l2.next;
            }
            node = node.next;
        }
        node.next = l1 == null ? l2 : l1;

        return dummyHead.next;
    }
}
```

**递归**

```java
class Solution {
    public ListNode mergeTwoLists(ListNode l1, ListNode l2) {
        if (l1 == null) return l2;
        if (l2 == null) return l1;

        if (l1.val < l2.val) {
            l1.next = mergeTwoLists(l1.next, l2);
            return l1;
        } else {
            l2.next = mergeTwoLists(l1, l2.next);
            return l2;
        }
    }
}
```

### [24 两两交换链表中的节点](https://leetcode-cn.com/problems/swap-nodes-in-pairs/)

```java
class Solution {
     public ListNode swapPairs(ListNode head) {
        if(head == null || head.next == null){
            return head;
        }
        ListNode dummy = new ListNode(-1);
        dummy.next = head;
        ListNode pre = dummy;
        while (pre.next != null && pre.next.next != null) {
            ListNode node1 = pre.next;
            ListNode node2 = node1.next;
            ListNode next = node2.next;

            pre.next = node2;
            node2.next = node1;
            node1.next = next;
            pre = node1;
        }
        return dummy.next;
    }
}
```

### [25 K 个一组翻转链表](https://leetcode-cn.com/problems/reverse-nodes-in-k-group/)

```java
class Solution {
    public ListNode reverseKGroup(ListNode head, int k) {
        if (head == null || head.next == null || k == 1)
            return head;

        ListNode dummyHead = new ListNode(-1);
        dummyHead.next = head;
        ListNode cur = dummyHead;
        ListNode pre = dummyHead;
        while (cur != null) {
            for (int i = 0; i < k && cur != null; i++) {
                cur = cur.next;
            }
            if (cur == null)
                break;
            // 断开链表
            ListNode next = cur.next;
            cur.next = null;
            // 反转
            ListNode reverseHead = pre.next;
            ListNode node = reverse(reverseHead);
            // 连接
            pre.next = node;
            reverseHead.next = next; // 此时 reverseHead 已经成为反转后节点的尾节点
            pre = reverseHead;
            cur = reverseHead;
        }
        return dummyHead.next;
    }

    public ListNode reverse(ListNode head) {
        if (head == null || head.next == null)
            return head;
        ListNode next = head.next;
        ListNode retNode = reverse(next);
        head.next = null;
        next.next = head;
        return retNode;
    }
}
```

### [83 删除链表中重复元素](https://leetcode-cn.com/problems/remove-duplicates-from-sorted-list/submissions/)

```java
class Solution {
    public ListNode deleteDuplicates(ListNode head) {
        if (head == null || head.next == null) 
            return head;
        ListNode pre = head;
        ListNode cur = head.next;
        while (cur != null) {
            if (pre.val == cur.val) {
                pre.next = cur.next;
            } else {
                pre = cur;
            }
            cur = cur.next;
        }
        return head;
    }
}
```

### [86 分隔链表](https://leetcode-cn.com/problems/partition-list/)

> 给定特定值 x，使所有小于 x 的节点都在 大于或等于 x 的节点之前 要求两个分区中每个节点的初始相对位置
>
> ```java
> class Solution {
>     public ListNode partition(ListNode head, int x) {
>         if (head == null || head.next == null)
>             return head;
>         ListNode dummy1 = new ListNode(-1);
>         ListNode dummy2 = new ListNode(-1);
>         ListNode node1 = dummy1;
>         ListNode node2 = dummy2;
>         while (head != null) {
>             if (head.val < x) {
>                 node1.next = new ListNode(head.val);
>                 node1 = node1.next;
>             } else {
>                 node2.next = new ListNode(head.val);
>                 node2 = node2.next;
>             }
>             head = head.next;
>         }
>         node1.next = dummy2.next;
>         return dummy1.next;
>     }
> }
> ```

### [92 反转链表 ii](https://leetcode-cn.com/problems/reverse-linked-list-ii/)

> 反转 m~n 之间的节点

```java
class Solution {
    public ListNode reverseBetween(ListNode head, int m, int n) {
        if (m >= n || head == null)
            return head;
        ListNode dummy = new ListNode(-1);
        dummy.next = head;
        ListNode cur = head;
        ListNode prev = dummy;
        ListNode end = null;
        for (int i = 1; i < m ; i ++) {
            prev = cur;
            cur = cur.next;
        }

        for (int i = m; i < n; i++) {
            ListNode removed = cur.next;
            cur.next = removed.next;
            // 头插法插到 prev 后面
            removed.next = prev.next;
            prev.next = removed;
        }
        return dummy.next;
    }
}
```

### [141 环形链表](https://leetcode-cn.com/problems/linked-list-cycle/)

```java
public class Solution {
    public boolean hasCycle(ListNode head) {
        if (head == null)
            return false;
        ListNode fast = head;
        ListNode slow = head;
        while (fast.next != null && fast.next.next != null) {
            fast = fast.next.next;
            slow = slow.next;
            if (fast == slow)
                return true;
        }
        return false;
    }
}
```

### [142 环形链表 II](https://leetcode-cn.com/problems/linked-list-cycle-ii/)

```java
public class Solution {
    public ListNode detectCycle(ListNode head) {
        if (head == null)
            return head;
        ListNode fast = head;
        ListNode slow = head;
        while (fast != null && fast.next != null) {
            fast = fast.next.next;
            slow = slow.next;
            if (fast == slow)
                break;
        }
        if (fast == null || fast.next == null)
            return null;
        fast = head;

        while (fast != slow) {
            fast = fast.next;
            slow = slow.next;
        }
        return fast;
    }
}
```

### [147 对链表进行插入排序](https://leetcode-cn.com/problems/insertion-sort-list/)

```java
class Solution {
    public ListNode insertionSortList(ListNode head) {
        if (head == null || head.next == null) {
            return head;
        }

        ListNode dummy = new ListNode(-1);
        dummy.next = head;
        ListNode cur = head.next;
        ListNode sorted = head;
        ListNode pre = dummy;
        while (cur != null) {
            if (cur.val >= sorted.val) {
                cur = cur.next;
                sorted = sorted.next;
            } else {
                ListNode after = cur.next;
                sorted.next = after;
                // 找到已排序部分第一个大于 cur.val 的数的前驱节点
                while (pre.next.val <= cur.val) {
                    pre = pre.next;
                }

                ListNode next = pre.next;
                pre.next = cur;
                cur.next = next;
                pre = dummy;
                cur = after;
            }
        }
        return dummy.next;
    }
}
```

### [160 相交链表](https://leetcode-cn.com/problems/intersection-of-two-linked-lists/)

```java
public class Solution {
    public ListNode getIntersectionNode(ListNode headA, ListNode headB) {
        if (headA == null || headB == null)
            return null;
        ListNode l1 = headA;
        ListNode l2 = headB;

        while (l1 != l2) {
            l1 = l1 == null ? headB : l1.next;
            l2 = l2 == null ? headA : l2.next;
        }

        return l1;
    }
}
```

### [206 反转链表](https://leetcode-cn.com/problems/reverse-linked-list/)

**递归**

```java
class Solution {
    public ListNode reverseList(ListNode head) {
        if (head == null || head.next == null)
            return head;
        ListNode next = head.next;
        ListNode newHead = reverseList(next);
        head.next = null;
        next.next = head;
        return newHead;
    }
}
```

**迭代**

```java
class Solution {
    public ListNode reverseList(ListNode head) {
        if (head == null || head.next == null)
            return head;
        ListNode pre = null;
        ListNode cur = head;
        ListNode next = head.next;

        // pre->cur->next
        // pre<-cur next
        while (cur != null) {
            next = cur.next;
            cur.next = pre;
            pre = cur;
            cur = next;
        }
        return pre;
    }
}
```

### [234 回文链表](https://leetcode-cn.com/problems/palindrome-linked-list/)

```java
class Solution {
    public boolean isPalindrome(ListNode head) {
        if (head == null || head.next == null)
            return true;
        ListNode fast = head;
        ListNode slow = head;
        while (fast != null && fast.next != null) {
            fast = fast.next.next;
            slow = slow.next;
        }
        ListNode node =  reverse(slow);
        fast = head;
        
        while (fast != null && node != null) {
            if (fast.val != node.val)
                return false;
            fast = fast.next;
            node = node.next;
        }
        return true;
    }
    
    private ListNode reverse(ListNode head) {
        if (head == null || head.next == null)
            return head;
        ListNode next = head.next;
        ListNode retNode = reverse(next);
        next.next = head;
        head.next = null;
        return retNode;
    }
}
```

#### [328 奇偶链表](https://leetcode-cn.com/problems/odd-even-linked-list/)

> 给定一个单链表，把奇数节点和偶数节点分别排在一起
>
>  输入: 1-&gt;2-&gt;3-&gt;4-&gt;5-&gt;NULL 
>
> 输出: 1-&gt;3-&gt;5-&gt;2-&gt;4-&gt;NULL

```java
class Solution {
    public ListNode oddEvenList(ListNode head) {
        if (head == null || head.next == null)
            return head;
        ListNode odd = head;
        ListNode even = head.next;
        ListNode evenHead = even;
        while (even != null && even.next != null) {
            odd.next = even.next;
            odd = even.next;
            even.next = odd.next;
            even = odd.next;
        }
        odd.next = evenHead;
        return head;
    }
}
```

