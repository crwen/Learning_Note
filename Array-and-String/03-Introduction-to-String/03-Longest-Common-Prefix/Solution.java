/**
 * ClassName: Solution
 * Description:
 * date: 2019/12/5 20:48
 *
 * @author crwen
 * @create 2019-12-05-20:48
 * @since JDK 1.8
 */
public class Solution {

	public String longestCommonPrefix(String[] strs) {
		if (strs.length == 0 || strs[0].length() == 0) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < strs[0].length(); i++) {
			char temp = strs[0].charAt(i);
			boolean same = true;
			for (int j = 1; j < strs.length; j++) {
				if(strs[j].length() <= i || strs[j].charAt(i) != temp) {
					// 如果长度前缀长度与最小单词长度相等， 或 有一个字符不相等，退出
					same = false;
					break;
				}
			}
			if(same) {
				sb.append(temp);
			}else {
				break;
			}
		}
		return sb.toString();
	}
}