
/**
 * ClassName: Solution
 * Description:
 * date: 2019/10/31 15:41
 *
 * @author crwen
 * @create 2019-10-31-15:41
 * @since JDK 1.8
 */
public class Solution {

	public String addBinary(String a, String b) {
		if (a == null || b == null || a.length() == 0 || b.length() == 0)
			return "";

		int lena = a.length();
		int lenb = b.length();
		int maxlen = lena > lenb ? lena : lenb;
		int step = 0;
		StringBuilder res = new StringBuilder();
		for (int i = lena - 1, j = lenb - 1; maxlen-- > 0; i--, j--) {
			int sum = 0;
			if (i < 0 ) {
				sum = (b.charAt(j) - '0') + step;
			} else if (j < 0) {
				sum = (a.charAt(i) - '0') + step;
			} else {
				sum = (a.charAt(i) - '0') + (b.charAt(j) - '0') + step;
			}

			step = sum / 2;
			sum = sum % 2;
			res.append(sum +"");
		}
		if (step == 1)
			res.append(step);
		return res.reverse().toString();
	}

	public static void main(String[] args) {
		String a = "10100000100100110110010000010101111011011001101110111111111101000000101111001110001111100001101";
		String b = "110101001011101110001111100110001010100001101011101010000011011011001011101111001100000011011110011";
		Solution res = new Solution();
		String s = res.addBinary(a, b);
		//System.out.println(s.equals("110111101100010011000101110110100000011101000101011001000011011000001100011110011010010011000000000"));

	}
}
