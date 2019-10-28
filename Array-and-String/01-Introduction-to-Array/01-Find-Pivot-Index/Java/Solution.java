
/**
 * ClassName: Solution
 * Description:
 * date: 2019/10/28 20:10
 *
 * @author crwen
 * @create 2019-10-28-20:10
 * @since JDK 1.8
 */
class Solution {
	public int pivotIndex(int[] nums) {

		int[] sum = new int[nums.length + 1];
		sum[0] = 0;
		for (int i = 0; i < nums.length; i++)
			sum[i+1] = sum[i] + nums[i];

		for (int i = 1; i < sum.length; i++) {
			int left = sum[i - 1];
			int right = sum[sum.length - 1] - sum[i];
			if (left == right)
				return i - 1;
		}

		return  -1;
	}

	public static void main(String[] args) {
		Solution res = new Solution();
		int[] arr = {-1,-1,0,1,1,0};
		System.out.println(res.pivotIndex(arr));
	}
}
