
class Solution {
    public int minSubArrayLen(int s, int[] nums) {
        int[] preSum = new int[nums.length + 1];
        preSum[0] = 0;
        for (int i = 0; i < nums.length; i++) {
            preSum[i+1] = preSum[i] + nums[i];
        }

        int j = 0;
        int ans = Integer.MAX_VALUE;
        for (int i = 0; i < preSum.length;) {
            if (preSum[i] - preSum[j] >= s) {
                ans = Math.min(ans, i - j);
                j++;
            } else {
                i++;
            }
        }

        return ans > nums.length ? 0 : ans;
    }
}