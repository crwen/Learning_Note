
class Solution {
    public int findMaxConsecutiveOnes(int[] nums) {
        int ans = 0;
        int j = 0;
        for (int i = 0; i < nums.length; i++) {
            if(nums[i] == 1) {
                ans = Math.max(ans, i - j + 1);
            } else {
                j = i + 1;
            }
        }

        return ans;
    }
}