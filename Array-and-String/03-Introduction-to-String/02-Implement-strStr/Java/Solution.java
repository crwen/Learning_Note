
/**
 * ClassName: Solution
 * Description:
 * date: 2019/10/31 16:24
 *
 * @author crwen
 * @create 2019-10-31-16:24
 * @since JDK 1.8
 */
public class Solution {
	public int strStr(String haystack, String needle) {
		if (needle.length() == 0)
			return 0;
		return haystack.indexOf(needle);
	}

	public static void main(String[] args) {
		String haystack = "sajgk";
		String needle = "";
		Solution res = new Solution();
		System.out.println(res.strStr(haystack, needle));
	}
}
