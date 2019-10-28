
import java.util.Arrays;

/**
 * ClassName: Solution
 * Description:
 * date: 2019/10/28 21:15
 *
 * @author crwen
 * @create 2019-10-28-21:15
 * @since JDK 1.8
 */
class Solution {
	public int[] plusOne(int[] digits) {
		int N = digits.length;
		int[] arr = new int[N + 1];
		for (int i = 0; i < N; i++) {
			arr[i] = digits[N - 1 - i];
		}

		int index = 0;
		arr[0] += 1;
		int cur = arr[0];
		while (cur > 9 && index < arr.length) {
			arr[index] = cur % 10;
			arr[index + 1] += 1;
			cur = arr[++index];
		}

		N = arr.length;
		while (arr[N - 1] == 0)
			N--;
		int[] res = new int[N];
		for (int i = 0; i < N; i++)
			res[i] = arr[N - 1 - i];

		return res;
	}

	public static void main(String[] args) {
		int[] nums = {9, 9, 9, 9 };
		Solution res = new Solution();
		int[] arr = res.plusOne(nums);
		System.out.println(Arrays.toString(arr));
	}
}