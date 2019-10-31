
import java.util.Arrays;

/**
 * ClassName: Solution
 * Description:
 * date: 2019/10/30 21:28
 *
 * @author crwen
 * @create 2019-10-30-21:28
 * @since JDK 1.8
 */
public class Solution {
	public int[] findDiagonalOrder(int[][] matrix) {
		int m = matrix.length;
		if (m == 0)
			return new int[0];
		int n = matrix[0].length;
		if (n == 0)
			return matrix[0];
		int[] res = new int[m * n];
		int x = 0, y = 0;

		for (int i = 0; i < m * n; i++) {
			if (x >= 0 && y >= 0 && x < m && y < n)
				res[i] = matrix[x][y];
			if (y == n - 1 && (x + y) % 2 == 0) {
				x++;
			} else if (x == m - 1 && (x + y) % 2 == 1) {
				y++;
			} else if ((x + y) % 2 == 0) {
				if (x == 0) {
					y++;
				} else {
					x--;
					y++;
				}
			} else if ((x + y) % 2 == 1 ) {
				if (y == 0) {
					x++;
				} else {
					x++;
					y--;
				}
			}
		}

		return res;
	}

	public static void main(String[] args) {
		Solution res = new Solution();
		int[][] matrix = { { 1, 2, 3 ,4}, { 5, 6,7,8 },{ 9,10,11,12 }, {13,14,15,16}};
		//int[][] matrix = {{2,3}};
		//int[][] matrix = {{2, 5}, {8, 4}, {0, -1}};
		//int[][] matrix = {};
		//int[][] matrix = {{}};
		//System.out.println(matrix.length);
		//System.out.println(matrix[0].length);
		int[] arr = res.findDiagonalOrder(matrix);
		System.out.println(Arrays.toString(arr));
	}


}