
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * ClassName: Solution
 * Description:
 * date: 2019/10/31 15:05
 *
 * @author crwen
 * @create 2019-10-31-15:05
 * @since JDK 1.8
 */
public class Solution {
	public List<List<Integer>> generate(int numRows) {

		List<List<Integer>> lists = new ArrayList<>();
		if (numRows == 0)
			return lists;
		lists.add(Arrays.asList(1));
		if (numRows == 1)
			return lists;
		lists.add(Arrays.asList(1, 1));

		for (int i = 2; i < numRows; i++) {
			List<Integer> list = new ArrayList<>();
			list.add(1);

			for (int j = 1; j < i; j++) {
				list.add(lists.get(i-1).get(j-1) + lists.get(i-1).get(j));
			}
			list.add(1);
			lists.add(list);
		}

		return lists;
	}
}
