
/**
 * ClassName: Solution
 * Description:
 * date: 2019/10/28 20:45
 *
 * @author crwen
 * @create 2019-10-28-20:45
 * @since JDK 1.8
 */
class Solution {
	public int dominantIndex(int[] nums) {
		int maxx = Integer.MIN_VALUE;
		int smaxx = maxx;
		int k = 0;

		for (int i = 0; i < nums.length; i++) {
			if (maxx < nums[i]) {
				smaxx = maxx;
				maxx = nums[i];
				k = i;
			} else {
				smaxx = Math.max(smaxx, nums[i]);
			}
		}
		if (maxx >= smaxx * 2)
			return k;
		return -1;
	}

	public static void main(String[] args) {
		int[] arr = {0,0,3,2};
		Solution res = new Solution();
		System.out.println(res.dominantIndex(arr));
	}
}